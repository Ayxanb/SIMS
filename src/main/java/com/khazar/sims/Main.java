package com.khazar.sims;

import java.io.IOException;
import java.sql.SQLException;

import com.khazar.sims.core.Session;

import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.image.Image;
import javafx.application.Application;

/**
 * Main class is the entry point of the application.
 * It initializes the JavaFX window, loads the root layout,
 * and sets up the initial login scene.
 */
public class Main extends Application {
  /**
   * Application entry point.
   * Delegates to JavaFX Application launch.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) { launch(args); }

  /**
   * Called by JavaFX to start the application.
   * Configures the primary stage, initializes the session,
   * loads the root layout, and sets the initial login scene.
   *
   * @param primaryStage The main application window
   */
  @Override
  public void start(Stage primaryStage) throws IOException, SQLException {
    primaryStage.getIcons().add(new Image("/images/khazar.png"));
    primaryStage.setTitle("Khazar University - SIMS");
    primaryStage.initStyle(StageStyle.UNDECORATED);
    Session.start(primaryStage);  /* start the Application session */
  }
}
