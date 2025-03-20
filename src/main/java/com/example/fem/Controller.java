package com.example.fem;

import javafx.fxml.FXML;
import javafx.scene.SubScene;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.PerspectiveCamera;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;

public class Controller {
    @FXML
    private TextField latitudeField;

    @FXML
    private TextField longitudeField;

    @FXML
    private AnchorPane globeContainer; // Контейнер для SubScene

    @FXML
    private AnchorPane histogramContainer; // Контейнер для гистограммы

    @FXML
    private Label optimalSlopeLabel; // Label для отображения оптимального угла

    @FXML
    private Label averageGhiLabel; // Label для отображения среднего значения GHI

    private GlobeView globeView;
    private HistogramView histogramView;
    private final ApiClient apiClient = new ApiClient();
    private final DataProcessor dataProcessor = new DataProcessor();
    private final FileWriterService fileWriterService = new FileWriterService();

    // Списки для хранения данных G(i) и T2m
    private final List<Double> ghiList = new ArrayList<>();
    private final List<Double> t2mList = new ArrayList<>();
    // Переменная для хранения оптимального угла наклона
    private double optimalSlope;

    @FXML
    public void initialize() {
        // Инициализируем земной шар
        globeView = new GlobeView();

        // Создаем SubScene и добавляем в него земной шар
        SubScene globeSubScene = new SubScene(globeView.getRoot(), globeContainer.getWidth(), globeContainer.getHeight());
        globeSubScene.setFill(javafx.scene.paint.Color.TRANSPARENT); // Прозрачный фон
        globeSubScene.setCamera(createCamera()); // Настраиваем камеру

        // Привязываем размер SubScene к размеру контейнера
        globeContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            globeSubScene.setWidth(newVal.doubleValue());
        });
        globeContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            globeSubScene.setHeight(newVal.doubleValue());
        });

        // Добавляем SubScene в контейнер
        globeContainer.getChildren().add(globeSubScene);

        // Инициализация гистограммы
        histogramView = new HistogramView(); // Передаем контейнер
        histogramContainer.getChildren().add(histogramView.getBarChart()); // Добавляем гистограмму в контейнер
    }

    /**
     * Создает и настраивает камеру для SubScene.
     */
    private PerspectiveCamera createCamera() {
        PerspectiveCamera camera = new PerspectiveCamera(true); // Включаем фиксированный угол обзора
        camera.setNearClip(0.1); // Ближняя плоскость отсечения
        camera.setFarClip(10000); // Дальняя плоскость отсечения
        camera.setTranslateZ(-700); // Отодвигаем камеру назад
        return camera;
    }

    @FXML
    protected void onGetDataButtonClick() {
        // Очищаем списки перед новым запросом
        ghiList.clear();
        t2mList.clear();

        // Получаем значения из полей ввода
        String latitude = latitudeField.getText();
        String longitude = longitudeField.getText();

        // Проверяем, что все поля заполнены
        if (latitude.isEmpty() || longitude.isEmpty()) {
            showAlert("Ошибка", "Пожалуйста, заполните все поля.");
            return;
        }

        // Формируем URL для запроса
        String urlAddress = String.format(
                "https://re.jrc.ec.europa.eu/api/seriescalc?lat=%s&lon=%s&optimalangles=1&startyear=2023&endyear=2023",
                latitude, longitude
        );

        // Создаем новый поток для выполнения HTTP-запроса
        new Thread(() -> {
            // Выполняем запрос и получаем результат
            String output = apiClient.getUrlContent(urlAddress);

            // Проверяем, что ответ не пустой
            if (output == null || output.isEmpty()) {
                javafx.application.Platform.runLater(() ->
                        showAlert("Ошибка", "Не удалось получить данные от API.")
                );
                return;
            }

            // Извлекаем оптимальный угол наклона (Slope)
            optimalSlope = dataProcessor.extractOptimalSlope(output);

            // Обрабатываем CSV-ответ
            dataProcessor.processData(output, ghiList, t2mList);

            // Проверяем количество данных
            if (ghiList.size() != 8760 || t2mList.size() != 8760) {
                javafx.application.Platform.runLater(() ->
                        showAlert("Ошибка", "Количество данных не соответствует ожидаемому (8760). Проверьте формат данных.")
                );
                return;
            }

            // Сохраняем данные в текстовый файл
            fileWriterService.saveDataToFile("output.txt", ghiList, t2mList, optimalSlope);

            // Устанавливаем маркер на земном шаре
            double lat = Double.parseDouble(latitude);
            double lon = Double.parseDouble(longitude);

            // Обновляем UI в основном потоке
            javafx.application.Platform.runLater(() -> {
                globeView.setMarker(lat, lon);
                histogramView.updateData(ghiList); // Обновляем гистограмму

                // Обновляем значения в интерфейсе
                optimalSlopeLabel.setText(String.format("%.2f°", optimalSlope)); // Оптимальный угол
                averageGhiLabel.setText(String.format("%.2f", calculateAverage(ghiList))); // Среднее значение GHI
            });

            // Выводим данные Оптимального угла наклона, G(i) и T2m в консоль для проверки
            System.out.println("Оптимальный угол наклона (Slope): " + optimalSlope);
            System.out.println("Данные G(i): " + ghiList);
            System.out.println("Данные T2m: " + t2mList);
        }).start(); // Запускаем поток
    }

    /**
     * Метод для расчета среднего значения из списка.
     */
    private double calculateAverage(List<Double> list) {
        if (list.isEmpty()) return 0;
        double sum = 0;
        for (Double value : list) {
            sum += value;
        }
        return sum;
    }

    /**
     * Метод для отображения уведомления пользователю.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}