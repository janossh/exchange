package ua.moyo.rabbitmq.view;

import com.google.common.eventbus.Subscribe;
import com.vaadin.addon.charts.themes.ValoLightTheme;
import com.vaadin.annotations.Title;
import ua.moyo.rabbitmq.DashboardUI;
import ua.moyo.rabbitmq.model.*;
import ua.moyo.rabbitmq.moyo.Enums.UsersRole;
import ua.moyo.rabbitmq.moyo.Service.MoYoService;
import ua.moyo.rabbitmq.repository.DatabaseRepository;
import ua.moyo.rabbitmq.event.DashboardEvent;
import ua.moyo.rabbitmq.event.DashboardEventBus;
import ua.moyo.rabbitmq.moyo.Events.CustomPoolEvent;
import ua.moyo.rabbitmq.moyo.Events.DatabaseChangeEvent;
import ua.moyo.rabbitmq.moyo.Events.DatabaseConnectionsUpdate;
import ua.moyo.rabbitmq.moyo.rabbitmq.MoYo;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.grid.ColumnResizeMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;


import javax.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.*;


@Title("MoYo")
@SuppressWarnings("serial")
@ViewScope
//@Theme("valo")
@SpringView(name = MoYoHomeView.VIEW_NAME)
public class MoYoHomeView extends VerticalLayout implements View  {

    public static final String VIEW_NAME = "MoYoHomeView";

    public static boolean  isBaseConnected;

    Boolean isSuperUser;
    DashboardUI ui;
    MoYoHome home;
    MoYoService moYoService;
    DatabaseRepository databaseRepository;
    ProgressBar progressBar;
    Grid<Database> dBList;
    Grid<Logger> logs;
    Grid<DatabaseTube> dBConnections, dBConnectionsFailed;
    Grid<DatabaseUnhandledPackages> databaseUnhandledPackages;
    Button connectionOnOff, deleteMessages, connectDB, newDB;
    Label labelDBSuccess, labelDBFailed, labelDBPercent;
    Panel charts;

    public MoYoHomeView() {
        DashboardEventBus.register(this);

        home = new MoYoHome();
        dBList = home.dBList;
        dBConnections = home.dBConnections;
        dBConnectionsFailed = home.dBConnectionsFailed;
        labelDBSuccess = home.labelDBSuccess;
        labelDBFailed = home.labelDBFailed;
        labelDBPercent = home.labelDBPercent;
        connectionOnOff = home.connectionOnOff;
        progressBar = home.progressBar;
        logs = home.logger;
        connectDB = home.connectDB;
        newDB = home.newDB;
        databaseUnhandledPackages = home.unhandledPackages;



    }

    @PostConstruct
    void init() {
        Responsive.makeResponsive(this);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {

        User user = (User) VaadinSession.getCurrent().getAttribute(User.class.getName());
        isSuperUser = user.getRole().equals(UsersRole.SUPER_USER);
        home.showHideButtonsPanel.setVisible(isSuperUser);
        home.buttonsPanel.setVisible(false);

        if(ui==null) {
            ui = ((DashboardUI) getUI());
            moYoService = ui.getMoYoService();
            home.buttonsPanel.setResponsive(true);

            home.showHideDB.addClickListener(clickEvent -> home.dBStatistic.setVisible(!home.dBStatistic.isVisible()));
            home.showHideMenu.addClickListener(clickEvent -> DashboardEventBus.post(new DashboardEvent.ShowHideMenu()));
            home.logOnOff.addClickListener(clickEvent -> logs.setVisible(!logs.isVisible()));
            home.showHideButtonsPanel.addClickListener(clickEvent -> home.buttonsPanel.setVisible(!home.buttonsPanel.isVisible()));

            dBConnections.setItems(ui.getMoYo().getDatabaseTube());
            dBConnectionsFailed.setItems(ui.getMoYo().getDatabaseTubeFailed());
            databaseUnhandledPackages.setItems(ui.getMoYo().getNumberUnhandledPackages());

            initShedulerUpdateDBConnections();
            //initShedulerShowLogMessages();
            initShedulerUpdateLogView();

            initButtonSetActiveDBReverse();
            initButtonConnectionOnOff();
            initButtonDeleteMessages();
            initButtonNewDB();
            initButtonEditDB();
            initButtonConnectDB();

            initGridDBList();
            initGridLogs();

            initProgressBar();

        }

        if (getComponentCount()==0){addComponent(home);}

        databaseRepository = ui.getDatabaseRepository();
        updateDatabases();
        updateConnectionOnOff();
        updateMesQuantity();
        updateInOut();

        setSizeFull();
        setMargin(false);
    }

    private void initShedulerUpdateDBConnections() {
        ui.getMoYo().executor.execute(() -> {

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    MoYo.logInfo("MoYoHomeView->executor", e.getMessage());
                }
                updateDatabaseConnections();

        });
    }

    private void initShedulerShowLogMessages() {
        ui.getMoYo().executor.execute(() -> {
            if (!isSuperUser) return;
                showLogMessages();

        });
    }

    private void initShedulerUpdateLogView() {
        ui.getMoYo().executor.execute(() -> {
                updateLogView();
        });
    }

