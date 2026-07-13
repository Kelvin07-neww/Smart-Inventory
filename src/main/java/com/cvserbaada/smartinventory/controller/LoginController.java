package com.cvserbaada.smartinventory.controller;

import com.cvserbaada.smartinventory.model.User;
import com.cvserbaada.smartinventory.service.AuthErrorType;
import com.cvserbaada.smartinventory.service.AuthException;
import com.cvserbaada.smartinventory.service.AuthService;
import com.cvserbaada.smartinventory.util.AlertUtil;
import com.cvserbaada.smartinventory.util.SessionManager;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

public class LoginController {
    private static final Duration INTRO_DURATION = Duration.millis(420);
    private final AuthService authService = new AuthService();

    @FXML
    private VBox loginCard;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberMeCheckBox;

    @FXML
    private Label messageLabel;

    @FXML
    private void initialize() {
        playIntroAnimation();
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isBlank()) {
            AlertUtil.showWarning("Validasi Login", "Email tidak boleh kosong.");
            emailField.requestFocus();
            return;
        }

        if (password.isBlank()) {
            AlertUtil.showWarning("Validasi Login", "Password tidak boleh kosong.");
            passwordField.requestFocus();
            return;
        }

        try {
            User user = authService.login(email, password);
            SessionManager.setCurrentUser(user);
            AlertUtil.showSuccess("Login Berhasil", "Selamat datang, " + user.getNama() + ".");
            openDashboard();
        } catch (AuthException exception) {
            handleAuthException(exception);
        }
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    private void playIntroAnimation() {
        loginCard.setOpacity(0);
        loginCard.setTranslateY(18);

        FadeTransition fadeTransition = new FadeTransition(INTRO_DURATION, loginCard);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);

        TranslateTransition translateTransition = new TranslateTransition(INTRO_DURATION, loginCard);
        translateTransition.setFromY(18);
        translateTransition.setToY(0);

        fadeTransition.play();
        translateTransition.play();
    }

    private void openDashboard() {
        try {
            Parent dashboardRoot = FXMLLoader.load(Objects.requireNonNull(
                    getClass().getResource("/fxml/MainLayout.fxml"),
                    "FXML resource not found: /fxml/MainLayout.fxml"
            ));
            Scene scene = loginCard.getScene();
            scene.setRoot(dashboardRoot);
        } catch (IOException exception) {
            AlertUtil.showError("Navigation Error", "Gagal membuka Dashboard: " + exception.getMessage());
        }
    }

    private void handleAuthException(AuthException exception) {
        if (exception.getErrorType() == AuthErrorType.EMAIL_NOT_FOUND) {
            AlertUtil.showError("Login Gagal", "Email tidak ditemukan.");
            emailField.requestFocus();
            return;
        }

        if (exception.getErrorType() == AuthErrorType.INVALID_PASSWORD) {
            AlertUtil.showError("Login Gagal", "Password salah.");
            passwordField.clear();
            passwordField.requestFocus();
            return;
        }

        AlertUtil.showError("Database Error", exception.getMessage());
    }
}
