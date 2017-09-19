package ua.moyo.rabbitmq.moyo.Service.ServiceImpl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import ua.moyo.rabbitmq.model.Database;
import ua.moyo.rabbitmq.model.DatabaseTube;
import ua.moyo.rabbitmq.model.Logger;
import ua.moyo.rabbitmq.repository.DatabaseRepository;
import ua.moyo.rabbitmq.repository.LogRepository;
import ua.moyo.rabbitmq.moyo.Enums.LogPriority;
import ua.moyo.rabbitmq.moyo.Odines.OdinesComConnection;
import ua.moyo.rabbitmq.moyo.Service.MoYoService;
import ua.moyo.rabbitmq.moyo.rabbitmq.MoYo;
import ua.moyo.rabbitmq.moyo.rabbitmq.MoYoConnection;
import ua.moyo.rabbitmq.view.MoYoHomeView;
import net.anotheria.moskito.aop.annotation.Monitor;

import org.jawin.COMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static ua.moyo.rabbitmq.moyo.rabbitmq.MoYo.databaseTubesFail;
import static ua.moyo.rabbitmq.moyo.rabbitmq.MoYo.shopPool;

@Service
@Monitor(producerId = "service", subsystem = "MoYo", category = "-")
public class MoYoServiceImpl implements MoYoService {

    @Autowired
    DatabaseRepository databaseRepository;

    @Autowired
    LogRepository logRepository;

    public void connectionsClose(){

        MoYoHomeView.isBaseConnected = false;
        databaseTubesFail.clear();

        MoYo.channels.forEach((database, channel) -> {

            try {
                if (channel!=null&&channel.isOpen()){
                channel.close();}
            } catch (IOException e) {
                e.printStackTrace();
                MoYo.logInfo("MoYoService->connectionsClose->Channel-IOException", e.getMessage());
            } catch (TimeoutException e) {
                e.printStackTrace();
                MoYo.logInfo("MoYoService->connectionsClose->Channel-TimeoutException", e.getMessage());
            }

        });
        //to avoid java.util.ConcurrentModificationException: null
        //channelsForDelete.forEach(database -> MoYo.channels.remove(database));
        MoYo.channels.clear();


        List<OdinesComConnection> connectionsForDelete = new ArrayList<>();
        for (OdinesComConnection connection : MoYo.OdiesComConnectionPool.values()) {
            try {
                connection.close();
                connectionsForDelete.add(connection);
                MoYo.databaseTubes.remove(connection.getSettings().getDatabase1C());
            }
            catch (Exception ex){
                System.out.println(ex.getMessage());
                MoYo.logInfo("MoYoService->connectionsClose->OdinesComConnection", ex.getMessage());
            }
        }
        //to avoid java.util.ConcurrentModificationException: null
        connectionsForDelete.forEach(connection -> MoYo.OdiesComConnectionPool.remove(connection));

        /* when connect 1 database we need connection
        try {
            MoYo.connection.close();
        } catch (IOException e) {
            e.printStackTrace();
            MoYo.logInfo("MoYoService->connectionsClose-> MoYo.connection-IOException", e.getMessage());
        }
        */
    }

    @Override
    public void deleteMessages() {
        String messageText = "";
        try {

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            databaseRepository.findAll().forEach(database -> {
                try {
                    channel.queuePurge(database.getPresentation());
                } catch (IOException e) {
                    e.printStackTrace();
                    MoYo.logInfo("MoYoService->deleteMessages", e.getMessage());
                }
            });
            messageText = "Сообщения успешно удалены";

        }catch (Exception ex){
            messageText = "Произошла ошибка, сообщения не могут быть удалены";
            MoYo.logInfo("MoYoService->deleteMessages", ex.getMessage());
        }
        MoYo.showNotification(messageText);

    }


    @Override
    public synchronized void log(String where, String description, LogPriority priority) {
        Logger logger = new Logger(where, description, priority);
        logRepository.saveAndFlush(logger);
    }

    @Override
    public synchronized void log(String where, String description, boolean show) {
        Logger logger = new Logger(where, description,  show);
        logRepository.saveAndFlush(logger);
    }

    @Override
    public void logClean() {
        logRepository.deleteAll();
    }

    @Override
    public Integer getTotalDatabaseConnectionForConnect() {
        return databaseRepository.findAll().stream().filter(Database::isActive).mapToInt(Database::getMaxConnection).sum();
    }

    @Override
    public Integer getTotalDatabaseConnectionConnected() {
        return MoYo.getDatabaseTubes().values().stream().mapToInt(DatabaseTube::getConnections).sum();
    }

    @Override
    public Integer getTotalDatabaseConnectionConnectedFail() {
        return MoYo.getDatabaseTubesFail().values().stream().mapToInt(DatabaseTube::getConnections).sum();
    }

    @Override
    public void disconnectDB(Database database, boolean inThread ){
        if (inThread){
            MoYo.executor.execute(() -> disconnectDB(database));
        }
        else {disconnectDB(database);}
    }

    public void disconnectDB(Database database){

        //sometimes we cant close connection or it has been closed,
        // but we need to reconnect to db, first close rabbitMQ Connection
        closeChannelsConnections(database);
        close1CConnections(database);

    }

