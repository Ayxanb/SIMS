package com.khazar.sims.ui;

import javafx.animation.*;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * SceneTransition provides smooth transition effects for switching
 * between nodes inside a StackPane container.
 * Supports default durations to reduce boilerplate parameters.
 */
public class SceneTransition {

  /* ---------- Supported transition types ---------- */
  public enum Type {
    NONE, FADE, SLIDE_LEFT, SLIDE_RIGHT, ZOOM
  }

  /* ---------- Default durations in milliseconds ---------- */
  private static final double DEFAULT_IN_DURATION = 300.0;

  /* ---------- Public API ---------- */

  /**
   * Apply a scene transition to the container using default durations.
   *
   * @param container The StackPane holding the current content
   * @param newNode   The new node to display
   * @param type      Transition type (FADE, SLIDE, ZOOM, NONE)
   */
  public static void apply(StackPane contentArea, Parent newNode, Type type) {
    apply(contentArea, newNode, type, DEFAULT_IN_DURATION);
  }

  /**
   * Apply a scene transition with custom types and durations.
   *
   * @param contentArea     The StackPane holding the current content
   * @param newNode       The new node to display
   * @param outType       Transition type for the outgoing node
   * @param outDurationMs Duration of outgoing transition (ms)
   * @param inType        Transition type for the incoming node
   * @param inDurationMs  Duration of incoming transition (ms)
   */
  public static void apply(
    StackPane contentArea,
    Parent newNode,
    Type inType, double inDurationMs
  ) {
    Parent oldNode = contentArea.getChildren().isEmpty() ? null : (Parent) contentArea.getChildren().get(contentArea.getChildren().size() - 1);
    contentArea.getChildren().add(newNode);
    prepareNodeForTransition(newNode, inType);
    Transition inTransition = makeTransition(newNode, inType, inDurationMs, true);
    if (oldNode == null) {
      inTransition.play();
      return;
    }
    inTransition.setOnFinished(e -> contentArea.getChildren().remove(oldNode));
    inTransition.play();
  }

  private static void prepareNodeForTransition(Parent node, Type type) {
    switch (type) {
      case FADE -> node.setOpacity(0);
      case SLIDE_LEFT -> {
        if (node.getParent() != null)
          node.setTranslateX(node.getParent().getLayoutBounds().getWidth());
      }
      case SLIDE_RIGHT -> {
        if (node.getParent() != null)
          node.setTranslateX(-node.getParent().getLayoutBounds().getWidth());
      }
      case ZOOM -> {
        node.setScaleX(0.8);
        node.setScaleY(0.8);
      }
      case NONE -> {}
    }
  }

  /* ---------- Internal helper ---------- */

  /**
   * Creates a JavaFX transition for a given node.
   *
   * @param node       Node to animate
   * @param type       Transition type
   * @param durationMs Duration in milliseconds
   * @param isIn       True if this is the incoming node
   * @return A configured Transition object
   */
  private static Transition makeTransition(Parent node, Type type, double durationMs, boolean isIn) {
    Duration duration = Duration.millis(durationMs);

    switch (type) {
      case FADE -> {
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(isIn ? 0 : 1);
        fade.setToValue(isIn ? 1 : 0);
        return fade;
      }
      case SLIDE_LEFT -> {
        TranslateTransition slide = new TranslateTransition(duration, node);
        double width = node.getScene() != null ? node.getScene().getWidth() : 800;
        slide.setFromX(isIn ? width : 0);
        slide.setToX(isIn ? 0 : -width);
        return slide;
      }
      case SLIDE_RIGHT -> {
        TranslateTransition slide = new TranslateTransition(duration, node);
        double width = node.getScene() != null ? node.getScene().getWidth() : 800;
        slide.setFromX(isIn ? -width : 0);
        slide.setToX(isIn ? 0 : width);
        return slide;
      }
      case ZOOM -> {
        ScaleTransition zoom = new ScaleTransition(duration, node);
        zoom.setFromX(isIn ? 0.8 : 1.0);
        zoom.setFromY(isIn ? 0.8 : 1.0);
        zoom.setToX(isIn ? 1.0 : 0.8);
        zoom.setToY(isIn ? 1.0 : 0.8);
        return zoom;
      }
      case NONE -> { return new PauseTransition(Duration.ZERO); }
      default -> { return new PauseTransition(Duration.ZERO); }
    }
  }
}
