package com.khazar.sims.controller;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import com.khazar.sims.core.Session;
import com.khazar.sims.core.data.User;
import com.khazar.sims.core.data.User.Role;
import com.khazar.sims.util.SceneTransition;

/**
 * AccountController handles the login/logout interface and user authentication logic.
 * It supports auto-login attempts after typing pauses, input validation,
 * and smooth transition to role-specific panels upon successful login.
 */
public class AccountController {

  /* ---------- FXML UI Components ---------- */

  @FXML private TextField idField;
  @FXML private PasswordField passwordField;
  @FXML private Label errorLabel;

  /* ---------- Internal State ---------- */

  private final PauseTransition typingPause = new PauseTransition(Duration.seconds(1.3));
  private boolean autoChecking = false;

  /* ---------- Initialization ---------- */

  /**
   * Called automatically after the FXML file is loaded.
   * Sets up reactive listeners for input fields and initializes UI states.
   */
  @FXML
  private void initialize() {
    errorLabel.setVisible(false);

    /* Restart auto-checking timer when user types */
    idField.textProperty().addListener((obs, o, n) -> restartAutoCheck());
    passwordField.textProperty().addListener((obs, o, n) -> restartAutoCheck());

    /* Trigger auto-login after pause in typing */
    typingPause.setOnFinished(e -> {
      if (!autoChecking) {
        autoChecking = true;
        tryAutoLogin();
        autoChecking = false;
      }
    });
  }

  /* ---------- Auto Login Mechanism ---------- */

  /**
   * Restarts the auto-checking timer to delay automatic login attempts.
   * Called whenever the user types in ID or password fields.
   */
  private void restartAutoCheck() {
    typingPause.stop();
    typingPause.playFromStart();
  }

  /**
   * Attempts to authenticate the user using the input ID or email and password.
   * Performs input validation and provides appropriate feedback.
   */
  private void tryAutoLogin() {
    String idOrEmail = idField.getText().trim();
    String password = passwordField.getText().trim();

    /* Hide error label if fields are empty */
    if (idOrEmail.isEmpty() || password.isEmpty()) {
      errorLabel.setVisible(false);
      return;
    }

    boolean isNumeric = idOrEmail.matches("\\d+");
    boolean isEmail = idOrEmail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /* Invalid input format (not an ID or email) */
    if (!isNumeric && !isEmail) {
      showError("Not a valid ID or email!", false);
      return;
    }

    /* Attempt login based on identifier type */
    User user = isEmail
      ? Session.loginByEmail(idOrEmail, password)
      : Session.loginById(Integer.parseInt(idOrEmail), password);

    if (user == null) {
      /* Invalid credentials */
      showError("Invalid ID/Email or password!", false);
      return;
    }

    /* Login successful */
    showError("✅ Login successful!", true);

    /* Slight delay for feedback before transitioning */
    PauseTransition pause = new PauseTransition(Duration.seconds(0.3));
    pause.setOnFinished(event -> loginUser(user));
    pause.play();
  }

  /* ---------- User Session Handling ---------- */

  /**
   * Handles user session creation and loads the appropriate UI
   * based on the user's role (Admin, Teacher, Student).
   *
   * @param user The authenticated user instance
   */
  private void loginUser(User user) {
    Session.setActiveUser(user);

    String fxmlPath;

    /* Determine the UI to load based on user role */
    switch (user.getRole()) {
      case User.Role.ADMIN -> fxmlPath = "/fxml/admin_panel.fxml";
      case User.Role.TEACHER -> fxmlPath = "/fxml/teacher_panel.fxml";
      case User.Role.STUDENT -> fxmlPath = "/fxml/student_panel.fxml";
      default -> {
        /* Handle unknown or undefined role */
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Authentication Error");
        alert.setHeaderText("User.Role Not Recognized");
        alert.setContentText("""
          The account exists, but no known role is attached.
          The system couldn't identify your role, so it won't let you in.
          Please contact your department to solve this problem.
        """);
        alert.showAndWait();
        return;
      }
    }

    /* Transition to the corresponding scene */
    Session.getRootController().loadScene(
      fxmlPath,
      SceneTransition.Type.FADE, 100.0,
      SceneTransition.Type.FADE, 300.0
    );
  }

  /* ---------- Error Label Helpers ---------- */

  /**
   * Displays an error or success message in the error label.
   * Applies different visual styles based on the success flag.
   *
   * @param message The message to display
   * @param success Whether the message indicates success
   */
  private void showError(String message, boolean success) {
    errorLabel.setText(message);
    errorLabel.setStyle(
      success
        ? "-fx-background-color: rgba(0, 255, 0, 0.1); -fx-border-color: lime; -fx-text-fill: green;"
        : "-fx-background-color: transparent; -fx-text-fill: red;"
    );
    errorLabel.setVisible(true);

    /* Fade out automatically only for success messages */
    if (success) {
      FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), errorLabel);
      fadeOut.setFromValue(1);
      fadeOut.setToValue(0);
      fadeOut.setDelay(Duration.seconds(1));
      fadeOut.setOnFinished(e -> errorLabel.setVisible(false));
      fadeOut.play();
    }
  }

  public static boolean isValidPassword(String password) {
    if (password == null || password.length() < 8)
      return false;
    boolean hasUpperCase = password.matches(".*[A-Z].*");
    boolean hasLowerCase = password.matches(".*[a-z].*");
    boolean hasDigit = password.matches(".*\\d.*");
    boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?].*");
    return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;
  }
}
