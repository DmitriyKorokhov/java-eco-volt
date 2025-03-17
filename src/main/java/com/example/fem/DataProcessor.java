package com.example.fem;

import java.util.List;

public class DataProcessor {

    /**
     * Обрабатывает CSV-ответ и извлекает данные G(i) и T2m.
     */
    public void processData(String output, List<Double> ghiList, List<Double> t2mList) {
        // Разделяем ответ на строки
        String[] lines = output.split("\n");

        // Флаг для начала обработки данных
        boolean isDataSection = false;

        // Обрабатываем каждую строку данных
        for (String line : lines) {
            line = line.trim(); // Убираем лишние пробелы

            // Пропускаем служебные строки
            if (line.startsWith("Latitude") || line.startsWith("Longitude") || line.startsWith("Elevation") ||
                    line.startsWith("Radiation database") || line.startsWith("Slope") || line.startsWith("Azimuth")) {
                continue;
            }

            // Начинаем обработку данных после строки с заголовками
            if (line.startsWith("time,G(i),H_sun,T2m,WS10m,Int")) {
                isDataSection = true;
                continue;
            }

            // Обрабатываем строки с данными
            if (isDataSection && !line.isEmpty()) {
                // Разделяем строку на столбцы по запятой
                String[] columns = line.split(",");

                // Проверяем, что строка содержит достаточно столбцов
                if (columns.length >= 4) { // 4 столбца: time, G(i), H_sun, T2m, ...

                    try {
                        // Извлекаем G(i) и T2m
                        double ghi = Double.parseDouble(columns[1].trim()); // G(i) — второй столбец
                        double t2m = Double.parseDouble(columns[3].trim()); // T2m — четвертый столбец

                        // Добавляем данные в списки
                        ghiList.add(ghi);
                        t2mList.add(t2m);

                    } catch (NumberFormatException e) {
                        // Если данные не являются числами, пропускаем строку
                        System.out.println("Ошибка при обработке строки: " + line);
                    }
                }
            }
        }
    }

    /**
     * Извлекает значение оптимального угла наклона (Slope) из ответа API.
     */
    public double extractOptimalSlope(String output) {
        // Ищем строку, содержащую "Slope:"
        for (String line : output.split("\n")) {
            if (line.contains("Slope:")) {
                // Используем регулярное выражение для извлечения числа
                String[] parts = line.split(":");
                if (parts.length > 1) {
                    String slopeValue = parts[1].trim().split(" ")[0]; // Берем первое слово после "Slope:"
                    try {
                        return Double.parseDouble(slopeValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Ошибка при парсинге значения Slope: " + slopeValue);
                    }
                }
            }
        }
        return 0.0; // Возвращаем 0, если значение не найдено
    }
}