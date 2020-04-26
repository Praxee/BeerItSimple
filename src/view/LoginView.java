package view;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import controller.EmployeeController;
import exception.EmployeeLoginException;
import exception.MatriculException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import model.Employee;

public class LoginView extends View {

    @FXML
    VBox loginContainer;
    @FXML
    JFXTextField employeeMatricule;
    @FXML
    JFXPasswordField employeePassword;
    @FXML
    JFXButton signinButton;

    private static final String PathToMainPanel = "FXML/MainPanel.fxml";

    private EmployeeController employeeController;

    @Override
    public void init() {
        signinButton.setOnAction(e -> {
            // TODO: Tenter de se connecter
            try {
                openSession(e);
            } catch (Exception exception) {
//                System.out.println(exception.getMessage());
                exception.printStackTrace();
            }
        });

        employeeController = new EmployeeController();
    }

    private void openSession(ActionEvent e) {

        try {
            int matricule = getMatricule();
            String password = getPassword();

            // Can save employee as user in view.Window to manage permission
            Employee employee = employeeController.getEmployee(matricule, password);
            Window session;
            session = new Window(PathToMainPanel, "BeerItSimple - Home");

            switchWindow(session);
//            System.out.println(employee);

        } catch (EmployeeLoginException exception) {
                exception.showMessage();
        } catch (MatriculException exception) {
                exception.showMessage();
        }
    }

    public int getMatricule() throws MatriculException {
        if (employeeMatricule.getText().matches("\\d+")) {
            return Integer.parseInt(employeeMatricule.getText());
        } else {
            throw new MatriculException();
        }
    }

    public String getPassword() {
        return employeePassword.getText();
    }

    public Pane getRoot() {
        return this.loginContainer;
    }

    @Override
    public String toString() {
        return "It's the class LoginView (I wanna die btw)";
    }
}
