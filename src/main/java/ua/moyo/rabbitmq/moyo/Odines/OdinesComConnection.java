package ua.moyo.rabbitmq.moyo.Odines;


import ua.moyo.rabbitmq.model.Database;
import ua.moyo.rabbitmq.model.DatabaseTube;
import ua.moyo.rabbitmq.moyo.rabbitmq.MoYo;
import org.jawin.COMException;
import org.jawin.DispatchPtr;

import java.io.UnsupportedEncodingException;
import java.util.Date;

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

            if(MoYo.databaseTubes.containsKey(database)){
                DatabaseTube databaseTube = MoYo.databaseTubes.get(database);
                databaseTube.setConnections(databaseTube.getConnections() +1);
            }
            else{
                MoYo.databaseTubes.put(database, new DatabaseTube(database,1));
            }


        }
        catch (COMException ex){
            connected = false;
            System.out.println(ex.toString());
            MoYo.logInfo("OdinesComConnection->constuctor->COMException", ex.getMessage());


            if(MoYo.databaseTubesFail.containsKey(database)){
                DatabaseTube databaseTube = MoYo.databaseTubesFail.get(database);
                databaseTube.setConnections(databaseTube.getConnections() +1);
            }
            else{
                MoYo.databaseTubesFail.put(database, new DatabaseTube(database,1));
            }



        }

        }

    public void close() throws COMException{
        connection.close();
        connector.connector.close();
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

    public OdinesConnectionSettings getSettings() {
        return settings;
    }

    public boolean isConnected() {
        return connected;
    }


}
