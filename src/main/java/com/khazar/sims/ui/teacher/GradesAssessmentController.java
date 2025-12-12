package com.khazar.sims.ui.teacher;

import java.sql.SQLException;
import java.util.List;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.Course;
import com.khazar.sims.database.data.CourseOffering;
import com.khazar.sims.database.data.Grade;
import com.khazar.sims.database.data.User;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class GradesAssessmentController {

  @FXML private ComboBox<CourseOption> cmbCourse;
  @FXML private TableView<Grade> gradesTable;

  @FXML private TableColumn<Grade, Integer> colStudentId;
  @FXML private TableColumn<Grade, String> colStudentName;
  @FXML private TableColumn<Grade, String> colAssessment;
  @FXML private TableColumn<Grade, Integer> colScore;
  @FXML private TableColumn<Grade, Integer> colMaxScore;
  @FXML private TableColumn<Grade, String> colDate;

  @FXML private Label statusLabel;

  private final ObservableList<Grade> gradeEntries = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    setupTable();
    loadCoursesAsync();
  }

  private void setupTable() {
    colStudentId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
    colAssessment.setCellValueFactory(new PropertyValueFactory<>("assessmentName"));
    colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
    colMaxScore.setCellValueFactory(new PropertyValueFactory<>("maxScore"));
    colDate.setCellValueFactory(new PropertyValueFactory<>("dateSubmitted"));

    colStudentName.setCellValueFactory(cell -> {
      int studentId = cell.getValue().getStudentId();
      return new javafx.beans.property.SimpleStringProperty(getStudentName(studentId));
    });

    gradesTable.setItems(gradeEntries);
    gradesTable.setEditable(false);
    gradesTable.setPlaceholder(new Label("Select a course to view exam results"));
  }

  private String getStudentName(int studentId) {
    try {
      User u = Session.getUsersTable().getById(studentId);
      return u.getFirstName() + " " + u.getLastName();
    }
    catch (SQLException e) {
      return "Unknown";
    }
  }

  private void loadCoursesAsync() {
    updateStatus("Loading courses...", "success");

    Task<List<CourseOption>> task = new Task<>() {
      @Override
      protected List<CourseOption> call() throws Exception {
        int teacherId = Session.getActiveUser().getId();
        List<CourseOffering> offerings = Session.getCourseOfferingTable().getByTeacherId(teacherId);
        return offerings.stream().map(offering -> {
          try {
            Course course = Session.getCourseTable().getById(offering.getCourseId());
            return new CourseOption(offering.getId(), course.getCode() + " - " + course.getName());
          } catch (SQLException e) {
            return new CourseOption(offering.getId(), "Unknown");
          }
        }).toList();
      }
    };

    task.setOnSucceeded(e -> {
      List<CourseOption> courses = task.getValue();
      cmbCourse.getItems().setAll(courses);
      if (!courses.isEmpty()) {
        cmbCourse.getSelectionModel().selectFirst();
        loadGradesAsync();
      }
      cmbCourse.setOnAction(evt -> loadGradesAsync());
      updateStatus("Courses loaded", "success");
    });

    task.setOnFailed(e -> {
      updateStatus("Failed to load courses: " + task.getException().getMessage(), "error");
    });

    new Thread(task).start();
  }

  private void loadGradesAsync() {
    CourseOption selected = cmbCourse.getValue();
    if (selected == null) return;

    updateStatus("Loading grades...", "success");

    Task<List<Grade>> task = new Task<>() {
      @Override
      protected List<Grade> call() throws Exception {
        return Session.getGradeTable().getByOfferingId(selected.offeringId);
      }
    };

    task.setOnSucceeded(e -> {
      gradeEntries.setAll(task.getValue());
      updateStatus("Loaded " + task.getValue().size() + " exam records", "success");
    });

    task.setOnFailed(e -> {
      updateStatus("Failed to load assessments: " + task.getException().getMessage(), "error");
    });

    new Thread(task).start();
  }

  private void updateStatus(String message, String type) {
    Platform.runLater(() -> {
      statusLabel.setText("Status: " + message);
      statusLabel.getStyleClass().removeAll("status-success", "status-error");
      statusLabel.getStyleClass().add("status-" + type);
    });
  }

  public static class CourseOption {
    private final int offeringId;
    private final String display;

    public CourseOption(int offeringId, String display) {
      this.offeringId = offeringId;
      this.display = display;
    }

    @Override
    public String toString() {
      return display;
    }
  }
}
