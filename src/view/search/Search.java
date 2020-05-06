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
    Tab betweenDateSearch;
    @FXML
    Tab zipCodeSearch;
    @FXML
    Tab customerRankSearch;
    @FXML
    Tab orderByCustomerSearch;

    private final String pathToBetweenDates = "/FXML/search/ordersBetweenTwoDates.fxml";
    private final String pathToZipCodeSearch = "/FXML/search/zipCodeSearch.fxml";
    private final String pathToCustomerRankSearch = "/FXML/search/customerRankSearch.fxml";
    private final String pathToorderByCustomerSearch = "/FXML/search/orderByCustomerSearch.fxml";

    @Override
    public void init() {
        betweenDateSearch.setOnSelectionChanged(e -> {
            setView(pathToBetweenDates);
        });

        zipCodeSearch.setOnSelectionChanged(e -> {
            // TODO
//            setView(pathToZipCodeSearch);
        });

        customerRankSearch.setOnSelectionChanged(e -> {
            // TODO
//            setView(pathToCustomerRankSearch);
        });

        orderByCustomerSearch.setOnSelectionChanged(e -> {
            // TODO
//            setView(pathToorderByCustomerSearch);
        });

        setView(pathToBetweenDates);

    }

    private void setView(String pathToFxml) {
        Parent view;

        try {
            view = FXMLLoader.load(getClass().getResource(pathToFxml));

            betweenDateSearch.setContent(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
