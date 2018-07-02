package ua.moyo.rabbitmq.schedule;

import ua.moyo.rabbitmq.moyo.Service.MoYoService;
import ua.moyo.rabbitmq.moyo.rabbitmq.MoYo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static ua.moyo.rabbitmq.moyo.rabbitmq.MoYo.sendMessageTimeoutSec;

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

        Future<Boolean> future = MoYo.executor.submit(() -> {
            moYoService.controlPoolConnections(); return true;
        });
        doSomethingWithTimeOut(future, 60, "ScheduledTasks->controlPoolConnections->");
    }

    @Scheduled(fixedRate = 60000)
    public void controlUnhandledPackages(){
        Future<Boolean> future = MoYo.executor.submit(() -> {
            moYoService.updateNumberUnhandledPackages(); return true;
        });
        doSomethingWithTimeOut(future, 40, "ScheduledTasks->controlUnhandledPackages->");

    }

    private void doSomethingWithTimeOut(Future<Boolean> future, long timeout, String where){
        try {
            future.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            MoYo.logInfo("ScheduledTasks->controlUnhandledPackages->InterruptedException", e.getMessage());
        } catch (ExecutionException e) {
            MoYo.logInfo("ScheduledTasks->controlUnhandledPackages->ExecutionException", e.getMessage());
        } catch (TimeoutException e) {
            MoYo.logInfo("ScheduledTasks->controlUnhandledPackages->TimeoutException", e.getMessage());
        }
    }

}
