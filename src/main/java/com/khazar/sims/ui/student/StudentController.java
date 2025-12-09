package com.khazar.sims.ui.student;

import java.util.List;

import com.khazar.sims.core.Session;
import com.khazar.sims.ui.SceneTransition;
import com.khazar.sims.ui.UIManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class StudentController {
  @FXML private StackPane contentArea;
  
  /* Navigation Buttons */
  @FXML private Button btnSchedule;
  @FXML private Button btnGrades;
  @FXML private Button btnTranscript;
  @FXML private Button btnAttendance;

  @FXML
  public void initialize() {
    handleSchedule();
  }

  private void setActiveButton(Button activeBtn) {
    List.of(
      btnSchedule,
      btnGrades,
      btnTranscript,
      btnAttendance
    ).forEach(btn -> btn.getStyleClass().remove("active-nav"));
    activeBtn.getStyleClass().add("active-nav");
  }

  @FXML
  private void handleSchedule() {
    setActiveButton(btnSchedule);
    UIManager.setView(
      contentArea,
      "/ui/student/schedule.fxml",
      SceneTransition.Type.NONE,
      0.0
    );
  }

  @FXML
  private void handleGrades() {
    setActiveButton(btnGrades);
    UIManager.setView(
      contentArea,
      "/ui/student/grades.fxml",
      SceneTransition.Type.NONE,
      0.0
    );
  }

  @FXML
  private void handleTranscript() {
    setActiveButton(btnTranscript);
    UIManager.setView(
      contentArea,
      "/ui/student/transcript.fxml",
      SceneTransition.Type.NONE,
      0.0
    );
  }

  @FXML
  private void handleAttendance() {
    setActiveButton(btnAttendance);
    UIManager.setView(
      contentArea,
      "/ui/student/attendance.fxml",
      SceneTransition.Type.NONE,
      0.0
    );
  }

  @FXML
  private void handleLogout() {
    Session.logout();
  }
}