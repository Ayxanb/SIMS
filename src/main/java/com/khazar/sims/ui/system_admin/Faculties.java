package com.khazar.sims.ui.system_admin;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.Faculty;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Controller class for managing Faculty records in the system.
 * Handles display, adding, editing, and deleting of faculties using asynchronous Tasks.
 */
public class Faculties {
  /* === UI Components === */
  @FXML private Button addButton;
  @FXML private Button editButton;
  @FXML private Button deleteButton;
  
  @FXML private TableView<Faculty> facultyTable;
  @FXML private TableColumn<Faculty, Integer> colId;
  @FXML private TableColumn<Faculty, String> colName;
  @FXML private TableColumn<Faculty, String> colCode;
  
  @FXML private Label statusLabel;
  
  @FXML private AnchorPane formPane;
  @FXML private Label formTitle;
  @FXML private TextField txtName;
  @FXML private TextField txtCode;
  
  /* === Data and State === */
  private ObservableList<Faculty> facultyList;
  private Faculty selectedFaculty;
  
  // Flag to track if a task is running.
  private volatile boolean isTaskRunning = false; 

  /**
   * Initializes the controller. Sets up columns, loads data, and sets up UI listeners.
   */
  @FXML
  public void initialize() {
    facultyList = FXCollections.observableArrayList();
    facultyTable.setItems(facultyList);

    setupTableColumns();
    setupTableListeners();
    
    editButton.setDisable(true);
    deleteButton.setDisable(true);
    formPane.setVisible(false);
    
    loadFacultyData();
  }
  
  /**
   * Configures the cell value factories for the TableView columns.
   */
  private void setupTableColumns() {
    colId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colName.setCellValueFactory(new PropertyValueFactory<>("name"));
    colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
  }
  
