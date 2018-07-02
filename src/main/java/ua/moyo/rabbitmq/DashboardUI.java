package ua.moyo.rabbitmq;

import com.google.common.eventbus.Subscribe;
import com.vaadin.annotations.Widgetset;
import ua.moyo.rabbitmq.repository.LogRepository;
import ua.moyo.rabbitmq.moyo.Events.CustomPoolEvent;
import ua.moyo.rabbitmq.event.DashboardEvent;
import ua.moyo.rabbitmq.moyo.Service.MoYoService;
import ua.moyo.rabbitmq.moyo.rabbitmq.MoYo;
import ua.moyo.rabbitmq.repository.UserRepository;
import ua.moyo.rabbitmq.view.DatabaseForm;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import ua.moyo.rabbitmq.repository.DatabaseRepository;
import ua.moyo.rabbitmq.domain.UserDashboard;
import ua.moyo.rabbitmq.event.DashboardEventBus;
import ua.moyo.rabbitmq.model.User;
import ua.moyo.rabbitmq.service.UserService;
import com.vaadin.navigator.View;
import com.vaadin.server.*;
import com.vaadin.shared.Position;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Window;

import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ua.moyo.rabbitmq.view.LoginView;
import ua.moyo.rabbitmq.view.MainView;
import ua.moyo.rabbitmq.view.MoYoHomeView;

import java.util.Locale;


@SuppressWarnings("serial")
@Theme("dashboard")
//@Theme("valo")
@Widgetset("com.vaadin.demo.dashboard.DashboardWidgetSet")
@Title("MoYo")
@SpringUI(path = "")
//@UIScope
@Push(PushMode.AUTOMATIC )
public class DashboardUI extends UI {

    private static final long serialVersionUID = 1L;

    private final DashboardEventBus dashboardEventbus = new DashboardEventBus();

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    DatabaseRepository databaseRepository;

    @Autowired
    DatabaseForm databaseForm;

    @Autowired
    LogRepository logRepository;

    @Autowired
    MoYo moYo;

    @Autowired
    MoYoService moYoService;

    @Value("${spring.datasource.password}")
    private String authorization_fault_not_correct_password;




    public DashboardUI(){


    }


    @Override
    protected void init(VaadinRequest vaadinRequest) {

        moYoService.logClean();

        setLocale(Locale.US);
        DashboardEventBus.register(this);
        Responsive.makeResponsive(this);
        addStyleName(ValoTheme.UI_WITH_MENU);

        updateContent();

        Page.getCurrent().addBrowserWindowResizeListener(
                (Page.BrowserWindowResizeListener) event -> DashboardEventBus.post(new DashboardEvent.BrowserResizeEvent()));

        addPollListener(pollEvent -> {

            if(getNavigator()!=null){
                View v = getNavigator().getCurrentView();
                if(v.getClass().equals(MoYoHomeView.class)){
                    DashboardEventBus.post(new CustomPoolEvent());
                }
            }



        });

        setPollInterval(5000);

    }

    @Override
    public void detach() {
        super.detach();
    }


    public DatabaseForm getDatabaseForm() {
        return databaseForm;
    }

    public DatabaseRepository getDatabaseRepository() {
        return databaseRepository;
    }

    public MoYo getMoYo() {
        return moYo;
    }

    public MoYoService getMoYoService() {
        return moYoService;
    }

    public LogRepository getLogRepository() {
        return logRepository;
    }

      private void updateContent() {
        UserDashboard user = (UserDashboard) VaadinSession.getCurrent().getAttribute(
                UserDashboard.class.getName());
        //if (user != null && "admin".equals(user.getRole())) {
        if (user != null) {
            // Authenticated user
            setContent(new MainView());
            removeStyleName("loginview");
            getNavigator().navigateTo(getNavigator().getState());
        } else {
            setContent(new LoginView());
            addStyleName("loginview");
        }
    }



    @Subscribe
    public void userLoginRequested(final DashboardEvent.UserLoginRequestedEvent event) {
        //UserDashboard user = getDataProvider().authenticate(event.getUserName(), event.getPassword());
        if(userService.isUserExistFindByEmail(event.getUserName())){

            User userByEmail = userService.getUserByEmail(event.getUserName());

            if(!userByEmail.getPassword().equals(event.getPassword())){
                showNotificationUnauthorized("Hello "+userByEmail.getUsername()+" you enter wrong password"); return;}

            if(!userByEmail.isRegistered()){return;}

            UserDashboard user = new UserDashboard();
            user.setFirstName(userByEmail.getUsername());
            user.setLastName("");
            user.setRole("admin");
            //user.setRole(userByEmail.getRole());
            user.setEmail(userByEmail.getEmail());
            VaadinSession.getCurrent().setAttribute(UserDashboard.class.getName(), user);
            VaadinSession.getCurrent().setAttribute(User.class.getName(), userByEmail);

            updateContent();
        }
        else{showNotificationUnauthorized("User with email "+event.getUserName()+" doesn't registered");}






    }

    private void showNotificationUnauthorized(String message){
        Notification notification = new Notification(message);
        //notification.setDescription("Incorrect login or password");
        //notification.setStyleName("tray dark small closable login-help");
        notification.setPosition(Position.TOP_CENTER);
        notification.setIcon(new ThemeResource("favicon.ico"));
        notification.show(Page.getCurrent());
    }

    @Subscribe
    public void closeOpenWindows(final DashboardEvent.CloseOpenWindowsEvent event) {
        for (Window window : getWindows()) {
            window.close();
        }
    }

    @Subscribe
    public void userLoggedOut(final DashboardEvent.UserLoggedOutEvent event) {
        // When the user logs out, current VaadinSession gets closed and the
        // page gets reloaded on the login screen. Do notice the this doesn't
        // invalidate the current HttpSession.
        VaadinSession.getCurrent().close();
        Page.getCurrent().reload();
    }

    public static DashboardEventBus getDashboardEventbus() {

        return ((DashboardUI) getCurrent()).dashboardEventbus;
    }


}
