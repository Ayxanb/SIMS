package com.khazar.sims.ui.teacher;

import java.sql.SQLException;
import java.util.List;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.Course;
import com.khazar.sims.database.data.CourseOffering;
import com.khazar.sims.database.data.Grade;
import com.khazar.sims.database.data.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

  @FXML private Label lblTotalStudents;
  @FXML private Label statusLabel;

  private final ObservableList<Grade> gradeEntries = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    setupTable();
    loadCourses();
  }

  private void setupTable() {
    colStudentId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
    colAssessment.setCellValueFactory(new PropertyValueFactory<>("assessmentName"));
    colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
    colMaxScore.setCellValueFactory(new PropertyValueFactory<>("maxScore"));
    colDate.setCellValueFactory(new PropertyValueFactory<>("dateSubmitted"));

    colStudentName.setCellValueFactory(cell -> {
      try {
        int studentId = cell.getValue().getStudentId();
        User u = Session.getUsersTable().getById(studentId);
        return new javafx.beans.property.SimpleStringProperty(
          u.getFirstName() + " " + u.getLastName()
        );
      }
      catch (SQLException e) {
        return new javafx.beans.property.SimpleStringProperty("Unknown");
      }
    });

    gradesTable.setItems(gradeEntries);
    gradesTable.setEditable(false);
    gradesTable.setPlaceholder(new Label("Select a course to view exam results"));
  }

  private void loadCourses() {
    try {
      int teacherId = Session.getActiveUser().getId();
      cmbCourse.getItems().clear();

      List<CourseOffering> offerings =
          Session.getCourseOfferingTable().getByTeacherId(teacherId);

      for (CourseOffering offering : offerings) {
        Course course = Session.getCourseTable().getById(offering.getCourseId());
        String display = course.getCode() + " - " + course.getName();
        cmbCourse.getItems().add(new CourseOption(offering.getId(), display));
      }

      if (!cmbCourse.getItems().isEmpty()) {
        cmbCourse.getSelectionModel().selectFirst();
        loadGrades();
      }

      cmbCourse.setOnAction(e -> loadGrades());
      updateStatus("Courses loaded", "success");

    }
    catch (SQLException e) {
      updateStatus("Failed to load courses: " + e.getMessage(), "error");
      System.err.println(e);
    }
  }

  private void loadGrades() {
    try {
      CourseOption selected = cmbCourse.getValue();
      if (selected == null) return;

      gradeEntries.clear();

      List<Grade> assessments =
          Session.getGradeTable().getByOfferingId(selected.offeringId);

      gradeEntries.addAll(assessments);

      lblTotalStudents.setText(String.valueOf(assessments.size()));
      updateStatus("Loaded " + assessments.size() + " exam records", "success");

    }
    catch (SQLException e) {
      updateStatus("Failed to load assessments: " + e.getMessage(), "error");
      System.err.println(e);
    }
  }

  private void updateStatus(String message, String type) {
    statusLabel.setText("Status: " + message);
    statusLabel.getStyleClass().removeAll("status-success", "status-error");
    statusLabel.getStyleClass().add("status-" + type);
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
