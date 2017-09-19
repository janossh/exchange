package ua.moyo.rabbitmq.moyo.Validation;

import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.SpringUI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

public class PasswordValidator extends RegexpValidator {

    private static final String PATTERN = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{6,}";

    private static String mes = "Задайте сложный пароль";

    public PasswordValidator() {super(mes, PATTERN, true);}

}
