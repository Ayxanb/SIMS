package com.khazar.sims.ui.system_admin;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.Department;
import com.khazar.sims.database.data.Faculty;
import com.khazar.sims.database.table.DepartmentTable;
import com.khazar.sims.database.table.FacultyTable;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyStringWrapper;

/**
 * Controller class for managing Department records.
 * Handles display, searching, adding, editing, and deleting of departments,
 * including loading associated Faculty data for selection and display.
 */
public class Departments {
  private static final String INPUT_REQUIRED_TITLE = "Input Required";
  private static final String DB_ERROR_TITLE = "Database Error";
  private static final String REQUIRED_FIELDS_MSG = "Department Name, Code, and Faculty are required fields.";
  
  /* === UI Components === */
  @FXML private TextField searchField;
  @FXML private Button addButton;
  @FXML private Button editButton;
  @FXML private Button deleteButton;
  
  @FXML private TableView<Department> departmentTable;
  @FXML private TableColumn<Department, Integer> colId;
  @FXML private TableColumn<Department, String> colName;
  @FXML private TableColumn<Department, String> colCode;
  @FXML private TableColumn<Department, String> colFaculty;
  @FXML private Label statusLabel;
  
  @FXML private AnchorPane formPane;
  @FXML private Label formTitle;
  @FXML private TextField txtName;
  @FXML private TextField txtCode;
  @FXML private ComboBox<Faculty> cmbFaculty;

  /* === Data and State === */
  private ObservableList<Department> departmentsList;
  private List<Faculty> facultyList;
  private Department selectedDepartment;

  /* Map for fast lookup of Faculty details (Faculty ID -> Faculty object) */
  private Map<Integer, Faculty> facultyMap; 

  private final DepartmentTable departmentsTable = Session.getDepartmentTable();
  private final FacultyTable facultiesTable = Session.getFacultyTable();
  
  private volatile boolean isTaskRunning = false; 

  /**
   * Initializes the controller. Sets up columns, loads supporting data, 
   * loads department data, and sets up UI listeners.
   */
  @FXML
  public void initialize() {
    setupTableColumns();
    loadSupportingData();
    setupTableListeners();
    
    editButton.setDisable(true);
    deleteButton.setDisable(true);
    formPane.setVisible(false);
  }
  
  /**
   * Configures the cell value factories for the TableView columns, 
   * including a custom factory to display the Faculty name.
   */
  private void setupTableColumns() {
    colId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colName.setCellValueFactory(new PropertyValueFactory<>("name"));
    colCode.setCellValueFactory(new PropertyValueFactory<>("code")); 

    /* Custom factory to display the Faculty name instead of just the ID */
    colFaculty.setCellValueFactory(cellData -> {
      if (facultyMap == null) return new ReadOnlyStringWrapper("Loading...");
      
      Faculty faculty = facultyMap.get(cellData.getValue().getFacultyId());
      String facultyName = (faculty != null) ? faculty.getName() : "N/A (Missing)";
      return new ReadOnlyStringWrapper(facultyName);
    });
  }

  /**
   * Loads Faculty data (supporting data for the ComboBox and mapping) asynchronously.
   */
  private void loadSupportingData() {
    statusLabel.setText("Loading supporting data...");
    setControlsDisabled(true);

    Task<List<Faculty>> loadTask = new Task<>() {
      @Override
      protected List<Faculty> call() throws SQLException {
        return facultiesTable.getAll();
      }

      @Override
      protected void succeeded() {
        facultyList = getValue();
        /* Create the map for quick lookups in the TableView */
        facultyMap = facultyList.stream()
          .collect(Collectors.toMap(Faculty::getId, Function.identity()));
          
        cmbFaculty.setItems(FXCollections.observableArrayList(facultyList));
        statusLabel.setText("Supporting data loaded. Loading departments...");

        /* Set up converter to display Faculty objects by name in the ComboBox */
        cmbFaculty.setConverter(new javafx.util.StringConverter<>() {
          @Override
          public String toString(Faculty object) {
            return object != null ? object.getName() : "";
          }
          @Override
          public Faculty fromString(String string) {
            return facultyList.stream()
              .filter(f -> f.getName().equals(string))
              .findFirst()
              .orElse(null);
          }
        });
        
        loadDepartmentData();
      }

      @Override
      protected void failed() {
        Throwable e = getException();
        statusLabel.setText("Failed to load supporting data.");
        showErrorAlert(DB_ERROR_TITLE, "Failed to load faculty data for selection.", e.getMessage());
        e.printStackTrace();
        System.err.println(e);
        isTaskRunning = false;
        setControlsDisabled(false);
      }
      
      @Override
      protected void running() {
        super.running();
        isTaskRunning = true;
      }
    };

    new Thread(loadTask).start();
  }

