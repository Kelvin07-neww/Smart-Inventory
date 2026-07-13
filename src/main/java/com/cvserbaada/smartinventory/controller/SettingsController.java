package com.cvserbaada.smartinventory.controller;

import com.cvserbaada.smartinventory.model.User;
import com.cvserbaada.smartinventory.service.SettingsService;
import com.cvserbaada.smartinventory.util.AlertUtil;
import com.cvserbaada.smartinventory.util.SessionManager;
import com.cvserbaada.smartinventory.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SettingsController {
    private static final Logger LOGGER = Logger.getLogger(SettingsController.class.getName());
    private final SettingsService settingsService = new SettingsService();
    private User currentUser;

    @FXML
    private BorderPane settingsRoot;

    @FXML
    private TextField namaField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField roleField;

    @FXML
    private ImageView profileImageView;

    @FXML
    private RadioButton lightModeRadio;

    @FXML
    private RadioButton darkModeRadio;

    @FXML
    private ToggleGroup themeToggleGroup;

    @FXML
    private Label databaseStatusLabel;

    @FXML
    private Label javaVersionLabel;

    @FXML
    private void initialize() {
        loadUserProfile();
        initializeApplicationInfo();
        initializeThemeControls();
    }

    @FXML
    private void handleSave() {
        if (currentUser == null) {
            AlertUtil.showWarning("Session", "User login tidak ditemukan. Silakan logout dan login ulang.");
            return;
        }

        try {
            currentUser = settingsService.updateProfile(currentUser, namaField.getText(), emailField.getText());
            SessionManager.setCurrentUser(currentUser);
            populateProfileForm(currentUser);
            AlertUtil.showSuccess("Profil Tersimpan", "Profil user berhasil diperbarui.");
        } catch (IllegalArgumentException exception) {
            AlertUtil.showWarning("Validasi Profil", exception.getMessage());
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to update user profile.", exception);
            AlertUtil.showError("Database Error", "Gagal menyimpan profil: " + exception.getMessage());
        }
    }

    @FXML
    private void handleReset() {
        if (!AlertUtil.showConfirmation("Reset Profil", "Kembalikan form ke data user saat ini?")) {
            return;
        }

        loadUserProfile();
        syncThemeControls();
        AlertUtil.showSuccess("Reset Berhasil", "Form settings berhasil dikembalikan.");
    }

    @FXML
    private void handleLogout() {
        if (!AlertUtil.showConfirmation("Logout", "Anda yakin ingin keluar dari aplikasi?")) {
            return;
        }

        SessionManager.clearSession();
        try {
            Parent loginRoot = FXMLLoader.load(Objects.requireNonNull(
                    getClass().getResource("/fxml/Login.fxml"),
                    "FXML resource not found: /fxml/Login.fxml"
            ));
            Scene scene = settingsRoot.getScene();
            scene.setRoot(loginRoot);
            ThemeManager.applyCurrentTheme(scene);
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Failed to logout and open login view.", exception);
            AlertUtil.showError("Navigation Error", "Gagal kembali ke halaman login: " + exception.getMessage());
        }
    }

    @FXML
    private void handleChoosePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Foto Profil");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(settingsRoot.getScene().getWindow());
        if (selectedFile == null) {
            return;
        }

        profileImageView.setImage(new Image(selectedFile.toURI().toString(), true));
        AlertUtil.showSuccess("Foto Profil", "Foto profil berhasil ditampilkan. Data foto bersifat opsional dan tidak disimpan ke database.");
    }

    @FXML
    private void handleLightMode() {
        ThemeManager.applyTheme(settingsRoot.getScene(), ThemeManager.Theme.LIGHT);
    }

    @FXML
    private void handleDarkMode() {
        ThemeManager.applyTheme(settingsRoot.getScene(), ThemeManager.Theme.DARK);
    }

    private void loadUserProfile() {
        currentUser = SessionManager.getCurrentUser().orElse(null);
        if (currentUser == null) {
            AlertUtil.showWarning("Session", "User login tidak ditemukan.");
            return;
        }

        try {
            currentUser = settingsService.refreshCurrentUser(currentUser);
            SessionManager.setCurrentUser(currentUser);
        } catch (SQLException exception) {
            LOGGER.log(Level.WARNING, "Failed to refresh current user profile.", exception);
            AlertUtil.showWarning("Database Warning", "Menggunakan data session karena profil terbaru gagal dimuat.");
        }

        populateProfileForm(currentUser);
    }

    private void populateProfileForm(User user) {
        namaField.setText(user.getNama());
        emailField.setText(user.getEmail());
        roleField.setText(user.getRole());
    }

    private void initializeApplicationInfo() {
        javaVersionLabel.setText(System.getProperty("java.version"));
        boolean databaseConnected = settingsService.isDatabaseConnected();
        databaseStatusLabel.setText(databaseConnected ? "Connected" : "Disconnected");
        databaseStatusLabel.getStyleClass().removeAll("status-connected", "status-disconnected");
        databaseStatusLabel.getStyleClass().add(databaseConnected ? "status-connected" : "status-disconnected");
    }

    private void initializeThemeControls() {
        syncThemeControls();
        themeToggleGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle == lightModeRadio) {
                handleLightMode();
            } else if (newToggle == darkModeRadio) {
                handleDarkMode();
            }
        });
    }

    private void syncThemeControls() {
        if (ThemeManager.getCurrentTheme() == ThemeManager.Theme.DARK) {
            darkModeRadio.setSelected(true);
        } else {
            lightModeRadio.setSelected(true);
        }
    }
}
