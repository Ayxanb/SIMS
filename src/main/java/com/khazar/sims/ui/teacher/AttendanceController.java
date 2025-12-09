package com.khazar.sims.ui.teacher;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.Attendance;
import com.khazar.sims.database.data.Enrollment;
import com.khazar.sims.database.data.Schedule;
import com.khazar.sims.database.data.User;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;

/**
 * Enhanced Attendance Controller - Manage student attendance with improved UX
 * All database operations are run asynchronously using JavaFX Task.
 */
public class AttendanceController {
  @FXML private ComboBox<CourseOption> cmbCourse;
  @FXML private DatePicker dpAttendanceDate;
  @FXML private TableView<AttendanceRecord> attendanceTable;
  @FXML private TableColumn<AttendanceRecord, Integer> colStudentNo;
  @FXML private TableColumn<AttendanceRecord, String> colStudentName;
  @FXML private TableColumn<AttendanceRecord, Boolean> colPresent;

  @FXML private Label lblTotalStudents;
  @FXML private Label lblPresentCount;
  @FXML private Label lblAbsentCount;
  @FXML private Label lblAttendanceRate;
  @FXML private Label statusLabel;
  @FXML private Button btnSave;
  @FXML private Button btnMarkAll;
  @FXML private Button btnClearAll;

  private final ObservableList<AttendanceRecord> attendanceRecords = FXCollections.observableArrayList();
  private boolean hasUnsavedChanges = false;
  
  private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

  @FXML
  public void initialize() {
    setupTable();
    setupDatePicker();
    setupListeners();
    loadCourses();
  }
  
  /* ================= SETUP ================= */

