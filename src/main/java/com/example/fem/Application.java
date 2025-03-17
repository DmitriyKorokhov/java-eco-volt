package com.example.fem;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("EcoVolt");
        stage.setScene(scene);

        // Включаем возможность переключения в полноэкранный режим
        stage.setFullScreen(true);

        // Обработчик событий для переключения между полноэкранным и оконным режимами
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case F11:
                    stage.setFullScreen(!stage.isFullScreen());
                    break;
                case ESCAPE:
                    if (stage.isFullScreen()) {
                        stage.setFullScreen(false);
                    }
                    break;
            }
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}