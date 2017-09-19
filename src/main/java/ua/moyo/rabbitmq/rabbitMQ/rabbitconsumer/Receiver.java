package ua.moyo.rabbitmq.rabbitMQ.rabbitconsumer;


public interface Receiver {

    public void receiveMessage(String message) throws Exception;

}