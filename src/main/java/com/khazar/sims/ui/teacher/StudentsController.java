package com.khazar.sims.ui.teacher;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.Course;
import com.khazar.sims.database.data.CourseOffering;
import com.khazar.sims.database.data.Department;
import com.khazar.sims.database.data.Enrollment;
import com.khazar.sims.database.data.Student;
import com.khazar.sims.database.data.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller for Viewing Enrolled Students
 */
public class StudentsController {

  @FXML private ComboBox<CourseOption> cmbCourse;
  @FXML private TextField txtSearch;
  @FXML private TableView<StudentView> studentTable;
  @FXML private TableColumn<StudentView, String> colStudentId;
  @FXML private TableColumn<StudentView, String> colFirstName;
  @FXML private TableColumn<StudentView, String> colLastName;
  @FXML private TableColumn<StudentView, String> colEmail;
  @FXML private TableColumn<StudentView, String> colDepartment;

  @FXML private Label lblTotalStudents;
  @FXML private Label statusLabel;

  private final ObservableList<StudentView> allStudents = FXCollections.observableArrayList();
  private final ObservableList<StudentView> filteredStudents = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    configureTable();
    configureFilters();
    loadCourses();
  }

  private void configureTable() {
    colStudentId.setCellValueFactory(new PropertyValueFactory<>("userId"));
    colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
    colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
    colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
    colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));

    studentTable.setItems(filteredStudents);
    studentTable.setPlaceholder(new Label("Select a course to view students"));
  }

  private void configureFilters() {
    txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    cmbCourse.setOnAction(e -> loadStudents());
  }

  private void loadCourses() {
    Task<List<CourseOption>> courseLoadTask = createCourseLoadTask();
    
    updateStatus("Loading assigned courses...", "info");
    
    courseLoadTask.setOnSucceeded(e -> {
      List<CourseOption> options = courseLoadTask.getValue();
      cmbCourse.getItems().setAll(options);

      if (!options.isEmpty()) {
        cmbCourse.getSelectionModel().selectFirst();
        loadStudents();
      }
      else {
        updateStatus("No assigned courses found", "info");
      }
    });

    courseLoadTask.setOnFailed(e -> {
      updateStatus("Error loading courses: " + e.getSource().getException().getMessage(), "error");
      System.err.println("Course load failed: " + e.getSource().getException().getMessage());
    });

    new Thread(courseLoadTask).start();
  }

  private Task<List<CourseOption>> createCourseLoadTask() {
    return new Task<>() {
      @Override
      protected List<CourseOption> call() throws SQLException {
        int teacherId = Session.getActiveUser().getId();
        List<CourseOffering> offerings = Session.getCourseOfferingTable().getByTeacherId(teacherId);

        return offerings.stream()
          .map(offering -> {
            try {
              Course course = Session.getCourseTable().getById(offering.getCourseId());
              String display = course.getCode() + " - " + course.getName();
              return new CourseOption(offering.getId(), display);
            }
            catch (SQLException e) {
              throw new RuntimeException("Failed to fetch course details: " + e.getMessage(), e);
            }
          })
          .collect(Collectors.toList());
      }
    };
  }

  private void loadStudents() {
    CourseOption selected = cmbCourse.getValue();
    if (selected == null) {
      allStudents.clear();
      applyFilters();
      return;
    }

    Task<List<StudentView>> studentLoadTask = createStudentLoadTask(selected.offeringId);
    
    updateStatus("Loading students for course offering " + selected.offeringId + "...", "info");

    studentLoadTask.setOnSucceeded(e -> {
      allStudents.setAll(studentLoadTask.getValue());
      applyFilters();
      updateStatus("Loaded " + allStudents.size() + " students", "success");
    });

    studentLoadTask.setOnFailed(e -> {
      updateStatus("Error loading students: " + e.getSource().getException().getMessage(), "error");
      System.err.println("Student load failed: " + e);
    });

    new Thread(studentLoadTask).start();
  }

  private Task<List<StudentView>> createStudentLoadTask(int offeringId) {
    return new Task<>() {
      @Override
      protected List<StudentView> call() throws SQLException {
        List<StudentView> studentViews = FXCollections.observableArrayList();
        
        List<Enrollment> enrollments = Session.getEnrollmentTable().getByOfferingId(offeringId);

        for (Enrollment enrollment : enrollments) {
          Student student = Session.getStudentTable().getByUserId(enrollment.getStudentId());
          User user = Session.getUsersTable().getById(student.getUserId());
          Department dept = Session.getDepartmentTable().getById(student.getDepartmentId());

          studentViews.add(new StudentView(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            dept.getName()
          ));
        }
        return studentViews;
      }
    };
  }

  private void applyFilters() {
    String search = txtSearch.getText() == null
      ? ""
      : txtSearch.getText().toLowerCase().trim();

    if (search.isEmpty()) {
      filteredStudents.setAll(allStudents);
    }
    else {
      filteredStudents.setAll(
        allStudents.stream().filter(s ->
          String.valueOf(s.getUserId()).contains(search) ||
          s.getFirstName().toLowerCase().contains(search) ||
          s.getLastName().toLowerCase().contains(search) ||
          s.getEmail().toLowerCase().contains(search) ||
          s.getDepartment().toLowerCase().contains(search)
        ).toList()
      );
    }

    updateStatistics();
  }

  private void updateStatistics() {
    lblTotalStudents.setText(String.valueOf(filteredStudents.size()));
  }

  private void updateStatus(String message, String type) {
    if (statusLabel == null) return;

    statusLabel.setText("Status: " + message);
    statusLabel.getStyleClass().removeAll(
      "status-success", "status-error", "status-info"
    );
    statusLabel.getStyleClass().add("status-" + type);
  }

  public static class StudentView {
    private final int userId;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String department;

    public StudentView(int userId, String firstName, String lastName,
              String email, String department) {
      this.userId = userId;
      this.firstName = firstName;
      this.lastName = lastName;
      this.email = email;
      this.department = department;
    }

    public int getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getDepartment() { return department; }
  }

  public static class CourseOption {
    private final int offeringId;
    private final String display;

    public CourseOption(int offeringId, String display) {
      this.offeringId = offeringId;
      this.display = display;
    }
    
    public int getCourseOfferingId() { return offeringId; }

    @Override
    public String toString() {
      return display;
    }
  }
}