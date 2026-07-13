package com.cvserbaada.smartinventory.controller;

import com.cvserbaada.smartinventory.model.Barang;
import com.cvserbaada.smartinventory.model.DetailPenjualan;
import com.cvserbaada.smartinventory.model.Penjualan;
import com.cvserbaada.smartinventory.model.User;
import com.cvserbaada.smartinventory.service.PenjualanService;
import com.cvserbaada.smartinventory.util.AlertUtil;
import com.cvserbaada.smartinventory.util.SessionManager;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PenjualanController {
    private static final Logger LOGGER = Logger.getLogger(PenjualanController.class.getName());
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PenjualanService penjualanService = new PenjualanService();
    private final ObservableList<Barang> barangOptions = FXCollections.observableArrayList();
    private final ObservableList<DetailPenjualan> cartItems = FXCollections.observableArrayList();
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.of("id", "ID"));

    private User currentUser;

    @FXML
    private BorderPane penjualanRoot;

    @FXML
    private Label noFakturLabel;

    @FXML
    private Label tanggalLabel;

    @FXML
    private Label kasirLabel;

    @FXML
    private TextField searchBarangField;

    @FXML
    private ComboBox<Barang> barangComboBox;

    @FXML
    private TextField namaBarangField;

    @FXML
    private TextField hargaField;

    @FXML
    private TextField stokField;

    @FXML
    private TextField qtyField;

    @FXML
    private Label itemSubtotalLabel;

    @FXML
    private TableView<DetailPenjualan> cartTable;

    @FXML
    private TableColumn<DetailPenjualan, Number> noColumn;

    @FXML
    private TableColumn<DetailPenjualan, String> kodeColumn;

    @FXML
    private TableColumn<DetailPenjualan, String> namaColumn;

    @FXML
    private TableColumn<DetailPenjualan, BigDecimal> hargaColumn;

    @FXML
    private TableColumn<DetailPenjualan, Integer> qtyColumn;

    @FXML
    private TableColumn<DetailPenjualan, BigDecimal> subtotalColumn;

    @FXML
    private TextField diskonField;

    @FXML
    private TextField ppnField;

    @FXML
    private Label subtotalLabel;

    @FXML
    private Label grandTotalLabel;

    @FXML
    private Button hapusBarangButton;

    @FXML
    private void initialize() {
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(2);
        currentUser = SessionManager.getCurrentUser().orElse(null);

        initializeHeader();
        initializeBarangComboBox();
        initializeTable();
        initializeListeners();
        playFadeAnimation();
        Platform.runLater(this::resetForm);
    }

    @FXML
    private void handleTambahBarang() {
        Barang selectedBarang = barangComboBox.getValue();
        if (selectedBarang == null) {
            AlertUtil.showWarning("Validasi", "Pilih barang terlebih dahulu.");
            return;
        }

        int qty;
        try {
            qty = parseQty(qtyField.getText());
        } catch (IllegalArgumentException exception) {
            AlertUtil.showWarning("Validasi", exception.getMessage());
            return;
        }

        if (isBarangAlreadyInCart(selectedBarang.getId())) {
            AlertUtil.showWarning("Validasi", "Barang tidak boleh duplikat dalam keranjang.");
            return;
        }

        if (qty > selectedBarang.getStok()) {
            AlertUtil.showError("Stok Tidak Cukup", "Qty tidak boleh melebihi stok tersedia.");
            return;
        }

        DetailPenjualan detail = new DetailPenjualan(
                0,
                0,
                selectedBarang.getId(),
                selectedBarang.getKodeBarang(),
                selectedBarang.getNamaBarang(),
                selectedBarang.getHargaJual(),
                qty,
                selectedBarang.getStok()
        );
        cartItems.add(detail);
        clearBarangForm();
        recalculateSummary();
    }

    @FXML
    private void handleHapusBarang() {
        DetailPenjualan selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            AlertUtil.showWarning("Validasi", "Pilih barang di keranjang yang akan dihapus.");
            return;
        }

        cartItems.remove(selectedItem);
        recalculateSummary();
    }

    @FXML
    private void handleSimpanTransaksi() {
        boolean confirmed = AlertUtil.showConfirmation("Konfirmasi Transaksi", "Simpan transaksi penjualan ini?");
        if (!confirmed) {
            return;
        }

        try {
            Penjualan penjualan = buildPenjualan();
            penjualanService.saveTransaction(penjualan);
            AlertUtil.showSuccess("Transaksi Berhasil", "Transaksi berhasil disimpan. Invoice dapat dipreview dan diexport dari menu Laporan.");
            resetForm();
        } catch (IllegalArgumentException exception) {
            AlertUtil.showWarning("Validasi", exception.getMessage());
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to save sales transaction.", exception);
            AlertUtil.showError("Transaksi Gagal", exception.getMessage());
        }
    }

    @FXML
    private void handleReset() {
        boolean confirmed = cartItems.isEmpty() || AlertUtil.showConfirmation("Konfirmasi Reset", "Reset transaksi saat ini?");
        if (confirmed) {
            resetForm();
        }
    }

    @FXML
    private void handleCetakInvoice() {
        AlertUtil.showWarning("Cetak Invoice", "Simpan transaksi terlebih dahulu, lalu cetak invoice dari menu Laporan.");
    }

    private void initializeHeader() {
        kasirLabel.setText(currentUser == null ? "-" : currentUser.getNama());
        tanggalLabel.setText(LocalDateTime.now().format(DATE_TIME_FORMATTER));
    }

    private void initializeBarangComboBox() {
        barangComboBox.setItems(barangOptions);
        barangComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Barang barang) {
                return barang == null ? "" : barang.getKodeBarang() + " - " + barang.getNamaBarang();
            }

            @Override
            public Barang fromString(String value) {
                return null;
            }
        });
    }

    private void initializeTable() {
        cartTable.setEditable(true);
        noColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cartTable.getItems().indexOf(cellData.getValue()) + 1));
        kodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKodeBarang()));
        namaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNamaBarang()));
        hargaColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getHarga()));
        hargaColumn.setCellFactory(column -> new CurrencyTableCell());
        qtyColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getQty()));
        qtyColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        qtyColumn.setOnEditCommit(event -> updateCartQty(event.getRowValue(), event.getNewValue()));
        subtotalColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getSubtotal()));
        subtotalColumn.setCellFactory(column -> new CurrencyTableCell());
        cartTable.setItems(cartItems);
    }

    private void initializeListeners() {
        searchBarangField.textProperty().addListener((observable, oldValue, newValue) -> loadBarangOptions(newValue));
        barangComboBox.valueProperty().addListener((observable, oldValue, newValue) -> fillBarangInfo(newValue));
        qtyField.textProperty().addListener((observable, oldValue, newValue) -> updateItemSubtotal());
        diskonField.textProperty().addListener((observable, oldValue, newValue) -> recalculateSummary());
        ppnField.textProperty().addListener((observable, oldValue, newValue) -> recalculateSummary());
        cartTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> hapusBarangButton.setDisable(newValue == null));
    }

    private void loadBarangOptions(String keyword) {
        try {
            List<Barang> data = penjualanService.findBarangForSale(keyword);
            barangOptions.setAll(data);
            if (!data.isEmpty() && barangComboBox.getScene() != null) {
                barangComboBox.show();
            }
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to load sales items.", exception);
            AlertUtil.showError("Database Error", "Gagal memuat data barang: " + exception.getMessage());
        }
    }

    private void fillBarangInfo(Barang barang) {
        if (barang == null) {
            namaBarangField.clear();
            hargaField.clear();
            stokField.clear();
            itemSubtotalLabel.setText(formatCurrency(BigDecimal.ZERO));
            return;
        }

        namaBarangField.setText(barang.getNamaBarang());
        hargaField.setText(formatCurrency(barang.getHargaJual()));
        stokField.setText(String.valueOf(barang.getStok()));
        if (qtyField.getText().isBlank()) {
            qtyField.setText("1");
        }
        updateItemSubtotal();
    }

    private void updateItemSubtotal() {
        Barang selectedBarang = barangComboBox.getValue();
        if (selectedBarang == null) {
            itemSubtotalLabel.setText(formatCurrency(BigDecimal.ZERO));
            return;
        }

        try {
            int qty = parseQty(qtyField.getText());
            itemSubtotalLabel.setText(formatCurrency(selectedBarang.getHargaJual().multiply(BigDecimal.valueOf(qty))));
        } catch (IllegalArgumentException exception) {
            itemSubtotalLabel.setText(formatCurrency(BigDecimal.ZERO));
        }
    }

    private void updateCartQty(DetailPenjualan detail, Integer newQty) {
        if (newQty == null || newQty < 1) {
            AlertUtil.showWarning("Validasi", "Qty minimal 1.");
            cartTable.refresh();
            return;
        }

        if (newQty > detail.getStokTersedia()) {
            AlertUtil.showError("Stok Tidak Cukup", "Qty tidak boleh melebihi stok tersedia.");
            cartTable.refresh();
            return;
        }

        detail.setQty(newQty);
        cartTable.refresh();
        recalculateSummary();
    }

    private Penjualan buildPenjualan() {
        BigDecimal subtotal = calculateSubtotal();
        BigDecimal diskon = parseMoney(diskonField.getText(), "Diskon");
        BigDecimal ppn = parseMoney(ppnField.getText(), "PPN");
        BigDecimal grandTotal = subtotal.subtract(diskon).add(ppn);

        return new Penjualan(
                0,
                noFakturLabel.getText(),
                LocalDateTime.now(),
                currentUser == null ? 0 : currentUser.getId(),
                currentUser == null ? "" : currentUser.getNama(),
                subtotal,
                diskon,
                ppn,
                grandTotal,
                List.copyOf(cartItems)
        );
    }

    private void recalculateSummary() {
        BigDecimal subtotal = calculateSubtotal();
        BigDecimal diskon = safeParseMoney(diskonField.getText());
        BigDecimal ppn = safeParseMoney(ppnField.getText());
        BigDecimal grandTotal = subtotal.subtract(diskon).add(ppn);

        subtotalLabel.setText(formatCurrency(subtotal));
        grandTotalLabel.setText(formatCurrency(grandTotal.max(BigDecimal.ZERO)));
    }

    private BigDecimal calculateSubtotal() {
        return cartItems.stream()
                .map(DetailPenjualan::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int parseQty(String value) {
        String normalizedValue = value == null ? "" : value.trim();
        if (normalizedValue.isBlank()) {
            throw new IllegalArgumentException("Qty wajib diisi.");
        }

        try {
            int qty = Integer.parseInt(normalizedValue);
            if (qty < 1) {
                throw new IllegalArgumentException("Qty minimal 1.");
            }
            return qty;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Qty harus angka.");
        }
    }

    private BigDecimal parseMoney(String value, String fieldName) {
        String normalizedValue = normalizeMoney(value);
        if (normalizedValue.isBlank()) {
            return BigDecimal.ZERO;
        }

        try {
            BigDecimal result = new BigDecimal(normalizedValue);
            if (result.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(fieldName + " tidak boleh negatif.");
            }
            return result;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " harus angka.");
        }
    }

    private BigDecimal safeParseMoney(String value) {
        try {
            return parseMoney(value, "Nilai");
        } catch (IllegalArgumentException exception) {
            return BigDecimal.ZERO;
        }
    }

    private String normalizeMoney(String value) {
        return value == null ? "" : value.trim().replace("_", "").replace(" ", "");
    }

    private boolean isBarangAlreadyInCart(int barangId) {
        return cartItems.stream().anyMatch(detail -> detail.getBarangId() == barangId);
    }

    private void resetForm() {
        cartItems.clear();
        clearBarangForm();
        diskonField.setText("0");
        ppnField.setText("0");
        tanggalLabel.setText(LocalDateTime.now().format(DATE_TIME_FORMATTER));
        hapusBarangButton.setDisable(true);
        loadBarangOptions("");
        generateNoFaktur();
        recalculateSummary();
    }

    private void clearBarangForm() {
        searchBarangField.clear();
        barangComboBox.getSelectionModel().clearSelection();
        namaBarangField.clear();
        hargaField.clear();
        stokField.clear();
        qtyField.setText("1");
        itemSubtotalLabel.setText(formatCurrency(BigDecimal.ZERO));
    }

    private void generateNoFaktur() {
        try {
            noFakturLabel.setText(penjualanService.generateNoFaktur());
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to generate invoice number.", exception);
            AlertUtil.showError("Database Error", "Gagal membuat nomor faktur: " + exception.getMessage());
            noFakturLabel.setText("-");
        }
    }

    private String formatCurrency(BigDecimal value) {
        return numberFormat.format(value == null ? BigDecimal.ZERO : value);
    }

    private void playFadeAnimation() {
        penjualanRoot.setOpacity(0);
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(280), penjualanRoot);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }

    private class CurrencyTableCell extends TableCell<DetailPenjualan, BigDecimal> {
        @Override
        protected void updateItem(BigDecimal value, boolean empty) {
            super.updateItem(value, empty);
            setText(empty ? null : formatCurrency(value));
        }
    }
}
