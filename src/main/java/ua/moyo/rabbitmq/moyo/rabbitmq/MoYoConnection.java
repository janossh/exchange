package ua.moyo.rabbitmq.moyo.rabbitmq;

import ua.moyo.rabbitmq.model.Database;
import ua.moyo.rabbitmq.model.DatabaseTube;
import ua.moyo.rabbitmq.moyo.Odines.OdinesComConnection;
import ua.moyo.rabbitmq.moyo.Odines.OdinesComConnector;
import ua.moyo.rabbitmq.moyo.Odines.OdinesConnectionSettings;
import com.rabbitmq.client.Channel;
import org.jawin.COMException;
import ua.moyo.rabbitmq.moyo.Service.MoYoService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

//            OdinesComConnector odinesComConnector = database.getIp().equals("10.0.30.28")||database.getIp().equals("10.0.30.33")
//                    ? OdinesComConnector.getConnectorOnline(): OdinesComConnector.getConnector();

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
                            "Не удалось подключиться к базе -\"+database.getName()+\"- за заданное время");
                    alive = false;
                    channelClose(channel);
                    MoYo.getMoYoService().updateTubesFail(database);
                    break;
                }

            }

        }

        catch(COMException ex){
            try {
                MoYo.logInfo("MoYoConnection->run->COMException", new String(ex.getMessage().getBytes("ISO-8859-1"),"windows-1251"));
            }
            catch (UnsupportedEncodingException uee){
                MoYo.logInfo("MoYoConnection->run->UnsupportedEncodingException", uee.getMessage());
            }


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
            MoYo.executor.submit(() -> shopConnection.close());
            return;
        }

        String dbConnection = db+"_"+i;
        if (shopConnection.isConnected()) {
            MoYo.getMoYoService().updateTubesSuccess(database);
            MoYo.OdiesComConnectionPool.put(dbConnection, shopConnection);
            MoYo.executor.execute(new MoYoConsumer(dbConnection, channel));
        }
        else {
            MoYo.getMoYoService().updateTubesFail(database);
            if(channel.isOpen()){
                MoYo.channels.remove(database);
                channelClose(channel);
            }
        }
    }




}