  private void setupTable() {
    colStudentNo.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getStudentId()).asObject());
    colStudentName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStudentName()));
    colPresent.setCellValueFactory(cell -> cell.getValue().presentProperty());
    colPresent.setCellFactory(CheckBoxTableCell.forTableColumn(colPresent));

    attendanceTable.setRowFactory(tv -> {
      TableRow<AttendanceRecord> row = new TableRow<>() {
        @Override
        protected void updateItem(AttendanceRecord record, boolean empty) {
          super.updateItem(record, empty);
          
          getStyleClass().removeAll("present", "absent");
          
          if (record != null && !empty) {
            if (record.isPresent()) {
              getStyleClass().add("present");
            } else {
              getStyleClass().add("absent");
            }
          }
        }
      };
      return row;
    });

    attendanceTable.setEditable(true);
    attendanceTable.setItems(attendanceRecords);
    attendanceTable.setPlaceholder(new Label("Select course and date to view attendance"));
  }

  private void setupDatePicker() {
    dpAttendanceDate.setValue(LocalDate.now());
    dpAttendanceDate.setDayCellFactory(picker -> new DateCell() {
      @Override
      public void updateItem(LocalDate date, boolean empty) {
        super.updateItem(date, empty);
        /* Disable future dates */
        if (date.isAfter(LocalDate.now())) {
          setDisable(true);
          setStyle("-fx-background-color: #f0f0f0;");
        }
      }
    });
  }

  private void setupListeners() {
    dpAttendanceDate.setOnAction(e -> loadAttendance());
    cmbCourse.setOnAction(e -> loadAttendance());
  }
  
  /* ================= COURSE LOADING ================= */
  
  private void loadCourses() {
    Task<List<CourseOption>> task = createCourseLoadTask();
    
    task.setOnRunning(e -> {
      updateStatus("Loading assigned courses...", "info");
    });
    
    task.setOnSucceeded(e -> {
      List<CourseOption> options = task.getValue();
      cmbCourse.getItems().setAll(options);

      if (!options.isEmpty()) {
        cmbCourse.getSelectionModel().selectFirst();
        updateStatus("Loaded " + options.size() + " courses", "success");
        loadAttendance();
      }
      else {
        updateStatus("No courses assigned to this teacher", "info");
      }
    });

    task.setOnFailed(e -> {
      Throwable exception = e.getSource().getException();
      updateStatus("Error loading courses: " + exception.getMessage(), "error");
      System.err.println("Course load failed: " + exception);
    });
    
    new Thread(task).start();
  }
  
  private Task<List<CourseOption>> createCourseLoadTask() {
    return new Task<>() {
      @Override
      protected List<CourseOption> call() throws SQLException {
        int teacherId = Session.getActiveUser().getId();
        List<CourseOption> options = new ArrayList<>();
        List<Integer> offeringIds = Session.getScheduleTable().getOfferingsByTeacher(teacherId);
        for (int id : offeringIds) {
          String display = Session.getScheduleTable().getCourseDisplayName(id); 
          options.add(new CourseOption(id, display));
        }
        return options;
      }
    };
  }

  /* ================= ATTENDANCE LOADING ================= */

  private void loadAttendance() {
    CourseOption selected = cmbCourse.getValue();
    LocalDate date = dpAttendanceDate.getValue();

    if (selected == null || date == null) {
      attendanceRecords.clear();
      updateStatistics();
      return;
    }

    Task<List<AttendanceRecord>> task = createAttendanceLoadTask(selected.getOfferingId(), date);
    
    task.setOnRunning(e -> {
      updateStatus("Loading attendance for " + formatDate(date) + "...", "info");
      attendanceTable.setPlaceholder(new Label("Loading attendance..."));
    });
    
    task.setOnSucceeded(e -> {
      List<AttendanceRecord> loadedRecords = task.getValue();
      attendanceRecords.setAll(loadedRecords);
      attendanceRecords.forEach(record -> {
        record.presentProperty().addListener((obs, oldVal, newVal) -> {
          hasUnsavedChanges = true;
          updateStatistics();
          attendanceTable.refresh();
          updateSaveButtonState();
        });
      });

      hasUnsavedChanges = false;
      updateStatistics();
      updateSaveButtonState();
      updateStatus("Attendance loaded for " + formatDate(date) + " - " + attendanceRecords.size() + " students", "success");
    });

    task.setOnFailed(e -> {
      attendanceRecords.clear();
      updateStatistics();
      Throwable exception = e.getSource().getException();
      if (exception instanceof NoScheduleException) {
        updateStatus(exception.getMessage(), "info");
      }
      else if (exception != null) {
        updateStatus("Error loading attendance: " + exception.getMessage(), "error");
        System.err.println("Attendance load failed: " + exception);
      }
      else {
        updateStatus("Error loading attendance: Unknown error", "error");
      }
      attendanceTable.setPlaceholder(new Label("Select course and date to view attendance"));
    });
    
    new Thread(task).start();
  }
  
  private Task<List<AttendanceRecord>> createAttendanceLoadTask(int offeringId, LocalDate date) {
    return new Task<>() {
      @Override
      protected List<AttendanceRecord> call() throws SQLException, NoScheduleException {
        Schedule schedule = Session.getScheduleTable().getByDate(offeringId, date);
        if (schedule == null) {
          throw new NoScheduleException("No class scheduled on " + formatDate(date));
        }

        int scheduleId = schedule.getId();
        
        List<Enrollment> enrollments = Session.getEnrollmentTable().getByOfferingId(offeringId);
        List<Attendance> existing = Session.getAttendanceTable().getForSchedule(scheduleId);

        Map<Integer, Boolean> presentMap = new HashMap<>();
        for (Attendance a : existing) {
          presentMap.put(a.getStudentId(), a.isPresent());
        }

        List<AttendanceRecord> records = new ArrayList<>();
        for (Enrollment e : enrollments) {
          User user = Session.getUsersTable().getById(e.getStudentId());
          
          records.add(new AttendanceRecord(
            user.getFirstName() + " " + user.getLastName(),
            e.getStudentId(),
            presentMap.getOrDefault(e.getStudentId(), false)
          ));
        }
        return records;
      }
    };
  }
  
  /* ================= ACTIONS ================= */

  @FXML
  private void handleSaveAttendance() {
    if (cmbCourse.getValue() == null || dpAttendanceDate.getValue() == null || attendanceRecords.isEmpty()) {
      showAlert("Validation Error", "Please select a course and date, and ensure the table is loaded.", Alert.AlertType.WARNING);
      return;
    }
    
    Task<Void> task = createSaveAttendanceTask();
    
    task.setOnRunning(e -> {
      btnSave.setDisable(true);
      updateStatus("Saving attendance...", "info");
    });
    
    task.setOnSucceeded(e -> {
      hasUnsavedChanges = false;
      btnSave.setDisable(false);
      updateSaveButtonState();
      updateStatus("✓ Attendance saved successfully for " + formatDate(dpAttendanceDate.getValue()), "success");
      showAlert("Success", "Attendance saved successfully!", Alert.AlertType.INFORMATION);
    });

    task.setOnFailed(e -> {
      Throwable exception = e.getSource().getException();
      String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
      updateStatus("Error saving attendance: " + errorMessage, "error");
      showAlert("Save Error", "Failed to save attendance: " + errorMessage, Alert.AlertType.ERROR);
      if (exception != null) {
        System.err.println("Save failed: " + exception);
      }
    });
    
    new Thread(task).start();
  }
  
  private Task<Void> createSaveAttendanceTask() {
    return new Task<>() {
      @Override
      protected Void call() throws SQLException, NoScheduleException {
        int offeringId = cmbCourse.getValue().getOfferingId();
        LocalDate date = dpAttendanceDate.getValue();
        Schedule schedule = Session.getScheduleTable().getByDate(offeringId, date);
        
        if (schedule == null) {
          throw new NoScheduleException("No class scheduled on this date");
        }
        
        int scheduleId = schedule.getId();
        List<Attendance> attendanceList = attendanceRecords.stream()
          .map(record -> new Attendance(scheduleId, record.getStudentId(), record.isPresent()))
          .collect(Collectors.toList());
        Session.getAttendanceTable().saveAttendance(attendanceList);
        return null;
      }
    };
  }

  @FXML
  private void handleMarkAllPresent() {
    if (attendanceRecords.isEmpty()) return;
    
    attendanceRecords.forEach(r -> r.setPresent(true));
    hasUnsavedChanges = true;
    updateStatistics();
    attendanceTable.refresh();
    updateSaveButtonState();
    updateStatus("All students marked as present", "info");
  }

  @FXML
  private void handleClearAll() {
    if (attendanceRecords.isEmpty()) return;
    
    attendanceRecords.forEach(r -> r.setPresent(false));
    hasUnsavedChanges = true;
    updateStatistics();
    attendanceTable.refresh();
    updateSaveButtonState();
    updateStatus("All students marked as absent", "info");
  }

  /* ================= UTILITIES & DTOs ================= */

  private void updateStatistics() {
    int total = attendanceRecords.size();
    long present = attendanceRecords.stream().filter(AttendanceRecord::isPresent).count();
    long absent = total - present;
    
    lblTotalStudents.setText(String.valueOf(total));
    lblPresentCount.setText(String.valueOf(present));
    lblAbsentCount.setText(String.valueOf(absent));
    
    double rate = total > 0 ? (present * 100.0 / total) : 0.0;
    lblAttendanceRate.setText(String.format("%.1f%%", rate));
  }

  private void updateSaveButtonState() {
    if (btnSave != null) {
      if (hasUnsavedChanges) {
        btnSave.getStyleClass().add("unsaved-changes");
      }
      else {
        btnSave.getStyleClass().remove("unsaved-changes");
      }
    }
  }

  private void updateStatus(String message, String type) {
    if (statusLabel != null) {
      Platform.runLater(() -> {
        statusLabel.setText("Status: " + message);
        statusLabel.getStyleClass().removeAll("status-success", "status-error", "status-info");
        statusLabel.getStyleClass().add("status-" + type);
      });
    }
  }

  private void showAlert(String title, String content, Alert.AlertType type) {
    Platform.runLater(() -> {
      Alert alert = new Alert(type);
      alert.setTitle(title);
      alert.setHeaderText(null);
      alert.setContentText(content);
      alert.showAndWait();
    });
  }

  private String formatDate(LocalDate date) {
    return date.format(DATE_FORMAT);
  }

  /**
   * Custom Exception for handling cases where no class is scheduled on a given date.
   */
  private static class NoScheduleException extends Exception {
    public NoScheduleException(String message) {
      super(message);
    }
  }

  public static class AttendanceRecord {
    private final String studentName;
    private final int studentId;
    private final BooleanProperty present;

    public AttendanceRecord(String studentName, int studentId, boolean present) {
      this.studentName = studentName;
      this.studentId = studentId;
      this.present = new SimpleBooleanProperty(present);
    }

    public String getStudentName() { return studentName; }
    public int getStudentId() { return studentId; }
    public boolean isPresent() { return present.get(); }
    public void setPresent(boolean value) { present.set(value); }
    public BooleanProperty presentProperty() { return present; }
  }

  public static class CourseOption {
    private final int offeringId;
    private final String display;

    public CourseOption(int offeringId, String display) {
      this.offeringId = offeringId;
      this.display = display;
    }

    public int getOfferingId() { return offeringId; }
    @Override public String toString() { return display; }
  }
}