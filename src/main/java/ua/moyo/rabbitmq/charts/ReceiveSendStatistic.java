package ua.moyo.rabbitmq.charts;


import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.examples.AbstractVaadinChartExample;
import com.vaadin.addon.charts.model.*;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.addon.charts.model.style.Style;
import ua.moyo.rabbitmq.moyo.rabbitmq.MoYo;
import com.vaadin.ui.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static ua.moyo.rabbitmq.config.MoYoConfig.maxMessageStatistic;

@SuppressWarnings("serial")
public class ReceiveSendStatistic extends AbstractVaadinChartExample {

    String title = "Статистика получения / отправки сообщений";
    String axisTitle = "Количество сообщений";


    Chart chart;
    Configuration conf;
    XAxis xAxis;
    YAxis yAxis;

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
        chart = new Chart(ChartType.COLUMN);
        conf = chart.getConfiguration();
        conf.setTitle(new Title());

        xAxis = new XAxis();


        yAxis = new YAxis();
        yAxis.setAllowDecimals(false);
        yAxis.setMin(0);
        yAxis.setTitle(new AxisTitle(axisTitle));

        StackLabels sLabels = new StackLabels(true);
        yAxis.setStackLabels(sLabels);

        conf.addyAxis(yAxis);


        Tooltip tooltip = new Tooltip();
        tooltip.setFormatter("function() { return '<b>'+ this.x +'</b><br/>'"
                + "+this.series.name +': '+ this.y +'<br/>'+'Total: '+ this.point.stackTotal; }");
        conf.setTooltip(tooltip);

        PlotOptionsColumn plotOptions = new PlotOptionsColumn();
        plotOptions.setStacking(Stacking.NORMAL);

        DataLabels labels = new DataLabels(true);
        Style style=new Style();
        style.setTextShadow("0 0 3px black");
        labels.setStyle(style);
        labels.setColor(new SolidColor("white"));
        plotOptions.setDataLabels(labels);
        conf.setPlotOptions(plotOptions);

        updateChart();

        runWhileAttached(chart, () -> updateChart(), 7000, 100);

        chart.drawChart(conf);
    }

    public void updateChart(){

        LinkedList<Number> delivered = new LinkedList<>();
        LinkedList<Number> notDelivered = new LinkedList<>();
        LinkedList<LocalDateTime> categories = new LinkedList<>();

        MoYo.sendStatistic.entrySet().stream().sorted((o1, o2) -> o2.getKey().compareTo(o1.getKey()))
                .limit(maxMessageStatistic).sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey()))
                .forEach(localDateTimeIntegerEntry -> {

            delivered.add(localDateTimeIntegerEntry.getValue());
            Integer totalMes = MoYo.getReceiveStatistic().get(localDateTimeIntegerEntry.getKey()); if (totalMes==null){totalMes=MoYo.getMoyoQueues();}
            notDelivered.add(totalMes - localDateTimeIntegerEntry.getValue());
            categories.add(localDateTimeIntegerEntry.getKey());

                });

        String[] categoriesNow =  xAxis.getCategories();
        for (String s : categoriesNow) {xAxis.removeCategory(s);}

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        String[] categoriesClone = new String[categories.size()];
        categories.forEach(s -> categoriesClone[categories.indexOf(s)] = s.format(dtf));
        xAxis.setCategories(categoriesClone);
        conf.addxAxis(xAxis);

        ListSeries serie1 = new ListSeries("Not delivered", notDelivered);
        ListSeries serie2 = new ListSeries("Delivered", delivered);
        conf.setSeries(serie1, serie2);
        chart.drawChart(conf);

    }

}
