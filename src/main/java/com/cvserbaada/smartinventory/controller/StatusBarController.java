package com.cvserbaada.smartinventory.controller;

import com.cvserbaada.smartinventory.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class StatusBarController {
    @FXML
    private Label databaseStatusLabel;

    @FXML
    private Label userLabel;

    @FXML
    private Label versionLabel;

    @FXML
    private void initialize() {
        databaseStatusLabel.setText("Database: Ready");
        String currentUser = SessionManager.getCurrentUser()
                .map(user -> user.getNama() + " (" + user.getRole() + ")")
                .orElse("Guest");
        userLabel.setText("User: " + currentUser);
        versionLabel.setText("Version: 1.0-SNAPSHOT");
    }
}
