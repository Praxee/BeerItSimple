package view.order;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import controller.CustomerController;
import controller.OrderController;
import controller.PaymentMethodController;
import controller.ProductController;
import exception.NoRowSelected;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import model.*;
import utils.Validators;
import view.PopUp;
import view.View;

import java.math.BigDecimal;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class Create extends View {

    @FXML
    JFXComboBox<Customer> customerList;
    @FXML
    JFXComboBox<PaymentMethod> paymentMethod;
    @FXML
    JFXCheckBox deliveryCheck;
    @FXML
    DatePicker deliveryDate;
    @FXML
    JFXButton cancelBtn;
    @FXML
    JFXButton submitBtn;
    @FXML
    JFXComboBox<Product> productList;
    @FXML
    JFXTextField productQuantity;
    @FXML
    JFXButton addArticleBtn;
    @FXML
    TableView<OrderLineTableFormat> tableArticle;
    @FXML
    TableColumn<OrderLineTableFormat, String> article;
    @FXML
    TableColumn<OrderLineTableFormat, Double> price;
    @FXML
    TableColumn<OrderLineTableFormat, Integer> quantity;
    @FXML
    TableColumn<OrderLineTableFormat, Double> totalExclVat;
    @FXML
    TableColumn<OrderLineTableFormat, Double> totalInclVat;
    @FXML
    JFXButton removeArticleBtn;
    @FXML
    Text totalAmountExclVat;
    @FXML
    Text totalAmountVatOnly;
    @FXML
    Text totalAmountVatInc;
    @FXML
    Group deliveryDisplay;

    CustomerController customerController;
    PaymentMethodController paymentMethodController;
    ProductController productController;
    OrderController orderController;

    @Override
    public void init() {
        customerController = new CustomerController();
        paymentMethodController = new PaymentMethodController();
        productController = new ProductController();
        orderController = new OrderController();

        Validators.setNumberValidator(productQuantity);

        ArrayList<Customer> allCustomers = customerController.getAllCustomers();
        customerList.setItems(FXCollections.observableArrayList(allCustomers));
        customerList.getSelectionModel().selectFirst();

        ArrayList<PaymentMethod> allPaymentMethod = paymentMethodController.getAllPaymentMethod();
        paymentMethod.setItems(FXCollections.observableArrayList(allPaymentMethod));
        paymentMethod.getSelectionModel().selectFirst();

        ArrayList<Product> allProducts = productController.getAllProducts();
        productList.setItems(FXCollections.observableArrayList(allProducts));
        productList.getSelectionModel().selectFirst();

        deliveryDisplay.setVisible(false);

       deliveryCheck.setOnAction(e -> {
           if (deliveryDisplay.isVisible()) {
               deliveryDisplay.setVisible(false);
           } else {
               deliveryDisplay.setVisible(true);
           }
       });

       initTable();

       addArticleBtn.setOnAction(e -> {
           if (!productQuantity.getText().equals("") && productQuantity.validate()) {
//               productQuantity.getStyleClass().remove("error");
               productQuantity.setStyle("");
               Product newProduct = productList.getValue();
               if (!checkPresentProduct(newProduct)) {
                    PopUp.showError("Duplicate error", "You try to add a product already present in the command !");
               } else {

                   addProduct(newProduct);
//                   String newVatAmount = String.format("%.2F", (newAmount) )
               }
           } else {
               productQuantity.setStyle("-fx-background-color: rgba(255,0,0,0.5)");
//               productQuantity.getStyleClass().add("error");

           }
       });

       removeArticleBtn.setOnAction(e -> {
           try {
               OrderLineTableFormat orderLineTableFormat = tableArticle.getSelectionModel().getSelectedItem();
               tableArticle.getItems().remove(tableArticle.getSelectionModel().getSelectedItem());
               removeProduct(orderLineTableFormat);
           } catch (NullPointerException exception) {
               new NoRowSelected();
           }
       });

       cancelBtn.setOnAction(e -> {
           closeWindow();
       });

       submitBtn.setOnAction(e -> {
           if (!tableArticle.getItems().isEmpty()) {
                if (deliveryDateCheck()) {

                    try {
                        if (newOrderInsert()) {
                            PopUp.showSuccess("New order added !", "The new order has been added successfully.");
                            Index index = (Index) getParentView();
                            index.updateTable();
                            closeWindow();
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
           }
       });
    }

    private void addProduct(Product product) {
        int productQty = Integer.parseInt(productQuantity.getText());

        OrderLineTableFormat orderLineTableFormat = new OrderLineTableFormat(product, productQty);
        tableArticle.getItems().add(orderLineTableFormat);
        double currentAmountExlVat = Double.parseDouble(totalAmountExclVat.getText().replace(',', '.'));

        double newAmountExclVat = currentAmountExlVat + orderLineTableFormat.getExclVat();
        String newTotalExclVat = String.format("%.2f", newAmountExclVat);
        totalAmountExclVat.setText(newTotalExclVat);

        double currentVatTotal = Double.parseDouble(totalAmountVatOnly.getText().replace(',', '.'));
        double newVatTotal = currentVatTotal + (orderLineTableFormat.getExclVat() * ((double) orderLineTableFormat.getVatCodeRate() / 100.0));
        String totalVat = String.format("%.2f", newVatTotal);
        totalAmountVatOnly.setText(totalVat);

        double newTotalVatIncl = newVatTotal + newAmountExclVat;
        String totalVatIncl = String.format("%.2f", newTotalVatIncl);
        totalAmountVatInc.setText(totalVatIncl);
    }

    private void removeProduct(OrderLineTableFormat orderLineTableFormat) {

        double currentAmountExlVat = Double.parseDouble(totalAmountExclVat.getText().replace(',', '.'));
        double newAmountExclVat = currentAmountExlVat - orderLineTableFormat.getExclVat();
        String newTotalExclVat = String.format("%.2f", newAmountExclVat);
        totalAmountExclVat.setText(newTotalExclVat);

        double currentVatTotal = Double.parseDouble(totalAmountVatOnly.getText().replace(',', '.'));
        double newVatTotal = 0;
        if (newAmountExclVat != 0) {
            newVatTotal = currentVatTotal - (orderLineTableFormat.getExclVat() * ((double) orderLineTableFormat.getVatCodeRate() / 100.0));
        }
        String totalVat = String.format("%.2f", newVatTotal);
        totalAmountVatOnly.setText(totalVat);

        double newTotalVatIncl = newAmountExclVat + newVatTotal;
        String totalVatIncl = String.format("%.2f", newTotalVatIncl);
        totalAmountVatInc.setText(totalVatIncl);
    }

    private boolean deliveryDateCheck() {
        if (deliveryCheck.isSelected() && LocalDate.now().isAfter(deliveryDate.getValue())) {
            PopUp.showError("Date error", "The delivery date can't be earlier than the current date.");
            return false;
        }
        return true;
    }

    public boolean checkPresentProduct(Product newProduct) {
        ArrayList<OrderLineTableFormat> products = new ArrayList<>(tableArticle.getItems());

        for (OrderLineTableFormat product : products) {
            if (product.getProductCode() == newProduct.getCode())
                return false;
        }

        return true;
    }

    private boolean newOrderInsert() throws SQLException {
        ArrayList<OrderLine> orderLines = new ArrayList<>();
        Product product;
        Delivery delivery = null;

        Order newOrder = new Order(
                customerList.getValue(),
                new GregorianCalendar(),
                paymentMethod.getValue()
        );

        if (deliveryCheck.isSelected()) {
            GregorianCalendar date = new GregorianCalendar();
            date.set(deliveryDate.getValue().getYear(), deliveryDate.getValue().getDayOfMonth(), deliveryDate.getValue().getDayOfMonth());
            delivery = new Delivery(
                    new Employee(2, "admin"),
                    date,
                    newOrder
            );

            newOrder.setDelivery(delivery);
        }

        for (OrderLineTableFormat line : tableArticle.getItems()) {
            product = productList.getItems().get(line.getProductCode() - 1);
            newOrder.addOrderLine(new OrderLine(product, newOrder, line.getQuantity(), line.getUnitPrice()));
        }

        return orderController.create(newOrder);
    }

    private void initTable() {
        article.setCellValueFactory(new PropertyValueFactory<>("product"));
        price.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalExclVat.setCellValueFactory(new PropertyValueFactory<>("exclVat"));
        totalInclVat.setCellValueFactory(new PropertyValueFactory<>("inclVat"));
    }
}
