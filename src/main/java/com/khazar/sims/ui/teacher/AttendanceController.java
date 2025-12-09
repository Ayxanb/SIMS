package com.khazar.sims.ui.teacher;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;

/**
 * AttendanceController - Manage student attendance with async loading and optimized UI handling.
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

  private final ObservableList<AttendanceRecord> attendanceRecords = FXCollections.observableArrayList();
  private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

  private boolean hasUnsavedChanges = false;
  private List<LocalDate> cachedValidDates = new ArrayList<>();

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

    attendanceTable.setRowFactory(tv -> new TableRow<>() {
      @Override
      protected void updateItem(AttendanceRecord record, boolean empty) {
        super.updateItem(record, empty);
        getStyleClass().removeAll("present", "absent");
        if (record != null && !empty) {
          getStyleClass().add(record.isPresent() ? "present" : "absent");
        }
      }
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

        CourseOption selected = cmbCourse.getValue();
        if (empty || date == null || selected == null || !cachedValidDates.contains(date)) {
          setDisable(true);
          setStyle("-fx-background-color: #f0f0f0; -fx-opacity: 0.5;");
          setTooltip(new Tooltip("No class scheduled on this date"));
        } else {
          setDisable(false);
          setStyle("");
          setTooltip(null);
        }
      }
    });
  }

  private void setupListeners() {
    cmbCourse.setOnAction(e -> loadCourseDatesAndAttendance());
    dpAttendanceDate.setOnAction(e -> loadAttendance());
  }

  /* ================= COURSE & SCHEDULE ================= */

  private void loadCourses() {
    Task<List<CourseOption>> task = new Task<>() {
      @Override
      protected List<CourseOption> call() throws SQLException {
        int teacherId = Session.getActiveUser().getId();
        List<Integer> offeringIds = Session.getScheduleTable().getOfferingsByTeacher(teacherId);
        List<CourseOption> courses = new ArrayList<>();
        for (int id : offeringIds) {
          String display = Session.getScheduleTable().getCourseDisplayName(id);
          courses.add(new CourseOption(id, display));
        }
        return courses;
      }
    };

    task.setOnRunning(e -> updateStatus("Loading assigned courses...", "info"));
    task.setOnSucceeded(e -> {
      List<CourseOption> courses = task.getValue();
      cmbCourse.getItems().setAll(courses);
      if (!courses.isEmpty()) cmbCourse.getSelectionModel().selectFirst();
      updateStatus(courses.isEmpty() ? "No courses assigned" : "Courses loaded", "success");
    });
    task.setOnFailed(e -> updateStatus("Error loading courses: " + task.getException().getMessage(), "error"));

    new Thread(task).start();
  }

  private void loadCourseDatesAndAttendance() {
    CourseOption selected = cmbCourse.getValue();
    if (selected == null) {
      cachedValidDates.clear();
      loadAttendance();
      return;
    }

    Task<List<LocalDate>> task = new Task<>() {
      @Override
      protected List<LocalDate> call() throws SQLException {
        return Session.getScheduleTable().getActualClassDates(selected.getCourseOfferingId());
      }
    };

    task.setOnRunning(e -> dpAttendanceDate.setDisable(true));
    task.setOnSucceeded(e -> {
      cachedValidDates = task.getValue();
      dpAttendanceDate.setDisable(false);

      // Refresh DatePicker display
      LocalDate current = dpAttendanceDate.getValue();
      dpAttendanceDate.setValue(null);
      dpAttendanceDate.setValue(current);

      loadAttendance();
    });
    task.setOnFailed(e -> {
      cachedValidDates.clear();
      dpAttendanceDate.setDisable(false);
      loadAttendance();
    });

    new Thread(task).start();
  }

  /* ================= ATTENDANCE ================= */

  private void loadAttendance() {
    CourseOption selected = cmbCourse.getValue();
    LocalDate date = dpAttendanceDate.getValue();
    if (selected == null || date == null) {
      attendanceRecords.clear();
      updateStatistics();
      return;
    }

    Task<List<AttendanceRecord>> task = new Task<>() {
      @Override
      protected List<AttendanceRecord> call() throws SQLException, NoScheduleException {
        Schedule schedule = Session.getScheduleTable().getByDate(selected.getCourseOfferingId(), date);
        if (schedule == null) throw new NoScheduleException("No class scheduled on " + formatDate(date));

        List<Enrollment> enrollments = Session.getEnrollmentTable().getByOfferingId(selected.getCourseOfferingId());
        List<Attendance> existing = Session.getAttendanceTable().getForSchedule(schedule.getId());

        Map<Integer, Boolean> presentMap = existing.stream()
            .collect(Collectors.toMap(Attendance::getStudentId, Attendance::isPresent));

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

    task.setOnRunning(e -> attendanceTable.setPlaceholder(new Label("Loading attendance...")));
    task.setOnSucceeded(e -> {
      attendanceRecords.setAll(task.getValue());
      attendanceRecords.forEach(r -> r.presentProperty().addListener((obs, o, n) -> markUnsaved()));
      hasUnsavedChanges = false;
      updateStatistics();
      updateSaveButtonState();
    });
    task.setOnFailed(e -> {
      attendanceRecords.clear();
      updateStatistics();
      attendanceTable.setPlaceholder(new Label("Select course and date to view attendance"));
    });

    new Thread(task).start();
  }

  private void markUnsaved() {
    hasUnsavedChanges = true;
    updateStatistics();
    attendanceTable.refresh();
    updateSaveButtonState();
  }

  @FXML
  private void handleSaveAttendance() {
    CourseOption selected = cmbCourse.getValue();
    LocalDate date = dpAttendanceDate.getValue();

    if (selected == null || date == null || attendanceRecords.isEmpty()) {
      showAlert("Validation Error", "Select course and date, ensure table is loaded.", Alert.AlertType.WARNING);
      return;
    }

    Task<Void> task = new Task<>() {
      @Override
      protected Void call() throws SQLException, NoScheduleException {
        Schedule schedule = Session.getScheduleTable().getByDate(selected.getCourseOfferingId(), date);
        if (schedule == null) throw new NoScheduleException("No class scheduled on this date");

        List<Attendance> attendanceList = attendanceRecords.stream()
            .map(r -> new Attendance(schedule.getId(), r.getStudentId(), r.isPresent()))
            .collect(Collectors.toList());

        Session.getAttendanceTable().saveAttendance(attendanceList);
        return null;
      }
    };

    task.setOnRunning(e -> btnSave.setDisable(true));
    task.setOnSucceeded(e -> {
      hasUnsavedChanges = false;
      btnSave.setDisable(false);
      updateSaveButtonState();
      showAlert("Success", "Attendance saved successfully!", Alert.AlertType.INFORMATION);
    });
    task.setOnFailed(e -> showAlert("Error", "Failed to save attendance: " + task.getException().getMessage(), Alert.AlertType.ERROR));

    new Thread(task).start();
  }

  @FXML
  private void handleMarkAllPresent() {
    attendanceRecords.forEach(r -> r.setPresent(true));
    markUnsaved();
  }

  @FXML
  private void handleClearAll() {
    attendanceRecords.forEach(r -> r.setPresent(false));
    markUnsaved();
  }

  /* ================= UTILITIES ================= */

  private void updateStatistics() {
    int total = attendanceRecords.size();
    long present = attendanceRecords.stream().filter(AttendanceRecord::isPresent).count();
    lblTotalStudents.setText(String.valueOf(total));
    lblPresentCount.setText(String.valueOf(present));
    lblAbsentCount.setText(String.valueOf(total - present));
    lblAttendanceRate.setText(total > 0 ? String.format("%.1f%%", present * 100.0 / total) : "0%");
  }

  private void updateSaveButtonState() {
    if (btnSave == null) return;
    if (hasUnsavedChanges) btnSave.getStyleClass().add("unsaved-changes");
    else btnSave.getStyleClass().remove("unsaved-changes");
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

  /* ================= DTOs ================= */

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
    public CourseOption(int offeringId, String display) { this.offeringId = offeringId; this.display = display; }
    public int getCourseOfferingId() { return offeringId; }
    @Override public String toString() { return display; }
  }

  private static class NoScheduleException extends Exception {
    public NoScheduleException(String message) { super(message); }
  }
}
