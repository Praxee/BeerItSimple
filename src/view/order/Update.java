package view.order;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import controller.OrderController;
import controller.PaymentMethodController;
import controller.ProductController;
import controller.StatusController;
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
import view.PopUp;
import view.View;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Update extends View {

    @FXML
    Label orderReference;
    @FXML
    Label createdAt;
    @FXML
    Label deliveredAt;
    @FXML
    JFXComboBox<Status> statusList;
    @FXML
    Label customer;
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

    Order order = null;
    PaymentMethodController paymentMethodController;
    OrderController orderController;
    ProductController productController;
    StatusController statusController;

    @Override
    public void init(){
        setShortcut(new KeyCodeCombination(KeyCode.ENTER), () -> addArticle());
        setShortcut(new KeyCodeCombination(KeyCode.DELETE), () -> removeArticle());

        paymentMethodController = new PaymentMethodController();
        orderController = new OrderController();
        productController = new ProductController();
        statusController = new StatusController();
        Index orderView = (Index) getParentView();

        orderReference.setText("[" + order.getReference() + "]");
        createdAt.setText("Created at " + Date.format(order.getStartingDate()));

        ArrayList<Product> allProducts = productController.getAllProducts();
        productList.setItems(FXCollections.observableArrayList(allProducts));
        productList.getSelectionModel().selectFirst();

        setCustomer();
        setCurrentStatus();
        setPaymentMethod();
        setDelivery();
        initTable();
        fillProductTable();

        addArticleBtn.setOnAction(e -> {
//            if (!productQuantity.getText().equals("") && productQuantity.validate()) {
////               productQuantity.getStyleClass().remove("error");
//                productQuantity.setStyle("");
//                Product newProduct = productList.getValue();
//                if (!checkPresentProduct(newProduct)) {
//                    PopUp.showError("Duplicate error", "You try to add a product already present in the command !");
//                } else {
//
////                    int productQty = Integer.parseInt(productQuantity.getText());
////
////                    OrderLineTableFormat orderLineTableFormat = new OrderLineTableFormat(newProduct, productQty);
////                    tableArticle.getItems().add(orderLineTableFormat);
////                    double amount = Double.parseDouble(totalAmount.getText().replace(',', '.'));
////
////                    String newTotal = String.format("%.2f", amount + orderLineTableFormat.getTotal());
////                    totalAmount.setText(newTotal);
//                    addProduct(newProduct);
//                }
//            } else {
//                productQuantity.setStyle("-fx-background-color: rgba(255,0,0,0.5)");
////               productQuantity.getStyleClass().add("error");
//
//            }
            addArticle();
        });

        removeArticleBtn.setOnAction(e -> {
            removeArticle();
        });

        deliveryCheck.setOnAction(e -> {
            if(order.getDelivery() != null){
                if(order.getDelivery().getDeliveredDate() == null) {
                    if (deliveryDisplay.isVisible()) {
                        deliveryDisplay.setVisible(false);
                    } else {
                        deliveryDisplay.setVisible(true);
                    }
                } else {
                    deliveryCheck.setSelected(true);
                }
            } else {
                if (deliveryDisplay.isVisible()) {
                    deliveryDisplay.setVisible(false);
                } else {
                    deliveryDisplay.setVisible(true);
                }
            }
        });

        submitBtn.setOnAction(e ->{
            if (!tableArticle.getItems().isEmpty()) {
                try{
                    if (updateOrder()) {
                        PopUp.showSuccess("Order updated !", "The order has been added successfully.");
                        orderView.updateTable();
                        closeWindow();
                    }
                } catch(SQLManageException ex) {
                    ex.showMessage();
                } catch (PaymentMethodException payErr){
                    payErr.showMessage();
                } catch(StatusException statusErr){
                    statusErr.showMessage();
                }
            } else {
                new UpdateOrderException("You can't save this order without products. If you want, you can delete this order.").showMessage();
            }
        });

        cancelBtn.setOnAction(e -> {
            closeWindow();
        });
    }

    public void addArticle () {
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
            tableArticle.getItems().remove(orderLineTableFormat);
//                double amount = Double.parseDouble(totalAmount.getText().replace(',', '.'));
//
//                String newTotal = String.format("%.2f", amount - orderLineTableFormat.getTotal());
//                totalAmount.setText(newTotal);

            removeProduct(orderLineTableFormat);

        } catch (NullPointerException exception) {
            new NoRowSelected();
        }
    }

    private boolean deliveryDateCheck() {
        if(order.getDelivery() == null || order.getDelivery().getDeliveredDate() == null) {
            if (deliveryCheck.isSelected() && LocalDate.now().isAfter(deliveryDate.getValue())) {
                PopUp.showError("Date error", "The delivery date can't be earlier than the current date.");
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

    private boolean updateOrder() throws SQLManageException, PaymentMethodException, StatusException {
        Product product;

        if (deliveryCheck.isSelected()) {
            if(deliveryDate != null) {
                GregorianCalendar date = new GregorianCalendar();
                date.set(deliveryDate.getValue().getYear(), deliveryDate.getValue().getDayOfMonth(), deliveryDate.getValue().getDayOfMonth());
                if(order.getDelivery() != null && order.getDelivery().getPlannedDate() != null && order.getDelivery().getPlannedDate() != date){
                    if(deliveryDateCheck()) {
                        order.getDelivery().setPlannedDate(date);
                    }
                } else if(order.getDelivery() == null) {
                    if(deliveryDateCheck()) {
                        Delivery delivery = new Delivery(
                                //TODO: Select d'employé ayant le role livreur
                                new Employee(2, "admin"),
                                date,
                                order
                        );
                        order.setDelivery(delivery);
                    }
                }
            }
        } else {
            order.setDelivery(null);
        }

        Status selectedStatus = statusList.getSelectionModel().getSelectedItem();
        if(selectedStatus.getId() != order.getStatus().getId()){

            if(selectedStatus.getLabel().equals("Finished")){
                if (order.getDelivery().getPlannedDate() != null && order.getDelivery().getDeliveredDate() == null) {
                    throw new StatusException("You can't finish order if delivery programmed but not done");
                } else {
                    order.setPaid(true);
                    order.setStatus(selectedStatus);
                }
            } else {
                order.setStatus(selectedStatus);
            }
        }

        if(paymentMethod.getSelectionModel().getSelectedItem().getId() != order.getPaymentMethod().getId()){
            if(order.getPaid()){
                //Can't change payment method after paid
                throw new PaymentMethodException();
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

    public void setOrder(Order order){
        this.order = order;
    }

    private void setCustomer(){
        customer.setText(order.getCustomer().getEntity().getContactName());
    }

    private void setPaymentMethod(){
        String paymentSelected = order.getPaymentMethod().getId().toString();
        ArrayList<PaymentMethod> allPaymentMethod = paymentMethodController.getAllPaymentMethod();
        int paymentIndex = -1;
        for(int i = 0; i < allPaymentMethod.size(); i++){
            if(allPaymentMethod.get(i).getId().toString().equals(paymentSelected)){
                paymentIndex = i;
                break;
            }
        }
        paymentMethod.setItems(FXCollections.observableArrayList(allPaymentMethod));
        if(paymentIndex != -1){
            paymentMethod.getSelectionModel().select(paymentIndex);
        } else {
            paymentMethod.getSelectionModel().selectFirst();
        }
    }

    private void setDelivery(){
        if(order.getDelivery() != null) {
            deliveryCheck.setSelected(true);
            if(order.getDelivery().getPlannedDate() != null) {
                GregorianCalendar plannedDate = order.getDelivery().getPlannedDate();
                Calendar calendar = plannedDate;
                deliveryDate.setValue(LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH)));
                if (order.getDelivery().getDeliveredDate() != null) {
                    deliveryDate.setDisable(true);
                }
            }
            if (order.getDelivery().getDeliveredDate() != null) {
                deliveredAt.setText("Delivered at " + Date.format(order.getDelivery().getDeliveredDate()));
            } else {
                deliveredAt.setVisible(false);
            }
        } else {
            deliveryDisplay.setVisible(false);
            deliveredAt.setVisible(false);
        }
    }

    private void setCurrentStatus(){
        String currentStatus = order.getStatus().getId().toString();
        ArrayList<Status> statusArrayList = statusController.getAllStatus();
        int statusIndex = -1;
        for(int i = 0; i < statusArrayList.size(); i++){
            if(statusArrayList.get(i).getId().toString().equals(currentStatus)){
                statusIndex = i;
                break;
            }
        }
        statusList.setItems(FXCollections.observableArrayList(statusArrayList));
        if(statusIndex != -1){
            statusList.getSelectionModel().select(statusIndex);
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

    private void fillProductTable(){
        double amountExclVat = 0;
        double amountInclVat = 0;
        for(OrderLine orderLine : order.getOrderLineList()){
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
