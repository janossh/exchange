package ua.moyo.rabbitmq.moyo.rabbitmq;

import com.rabbitmq.client.impl.StandardMetricsCollector;
import ua.moyo.rabbitmq.model.Database;
import ua.moyo.rabbitmq.model.DatabaseTube;
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
    public static volatile HashMap<LocalDateTime, Integer> receiveStatistic = new HashMap<>();
    public static volatile LinkedHashMap<LocalDateTime, Integer> sendStatistic = new LinkedHashMap<>();
    public static volatile LinkedHashMap<LocalDateTime, LocalDateTime> sendStatisticTime = new LinkedHashMap<>();
    public static volatile LinkedHashMap<LocalDateTime, Integer> sendStatisticSpeed = new LinkedHashMap<>();

    public static StandardMetricsCollector metrics;


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

    public static synchronized void receiveStatisticPlus(LocalDateTime localDateTime, Integer q){
        Integer plus;
        Integer rq = receiveStatistic.get(localDateTime);
        if (rq==null){
            plus = q;
        }
        else {plus = rq + q;}
        receiveStatistic.put(localDateTime, plus);
    }

    public static synchronized HashMap<LocalDateTime, Integer> getReceiveStatistic(){
        return receiveStatistic;
    }

    public static synchronized void sendStatisticTimePlus(LocalDateTime startTime, LocalDateTime endTime){
        sendStatisticTime.put(startTime, endTime);
    }

    public static synchronized void sendStatisticPlus(LocalDateTime localDateTime){
        Integer q = sendStatistic.get(localDateTime);
        sendStatistic.put(localDateTime, q==null ? 1 : ++q);
    }

    public static synchronized void sendStatisticSpeedPlus(LocalDateTime localDateTime){
        Integer q = sendStatisticSpeed.get(localDateTime);
        q = q==null ? 1 : ++q;
        sendStatisticSpeed.put(localDateTime, q);

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

    public static void setDatabaseTubesFail(HashMap<Database, DatabaseTube> databaseTubesFail) {
        MoYo.databaseTubesFail = databaseTubesFail;
    }

    public static void setDatabaseTubes(HashMap<Database, DatabaseTube> databaseTubes) {
        MoYo.databaseTubes = databaseTubes;
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
        showNotification(description);
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

    public static void setMoYoService(MoYoService moYoService) {
        MoYo.moYoService = moYoService;
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

    public static void setMoyoQueues(Integer moyoQueues) {
        MoYo.moyoQueues = moyoQueues;
    }

}
