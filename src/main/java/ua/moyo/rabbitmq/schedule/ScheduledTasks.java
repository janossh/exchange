package ua.moyo.rabbitmq.schedule;

import ua.moyo.rabbitmq.moyo.Service.MoYoService;
import ua.moyo.rabbitmq.moyo.rabbitmq.MoYo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

@Component
public class ScheduledTasks {

    @Autowired
    MoYoService moYoService;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(cron = "0 00 06 * * ?")
    public void startConnections(){
        moYoService.logClean();
        MoYo.mesQuantity = 0;
        MoYo.whenBaseConnected = LocalDateTime.now();
        moYoService.connectAllDB();
    }

    @Scheduled(cron = "0 50 23 * * ?")
    public void stopConnections(){
        moYoService.connectionsClose();
    }

    @Scheduled(fixedRate = 300000)
    public void controlPoolConnections(){
        moYoService.controlPoolConnections();
    }

}
