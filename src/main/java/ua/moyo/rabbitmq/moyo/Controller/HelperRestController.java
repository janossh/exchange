package ua.moyo.rabbitmq.moyo.Controller;

import ua.moyo.rabbitmq.moyo.Service.MoYoService;
import ua.moyo.rabbitmq.moyo.rabbitmq.MoYo;
import ua.moyo.rabbitmq.moyo.rabbitmq.MoyoProducer;
import ua.moyo.rabbitmq.moyo.Odines.OdinesComConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/helper")
public class HelperRestController {

    @Autowired
    MoyoProducer producer;

    @Autowired
    MoYoService moYoService;

    @RequestMapping(path = "update_com_connection", method = RequestMethod.GET)
    String add() throws Exception {
        moYoService.connectionsClose();
        return "ok";
    }

    @RequestMapping(path = "show_com_connections", method = RequestMethod.GET)
    String show() throws Exception {

        System.out.println("---------------------DB_CONNECTED--------------------------------------");
        for (OdinesComConnection connection : MoYo.OdiesComConnectionPool.values()) {
            System.out.println("host: "+connection.getSettings().getHost()+", db: "+connection.getSettings().getDatabase()+"");
        }
        System.out.println("-----------------------------------------------------------------------");
        return "ok";



    }

    @RequestMapping(path = "delete_messages", method = RequestMethod.GET)
    String deleteMessages(){
        moYoService.deleteMessages();
        return "ok";
    }


}
