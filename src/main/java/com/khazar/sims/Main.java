package com.khazar.sims;

import java.io.IOException;

import com.khazar.sims.controller.RootController;
import com.khazar.sims.core.Session;

import javafx.fxml.FXMLLoader;
import javafx.application.Application;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.StageStyle;
import javafx.scene.image.Image;
import javafx.scene.control.Alert;

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
  public static void main(String[] args) {
    launch(args);
  }

  /**
   * Called by JavaFX to start the application.
   * Configures the primary stage, initializes the session,
   * loads the root layout, and sets the initial login scene.
   *
   * @param primaryStage The main application window
   */
  @Override
  public void start(Stage primaryStage) {
    /* Configure window style and icon */
    configureWindow(primaryStage);

    /* Initialize application session */
    if (!Session.start()) return;

    try {
      /* Load root FXML layout */
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/root.fxml"));
      Parent root = loader.load();
      RootController rootController = loader.getController();

      /* Set scene and show stage */
      Scene scene = new Scene(root);
      primaryStage.setScene(scene);
      primaryStage.show();

      /* Load the initial login scene inside root content */
      rootController.loadScene("/fxml/login.fxml");

      /* Store root controller in session for global access */
      Session.setRootController(rootController);
    }
    catch (IOException e) {
      /* Show error if root FXML fails to load */
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Error");
      alert.setContentText("Failed to load the application UI:\n" + e.toString());
      alert.showAndWait();
    }
  }

  /**
   * Configures the primary stage window.
   * Sets the application icon and removes window decorations.
   *
   * @param primaryStage The main application window
   */
  public static void configureWindow(Stage primaryStage) {
    /* Set application icon */
    primaryStage.getIcons().add(new Image("/images/khazar.png"));

    /* Remove default OS window decorations for custom styling */
    primaryStage.initStyle(StageStyle.UNDECORATED);
  }
}
