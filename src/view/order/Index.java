package view.order;

import com.jfoenix.controls.JFXButton;
import controller.OrderController;
import exception.*;
import model.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import utils.PopUp;
import view.View;
import view.Window;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Index extends View implements Initializable {
    @FXML
    private VBox vbox;
    @FXML
    private JFXButton refreshBtn;
    @FXML
    private JFXButton newOrderBtn;
    @FXML
    private JFXButton editOrderBtn;
    @FXML
    private JFXButton detailBtn;
    @FXML
    private JFXButton deleteBtn;
    @FXML
    private TableView<OrderTableFormat> orderTable;
    @FXML
    private TableColumn<OrderTableFormat, Integer> reference;
    @FXML
    private TableColumn<OrderTableFormat, String> startingDate;
    @FXML
    private TableColumn<OrderTableFormat, Integer> customerId;
    @FXML
    private TableColumn<OrderTableFormat, String> customerName;
    @FXML
    private TableColumn<OrderTableFormat, String> customerRank;
    @FXML
    private TableColumn<OrderTableFormat, String> paid;
    @FXML
    private TableColumn<OrderTableFormat, String> paymentMethod;
    @FXML
    private TableColumn<OrderTableFormat, String> status;
    @FXML
    private TableColumn<OrderTableFormat, String> customerAddress;
    @FXML
    private TableColumn<OrderTableFormat, String> customerCity;
    @FXML
    private TableColumn<OrderTableFormat, String> customerPhoneNumber;
    @FXML
    private TableColumn<OrderTableFormat, String> plannedDate;
    @FXML
    private TableColumn<OrderTableFormat, String> deliveredDate;
    @FXML
    private TableColumn<OrderTableFormat, String> deliveryMan;

    private OrderController orderController;
    private Customer customer = null;
    private City city = null;

    public Index() {
        try {
            this.orderController = new OrderController();
        } catch (ConnectionException e) {
            showError(e.getTypeError(),e.getMessage());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initTableOrder();
        init();
    }

    @Override
    public void init() {

        refreshBtn.setOnAction(e -> {
            updateTable();
        });

        newOrderBtn.setOnAction(e -> {
            Window newOrder = new Window("FXML/order/newOrder.fxml", "BeerItSimple - New order");
            newOrder.load();
            newOrder.getView().setParentView(this);
            newOrder.resizable(false);
            Create create = (Create) newOrder.getView();
            newOrder.show();
            if(customer != null) create.selectCustomer(customer);
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

        detailBtn.setOnAction((e) -> {
            Order selectedOrder;
            Window detailOrder;
//            try {
            selectedOrder = getSelectedOrder();
            if (selectedOrder != null) {
                detailOrder = new Window("FXML/order/detailOrder.fxml", "BeerItSimple - Order : " + selectedOrder.getReference());


                detailOrder.load();
                detailOrder.resizable(false);
                detailOrder.getView().setParentView(this);
                // assurément Update car on le crée nous même juste avant
                Read detail = (Read) detailOrder.getView();

                detail.setOrder(selectedOrder);
                detailOrder.show();
            }
        });

        deleteBtn.setOnAction(e -> {
            try {
                ArrayList<Order> orders = getMultipleSelectedOrder();

                if (orders.isEmpty())
                    throw new NoRowSelected();

                String message = (orders.size() > 1) ? "Are you sur you want to delete these multiple orders ?" : "Are you sur you want to delete this order ?";
                if(PopUp.showConfirm("Confirm delete", message)) {
                    for (Order o : orders) {
                        if (orderController.deleteOrder(o)) {
                            updateTable();
                        }
                    }
                } else {
                    throw new NoRowSelected();
                }
            } catch (DeletionException | NoRowSelected ex) {
                showError(ex.getTypeError(), ex.getMessage());
            } catch (NullObjectException nullObjectException) {
                System.out.println(nullObjectException.getMessage());
            }
        });
    }

    private Order getSelectedOrder() {
        Order order = null;
        try {
            OrderTableFormat orderTableFormat = orderTable.getSelectionModel().getSelectedItem();

            if (orderTableFormat == null)
                throw new NoRowSelected();

            order = orderController.getOrder(orderTableFormat.getReference());
        } catch (DataQueryException | NoRowSelected e) {
            showError(e.getTypeError(), e.getMessage());
        }

        return order;
    }

    private ArrayList<Order> getMultipleSelectedOrder() {
        ArrayList<Order> selectedOrders = new ArrayList<>();
        orderTable.getSelectionModel().getSelectedItems().forEach(o -> {
            try {
                selectedOrders.add(orderController.getOrder(o.getReference()));
            } catch (DataQueryException e) {
                showError(e.getTypeError(), e.getMessage());
            }
        });

        return selectedOrders;
    }

    public void initTableOrder() {
        // Add the factory to the cell
        // That allow the cell to retrieve its data and display it
        reference.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, Integer>("reference"));
        startingDate.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, String>("startingDate"));
        customerId.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, Integer>("customerId"));
        customerName.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, String>("customerName"));
        customerRank.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, String>("customerRank"));
        paid.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, String>("paid"));
        paymentMethod.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, String>("paymentMethod"));
        status.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, String>("status"));
        customerAddress.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, String>("customerAddress"));
        customerCity.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, String>("customerCity"));
        customerPhoneNumber.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, String>("customerPhoneNumber"));
        plannedDate.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, String>("plannedDate"));
        deliveredDate.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, String>("deliveredDate"));
        deliveryMan.setCellValueFactory(new PropertyValueFactory<OrderTableFormat, String>("deliveryMan"));

        // Transforme les orders en OrderTableFormat pour l'affichage
        ArrayList<Order> orderList = new ArrayList<Order>();
        try{
            orderList = orderController.getAllOrders();
        } catch(DataQueryException ex){
            showError(ex.getTypeError(), ex.getMessage());
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
        try {
            updateTable(orderController.getAllOrders());
        } catch (DataQueryException e) {
            showError(e.getTypeError(), e.getMessage());
        }
    }

    public boolean updateTable(ArrayList<Order> orderList) {
        ArrayList<OrderTableFormat> ordersList = new ArrayList<>();

        for (Order order : orderList) {
            ordersList.add(new OrderTableFormat(order));
        }
        return orderTable.getItems().setAll(ordersList);
    }

    public void setCustomer(Customer customer){
        this.customer = customer;
    }

    public void setZipCode(City city) {this.city = city;}

    public void hideRefreshButton(){
        refreshBtn.setVisible(false);
    }

    public VBox getMainContainer() { return this.vbox; }
}