package ua.moyo.rabbitmq.moyo.Controller;

import ua.moyo.rabbitmq.model.Database;
import ua.moyo.rabbitmq.repository.DatabaseRepository;
import ua.moyo.rabbitmq.moyo.rabbitmq.MoYo;
import ua.moyo.rabbitmq.moyo.rabbitmq.MoyoProducer;
import ua.moyo.rabbitmq.rabbitMQ.rabbitproducer.Message;

import net.anotheria.moskito.aop.annotation.Monitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


@RestController
@RequestMapping("/send")
@Monitor(producerId = "Controller", subsystem = "MoYo", category = "-")
public class ProducerRestController {

    @Autowired
    MoyoProducer producer;

    @Autowired
    DatabaseRepository databaseRepository;

    @RequestMapping(path = "/all_shops", method = RequestMethod.POST)//, consumes = MediaType.APPLICATION_JSON_VALUE
    String add(@RequestBody String message) throws Exception {
        MoYo.mesQuantity++;
        producer.send(new Message("moyo", message));
        //add after sending to rabbit, have situations where in rabbit message registered next second we have in statistic

        statisticPlus(MoYo.getMoyoQueues());
        return "ok";
    }

    @RequestMapping(path = "/one_base", method = RequestMethod.POST)
    String addOneBase(@RequestBody String message,
                      @RequestHeader(value="server") String server,
                      @RequestHeader(value="base") String base) throws Exception {

        final Database[] database1 = new Database[1];

        databaseRepository.findAll().forEach(database -> {
            if (database.getIp().equals(server)&&database.getBase().equals(base)){
                database1[0] = database;
           }
        });

        if (database1[0]==null){return "error";}
        MoYo.mesQuantity++;
        producer.send(new Message("moyo", message), database1[0]);
        statisticPlus(null);
        return "ok";


    }

    private void statisticPlus(Integer q){

        LocalDateTime time = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        MoYo.receiveStatisticPlus(time, q);
        MoYo.sendStatisticTimePlus(time, time);

    }



}
