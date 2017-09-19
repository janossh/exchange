package ua.moyo.rabbitmq.moyo.rabbitmq;

import ua.moyo.rabbitmq.model.Database;
import ua.moyo.rabbitmq.moyo.Odines.OdinesComConnection;
import ua.moyo.rabbitmq.moyo.Odines.OdinesComConnector;
import ua.moyo.rabbitmq.moyo.Odines.OdinesConnectionSettings;
import com.rabbitmq.client.Channel;
import org.jawin.COMException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by JLD on 28.07.2017.
 */
public class MoYoConnection implements Runnable {

    String db;
    OdinesConnectionSettings shop;
    Database database;

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
                OdinesComConnection shopConnection = new OdinesComConnection(odinesComConnector, shop, true);
                String dbConnection = db+"_"+i;
                if (shopConnection.isConnected()) {
                    MoYo.OdiesComConnectionPool.put(dbConnection, shopConnection);
                    MoYo.executor.execute(new MoYoConsumer(dbConnection, channel));
                }
                else {
                    if(channel.isOpen()){
                        MoYo.channels.remove(database);
                        try {
                            channel.close();
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                            MoYo.logInfo("MoYoConnection->run->close channal because have no 1C connection", database.getPresentation()+"");
                        }
                    }
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
}
