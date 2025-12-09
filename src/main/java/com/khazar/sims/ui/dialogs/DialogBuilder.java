package com.khazar.sims.ui.dialogs;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DialogBuilder {
  private String title;
  private Dialog controller;
  private final Map<String, Runnable> buildQueue = new HashMap<>();

  public static DialogBuilder create(String title) {
    DialogBuilder b = new DialogBuilder();
    b.title = title;
    return b;
  }

  public DialogBuilder text(String key, String label) {
    buildQueue.put(key, () -> controller.addTextField(key, label, "", false));
    return this;
  }

  public DialogBuilder email(String key, String label) {
    buildQueue.put(key, () -> controller.addTextField(key, label, "", false));
    return this;
  }

  public DialogBuilder password(String key, String label) {
    buildQueue.put(key, () -> controller.addTextField(key, label, "", true));
    return this;
  }

  public DialogBuilder date(String key, String label) {
    buildQueue.put(key, () -> controller.addDatePicker(key, label));
    return this;
  }

  public <T> DialogBuilder combo(String key, String label, List<T> items, Function<T, String> labelProvider) {
    buildQueue.put(key, () -> controller.addComboBox(key, label, items, labelProvider));
    return this;
  }

  public DialogResult accept() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/dialogs/dialog.fxml"));
      Stage stage = new Stage();
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.setTitle(title);
      stage.setScene(new Scene(loader.load()));

      controller = loader.getController();
      controller.setStage(stage);

      buildQueue.values().forEach(Runnable::run);

      stage.showAndWait();
      return new DialogResult(controller.isConfirmed(), controller.getValues());
    }
    catch (IOException e) {
      System.out.println(e);
      return new DialogResult(false, Map.of());
    }
  }
}
