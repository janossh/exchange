package ua.moyo.rabbitmq.view;

import com.google.gson.JsonObject;
import com.vaadin.data.Binder;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.data.validator.StringLengthValidator;
import ua.moyo.rabbitmq.model.LoginRegistrationInfo;
import ua.moyo.rabbitmq.moyo.Validation.PasswordValidator;
import ua.moyo.rabbitmq.event.DashboardEvent.UserLoginRequestedEvent;
import ua.moyo.rabbitmq.event.DashboardEventBus;
import ua.moyo.rabbitmq.model.User;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.shared.Position;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("serial")
public class LoginView extends VerticalLayout {

    Component loginForm;
    Component c;
    VerticalLayout loginPanel;
    LoginRegistrationInfo info;
    Binder<LoginRegistrationInfo> binder;
    Binder<LoginRegistrationInfo> regBinder;
    int quontityAttempts = 1;
    Button register;

    public LoginView() {
        setSizeFull();

        loginForm = buildLoginForm();
        Responsive.makeResponsive(loginForm);
        addComponent(loginForm);
        setComponentAlignment(loginForm, Alignment.MIDDLE_CENTER);

        Notification notification = new Notification("Добро пожаловать в MoYo");
        notification.setDescription("Введите свои данные для входа в приложение");
        notification.setStyleName("tray dark small closable login-help");
        notification.setPosition(Position.TOP_CENTER);
        notification.setDelayMsec(5000);
        notification.show(Page.getCurrent());

    }

    private Component buildLoginForm() {
        loginPanel = new VerticalLayout();
        loginPanel.setSizeUndefined();
        loginPanel.setSpacing(true);
        loginPanel.addStyleName("login-panel");

        loginPanel.addComponent(buildLabels());
        c = buildFields();
        loginPanel.addComponent(c);
        loginPanel.setExpandRatio(c, 1);

        Responsive.makeResponsive(loginPanel);
        return loginPanel;
    }

    private Component buildFields() {

        binder = new Binder<>();
        regBinder = new Binder<>();

        VerticalLayout fields = new VerticalLayout();
        fields.setSpacing(true);
        fields.addStyleName("fields");
        fields.setSizeFull();

        final TextField username = new TextField("Email");
        username.setIcon(VaadinIcons.USER);//FontAwesome.USER
        username.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);

