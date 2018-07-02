package ua.moyo.rabbitmq.moyo.Odines;


import ua.moyo.rabbitmq.model.Database;
import ua.moyo.rabbitmq.model.DatabaseTube;
import ua.moyo.rabbitmq.moyo.rabbitmq.MoYo;
import org.jawin.COMException;
import org.jawin.DispatchPtr;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import static ua.moyo.rabbitmq.moyo.rabbitmq.MoYo.NumberUnhandledPackagesDefault;

public class OdinesComConnection extends DispatchPtr {

    OdinesComConnector connector;
    OdinesConnectionSettings settings;
    DispatchPtr connection;
    boolean connected = false;
    DispatchPtr obModul;

    public OdinesComConnection(OdinesComConnector connector,
                                                   OdinesConnectionSettings settings, boolean server)  {


        this.connector = connector;
        this.settings = settings;

        Database database = settings.getDatabase1C();

        try {

            if(server){
                this.connection = connector.invoke("Srvr=\""
                        + settings.host + "\";Ref=\"" + settings.database + "\";Usr=\""
                        + settings.user + "\";Pwd=\"" + settings.password + "\"");
            }
            else{
                this.connection = connector.invoke("File="+settings.database+"");
            }
            connected = true;


            System.out.println("host: "+settings.host+", db: "+settings.database+" connected "+new Date());




        }
        catch (COMException ex){
            connected = false;
            System.out.println(ex.toString());
            MoYo.logInfo("OdinesComConnection->constuctor->COMException", ex.getMessage());






        }

        }

    public void close() {
        try {
            connection.close();
            connector.connector.close();
        }
        catch (COMException ex){
            try {
                System.out.println(new String(ex.getMessage().getBytes("ISO-8859-1"),"windows-1251"));
                MoYo.logInfo("OdinesComConnection->sendMessage->COMException", ex.getMessage());
            }
            catch (UnsupportedEncodingException eex){
                System.out.println(eex.getMessage());
                MoYo.logInfo("MoyoSenderMessage->run->UnsupportedEncodingException", eex.getMessage());
            }

        }

    }

    public boolean sendMessage(String message) throws UnsupportedEncodingException {
        boolean delivered = true;
        try {

            DispatchPtr gitDisp = (DispatchPtr)connection.createGITRef();

            obModul = (DispatchPtr)gitDisp.get("RABBIT_MQ");
            String mes = (String) obModul.invoke("fun", message);
            obModul.close();
            if(!mes.equals("ok")){delivered = false;}

            gitDisp.close();


            }
        catch (COMException ex){
            delivered = false;
            System.out.println(new String(ex.getMessage().getBytes("ISO-8859-1"),"windows-1251"));
            MoYo.logInfo("OdinesComConnection->sendMessage->COMException", ex.getMessage());

        }

        return delivered;
    }

    public boolean isOnline() throws UnsupportedEncodingException {
        boolean isOnline = true;
        try {
            DispatchPtr gitDisp = (DispatchPtr)connection.createGITRef();
            obModul = (DispatchPtr)gitDisp.get("RABBIT_MQ");
            String mes = (String) obModul.invoke("hello", "");
            obModul.close();
            if(!mes.equals("ok")){isOnline = false;}
            gitDisp.close();
        }
        catch (COMException ex){
            String exMes = new String(ex.getMessage().getBytes("ISO-8859-1"),"windows-1251");
            System.out.println(exMes);
            MoYo.logInfo("OdinesComConnection->isOnline->COMException", exMes);
            isOnline = false;
        }


        return isOnline;
    }

    public Integer getNumberUnhandledPackages() {
        Integer NumberUnhandledPackages = NumberUnhandledPackagesDefault;
        try {
            DispatchPtr gitDisp = (DispatchPtr)connection.createGITRef();
            obModul = (DispatchPtr)gitDisp.get("RABBIT_MQ");
            NumberUnhandledPackages =  (Integer)obModul.invoke("hello", "NumberUnhandledPackages");
            obModul.close();
            gitDisp.close();
        }
        catch (COMException ex){
            try {
                MoYo.logInfo("OdinesComConnection->getNumberUnhandledPackages->COMException", new String(ex.getMessage().getBytes("ISO-8859-1"),"windows-1251"));
            }
            catch (UnsupportedEncodingException uee){
                MoYo.logInfo("OdinesComConnection->getNumberUnhandledPackages->UnsupportedEncodingException", uee.getMessage());
            }
        }
        catch (Exception e){
            MoYo.logInfo("OdinesComConnection->getNumberUnhandledPackages->Exception", e.getMessage());
        }
        return NumberUnhandledPackages;
    }

    public OdinesConnectionSettings getSettings() {
        return settings;
    }

    public boolean isConnected() {
        return connected;
    }


}
