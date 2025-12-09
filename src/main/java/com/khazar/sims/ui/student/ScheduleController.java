package com.khazar.sims.ui.student;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.Course;
import com.khazar.sims.database.data.CourseOffering;
import com.khazar.sims.database.data.Enrollment;
import com.khazar.sims.database.data.Schedule;
import com.khazar.sims.database.data.Student;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ScheduleController {

  @FXML private TableView<ScheduleView> scheduleTable;
  @FXML private TableColumn<ScheduleView, String> colDay;
  @FXML private TableColumn<ScheduleView, String> colTime;
  @FXML private TableColumn<ScheduleView, String> colCourse;
  @FXML private TableColumn<ScheduleView, String> colRoom;
  @FXML private Label statusLabel;

  private final ObservableList<ScheduleView> scheduleItems = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    setupTable();
    loadSchedule();
  }

  private void setupTable() {
    colDay.setCellValueFactory(new PropertyValueFactory<>("dayOfWeek"));
    colTime.setCellValueFactory(new PropertyValueFactory<>("timeRange"));
    colCourse.setCellValueFactory(new PropertyValueFactory<>("courseName"));
    colRoom.setCellValueFactory(new PropertyValueFactory<>("room"));

    colDay.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(String day, boolean empty) {
        super.updateItem(day, empty);

        getStyleClass().removeAll(
          "day-mon","day-tue","day-wed","day-thu",
          "day-fri","day-sat","day-sun"
        );

        if (empty || day == null) {
          setText(null);
        } else {
          setText(day);
          getStyleClass().add("day-" + day.toLowerCase());
        }
      }
    });

    scheduleTable.setItems(scheduleItems);
    scheduleTable.setPlaceholder(new Label("No classes scheduled"));
  }

  private void loadSchedule() {
    updateStatus("Loading schedule...", "info");

    Task<List<ScheduleView>> task = new Task<>() {
      @Override
      protected List<ScheduleView> call() throws Exception {
        return loadScheduleData();
      }
    };

    task.setOnSucceeded(e -> {
      scheduleItems.setAll(task.getValue());
      updateStatus("Schedule loaded - " + scheduleItems.size() + " classes", "success");
    });

    task.setOnFailed(e -> {
      updateStatus("Error loading schedule: " + task.getException().getMessage(), "error");
      System.err.println(e);
    });

    Thread t = new Thread(task);
    t.setDaemon(true);
    t.start();
  }

  private List<ScheduleView> loadScheduleData() throws SQLException {
    ObservableList<ScheduleView> result = FXCollections.observableArrayList();

    int userId = Session.getActiveUser().getId();
    Student student = Session.getStudentTable().getByUserId(userId);

    List<Enrollment> enrollments =
      Session.getEnrollmentTable().getByStudentId(student.getUserId());

    for (Enrollment enrollment : enrollments) {
      CourseOffering offering =
        Session.getCourseOfferingTable().getById(enrollment.getCourseOfferingId());

      Course course =
        Session.getCourseTable().getById(offering.getCourseId());

      List<Schedule> schedules =
        Session.getScheduleTable().getSchedulesForOffering(offering.getId());

      for (Schedule schedule : schedules) {
        result.add(new ScheduleView(
          schedule.getDayOfWeek(),
          formatTime(schedule.getStartTime(), schedule.getEndTime()),
          course.getCode() + " - " + course.getName(),
          schedule.getRoom()
        ));
      }
    }

    result.sort((s1, s2) -> {
      int dayCompare = getDayOrder(s1.getDayOfWeek()) - getDayOrder(s2.getDayOfWeek());
      return (dayCompare != 0) ? dayCompare : s1.getTimeRange().compareTo(s2.getTimeRange());
    });

    return result;
  }

  private String formatTime(LocalTime start, LocalTime end) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    return start.format(formatter) + " - " + end.format(formatter);
  }

  private int getDayOrder(String day) {
    return switch (day.toUpperCase()) {
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

  private void updateStatus(String message, String type) {
    if (statusLabel != null) {
      Platform.runLater(() -> {
        statusLabel.setText("Status: " + message);
        statusLabel.getStyleClass().removeAll(
          "status-success","status-error","status-info"
        );
        statusLabel.getStyleClass().add("status-" + type);
      });
    }
  }

  public static class ScheduleView {
    private final String dayOfWeek;
    private final String timeRange;
    private final String courseName;
    private final String room;

    public ScheduleView(String dayOfWeek, String timeRange, String courseName, String room) {
      this.dayOfWeek = dayOfWeek;
      this.timeRange = timeRange;
      this.courseName = courseName;
      this.room = room;
    }

    public String getDayOfWeek() { return dayOfWeek; }
    public String getTimeRange() { return timeRange; }
    public String getCourseName() { return courseName; }
    public String getRoom() { return room; }
  }
}
