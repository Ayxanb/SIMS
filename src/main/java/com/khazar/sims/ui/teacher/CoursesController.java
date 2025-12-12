package com.khazar.sims.ui.teacher;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.Course;
import com.khazar.sims.database.data.CourseOffering;
import com.khazar.sims.database.data.Semester;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Teacher's Course Offerings View
 * Displays all course offerings assigned to the logged-in teacher
 * Uses JavaFX Task for asynchronous loading.
 */
public class CoursesController {
  @FXML private TableView<CourseOfferingView> courseTable;
  @FXML private TableColumn<CourseOfferingView, String> colCourseCode;
  @FXML private TableColumn<CourseOfferingView, String> colCourseName;
  @FXML private TableColumn<CourseOfferingView, String> colSection;
  @FXML private TableColumn<CourseOfferingView, String> colSemester;
  @FXML private TableColumn<CourseOfferingView, Integer> colCredits;
  @FXML private TableColumn<CourseOfferingView, Integer> colEnrolled;
  @FXML private TableColumn<CourseOfferingView, Integer> colCapacity;

  @FXML private Label lblTotalCourses;
  @FXML private Label lblTotalStudents;
  @FXML private Label lblActiveOfferings;
  @FXML private Label statusLabel;

  private final ObservableList<CourseOfferingView> offerings = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    setupTable();
    loadCourses();
  }

  private void setupTable() {
    colCourseCode.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
    colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
    colSemester.setCellValueFactory(new PropertyValueFactory<>("semesterName"));
    colCredits.setCellValueFactory(new PropertyValueFactory<>("credits"));
    colEnrolled.setCellValueFactory(new PropertyValueFactory<>("enrolledCount"));
    colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));

    courseTable.setItems(offerings);
    courseTable.setPlaceholder(new Label("No courses assigned"));
  }
  
  /**
   * Initiates the asynchronous loading of all course offerings for the active teacher.
   */
  private void loadCourses() {
    Task<List<CourseOfferingView>> courseLoadTask = createCourseLoadTask();
    
    updateStatus("Loading assigned course offerings...", "info");
    courseTable.setPlaceholder(new Label("Loading courses..."));
    
    courseLoadTask.setOnSucceeded(e -> {
      List<CourseOfferingView> loadedOfferings = courseLoadTask.getValue();
      offerings.setAll(loadedOfferings);
      updateStatistics();
      updateStatus("Loaded " + offerings.size() + " course offerings", "success");
      courseTable.setPlaceholder(new Label("No courses assigned"));
    });

    courseLoadTask.setOnFailed(e -> {
      Throwable exception = e.getSource().getException();
      updateStatus("Failed to load courses: " + exception.getMessage(), "error");
      System.err.println("Course loading task failed: " + exception);
      courseTable.setPlaceholder(new Label("Failed to load courses. See logs for details."));
    });

    new Thread(courseLoadTask).start();
  }

  /**
   * Creates the background task to fetch all necessary data from the database.
   * @return A Task that returns a List of CourseOfferingView objects.
   */
  private Task<List<CourseOfferingView>> createCourseLoadTask() {
    return new Task<>() {
      @Override
      protected List<CourseOfferingView> call() throws SQLException {
        int teacherId = Session.getActiveUser().getId();
        List<CourseOfferingView> courseViews = new ArrayList<>();
        
        List<CourseOffering> teacherOfferings = 
          Session.getCourseOfferingTable().getByTeacherId(teacherId);

        for (CourseOffering offering : teacherOfferings) {
          Course course = Session.getCourseTable().getById(offering.getCourseId());
          Semester semester = Session.getSemesterTable().getById(offering.getSemesterId());
          
          int enrolledCount = 
            Session.getEnrollmentTable().getByOfferingId(offering.getId()).size();

          courseViews.add(new CourseOfferingView(
            course.getCode(),
            course.getName(),
            offering.getSection(),
            semester.getName(),
            course.getCredits(),
            enrolledCount,
            offering.getCapacity()
          ));
        }
        return courseViews;
      }
    };
  }

  private void updateStatistics() {
    int totalCourses = offerings.size();
    int totalStudents = offerings.stream().mapToInt(CourseOfferingView::getEnrolledCount).sum();

    lblTotalCourses.setText(String.valueOf(totalCourses));
    lblTotalStudents.setText(String.valueOf(totalStudents));
    lblActiveOfferings.setText(String.valueOf(totalCourses));
  }

  private void updateStatus(String message, String type) {
    if (statusLabel == null) return;

    statusLabel.setText("Status: " + message);
    statusLabel.getStyleClass().removeAll("status-success", "status-error", "status-info");
    statusLabel.getStyleClass().add("status-" + type);
  }

  public static class CourseOfferingView {
    private final String courseCode;
    private final String courseName;
    private final String section;
    private final String semesterName;
    private final int credits;
    private final int enrolledCount;
    private final int capacity;

    public CourseOfferingView(String courseCode, String courseName, String section,
                  String semesterName, int credits, int enrolledCount, int capacity) {
      this.courseCode = courseCode;
      this.courseName = courseName;
      this.section = section;
      this.semesterName = semesterName;
      this.credits = credits;
      this.enrolledCount = enrolledCount;
      this.capacity = capacity;
    }

    public String getCourseCode() { return courseCode; }
    public String getCourseName() { return courseName; }
    public String getSection() { return section; }
    public String getSemesterName() { return semesterName; }
    public int getCredits() { return credits; }
    public int getEnrolledCount() { return enrolledCount; }
    public int getCapacity() { return capacity; }
  }
}