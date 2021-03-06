package view.news;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import controller.NewsController;
import exception.ConnectionException;
import exception.DataQueryException;
import exception.NullObjectException;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import model.News;
import utils.Validators;
import utils.PopUp;
import view.View;

import java.time.LocalDate;
import java.util.GregorianCalendar;

public class Create extends View {
    @FXML
    private JFXTextField title;
    @FXML
    private DatePicker startingDate;
    @FXML
    private DatePicker endDate;
    @FXML
    private JFXTextArea contentArea;
    @FXML
    private JFXButton closeBtn;
    @FXML
    private JFXButton submitBtn;

    NewsController newsController;

    @Override
    public void init() {
        try {
            newsController = new NewsController();
        } catch (ConnectionException e) {
            showError(e.getTypeError(), e.getMessage());
        }

        Validators.setReqField(title);
        Validators.setReqField(contentArea);

        closeBtn.setOnAction(e -> {
            this.closeWindow();
        });

        submitBtn.setOnAction(e -> {
            if (validateInfo()) {
                insertNews();
                PopUp.showSuccess("News added", "Your news has been successfully added");
                this.closeWindow();
            }
        });
    }

    private boolean validateInfo() {
        LocalDate start = startingDate.getValue();
        LocalDate end = endDate.getValue();
        if (Validators.validate(title) && Validators.validate(contentArea) && contentArea.validate()) {
            if (startingDate.getValue() == null) {
                PopUp.showError("Date error", "Please choose start date.");
                return false;
            }

            if (endDate.getValue() == null) {
                PopUp.showError("Date error", "Please choose end date.");
                return false;
            }

            if(Validators.startingDateIsBeforeNow(start)){
                PopUp.showError("Date error", "Starting date must be today or later.");
                return false;
            }

            if (!Validators.endIsAfterStart(start, end)) {
                PopUp.showError("Date error", "End date must be later than the start date.");
                return false;
            }
            return true;
        }
        return false;
    }

    public void insertNews() {
        LocalDate start = startingDate.getValue();
        LocalDate end = endDate.getValue();
        GregorianCalendar startGC = new GregorianCalendar(start.getYear(), start.getMonthValue() - 1, start.getDayOfMonth());
        GregorianCalendar endGC = new GregorianCalendar(end.getYear(), end.getMonthValue() - 1, end.getDayOfMonth());
        News news = new News(title.getText(), contentArea.getText(), startGC, endGC, 2);
        try {
            newsController.insertNews(news);
        } catch (DataQueryException e) {
            showError(e.getTypeError(), e.getMessage());
        } catch (NullObjectException e) {
            System.out.println(e.getMessage());
        }
    }
}
