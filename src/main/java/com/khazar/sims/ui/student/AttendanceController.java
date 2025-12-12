package com.khazar.sims.ui.student;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Student Attendance Controller - View attendance records by course
 * Database operations are now run asynchronously.
 */
public class AttendanceController {
  
  @FXML private ComboBox<CourseOption> cmbCourse;
  @FXML private TableView<AttendanceView> attendanceTable;
  @FXML private TableColumn<AttendanceView, String> colDate;
  @FXML private TableColumn<AttendanceView, String> colDay;
  @FXML private TableColumn<AttendanceView, String> colTime;
  @FXML private TableColumn<AttendanceView, String> colStatus;
  
  @FXML private Label lblTotalSessions;
  @FXML private Label lblPresent;
  @FXML private Label lblAbsent;
  @FXML private Label lblAttendanceRate;
  @FXML private Label statusLabel;
  
  private ObservableList<AttendanceView> attendanceRecords = FXCollections.observableArrayList();
  private int studentUserId;
  private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
  
  @FXML
  public void initialize() {
    setupTable();
    loadCourses();
  }
  
  /* ================= SETUP ================= */
  
  private void setupTable() {
    colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
    colDay.setCellValueFactory(new PropertyValueFactory<>("dayOfWeek"));
    colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
    colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    
    /* Custom cell factory for status column with color coding */
    colStatus.setCellFactory(col -> new TableCell<AttendanceView, String>() {
      @Override
      protected void updateItem(String status, boolean empty) {
        super.updateItem(status, empty);
        
        getStyleClass().removeAll("status-present", "status-absent", "status-not-recorded");
        
        if (empty || status == null) {
          setText(null);
        } else {
          setText(status);
          if (status.equals("Present")) {
            getStyleClass().add("status-present");
          } else if (status.equals("Absent")) {
            getStyleClass().add("status-absent");
          } else {
            getStyleClass().add("status-not-recorded");
          }
        }
      }
    });
    
    /* Row factory for conditional styling */
    attendanceTable.setRowFactory(tv -> new TableRow<AttendanceView>() {
      @Override
      protected void updateItem(AttendanceView record, boolean empty) {
        super.updateItem(record, empty);
        
        getStyleClass().removeAll("row-present", "row-absent", "row-not-recorded");
        
        if (record != null && !empty) {
          if (record.getStatus().equals("Present")) {
            getStyleClass().add("row-present");
          } else if (record.getStatus().equals("Absent")) {
            getStyleClass().add("row-absent");
          } else {
            getStyleClass().add("row-not-recorded");
          }
        }
      }
    });
    
    attendanceTable.setItems(attendanceRecords);
    attendanceTable.setPlaceholder(new Label("Select a course to view attendance"));
  }
  
  /* ================= COURSE LOADING ================= */

  private void loadCourses() {
    
    Task<List<CourseOption>> task = createCourseLoadTask();
    
    task.setOnRunning(e -> {
      updateStatus("Loading enrolled courses...", "info");
    });
    
    task.setOnSucceeded(e -> {
      List<CourseOption> options = task.getValue();
      cmbCourse.getItems().setAll(options);

      if (!options.isEmpty()) {
        cmbCourse.getSelectionModel().selectFirst();
        updateStatus("Loaded " + options.size() + " courses", "success");
        loadAttendance(); /* Initial load */
      } else {
        updateStatus("No courses enrolled", "info");
      }
      
      /* Set listener after initial load to avoid redundant call */
      cmbCourse.setOnAction(evt -> loadAttendance());
    });
    
    task.setOnFailed(e -> {
      updateStatus("Error loading courses: " + e.getSource().getException().getMessage(), "error");
    });
    
    new Thread(task).start();
  }
  
