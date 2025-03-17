package com.example.fem;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class FileWriterService {

    /**
     * Сохраняет данные Оптимального угла наклона (Slope), G(i) и T2m в текстовый файл
     */
    public void saveDataToFile(String fileName, List<Double> ghiList, List<Double> t2mList, double optimalSlope) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // Записываем оптимальный угол наклона в первую строку
            writer.write("Optimal Slope: " + optimalSlope + " deg.\n");

            // Записываем заголовки
            writer.write("G(i)\tT2m\n");

            // Записываем данные построчно
            for (int i = 0; i < ghiList.size(); i++) {
                writer.write(ghiList.get(i) + "\t" + t2mList.get(i) + "\n");
            }

            System.out.println("Данные успешно сохранены в файл: " + fileName);
        } catch (IOException e) {
            System.out.println("Ошибка при записи данных в файл: " + e.getMessage());
        }
    }
}