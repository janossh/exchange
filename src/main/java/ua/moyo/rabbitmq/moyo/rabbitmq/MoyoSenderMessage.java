package ua.moyo.rabbitmq.moyo.rabbitmq;

import ua.moyo.rabbitmq.model.Database;
import ua.moyo.rabbitmq.moyo.Odines.OdinesComConnection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Created by JLD on 28.07.2017.
 */
public class MoyoSenderMessage implements Runnable {

    OdinesComConnection shopConnection;
    String message;
    Envelope envelope;
    Channel channel;
    String db;
    Database database;
    LocalDateTime dateIn;

    public MoyoSenderMessage(OdinesComConnection shopConnection, String message, Envelope envelope, Channel channel, String db, LocalDateTime dateIn) {
        this.shopConnection = shopConnection;
        this.message = message;
        this.envelope = envelope;
        this.channel = channel;
        this.db = db;
        this.database = shopConnection.getSettings().getDatabase1C();
        this.dateIn = dateIn;
    }

    @Override
    public void run() {
        sendMessage();
    }

    public void sendMessage(){

        if(channel==null)return; if (!channel.isOpen()){return;}
        Boolean delivered = false;

        try {
            delivered = shopConnection.sendMessage(message);
            Long messageTag = envelope.getDeliveryTag();
            if(delivered){
                if (channel.isOpen()){
                channel.basicAck(messageTag, false);
                MoYo.sendStatisticPlus(dateIn);
                LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
                MoYo.sendStatisticTimePlus(dateIn, ldt);
                MoYo.sendStatisticSpeedPlus(ldt);
                }
                else {
                    delivered = false;
                    MoYo.logInfo("MoyoSenderMessage->run->delivered&!channel.isOpen()", database.getPresentation()+" couldn't receive message: ");
                }
            }
            else {

                MoYo.logInfo("MoyoSenderMessage->run->!delivered", database.getPresentation()+" couldn't receive message: ");
                channel.basicNack(messageTag, false, true);
                MoYo.getMoYoService().disconnectDB(database, false);
            }
        }catch (UnsupportedEncodingException ex){
            System.out.println(ex.getMessage());
            MoYo.logInfo("MoyoSenderMessage->run->UnsupportedEncodingException", ex.getMessage());
        }catch (IOException ex){
            System.out.println(ex.getMessage()+ex.toString());
            MoYo.logInfo("MoyoSenderMessage->run->IOException", ex.getMessage());
        }

        if (!delivered){ MoYo.getMoYoService().disconnectDB(database, false);}
    }

    public OdinesComConnection getShopConnection() {
        return shopConnection;
    }

    public void setShopConnection(OdinesComConnection shopConnection) {
        this.shopConnection = shopConnection;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }
}