    private void initButtonSetActiveDBReverse() {
        home.dbOnOff.addClickListener(
                clickEvent -> {
                    ui.getDatabaseRepository().findAll().forEach(database -> {
                        database.setActive(!database.isActive());
                        ui.getDatabaseRepository().saveAndFlush(database);
                    });

                    updateDatabases();
                }
                );
    }

    private void initGridDBList() {
        dBList.setColumnReorderingAllowed(true);
        dBList.getColumns().forEach(databaseColumn -> databaseColumn.setStyleGenerator(database -> { return !database.isActive() ? "nonactive": null;}));
        Page.getCurrent().getStyles().add(".dashboard .v-grid-cell.nonactive { background-color: #ffe6e6; }");
        dBList.addItemClickListener(itemClick -> {

            Database database = itemClick.getItem();
            home.editDB.setVisible(database!=null ? true : false);
            connectDB.setVisible(true);
            updateTitlesConnectDB(database, false);

        });
    }

    private void initButtonConnectionOnOff() {
        connectionOnOff.setStyleName(ValoTheme.BUTTON_FRIENDLY);
        connectionOnOff.setIcon(VaadinIcons.FLIGHT_TAKEOFF);
        connectionOnOff.addClickListener(clickEvent -> {

            isBaseConnected = !isBaseConnected;
            MoYo.whenBaseConnected = LocalDateTime.now();
            if(isBaseConnected){
                ui.getMoYo().initOdisesConnections();
                launchProgressUpdater();
            }
            else{
                ui.getMoYoService().connectionsClose();
            }

            updateConnectionOnOff();
        });
    }

    private void initButtonDeleteMessages() {
        deleteMessages = home.deleteMessages;
        deleteMessages.setStyleName(String.valueOf(ValoLightTheme.class));
        deleteMessages.setStyleName(ValoTheme.BUTTON_DANGER);
        deleteMessages.setIcon(VaadinIcons.ERASER);
        deleteMessages.addClickListener(clickEvent -> ui.getMoYoService().deleteMessages());
    }

    private void initButtonNewDB() {

        newDB.setStyleName(String.valueOf(ValoLightTheme.class));
        newDB.setStyleName(ValoTheme.BUTTON_PRIMARY);
        newDB.setIcon(VaadinIcons.PLUS_CIRCLE_O);
        newDB.addClickListener(clickEvent -> {
            ui.getDatabaseForm().setEntity(new Database());
            ui.getDatabaseForm().openInModalPopup();
        });
    }

    private void initButtonEditDB() {
        home.editDB.setVisible(false);
        home.editDB.addClickListener(clickEvent -> {
            ui.getDatabaseForm().setEntity(dBList.asSingleSelect().getValue());
            ui.getDatabaseForm().openInModalPopup();
        });
    }

    private void initProgressBar() {
        progressBar.setVisible(false);
        progressBar.setEnabled(true);
        progressBar.setWidth("100%");
    }

    private void initGridLogs() {
        logs.setColumnReorderingAllowed(true);
        logs.setColumnResizeMode(ColumnResizeMode.ANIMATED);
        updateLogView();
    }

    private void initButtonConnectDB() {
        connectDB.addClickListener(clickEvent -> {
            Database db = dBList.asSingleSelect().getValue();
            if (!isDatabaseConnected(db)){
                updateTitlesConnectDB(db, true);
                moYoService.connectDB(db);
            }
            else{
                updateTitlesConnectDB(db, false);
                moYoService.disconnectDB(db, true);
            }

        });
    }

    private void updateMesQuantity() {
        home.mesQuantity.setValue(String.valueOf(MoYo.mesQuantity));
    }

    private void updateInOut(){
        home.mesIN.setValue(String.valueOf(MoYo.mesIN));
        home.mesOUT.setValue(String.valueOf(MoYo.mesOUT));
    }

    private void updateNumberUnhandledPackages(){
        databaseUnhandledPackages.setItems(ui.getMoYo().getNumberUnhandledPackages());
    }

    private void updateTitlesConnectDB(Database db, Boolean isConnected) {

        if(db==null){return;}

        if (!isConnected){
            isConnected =  isDatabaseConnected(db);
        }

        if (isConnected){
            connectDB.setCaption("Отключить базу");
            connectDB.setStyleName(ValoTheme.BUTTON_DANGER);
        }
        else{
            connectDB.setCaption("Подключить базу");
            connectDB.setStyleName(ValoTheme.BUTTON_FRIENDLY);
        }

    }

    private boolean isDatabaseConnected(Database database){

        if (database==null){return false;}
        return MoYo.databaseTubes.containsKey(database) ? true : false;

    }

    public void updateLogView(){

        if (!ui.isAttached()){return;}
        if(!logs.isVisible()){return;}

        try {
            ui.access(() -> {

                List<Logger> loggers = new ArrayList<>();
                ui.getLogRepository().findAll().stream().sorted(Comparator.comparing(Logger::getDate).reversed()).limit(50).forEach(logger -> {

                    loggers.add(logger);

                });
                logs.setItems(loggers);
                logs.getDataProvider().refreshAll();

            });
        }catch (Exception ex){
            //System.out.println(ex.getMessage());
            //MoYo.logInfo("MoYoHomeView->updateLogView", ex.getMessage());

        }

    }