  private Task<List<CourseOption>> createCourseLoadTask() {
    return new Task<>() {
      @Override
      protected List<CourseOption> call() throws Exception {
        int userId = Session.getActiveUser().getId();
        Student student = Session.getStudentTable().getByUserId(userId);
        studentUserId = student.getUserId();

        List<Enrollment> enrollments = Session.getEnrollmentTable().getByStudentId(studentUserId);
        List<CourseOption> options = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
          CourseOffering offering = Session.getCourseOfferingTable().getById(enrollment.getCourseOfferingId());
          Course course = Session.getCourseTable().getById(offering.getCourseId());
          
          String display = String.format("%s - %s (Section %s)", 
            course.getCode(), course.getName(), offering.getSection());
          options.add(new CourseOption(offering.getId(), display));
        }
        return options;
      }
    };
  }

  /* ================= ATTENDANCE LOADING ================= */
  
  private void loadAttendance() {
    CourseOption selected = cmbCourse.getValue();
    if (selected == null) {
      attendanceRecords.clear();
      updateStatistics();
      return;
    }
    
    Task<List<AttendanceView>> task = createAttendanceLoadTask(selected.getOfferingId());

    task.setOnRunning(e -> {
      updateStatus("Loading attendance for " + selected.toString() + "...", "info");
      attendanceTable.setPlaceholder(new Label("Loading attendance..."));
    });
    
    task.setOnSucceeded(e -> {
      attendanceRecords.setAll(task.getValue());
      updateStatistics();
      updateStatus("Attendance loaded - " + attendanceRecords.size() + " sessions", "success");
    });
    
    task.setOnFailed(e -> {
      attendanceRecords.clear();
      updateStatistics();
      Throwable exception = e.getSource().getException();
      updateStatus("Error loading attendance: " + exception.getMessage(), "error");
      exception.printStackTrace();
      attendanceTable.setPlaceholder(new Label("Error loading attendance."));
    });
    
    new Thread(task).start();
  }

  private Task<List<AttendanceView>> createAttendanceLoadTask(int offeringId) {
    return new Task<>() {
      @Override
      protected List<AttendanceView> call() throws SQLException {
        
        /* 1. Get all scheduled sessions (with dates) for the course offering */
        List<Schedule> schedules = Session.getScheduleTable().getSchedulesForOffering(offeringId);
        if (schedules.isEmpty()) {
          return new ArrayList<>();
        }
        
        /* 2. Get all attendance records for this student across ALL schedules */
        List<Attendance> allStudentAttendance = Session.getAttendanceTable().getByStudentId(studentUserId);
        
        /* Map attendance records by schedule ID for quick lookup: ScheduleId -> isPresent */
        Map<Integer, Boolean> attendanceMap = allStudentAttendance.stream()
          .collect(Collectors.toMap(Attendance::getSessionId, Attendance::isPresent, (a, b) -> a));

        List<AttendanceView> records = new ArrayList<>();
        
        /* 3. Combine schedules with attendance status */
        for (Schedule schedule : schedules) {
          
          Boolean present = attendanceMap.get(schedule.getId());
          
          String status;
          if (present == null) {
            status = "Not Recorded";
          } else if (present) {
            status = "Present";
          } else {
            status = "Absent";
          }
          
          String date = schedule.getDate().format(DATE_FORMATTER);
          String time = formatTime(
            schedule.getStartTime().toString(), 
            schedule.getEndTime().toString()
          );
          
          records.add(new AttendanceView(
            date, 
            schedule.getDayOfWeek(),
            time,
            status
          ));
        }
        
        /* Sorting is already handled in the ScheduleTable query (ORDER BY date) */
        return records;
      }
    };
  }
  
  /* ================= UTILITIES & DTOs ================= */
  
  private String formatTime(String start, String end) {
    /* Extracts HH:mm from time strings */
    try {
      String startTime = start.length() >= 5 ? start.substring(0, 5) : start;
      String endTime = end.length() >= 5 ? end.substring(0, 5) : end;
      return startTime + " - " + endTime;
    } catch (Exception e) {
      return start + " - " + end;
    }
  }
  
  private void updateStatistics() {
    int total = attendanceRecords.size();
    long present = attendanceRecords.stream()
      .filter(a -> a.getStatus().equals("Present"))
      .count();
    long absent = attendanceRecords.stream()
      .filter(a -> a.getStatus().equals("Absent"))
      .count();
    
    lblTotalSessions.setText(String.valueOf(total));
    lblPresent.setText(String.valueOf(present));
    lblAbsent.setText(String.valueOf(absent));
    
    /* Calculate rate based on recorded sessions only (excluding "Not Recorded") */
    long recordedSessions = present + absent;
    double rate = recordedSessions > 0 ? (present * 100.0 / recordedSessions) : 0.0;
    lblAttendanceRate.setText(String.format("%.1f%%", rate));
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
  
  public static class AttendanceView {
    private final String date;
    private final String dayOfWeek;
    private final String time;
    private final String status;
    
    public AttendanceView(String date, String dayOfWeek, String time, String status) {
      this.date = date;
      this.dayOfWeek = dayOfWeek;
      this.time = time;
      this.status = status;
    }
    
    public String getDate() { return date; }
    public String getDayOfWeek() { return dayOfWeek; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
  }
  
  public static class CourseOption {
    private final int offeringId;
    private final String display;
    
    public CourseOption(int offeringId, String display) {
      this.offeringId = offeringId;
      this.display = display;
    }
    
    public int getOfferingId() { return offeringId; }
    
    @Override
    public String toString() { return display; }
  }
}