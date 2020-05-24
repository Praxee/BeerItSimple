package view.businessTask;

import com.jfoenix.controls.JFXButton;
import controller.ProductController;
import exception.DateException;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import model.OrderTableFormat;
import model.Product;
import model.ProductIncome;
import utils.Validators;
import view.PopUp;
import view.View;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.function.Function;

public class incomeView extends View {

    @FXML
    private DatePicker startDate;
    @FXML
    private DatePicker endDate;
    @FXML
    private JFXButton refreshBtn;
    @FXML
    private TableView<ProductIncome> incomeTable;
    @FXML
    private TableColumn<ProductIncome, Integer> productCode;
    @FXML
    private TableColumn<ProductIncome, String> productName;
    @FXML
    private TableColumn<ProductIncome, Integer> amountSold;
    @FXML
    private TableColumn<ProductIncome, Double> income;
    @FXML
    private TableColumn<ProductIncome, Double> salePercentage;
    @FXML
    private PieChart pieChart;
    @FXML
    private Text totalIncome;

    @Override
    public void init() {
        refreshBtn.setOnAction(e -> {
            LocalDate start = startDate.getValue();
            LocalDate end = endDate.getValue();

            if (Validators.validateBetweenDates(start, end)) {
                ArrayList<ProductIncome> productIncomes = new ProductController().getAllProductsIncome(start, end);
                updateTable(productIncomes);

            } else {
                new DateException();
            }
        });

        startDate.setValue(LocalDate.now().minusYears(1));
        endDate.setValue(LocalDate.now());

        ArrayList<ProductIncome> productIncomes = new ProductController().getAllProductsIncome(startDate.getValue(), endDate.getValue());
        double total = 0;
        for (ProductIncome p : productIncomes) {
            total += p.getSalePercentageNumber();
        }

        pieChart.setLabelLineLength(50);
        pieChart.setLabelsVisible(true);
        initTable();
        updateTable(productIncomes);
    }

    void initTable() {
        productCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        productName.setCellValueFactory(new PropertyValueFactory<>("label"));
        amountSold.setCellValueFactory(new PropertyValueFactory<>("amountSold"));
        income.setCellValueFactory(new PropertyValueFactory<>("income"));
        salePercentage.setCellValueFactory(new PropertyValueFactory<>("salePercentage"));

        incomeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        for (int i = 0; i < incomeTable.getColumns().size(); i++) {
            incomeTable.getColumns().get(i).prefWidthProperty().bind(incomeTable.widthProperty().multiply(((double) 1 / incomeTable.getColumns().size()) - 0.001));
        }
    }

    void updateTable(ArrayList<ProductIncome> products) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        products.forEach(p -> pieChartData.add(new PieChart.Data(p.getLabel(), p.getSalePercentage())));
        pieChart.setData(pieChartData);

        incomeTable.getItems().setAll(products);

        double total = products.stream()
                                .mapToDouble(ProductIncome::getIncome)
                                .sum();

        totalIncome.setText(String.format("%.2f", total));
    }
}
