package com.khazar.sims.ui.system_admin;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.User;
import com.khazar.sims.database.table.UserTable;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class UserManagement {

  private static final String REQUIRED_FIELDS_MSG = "Full Name, Email, Role, and a Password (for new users) are required.";
  private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

  @FXML private TableView<User> userTable;
  @FXML private TableColumn<User, Integer> colId;
  @FXML private TableColumn<User, String> colFullName;
  @FXML private TableColumn<User, String> colEmail;
  @FXML private TableColumn<User, String> colRole;
  @FXML private TableColumn<User, String> colStatus;
  @FXML private TableColumn<User, Date> colLastLogin;

  @FXML private TextField searchField;
  @FXML private ComboBox<String> cmbRoleFilter;
  @FXML private Label statusLabel;
  @FXML private Button editButton;
  @FXML private Button deleteButton;

  @FXML private AnchorPane formPane;
  @FXML private Label formTitle;
  @FXML private TextField txtFullName;
  @FXML private TextField txtEmail;
  @FXML private ComboBox<String> cmbRole;
  @FXML private TextField pwdPassword;
  @FXML private CheckBox chkActive;

  private ObservableList<User> masterUserData;
  private FilteredList<User> filteredUserData;
  private User selectedUser = null;
  private final UserTable usersTable = Session.getUsersTable();

  @FXML
  public void initialize() {
    setupTableColumns();
    loadUserData();
    setupTableListeners();

    List<String> roles = List.of("Admin", "Faculty", "Student");
    cmbRole.setItems(FXCollections.observableArrayList(roles));

    ObservableList<String> filterRoles = FXCollections.observableArrayList("All");
    filterRoles.addAll(roles);
    cmbRoleFilter.setItems(filterRoles);
    cmbRoleFilter.getSelectionModel().selectFirst();

    formPane.setVisible(false);
  }

  private void setupTableColumns() {
    colId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
    colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

    colFullName.setCellValueFactory(cell -> 
      new ReadOnlyStringWrapper(cell.getValue().getFirstName() + " " + cell.getValue().getLastName())
    );

    colStatus.setCellValueFactory(cell -> 
      new ReadOnlyStringWrapper(cell.getValue().isActive() ? "Active" : "Inactive")
    );

    colLastLogin.setCellValueFactory(new PropertyValueFactory<>("lastLogin"));
    colLastLogin.setCellFactory(column -> new TableCell<>() {
      private final SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
      @Override
      protected void updateItem(Date date, boolean empty) {
        super.updateItem(date, empty);
        setText(empty || date == null ? "N/A" : formatter.format(date));
      }
    });

    userTable.setPlaceholder(new Label("No users found."));
  }

  private void loadUserData() {
    statusLabel.setText("Loading user accounts...");
    Task<List<User>> task = new Task<>() {
      @Override
      protected List<User> call() throws Exception {
        return usersTable.getAll();
      }

      @Override
      protected void succeeded() {
        masterUserData = FXCollections.observableArrayList(getValue());
        filteredUserData = new FilteredList<>(masterUserData, p -> true);
        userTable.setItems(filteredUserData);
        filterUsers();
      }

      @Override
      protected void failed() {
        Throwable e = getException();
        statusLabel.setText("Failed to load user data.");
        showErrorAlert("Database Error", "Failed to load users.", e.getMessage());
      }
    };
    new Thread(task).start();
  }

  private void setupTableListeners() {
    userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
      boolean isSelected = newSel != null;
      editButton.setDisable(!isSelected);
      deleteButton.setDisable(!isSelected);
      selectedUser = newSel;
    });

    userTable.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY && selectedUser != null) {
        handleEditUser();
      }
    });

    searchField.textProperty().addListener((obs, oldVal, newVal) -> filterUsers());
    cmbRoleFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterUsers());
  }

  @FXML
  private void handleAddUser() {
    selectedUser = null;
    formTitle.setText("Add New User");
    clearFormFields();
    pwdPassword.setDisable(false);
    formPane.setVisible(true);
  }

  @FXML
  private void handleEditUser() {
    if (selectedUser == null) return;

    formTitle.setText("Edit User: " + selectedUser.getFirstName() + " " + selectedUser.getLastName());
    txtFullName.setText(selectedUser.getFirstName() + " " + selectedUser.getLastName());
    txtEmail.setText(selectedUser.getEmail());
    cmbRole.getSelectionModel().select(selectedUser.getRole());
    chkActive.setSelected(selectedUser.isActive());
    pwdPassword.clear();
    pwdPassword.setText(selectedUser.getPassword());
    pwdPassword.setPromptText("Enter new password to change or leave blank to keep current");
    pwdPassword.setDisable(false);
    formPane.setVisible(true);
  }

  @FXML
  private void handleDeleteUser() {
    if (selectedUser == null) return;

    boolean confirmed = showConfirmationAlert(
        "Confirm Deletion",
        "Are you sure you want to delete user: " + selectedUser.getFirstName() + " " + selectedUser.getLastName() + "?\nThis cannot be undone."
    );

    if (!confirmed) return;

    statusLabel.setText("Deleting user...");
    Task<Void> task = new Task<>() {
      @Override
      protected Void call() throws Exception {
        usersTable.delete(selectedUser.getId());
        return null;
      }

      @Override
      protected void succeeded() {
        showInfoAlert("Success", "User deleted successfully.");
        loadUserData();
      }

      @Override
      protected void failed() {
        Throwable e = getException();
        statusLabel.setText("Deletion failed.");
        showErrorAlert("Database Error", "Failed to delete user.", e.getMessage());
      }
    };
    new Thread(task).start();
  }

  @FXML
  private void handleSaveUser() {
    String fullName = txtFullName.getText().trim();
    String email = txtEmail.getText().trim();
    String role = cmbRole.getValue();
    String password = pwdPassword.getText();
    boolean isActive = chkActive.isSelected();

    if (fullName.isEmpty() || email.isEmpty() || role == null || (selectedUser == null && password.isEmpty())) {
      showWarningAlert("Input Required", REQUIRED_FIELDS_MSG);
      return;
    }

    String[] nameParts = fullName.split("\\s+", 2);
    String firstName = nameParts[0];
    String lastName = nameParts.length > 1 ? nameParts[1] : "";

    final boolean isAddMode = selectedUser == null;
    final User userToModify = isAddMode ? new User() : selectedUser;
    userToModify.setFirstName(firstName);
    userToModify.setLastName(lastName);
    userToModify.setEmail(email);
    userToModify.setRole(role);
    userToModify.setIsActive(isActive);

    statusLabel.setText(isAddMode ? "Adding user..." : "Updating user...");
    Task<Void> task = new Task<>() {
      @Override
      protected Void call() throws Exception {
        if (isAddMode) usersTable.add(userToModify, password);
        else {
          usersTable.update(userToModify);
          if (!password.isEmpty()) usersTable.updatePassword(userToModify.getId(), password);
        }
        return null;
      }

      @Override
      protected void succeeded() {
        showInfoAlert("Success", "User " + (isAddMode ? "added" : "updated") + " successfully.");
        loadUserData();
        handleCancel();
      }

      @Override
      protected void failed() {
        Throwable e = getException();
        statusLabel.setText("Save failed.");
        showErrorAlert("Database Error", "Failed to save user.", e.getMessage());
      }
    };
    new Thread(task).start();
  }

  @FXML
  private void handleCancel() {
    formPane.setVisible(false);
    clearFormFields();
    userTable.getSelectionModel().clearSelection();
    selectedUser = null;
  }

  @FXML
  private void filterUsers() {
    if (filteredUserData == null) return;

    String searchText = searchField.getText().toLowerCase().trim();
    String roleFilter = cmbRoleFilter.getValue();

    filteredUserData.setPredicate(user -> {
      if (roleFilter != null && !roleFilter.equals("All") && !user.getRole().equals(roleFilter)) return false;
      if (searchText.isEmpty()) return true;
      String fullName = (user.getFirstName() + " " + user.getLastName()).toLowerCase();
      String email = user.getEmail().toLowerCase();
      return fullName.contains(searchText) || email.contains(searchText);
    });

    statusLabel.setText(String.format("Displaying %d of %d Users.", filteredUserData.size(), masterUserData.size()));
  }

  private void clearFormFields() {
    txtFullName.clear();
    txtEmail.clear();
    pwdPassword.clear();
    pwdPassword.setPromptText("Password");
    cmbRole.getSelectionModel().clearSelection();
    chkActive.setSelected(true);
  }

  private void showWarningAlert(String title, String content) {
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setTitle(title);
      alert.setHeaderText(null);
      alert.setContentText(content);
      alert.showAndWait();
    });
  }

  private void showInfoAlert(String title, String content) {
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle(title);
      alert.setHeaderText(null);
      alert.setContentText(content);
      alert.showAndWait();
    });
  }

  private boolean showConfirmationAlert(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    Optional<ButtonType> result = alert.showAndWait();
    return result.isPresent() && result.get() == ButtonType.OK;
  }

  private void showErrorAlert(String title, String content, String details) {
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle(title);
      alert.setHeaderText(content);
      alert.setContentText(details.split("\n")[0]);

      TextArea textArea = new TextArea(details);
      textArea.setEditable(false);
      textArea.setWrapText(true);

      ScrollPane scrollPane = new ScrollPane(textArea);
      scrollPane.setFitToWidth(true);
      alert.getDialogPane().setExpandableContent(scrollPane);
      alert.getDialogPane().setExpanded(true);
      alert.showAndWait();
    });
  }
}
