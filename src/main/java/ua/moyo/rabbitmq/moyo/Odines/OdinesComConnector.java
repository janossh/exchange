package ua.moyo.rabbitmq.moyo.Odines;


import org.jawin.COMException;
import org.jawin.DispatchPtr;

import java.util.HashMap;

public class OdinesComConnector{

    DispatchPtr connector;

    static volatile OdinesComConnector odinesComConnectorS;

    static HashMap<OdinesComConnector, Integer> connectorPool = new HashMap<>();

    static Integer maxConnection = 3;

    public static synchronized OdinesComConnector getConnector() throws COMException {

        //if (odinesComConnectorS!=null) return odinesComConnectorS;

        OdinesComConnector odinesComConnector = new OdinesComConnector();
        DispatchPtr connector1C = new DispatchPtr("V83.COMConnector");
        connector1C.put("PoolCapacity", maxConnection);
        connector1C.put("PoolTimeout",300);
        connector1C.put("MaxConnections", maxConnection);
        odinesComConnector.connector = connector1C;

        //odinesComConnectorS = odinesComConnector;

        return odinesComConnector;
   }

    public static synchronized OdinesComConnector getConnectorOnline() throws COMException {

        OdinesComConnector odinesComConnector = new OdinesComConnector();
        DispatchPtr connector1C = new DispatchPtr("V83.COMConnectorOnline");
        connector1C.put("PoolCapacity", maxConnection);
        connector1C.put("PoolTimeout",300);
        connector1C.put("MaxConnections", maxConnection);
        odinesComConnector.connector = connector1C;

        return odinesComConnector;
    }

    public DispatchPtr invoke(String connectionString) throws COMException{
        return (DispatchPtr) connector.invoke("Connect",connectionString);
    }


}
