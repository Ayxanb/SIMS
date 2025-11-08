package com.khazar.sims;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.application.Application;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        Label label = new Label("Hello JavaFX!");
        Scene scene = new Scene(label, 400, 200);
        stage.setScene(scene);
        stage.setTitle("JavaFX App");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
