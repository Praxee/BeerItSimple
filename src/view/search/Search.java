package view.search;

import com.jfoenix.controls.JFXButton;
import controller.OrderController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import model.Order;
import view.PopUp;
import view.View;
import view.order.Index;

import java.time.LocalDate;
import java.util.ArrayList;

public class Search extends View {
    @FXML
    private Tab betweenDateSearch;
    @FXML
    private Tab zipCodeSearch;
    @FXML
    private Tab customerRankSearch;
    @FXML
    private Tab ordersFromSelectedCustomer;

    private final String pathToBetweenDates = "/FXML/search/ordersBetweenTwoDates.fxml";
    private final String pathToZipCodeSearch = "/FXML/search/zipcodeSearch.fxml";
    private final String pathToCustomerRankSearch = "/FXML/search/ordersFromCustomerRank.fxml";
    private final String pathToOrdersFromSelectedCustomer = "/FXML/search/ordersFromSelectedCustomer.fxml";

    @Override
    public void init() {
        betweenDateSearch.setOnSelectionChanged(e -> {
            setView(betweenDateSearch, pathToBetweenDates);
        });

        zipCodeSearch.setOnSelectionChanged(e -> {
            setView(zipCodeSearch, pathToZipCodeSearch);
        });

        customerRankSearch.setOnSelectionChanged(e -> {
            setView(customerRankSearch, pathToCustomerRankSearch);
        });

        ordersFromSelectedCustomer.setOnSelectionChanged(e -> {
              setView(ordersFromSelectedCustomer, pathToOrdersFromSelectedCustomer);
        });

        setView(betweenDateSearch, pathToBetweenDates);

    }

    private void setView(Tab tab, String pathToFxml) {
        Parent view;
        try {
            view = FXMLLoader.load(getClass().getResource(pathToFxml));
            tab.setContent(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
