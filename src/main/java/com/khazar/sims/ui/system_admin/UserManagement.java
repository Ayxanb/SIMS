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
import javafx.util.Callback;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the User Management interface.
 * Handles display, searching, filtering, and CRUD operations for user accounts
 * (Create, Read, Update, Delete).
 * The view is dynamically filtered based on search input and role selection.
 */
public class UserManagement {
  /* --- CONSTANTS --- */
  private static final String INPUT_REQUIRED_TITLE = "Input Required";
  private static final String DB_ERROR_TITLE = "Database Error";
  private static final String REQUIRED_FIELDS_MSG = "Full Name, Email, Role, and a Password (for new users) are required.";
  private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

  /* --- FXML Elements: Table and Filters --- */
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

  /* --- FXML Elements: Add/Edit Form Overlay --- */
  @FXML private AnchorPane formPane;
  @FXML private Label formTitle;
  @FXML private TextField txtFullName;
  @FXML private TextField txtEmail;
  @FXML private ComboBox<String> cmbRole;
  @FXML private PasswordField pwdPassword;
  @FXML private CheckBox chkActive;

  /* --- Data and State Management --- */
  private ObservableList<User> masterUserData; /* Holds all users fetched from the DB */
  private FilteredList<User> filteredUserData; /* Used for table display (search/filter applied) */
  private User selectedUser = null; /* Holds the user object currently being edited/added */

  private final UserTable usersTable = Session.getUsersTable(); /* Database access layer */

  /**
   * Initializes the controller. Sets up columns, loads initial data,
   * and attaches listeners for UI interaction, including dynamic filtering.
   */
  @FXML
  public void initialize() {
    /* Set up how data maps to table columns */
    setupTableColumns();
    /* Load data from the database into the TableView */
    loadUserData();
    /* Configure interactive listeners for the table and filters */
    setupTableListeners();

    /* Populate Role ComboBoxes for both form and filter */
    List<String> roles = List.of("Admin", "Faculty", "Student");
    cmbRole.setItems(FXCollections.observableArrayList(roles));

    /* Add "All" option to the filter combo box and select it by default */
    List<String> filterRoles = new java.util.ArrayList<>(roles);
    filterRoles.add(0, "All");
    cmbRoleFilter.setItems(FXCollections.observableArrayList(filterRoles));
    cmbRoleFilter.getSelectionModel().selectFirst();

    /* Ensure the form pane is initially hidden */
    formPane.setVisible(false);
  }

