package com.khazar.sims.ui.teacher;

import java.util.List;

import com.khazar.sims.core.Session;
import com.khazar.sims.ui.SceneTransition;
import com.khazar.sims.ui.UIManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

/**
 * Main Controller for Teacher Dashboard
 * Manages navigation between different teacher functionalities
 * 
 * Teacher Capabilities:
 * 1. View assigned course offerings
 * 2. View enrolled students
 * 3. Manage attendance
 * 4. View exam results (read-only)
 * 5. View final assessments (read-only)
 * 6. View schedules
 * 
 */
public class TeacherController {
  @FXML private StackPane contentArea;
  
  /* Navigation Buttons */
  @FXML private Button btnCourses;
  @FXML private Button btnStudents;
  @FXML private Button btnAttendance;
  @FXML private Button btnSchedule;
  @FXML private Button btnGrades;
  
  /**
   * Initializes the teacher dashboard
   * Sets up navigation buttons and loads default view
   */
  @FXML
  public void initialize() {    
    /* Load default view - Courses */
    handleCoursesClicked();
  }
  
  /**
   * Sets the active navigation button style
   * Removes active class from all buttons and adds to the specified button
   * 
   * @param activeBtn The button to mark as active
   */
  private void setActiveButton(Button activeBtn) {
    List.of(
      btnCourses, 
      btnStudents, 
      btnAttendance, 
      btnSchedule,
      btnGrades
    ).forEach(btn -> btn.getStyleClass().remove("active-nav"));
    activeBtn.getStyleClass().add("active-nav");
  }
  
  /**
   * Navigates to Courses view
   * Shows teacher's assigned course offerings
   */
  @FXML
  private void handleCoursesClicked() {
    setActiveButton(btnCourses);
    UIManager.setView(
      contentArea, 
      "/ui/teacher/courses.fxml", 
      SceneTransition.Type.NONE, 
      0.0
    );
  }
  
  /**
   * Navigates to Students view
   * Shows enrolled students in teacher's courses
   */
  @FXML
  private void handleStudentsClicked() {
    setActiveButton(btnStudents);
    UIManager.setView(
      contentArea, 
      "/ui/teacher/students.fxml", 
      SceneTransition.Type.NONE, 
      0.0
    );
  }
  
  /**
   * Navigates to Attendance view
   * Allows teacher to manage student attendance
   */
  @FXML
  private void handleAttendanceClicked() {
    setActiveButton(btnAttendance);
    UIManager.setView(
      contentArea, 
      "/ui/teacher/attendance.fxml", 
      SceneTransition.Type.NONE, 
      0.0
    );
  }
    
  /**
   * Navigates to Schedule view
   * Shows teacher's teaching schedule
   */
  @FXML
  private void handleScheduleClicked() {
    setActiveButton(btnSchedule);
    UIManager.setView(
      contentArea, 
      "/ui/teacher/schedule.fxml", 
      SceneTransition.Type.NONE, 
      0.0
    );
  }

  @FXML
  private void handleGradesAssessmentsClicked() {
    setActiveButton(btnGrades);
    UIManager.setView(
      contentArea, 
      "/ui/teacher/grades_assessment.fxml", 
      SceneTransition.Type.NONE, 
      0.0
    );
  }

  /**
   * Handles logout action
   * Logs out the current teacher and returns to login screen
   */
  @FXML
  private void handleLogout() {
    Session.logout();
  }
}