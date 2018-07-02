package ua.moyo.rabbitmq.view;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.ui.declarative.Design;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Grid;

/**
 * !! DO NOT EDIT THIS FILE !!
 * <p>
 * This class is generated by Vaadin Designer and will be overwritten.
 * <p>
 * Please make a subclass with logic and additional interfaces as needed,
 * e.g class LoginView extends LoginDesign implements view { }
 */
@DesignRoot
@AutoGenerated
@SuppressWarnings("serial")
public class MoYoHome extends VerticalLayout {
    protected HorizontalLayout buttonsPanel;
    protected Button newDB;
    protected Button editDB;
    protected Button connectDB;
    protected Button deleteMessages;
    protected Button connectionOnOff;
    protected HorizontalLayout buttonsPanel1;
    protected Label mesIN;
    protected Label mesOUT;
    protected Button showHideMenu;
    protected Label mesQuantity;
    protected Button showHideButtonsPanel;
    protected Button showHideDB;
    protected Button logOnOff;
    protected Button dbOnOff;
    protected HorizontalLayout statistic;
    protected Label labelDBSuccess;
    protected Label labelDBFailed;
    protected Label labelDBPercent;
    protected ProgressBar progressBar;
    protected HorizontalLayout dBStatistic;
    protected Grid<ua.moyo.rabbitmq.model.Database> dBList;
    protected VerticalLayout cons;
    protected Grid<ua.moyo.rabbitmq.model.DatabaseTube> dBConnections;
    protected Grid<ua.moyo.rabbitmq.model.DatabaseTube> dBConnectionsFailed;
    protected VerticalLayout unhandledPackagesLayout;
    protected Grid<ua.moyo.rabbitmq.model.DatabaseUnhandledPackages> unhandledPackages;
    protected Grid<ua.moyo.rabbitmq.model.Logger> logger;


    public MoYoHome() {
        Design.read(this);
    }
}