        final PasswordField password = new PasswordField("Password");
        password.setIcon(VaadinIcons.LOCK);//FontAwesome.LOCK
        password.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);

        final Button signin = new Button("Sign In");
        signin.addStyleName(ValoTheme.BUTTON_PRIMARY);
        signin.setClickShortcut(KeyCode.ENTER);
        signin.focus();

        fields.addComponents(username, password, signin);

        signin.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                DashboardEventBus.post(new UserLoginRequestedEvent(username
                        .getValue(), password.getValue()));
            }
        });

        TabSheet tabsheet = new TabSheet();
        tabsheet.setWidth(300, Unit.PIXELS);
        tabsheet.addTab(fields, "Sign In");
        tabsheet.setStyleName(ValoTheme.TABSHEET_CENTERED_TABS);
        VerticalLayout fieldsRegister = new VerticalLayout();
        fieldsRegister.setSpacing(true);
        fieldsRegister.addStyleName("fields");
        fieldsRegister.setSizeFull();

        TextField usernameReg = new TextField("Email");

        usernameReg.setIcon(VaadinIcons.USER);//FontAwesome.USER
        usernameReg.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);

        final PasswordField passwordReg = new PasswordField("Password");
        passwordReg.setIcon(VaadinIcons.LOCK);//FontAwesome.LOCK
        passwordReg.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);

        TextField firstName = new TextField("First name");

        TextField lastName = new TextField("Last name");

        register = new Button("Register");
        register.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        register.addClickListener(event ->
                {

                    //make request for send message, if ok show submitting view, else show appologies
                    User user = new User();
                    user.setUsername(info.getFirstName()+" "+info.getLastName());
                    user.setEmail(info.getEmailReg());
                    user.setPassword(info.getPasswordReg());
                    user.setPhone("");

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("username", user.getUsername());
                    jsonObject.addProperty("password", user.getPassword());
                    jsonObject.addProperty("email", user.getEmail());
                    jsonObject.addProperty("phone", user.getPhone());
                    String jsonString = jsonObject.toString();

                    RestTemplate restTemplate = new RestTemplate();
                    restTemplate.getMessageConverters()
                            .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
                    restTemplate.setInterceptors(Arrays.asList(new CustomHttpRequestInterceptor()));
                    boolean isTest = false;
                    String host = "localhost:8080";
                    String url = "http://"+host+"/api/auth/registration";

                    HttpEntity<String> request = new HttpEntity<>(jsonString);

                    restTemplate.setErrorHandler(new ResponseErrorHandler() {
                        @Override
                        public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
                            return false;
                        }

                        @Override
                        public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {

                        }
                    });
                    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);


                    buildCodeSubmittingComponent();

                    register.setEnabled(false);
                    loginPanel.setVisible(false);
                    loginPanel.detach();
                    loginPanel.setEnabled(false);

                }
        );


        fieldsRegister.addComponents(firstName, lastName, usernameReg, passwordReg, register);

        fields.setComponentAlignment(signin, Alignment.BOTTOM_LEFT);
        fieldsRegister.setComponentAlignment(register, Alignment.BOTTOM_LEFT);

        info = new LoginRegistrationInfo();

        StringLengthValidator stringLengthValidator = new StringLengthValidator("Must be at least 3 characters",3,30);
        EmailValidator emailValidator = new EmailValidator("Email is incorrect");
        PasswordValidator passwordValidator = new PasswordValidator();

        binder.forField(username).withValidator(emailValidator).bind(LoginRegistrationInfo::getEmail,LoginRegistrationInfo::setEmail);
        binder.forField(password).withValidator(passwordValidator).bind(LoginRegistrationInfo::getPassword,LoginRegistrationInfo::setPassword);

        binder.forField(usernameReg).withValidator(emailValidator).bind(LoginRegistrationInfo::getEmailReg,LoginRegistrationInfo::setEmailReg);
        binder.forField(passwordReg).withValidator(passwordValidator).bind(LoginRegistrationInfo::getPasswordReg,LoginRegistrationInfo::setPasswordReg);

        binder.forField(firstName).withValidator(stringLengthValidator).bind(LoginRegistrationInfo::getFirstName,LoginRegistrationInfo::setFirstName);
        binder.forField(lastName).withValidator(stringLengthValidator).bind(LoginRegistrationInfo::getLastName,LoginRegistrationInfo::setLastName);
        register.setEnabled(false);


        regBinder.forField(usernameReg).withValidator(emailValidator).bind(LoginRegistrationInfo::getEmailReg,LoginRegistrationInfo::setEmailReg);
        regBinder.forField(passwordReg).withValidator(passwordValidator).bind(LoginRegistrationInfo::getPasswordReg,LoginRegistrationInfo::setPasswordReg);
        regBinder.forField(firstName).withValidator(stringLengthValidator).bind(LoginRegistrationInfo::getFirstName,LoginRegistrationInfo::setFirstName);
        regBinder.forField(lastName).withValidator(stringLengthValidator).bind(LoginRegistrationInfo::getLastName,LoginRegistrationInfo::setLastName);

        usernameReg.addValueChangeListener(valueChangeEvent -> formHasChanged());
        passwordReg.addValueChangeListener(valueChangeEvent -> formHasChanged());
        firstName.addValueChangeListener(valueChangeEvent -> formHasChanged());
        lastName.addValueChangeListener(valueChangeEvent -> formHasChanged());

        binder.setBean(info);
        regBinder.setBean(info);

        return tabsheet;
    }

    public void formHasChanged() {
            register.setEnabled(regBinder.isValid());
    }

    private void buildCodeSubmittingComponent(){
        VerticalLayout fields = new VerticalLayout();
        fields.setSizeUndefined();
        VerticalLayout submitCodePanel = new VerticalLayout();
        fields.addComponent(submitCodePanel);
        submitCodePanel.setSizeFull();
        submitCodePanel.setSpacing(true);
        TextField code = new TextField("Enter code on email");
        Button submit = new Button("Submit");
        submit.addClickListener(clickEvent -> {

            confirmRegistration(code.getValue());

        });
        code.setSizeFull();
        submit.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        submit.setWidth("100%");
        submitCodePanel.addComponents(code, submit);
        submitCodePanel.setComponentAlignment(submit, Alignment.MIDDLE_CENTER);
        addComponent(fields);
        setComponentAlignment(fields, Alignment.MIDDLE_CENTER);
    }


    private void confirmRegistration(String code){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("email", info.getEmailReg());
        jsonObject.addProperty("confirmation_code", Integer.valueOf(code));

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        restTemplate.setInterceptors(Arrays.asList(new CustomHttpRequestInterceptor()));
        boolean isTest = false;
        String host = "localhost:8080";
        String url = "http://"+host+"/api/auth/confirmation";
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString());
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {

            }
        });
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        if(response.getBody().contains("ok")){
            DashboardEventBus.post(new UserLoginRequestedEvent(binder.getBean().getEmailReg(), binder.getBean().getPasswordReg()));
        }
        else{
            if(quontityAttempts<=3){
                quontityAttempts++;
                showNonificationWrongCode();
                confirmRegistration(code);}
            else{DashboardEventBus.post(new UserLoginRequestedEvent("", ""));}
        }
    }

    private void showNonificationWrongCode() {
        Notification notification = new Notification("You enter a wrong code, pleasure try again");
        notification.setStyleName("tray dark small closable login-help");
        notification.setPosition(Position.TOP_CENTER);
        notification.setDelayMsec(3000);
        notification.show(Page.getCurrent());
    }

    private Component buildLabels() {
        CssLayout labels = new CssLayout();
        labels.addStyleName("labels");

        Label welcome = new Label("Welcome to MoYo");
        welcome.setSizeUndefined();
        welcome.addStyleName(ValoTheme.LABEL_H4);
        welcome.addStyleName(ValoTheme.LABEL_COLORED);
        labels.addComponent(welcome);

        return labels;
    }


    public class  CustomHttpRequestInterceptor implements ClientHttpRequestInterceptor
    {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException
        {
            HttpHeaders headers = request.getHeaders();
            headers.remove(HttpHeaders.ACCEPT);
            headers.remove(HttpHeaders.USER_AGENT);
            headers.remove(HttpHeaders.CONNECTION);
            headers.remove(HttpHeaders.CONTENT_TYPE);
            headers.add(HttpHeaders.CONTENT_TYPE,"application/json");


            return execution.execute(request, body);
        }

    }

    public class MyGsonHttpMessageConverter extends GsonHttpMessageConverter {
        public MyGsonHttpMessageConverter() {
            List<MediaType> types = Arrays.asList(
                    new MediaType("text", "html", DEFAULT_CHARSET),
                    new MediaType("application", "json", DEFAULT_CHARSET),
                    new MediaType("application", "*+json", DEFAULT_CHARSET),
                    new MediaType("text", "plain", DEFAULT_CHARSET)
            );
            super.setSupportedMediaTypes(types);
        }
    }

}
