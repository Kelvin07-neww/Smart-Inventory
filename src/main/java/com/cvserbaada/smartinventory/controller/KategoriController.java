package com.cvserbaada.smartinventory.controller;

import com.cvserbaada.smartinventory.model.Kategori;
import com.cvserbaada.smartinventory.service.KategoriService;
import com.cvserbaada.smartinventory.util.AlertUtil;
import javafx.animation.FadeTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KategoriController {
    private static final Logger LOGGER = Logger.getLogger(KategoriController.class.getName());
    private static final int PAGE_SIZE = 10;

    private final KategoriService kategoriService = new KategoriService();
    private final ObservableList<Kategori> kategoriList = FXCollections.observableArrayList();

    private Kategori selectedKategori;
    private int currentPageIndex;

    @FXML
    private BorderPane kategoriRoot;

    @FXML
    private TextField searchField;

    @FXML
    private TextField kodeKategoriField;

    @FXML
    private TextField namaKategoriField;

    @FXML
    private TableView<Kategori> kategoriTable;

    @FXML
    private TableColumn<Kategori, Number> noColumn;

    @FXML
    private TableColumn<Kategori, String> kodeColumn;

    @FXML
    private TableColumn<Kategori, String> namaColumn;

    @FXML
    private Pagination pagination;

    @FXML
    private Label totalDataLabel;

    @FXML
    private Button ubahButton;

    @FXML
    private Button hapusButton;

    @FXML
    private void initialize() {
        initializeTable();
        initializePagination();
        initializeSearch();
        initializeSelection();
        refreshData();
        setFormMode(false);
        generateNextKodeKategori();
        playFadeAnimation();
    }

    @FXML
    private void handleTambah() {
        clearForm();
        namaKategoriField.requestFocus();
    }

    @FXML
    private void handleSimpan() {
        try {
            kategoriService.create(readForm(0));
            AlertUtil.showSuccess("Berhasil", "Data kategori berhasil ditambahkan.");
            clearForm();
            refreshData();
        } catch (IllegalArgumentException exception) {
            AlertUtil.showWarning("Validasi", exception.getMessage());
        } catch (SQLException exception) {
            AlertUtil.showError("Database Error", "Gagal menyimpan kategori: " + exception.getMessage());
        }
    }

    @FXML
    private void handleUbah() {
        if (selectedKategori == null) {
            AlertUtil.showWarning("Validasi", "Pilih data kategori yang akan diubah.");
            return;
        }

        try {
            kategoriService.update(readForm(selectedKategori.getId()));
            AlertUtil.showSuccess("Berhasil", "Data kategori berhasil diubah.");
            clearForm();
            refreshData();
        } catch (IllegalArgumentException exception) {
            AlertUtil.showWarning("Validasi", exception.getMessage());
        } catch (SQLException exception) {
            AlertUtil.showError("Database Error", "Gagal mengubah kategori: " + exception.getMessage());
        }
    }

    @FXML
    private void handleHapus() {
        if (selectedKategori == null) {
            AlertUtil.showWarning("Validasi", "Pilih data kategori yang akan dihapus.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation(
                "Konfirmasi Hapus",
                "Hapus kategori " + selectedKategori.getNamaKategori() + "?"
        );
        if (!confirmed) {
            return;
        }

        try {
            kategoriService.delete(selectedKategori);
            AlertUtil.showSuccess("Berhasil", "Data kategori berhasil dihapus.");
            clearForm();
            refreshData();
        } catch (IllegalArgumentException exception) {
            AlertUtil.showWarning("Validasi", exception.getMessage());
        } catch (SQLException exception) {
            AlertUtil.showError("Database Error", "Gagal menghapus kategori: " + exception.getMessage());
        }
    }

    @FXML
    private void handleBatal() {
        clearForm();
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        currentPageIndex = 0;
        clearForm();
        refreshData();
    }

    private void initializeTable() {
        noColumn.setCellValueFactory(cellData -> {
            int rowIndex = kategoriTable.getItems().indexOf(cellData.getValue()) + 1;
            return new ReadOnlyObjectWrapper<>(currentPageIndex * PAGE_SIZE + rowIndex);
        });
        kodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKodeKategori()));
        namaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNamaKategori()));
        kategoriTable.setItems(kategoriList);
    }

    private void initializeSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentPageIndex = 0;
            refreshData();
        });
    }

    private void initializePagination() {
        pagination.setPageFactory(pageIndex -> {
            currentPageIndex = pageIndex;
            loadCurrentPage();

            Region spacer = new Region();
            spacer.setMinHeight(0);
            spacer.setPrefHeight(0);
            spacer.setMaxHeight(0);
            return spacer;
        });
    }

    private void initializeSelection() {
        kategoriTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedKategori = newValue;
            if (newValue != null) {
                fillForm(newValue);
            }
            setFormMode(newValue != null);
        });
    }

    private void refreshData() {
        try {
            String keyword = searchField.getText();
            int totalData = kategoriService.count(keyword);
            int pageCount = Math.max(1, (int) Math.ceil((double) totalData / PAGE_SIZE));

            if (currentPageIndex >= pageCount) {
                currentPageIndex = pageCount - 1;
            }

            totalDataLabel.setText("Total Data: " + totalData);
            configurePagination(pageCount);
            loadCurrentPage();
        } catch (SQLException exception) {
            AlertUtil.showError("Database Error", "Gagal memuat data kategori: " + exception.getMessage());
        }
    }

    private void configurePagination(int pageCount) {
        pagination.setPageCount(pageCount);
        if (pagination.getCurrentPageIndex() != currentPageIndex) {
            pagination.setCurrentPageIndex(currentPageIndex);
        }
    }

    private void loadCurrentPage() {
        try {
            List<Kategori> data = kategoriService.findPage(searchField.getText(), currentPageIndex, PAGE_SIZE);
            kategoriList.setAll(data);
            kategoriTable.refresh();
        } catch (SQLException exception) {
            AlertUtil.showError("Database Error", "Gagal memuat halaman data: " + exception.getMessage());
        }
    }

    private Kategori readForm(int id) {
        return new Kategori(
                id,
                kodeKategoriField.getText(),
                namaKategoriField.getText()
        );
    }

    private void fillForm(Kategori kategori) {
        kodeKategoriField.setText(kategori.getKodeKategori());
        namaKategoriField.setText(kategori.getNamaKategori());
    }

    private void clearForm() {
        selectedKategori = null;
        kategoriTable.getSelectionModel().clearSelection();
        kodeKategoriField.clear();
        namaKategoriField.clear();
        setFormMode(false);
        generateNextKodeKategori();
    }

    private void generateNextKodeKategori() {
        try {
            kodeKategoriField.setText(kategoriService.generateNextKode());
        } catch (SQLException exception) {
            LOGGER.log(Level.WARNING, "Failed to generate next kategori code.", exception);
            kodeKategoriField.clear();
        }
    }

    private void setFormMode(boolean editing) {
        ubahButton.setDisable(!editing);
        hapusButton.setDisable(!editing);
    }

    private void playFadeAnimation() {
        kategoriRoot.setOpacity(0);
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(280), kategoriRoot);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }
}