  /**
   * Sets up listeners for table selection changes and double-click events.
   */
  private void setupTableListeners() {
    /* 1. Enable/Disable Edit/Delete buttons on selection change */
    facultyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      selectedFaculty = newSelection;
      boolean isSelected = newSelection != null;
      
      /* Only update buttons if no task is currently running */
      if (!isTaskRunning) {
        editButton.setDisable(!isSelected);
        deleteButton.setDisable(!isSelected);
      }
    });

    /* 2. Double-click to edit */
    facultyTable.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY && selectedFaculty != null) {
        if (!isTaskRunning) {
          showEditForm();
        }
      }
    });
  }

  /**
   * Loads faculty data from the database asynchronously.
   */
  private void loadFacultyData() {
    statusLabel.setText("Loading faculties...");
    setControlsDisabled(true); /* Disable all controls during load */

    Task<List<Faculty>> loadTask = new Task<>() {
      @Override
      protected List<Faculty> call() throws SQLException {
        return Session.getFacultyTable().getAll();
      }

      @Override
      protected void succeeded() {
        List<Faculty> faculties = getValue();
        facultyList.setAll(faculties);
        statusLabel.setText(String.format("Total Faculties: %d. Double-click to edit.", faculties.size()));
        
        // Cleanup on success
        isTaskRunning = false;
        setControlsDisabled(false); 
      }

      @Override
      protected void failed() {
        Throwable e = getException();
        statusLabel.setText("Failed to load faculties.");
        showErrorAlert("Database Load Error", "Failed to load faculties from the database.", e.getMessage());
        System.err.println("Load failed: " + e);

        // Cleanup on failure
        isTaskRunning = false;
        setControlsDisabled(false); 
      }

      @Override
      protected void running() {
        super.running();
        isTaskRunning = true;
      }
      
      // NOTE: The non-existent 'finished()' method has been removed.
    };

    new Thread(loadTask).start();
  }
  
  /**
   * Checks if the form input fields are valid.
   */
  private boolean isInputValid(final String name, final String code) {
    if (name.isEmpty()) {
      showWarningAlert("Input Missing", "Faculty Name is required.");
      return false;
    }
    if (code.isEmpty()) {
      showWarningAlert("Input Missing", "Faculty Code is required.");
      return false;
    }
    if (code.length() > 5) {
      showWarningAlert("Invalid Code", "Faculty Code should generally be 5 characters or less (e.g., CS, ENG, ARTS).");
      return false;
    }
    return true;
  }

  /* === Event Handlers === */

  @FXML
  private void handleAddFaculty() {
    selectedFaculty = null;
    clearForm();
    formTitle.setText("Add New Faculty");
    formPane.setVisible(true);
  }

  @FXML
  private void handleEditFaculty() {
    showEditForm();
  }
  
  /*
   * Shows the form and populates it with selected faculty data for editing. 
   */
  private void showEditForm() {
    if (selectedFaculty != null) {
      formTitle.setText("Edit Faculty: " + selectedFaculty.getName());
      txtName.setText(selectedFaculty.getName());
      txtCode.setText(selectedFaculty.getCode());
      formPane.setVisible(true);
    }
  }

  /**
   * Handles saving (add or update) a faculty record asynchronously.
   */
  @FXML
  private void handleSaveFaculty() {
    final String name = txtName.getText().trim();
    final String code = txtCode.getText().trim().toUpperCase();

    if (!isInputValid(name, code)) {
      return;
    }

    statusLabel.setText(selectedFaculty == null ? "Adding faculty..." : "Updating faculty...");
    setControlsDisabled(true);

    Task<Void> saveTask = new Task<>() {
      @Override
      protected Void call() throws SQLException {
        if (selectedFaculty == null) {
          /* ADD Mode */
          Faculty newFaculty = new Faculty(name, code);
          Session.getFacultyTable().add(newFaculty);
        }
        else {
          /* EDIT Mode */
          selectedFaculty.setName(name);
          selectedFaculty.setCode(code);
          Session.getFacultyTable().update(selectedFaculty);
        }
        return null;
      }

      @Override
      protected void succeeded() {
        String action = selectedFaculty == null ? "added" : "updated";
        showInfoAlert("Success", "Faculty " + action + " successfully!");
        handleCancel(); /* Close form and clear selection */
        
        // loadFacultyData will start a new task and manage the state transition
        loadFacultyData(); 
      }

      @Override
      protected void failed() {
        Throwable e = getException();
        statusLabel.setText("Save failed.");
        
        String errorMessage = e.getMessage();
        if (errorMessage != null && errorMessage.toLowerCase().contains("unique constraint")) {
          errorMessage = "A Faculty with the same Code or Name already exists.";
        }
        
        showErrorAlert("Database Save Error", "Failed to save faculty.", errorMessage);
        System.err.println("Save failed: " + e);
        
        // Cleanup on failure
        isTaskRunning = false;
        setControlsDisabled(false); 
      }

      @Override
      protected void running() {
        super.running();
        isTaskRunning = true;
      }
    };

    new Thread(saveTask).start();
  }

  /**
   * Handles deleting the selected faculty record asynchronously.
   */
  @FXML
  private void handleDeleteFaculty() {
    if (selectedFaculty == null) return;
    
    final String facultyName = selectedFaculty.getName();

    /* Show confirmation dialog on the FX thread */
    if (showConfirmationAlert("Confirm Deletion", 
      "Are you absolutely sure you want to delete Faculty: " + facultyName + "?\n" +
      "WARNING: This action may result in cascading deletion of all associated Departments, Courses, and linked records.")) {
      
      statusLabel.setText("Deleting faculty: " + facultyName + "...");
      setControlsDisabled(true);

      Task<Void> deleteTask = new Task<>() {
        @Override
        protected Void call() throws SQLException {
          Session.getFacultyTable().delete(selectedFaculty.getId());
          return null;
        }

        @Override
        protected void succeeded() {
          showInfoAlert("Success", "Faculty '" + facultyName + "' deleted successfully!");
          facultyTable.getSelectionModel().clearSelection();
          
          // loadFacultyData will start a new task and manage the state transition
          loadFacultyData(); 
        }

        @Override
        protected void failed() {
          Throwable e = getException();
          statusLabel.setText("Deletion failed.");
          showErrorAlert("Database Delete Error", "Failed to delete faculty '" + facultyName + "'. Check database constraints (e.g., existing departments).", e.getMessage());
          System.err.println("Delete failed: " + e);
          
          // Cleanup on failure
          isTaskRunning = false;
          setControlsDisabled(false);
        }
        
        @Override
        protected void running() {
          super.running();
          isTaskRunning = true;
        }
      };
      
      new Thread(deleteTask).start();
    }
  }

  /**
   * Handles the cancel button, hiding the form and clearing the selection.
   */
  @FXML
  private void handleCancel() {
    formPane.setVisible(false);
    clearForm();
    facultyTable.getSelectionModel().clearSelection();
  }
  
  /**
   * Clears the input fields and resets the selectedFaculty object.
   */
  private void clearForm() {
    txtName.clear();
    txtCode.clear();
    selectedFaculty = null;
  }
  
  /**
   * Disables or enables main action buttons.
   */
  private void setControlsDisabled(boolean disabled) {
    Platform.runLater(() -> {
      // Note: isTaskRunning flag is managed by Task.running() and its result methods (succeeded/failed)
      
      addButton.setDisable(disabled);
      
      /*
       * When disabled=true, disable Edit/Delete regardless of selection (active task).
       * When disabled=false, enable Edit/Delete only if an item is selected (no active task).
       */
      if (disabled) {
        editButton.setDisable(true);
        deleteButton.setDisable(true);
      }
      else {
        boolean isSelected = selectedFaculty != null;
        editButton.setDisable(!isSelected);
        deleteButton.setDisable(!isSelected);
      }
      
      facultyTable.setDisable(disabled);
    });
  }

  /* === Alert Utilities === */
  private void showWarningAlert(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }

  private void showInfoAlert(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
  
  private void showErrorAlert(String title, String content, String details) {
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle(title);
      alert.setHeaderText(content);
      
      /* Create expandable content for details */
      TextArea textArea = new TextArea(details);
      textArea.setEditable(false);
      textArea.setWrapText(true);
      
      alert.getDialogPane().setExpandableContent(textArea);
      alert.getDialogPane().setExpanded(true);
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
}