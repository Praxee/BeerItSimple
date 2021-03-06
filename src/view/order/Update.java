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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.text.Text;
import model.*;
import utils.Date;
import utils.PopUp;
import view.View;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Update extends View {
    @FXML
    private Label orderReference;
    @FXML
    private Label createdAt;
    @FXML
    private Label deliveredAt;
    @FXML
    private JFXComboBox<Status> statusList;
    @FXML
    private Label customer;
    @FXML
    private JFXComboBox<PaymentMethod> paymentMethod;
    @FXML
    private JFXComboBox<Employee> deliveryMan;
    @FXML
    private JFXCheckBox deliveryCheck;
    @FXML
    private JFXCheckBox paid;
    @FXML
    private JFXCheckBox delivered;
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

    private Order order = null;
    private PaymentMethodController paymentMethodController;
    private EmployeeController employeeController;
    private OrderController orderController;
    private ProductController productController;
    private StatusController statusController;

    @Override
    public void init() {
        setShortcut(new KeyCodeCombination(KeyCode.ENTER), () -> addArticle());
        setShortcut(new KeyCodeCombination(KeyCode.DELETE), () -> removeArticle());

        Index orderView = (Index) getParentView();
        try {
            paymentMethodController = new PaymentMethodController();
            employeeController = new EmployeeController();
            orderController = new OrderController();
            productController = new ProductController();
            statusController = new StatusController();

            orderReference.setText("[" + order.getReference() + "]");
            createdAt.setText("Created at " + Date.format(order.getStartingDate()));

            ArrayList<Product> allProducts = productController.getAllProducts();
            productList.setItems(FXCollections.observableArrayList(allProducts));
            productList.getSelectionModel().selectFirst();

            setCurrentStatus();
        } catch (ConnectionException | DataQueryException e) {
            showError(e.getTypeError(), e.getMessage());
        }
        setCustomer();
        setPaymentMethod();
        setDelivery();
        initTable();
        fillProductTable();

        statusList.setOnAction(e -> {
            if (!statusList.getSelectionModel().getSelectedItem().getLabel().equals("Finished")) {
                if (order.getDelivery() == null) {
                    if (deliveryCheck.isDisable()) {
                        deliveryCheck.setDisable(false);
                    }
                }
            }
        });

        addArticleBtn.setOnAction(e -> {
            addArticle();
        });

        removeArticleBtn.setOnAction(e -> {
            removeArticle();
        });

        deliveryCheck.setOnAction(e -> {
            if (order.getDelivery() != null) {
                if (order.getDelivery().getDeliveredDate() == null) {
                    if (deliveryDisplay.isVisible()) {
                        deliveryDisplay.setVisible(false);
                        deliveryMan.setVisible(false);
                        delivered.setVisible(false);
                    } else {
                        deliveryDisplay.setVisible(true);
                        deliveryMan.setVisible(true);
                        delivered.setVisible(true);
                    }
                } else {
                    deliveryCheck.setSelected(true);
                }
            } else {
                if (deliveryDisplay.isVisible()) {
                    deliveryDisplay.setVisible(false);
                    deliveryMan.setVisible(false);
                    delivered.setVisible(false);
                } else {
                    deliveryDisplay.setVisible(true);
                    deliveryMan.setVisible(true);
                    delivered.setVisible(true);
                }
            }
        });

        submitBtn.setOnAction(e -> {
            if (!tableArticle.getItems().isEmpty()) {
                if (deliveryDateCheck()) {
                    try {
                        if (updateOrder()) {
                            PopUp.showSuccess("Order updated !", "The order has been added successfully.");
                            orderView.updateTable();
                            closeWindow();
                        }
                    } catch (UpdateException | DataQueryException ex) {
                        showError(ex.getTypeError(), ex.getMessage());
                    } catch (NullObjectException nullObjectException) {
                        System.out.println(nullObjectException.getMessage());
                    }
                }
            } else {
                PopUp.showError("Order error", "You can't save an order without atleast one orderline.");
                ;
            }
        });

        cancelBtn.setOnAction(e -> {
            closeWindow();
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

    public void removeArticle() {
        try {

            OrderLineTableFormat orderLineTableFormat = tableArticle.getSelectionModel().getSelectedItem();
            if (orderLineTableFormat == null)
                throw new NoRowSelected();

            tableArticle.getItems().remove(orderLineTableFormat);
            removeProduct(orderLineTableFormat);

        } catch (NoRowSelected e) {
            showError(e.getTypeError(), e.getMessage());
        }
    }

    private boolean deliveryDateCheck() {
        if (deliveryCheck.isSelected()) {
            if (deliveryDate.getValue() == null || deliveryDate.getValue().isBefore(LocalDate.now())) {
                PopUp.showError("Delivery error", "Please, choose a delivery dater after the current date or remove the delivery.");
                return false;
            }
        }
        return true;
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

        Create.computeAndDisplayNewAmount(orderLineTableFormat, totalAmountExclVat, totalAmountVatOnly, totalAmountVatInc);
    }

    private boolean updateOrder() throws UpdateException, DataQueryException, NullObjectException {
        Product product;

        if (deliveryCheck.isSelected()) {
            if (deliveryDate != null) {
                if (deliveryMan.getSelectionModel().isSelected(-1)) {
                    throw new UpdateException();
                }

                LocalDate date = deliveryDate.getValue();
                // Create the right format for delivery.plannedDate (-1 and +1 to get the right value)
                GregorianCalendar gc = new GregorianCalendar(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth() + 1);

                if (order.getDelivery() != null && order.getDelivery().getPlannedDate() != null) {
                    order.getDelivery().setPlannedDate(gc);
                } else if (order.getDelivery() == null) {
                    Delivery delivery = new Delivery(
                            deliveryMan.getSelectionModel().getSelectedItem(),
                            gc,
                            order
                    );
                    order.setDelivery(delivery);
                }

                if (delivered.isSelected()) {
                    if (order.getDelivery() != null && order.getDelivery().getDeliveredDate() == null) {
                        order.getDelivery().setDeliveredDate(gc);
                    }
                }
            }
        } else {
            order.setDelivery(null);
        }

        if (paid.isSelected()) {
            order.setPaid(true);
        }

        Status selectedStatus = statusList.getSelectionModel().getSelectedItem();
        if (selectedStatus.getId() != order.getStatus().getId()) {

            if (selectedStatus.getLabel().equals("Finished")) {
                if (order.getDelivery().getPlannedDate() != null && order.getDelivery().getDeliveredDate() == null) {
                    PopUp.showError("Order error", "You can't finish order if delivery programmed but not done");
                } else {
                    order.setPaid(true);
                    order.setStatus(selectedStatus);
                }
            } else {
                order.setStatus(selectedStatus);
            }
        }

        if (paymentMethod.getSelectionModel().getSelectedItem().getId() != order.getPaymentMethod().getId()) {
            if (order.getPaid()) {
                //Can't change payment method after paid
                showError("Payment method error", "You can't change payment method after order is paid.");
            } else {
                order.setPaymentMethod(paymentMethod.getSelectionModel().getSelectedItem());
            }
        }

        ArrayList<OrderLine> orderLineArrayList = new ArrayList<>();
        for (OrderLineTableFormat line : tableArticle.getItems()) {
            product = productList.getItems().get(line.getProductCode() - 1);
            orderLineArrayList.add(new OrderLine(product, order, line.getQuantity(), line.getUnitPrice()));
        }
        order.setAllOrderLines(orderLineArrayList);

        return orderController.updateOrder(order);
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    private void setCustomer() {
        if(order.getCustomer() != null) {
            customer.setText(order.getCustomer().getEntity().getContactName());
        } else {
            customer.setText("Deleted Customer");
        }
    }

    private void setPaymentMethod() {
        String paymentSelected = order.getPaymentMethod().getId().toString();
        ArrayList<PaymentMethod> allPaymentMethod = null;
        try {
            allPaymentMethod = paymentMethodController.getAllPaymentMethod();
        } catch (DataQueryException e) {
            showError(e.getTypeError(), e.getMessage());
        }
        int paymentIndex = -1;
        for (int i = 0; i < allPaymentMethod.size(); i++) {
            if (allPaymentMethod.get(i).getId().toString().equals(paymentSelected)) {
                paymentIndex = i;
                break;
            }
        }
        paymentMethod.setItems(FXCollections.observableArrayList(allPaymentMethod));
        if (paymentIndex != -1) {
            paymentMethod.getSelectionModel().select(paymentIndex);
        } else {
            paymentMethod.getSelectionModel().selectFirst();
        }

        if (order.getPaid()) {
            paymentMethod.setDisable(true);
            paid.setSelected(true);
            paid.setDisable(true);
        }
    }

    private void setDeliveryList() {
        try {
            ArrayList<Employee> deliveryList = employeeController.getAllDeliveryEmployee();
            int deliveryManIndex = -1;
            if (order.getDelivery() != null) {
                for (int i = 0; i < deliveryList.size(); i++) {
                    if (deliveryList.get(i).getEntity().getId() == order.getDelivery().getEmployee().getEntity().getId()) {
                        deliveryManIndex = i;
                        break;
                    }
                }
            }
            deliveryMan.setItems(FXCollections.observableArrayList(deliveryList));
            if (deliveryManIndex != -1) {
                deliveryMan.getSelectionModel().select(deliveryManIndex);
            }
        } catch (DataQueryException e) {
            showError(e.getTypeError(), e.getMessage());
        }
    }

    private void setDelivery() {
        if (order.getDelivery() != null) {
            deliveryCheck.setSelected(true);
            if (order.getDelivery().getPlannedDate() != null) {
                GregorianCalendar plannedDate = order.getDelivery().getPlannedDate();
                Calendar calendar = plannedDate;
                deliveryDate.setValue(LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
                if (order.getDelivery().getDeliveredDate() != null) {
                    deliveryCheck.setDisable(true);
                    deliveryDate.setDisable(true);
                    deliveryMan.setDisable(true);
                }
            }
            if (order.getDelivery().getDeliveredDate() != null) {
                deliveredAt.setText("Delivered at " + Date.format(order.getDelivery().getDeliveredDate()));
                delivered.setVisible(false);
            } else {
                deliveredAt.setVisible(false);
            }
        } else {
            deliveryDisplay.setVisible(false);
            deliveredAt.setVisible(false);
            delivered.setVisible(false);
            deliveryMan.setVisible(false);
        }
        setDeliveryList();
    }

    private void setCurrentStatus() throws DataQueryException {
        String currentStatus = order.getStatus().getId().toString();
        ArrayList<Status> statusArrayList = statusController.getAllStatus();
        int statusIndex = -1;
        for (int i = 0; i < statusArrayList.size(); i++) {
            if (statusArrayList.get(i).getId().toString().equals(currentStatus)) {
                statusIndex = i;
                break;
            }
        }
        statusList.setItems(FXCollections.observableArrayList(statusArrayList));
        if (statusIndex != -1) {
            statusList.getSelectionModel().select(statusIndex);
            if (statusArrayList.get(statusIndex).getLabel().equals("Finished")) {
                deliveryCheck.setDisable(true);
            }
        } else {
            statusList.getSelectionModel().selectFirst();
        }
    }

    public boolean checkPresentProduct(Product newProduct) {
        ArrayList<OrderLineTableFormat> products = new ArrayList<>(tableArticle.getItems());

        for (OrderLineTableFormat product : products) {
            if (product.getProductCode() == newProduct.getCode())
                return false;
        }

        return true;
    }

    private void initTable() {
        article.setCellValueFactory(new PropertyValueFactory<>("product"));
        price.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalExclVat.setCellValueFactory(new PropertyValueFactory<>("exclVat"));
        totalInclVat.setCellValueFactory(new PropertyValueFactory<>("inclVat"));
    }

    private void fillProductTable() {
        double amountExclVat = 0;
        double amountInclVat = 0;
        for (OrderLine orderLine : order.getOrderLineList()) {
            OrderLineTableFormat orderLineTableFormat = new OrderLineTableFormat(orderLine.getProduct(), orderLine.getQuantity());
            tableArticle.getItems().add(orderLineTableFormat);
//            double amount = Double.parseDouble(total.getText().replace(',', '.'));
            amountExclVat += orderLineTableFormat.getExclVat();
            amountInclVat += orderLineTableFormat.getInclVat();
        }

        double amountVatOnly = amountInclVat - amountExclVat;
        String newAmountExclVat = String.format("%.2f", amountExclVat);
        totalAmountExclVat.setText(newAmountExclVat);
        String newAmountVatOnly = String.format("%.2f", amountVatOnly);
        totalAmountVatOnly.setText(newAmountVatOnly);
        String newAmountInclVat = String.format("%.2f", amountInclVat);
        totalAmountVatInc.setText(newAmountInclVat);

    }
}
