package ua.moyo.rabbitmq.view;

import com.vaadin.annotations.Title;
import ua.moyo.rabbitmq.event.DashboardEventBus;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Responsive;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.*;

import javax.annotation.PostConstruct;


@Title("MoYo")
@SuppressWarnings("serial")
@ViewScope
@SpringView(name = MoYoWebView.VIEW_NAME)
public class MoYoWebView extends VerticalLayout implements View  {

    public static final String VIEW_NAME = "MoYoHomeView";

    BrowserFrame web;
    String caption, source;
    String server = "http://10.0.30.55";

    public MoYoWebView() {
        DashboardEventBus.register(this);
    }

    @PostConstruct
    void init() {
        Responsive.makeResponsive(this);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        String viewName = viewChangeEvent.getViewName();
        caption = "viewName";
        if(viewName=="performance"){
            source = server+"/Performance.php";
        }
        else if(viewName=="XRM"){
            source = server+"/XRM+++.php";
        }
        else if(viewName=="Obmen"){
            source = server+"/Monitor_ut_obmenNew.php";
        }
        else if(viewName=="Monitor Korrektnosti Danih"){
            source = server+"/MonitorKorrektnostiDanih.php";
        }


        if(web==null){
        web = new BrowserFrame("", new ExternalResource(source));
        web.setSizeFull();
        addComponent(web);}

        setSizeFull();
        setMargin(false);
    }


}
