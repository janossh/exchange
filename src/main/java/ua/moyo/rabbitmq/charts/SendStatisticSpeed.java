package ua.moyo.rabbitmq.charts;


import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.examples.AbstractVaadinChartExample;
import com.vaadin.addon.charts.model.*;
import com.vaadin.addon.charts.model.style.SolidColor;
import ua.moyo.rabbitmq.moyo.rabbitmq.MoYo;
import com.vaadin.ui.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@SuppressWarnings("serial")
public class SendStatisticSpeed extends AbstractVaadinChartExample {

    String title = "Скорость отправки сообщений";
    String speedDef = "mes/sec";
    Number initSpeedValue = 0;

    Chart chart;
    Configuration configuration;
    ListSeries series;

    @Override
    public String getDescription() {
        return title;
    }

    @Override
    protected Component getChart() {
        init();
        return chart;
    }

    private void init(){
        chart = new Chart();

        configuration = chart.getConfiguration();
        configuration.getChart().setType(ChartType.SOLIDGAUGE);
        configuration.getTitle().setText("");

        Pane pane = new Pane();
        pane.setCenter("50%", "85%");
        pane.setSize("90%");
        //pane.setSize("140%");
        pane.setStartAngle(-90);
        pane.setEndAngle(90);
        configuration.addPane(pane);

        configuration.getTooltip().setEnabled(false);

        Background bkg = new Background();
        bkg.setBackgroundColor(new SolidColor("#eeeeee"));
        bkg.setInnerRadius("60%");
        bkg.setOuterRadius("100%");
        bkg.setShape("arc");
        bkg.setBorderWidth(0);
        pane.setBackground(bkg);

        YAxis yaxis = configuration.getyAxis();
        yaxis.setLineWidth(0);
        yaxis.setTickInterval(200);
        yaxis.setTickWidth(0);
        yaxis.setMin(0);
        yaxis.setMax(50);
        yaxis.setTitle("");
        yaxis.getTitle().setY(-70);
        yaxis.setLabels(new Labels());
        yaxis.getLabels().setY(16);
        Stop stop1 = new Stop(0.1f, SolidColor.GREEN);
        Stop stop2 = new Stop(0.5f, SolidColor.YELLOW);
        Stop stop3 = new Stop(0.9f, SolidColor.RED);
        yaxis.setStops(stop1, stop2, stop3);

        PlotOptionsSolidgauge plotOptions = new PlotOptionsSolidgauge();
        plotOptions.setTooltip(new SeriesTooltip());
        plotOptions.getTooltip().setValueSuffix(" "+speedDef);
        DataLabels labels = new DataLabels();
        labels.setY(5);
        labels.setBorderWidth(0);
        labels.setUseHTML(true);
        labels.setFormat("<div style=\"text-align:center\"><span style=\"font-size:25px;\">{y}</span><br/>"
                + "                       <span style=\"font-size:12pxg\">"+speedDef+"</span></div>");
        plotOptions.setDataLabels(labels);
        configuration.setPlotOptions(plotOptions);

        series = new ListSeries("Speed", initSpeedValue);
        configuration.setSeries(series);

        updateChart();

        runWhileAttached(chart, () -> updateChart(), 1000, 100);

        chart.drawChart(configuration);
    }

    public void updateChart(){


        LocalDateTime ldtMinusSecond = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).minusSeconds(1);
        Integer speed = MoYo.sendStatisticSpeed.get(ldtMinusSecond);
        speed = speed==null ? 0 : speed;
        series.updatePoint(0, speed);

    }

}
