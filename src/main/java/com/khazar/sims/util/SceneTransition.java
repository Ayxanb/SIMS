package com.khazar.sims.util;

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
  private static final double DEFAULT_OUT_DURATION = 150.0;
  private static final double DEFAULT_IN_DURATION = 300.0;

  /* ---------- Public API ---------- */

  /**
   * Apply a scene transition to the container using default durations.
   *
   * @param container The StackPane holding the current content
   * @param newNode   The new node to display
   * @param type      Transition type (FADE, SLIDE, ZOOM, NONE)
   */
  public static void apply(StackPane container, Parent newNode, Type type) {
    apply(container, newNode, type, DEFAULT_OUT_DURATION, type, DEFAULT_IN_DURATION);
  }

  /**
   * Apply a scene transition with custom types and durations.
   *
   * @param container     The StackPane holding the current content
   * @param newNode       The new node to display
   * @param outType       Transition type for the outgoing node
   * @param outDurationMs Duration of outgoing transition (ms)
   * @param inType        Transition type for the incoming node
   * @param inDurationMs  Duration of incoming transition (ms)
   */
  public static void apply(
      StackPane container,
      Parent newNode,
      Type outType, double outDurationMs,
      Type inType, double inDurationMs
  ) {
    Parent oldNode = container.getChildren().isEmpty() ? null :
                     (Parent) container.getChildren().get(container.getChildren().size() - 1);

    if (oldNode == null || outType == Type.NONE) {
      container.getChildren().setAll(newNode);
      return;
    }

    container.getChildren().add(newNode);

    /* Create transitions for old and new nodes */
    Transition outTransition = makeTransition(oldNode, outType, outDurationMs, false);
    Transition inTransition  = makeTransition(newNode, inType, inDurationMs, true);

    /* Remove old node after outgoing transition */
    outTransition.setOnFinished(e -> container.getChildren().remove(oldNode));

    /* Play both transitions simultaneously */
    new ParallelTransition(outTransition, inTransition).play();
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
