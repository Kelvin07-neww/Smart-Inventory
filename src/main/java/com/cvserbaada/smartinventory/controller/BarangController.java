package com.cvserbaada.smartinventory.controller;

import com.cvserbaada.smartinventory.model.Barang;
import com.cvserbaada.smartinventory.model.Kategori;
import com.cvserbaada.smartinventory.model.Supplier;
import com.cvserbaada.smartinventory.service.BarangService;
import com.cvserbaada.smartinventory.util.AlertUtil;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BarangController {
    private static final Logger LOGGER = Logger.getLogger(BarangController.class.getName());
    private static final int PAGE_SIZE = 10;

    private final BarangService barangService = new BarangService();
    private final ObservableList<Barang> barangList = FXCollections.observableArrayList();
    private final ObservableList<Kategori> kategoriOptions = FXCollections.observableArrayList();
    private final ObservableList<Supplier> supplierOptions = FXCollections.observableArrayList();
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.of("id", "ID"));

    private Barang selectedBarang;
    private int currentPageIndex;

    @FXML
    private BorderPane barangRoot;

    @FXML
    private TextField searchField;

    @FXML
    private TextField kodeBarangField;

    @FXML
    private TextField namaBarangField;

    @FXML
    private ComboBox<Kategori> kategoriComboBox;

    @FXML
    private ComboBox<Supplier> supplierComboBox;

    @FXML
    private TextField hargaBeliField;

    @FXML
    private TextField hargaJualField;

    @FXML
    private TextField stokField;

    @FXML
    private TableView<Barang> barangTable;

    @FXML
    private TableColumn<Barang, Number> noColumn;

    @FXML
    private TableColumn<Barang, String> kodeColumn;

    @FXML
    private TableColumn<Barang, String> namaColumn;

    @FXML
    private TableColumn<Barang, String> kategoriColumn;

    @FXML
    private TableColumn<Barang, String> supplierColumn;

    @FXML
    private TableColumn<Barang, BigDecimal> hargaBeliColumn;

    @FXML
    private TableColumn<Barang, BigDecimal> hargaJualColumn;

    @FXML
    private TableColumn<Barang, Number> stokColumn;

    @FXML
    private Pagination pagination;

    @FXML
    private Label totalDataLabel;

    @FXML
    private Button editButton;

    @FXML
    private Button hapusButton;

    @FXML
    private void initialize() {
        currencyFormat.setMinimumFractionDigits(0);
        currencyFormat.setMaximumFractionDigits(2);
        initializeComboBoxes();
        initializeTable();
        initializePagination();
        initializeSearch();
        initializeSelection();
        setFormMode(false);
        playFadeAnimation();
        Platform.runLater(() -> {
            loadReferenceData();
            refreshData();
            generateNextKodeBarang();
        });
    }

    @FXML
    private void handleTambah() {
        clearForm();
        namaBarangField.requestFocus();
    }

    @FXML
    private void handleSimpan() {
        try {
            barangService.create(readForm(0));
            AlertUtil.showSuccess("Berhasil", "Data barang berhasil ditambahkan.");
            clearForm();
            refreshData();
        } catch (IllegalArgumentException exception) {
            AlertUtil.showWarning("Validasi", exception.getMessage());
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to save barang.", exception);
            AlertUtil.showError("Database Error", "Gagal menyimpan barang: " + exception.getMessage());
        }
    }

    @FXML
    private void handleEdit() {
        if (selectedBarang == null) {
            AlertUtil.showWarning("Validasi", "Pilih data barang yang akan diedit.");
            return;
        }

        try {
            barangService.update(readForm(selectedBarang.getId()));
            AlertUtil.showSuccess("Berhasil", "Data barang berhasil diedit.");
            clearForm();
            refreshData();
        } catch (IllegalArgumentException exception) {
            AlertUtil.showWarning("Validasi", exception.getMessage());
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to update barang.", exception);
            AlertUtil.showError("Database Error", "Gagal mengedit barang: " + exception.getMessage());
        }
    }

    @FXML
    private void handleHapus() {
        if (selectedBarang == null) {
            AlertUtil.showWarning("Validasi", "Pilih data barang yang akan dihapus.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation(
                "Konfirmasi Hapus",
                "Hapus barang " + selectedBarang.getNamaBarang() + "?"
        );
        if (!confirmed) {
            return;
        }

        try {
            barangService.delete(selectedBarang);
            AlertUtil.showSuccess("Berhasil", "Data barang berhasil dihapus.");
            clearForm();
            refreshData();
        } catch (IllegalArgumentException exception) {
            AlertUtil.showWarning("Validasi", exception.getMessage());
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to delete barang.", exception);
            AlertUtil.showError("Database Error", "Gagal menghapus barang: " + exception.getMessage());
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
        loadReferenceData();
        refreshData();
    }

    @FXML
    private void handleImportExcel() {
        AlertUtil.showWarning("Import Excel", "Struktur import Excel sudah disiapkan. Implementasi Apache POI dilakukan pada tahap berikutnya.");
    }

    @FXML
    private void handleExportExcel() {
        AlertUtil.showWarning("Export Excel", "Struktur export Excel sudah disiapkan. Implementasi Apache POI dilakukan pada tahap berikutnya.");
    }

    private void initializeComboBoxes() {
        kategoriComboBox.setItems(kategoriOptions);
        supplierComboBox.setItems(supplierOptions);

        kategoriComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Kategori kategori) {
                return kategori == null ? "" : kategori.getKodeKategori() + " - " + kategori.getNamaKategori();
            }

            @Override
            public Kategori fromString(String value) {
                return null;
            }
        });

        supplierComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Supplier supplier) {
                return supplier == null ? "" : supplier.getKodeSupplier() + " - " + supplier.getNamaSupplier();
            }

            @Override
            public Supplier fromString(String value) {
                return null;
            }
        });
    }

    private void initializeTable() {
        noColumn.setCellValueFactory(cellData -> {
            int rowIndex = barangTable.getItems().indexOf(cellData.getValue()) + 1;
            return new ReadOnlyObjectWrapper<>(currentPageIndex * PAGE_SIZE + rowIndex);
        });
        kodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKodeBarang()));
        namaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNamaBarang()));
        kategoriColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKategoriNama()));
        supplierColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSupplierNama()));
        hargaBeliColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getHargaBeli()));
        hargaJualColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getHargaJual()));
        hargaBeliColumn.setCellFactory(column -> new CurrencyTableCell());
        hargaJualColumn.setCellFactory(column -> new CurrencyTableCell());
        stokColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getStok()));
        barangTable.setItems(barangList);
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

    private void initializeSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentPageIndex = 0;
            refreshData();
        });
    }

    private void initializeSelection() {
        barangTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedBarang = newValue;
            if (newValue != null) {
                fillForm(newValue);
            }
            setFormMode(newValue != null);
        });
    }

    private void loadReferenceData() {
        try {
            kategoriOptions.setAll(barangService.findKategoriOptions());
            supplierOptions.setAll(barangService.findSupplierOptions());
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to load barang references.", exception);
            AlertUtil.showError("Database Error", "Gagal memuat data kategori/supplier: " + exception.getMessage());
        }
    }

    private void refreshData() {
        try {
            String keyword = searchField.getText();
            int totalData = barangService.count(keyword);
            int pageCount = Math.max(1, (int) Math.ceil((double) totalData / PAGE_SIZE));

            if (currentPageIndex >= pageCount) {
                currentPageIndex = pageCount - 1;
            }

            totalDataLabel.setText("Total Data: " + totalData);
            configurePagination(pageCount);
            loadCurrentPage();
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to load barang data.", exception);
            AlertUtil.showError("Database Error", "Gagal memuat data barang: " + exception.getMessage());
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
            List<Barang> data = barangService.findPage(searchField.getText(), currentPageIndex, PAGE_SIZE);
            barangList.setAll(data);
            barangTable.refresh();
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to load barang page.", exception);
            AlertUtil.showError("Database Error", "Gagal memuat halaman data: " + exception.getMessage());
        }
    }

    private Barang readForm(int id) {
        Kategori kategori = kategoriComboBox.getValue();
        Supplier supplier = supplierComboBox.getValue();

        return new Barang(
                id,
                kodeBarangField.getText(),
                namaBarangField.getText(),
                kategori == null ? 0 : kategori.getId(),
                kategori == null ? "" : kategori.getNamaKategori(),
                supplier == null ? 0 : supplier.getId(),
                supplier == null ? "" : supplier.getNamaSupplier(),
                parsePrice(hargaBeliField.getText(), "Harga beli"),
                parsePrice(hargaJualField.getText(), "Harga jual"),
                parseStock(stokField.getText())
        );
    }

    private BigDecimal parsePrice(String value, String fieldName) {
        String normalizedValue = normalizeNumberInput(value);
        if (normalizedValue.isBlank()) {
            throw new IllegalArgumentException(fieldName + " wajib diisi.");
        }

        try {
            return new BigDecimal(normalizedValue);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " harus angka.");
        }
    }

    private int parseStock(String value) {
        String normalizedValue = normalizeNumberInput(value);
        if (normalizedValue.isBlank()) {
            throw new IllegalArgumentException("Stok wajib diisi.");
        }

        try {
            return Integer.parseInt(normalizedValue);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Stok harus angka.");
        }
    }

    private String normalizeNumberInput(String value) {
        return value == null ? "" : value.trim().replace("_", "").replace(" ", "");
    }

    private void fillForm(Barang barang) {
        kodeBarangField.setText(barang.getKodeBarang());
        namaBarangField.setText(barang.getNamaBarang());
        kategoriComboBox.setValue(findKategoriById(barang.getKategoriId()));
        supplierComboBox.setValue(findSupplierById(barang.getSupplierId()));
        hargaBeliField.setText(formatPlainNumber(barang.getHargaBeli()));
        hargaJualField.setText(formatPlainNumber(barang.getHargaJual()));
        stokField.setText(String.valueOf(barang.getStok()));
    }

    private Kategori findKategoriById(int kategoriId) {
        return kategoriOptions.stream()
                .filter(kategori -> kategori.getId() == kategoriId)
                .findFirst()
                .orElse(null);
    }

    private Supplier findSupplierById(int supplierId) {
        return supplierOptions.stream()
                .filter(supplier -> supplier.getId() == supplierId)
                .findFirst()
                .orElse(null);
    }

    private void clearForm() {
        selectedBarang = null;
        barangTable.getSelectionModel().clearSelection();
        kodeBarangField.clear();
        namaBarangField.clear();
        kategoriComboBox.getSelectionModel().clearSelection();
        supplierComboBox.getSelectionModel().clearSelection();
        hargaBeliField.clear();
        hargaJualField.clear();
        stokField.clear();
        setFormMode(false);
        generateNextKodeBarang();
    }

    private void generateNextKodeBarang() {
        try {
            kodeBarangField.setText(barangService.generateNextKode());
        } catch (SQLException exception) {
            LOGGER.log(Level.WARNING, "Failed to generate next barang code.", exception);
            kodeBarangField.clear();
        }
    }

    private void setFormMode(boolean editing) {
        editButton.setDisable(!editing);
        hapusButton.setDisable(!editing);
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        return currencyFormat.format(value);
    }

    private String formatPlainNumber(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private void playFadeAnimation() {
        barangRoot.setOpacity(0);
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(280), barangRoot);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }

    private class CurrencyTableCell extends TableCell<Barang, BigDecimal> {
        @Override
        protected void updateItem(BigDecimal value, boolean empty) {
            super.updateItem(value, empty);
            setText(empty ? null : formatCurrency(value));
        }
    }
}
