package com.cvserbaada.smartinventory.controller;

import com.cvserbaada.smartinventory.util.AlertUtil;
import com.cvserbaada.smartinventory.util.SessionManager;
import com.cvserbaada.smartinventory.util.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainLayoutController {
    private static final Logger LOGGER = Logger.getLogger(MainLayoutController.class.getName());
    private static final Duration CONTENT_TRANSITION_DURATION = Duration.millis(180);

    @FXML
    private StackPane contentArea;

    @FXML
    private SidebarController sidebarController;

    @FXML
    private void initialize() {
        Platform.runLater(() -> ThemeManager.applyCurrentTheme(contentArea.getScene()));
        sidebarController.setMainLayoutController(this);
        showDashboard();
    }

    public void logout() {
        if (!AlertUtil.showConfirmation("Logout", "Anda yakin ingin keluar dari aplikasi?")) {
            return;
        }

        SessionManager.clearSession();
        try {
            Parent loginRoot = FXMLLoader.load(Objects.requireNonNull(
                    getClass().getResource("/fxml/Login.fxml"),
                    "FXML resource not found: /fxml/Login.fxml"
            ));
            Scene scene = contentArea.getScene();
            scene.setRoot(loginRoot);
            ThemeManager.applyCurrentTheme(scene);
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Failed to logout and open login view.", exception);
            AlertUtil.showError("Navigation Error", "Gagal kembali ke halaman login: " + exception.getMessage());
        }
    }

    public void showDashboard() {
        setContent(loadView("/fxml/Dashboard.fxml"));
    }

    public void showKategori() {
        setContent(loadView("/fxml/Kategori.fxml"));
    }

    public void showSupplier() {
        setContent(loadView("/fxml/Supplier.fxml"));
    }

    public void showBarang() {
        setContent(loadView("/fxml/Barang.fxml"));
    }

    public void showPenjualan() {
        setContent(loadView("/fxml/Penjualan.fxml"));
    }

    public void showLaporan() {
        setContent(loadView("/fxml/Laporan.fxml"));
    }

    public void showSettings() {
        setContent(loadView("/fxml/Settings.fxml"));
    }

    private Node loadView(String resourcePath) {
        try {
            return FXMLLoader.load(Objects.requireNonNull(
                    getClass().getResource(resourcePath),
                    "FXML resource not found: " + resourcePath
            ));
        } catch (IOException | RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Failed to load view: " + resourcePath, exception);
            return createLoadErrorView(resourcePath, exception);
        }
    }

    private Node createLoadErrorView(String resourcePath, Exception exception) {
        Label titleLabel = new Label("View gagal dibuka");
        titleLabel.getStyleClass().add("load-error-title");

        Label messageLabel = new Label(resourcePath + "\n" + exception.getMessage());
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("load-error-message");

        VBox container = new VBox(10, titleLabel, messageLabel);
        container.getStyleClass().add("load-error-panel");
        return container;
    }

    private void setContent(Node node) {
        node.setOpacity(0);
        contentArea.getChildren().setAll(node);

        FadeTransition fadeTransition = new FadeTransition(CONTENT_TRANSITION_DURATION, node);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }
}
