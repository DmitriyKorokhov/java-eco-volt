package com.example.fem;

import javafx.fxml.FXML;
import javafx.scene.SubScene;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.PerspectiveCamera;

import java.util.ArrayList;
import java.util.List;

public class Controller {
    @FXML
    private TextField latitudeField;

    @FXML
    private TextField longitudeField;

    @FXML
    private AnchorPane globeContainer; // Контейнер для SubScene

    private GlobeView globeView;
    private final ApiClient apiClient = new ApiClient();
    private final DataProcessor dataProcessor = new DataProcessor();
    private final FileWriterService fileWriterService = new FileWriterService();

    // Списки для хранения данных G(i) и T2m
    private final List<Double> ghiList = new ArrayList<>();
    private final List<Double> t2mList = new ArrayList<>();

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
                "https://re.jrc.ec.europa.eu/api/seriescalc?lat=%s&lon=%s&startyear=2023&endyear=2023",
                latitude, longitude
        );

        // Выполняем запрос и получаем результат
        String output = apiClient.getUrlContent(urlAddress);

        // Проверяем, что ответ не пустой
        if (output == null || output.isEmpty()) {
            showAlert("Ошибка", "Не удалось получить данные от API.");
            return;
        }

        // Обрабатываем CSV-ответ
        dataProcessor.processData(output, ghiList, t2mList);

        // Проверяем количество данных
        if (ghiList.size() != 8760 || t2mList.size() != 8760) {
            showAlert("Ошибка", "Количество данных не соответствует ожидаемому (8760). Проверьте формат данных.");
            return;
        }

        // Сохраняем данные в текстовый файл
        fileWriterService.saveDataToFile("output.txt", ghiList, t2mList);

        // Устанавливаем маркер на земном шаре
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);
        globeView.setMarker(lat, lon);

        // Выводим данные G(i) и T2m в консоль для проверки
        System.out.println("Данные G(i): " + ghiList);
        System.out.println("Данные T2m: " + t2mList);
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