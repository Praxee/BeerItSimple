package view.news;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTimePicker;
import controller.NewsController;
import exception.SQLManageException;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import model.News;
import utils.Validators;
import view.PopUp;
import view.View;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Update extends View {
    @FXML
    private JFXTextField title;
    @FXML
    private DatePicker startingDate;
    @FXML
    private DatePicker endDate;
    @FXML
    private JFXTimePicker startingTime;
    @FXML
    private JFXTimePicker endTime;
    @FXML
    private JFXTextArea contentArea;
    @FXML
    private JFXButton closeBtn;
    @FXML
    private JFXButton submitBtn;

    private News news = null;

    @Override
    public void init() {
        if(news != null) {
            title.setText(news.getTitle());
            contentArea.setText(news.getContent());

            startingTime.set24HourView(true);
            endTime.set24HourView(true);
            initDateAndTimePicker();

            Validators.setReqField(title);
            Validators.setReqField(contentArea);
            Validators.setReqField(startingTime);
            Validators.setReqField(endTime);
            Validators.setTextAndNumberValidator(title);
            Validators.setTextAndNumberValidator(contentArea);

            closeBtn.setOnAction(e -> {
                this.closeWindow();
            });

            submitBtn.setOnAction(e -> {
                if (validateInfo()) {
                    try {
                        updateNews();
                        PopUp.showSuccess("News updated", "Your news has been successfully updated");
                        this.closeWindow();
                    } catch (SQLManageException err) {
                        err.showMessage();
                    }
                }
            });
        } else {
            this.closeWindow();
        }
    }

    private boolean validateInfo(){
        LocalDate start = startingDate.getValue();
        LocalDate end = endDate.getValue();
        if(Validators.validate(title) && Validators.validate(contentArea) && Validators.validate(startingTime) && Validators.validate(endTime) && contentArea.validate()){
            if(startingDate.getValue() == null){
                PopUp.showError("Date error", "Please choose start date.");
                return false;
            }

            if(endDate.getValue() == null){
                PopUp.showError("Date error", "Please choose end date.");
                return false;
            }

            if(Validators.validateBetweenDates(end, start)){
                PopUp.showError("Date error", "End date must be later than the start date.");
                return false;
            }
            return true;
        }
        return false;
    }

    public void updateNews() throws SQLManageException {
        NewsController newsController = new NewsController();
        LocalDate start = startingDate.getValue();
        LocalDate end = endDate.getValue();
        LocalTime startT = startingTime.getValue();
        LocalTime endT = endTime.getValue();
        GregorianCalendar startGC = new GregorianCalendar(start.getYear(), start.getMonthValue()-1, start.getDayOfMonth(), startT.getHour(), startT.getMinute(), startT.getSecond());
        GregorianCalendar endGC = new GregorianCalendar(end.getYear(), end.getMonthValue()-1, end.getDayOfMonth(), endT.getHour(), endT.getMinute(), endT.getSecond());
        News updateNews = new News(news.getId(), title.getText(), contentArea.getText(), startGC, endGC, 2);
        try {
            newsController.updateNews(updateNews);
        } catch(SQLManageException e){
            throw e;
        }
    }

    public void setNews(News news){
        this.news = news;
    }

    private void initDateAndTimePicker(){
        //Starting
        GregorianCalendar startingDateGC = news.getStartingDate();
        Calendar calendar = startingDateGC;
        LocalTime startLocalTime = LocalTime.of(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        startingDate.setValue(LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH)));
        startingTime.setValue(startLocalTime);
        //Ending
        GregorianCalendar endingDateGC = news.getEndDate();
        Calendar calendarEnd = endingDateGC;
        LocalTime endLocalTime = LocalTime.of(calendarEnd.get(Calendar.HOUR_OF_DAY), calendarEnd.get(Calendar.MINUTE));
        endDate.setValue(LocalDate.of(calendarEnd.get(Calendar.YEAR), calendarEnd.get(Calendar.MONTH)+1, calendarEnd.get(Calendar.DAY_OF_MONTH)));
        endTime.setValue(endLocalTime);
    }
}
