package com.cvserbaada.smartinventory.controller;

import com.cvserbaada.smartinventory.model.Supplier;
import com.cvserbaada.smartinventory.service.SupplierService;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SupplierController {
    private static final Logger LOGGER = Logger.getLogger(SupplierController.class.getName());
    private static final int PAGE_SIZE = 10;

    private final SupplierService supplierService = new SupplierService();
    private final ObservableList<Supplier> supplierList = FXCollections.observableArrayList();

    private Supplier selectedSupplier;
    private int currentPageIndex;

    @FXML
    private BorderPane supplierRoot;

    @FXML
    private TextField searchField;

    @FXML
    private TextField kodeSupplierField;

    @FXML
    private TextField namaSupplierField;

    @FXML
    private TextArea alamatField;

    @FXML
    private TextField teleponField;

    @FXML
    private TableView<Supplier> supplierTable;

    @FXML
    private TableColumn<Supplier, Number> noColumn;

    @FXML
    private TableColumn<Supplier, String> kodeColumn;

    @FXML
    private TableColumn<Supplier, String> namaColumn;

    @FXML
    private TableColumn<Supplier, String> alamatColumn;

    @FXML
    private TableColumn<Supplier, String> teleponColumn;

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
        initializeTable();
        initializePagination();
        initializeSearch();
        initializeSelection();
        refreshData();
        setFormMode(false);
        generateNextKodeSupplier();
        playFadeAnimation();
    }

    @FXML
    private void handleTambah() {
        clearForm();
        namaSupplierField.requestFocus();
    }

    @FXML
    private void handleSimpan() {
        try {
            supplierService.create(readForm(0));
            AlertUtil.showSuccess("Berhasil", "Data supplier berhasil ditambahkan.");
            clearForm();
            refreshData();
        } catch (IllegalArgumentException exception) {
            AlertUtil.showWarning("Validasi", exception.getMessage());
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to save supplier.", exception);
            AlertUtil.showError("Database Error", "Gagal menyimpan supplier: " + exception.getMessage());
        }
    }

    @FXML
    private void handleEdit() {
        if (selectedSupplier == null) {
            AlertUtil.showWarning("Validasi", "Pilih data supplier yang akan diedit.");
            return;
        }

        try {
            supplierService.update(readForm(selectedSupplier.getId()));
            AlertUtil.showSuccess("Berhasil", "Data supplier berhasil diedit.");
            clearForm();
            refreshData();
        } catch (IllegalArgumentException exception) {
            AlertUtil.showWarning("Validasi", exception.getMessage());
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to update supplier.", exception);
            AlertUtil.showError("Database Error", "Gagal mengedit supplier: " + exception.getMessage());
        }
    }

    @FXML
    private void handleHapus() {
        if (selectedSupplier == null) {
            AlertUtil.showWarning("Validasi", "Pilih data supplier yang akan dihapus.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation(
                "Konfirmasi Hapus",
                "Hapus supplier " + selectedSupplier.getNamaSupplier() + "?"
        );
        if (!confirmed) {
            return;
        }

        try {
            supplierService.delete(selectedSupplier);
            AlertUtil.showSuccess("Berhasil", "Data supplier berhasil dihapus.");
            clearForm();
            refreshData();
        } catch (IllegalArgumentException exception) {
            AlertUtil.showWarning("Validasi", exception.getMessage());
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to delete supplier.", exception);
            AlertUtil.showError("Database Error", "Gagal menghapus supplier: " + exception.getMessage());
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

    @FXML
    private void handleExportPdf() {
        try {
            int totalData = supplierService.count(searchField.getText());
            if (totalData == 0) {
                AlertUtil.showWarning("Export PDF", "Tidak ada data supplier untuk diexport.");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Supplier PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName("laporan-supplier.pdf");
            File file = fileChooser.showSaveDialog(supplierRoot.getScene().getWindow());
            if (file == null) {
                return;
            }

            List<Supplier> suppliers = supplierService.findPage(searchField.getText(), 0, totalData);
            exportSuppliersToPdf(file, suppliers);
            AlertUtil.showSuccess("Export PDF", "Data supplier berhasil diexport ke PDF.");
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to load supplier data for PDF export.", exception);
            AlertUtil.showError("Database Error", "Gagal memuat data export: " + exception.getMessage());
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Failed to export supplier PDF.", exception);
            AlertUtil.showError("Export PDF", "Gagal export PDF: " + exception.getMessage());
        }
    }

    private void initializeTable() {
        noColumn.setCellValueFactory(cellData -> {
            int rowIndex = supplierTable.getItems().indexOf(cellData.getValue()) + 1;
            return new ReadOnlyObjectWrapper<>(currentPageIndex * PAGE_SIZE + rowIndex);
        });
        kodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKodeSupplier()));
        namaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNamaSupplier()));
        alamatColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAlamat()));
        teleponColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTelepon()));
        supplierTable.setItems(supplierList);
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
        supplierTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedSupplier = newValue;
            if (newValue != null) {
                fillForm(newValue);
            }
            setFormMode(newValue != null);
        });
    }

    private void refreshData() {
        try {
            String keyword = searchField.getText();
            int totalData = supplierService.count(keyword);
            int pageCount = Math.max(1, (int) Math.ceil((double) totalData / PAGE_SIZE));

            if (currentPageIndex >= pageCount) {
                currentPageIndex = pageCount - 1;
            }

            totalDataLabel.setText("Total Data: " + totalData);
            configurePagination(pageCount);
            loadCurrentPage();
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to load supplier data.", exception);
            AlertUtil.showError("Database Error", "Gagal memuat data supplier: " + exception.getMessage());
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
            List<Supplier> data = supplierService.findPage(searchField.getText(), currentPageIndex, PAGE_SIZE);
            supplierList.setAll(data);
            supplierTable.refresh();
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to load supplier page.", exception);
            AlertUtil.showError("Database Error", "Gagal memuat halaman data: " + exception.getMessage());
        }
    }

    private Supplier readForm(int id) {
        return new Supplier(
                id,
                kodeSupplierField.getText(),
                namaSupplierField.getText(),
                alamatField.getText(),
                teleponField.getText()
        );
    }

    private void fillForm(Supplier supplier) {
        kodeSupplierField.setText(supplier.getKodeSupplier());
        namaSupplierField.setText(supplier.getNamaSupplier());
        alamatField.setText(supplier.getAlamat());
        teleponField.setText(supplier.getTelepon());
    }

    private void clearForm() {
        selectedSupplier = null;
        supplierTable.getSelectionModel().clearSelection();
        kodeSupplierField.clear();
        namaSupplierField.clear();
        alamatField.clear();
        teleponField.clear();
        setFormMode(false);
        generateNextKodeSupplier();
    }

    private void generateNextKodeSupplier() {
        try {
            kodeSupplierField.setText(supplierService.generateNextKode());
        } catch (SQLException exception) {
            LOGGER.log(Level.WARNING, "Failed to generate next supplier code.", exception);
            kodeSupplierField.clear();
        }
    }

    private void setFormMode(boolean editing) {
        editButton.setDisable(!editing);
        hapusButton.setDisable(!editing);
    }

    private void playFadeAnimation() {
        supplierRoot.setOpacity(0);
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(280), supplierRoot);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }

    private void exportSuppliersToPdf(File file, List<Supplier> suppliers) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                stream.beginText();
                stream.newLineAtOffset(50, 790);
                stream.showText("Laporan Supplier - SMART INVENTORY & SALES MANAGEMENT SYSTEM");
                stream.endText();

                stream.setFont(new PDType1Font(Standard14Fonts.FontName.COURIER), 9);
                stream.beginText();
                stream.setLeading(13.0f);
                stream.newLineAtOffset(50, 765);
                stream.showText(String.format("%-5s %-15s %-28s %-18s %s", "No", "Kode", "Nama", "Telepon", "Alamat"));
                stream.newLine();
                stream.showText("------------------------------------------------------------------------------------------");
                stream.newLine();

                for (int i = 0; i < suppliers.size(); i++) {
                    Supplier supplier = suppliers.get(i);
                    stream.showText(String.format(
                            "%-5d %-15s %-28s %-18s %s",
                            i + 1,
                            trimForPdf(supplier.getKodeSupplier(), 14),
                            trimForPdf(supplier.getNamaSupplier(), 27),
                            trimForPdf(supplier.getTelepon(), 17),
                            trimForPdf(supplier.getAlamat(), 28)
                    ));
                    stream.newLine();
                }
                stream.endText();
            }

            document.save(file);
        }
    }

    private String trimForPdf(String value, int maxLength) {
        String safeValue = value == null ? "" : value;
        return safeValue.length() <= maxLength ? safeValue : safeValue.substring(0, maxLength - 3) + "...";
    }
}
