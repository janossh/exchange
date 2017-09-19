package ua.moyo.rabbitmq.charts;


import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.examples.AbstractVaadinChartExample;
import com.vaadin.addon.charts.model.*;
import ua.moyo.rabbitmq.moyo.rabbitmq.MoYo;
import com.vaadin.ui.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

import static ua.moyo.rabbitmq.config.MoYoConfig.maxMessageStatistic;

@SuppressWarnings("serial")
public class DeliveryMessageTime extends AbstractVaadinChartExample {

    String title = "Статистика доставки сообщений";
    String titleListSeries = "Время доставки сообщения";
    String axisTitle = "сек.";

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

        PlotOptionsColumn plotOptions = new PlotOptionsColumn();
        plotOptions.setStacking(Stacking.NORMAL);

        conf.setPlotOptions(plotOptions);

        updateChart();

        runWhileAttached(chart, () -> updateChart(), 7000, 100);

        chart.drawChart(conf);
    }

    public void updateChart(){

        LinkedList<Number> delivered = new LinkedList<>();
        LinkedList<LocalDateTime> categories = new LinkedList<>();

        MoYo.sendStatisticTime.entrySet().stream().sorted((o1, o2) -> o2.getKey().compareTo(o1.getKey()))
                .limit(maxMessageStatistic).sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey()))
                .forEach(entry -> {

                    Duration duration = Duration.between(entry.getKey(), entry.getValue());

            delivered.add(duration.getSeconds());
            categories.add(entry.getKey());

                });

        String[] categoriesNow =  xAxis.getCategories();
        for (String s : categoriesNow) {xAxis.removeCategory(s);}

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        String[] categoriesClone = new String[categories.size()];
        categories.forEach(s -> categoriesClone[categories.indexOf(s)] = s.format(dtf));
        xAxis.setCategories(categoriesClone);
        conf.addxAxis(xAxis);


        ListSeries serie = new ListSeries(titleListSeries, delivered);
        conf.setSeries(serie);
        chart.drawChart(conf);

    }

}
