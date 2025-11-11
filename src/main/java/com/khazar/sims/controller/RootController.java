package com.khazar.sims.controller;

import com.khazar.sims.util.SceneTransition;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * RootController manages the primary application window.
 * It handles window dragging, minimizing, maximizing, closing,
 * and smooth scene transitions within the main content area.
 */
public class RootController {

  /* ---------- FXML UI Components ---------- */

  @FXML private HBox titlebar;
  @FXML private BorderPane root;
  @FXML private StackPane contentPane;
  @FXML private Button closeButton;
  @FXML private Button minimizeButton;
  @FXML private Button maximizeButton;

  /* ---------- Window State Variables ---------- */

  private double xOffset;
  private double yOffset;
  private double prevX;
  private double prevY;
  private double prevWidth;
  private double prevHeight;
  private boolean maximized = false;

  /* ---------- Initialization ---------- */

  /**
   * Called automatically by JavaFX after FXML loading.
   * Sets up window dragging behavior and window control buttons.
   */
  @FXML
  private void initialize() {
    setupWindowDrag();
    setupButtons();
  }

  /**
   * Returns the main content pane of the window.
   * Used for scene transitions and dynamic content changes.
   *
   * @return StackPane used as the main content area
   */
  public StackPane getContentPane() {
    return contentPane;
  }

  /* ---------- Scene Loading & Transition ---------- */

  /**
   * Loads a new FXML scene into the content pane with transition effects.
   *
   * @param fxmlPath Path to the FXML file (relative to resources)
   * @param outType  Type of outgoing transition animation
   * @param outDurationMs Duration of the outgoing animation in milliseconds
   * @param inType   Type of incoming transition animation
   * @param inDurationMs Duration of the incoming animation in milliseconds
   */
  public void loadScene(
    String fxmlPath,
    SceneTransition.Type outType, double outDurationMs,
    SceneTransition.Type inType, double inDurationMs
  ) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
      Parent newContent = loader.load();

      /* Apply the transition animation between current and new content */
      SceneTransition.apply(contentPane, newContent, outType, outDurationMs, inType, inDurationMs);
    }
    catch (IOException e) {
      showError("Could not load the scene file `" + fxmlPath + "`.", e.getMessage());
    }
  }

  /**
   * Loads a new FXML scene into the content pane with no transition effects.
   *
   * @param fxmlPath Path to the FXML file (relative to resources)
   */
  public void loadScene(String fxmlPath) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
      Parent newContent = loader.load();

      /* Apply the transition animation between current and new content */
      SceneTransition.apply(contentPane, newContent, null, 0, null, 0);
    }
    catch (IOException e) {
      showError("Could not load the scene file `" + fxmlPath + "`.", e.getMessage());
    }
  }

  /* ---------- Window Control Setup ---------- */

  /**
   * Configures mouse events for dragging and double-click maximizing
   * the undecorated JavaFX window.
   */
  private void setupWindowDrag() {
    root.setOnMousePressed(event -> {
      Stage stage = (Stage) root.getScene().getWindow();
      /* Only store drag offsets if window is not maximized */
      if (!maximized) {
        xOffset = stage.getX() - event.getScreenX();
        yOffset = stage.getY() - event.getScreenY();
      }
      root.setCursor(Cursor.CLOSED_HAND);
    });

    root.setOnMouseDragged(event -> {
      Stage stage = (Stage) root.getScene().getWindow();
      if (!maximized) {
        stage.setX(event.getScreenX() + xOffset);
        stage.setY(event.getScreenY() + yOffset);
      }
    });

    root.setOnMouseReleased(e -> root.setCursor(Cursor.DEFAULT));

    /* Double-clicking the title area toggles maximize/restore */
    root.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2) {
        Stage stage = (Stage) root.getScene().getWindow();
        toggleMaximize(stage);
      }
    });
  }

  /**
   * Configures button actions for minimizing, maximizing, and closing the window.
   */
  private void setupButtons() {
    minimizeButton.setOnAction(e -> {
      Stage stage = (Stage) root.getScene().getWindow();
      stage.setIconified(true);
    });

    maximizeButton.setOnAction(e -> {
      Stage stage = (Stage) root.getScene().getWindow();
      toggleMaximize(stage);
    });

    closeButton.setOnAction(e -> {
      Stage stage = (Stage) root.getScene().getWindow();
      stage.close();
    });
  }

  /* ---------- Window Behavior ---------- */

  /**
   * Toggles between maximized and restored window states.
   *
   * @param stage The current window stage
   */
  private void toggleMaximize(Stage stage) {
    Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

    if (!maximized) {
      /* Save previous position and size before maximizing */
      prevX = stage.getX();
      prevY = stage.getY();
      prevWidth = stage.getWidth();
      prevHeight = stage.getHeight();

      /* Expand window to fill available screen bounds */
      stage.setX(bounds.getMinX());
      stage.setY(bounds.getMinY());
      stage.setWidth(bounds.getWidth());
      stage.setHeight(bounds.getHeight());

      maximizeButton.setText("❐");
      maximized = true;
    }
    else {
      /* Restore previous window position and size */
      stage.setX(prevX);
      stage.setY(prevY);
      stage.setWidth(prevWidth);
      stage.setHeight(prevHeight);

      maximizeButton.setText("☐");
      maximized = false;
    }

    /* Refresh layout to prevent visual glitches */
    root.requestLayout();
    titlebar.requestLayout();
  }

  /* ---------- Utility ---------- */

  /**
   * Displays an error dialog with the specified header and message.
   *
   * @param header The header text of the alert
   * @param message The detailed error message
   */
  private void showError(String header, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText(header);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
