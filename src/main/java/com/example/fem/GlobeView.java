package com.example.fem;

import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

public class GlobeView {
    private final Group root = new Group();
    private final Group globeGroup = new Group(); // Группа для земного шара и маркера
    private final Sphere globe;
    private final Sphere marker;
    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

    public GlobeView() {
        // Создаем земной шар
        globe = new Sphere(150);

        // Загружаем текстуру
        PhongMaterial earthMaterial = new PhongMaterial();
        try {
            Image texture = new Image("file:world_map.jpg"); // Убедитесь, что путь к файлу правильный
            if (texture.isError()) {
                throw texture.getException();
            }
            earthMaterial.setDiffuseMap(texture);
            System.out.println("Текстура загружена успешно.");
        } catch (Exception e) {
            System.err.println("Ошибка загрузки текстуры: " + e.getMessage());
            // Используем цвет по умолчанию, если текстура не загружена
            earthMaterial.setDiffuseColor(Color.BLUE);
        }
        globe.setMaterial(earthMaterial);

        // Создаем маркер
        marker = new Sphere(5);
        marker.setMaterial(new PhongMaterial(Color.RED));

        // Добавляем земной шар и маркер в одну группу
        globeGroup.getChildren().addAll(globe, marker);
        root.getChildren().add(globeGroup);

        // Инициализируем вращение
        globeGroup.getTransforms().addAll(rotateX, rotateY);

        // Обработчики событий мыши для вращения
        root.setOnMousePressed(this::handleMousePressed);
        root.setOnMouseDragged(this::handleMouseDragged);
    }

    public Group getRoot() {
        return root;
    }

    /**
     * Устанавливает маркер на заданные координаты (широта и долгота).
     *
     * @param latitude  Широта в градусах (от -90 до 90).
     * @param longitude Долгота в градусах (от -180 до 180).
     */
    public void setMarker(double latitude, double longitude) {
        // Преобразуем широту и долготу в радианы
        double latRad = Math.toRadians(latitude - 12);
        double lonRad = Math.toRadians(longitude);

        double radius = globe.getRadius();

        // Преобразуем широту и долготу в декартовы координаты
        // Учитываем ориентацию осей в JavaFX и направление текстуры
        double x = radius * Math.cos(latRad) * Math.sin(lonRad); // X направлен вправо
        double y = -radius * Math.sin(latRad); // Y вниз, северная широта положительна
        double z = -radius * Math.cos(latRad) * Math.cos(lonRad); // Z направлен вглубь экрана

        // Устанавливаем позицию маркера
        marker.setTranslateX(x);
        marker.setTranslateY(y);
        marker.setTranslateZ(z);
    }

    private void handleMousePressed(MouseEvent event) {
        anchorX = event.getSceneX();
        anchorY = event.getSceneY();
        anchorAngleX = rotateX.getAngle();
        anchorAngleY = rotateY.getAngle();
    }

    private void handleMouseDragged(MouseEvent event) {
        rotateX.setAngle(anchorAngleX - (anchorY - event.getSceneY()));
        rotateY.setAngle(anchorAngleY + (anchorX - event.getSceneX()));
    }
}