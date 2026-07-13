package com.cvserbaada.smartinventory.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.util.stream.Stream;

public class SidebarController {
    private MainLayoutController mainLayoutController;

    @FXML
    private VBox menuContainer;

    @FXML
    private Button logoutButton;

    @FXML
    private void initialize() {
        setActiveMenu("Dashboard");
    }

    @FXML
    private void selectDashboard() {
        if (mainLayoutController == null) {
            return;
        }
        mainLayoutController.showDashboard();
        setActiveMenu("Dashboard");
    }

    @FXML
    private void selectKategori() {
        if (mainLayoutController == null) {
            return;
        }
        mainLayoutController.showKategori();
        setActiveMenu("Kategori");
    }

    @FXML
    private void selectBarang() {
        if (mainLayoutController == null) {
            return;
        }
        mainLayoutController.showBarang();
        setActiveMenu("Barang");
    }

    @FXML
    private void selectSupplier() {
        if (mainLayoutController == null) {
            return;
        }
        mainLayoutController.showSupplier();
        setActiveMenu("Supplier");
    }

    @FXML
    private void selectPenjualan() {
        if (mainLayoutController == null) {
            return;
        }
        mainLayoutController.showPenjualan();
        setActiveMenu("Penjualan");
    }

    @FXML
    private void selectLaporan() {
        if (mainLayoutController == null) {
            return;
        }
        mainLayoutController.showLaporan();
        setActiveMenu("Laporan");
    }

    @FXML
    private void selectSettings() {
        if (mainLayoutController == null) {
            return;
        }
        mainLayoutController.showSettings();
        setActiveMenu("Settings");
    }

    @FXML
    private void selectLogout() {
        if (mainLayoutController == null) {
            return;
        }
        mainLayoutController.logout();
    }

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    private void setActiveMenu(String menuName) {
        menuButtons()
                .forEach(button -> button.getStyleClass().remove("active"));

        menuButtons()
                .filter(button -> menuName.equals(button.getUserData()))
                .findFirst()
                .ifPresent(button -> button.getStyleClass().add("active"));
    }

    private Stream<Button> menuButtons() {
        return Stream.concat(
                menuContainer.getChildren().stream()
                        .filter(Button.class::isInstance)
                        .map(Button.class::cast),
                Stream.of(logoutButton)
        );
    }
}
