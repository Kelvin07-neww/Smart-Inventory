package com.cvserbaada.smartinventory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App extends Application {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    private static final String APPLICATION_TITLE = "SMART INVENTORY & SALES MANAGEMENT SYSTEM";
    private static final String APPLICATION_ICON = "/icons/smart-inventory.ico";
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 720;

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = loadRootLayout();
            Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            applyStylesheet(scene);

            primaryStage.setTitle(APPLICATION_TITLE);
            applyApplicationIcon(primaryStage);
            primaryStage.setResizable(true);
            primaryStage.setMaximized(true);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Failed to start JavaFX application.", exception);
            throw new IllegalStateException("Application startup failed.", exception);
        }
    }

    private Parent loadRootLayout() throws IOException {
        URL fxmlUrl = Objects.requireNonNull(
                App.class.getResource("/fxml/Login.fxml"),
                "FXML resource not found: /fxml/Login.fxml"
        );
        return FXMLLoader.load(fxmlUrl);
    }

    private void applyStylesheet(Scene scene) {
        addStylesheet(scene, "/css/main.css");
        addStylesheet(scene, "/css/header.css");
        addStylesheet(scene, "/css/sidebar.css");
        addStylesheet(scene, "/css/home.css");
        addStylesheet(scene, "/css/statusbar.css");
        addStylesheet(scene, "/css/login.css");
        addStylesheet(scene, "/css/dashboard.css");
        addStylesheet(scene, "/css/kategori.css");
        addStylesheet(scene, "/css/supplier.css");
        addStylesheet(scene, "/css/barang.css");
        addStylesheet(scene, "/css/penjualan.css");
        addStylesheet(scene, "/css/laporan.css");
        addStylesheet(scene, "/css/settings.css");
        addStylesheet(scene, "/css/theme.css");
    }

    private void applyApplicationIcon(Stage stage) {
        URL iconUrl = App.class.getResource(APPLICATION_ICON);
        if (iconUrl != null) {
            stage.getIcons().add(new Image(iconUrl.toExternalForm()));
        }
    }

    private void addStylesheet(Scene scene, String resourcePath) {
        URL cssUrl = Objects.requireNonNull(
                App.class.getResource(resourcePath),
                "CSS resource not found: " + resourcePath
        );
        scene.getStylesheets().add(cssUrl.toExternalForm());
    }
}
