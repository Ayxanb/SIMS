package com.khazar.sims.ui;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import com.khazar.sims.Main;

/**
 * UIManager handles:
 *  - Loading FXML files
 *  - Injecting them into a StackPane
 *  - Running transitions
 *  - Optional view caching
 */
public class UIManager {
  /**
   * Loads an FXML file and places it into a container with a transition.
   *
   * @param contentArea StackPane that holds the content
   * @param fxmlPath  Path to the FXML relative to resources
   * @param type      Transition type (fade, slide, zoom, none)
   * @param duration  Duration of animation in ms
   */
  public static void setView(StackPane contentArea, String fxmlPath, SceneTransition.Type type, double duration) {
    Parent newContent = loadView(fxmlPath);
    if (newContent == null) {
      System.err.println("UIManager: Failed to load " + fxmlPath);
      return;
    }
    SceneTransition.apply(contentArea, newContent, type, duration);
  }


  /**
   * Same as setView but uses default animation duration.
   */
  public static void setView(StackPane contentArea, String fxmlPath, SceneTransition.Type type) {
    setView(contentArea, fxmlPath, type, 250);
  }


  /**
   * Loads and optionally caches an FXML file.
   *
   * @param path resource path to FXML
   * @return loaded FXML root node or null on failure
   */
  public static Parent loadView(String path) {
    try {
      FXMLLoader loader = new FXMLLoader(Main.class.getResource(path));
      Parent root = loader.load();
      return root;
    }
    catch (IOException e) {
      System.out.println(e);
      return null;
    }
  }

}
