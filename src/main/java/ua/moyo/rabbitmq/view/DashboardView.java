package ua.moyo.rabbitmq.view;

import com.google.common.eventbus.Subscribe;
import ua.moyo.rabbitmq.event.DashboardEvent;
import ua.moyo.rabbitmq.event.DashboardEventBus;
import com.vaadin.event.LayoutEvents;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;

import com.vaadin.server.Responsive;
import com.vaadin.spring.annotation.SpringView;

import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.*;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.ValoTheme;

import javax.annotation.PostConstruct;

import java.util.*;

@SuppressWarnings("serial")
@ViewScope
@SpringView(name = DashboardView.VIEW_NAME)
public class DashboardView extends Panel implements View  {

    public static final String VIEW_NAME = "dashboard";

    public static final String EDIT_ID = "dashboard-edit";
    public static final String TITLE_ID = "dashboard-title";

    private final VerticalLayout root;
    private com.vaadin.ui.Label titleLabel = new com.vaadin.ui.Label();


    HashMap<String, Integer> syncDocFaultsStats;


    public DashboardView() {
        setSizeFull();


        addStyleName(ValoTheme.PANEL_BORDERLESS);

        DashboardEventBus.register(this);

        root = new VerticalLayout();
        root.setSizeFull();
        root.setMargin(true);
        root.addStyleName("dashboard-view");
        setContent(root);
        Responsive.makeResponsive(root);

        Component header = buildHeader();


        root.addComponent(header);


        // All the open sub-windows should be closed whenever the root layout
        // gets clicked.


        root.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(final LayoutEvents.LayoutClickEvent event) {
                DashboardEventBus.post(new DashboardEvent.CloseOpenWindowsEvent());
            }
        });



    }


    private Component buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addStyleName("viewheader");
        header.setSpacing(true);

        //titleLabel = new Label();
        titleLabel.setCaption("Dashboard");
        titleLabel.setId(TITLE_ID);
        titleLabel.setSizeUndefined();
        titleLabel.addStyleName(ValoTheme.LABEL_H1);
        titleLabel.addStyleName(ValoTheme.LABEL_NO_MARGIN);
        header.addComponent(titleLabel);

        return header;
    }

    @PostConstruct
    void init() {
        System.out.println("init");
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {

    }

    public void addUpdateSyncDocFaultsStats(String key){
        if(!syncDocFaultsStats.containsKey(convertString(key))){syncDocFaultsStats.put(convertString(key),1);}
        else{syncDocFaultsStats.put(convertString(key),syncDocFaultsStats.get(convertString(key))+1);}
    }


    public String convertString(String in){
        String out = in;
        return out;
    }

    @Subscribe
    public void postViewChange(final DashboardEvent.ShowHideMenu event) {
        setVisible(!isVisible());
    }

}
