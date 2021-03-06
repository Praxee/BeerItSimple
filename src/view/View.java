package view;

import javafx.scene.Node;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import utils.PopUp;

public abstract class View extends Node {
    private double x = 0.0;
    private double y = 0.0;
    protected Window window;
    private View parentView = null;

    public final void init(Window window) {
        this.window = window;
        this.init();
    }

    public abstract void init();

    public void closeWindow() {
        this.window.close();
    }

    public void switchWindow(Window newWindow) {
        this.window.close();
        newWindow.load();
        newWindow.show();
    }

    public void setParentView(View view) {
        this.parentView = view;
    }

    public View getParentView() {
        return this.parentView;
    }

    public void setShortcut(KeyCombination keyCode, Runnable runnable) {
        this.window.getStage().getScene().getAccelerators().put(keyCode, runnable);
    }

    public Stage getStage() {
       return window.getStage();
    }

    public void makeDraggable(Pane main) {
        main.setOnMousePressed(e -> {
            x = e.getSceneX();
            y = e.getSceneY();
        });

        main.setOnMouseDragged(e -> {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setX(e.getScreenX() - x);
            stage.setY(e.getScreenY() - y);
            stage.setOpacity(0.7);
        });

        main.setOnMouseReleased(e -> {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setOpacity(1);
        });
    }

    public void showError(String typeError, String message) {
        PopUp.showError(typeError, message);
    }

    public void showSuccess(String typeError, String message) {
        PopUp.showSuccess(typeError, message);
    }
}
