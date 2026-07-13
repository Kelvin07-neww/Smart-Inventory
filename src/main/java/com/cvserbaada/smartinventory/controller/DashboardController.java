package com.cvserbaada.smartinventory.controller;

import com.cvserbaada.smartinventory.model.DashboardStatistics;
import com.cvserbaada.smartinventory.model.User;
import com.cvserbaada.smartinventory.service.DashboardService;
import com.cvserbaada.smartinventory.util.SessionManager;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardController {
    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.of("id", "ID"));
    private static final Duration REFRESH_INTERVAL = Duration.seconds(30);

    private final DashboardService dashboardService = new DashboardService();
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.of("id", "ID"));
    private final Timeline refreshTimeline = new Timeline(new KeyFrame(REFRESH_INTERVAL, event -> refreshDashboard()));

    @FXML
    private VBox dashboardRoot;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label totalBarangLabel;

    @FXML
    private Label totalSupplierLabel;

    @FXML
    private Label totalPenjualanHariIniLabel;

    @FXML
    private Label totalNilaiInventoryLabel;

    @FXML
    private Label barangCaptionLabel;

    @FXML
    private Label supplierCaptionLabel;

    @FXML
    private Label penjualanCaptionLabel;

    @FXML
    private Label inventoryCaptionLabel;

    @FXML
    private Label appVersionLabel;

    @FXML
    private Label databaseStatusLabel;

    @FXML
    private Label loginStatusLabel;

    @FXML
    private Label roleUserLabel;

    @FXML
    private TableView<DashboardStatistics.ProductSales> bestProductTable;

    @FXML
    private TableColumn<DashboardStatistics.ProductSales, String> bestProductCodeColumn;

    @FXML
    private TableColumn<DashboardStatistics.ProductSales, String> bestProductNameColumn;

    @FXML
    private TableColumn<DashboardStatistics.ProductSales, Number> bestProductQtyColumn;

    @FXML
    private TableColumn<DashboardStatistics.ProductSales, BigDecimal> bestProductValueColumn;

    @FXML
    private TableView<DashboardStatistics.LowStockItem> lowStockTable;

    @FXML
    private TableColumn<DashboardStatistics.LowStockItem, String> lowStockCodeColumn;

    @FXML
    private TableColumn<DashboardStatistics.LowStockItem, String> lowStockNameColumn;

    @FXML
    private TableColumn<DashboardStatistics.LowStockItem, Number> lowStockQtyColumn;

    @FXML
    private TableView<DashboardStatistics.RecentTransaction> recentTransactionTable;

    @FXML
    private TableColumn<DashboardStatistics.RecentTransaction, String> transactionInvoiceColumn;

    @FXML
    private TableColumn<DashboardStatistics.RecentTransaction, String> transactionDateColumn;

    @FXML
    private TableColumn<DashboardStatistics.RecentTransaction, String> transactionCashierColumn;

    @FXML
    private TableColumn<DashboardStatistics.RecentTransaction, BigDecimal> transactionTotalColumn;

    @FXML
    private LineChart<String, Number> monthlySalesChart;

    @FXML
    private BarChart<String, Number> stockBarChart;

    @FXML
    private PieChart categoryPieChart;

    @FXML
    private void initialize() {
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(2);
        initializeHeader();
        initializeInfoPanel();
        initializeTables();
        initializeCharts();
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        dashboardRoot.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                refreshTimeline.stop();
            } else {
                refreshTimeline.play();
            }
        });
        refreshDashboard();
        playDashboardEntranceAnimation();
    }

    private void initializeHeader() {
        String userName = SessionManager.getCurrentUser()
                .map(User::getNama)
                .orElse("Guest");

        welcomeLabel.setText("Welcome back, " + userName);
        dateLabel.setText(LocalDate.now().format(DATE_FORMATTER));
    }

    private void initializeInfoPanel() {
        String role = SessionManager.getCurrentUser()
                .map(User::getRole)
                .orElse("Guest");

        appVersionLabel.setText("SmartInventorySystem v1.0.0");
        databaseStatusLabel.setText("Checking...");
        loginStatusLabel.setText("Active");
        roleUserLabel.setText(role);
    }

    private void initializeTables() {
        bestProductCodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().kodeBarang()));
        bestProductNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().namaBarang()));
        bestProductQtyColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().totalQty()));
        bestProductValueColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().totalNilai()));
        bestProductValueColumn.setCellFactory(column -> new CurrencyTableCell<DashboardStatistics.ProductSales>());

        lowStockCodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().kodeBarang()));
        lowStockNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().namaBarang()));
        lowStockQtyColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().stok()));

        transactionInvoiceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().noFaktur()));
        transactionDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().tanggal()));
        transactionCashierColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().kasir()));
        transactionTotalColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().grandTotal()));
        transactionTotalColumn.setCellFactory(column -> new CurrencyTableCell<DashboardStatistics.RecentTransaction>());
    }

    private void initializeCharts() {
        monthlySalesChart.setAnimated(false);
        monthlySalesChart.setLegendVisible(false);
        stockBarChart.setAnimated(false);
        stockBarChart.setLegendVisible(false);
        categoryPieChart.setLegendVisible(true);
    }

    private void refreshDashboard() {
        try {
            DashboardStatistics statistics = dashboardService.loadStatistics();
            updateCards(statistics);
            updateTables(statistics);
            updateCharts(statistics);
            databaseStatusLabel.setText("Online");
            databaseStatusLabel.getStyleClass().remove("error-text");
            if (!databaseStatusLabel.getStyleClass().contains("success-text")) {
                databaseStatusLabel.getStyleClass().add("success-text");
            }
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to refresh realtime dashboard.", exception);
            databaseStatusLabel.setText("Database Offline");
            databaseStatusLabel.getStyleClass().remove("success-text");
            if (!databaseStatusLabel.getStyleClass().contains("error-text")) {
                databaseStatusLabel.getStyleClass().add("error-text");
            }
        }
    }

    private void updateCards(DashboardStatistics statistics) {
        totalBarangLabel.setText(formatNumber(statistics.getTotalBarang()));
        totalSupplierLabel.setText(formatNumber(statistics.getTotalSupplier()));
        totalPenjualanHariIniLabel.setText(formatCurrency(statistics.getTotalPenjualanHariIni()));
        totalNilaiInventoryLabel.setText(formatCurrency(statistics.getTotalNilaiInventory()));
        barangCaptionLabel.setText("Realtime dari tabel barang");
        supplierCaptionLabel.setText("Realtime dari tabel supplier");
        penjualanCaptionLabel.setText("Total transaksi hari ini");
        inventoryCaptionLabel.setText("SUM harga beli x stok");
    }

    private void updateTables(DashboardStatistics statistics) {
        bestProductTable.setItems(FXCollections.observableArrayList(statistics.getProdukTerlaris()));
        lowStockTable.setItems(FXCollections.observableArrayList(statistics.getBarangStokMenipis()));
        recentTransactionTable.setItems(FXCollections.observableArrayList(statistics.getTransaksiTerbaru()));
    }

    private void updateCharts(DashboardStatistics statistics) {
        XYChart.Series<String, Number> monthlySeries = new XYChart.Series<>();
        statistics.getPenjualanBulanan().forEach(item -> monthlySeries.getData().add(
                new XYChart.Data<>(item.bulan(), item.total())
        ));
        ObservableList<XYChart.Series<String, Number>> monthlyData = FXCollections.observableArrayList();
        monthlyData.add(monthlySeries);
        monthlySalesChart.setData(monthlyData);

        XYChart.Series<String, Number> stockSeries = new XYChart.Series<>();
        statistics.getStokTerbanyak().forEach(item -> stockSeries.getData().add(
                new XYChart.Data<>(item.namaBarang(), item.stok())
        ));
        ObservableList<XYChart.Series<String, Number>> stockData = FXCollections.observableArrayList();
        stockData.add(stockSeries);
        stockBarChart.setData(stockData);

        ObservableList<PieChart.Data> categoryData = FXCollections.observableArrayList();
        statistics.getDistribusiKategori().forEach(item -> categoryData.add(
                new PieChart.Data(item.namaKategori(), item.totalBarang())
        ));
        categoryPieChart.setData(categoryData);

        playFadeAnimation(monthlySalesChart, 260);
        playFadeAnimation(stockBarChart, 260);
        playFadeAnimation(categoryPieChart, 260);
    }

    private String formatNumber(Number value) {
        return numberFormat.format(value == null ? 0 : value);
    }

    private String formatCurrency(BigDecimal value) {
        return "Rp " + numberFormat.format(value == null ? BigDecimal.ZERO : value);
    }

    private void playFadeAnimation(Node node, int millis) {
        node.setOpacity(0);
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(millis), node);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }

    private void playDashboardEntranceAnimation() {
        List<Node> animatedNodes = new ArrayList<>();
        collectAnimatedNodes(dashboardRoot, animatedNodes);

        for (int i = 0; i < animatedNodes.size(); i++) {
            Node node = animatedNodes.get(i);
            node.setOpacity(0);
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(420), node);
            fadeTransition.setDelay(Duration.millis(i * 45L));
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);
            fadeTransition.play();
        }
    }

    private void collectAnimatedNodes(Parent parent, List<Node> animatedNodes) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child.getStyleClass().contains("metric-card") || child.getStyleClass().contains("dashboard-panel")) {
                animatedNodes.add(child);
            }
            if (child instanceof Parent childParent) {
                collectAnimatedNodes(childParent, animatedNodes);
            }
        }
    }

    private class CurrencyTableCell<S> extends TableCell<S, BigDecimal> {
        @Override
        protected void updateItem(BigDecimal value, boolean empty) {
            super.updateItem(value, empty);
            setText(empty ? null : formatCurrency(value));
        }
    }
}
