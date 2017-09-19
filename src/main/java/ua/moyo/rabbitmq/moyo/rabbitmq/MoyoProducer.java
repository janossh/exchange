package ua.moyo.rabbitmq.moyo.rabbitmq;

import ua.moyo.rabbitmq.model.Database;
import ua.moyo.rabbitmq.rabbitMQ.rabbitproducer.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MoyoProducer {

    public final static String routingKey = "device-sample";

    @Autowired
    RabbitTemplate rabbitTemplate;

    public void send(Message message) throws Exception {
        rabbitTemplate.setExchange("moyo");
        rabbitTemplate.convertAndSend(message.getMessageBody());
    }

    public void send(Message message, Database database) throws Exception {
        rabbitTemplate.setExchange(null);
        rabbitTemplate.convertAndSend(database.getPresentation(),message.getMessageBody());
    }


}
