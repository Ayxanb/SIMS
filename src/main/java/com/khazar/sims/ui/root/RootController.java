package com.khazar.sims.ui.root;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;

import javafx.scene.Cursor;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;

import javafx.stage.Stage;
import javafx.stage.Screen;

/**
 * RootController manages the primary application window.
 * It handles window dragging, minimizing, maximizing, closing,
 * and smooth scene transitions within the main content area.
 */
public class RootController {
  /* ---------- FXML UI Components ---------- */
  @FXML private BorderPane root;        /* the window itself */

  @FXML private HBox titlebar;
  @FXML private Button closeButton;     /* inside titlebar */
  @FXML private Button minimizeButton;  /* inside titlebar */
  @FXML private Button maximizeButton;  /* inside titlebar */
  @FXML private StackPane contentArea;  /* every top scene will be loaded in this */  

  /* ---------- Window State Variables ---------- */
  private double xOffset;
  private double yOffset;
  private double previousX;           /* X position before maximize */
  private double previousY;           /* Y position before maximize */
  private double previousWidth;       /* width before maximize */
  private double previousHeight;      /* height before maximize */
  private boolean maximized = false;

  public StackPane getContentArea() { return contentArea; }

  /**
   * Called automatically by JavaFX after FXML loading.
   * Sets up window dragging behavior and window control buttons.
   */
  @FXML
  private void initialize() {
    initializeWindowDrag();
    initializeButtons();
  }

  /**
   * Configures mouse events for dragging and double-click maximizing
   * the undecorated JavaFX window.
   */
  private void initializeWindowDrag() {
    /* when mouse is pressed, enable window dragging. */
    root.setOnMousePressed(event -> {
      Stage stage = (Stage) root.getScene().getWindow();
      /* Don't do anything if maximized */
      if (maximized) return;
      xOffset = stage.getX() - event.getScreenX();
      yOffset = stage.getY() - event.getScreenY();
      root.setCursor(Cursor.CLOSED_HAND);
    });

    /* when mouse is dragged, track its movement to move window as well. */
    root.setOnMouseDragged(event -> {
      Stage stage = (Stage) root.getScene().getWindow();
      if (!maximized) {
        stage.setX(event.getScreenX() + xOffset);
        stage.setY(event.getScreenY() + yOffset);
      }
    });

    /* Double-clicking the title area toggles maximize/restore */
    root.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2) {
        Stage stage = (Stage) root.getScene().getWindow();
        toggleMaximize(stage);
      }
    });

    /* when mouse button is released, fix the cursor. */
    root.setOnMouseReleased(e -> root.setCursor(Cursor.DEFAULT));
  }

  /**
   * Configures button actions for minimizing, maximizing, and closing the window.
   */
  private void initializeButtons() {
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

  /**
   * Toggles between maximized and restored window states.
   *
   * @param stage The current window stage
   */
  private void toggleMaximize(Stage stage) {
    /* Screen size */
    Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
    if (!maximized) {
      /* Save previous position and size before maximizing */
      previousX = stage.getX();
      previousY = stage.getY();
      previousWidth = stage.getWidth();
      previousHeight = stage.getHeight();

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
      stage.setX(previousX);
      stage.setY(previousY);
      stage.setWidth(previousWidth);
      stage.setHeight(previousHeight);

      maximizeButton.setText("☐");
      maximized = false;
    }
    /* Refresh layout to prevent visual glitches */
    root.requestLayout();
    titlebar.requestLayout();
  }
}