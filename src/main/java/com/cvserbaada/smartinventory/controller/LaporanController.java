package com.cvserbaada.smartinventory.controller;

import com.cvserbaada.smartinventory.model.DetailPenjualan;
import com.cvserbaada.smartinventory.model.Penjualan;
import com.cvserbaada.smartinventory.service.PenjualanService;
import com.cvserbaada.smartinventory.util.AlertUtil;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class LaporanController {
    private static final Logger LOGGER = LoggerFactory.getLogger(LaporanController.class);
    private static final int REPORT_LIMIT = 100;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PenjualanService penjualanService = new PenjualanService();
    private final ObservableList<Penjualan> penjualanList = FXCollections.observableArrayList();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));

    @FXML
    private BorderPane laporanRoot;

    @FXML
    private TableView<Penjualan> laporanTable;

    @FXML
    private TableColumn<Penjualan, Number> noColumn;

    @FXML
    private TableColumn<Penjualan, String> fakturColumn;

    @FXML
    private TableColumn<Penjualan, String> tanggalColumn;

    @FXML
    private TableColumn<Penjualan, String> kasirColumn;

    @FXML
    private TableColumn<Penjualan, BigDecimal> totalColumn;

    @FXML
    private TextArea previewArea;

    @FXML
    private Label totalTransaksiLabel;

    @FXML
    private Label totalNilaiLabel;

    @FXML
    private void initialize() {
        initializeTable();
        refreshReport();
    }

    @FXML
    private void handleRefresh() {
        refreshReport();
    }

    @FXML
    private void handlePreview() {
        Penjualan selectedPenjualan = laporanTable.getSelectionModel().getSelectedItem();
        if (selectedPenjualan == null) {
            AlertUtil.showWarning("Preview Laporan", "Pilih transaksi yang akan dipreview.");
            return;
        }

        previewArea.setText(buildInvoicePreview(selectedPenjualan));
    }

    @FXML
    private void handlePrint() {
        if (previewArea.getText().isBlank()) {
            handlePreview();
        }
        if (previewArea.getText().isBlank()) {
            return;
        }

        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob == null || !printerJob.showPrintDialog(laporanRoot.getScene().getWindow())) {
            return;
        }

        boolean printed = printerJob.printPage(previewArea);
        if (printed) {
            printerJob.endJob();
            AlertUtil.showSuccess("Print Laporan", "Laporan berhasil dikirim ke printer.");
        } else {
            AlertUtil.showError("Print Laporan", "Laporan gagal dicetak.");
        }
    }

    @FXML
    private void handleExportPdf() {
        if (previewArea.getText().isBlank()) {
            handlePreview();
        }
        if (previewArea.getText().isBlank()) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Laporan PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("laporan-penjualan.pdf");
        File file = fileChooser.showSaveDialog(laporanRoot.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            exportPreviewToPdf(file, previewArea.getText());
            AlertUtil.showSuccess("Export PDF", "Laporan berhasil diexport ke PDF.");
        } catch (IOException exception) {
            LOGGER.error("Failed to export sales report PDF.", exception);
            AlertUtil.showError("Export PDF", "Gagal export PDF: " + exception.getMessage());
        }
    }

    private void initializeTable() {
        noColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(laporanTable.getItems().indexOf(cellData.getValue()) + 1));
        fakturColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNoFaktur()));
        tanggalColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTanggal().format(DATE_TIME_FORMATTER)));
        kasirColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKasir()));
        totalColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getGrandTotal()));
        totalColumn.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormat.format(item));
            }
        });
        laporanTable.setItems(penjualanList);
        laporanTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                previewArea.setText(buildInvoicePreview(newValue));
            }
        });
    }

    private void refreshReport() {
        try {
            penjualanList.setAll(penjualanService.findRecentSales(REPORT_LIMIT));
            updateSummary();
            if (!penjualanList.isEmpty()) {
                laporanTable.getSelectionModel().selectFirst();
            } else {
                previewArea.setText("Belum ada transaksi penjualan untuk ditampilkan.");
            }
        } catch (SQLException exception) {
            LOGGER.error("Failed to load sales report.", exception);
            AlertUtil.showError("Database Error", "Gagal memuat laporan: " + exception.getMessage());
        }
    }

    private void updateSummary() {
        BigDecimal totalNilai = penjualanList.stream()
                .map(Penjualan::getGrandTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalTransaksiLabel.setText(String.valueOf(penjualanList.size()));
        totalNilaiLabel.setText(currencyFormat.format(totalNilai));
    }

    private String buildInvoicePreview(Penjualan penjualan) {
        try {
            List<DetailPenjualan> details = penjualanService.findDetailsByPenjualanId(penjualan.getId());
            StringBuilder builder = new StringBuilder();
            builder.append("SMART INVENTORY & SALES MANAGEMENT SYSTEM\n");
            builder.append("CV. SERBA ADA\n");
            builder.append("========================================\n");
            builder.append("No Faktur : ").append(penjualan.getNoFaktur()).append('\n');
            builder.append("Tanggal   : ").append(penjualan.getTanggal().format(DATE_TIME_FORMATTER)).append('\n');
            builder.append("Kasir     : ").append(penjualan.getKasir()).append('\n');
            builder.append("========================================\n");
            for (DetailPenjualan detail : details) {
                builder.append(detail.getKodeBarang()).append(" - ").append(detail.getNamaBarang()).append('\n');
                builder.append(detail.getQty()).append(" x ").append(currencyFormat.format(detail.getHarga()))
                        .append(" = ").append(currencyFormat.format(detail.getSubtotal())).append('\n');
            }
            builder.append("========================================\n");
            builder.append("Subtotal    : ").append(currencyFormat.format(penjualan.getSubtotal())).append('\n');
            builder.append("Diskon      : ").append(currencyFormat.format(penjualan.getDiskon())).append('\n');
            builder.append("PPN         : ").append(currencyFormat.format(penjualan.getPpn())).append('\n');
            builder.append("Grand Total : ").append(currencyFormat.format(penjualan.getGrandTotal())).append('\n');
            return builder.toString();
        } catch (SQLException exception) {
            LOGGER.error("Failed to build invoice preview.", exception);
            AlertUtil.showError("Database Error", "Gagal memuat detail laporan: " + exception.getMessage());
            return "";
        }
    }

    private void exportPreviewToPdf(File file, String content) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.COURIER), 10);
                stream.beginText();
                stream.setLeading(14.5f);
                stream.newLineAtOffset(50, 780);
                for (String line : content.split("\\R")) {
                    stream.showText(line.length() > 90 ? line.substring(0, 90) : line);
                    stream.newLine();
                }
                stream.endText();
            }

            document.save(file);
        }
    }
}
