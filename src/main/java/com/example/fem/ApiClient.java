package com.example.fem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class ApiClient {

    /**
     * Выполняет HTTP-запрос и возвращает ответ в виде строки.
     */
    public String getUrlContent(String urlAddress) {
        StringBuilder content = new StringBuilder();

        try {
            URL url = new URL(urlAddress);
            URLConnection urlConnection = url.openConnection();

            // Проверяем код ответа
            if (urlConnection instanceof HttpURLConnection) {
                HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
                int responseCode = httpConnection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    System.out.println("Ошибка HTTP: " + responseCode);
                    return null; // Возвращаем null в случае ошибки
                }
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append("\n");
            }
            bufferedReader.close();
        } catch (Exception e) {
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
        }

        return content.toString();
    }
}
