package view.order;

import com.jfoenix.controls.JFXButton;
import controller.OrderController;
import exception.*;
import model.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import view.PopUp;
import view.View;
import view.Window;
import view.order.Update;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Index extends View implements Initializable {

    @FXML
    private VBox vbox;
    @FXML
    private Label label;
    @FXML
    private JFXButton newOrderBtn;
    @FXML
    private JFXButton editOrderBtn;
    @FXML
    private JFXButton viewOrder;
    @FXML
    private JFXButton deleteBtn;
    @FXML
    private TableView<OrderTableFormat> orderTable;
    @FXML
    private TableColumn<OrderTableFormat, Integer> reference;
    @FXML
    private TableColumn<OrderTableFormat, String> startingDate;
    @FXML
    private TableColumn<OrderTableFormat, String> customerName;
    @FXML
    private TableColumn<OrderTableFormat, String> paid;
    @FXML
    private TableColumn<OrderTableFormat, String> paymentMethod;
    @FXML
    private TableColumn<OrderTableFormat, String> status;
    @FXML
    private TableColumn<OrderTableFormat, String> plannedDate;
    @FXML
    private TableColumn<OrderTableFormat, String> deliveredDate;
    @FXML
    private TableColumn<OrderTableFormat, String> deliveryMan;

    private OrderController orderController;

    public Index() {
        this.orderController = new OrderController();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initTableOrder();
        init();
    }

    @Override
    public void init() {
        newOrderBtn.setOnAction(e -> {
            Window newOrder = new Window("FXML/order/newOrder.fxml", "BeerItSimple - New order");
            newOrder.load();
            newOrder.getView().setParentView(this);
            newOrder.show();
        });

        editOrderBtn.setOnAction(e -> {
            Window editOrder = new Window("FXML/order/update.fxml", "BeerItSimple - Edit order");
            editOrder.load();
            editOrder.resizable(false);
            editOrder.getView().setParentView(this);

            Update Update = (Update) editOrder.getView();
            Order order = null;
            try {
                order = getSelectedOrder();
            } catch (Exception exception) {
                PopUp.showError("Order not found", "You may be didn't selected an order in the table.");
            }

            if (order != null) {
                Update.setOrder(order);
                editOrder.show();
            } else {
                editOrder.close();
            }
        });

        deleteBtn.setOnAction(e -> {
            try {
                Order order = getSelectedOrder();
                if(PopUp.showConfirm("Confirm delete", "Are you sur you want to delete the order [" + order.getReference() + "] ?")) {
                    if (orderController.deleteOrder(order)) {
                        updateTable();
                    }
                }
            } catch (NoRowSelected ex) {
                ex.showError();
            } catch (DeletionExceiption ex) {
                ex.showError();
            } catch (NullPointerException ex) {
                new NoRowSelected();
            }

            updateTable();
        });
    }

    private Order getSelectedOrder() throws NoRowSelected {
        OrderTableFormat orderTableFormat = orderTable.getSelectionModel().getSelectedItem();
        Order order = orderController.getOrder(orderTableFormat.getReference());

        return order;
    }

    public void initTableOrder() {
        // Add the factory to the cell
        // That allow the cell to retrieve its data and display it
        reference.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, Integer>("reference"));
        startingDate.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, String>("startingDate"));
        customerName.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, String>("customerName"));
        paid.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, String>("paid"));
        paymentMethod.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, String>("paymentMethod"));
        status.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, String>("status"));
        plannedDate.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, String>("plannedDate"));
        deliveredDate.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, String>("deliveredDate"));
        deliveryMan.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, String>("deliveryMan"));

        // Transforme les orders en OrderTableFormat pour l'affichage
        ArrayList<Order> orderList = new ArrayList<Order>();
        try{
            orderList = orderController.getAllOrders();
        } catch(SQLException ex){
            new SQLManageException(ex).showMessage();
        }
        ArrayList<OrderTableFormat> ordersRow = new ArrayList<>();
        for (Order order : orderList) {
            ordersRow.add(new OrderTableFormat(order));
        }

        orderTable.getItems().setAll(ordersRow);

        // Permet de redimensionner les colonnes lorsque la taille de la fenêtre change
        orderTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        for (int i = 0; i < orderTable.getColumns().size(); i++) {
            orderTable.getColumns().get(i).prefWidthProperty().bind(orderTable.widthProperty().multiply((double) 1 / orderTable.getColumns().size()));
        }
    }

    public void updateTable() {

        ArrayList<OrderTableFormat> ordersList = new ArrayList<>();
        try {

            for (Order order : orderController.getAllOrders()) {
                ordersList.add(new OrderTableFormat(order));
            }

            orderTable.getItems().setAll(ordersList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}