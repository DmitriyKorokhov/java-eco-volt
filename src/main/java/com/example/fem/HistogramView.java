package com.example.fem;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;

import java.util.List;

public class HistogramView {
    private final BarChart<String, Number> barChart; // Ось X - String (категории), ось Y - Number
    private final XYChart.Series<String, Number> series;

    public HistogramView() {
        // Создаем оси
        CategoryAxis xAxis = new CategoryAxis(); // Ось X - категории (дни)
        xAxis.setLabel("T, дни");

        NumberAxis yAxis = new NumberAxis(); // Ось Y - числа (значения GHI)
        yAxis.setLabel("G, кВт/кв.м");

        // Создаем BarChart
        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Диаграмма прихода солнечного излучения в течение года");

        // Создаем серию данных
        series = new XYChart.Series<>();
        series.setName("G(T)");

        // Добавляем серию в BarChart
        barChart.getData().add(series);

        // Настраиваем AnchorPane для BarChart
        AnchorPane.setTopAnchor(barChart, 0.0);
        AnchorPane.setBottomAnchor(barChart, 0.0);
        AnchorPane.setLeftAnchor(barChart, 0.0);
        AnchorPane.setRightAnchor(barChart, 0.0);
    }

    public BarChart<String, Number> getBarChart() {
        return barChart;
    }

    public void updateData(List<Double> ghiList) {
        series.getData().clear(); // Очищаем предыдущие данные

        // Проходим по данным с шагом 24 (один день)
        for (int day = 0; day < 365; day++) {
            double max = 0; // Максимальное значение за день
            int startIndex = day * 24; // Начальный индекс для текущего дня
            int endIndex = startIndex + 24; // Конечный индекс для текущего дня

            // Находим максимальное значение за день
            for (int i = startIndex; i < endIndex; i++) {
                double currentValue = ghiList.get(i);
                if (currentValue > max) {
                    max = currentValue; // Обновляем максимальное значение
                }
            }

            // Добавляем максимальное значение в серию данных
            series.getData().add(new XYChart.Data<>(String.valueOf(day + 1), max)); // День от 1 до 365
        }
    }
}
