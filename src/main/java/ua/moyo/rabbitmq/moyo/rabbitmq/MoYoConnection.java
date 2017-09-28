package ua.moyo.rabbitmq.moyo.rabbitmq;

import ua.moyo.rabbitmq.model.Database;
import ua.moyo.rabbitmq.model.DatabaseTube;
import ua.moyo.rabbitmq.moyo.Odines.OdinesComConnection;
import ua.moyo.rabbitmq.moyo.Odines.OdinesComConnector;
import ua.moyo.rabbitmq.moyo.Odines.OdinesConnectionSettings;
import com.rabbitmq.client.Channel;
import org.jawin.COMException;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static ua.moyo.rabbitmq.moyo.rabbitmq.MoYo.connectionTimeoutSec;

/**
 * Created by JLD on 28.07.2017.
 */
public class MoYoConnection implements Runnable {

    String db;
    OdinesConnectionSettings shop;
    Database database;
    boolean alive = true;

    public MoYoConnection(Database database, String db) {
        this.database = database;
        if (!database.isNotShop()) {
            this.db = db;
            shop = new OdinesConnectionSettings(database, db, MoYo.getShopName(), MoYo.getShopUsername(), MoYo.getShopPassword());
        } else {
            this.db = database.getPresentation();
            shop = new OdinesConnectionSettings(database, database.getIp(), database.getBase(), database.getUser(), database.getPassword());
        }
    }


    public void run() {



        try {
            Channel channel = MoYo.connection.createChannel();

            MoYo.channels.put(database, channel);
            //channel.basicQos(0, false);
            //channel.basicQos(1, false);
            //channel.basicQos(MoYo.connectionPerDatabase, true);
            channel.basicQos(database.getMaxConnection(), true);
            OdinesComConnector odinesComConnector = OdinesComConnector.getConnector();


            for (int i = 1; i <= database.getMaxConnection(); i++) {
                if (!channel.isOpen()){return;}
                final int finalI = i;

                try {
                    Future future = MoYo.executor.submit(() -> connect1C(channel, odinesComConnector, finalI) );
                    future.get(connectionTimeoutSec, TimeUnit.SECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                    MoYo.logInfo("MoYoConnection->run->Exception->Future",
                            "Не удалось подключиться к базе -"+database.getName()+"- за заданное время");
                    alive = false; channelClose(channel); break;
                }

            }

        }

        catch(COMException ex){
            System.out.println(ex.getMessage());
            MoYo.logInfo("MoYoConnection->run->COMException", ex.getMessage());
        }

        catch (IOException ioex){
            System.out.println(ioex.toString());
            MoYo.logInfo("MoYoConnection->run->IOException", ioex.getMessage());
        }
    }

    private void channelClose(Channel channel) {
        try {
            channel.close();
        } catch (TimeoutException e1) {
            e1.printStackTrace();
            MoYo.logInfo("MoYoConnection->channelClose", e1.getMessage());
        }
        catch (IOException ioex){
            System.out.println(ioex.toString());
            MoYo.logInfo("MoYoConnection->channelClose", ioex.getMessage());
        }
    }

    private void connect1C(Channel channel, OdinesComConnector odinesComConnector, int i) {
        OdinesComConnection shopConnection = new OdinesComConnection(odinesComConnector, shop, true);

        if (!alive){
            updateTubesFail();
            MoYo.executor.submit(() -> shopConnection.close());
            return;
        }

        String dbConnection = db+"_"+i;
        if (shopConnection.isConnected()) {
            updateTubesSuccess();
            MoYo.OdiesComConnectionPool.put(dbConnection, shopConnection);
            MoYo.executor.execute(new MoYoConsumer(dbConnection, channel));
        }
        else {
            updateTubesFail();
            if(channel.isOpen()){
                MoYo.channels.remove(database);
                channelClose(channel);
            }
        }
    }

    private void updateTubesFail() {
        if(MoYo.databaseTubesFail.containsKey(database)){
            DatabaseTube databaseTube = MoYo.databaseTubesFail.get(database);
            databaseTube.setConnections(databaseTube.getConnections() +1);
        }
        else{
            MoYo.databaseTubesFail.put(database, new DatabaseTube(database,1));
        }
    }

    private void updateTubesSuccess() {
        if(MoYo.databaseTubes.containsKey(database)){
            DatabaseTube databaseTube = MoYo.databaseTubes.get(database);
            databaseTube.setConnections(databaseTube.getConnections() +1);
        }
        else{
            MoYo.databaseTubes.put(database, new DatabaseTube(database,1));
        }
    }


}
