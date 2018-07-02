package ua.moyo.rabbitmq.moyo.rabbitmq;

import com.rabbitmq.client.impl.StandardMetricsCollector;
import ua.moyo.rabbitmq.model.Database;
import ua.moyo.rabbitmq.model.DatabaseTube;
import ua.moyo.rabbitmq.model.DatabaseUnhandledPackages;
import ua.moyo.rabbitmq.repository.DatabaseRepository;
import ua.moyo.rabbitmq.repository.LogRepository;
import ua.moyo.rabbitmq.moyo.Enums.LogPriority;
import ua.moyo.rabbitmq.moyo.Odines.OdinesComConnection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import ua.moyo.rabbitmq.moyo.Service.MoYoService;
import com.vaadin.server.ClassResource;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import static com.vaadin.ui.Notification.Type.HUMANIZED_MESSAGE;


@Component
public class MoYo {

    @Autowired
    DatabaseRepository databaseRepository;
    @Autowired
    MoYoService tmoYoService;
    public static MoYoService moYoService;
    @Autowired
    LogRepository logRepository;

    private static String shopUsername;
    private static String shopPassword;
    private static String shopName;
    private static Integer moyoQueues;

    public static LocalDateTime whenBaseConnected;
    public static long mesQuantity = 0;
    private static final int NTHREDS = 300;
    public static HashMap<String, OdinesComConnection> OdiesComConnectionPool = new HashMap<>();
    public static HashSet<Database> shopPool = new HashSet<>();
    public static ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
    public static Connection connection;
    public static volatile HashMap<Database, DatabaseTube> databaseTubes = new HashMap<>();
    public static volatile HashMap<Database, DatabaseTube> databaseTubesFail = new HashMap<>();
    public static volatile HashMap<Database, Channel> channels = new HashMap<>();
    public static volatile List<DatabaseUnhandledPackages> numberUnhandledPackages = new ArrayList<>();

    public static long mesIN = 0;
    public static long mesOUT = 0;


    public static StandardMetricsCollector metrics;
    public static final long connectionTimeoutSec = 90;
    public static final long sendMessageTimeoutSec = 120;

    public static final Integer NumberUnhandledPackagesDefault = 999;

    @Value("${moyo.shop.username}")
    private String shopUsernameNS;

    @Value("${moyo.shop.password}")
    private String shopPasswordNS;

    @Value("${moyo.shop.name}")
    private String shopnameNS;

    public MoYo() {
        initConnectionFactory();
    }

    @PostConstruct
    public void init() {
        moYoService = tmoYoService;
        shopUsername = shopUsernameNS;
        shopPassword = shopPasswordNS;
        shopName = shopnameNS;
        moyoQueues = getMoYoService().getTotalDatabaseConnectionForConnect();

    }

    public static synchronized void plusIn(){
        mesIN++;
    }

    public static synchronized void plusOut(){
        mesOUT++;
    }

    public static synchronized void profileIn(Database database){
        if (database.getRabbitqueue().equals("online")){plusIn();}
    }

    public static synchronized void profileOut(Database database){
        if (database.getRabbitqueue().equals("online")){plusOut();}
    }

    private void initConnectionFactory(){
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            connection = factory.newConnection();
        }

        catch (IOException ioex){
            System.out.println(ioex.toString());
            MoYo.logInfo("MoYo->initConnectionFactory->IOException", ioex.getMessage());
        }

        catch (TimeoutException toex){
            System.out.println(toex.toString());
            MoYo.logInfo("MoYo->initConnectionFactory->TimeoutException", toex.getMessage());
        }
    }

    public void initOdisesConnections(){
        moYoService.connectAllDB();
    }

    public static ArrayList<DatabaseTube> getDatabaseTube(){

        ArrayList<DatabaseTube> databaseTubesList = new ArrayList<>();
        databaseTubes.values().forEach(databaseTube -> databaseTubesList.add(databaseTube));
        return databaseTubesList;
    }

    public static ArrayList<DatabaseTube> getDatabaseTubeFailed(){

        ArrayList<DatabaseTube> databaseTubesList = new ArrayList<>();
        databaseTubesFail.values().forEach(databaseTube -> databaseTubesList.add(databaseTube));
        return databaseTubesList;
    }

    public static HashMap<Database, DatabaseTube> getDatabaseTubes() {
        return databaseTubes;
    }

    public static HashMap<Database, DatabaseTube> getDatabaseTubesFail() {
        return databaseTubesFail;
    }

    public static synchronized List<DatabaseUnhandledPackages> getNumberUnhandledPackages() {
        return numberUnhandledPackages;
    }


    public static void showNotification(String messageText){

            Notification notif = new Notification(split(messageText, 120), HUMANIZED_MESSAGE);
            notif.setDelayMsec(3000);
            notif.setHtmlContentAllowed(true);
            notif.setPosition(Position.BOTTOM_RIGHT);
            notif.setIcon(new ClassResource("/drawable/moyo++.png"));
            notif.show(Page.getCurrent());

    }

    public static void logInfo(String where, String description){
        moYoService.log(where, description, LogPriority.INFO);
    }

    public static void log(String where, String description, LogPriority priority){
        moYoService.log(where, description, priority);
    }

    public static String split(String s, int length) {
        if(s == null) { // Lazy guys may invoke me with nulls too!
            return "";
        }
        if(s.indexOf('\r') >= 0) { // I don't want those ones from the Winso$ world!
            s.replaceAll("\\r", "");
        }
        int pos = s.indexOf('\n'); // Break at newline anyway
        if(pos >= 0) {
            return split(s.substring(0, pos), length) + "<BR/>" +  split(s.substring(pos + 1), length);
        }
        s = s.trim();
        if(s.length() <= length || length <= 0) { // Already small or invalid length specified
            return s;
        }
        pos = length;
        while(pos >= 0) { // Try to split at whitespace at or before the length
            if(Character.isWhitespace(s.charAt(pos))) {
                return s.substring(0, pos).trim() + "<BR/>" + split(s.substring(pos + 1), length);
            }
            --pos;
        }
        // No whitespace found, just split
        return s.substring(0, length) + "<BR/>" + split(s.substring(length), length);
    }

    public static MoYoService getMoYoService() {
        return moYoService;
    }

    public static String getShopUsername() {
        return shopUsername;
    }

    public static String getShopPassword() {
        return shopPassword;
    }

    public static String getShopName() {
        return shopName;
    }

    public static Integer getMoyoQueues() {
        return getMoYoService().getTotalDatabaseConnectionForConnect();
    }


}
