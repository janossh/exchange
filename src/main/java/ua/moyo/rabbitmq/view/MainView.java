package ua.moyo.rabbitmq.view;

//import DashboardNavigator;
import ua.moyo.rabbitmq.DashboardNavigator;
import com.vaadin.ui.*;

/*
 * Dashboard MainView is a simple HorizontalLayout that wraps the menu on the
 * left and creates a simple container for the navigator on the right.
 */

@SuppressWarnings("serial")
public class MainView extends HorizontalLayout  {

    public MainView() {
        setSizeFull();

        addStyleName("mainview");

        Component dashboardMenu = new DashboardMenu();
        addComponent(dashboardMenu);

        ComponentContainer content = new CssLayout();
        content.addStyleName("view-content");
        content.setSizeFull();
        addComponent(content);
        setExpandRatio(content, 1.0f);

        new DashboardNavigator(content);

    }


}
