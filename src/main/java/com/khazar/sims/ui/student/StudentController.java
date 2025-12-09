package com.khazar.sims.ui.student;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.util.List;

import com.khazar.sims.core.Session;
import com.khazar.sims.ui.SceneTransition;
import com.khazar.sims.ui.UIManager;

public class StudentController {
  @FXML private StackPane contentArea;
  
  @FXML private Button btnCourses;
  @FXML private Button btnEnrollments;
  @FXML private Button btnAttendance;

  private List<Button> navButtons;

  @FXML
  public void initialize() {
    navButtons = List.of(btnCourses, btnEnrollments, btnAttendance);
    handleA();
  }

  private void setActiveButton(Button activeBtn) {
    navButtons.forEach(btn -> btn.getStyleClass().remove("active-nav"));
    activeBtn.getStyleClass().add("active-nav");
  }

  @FXML
  private void handleA() {
    setActiveButton(btnCourses);
    UIManager.setView(contentArea, "/ui/student/a.fxml", SceneTransition.Type.NONE, 0.0);
  }

  @FXML
  private void handleB() {
    setActiveButton(btnEnrollments);
    UIManager.setView(contentArea, "/ui/student/b.fxml", SceneTransition.Type.NONE, 0.0);
  }

  @FXML
  private void handleC() {
    setActiveButton(btnAttendance);
    UIManager.setView(contentArea, "/ui/student/c.fxml", SceneTransition.Type.NONE, 0.0);
  }

  @FXML
  private void handleLogout() {
    Session.logout();
  }
}