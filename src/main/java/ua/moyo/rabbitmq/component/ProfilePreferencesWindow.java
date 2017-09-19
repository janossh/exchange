package ua.moyo.rabbitmq.component;


import ua.moyo.rabbitmq.domain.UserDashboard;
import ua.moyo.rabbitmq.event.DashboardEvent;
import ua.moyo.rabbitmq.event.DashboardEventBus;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class ProfilePreferencesWindow extends Window {

    public static final String ID = "profilepreferenceswindow";

    public static void open(final UserDashboard user, final boolean preferencesTabActive) {
        DashboardEventBus.post(new DashboardEvent.CloseOpenWindowsEvent());

    }

}
