package com.openlib.fx;

import com.openlib.fx.service.LocalDataService;
import com.openlib.fx.util.NavigationManager;
import com.openlib.fx.util.SessionManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Punto de entrada principal de OpenLib Market — JavaFX Client.
 * Sin backend: toda la persistencia se maneja en archivos JSON locales (./data/).
 */
public class OpenLibApp extends Application {

    public static final String APP_TITLE = "OpenLib Market";
    public static final String API_BASE  = "http://localhost:8080/api";

    @Override
    public void start(Stage primaryStage) {
        // LocalDataService.init(); // Deshabilitado para usar backend real

        primaryStage.setTitle(APP_TITLE);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(700);
        primaryStage.setWidth(1280);
        primaryStage.setHeight(800);

        NavigationManager.init(primaryStage);
        SessionManager.clear();

        NavigationManager.navigateTo("login");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
