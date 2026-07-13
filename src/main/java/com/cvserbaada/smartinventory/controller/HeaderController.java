package com.cvserbaada.smartinventory.controller;

import com.cvserbaada.smartinventory.model.User;
import com.cvserbaada.smartinventory.util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class HeaderController {
    private static final DateTimeFormatter CLOCK_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy", Locale.of("id", "ID"));

    @FXML
    private Label clockLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label userLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private void initialize() {
        initializeUserInfo();
        updateClock();
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateClock()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateClock() {
        LocalDateTime now = LocalDateTime.now();
        clockLabel.setText(now.format(CLOCK_FORMATTER));
        dateLabel.setText(now.format(DATE_FORMATTER));
    }

    private void initializeUserInfo() {
        User currentUser = SessionManager.getCurrentUser().orElse(null);
        userLabel.setText(currentUser == null ? "Guest" : currentUser.getNama());
        roleLabel.setText(currentUser == null ? "Guest" : currentUser.getRole());
    }
}
