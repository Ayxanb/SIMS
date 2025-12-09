package com.khazar.sims.ui.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;
import java.util.function.Function;

public class Dialog {
  @FXML private VBox formContainer;
  @FXML private Button btnSave;
  @FXML private Button btnCancel;

  private final Map<String, Control> fields = new HashMap<>();
  private Stage stage;
  private boolean confirmed = false;

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  public void addTextField(String key, String label, String defaultValue, boolean password) {
    Label l = new Label(label);
    Control input;
    if (password) {
      PasswordField pf = new PasswordField();
      pf.setPrefWidth(360);
      input = pf;
    }
    else {
      TextField tf = new TextField(defaultValue);
      tf.setPrefWidth(360);
      input = tf;
    }
    VBox box = new VBox(4, l, input);
    formContainer.getChildren().add(box);
    fields.put(key, input);
  }

  public void addDatePicker(String key, String label) {
    Label l = new Label(label);
    DatePicker dp = new DatePicker();
    dp.setPrefWidth(260);
    VBox box = new VBox(4, l, dp);
    formContainer.getChildren().add(box);
    fields.put(key, dp);
  }

  public <T> void addComboBox(String key, String label, List<T> items, Function<T, String> labelProvider) {
    Label l = new Label(label);
    ComboBox<T> comboBox = new ComboBox<>();
    comboBox.getItems().addAll(items);

    comboBox.setCellFactory(cb -> new ListCell<>() {
      @Override
      protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : labelProvider.apply(item));
      }
    });
    comboBox.setButtonCell(comboBox.getCellFactory().call(null));

    VBox box = new VBox(4, l, comboBox);
    formContainer.getChildren().add(box);
    fields.put(key, comboBox);
  }

  public Map<String, Object> getValues() {
    Map<String, Object> out = new HashMap<>();
    for (var entry : fields.entrySet()) {
      Control c = entry.getValue();
      if (c instanceof TextField tf) out.put(entry.getKey(), tf.getText());
      else if (c instanceof ComboBox<?> cb) out.put(entry.getKey(), cb.getValue());
      else if (c instanceof DatePicker dp) out.put(entry.getKey(), dp.getValue());
    }
    return out;
  }

  @FXML
  private void initialize() {
    btnCancel.setOnAction(e -> stage.close());
    btnSave.setOnAction(e -> {
      confirmed = true;
      stage.close();
    });
  }

  public boolean isConfirmed() {
    return confirmed;
  }
}
