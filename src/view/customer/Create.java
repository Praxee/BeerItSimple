package view.customer;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import controller.CityController;
import controller.CustomerController;
import controller.RankController;
import exception.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.ToggleGroup;
import model.City;
import model.Customer;
import model.Entity;
import model.Rank;
import utils.Validators;
import utils.PopUp;
import view.View;

import java.util.ArrayList;

public class Create extends View {
    @FXML
    private JFXRadioButton privateCustomer;
    @FXML
    private JFXRadioButton businessCustomer;
    @FXML
    private JFXComboBox<Rank> customerRank;
    @FXML
    private JFXTextField contactName;
    @FXML
    private JFXTextField phoneNumber;
    @FXML
    private JFXTextField mail;
    @FXML
    private JFXTextField houseNumber;
    @FXML
    private JFXTextField address;
    @FXML
    private JFXComboBox<City> regionBox;
    @FXML
    private JFXTextField accountNumber;
    @FXML
    private JFXTextField businessNumber;
    @FXML
    private Group businessView;
    @FXML
    private JFXButton cancelBtn;
    @FXML
    private JFXButton submitBtn;

    RankController rankController;
    CityController cityController;
    CustomerController customerController;

    @Override
    public void init() {
        // Initialise le controller
        try {

            rankController = new RankController();
            cityController = new CityController();
            customerController = new CustomerController();
        } catch (ConnectionException e) {
            showError(e.getTypeError(), e.getMessage());
        }

        // affiche de base pour un client particulier
        businessView.setVisible(false);

        Validators.setMailValidators(mail);
        Validators.setReqField(contactName);
        Validators.setPhoneNumberValidator(phoneNumber);
        Validators.setAddressValidator(address);
        Validators.setHouseNumberValidator(houseNumber);
        Validators.setAccountNumberValidator(accountNumber);
        Validators.setBusinessNumberValidator(businessNumber);

        // permet lors du clic des radio buttons de changer la vue
//        privateCustomer.setUserData("private");
        privateCustomer.setOnAction(e -> {
            businessView.setVisible(false);
        });

//        businessCustomer.setUserData("business");
        businessCustomer.setOnAction(e -> {
            businessView.setVisible(true);
        });

        //Remplis la combobox Rank
        ArrayList<Rank> rankList = null;
        ArrayList<City> cityList = null;
        try {
            rankList = rankController.getAllRanks();
            cityList = cityController.getAllCities();
        } catch (DataQueryException e) {
            showError(e.getTypeError(), e.getMessage());
        }
        customerRank.setItems(FXCollections.observableArrayList(rankList));
        customerRank.getSelectionModel().selectFirst();

        regionBox.setItems(FXCollections.observableArrayList(cityList));
        regionBox.getSelectionModel().selectFirst();


        // Associe l'action aux buttons
        cancelBtn.setOnAction(e -> {
            closeWindow();
        });

        submitBtn.setOnAction(e -> {
            if(Validators.validate(contactName, phoneNumber, address, houseNumber)  && Validators.validateNullableValue(mail, businessNumber, accountNumber)) {
                try {
                    insertCustomer();
                    PopUp.showSuccess("Customer created with success !", "Success");
                    Index customersView = (Index) getParentView();
                    customersView.updateTable();
                    closeWindow();
                } catch (DuplicataException exception) {
                    PopUp.showError(exception.getTypeError(), exception.getMessage());
                } catch (NullObjectException nullObjectException) {
                    System.out.println(nullObjectException.getMessage());
                }
            }
        });
    }

    private boolean insertCustomer() throws DuplicataException, NullObjectException {

        Customer newCustomer;
        Entity newEntity = new Entity();

        newEntity.setContactName(contactName.getText());
        newEntity.setPhoneNumber(phoneNumber.getText());
        newEntity.setMail(mail.getText());
        newEntity.setStreet(address.getText());
        newEntity.setHouseNumber(Integer.parseInt(houseNumber.getText()));

        if(businessCustomer.isSelected()) {
            if (accountNumber.validate())
                newEntity.setBankAccountNumber(accountNumber.getText());

            if (businessNumber.validate())
                newEntity.setBusinessNumber(businessNumber.getText());
        }

        Rank selectedRank = customerRank.getValue();
        City city = regionBox.getValue();
        newEntity.setCity(city);
        newCustomer = new Customer(newEntity, selectedRank);
        return customerController.create(newCustomer);
    }
}