  /**
   * Loads department data from the database asynchronously.
   */
  private void loadDepartmentData() {
    statusLabel.setText("Loading departments...");
    setControlsDisabled(true);

    Task<List<Department>> loadTask = new Task<>() {
      @Override
      protected List<Department> call() throws SQLException {
        return departmentsTable.getAll();
      }

      @Override
      protected void succeeded() {
        List<Department> departments = getValue();
        departmentsList = FXCollections.observableArrayList(departments);
        departmentTable.setItems(departmentsList);
        statusLabel.setText("Total Departments: " + departments.size() + ". Double-click to edit.");
        
        /* Reapply filter in case it was active */
        filterData(searchField.getText()); 
        
        isTaskRunning = false;
        setControlsDisabled(false);
      }

      @Override
      protected void failed() {
        Throwable e = getException();
        statusLabel.setText("Failed to load departments.");
        showErrorAlert(DB_ERROR_TITLE, "Failed to load departments.", e.getMessage());
        System.err.println(e);
        
        isTaskRunning = false;
        setControlsDisabled(false);
      }
      
      @Override
      protected void running() {
        super.running();
        isTaskRunning = true;
      }
    };
    
    new Thread(loadTask).start();
  }
  
  /**
   * Sets up listeners for table selection, double-click, and search field changes.
   */
  private void setupTableListeners() {
    /* Enable/Disable Edit/Delete buttons on selection change */
    departmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      selectedDepartment = newSelection;
      boolean isSelected = newSelection != null;
      
      if (!isTaskRunning) {
        editButton.setDisable(!isSelected);
        deleteButton.setDisable(!isSelected);
      }
    });

    /* Double-click to edit */
    departmentTable.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
        if (selectedDepartment != null && !isTaskRunning) {
          showEditForm();
        }
      }
    });
    
    /* Live filtering on search field text change */
    searchField.textProperty().addListener((obs, oldVal, newVal) -> {
      filterData(newVal);
    });
  }
  
  /**
   * Filters the TableView data based on the provided search text.
   */
  private void filterData(String searchText) {
    if (departmentsList == null) {
      return;
    }
    
    final String lowerCaseSearchText = searchText.toLowerCase().trim();
    
    if (lowerCaseSearchText.isEmpty()) {
      departmentTable.setItems(departmentsList);
      return;
    }

    ObservableList<Department> filteredList = departmentsList.filtered(d -> {
      Faculty faculty = facultyMap.get(d.getFacultyId());
      String facultyName = (faculty != null) ? faculty.getName() : "";
      
      boolean matchesNameOrCode = d.getName().toLowerCase().contains(lowerCaseSearchText) || 
        d.getCode().toLowerCase().contains(lowerCaseSearchText);
      
      boolean matchesFaculty = !facultyName.isEmpty() && facultyName.toLowerCase().contains(lowerCaseSearchText);
      
      return matchesNameOrCode || matchesFaculty;
    });
      
    departmentTable.setItems(filteredList);
  }

  /* === CRUD Operations === */

  /**
   * Handles the click event for the "Add Department" button.
   */
  @FXML
  private void handleAddDepartment() {
    if (isTaskRunning) return;
    selectedDepartment = null;
    clearForm();
    formTitle.setText("Add New Department");
    formPane.setVisible(true);
  }

  /**
   * Handles the click event for the "Edit Department" button.
   */
  @FXML
  private void handleEditDepartment() {
    if (isTaskRunning) return;
    showEditForm();
  }
  
  /** * Shows the form and populates it with selected department data for editing. 
   */
  private void showEditForm() {
    if (selectedDepartment != null) {
      formTitle.setText("Edit Department: " + selectedDepartment.getName());
      txtName.setText(selectedDepartment.getName());
      txtCode.setText(selectedDepartment.getCode());
      
      cmbFaculty.getSelectionModel().select(facultyMap.get(selectedDepartment.getFacultyId()));
      
      formPane.setVisible(true);
    }
  }

  /**
   * Handles the cancel button, hiding the form and clearing the selection.
   */
  @FXML
  private void handleCancel() {
    formPane.setVisible(false);
    clearForm();
    departmentTable.getSelectionModel().clearSelection();
  }
  
  /**
   * Clears the input fields and resets the selectedDepartment object.
   */
  private void clearForm() {
    txtName.clear();
    txtCode.clear();
    cmbFaculty.getSelectionModel().clearSelection();
    selectedDepartment = null;
  }

  /**
   * Disables or enables main action buttons and the table.
   */
  private void setControlsDisabled(boolean disabled) {
    Platform.runLater(() -> {
      addButton.setDisable(disabled);
      departmentTable.setDisable(disabled);
      
      /* * When disabled=true, disable Edit/Delete regardless of selection.
       * When disabled=false, enable Edit/Delete only if an item is selected.
       */
      if (disabled) {
        editButton.setDisable(true);
        deleteButton.setDisable(true);
      }
      else {
        boolean isSelected = selectedDepartment != null;
        editButton.setDisable(!isSelected);
        deleteButton.setDisable(!isSelected);
      }
    });
  }

  /**
   * Handles saving (add or update) a department record asynchronously.
   */
  @FXML
  private void handleSaveDepartment() {
    if (isTaskRunning) return;
    
    final String name = txtName.getText().trim();
    final String code = txtCode.getText().trim();
    final Faculty faculty = cmbFaculty.getSelectionModel().getSelectedItem();

    if (name.isEmpty() || code.isEmpty() || faculty == null) {
      showWarningAlert(INPUT_REQUIRED_TITLE, REQUIRED_FIELDS_MSG);
      return;
    }
    
    statusLabel.setText(selectedDepartment == null ? "Adding department..." : "Updating department...");
    setControlsDisabled(true);

    Task<Void> saveTask = new Task<>() {
      @Override
      protected Void call() throws SQLException {
        if (selectedDepartment == null) {
          /* ADD Mode */
          Department newDept = new Department(faculty.getId(), name, code);
          departmentsTable.add(newDept);
        } else {
          /* EDIT Mode */
          selectedDepartment.setName(name);
          selectedDepartment.setCode(code);
          selectedDepartment.setFacultyId(faculty.getId());
          departmentsTable.update(selectedDepartment);
        }
        return null;
      }

      @Override
      protected void succeeded() {
        /* UI updates on FX thread */
        String action = selectedDepartment == null ? "added" : "updated";
        showInfoAlert("Success", "Department " + action + " successfully.");
        handleCancel();
        loadDepartmentData(); 
      }

      @Override
      protected void failed() {
        /* UI updates on FX thread */
        Throwable e = getException();
        statusLabel.setText("Save failed.");
        showErrorAlert(DB_ERROR_TITLE, "Failed to save department. Check uniqueness constraints.", e.getMessage());
        System.err.println(e);
        
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
   * Handles deleting the selected department record asynchronously.
   */
  @FXML
  private void handleDeleteDepartment() {
    if (isTaskRunning) return;
    
    if (selectedDepartment == null) {
      return;
    }

    if (showConfirmationAlert("Confirm Deletion", 
      "Are you sure you want to delete Department: " + selectedDepartment.getName() + "?\n" +
      "This action cannot be undone and will affect associated student and course records.")) {
      
      statusLabel.setText("Deleting department...");
      setControlsDisabled(true);
      
      Task<Void> deleteTask = new Task<>() {
        @Override
        protected Void call() throws SQLException {
          departmentsTable.delete(selectedDepartment.getId());
          return null;
        }

        @Override
        protected void succeeded() {
          /* UI updates on FX thread */
          showInfoAlert("Success", "Department deleted successfully.");
          departmentTable.getSelectionModel().clearSelection();
          loadDepartmentData();
        } 
        
        @Override
        protected void failed() {
          /* UI updates on FX thread */
          Throwable e = getException();
          statusLabel.setText("Deletion failed.");
          showErrorAlert(DB_ERROR_TITLE, "Failed to delete department. Possible constraint violation.", e.getMessage());
          e.printStackTrace();
          System.err.println(e);
          
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

  /* === Alert Methods (Synchronous/FX Thread Safe) === */

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
    /* Must run on FX thread, especially if called from a background Task */
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle(title);
      alert.setHeaderText(content);
      
      /* Make detailed error message expandable and scrollable */
      TextArea textArea = new TextArea(details);
      textArea.setEditable(false);
      textArea.setWrapText(true);
      textArea.setMaxWidth(Double.MAX_VALUE);
      textArea.setMaxHeight(Double.MAX_VALUE);
      
      ScrollPane scrollPane = new ScrollPane(textArea);
      scrollPane.setFitToWidth(true);
      
      alert.getDialogPane().setExpandableContent(scrollPane);
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