  /**
   * Configures the cell value factories for all TableView columns,
   * including custom handling for Full Name, Status, and formatted Last Login date.
   */
  private void setupTableColumns() {
    /* Standard PropertyValueFactory for simple fields */
    colId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
    colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

    /* Custom factory for combining first and last name */
    colFullName.setCellValueFactory(cellData ->
        new ReadOnlyStringWrapper(cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName())
    );

    /* Custom factory for displaying boolean 'isActive' as 'Active'/'Inactive' */
    colStatus.setCellValueFactory(cellData -> {
      String status = cellData.getValue().isActive() ? "Active" : "Inactive";
      return new ReadOnlyStringWrapper(status);
    });

    /* Custom factory for the Date object (set property) */
    colLastLogin.setCellValueFactory(new PropertyValueFactory<>("lastLogin"));

    /* Custom cell factory to format the Date object for display */
    colLastLogin.setCellFactory(column -> new TableCell<User, Date>() {
      private final SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);

      @Override
      protected void updateItem(Date date, boolean empty) {
        super.updateItem(date, empty);
        if (empty || date == null) {
          setText("N/A");
        } else {
          setText(formatter.format(date));
        }
      }
    });
  }

  /**
   * Loads user data from the database asynchronously using a JavaFX Task.
   * Updates the status label and initializes the FilteredList upon success.
   */
  private void loadUserData() {
    statusLabel.setText("Loading user accounts...");

    Task<List<User>> loadTask = new Task<>() {
      @Override
      protected List<User> call() throws SQLException {
        /* Database operation in background thread */
        return usersTable.getAll();
      }

      @Override
      protected void succeeded() {
        /* Update UI on the JavaFX Application Thread */
        masterUserData = FXCollections.observableArrayList(getValue());

        /* Initialize FilteredList based on master data */
        filteredUserData = new FilteredList<>(masterUserData);
        userTable.setItems(filteredUserData);

        /* Apply current filters/search to the newly loaded data */
        filterUsers();
      }

      @Override
      protected void failed() {
        /* Handle errors on the JavaFX Application Thread */
        Throwable e = getException();
        statusLabel.setText("Failed to load user data.");
        showErrorAlert(DB_ERROR_TITLE, "Failed to load user accounts.", e.getMessage());
      }
    };

    new Thread(loadTask).start(); /* Start the background loading thread */
  }

  /**
   * Sets up listeners for double-click editing, search field changes,
   * and role filter selection changes, all triggering `filterUsers()`.
   */
  private void setupTableListeners() {
    /* 1. Enable/Disable Edit/Delete buttons on selection change */
    userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      boolean isSelected = newSelection != null;
      editButton.setDisable(!isSelected);
      deleteButton.setDisable(!isSelected);
      selectedUser = newSelection;
    });

    /* 2. Double-click to edit */
    userTable.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
        if (selectedUser != null) {
          handleEditUser();
        }
      }
    });

    /* 3. Dynamic Filtering: Search field listener */
    searchField.textProperty().addListener((obs, oldVal, newVal) -> filterUsers());

    /* 4. Dynamic Filtering: Role filter listener */
    cmbRoleFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterUsers()); // <-- Listener attached
  }

  /**
   * Handles the click event for the "Add User" button.
   * Clears the form, sets the mode to ADD, and shows the form pane.
   */
  @FXML
  private void handleAddUser() {
    selectedUser = null; /* Null indicates ADD mode */
    formTitle.setText("Add New User");
    clearFormFields();
    pwdPassword.setPromptText("Required for new user");
    pwdPassword.setDisable(false);
    formPane.setVisible(true);
  }

  /**
   * Handles the click event for the "Edit User" button or table double-click.
   * Populates the form with the selected user's data and sets the mode to EDIT.
   */
  @FXML
  private void handleEditUser() {
    final User userToEdit = userTable.getSelectionModel().getSelectedItem();
    if (userToEdit == null) {
      showWarningAlert(INPUT_REQUIRED_TITLE, "Please select a user to edit.");
      return;
    }

    selectedUser = userToEdit;

    /* Populate fields from the selected User object */
    String fullName = userToEdit.getFirstName() + " " + userToEdit.getLastName();
    formTitle.setText(String.format("Edit User: %s (ID: %d)", fullName, userToEdit.getId()));

    txtFullName.setText(fullName);
    txtEmail.setText(userToEdit.getEmail());
    cmbRole.getSelectionModel().select(userToEdit.getRole());
    chkActive.setSelected(userToEdit.isActive());

    /* Password field is cleared and remains enabled. Save logic will only update
     * password if the user explicitly enters a value.
     */
    pwdPassword.clear();
    pwdPassword.setPromptText("Enter new password to change, leave blank to keep old.");
    pwdPassword.setDisable(false);

    formPane.setVisible(true);
  }

  /**
   * Handles deleting the selected user record asynchronously after confirmation.
   */
  @FXML
  private void handleDeleteUser() {
    final User userToDelete = userTable.getSelectionModel().getSelectedItem();
    if (userToDelete == null) {
      showWarningAlert(INPUT_REQUIRED_TITLE, "Please select a user to delete.");
      return;
    }

    if (showConfirmationAlert("Confirm Deletion",
        "Are you sure you want to delete user: " + userToDelete.getFirstName() + " " + userToDelete.getLastName() + "?\n" +
            "This action cannot be undone.")) {

      statusLabel.setText("Deleting user...");

      Task<Void> deleteTask = new Task<>() {
        @Override
        protected Void call() throws SQLException {
          usersTable.delete(userToDelete.getId());
          return null;
        }

        @Override
        protected void succeeded() {
          /* Reload data upon successful deletion */
          showInfoAlert("Success", "User deleted successfully.");
          loadUserData();
        }

        @Override
        protected void failed() {
          /* Handle deletion failure */
          Throwable e = getException();
          statusLabel.setText("Deletion failed.");
          showErrorAlert(DB_ERROR_TITLE, "Failed to delete user. Possible related records exist.", e.getMessage());
        }
      };

      new Thread(deleteTask).start();
    }
  }

  /**
   * Handles saving (add or update) a user record asynchronously.
   * Validates input before performing the database operation.
   */
  @FXML
  private void handleSaveUser() {
    /* Capture current form input */
    final String fullName = txtFullName.getText().trim();
    final String email = txtEmail.getText().trim();
    final String role = cmbRole.getValue();
    final String password = pwdPassword.getText();
    final boolean isActive = chkActive.isSelected();

    /* Simple split of Full Name into First and Last Name (robust to single/multiple spaces) */
    String firstName, lastName;
    String[] nameParts = fullName.split("\\s+", 2);
    firstName = nameParts.length > 0 ? nameParts[0] : "";
    lastName = nameParts.length > 1 ? nameParts[1] : "";
    final String finalFirstName = firstName;
    final String finalLastName = lastName;


    /* Input Validation */
    if (fullName.isEmpty() || email.isEmpty() || role == null ||
        (selectedUser == null && password.isEmpty())) { /* Password required for ADD mode */
      showWarningAlert(INPUT_REQUIRED_TITLE, REQUIRED_FIELDS_MSG);
      return;
    }

    statusLabel.setText(selectedUser == null ? "Adding user..." : "Updating user...");
    final boolean isAddMode = selectedUser == null;
    final User userToModify = isAddMode ? new User() : selectedUser;

    Task<Void> saveTask = new Task<>() {
      @Override
      protected Void call() throws SQLException {
        /* Update user object details for both Add and Edit modes */
        userToModify.setFirstName(finalFirstName);
        userToModify.setLastName(finalLastName);
        userToModify.setEmail(email);
        userToModify.setRole(role);
        userToModify.setIsActive(isActive);

        if (isAddMode) {
          /* --- ADD Mode --- */
          /* Assuming usersTable.add() handles password hashing */
          usersTable.add(userToModify, password);
        }
        else {
          /* --- EDIT Mode --- */
          /* Update user details (excluding password) */
          usersTable.update(userToModify);

          /* Only update password if a new one was provided */
          if (!password.isEmpty()) {
            usersTable.updatePassword(userToModify.getId(), password);
          }
        }
        return null;
      }

      @Override
      protected void succeeded() {
        String action = isAddMode ? "added" : "updated";
        showInfoAlert("Success", "User " + action + " successfully.");
        loadUserData(); /* Reload to update the table immediately */
        handleCancel(); /* Close the form */
      }

      @Override
      protected void failed() {
        Throwable e = getException();
        statusLabel.setText("Save failed.");
        showErrorAlert(DB_ERROR_TITLE, "Failed to save user. Check email uniqueness or database constraints.", e.getMessage());
      }
    };

    new Thread(saveTask).start();
  }

  /**
   * Handles the cancel button, hiding the form, clearing the input fields,
   * and resetting the selected user state.
   */
  @FXML
  private void handleCancel() {
    formPane.setVisible(false);
    clearFormFields();
    userTable.getSelectionModel().clearSelection();
    selectedUser = null;
  }

  /**
   * Filters the user data displayed in the TableView based on the text in
   * the search field and the selected role filter.
   */
  @FXML
  private void filterUsers() {
    if (filteredUserData == null) {
      return; /* Data not yet loaded */
    }

    final String searchText = searchField.getText().toLowerCase().trim();
    final String roleFilter = cmbRoleFilter.getValue();

    filteredUserData.setPredicate(user -> {
      /* 1. Role Filter Check: Check if a specific role is selected, and if the user's role does not match it. */
      if (roleFilter != null && !roleFilter.equals("All") && !user.getRole().equals(roleFilter)) {
        return false; /* Does not match role filter, so exclude the user */
      }

      /* 2. Search Text Filter Check (Full Name or Email) */
      if (searchText.isEmpty()) {
        return true; /* Passes if no search text */
      }

      String fullName = (user.getFirstName() + " " + user.getLastName()).toLowerCase();
      String email = user.getEmail().toLowerCase();

      return fullName.contains(searchText) || email.contains(searchText);
    });

    /* Update status label with current display count */
    if (masterUserData != null) {
      statusLabel.setText(String.format("Displaying %d of %d Users.", filteredUserData.size(), masterUserData.size()));
    }
  }

  /**
   * Clears all input fields on the Add/Edit form.
   */
  private void clearFormFields() {
    txtFullName.clear();
    txtEmail.clear();
    pwdPassword.clear();
    cmbRole.getSelectionModel().clearSelection();
    chkActive.setSelected(true);
    pwdPassword.setPromptText("Password");
  }

  /* --- Common Alert Utility Methods --- */

  /**
   * Displays a standard warning alert dialog.
   */
  private void showWarningAlert(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }

  /**
   * Displays a standard informational alert dialog.
   */
  private void showInfoAlert(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }

  /**
   * Displays a confirmation dialog and returns true if OK is clicked.
   */
  private boolean showConfirmationAlert(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);

    Optional<ButtonType> result = alert.showAndWait();
    return result.isPresent() && result.get() == ButtonType.OK;
  }

  /**
   * Displays a detailed error alert with an expandable stack trace/details section.
   * Ensures the alert is shown safely on the JavaFX Application Thread.
   */
  private void showErrorAlert(String title, String content, String details) {
    /* Platform.runLater ensures this UI update is thread-safe */
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle(title);
      alert.setHeaderText(content);
      alert.setContentText("Details: " + details.lines().findFirst().orElse("No further details available."));

      /* Add full error details to an expandable section */
      TextArea textArea = new TextArea(details);
      textArea.setEditable(false);
      textArea.setWrapText(true);
      textArea.setMaxWidth(Double.MAX_VALUE);
      textArea.setMaxHeight(Double.MAX_VALUE);
      textArea.setStyle("-fx-font-family: monospace;");

      ScrollPane scrollPane = new ScrollPane(textArea);
      scrollPane.setFitToWidth(true);

      alert.getDialogPane().setExpandableContent(scrollPane);
      alert.getDialogPane().setExpanded(true);

      alert.showAndWait();
    });
  }
}