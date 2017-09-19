package ua.moyo.rabbitmq.moyo.rabbitmq;

import ua.moyo.rabbitmq.model.Database;
import ua.moyo.rabbitmq.moyo.Odines.OdinesComConnection;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Created by JLD on 28.07.2017.
 */
public class MoYoConsumer implements Runnable {

    String base;
    Channel channel;
    Database database;

    MoYoConsumer(String db, Channel channel){
        this.base = db;
        this.channel = channel;
        database = MoYo.OdiesComConnectionPool.get(base).getSettings().getDatabase1C();
    }

    public void run() {

        try {

            Consumer consumer = new DefaultConsumer(channel) {

                OdinesComConnection shopConnection = MoYo.OdiesComConnectionPool.get(base);

                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body) throws IOException {

                    LocalDateTime dateIn = LocalDateTime.ofInstant(properties.getTimestamp().toInstant(), ZoneId.systemDefault());

                    if (channel!=null&&channel.isOpen()) {
                        MoyoSenderMessage moyoSenderMessage = new MoyoSenderMessage(shopConnection, new String(body, "UTF-8"), envelope, channel, base, dateIn);
                        MoYo.executor.execute(moyoSenderMessage);
                    }
                }
            };

            String queue = database.getPresentation();
            channel.basicConsume(queue, false, consumer);


        }catch (IOException ioex){
            ioex.printStackTrace();
            System.out.println(ioex.toString());
            MoYo.logInfo("MoYoConsumer->run->IOException", ioex.getMessage());
        }

    }

}