    @Override
    public void connectDB(Database database) {
        deleteDbInDatabaseTubesFail(database);
        MoYo.executor.execute(new MoYoConnection(database, database.getIp()));
    }

    private void deleteDbInDatabaseTubesFail(Database database){
        if (MoYo.databaseTubesFail.containsKey(database)){MoYo.databaseTubesFail.remove(database);}
    }

    @Override
    public void connectAllDB() {
        initShopPool();
        MoYoHomeView.isBaseConnected = true;
        shopPool.forEach(this::connectDB);
    }

    public synchronized void initShopPool() {
        databaseTubesFail.clear();
        shopPool.clear();
        List<Database> databases = databaseRepository.findAll();
        databases.forEach(database -> {
            if (database.isActive()){
                shopPool.add(database);
            }

        });


    }

    private void close1CConnections(Database database){

        MoYo.OdiesComConnectionPool.values().forEach(connection -> {
            if (connection.getSettings().getDatabase1C().equals(database)){
                //sometimes we cant close connection or it has been closed,
                // but we need to reconnect to db, first delete from hashmaps
                MoYo.databaseTubes.remove(connection.getSettings().getDatabase1C());
                try {
                    connection.close();
                } catch (COMException e) {
                    e.printStackTrace();
                    MoYo.logInfo("MoYoService->close1CConnections-COMException", e.getMessage());
                }
                MoYo.OdiesComConnectionPool.remove(connection);
            }
        });
    }

    private void closeChannelsConnections(Database database){
        try {
            Channel channel = MoYo.channels.get(database); if(channel==null)return; if (!channel.isOpen()){return;}
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
            MoYo.logInfo("MoYoService->closeChannelsConnections->IOException", e.getMessage());
        } catch (TimeoutException e) {
            e.printStackTrace();
            MoYo.logInfo("MoYoService->closeChannelsConnections->TimeoutException", e.getMessage());
        }
    }


    @Override
    public void controlPoolConnections() {

        String controlPoolConnections = "MoYoService->ControlPoolConnections";
        log(controlPoolConnections,"start...",false);

        if (!MoYoHomeView.isBaseConnected) return;
        log(controlPoolConnections,"whenBaseConnected "+MoYo.whenBaseConnected, false);
        if (MoYo.whenBaseConnected==null) return;
        long offsetMinutes = MoYo.whenBaseConnected.until(LocalDateTime.now(), ChronoUnit.MINUTES);
        log(controlPoolConnections,"offsetMinutes "+offsetMinutes, false);
        if (offsetMinutes < 2) return;

        log(controlPoolConnections,"check db connections...", false);
        String titleAddForDelete = " add for delete ";

        List<Database> forDelete = new ArrayList<>();

        MoYo.databaseTubes.forEach((database, databaseTube) -> {
            if (!MoYo.channels.containsKey(database)){forDelete.add(database);}
        });

        MoYo.databaseTubes.forEach((database, databaseTube) -> {

            if (!MoYoHomeView.isBaseConnected) return;

            MoYo.OdiesComConnectionPool.forEach((s, odinesComConnection) -> {

                if (odinesComConnection.getSettings().getDatabase1C()==database) {

                    if (!odinesComConnection.isConnected()) {
                        forDelete.add(database);
                        log(controlPoolConnections, database + titleAddForDelete+": not connected", false);
                    }

                    try {
                        boolean isOnline = odinesComConnection.isOnline();
                        if (!isOnline){
                            if (!forDelete.contains(database)) {
                                forDelete.add(database);
                                log(controlPoolConnections, database + titleAddForDelete+": is offline", false);
                            }
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        if (!forDelete.contains(database)) {forDelete.add(database);}
                        log(controlPoolConnections+"->UnsupportedEncodingException", e.getMessage(), false);
                        log(controlPoolConnections, database + titleAddForDelete+": UnsupportedEncodingException", false);

                    }


                }

            });


        });
        log(controlPoolConnections,"...check db connections", false);

        log(controlPoolConnections,"disconnectDB...", false);
        forDelete.forEach(database -> {
            log(controlPoolConnections,"disconnect database... "+database, false);
            disconnectDB(database, false);
            log(controlPoolConnections,"...disconnect database "+database, false);
        });
        log(controlPoolConnections,"...disconnectDB", false);

        log(controlPoolConnections,"connectDB...", false);
        initShopPool();
        shopPool.forEach(database ->
        {

            if (!MoYoHomeView.isBaseConnected) return;

            if (MoYo.databaseTubes.containsKey(database)) {
                DatabaseTube databaseTube = MoYo.databaseTubes.get(database);
                Integer connectionQuantity = databaseTube.getConnections();
                if (connectionQuantity < database.getMaxConnection()) {
                    log(controlPoolConnections,"connectDB..."+database, false);
                    connectDB(database);
                    log(controlPoolConnections,"...connectDB"+database, false);

                }
            } else {
                log(controlPoolConnections,"connectDB..."+database, false);
                connectDB(database);
                log(controlPoolConnections,"...connectDB"+database, false);
            }

        });
        log(controlPoolConnections,"...connectDB", false);

        log(controlPoolConnections,"...end", false);
    }
}
