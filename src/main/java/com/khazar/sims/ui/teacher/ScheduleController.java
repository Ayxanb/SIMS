package com.khazar.sims.ui.teacher;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.Course;
import com.khazar.sims.database.data.CourseOffering;
import com.khazar.sims.database.data.Schedule;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller for Teacher's Schedule View.
 */
public class ScheduleController {
  @FXML private TableView<ScheduleView> scheduleTable;
  @FXML private TableColumn<ScheduleView, String> colDay;
  @FXML private TableColumn<ScheduleView, String> colTime;
  @FXML private TableColumn<ScheduleView, String> colCourse;
  @FXML private TableColumn<ScheduleView, String> colRoom;
  @FXML private TableColumn<ScheduleView, Integer> colEnrolled;

  @FXML private Label lblTotalClasses;
  @FXML private Label lblTotalHours;
  @FXML private Label lblDaysTeaching;
  @FXML private Label statusLabel;

  private final ObservableList<ScheduleView> scheduleItems = FXCollections.observableArrayList();
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  @FXML
  public void initialize() {
    setupTable();
    loadScheduleAsync();
  }

  private void setupTable() {
    colDay.setCellValueFactory(new PropertyValueFactory<>("dayOfWeek"));
    colTime.setCellValueFactory(new PropertyValueFactory<>("timeRange"));
    colCourse.setCellValueFactory(new PropertyValueFactory<>("courseName"));
    colRoom.setCellValueFactory(new PropertyValueFactory<>("room"));
    colEnrolled.setCellValueFactory(new PropertyValueFactory<>("enrolledCount"));

    scheduleTable.setItems(scheduleItems);
    scheduleTable.setPlaceholder(new Label("No classes scheduled"));
  }

  private void loadScheduleAsync() {
    Task<List<ScheduleView>> task = new Task<>() {
      @Override
      protected List<ScheduleView> call() throws SQLException {
        int teacherId = Session.getActiveUser().getId();
        List<ScheduleView> views = new ArrayList<>();

        for (CourseOffering offering : Session.getCourseOfferingTable().getByTeacherId(teacherId)) {
          Course course = Session.getCourseTable().getById(offering.getCourseId());
          int enrolledCount = Session.getEnrollmentTable().getByOfferingId(offering.getId()).size();

          for (Schedule schedule : Session.getScheduleTable().getSchedulesForOffering(offering.getId())) {
            views.add(new ScheduleView(
                schedule.getDayOfWeek(),
                formatTime(schedule.getStartTime(), schedule.getEndTime()),
                course.getCode() + " - " + course.getName(),
                offering.getSection(),
                schedule.getRoom(),
                enrolledCount
            ));
          }
        }
        return views;
      }
    };

    task.setOnRunning(e -> {
      updateStatus("Loading weekly schedule...", "info");
      scheduleTable.setPlaceholder(new Label("Loading schedule..."));
    });

    task.setOnSucceeded(e -> {
      scheduleItems.setAll(task.getValue());
      sortSchedule();
      updateStatistics();
      updateStatus("Schedule loaded - " + scheduleItems.size() + " classes", "success");
    });

    task.setOnFailed(e -> {
      Throwable ex = task.getException();
      updateStatus("Error loading schedule: " + (ex != null ? ex.getMessage() : "Unknown"), "error");
      System.err.println("Schedule load failed: " + (ex != null ? ex.getMessage() : "Unknown"));
    });

    new Thread(task).start();
  }

  private void sortSchedule() {
    scheduleItems.sort(Comparator
        .comparingInt((ScheduleView s) -> getDayOrder(s.getDayOfWeek()))
        .thenComparing(ScheduleView::getTimeRange));
  }

  private String formatTime(LocalTime start, LocalTime end) {
    return start.format(TIME_FORMATTER) + " - " + end.format(TIME_FORMATTER);
  }

  private int getDayOrder(String day) {
    return switch (day.toUpperCase(Locale.ROOT)) {
      case "MON" -> 1;
      case "TUE" -> 2;
      case "WED" -> 3;
      case "THU" -> 4;
      case "FRI" -> 5;
      case "SAT" -> 6;
      case "SUN" -> 7;
      default -> 8;
    };
  }

  private void updateStatistics() {
    int totalClasses = scheduleItems.size();
    lblTotalClasses.setText(String.valueOf(totalClasses));

    Set<String> uniqueDays = scheduleItems.stream()
        .map(ScheduleView::getDayOfWeek)
        .collect(Collectors.toSet());
    lblDaysTeaching.setText(String.valueOf(uniqueDays.size()));

    double totalHours = scheduleItems.stream()
        .mapToDouble(s -> {
          try {
            String[] times = s.getTimeRange().split(" - ");
            LocalTime start = LocalTime.parse(times[0], TIME_FORMATTER);
            LocalTime end = LocalTime.parse(times[1], TIME_FORMATTER);
            return (end.toSecondOfDay() - start.toSecondOfDay()) / 3600.0;
          } catch (Exception e) {
            return 0.0;
          }
        }).sum();
    lblTotalHours.setText(String.format("%.1f", totalHours));
  }

  private void updateStatus(String message, String type) {
    Platform.runLater(() -> {
      statusLabel.setText("Status: " + message);
      statusLabel.getStyleClass().removeAll("status-success", "status-error", "status-info");
      statusLabel.getStyleClass().add("status-" + type);
    });
  }

  public static class ScheduleView {
    private final String dayOfWeek;
    private final String timeRange;
    private final String courseName;
    private final String section;
    private final String room;
    private final int enrolledCount;

    public ScheduleView(String dayOfWeek, String timeRange, String courseName,
              String section, String room, int enrolledCount) {
      this.dayOfWeek = dayOfWeek;
      this.timeRange = timeRange;
      this.courseName = courseName;
      this.section = section;
      this.room = room;
      this.enrolledCount = enrolledCount;
    }

    public String getDayOfWeek() { return dayOfWeek; }
    public String getTimeRange() { return timeRange; }
    public String getCourseName() { return courseName; }
    public String getSection() { return section; }
    public String getRoom() { return room; }
    public int getEnrolledCount() { return enrolledCount; }
  }
}