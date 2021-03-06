package view.order;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import controller.*;
import exception.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.text.Text;
import model.*;
import utils.Validators;
import utils.PopUp;
import view.View;

import java.net.ConnectException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class Create extends View {
    @FXML
    private JFXComboBox<Customer> customerList;
    @FXML
    private JFXComboBox<PaymentMethod> paymentMethod;
    @FXML
    private JFXComboBox<Employee> deliveryMan;
    @FXML
    private JFXCheckBox deliveryCheck;
    @FXML
    private DatePicker deliveryDate;
    @FXML
    private JFXButton cancelBtn;
    @FXML
    private JFXButton submitBtn;
    @FXML
    private JFXComboBox<Product> productList;
    @FXML
    private JFXTextField productQuantity;
    @FXML
    private JFXButton addArticleBtn;
    @FXML
    private TableView<OrderLineTableFormat> tableArticle;
    @FXML
    private TableColumn<OrderLineTableFormat, String> article;
    @FXML
    private TableColumn<OrderLineTableFormat, Double> price;
    @FXML
    private TableColumn<OrderLineTableFormat, Integer> quantity;
    @FXML
    private TableColumn<OrderLineTableFormat, Double> totalExclVat;
    @FXML
    private TableColumn<OrderLineTableFormat, Double> totalInclVat;
    @FXML
    private JFXButton removeArticleBtn;
    @FXML
    private Text totalAmountExclVat;
    @FXML
    private Text totalAmountVatOnly;
    @FXML
    private Text totalAmountVatInc;
    @FXML
    private Group deliveryDisplay;

    private CustomerController customerController;
    private EmployeeController employeeController;
    private PaymentMethodController paymentMethodController;
    private ProductController productController;
    private OrderController orderController;

    @Override
    public void init() {
        try {

            customerController = new CustomerController();
            employeeController = new EmployeeController();
            paymentMethodController = new PaymentMethodController();
            productController = new ProductController();
            orderController = new OrderController();
        } catch (ConnectionException e) {
            showError(e.getTypeError(), e.getMessage());
        }

        setShortcut(new KeyCodeCombination(KeyCode.ENTER), () -> addArticle());
        setShortcut(new KeyCodeCombination(KeyCode.DELETE), () -> removeProduct());

        Validators.setNumberValidator(productQuantity);

        ArrayList<Customer> allCustomers = null;
        ArrayList<PaymentMethod> allPaymentMethod = null;
        ArrayList<Product> allProducts = null;

        try {
            allCustomers = customerController.getAllCustomers();
            allPaymentMethod = paymentMethodController.getAllPaymentMethod();
            allProducts = productController.getAllProducts();
        } catch (DataQueryException e) {
            showError(e.getTypeError(), e.getMessage());
        }
        customerList.setItems(FXCollections.observableArrayList(allCustomers));
        customerList.getSelectionModel().selectFirst();

        paymentMethod.setItems(FXCollections.observableArrayList(allPaymentMethod));
        paymentMethod.getSelectionModel().selectFirst();

        productList.setItems(FXCollections.observableArrayList(allProducts));
        productList.getSelectionModel().selectFirst();

        try {
            ArrayList<Employee> deliveryList = employeeController.getAllDeliveryEmployee();
            deliveryMan.setItems(FXCollections.observableArrayList(deliveryList));
        } catch(DataQueryException e){
            showError(e.getTypeError(), e.getMessage());
        }

        deliveryDisplay.setVisible(false);
        deliveryMan.setVisible(false);

       deliveryCheck.setOnAction(e -> {
           if (deliveryDisplay.isVisible()) {
               deliveryDisplay.setVisible(false);
               deliveryMan.setVisible(false);
           } else {
               deliveryDisplay.setVisible(true);
               deliveryMan.setVisible(true);
           }
       });

       initTable();

       addArticleBtn.setOnAction(e -> {
           addArticle();
       });

       removeArticleBtn.setOnAction(e -> {
           removeProduct();
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
                    } catch (UpdateException ex) {
                        showError(ex.getTypeError(), ex.getMessage());
                    } catch (NullObjectException nullObjectException) {
                        System.out.println(nullObjectException.getMessage());
                    }
                }
           }
       });
    }

    public void addArticle() {
        if (!productQuantity.getText().equals("") && productQuantity.validate()) {
//               productQuantity.getStyleClass().remove("error");
            productQuantity.setStyle("");
            Product newProduct = productList.getValue();
            if (!checkPresentProduct(newProduct)) {
                PopUp.showError("Duplicate error", "You try to add a product already present in the command !");
            } else {

                addProduct(newProduct);
            }
        } else {
            productQuantity.setStyle("-fx-background-color: rgba(255,0,0,0.5)");
        }
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

    private void removeProduct() {
        try {

            OrderLineTableFormat orderLineTableFormat = tableArticle.getSelectionModel().getSelectedItem();
            if (orderLineTableFormat == null)
                throw new NoRowSelected();

            tableArticle.getItems().remove(tableArticle.getSelectionModel().getSelectedItem());

            computeAndDisplayNewAmount(orderLineTableFormat, totalAmountExclVat, totalAmountVatOnly, totalAmountVatInc);
        } catch (NoRowSelected e) {
            showError(e.getTypeError(),e.getMessage());
        }
    }

    static void computeAndDisplayNewAmount(OrderLineTableFormat orderLineTableFormat, Text totalAmountExclVat, Text totalAmountVatOnly, Text totalAmountVatInc) {
        double currentAmountExlVat = Double.parseDouble(totalAmountExclVat.getText().replace(',', '.'));
        double newAmountExclVat = currentAmountExlVat - orderLineTableFormat.getExclVat();
        String newTotalExclVat = String.format("%.2f", newAmountExclVat);
        totalAmountExclVat.setText(newTotalExclVat);

        double currentVatTotal = Double.parseDouble(totalAmountVatOnly.getText().replace(',', '.'));
        double newVatTotal = 0;
        if (newAmountExclVat != 0) {
            newVatTotal = currentVatTotal - (orderLineTableFormat.getExclVat() * (orderLineTableFormat.getVatCodeRate() / 100.0));
        }
        String totalVat = String.format("%.2f", newVatTotal);
        totalAmountVatOnly.setText(totalVat);

        double newTotalVatIncl = newAmountExclVat + newVatTotal;
        String totalVatIncl = String.format("%.2f", newTotalVatIncl);
        totalAmountVatInc.setText(totalVatIncl);
    }

    private boolean deliveryDateCheck() {
        if (deliveryCheck.isSelected()){
            if(deliveryDate.getValue() == null) {
                PopUp.showError("Delivery error", "Please choose delivery date or unselect the delivery checkbox.");
                return false;
            } else if(LocalDate.now().isAfter(deliveryDate.getValue())) {
                PopUp.showError("Date error", "The delivery date can't be earlier than the current date.");
                return false;
            }
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

    private boolean newOrderInsert() throws UpdateException, NullObjectException {
        Product product;
        Delivery delivery = null;
        Boolean orderCreated = false;

        Order newOrder = new Order(
                customerList.getValue(),
                new GregorianCalendar(),
                paymentMethod.getValue()
        );

        if (deliveryCheck.isSelected()) {
            Employee deliverer = deliveryMan.getValue();

            if(deliverer == null) {
                PopUp.showError("Delivery error","You need to select a delivery man if you want a delivery");
            } else {

                LocalDate date = deliveryDate.getValue();
                // Create the right format for delivery.plannedDate (-1 and +1 to get the right value)
                GregorianCalendar gc = new GregorianCalendar(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth() + 1);

                delivery = new Delivery(
                        deliverer,
                        gc,
                        newOrder
                );

                newOrder.setDelivery(delivery);
            }
        }

        for (OrderLineTableFormat line : tableArticle.getItems()) {
            product = productList.getItems().get(line.getProductCode() - 1);
            newOrder.addOrderLine(new OrderLine(product, newOrder, line.getQuantity(), line.getUnitPrice()));
        }

        try {
            orderCreated = orderController.create(newOrder);
        } catch (NullObjectException e) {
            System.out.println(e.getMessage());
        }
        if(orderCreated){
            Rank updateRank = orderController.updateCustomerRank(newOrder.getCustomer());
            if(updateRank != null){
                showSuccess("Rank up !", "Thank the client and announce their new rank is: " + updateRank.getLabel() + ".\n His new credit limit is: " + updateRank.getCreditLimit() + "\n");
            }
        }
        return orderCreated;
    }

    private void initTable() {
        article.setCellValueFactory(new PropertyValueFactory<>("product"));
        price.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalExclVat.setCellValueFactory(new PropertyValueFactory<>("exclVat"));
        totalInclVat.setCellValueFactory(new PropertyValueFactory<>("inclVat"));
    }

    public void selectCustomer(Customer customer){
        int indexCust = getCustomerIndex(customer);
        if(indexCust != -1) customerList.getSelectionModel().select(indexCust);
    }

    private int getCustomerIndex(Customer customer){
        int index = 0;
        for(Customer cust : customerList.getItems()){
            if(cust.getEntity().getId() == customer.getEntity().getId()){
                return index;
            }
            index++;
        }
        return index;
    }
}
