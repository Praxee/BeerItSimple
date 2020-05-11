package view.search;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import controller.CityController;

import controller.OrderController;
import exception.SQLManageException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import model.City;
import model.Order;
import view.PopUp;
import view.View;
import view.Window;
import view.order.Index;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class zipcodeSearch extends View implements Initializable {

    @FXML
    JFXComboBox<City> zipcodeBox;
    @FXML
    JFXButton cancelBtn;
    @FXML
    JFXButton searchBtn;

    CityController cityController;
    OrderController orderController;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        init();
    }

    @Override
    public void init() {
        cityController = new CityController();
        orderController = new OrderController();
        ArrayList<City> allCities = cityController.getAllCities();
        zipcodeBox.setItems(FXCollections.observableArrayList(allCities));
        zipcodeBox.getSelectionModel().selectFirst();
        zipcodeBox.getStyleClass().add("whiteComboBox");

        cancelBtn.setOnAction(e -> {
            closeWindow();
        });

        searchBtn.setOnAction(e -> {
            search();
        });
    }

    private void search(){
        try {
            City city = zipcodeBox.getSelectionModel().getSelectedItem();
            ArrayList<Order> allOrders = orderController.getAllOrdersFromCustomer(city);
            if(allOrders == null || allOrders.size() == 0){
                PopUp.showError("No order", "There are no orders delivered for this zipcode");
            } else {
                openNewTabView(allOrders, city);
            }
        } catch(SQLManageException e){
            e.showMessage();
        }


    }

    private void openNewTabView(ArrayList<Order> allOrdersFromZipcode, City city) {
        Window displayResult = new Window("FXML/order/index.fxml", "BeerItSimple - All orders from selected zipcode : " + city.getZipCode());
        displayResult.load();
        displayResult.getView().setParentView(this);
        Index index = (Index) displayResult.getView();
        index.updateTable(allOrdersFromZipcode);
        index.hideRefreshButton();
        index.setCustomer(customer);
        displayResult.show();
    }

}