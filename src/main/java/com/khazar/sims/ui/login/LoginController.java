package com.khazar.sims.ui.login;

import java.sql.SQLException;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.User;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class LoginController {
  private static final int MIN_PASSWORD_SIZE = 5;
  private static final double AUTO_LOGIN_DELAY_SECONDS = 1.2;
  private static final double MESSAGE_DISPLAY_DURATION = 4.0; /* How long error messages stay visible */

  private static final String EMAIL_REGEX =
    "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$";

  /* Style constants for the message label. These will be merged with the CSS definitions.*/
  private static final String MSG_BASE_STYLE = 
    "-fx-padding: 10px 15px; -fx-border-width: 1px;";
  private static final String MSG_ERROR_STYLE =
    "-fx-background-color: rgba(239, 68, 68, 0.1); -fx-border-color: #ef4444; -fx-text-fill: #ef4444;";
  private static final String MSG_SUCCESS_STYLE =
    "-fx-background-color: rgba(34, 197, 94, 0.1); -fx-border-color: #22c55e; -fx-text-fill: #22c55e;";

  @FXML private Label messageLabel;
  @FXML private TextField idOrEmailField;
  @FXML private PasswordField passwordField;

  /* Delay before triggering login after the user stops typing */
  private final PauseTransition typingPause = new PauseTransition(Duration.seconds(AUTO_LOGIN_DELAY_SECONDS));
  private boolean isProcessing = false;

  @FXML
  private void initialize() {
    messageLabel.setVisible(false);
    
    /* Listener to trigger auto-login when fields are populated and user pauses typing */
    idOrEmailField.textProperty().addListener((a, b, c) -> triggerAutoLogin());
    passwordField.textProperty().addListener((a, b, c) -> triggerAutoLogin());
    
    /* Focus and direct login on ENTER key press */
    idOrEmailField.setOnAction(e -> passwordField.requestFocus());
    passwordField.setOnAction(e -> tryLogin());
    
    /* Action to perform when typing pause finishes */
    typingPause.setOnFinished(e -> tryLogin());
  }

  /* Plays the pause timer when both fields have content */
  private void triggerAutoLogin() {
    if (!idOrEmailField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
      typingPause.playFromStart();
    } else {
      typingPause.stop();
    }
  }

  /* ---------------- Login Flow ---------------- */
  @FXML
  private void tryLogin() {
    if (isProcessing) return;
    
    String input = idOrEmailField.getText().trim();
    String pass = passwordField.getText();

    /* Validation checks */
    if (input.isEmpty() || pass.isEmpty()) {
      showMessage("ID/Email and Password are required.", false);
      return;
    }
    
    boolean isEmail = input.matches(EMAIL_REGEX);
    boolean isNumeric = input.matches("\\d+");
    
    if (!isEmail && !isNumeric) {
      showMessage("Enter a valid ID or email address.", false);
      return;
    }
    if (pass.length() < MIN_PASSWORD_SIZE) {
      showMessage("Password must be at least " + MIN_PASSWORD_SIZE + " characters.", false);
      return;
    }
    
    /* Start the asynchronous authentication process */
    authenticateAsync(input, pass, isEmail);
  }

  /* ---------------- Asynchronous Authentication (The Fix) ---------------- */
  private void authenticateAsync(String input, String pass, boolean isEmail) {
    isProcessing = true;
    setControlsDisabled(true);
    showMessage("Authenticating...", true); /* Use success style for waiting message */

    Task<User> authTask = new Task<>() {
      @Override
      protected User call() throws SQLException {
        /* This runs in a background thread */
        return isEmail 
          ? Session.getUsersTable().getByEmail(input) 
          : Session.getUsersTable().getById(Integer.parseInt(input));
      }
    };

    authTask.setOnSucceeded(e -> {
      isProcessing = false;
      User user = authTask.getValue();

      if (user == null) {
        showMessage(isEmail ? "Email not found." : "Invalid user ID.", false);
        setControlsDisabled(false);
        return;
      }

      if (!user.getPassword().equals(pass)) {
        showMessage("Incorrect password.", false);
        setControlsDisabled(false);
        return;
      }
      
      /* Login successful, perform final updates and navigate */
      try {
        Session.getUsersTable().updateLastLogin(user.getId());
      }
      catch (SQLException ex) {
        System.err.println("Failed to update last_login: " + ex.getMessage());
      }

      showMessage("✅ Login successful! Redirecting...", true);
      /* Navigation will happen after the fade transition completes */
      Session.navigateUser(user); 
    });

    authTask.setOnFailed(e -> {
      isProcessing = false;
      setControlsDisabled(false);
      showMessage("Database Error: " + authTask.getException().getMessage(), false);
    });
    
    /* Start the Task in a new thread */
    new Thread(authTask).start();
  }

  /* ---------------- UI Helpers ---------------- */
  private void setControlsDisabled(boolean disabled) {
    idOrEmailField.setDisable(disabled);
    passwordField.setDisable(disabled);
  }

  private void showMessage(String msg, boolean success) {
    Platform.runLater(() -> {
      messageLabel.setText(msg);
      
      /* Set the style based on success/error type */
      messageLabel.setStyle(MSG_BASE_STYLE + (success ? MSG_SUCCESS_STYLE : MSG_ERROR_STYLE));
      messageLabel.setVisible(true);
      messageLabel.setOpacity(1);

      /* Always set up a fade transition to hide the message after a duration */
      FadeTransition ft = new FadeTransition(Duration.seconds(1.0), messageLabel);
      ft.setFromValue(1);
      ft.setToValue(0);
      
      if (success) {
        /* For successful login, delay the fade until after navigation starts */
        ft.setDelay(Duration.seconds(MESSAGE_DISPLAY_DURATION - 3.0));
      }
      else {
        /* For errors or informational messages, fade out after a fixed delay */
        ft.setDelay(Duration.seconds(MESSAGE_DISPLAY_DURATION));
        ft.setOnFinished(e -> messageLabel.setVisible(false));
      }
      ft.play();
    });
  }
}