    public void showLogMessages(){

        if (!ui.isAttached()){return;}
        try {
            ui.access(() -> {

                ui.getLogRepository().findAll().stream().sorted(Comparator.comparing(Logger::getDate).reversed()).limit(100).filter(Logger::isNotShowen).forEach(logger -> {

                    MoYo.showNotification(logger.getDescription());
                    logger.setShowen(true);
                    ui.getLogRepository().saveAndFlush(logger);

                });

            });
        }catch (Exception ex){
            //ex.printStackTrace();
            //MoYo.logInfo("MoYoHomeView->updateLogView", ex.getMessage());

        }

    }

    private void updateDatabases(){
        dBList.setItems(databaseRepository.findAll());
        dBList.getDataProvider().refreshAll();
    }

    private void updateConnectionOnOff(){
        if(isBaseConnected){

            connectionOnOff.setCaption("Отключить базы");
            connectionOnOff.setStyleName(ValoTheme.BUTTON_DANGER);
            connectionOnOff.setIcon(VaadinIcons.FLIGHT_LANDING);
        }
        else {

            connectionOnOff.setCaption("Подключить базы");
            connectionOnOff.setStyleName(ValoTheme.BUTTON_FRIENDLY);
            connectionOnOff.setIcon(VaadinIcons.FLIGHT_TAKEOFF);
        }
    }

    private void launchProgressUpdater() {
        progressBar.setVisible(true);
        new Thread(() -> {

            do {
                try {
                    Thread.sleep(500);
                } catch (final InterruptedException e) {
                    System.out.println(e.getMessage());
                    MoYo.logInfo("MoYoHomeView->launchProgressUpdater", e.getMessage());
                    }

                Boolean theEnd = updateProgressBar();
                if(theEnd){break;}

            }while (true);


        }).start();
    }

    private boolean updateProgressBar() {

        MoYoService moYoService = ui.getMoYoService();

        Integer progress = moYoService.getTotalDatabaseConnectionConnected();
        Integer maxProgress = moYoService.getTotalDatabaseConnectionForConnect()
                            - moYoService.getTotalDatabaseConnectionConnectedFail();

        ui.access(() -> {
            final float newValue;
            if (progress == maxProgress) {
                newValue = 0f;
                progressBar.setVisible(false);
            } else {
                newValue = (float) progress / maxProgress;
            }
            progressBar.setValue(newValue);

        });
        return (progress == maxProgress);
    }

    public void updateDatabaseConnections(){

        if (!ui.isAttached()){return;}
        try {
        ui.access(() -> {

            dBConnections.setItems(ui.getMoYo().getDatabaseTube());
            dBConnections.getDataProvider().refreshAll();

            dBConnectionsFailed.setItems(ui.getMoYo().getDatabaseTubeFailed());
            dBConnectionsFailed.getDataProvider().refreshAll();

            //this was outside / under this procedure in initShedulerUpdateDBConnections...
            dBConnections.setVisible(MoYo.getDatabaseTubes().size()!=0);
            dBConnectionsFailed.setVisible(MoYo.getDatabaseTubesFail().size()!=0);
            boolean consVisible = (MoYo.getDatabaseTubes().size() + MoYo.getDatabaseTubesFail().size())>0;
            home.cons.setVisible(consVisible);

            Integer forConnectDB = moYoService.getTotalDatabaseConnectionForConnect();
            Integer connectedDB = moYoService.getTotalDatabaseConnectionConnected();
            Integer failDB = moYoService.getTotalDatabaseConnectionConnectedFail();

            boolean isDataForShow = (connectedDB + failDB)>0;
            if (isDataForShow){
                labelDBSuccess.setValue(connectedDB.toString());
                labelDBFailed.setValue(failDB.toString());
                Integer percent = Math.round(100 * (float) connectedDB / forConnectDB);
                labelDBPercent.setValue(percent.toString()+" %");
            }
            else {
                labelDBSuccess.setValue("0");
                labelDBFailed.setValue("0");
                labelDBPercent.setValue("0.0%");
            }
            home.statistic.setVisible(isDataForShow);
            //...this was outside / under this procedure in initShedulerUpdateDBConnections
        });
        }catch (Exception ex){
            ex.printStackTrace();
            //MoYo.logInfo("MoYoHomeView->updateDatabaseConnections", ex.getMessage());
        }
    }

    @Subscribe
    public void userLoginRequested(final DatabaseChangeEvent event) {
        updateDatabases();
    }

    @Subscribe
    public void updateDatabaseConnections(final DatabaseConnectionsUpdate event) {
        updateDatabaseConnections();
    }

    @Subscribe
    public void updateWithPool(final CustomPoolEvent customPoolEvent){
        initShedulerUpdateDBConnections();
//        initShedulerShowLogMessages();
        initShedulerUpdateLogView();
        updateMesQuantity();
        updateInOut();
        updateNumberUnhandledPackages();

    }


}
