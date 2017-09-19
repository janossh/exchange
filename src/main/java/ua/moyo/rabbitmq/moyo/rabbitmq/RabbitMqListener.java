package ua.moyo.rabbitmq.moyo.rabbitmq;

import org.jawin.COMException;
import org.jawin.DispatchPtr;
import org.jawin.win32.Ole32;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.stereotype.Component;

@EnableRabbit
@Component
public class RabbitMqListener {


    public void handleMessage(String message, String oneSHostname, String oneSDatabase, String username, String password){
        try {

            Ole32.CoInitialize();
            DispatchPtr app = new DispatchPtr("V83.COMConnector");
            DispatchPtr ref = (DispatchPtr) app.invoke("Connect", "Srvr=\""
                    + oneSHostname + "\";Ref=\"" + oneSDatabase + "\";Usr=\""
                    + username + "\";Pwd=\"" + password + "\"");

            DispatchPtr obModul = (DispatchPtr) ref.get("RABBIT_MQ");
            String mes = (String) obModul.invoke("fun", message);
            Ole32.CoUninitialize();
        }
        catch (COMException ex){
            System.out.println(ex.toString());
            MoYo.logInfo("RabbitMqListener->handleMessage->COMException", ex.getMessage());
        }
    }

}
