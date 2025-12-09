package com.khazar.sims.ui.login;

import java.sql.SQLException;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.User;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class LoginController {
  /* ---------------- Constants ---------------- */
  private static final int MIN_PASSWORD_SIZE = 5;
  private static final double AUTO_LOGIN_DELAY_SECONDS = 1.2;
  private static final double SUCCESS_FADE_DELAY_SECONDS = 1.0;

  private static final String EMAIL_REGEX =
    "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$";

  /* Style constants for the message label. These will be merged with the CSS definitions.*/
  private static final String MSG_BASE_STYLE = 
    "-fx-padding: 10px 15px; -fx-border-width: 1px;";
  private static final String MSG_ERROR_STYLE =
    "-fx-background-color: rgba(239, 68, 68, 0.1); -fx-border-color: #ef4444; -fx-text-fill: #ef4444;"; // Red tint
  private static final String MSG_SUCCESS_STYLE =
    "-fx-background-color: rgba(34, 197, 94, 0.1); -fx-border-color: #22c55e; -fx-text-fill: #22c55e;"; // Green tint

  /* ---------------- FXML ---------------- */
  @FXML private Label messageLabel;
  @FXML private TextField idOrEmailField;
  @FXML private PasswordField passwordField;

  /* ---------------- State ---------------- */
  private final PauseTransition typingPause = new PauseTransition(Duration.seconds(AUTO_LOGIN_DELAY_SECONDS));
  private boolean isProcessing = false;

  @FXML
  private void initialize() {
    messageLabel.setVisible(false);
    idOrEmailField.textProperty().addListener((a, b, c) -> triggerAutoLogin());
    passwordField.textProperty().addListener((a, b, c) -> triggerAutoLogin());
    
    passwordField.setOnAction(e -> tryLogin());

    typingPause.setOnFinished(e -> tryLogin());
  }

  private void triggerAutoLogin() {
    if (!idOrEmailField.getText().isEmpty() && !passwordField.getText().isEmpty())
      typingPause.playFromStart();
  }

  /* ---------------- Login Flow ---------------- */
  @FXML
  private void tryLogin() {
    if (isProcessing)
        return;

    isProcessing = true;
    typingPause.stop(); /* Stop autologin timer if user clicks the button */

    String input = idOrEmailField.getText().trim();
    String pass = passwordField.getText();
    
    /* Input validation checks */
    if (input.isEmpty() || pass.isEmpty()) {
      showMessage("ID/Email and Password are required.", false);
      isProcessing = false;
      return;
    }

    boolean email = input.matches(EMAIL_REGEX);
    boolean numeric = input.matches("\\d+");

    if (!email && !numeric) {
      showMessage("Enter a valid ID (numeric) or email address.", false);
      isProcessing = false;
      return;
    }

    if (pass.length() < MIN_PASSWORD_SIZE) {
      showMessage("Password must be at least " + MIN_PASSWORD_SIZE + " characters long.", false);
      isProcessing = false;
      return;
    }
    
    /* Disable controls while authenticating */
    setControlsDisabled(true); 
    authenticate(input, pass, email);
  }

  /* ---------------- Authentication ---------------- */
  private void authenticate(String input, String pass, boolean isEmail) {
    User user;

    try {
      user = isEmail
        ? Session.getUsersTable().getByEmail(input)
        : Session.getUsersTable().getById(Integer.parseInt(input));
    }
    catch (SQLException e) {
      showMessage("Database error: " + e, false);
      setControlsDisabled(false);
      isProcessing = false;
      return;
    }
    catch (NumberFormatException e) {
      /* Should ideally be caught by regex check, but safe fallback */
      showMessage("Invalid user ID format.", false);
      setControlsDisabled(false);
      isProcessing = false;
      return;
    }

    if (user == null) {
      showMessage(isEmail ? "Email not found." : "Invalid user ID.", false);
      setControlsDisabled(false);
      isProcessing = false;
      return;
    }

    if (!user.getPassword().equals(pass)) {
      showMessage("Incorrect password.", false);
      setControlsDisabled(false);
      isProcessing = false;
      return;
    }
    
    /* Successful login */
    showMessage("✅ Login successful! Redirecting...", true);
    PauseTransition delay = new PauseTransition(Duration.seconds(SUCCESS_FADE_DELAY_SECONDS));
    delay.setOnFinished(e -> Session.navigateUser(user));
    delay.play();
  }

  /* ---------------- UI Helpers ---------------- */
  private void setControlsDisabled(boolean disabled) {
    idOrEmailField.setDisable(disabled);
    passwordField.setDisable(disabled);
  }

  private void showMessage(String msg, boolean success) {
    Platform.runLater(() -> {
      messageLabel.setText(msg);
      messageLabel.setStyle(MSG_BASE_STYLE + (success ? MSG_SUCCESS_STYLE : MSG_ERROR_STYLE));
      messageLabel.setVisible(true);
      messageLabel.setOpacity(1);

      if (success) {
        /* Fade the label out after a delay (but before navigation) */
        FadeTransition ft = new FadeTransition(Duration.seconds(0.5), messageLabel);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setDelay(Duration.seconds(SUCCESS_FADE_DELAY_SECONDS - 0.5)); // Start fade just before navigation
        ft.setOnFinished(e -> messageLabel.setVisible(false));
        ft.play();
      }
      else {
        /* Non-success messages stay visible until the next trigger */
        FadeTransition ft = new FadeTransition(Duration.seconds(0.5), messageLabel);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setDelay(Duration.seconds(4.0)); // Error messages fade out after 4 seconds
        ft.setOnFinished(e -> messageLabel.setVisible(false));
        ft.play();
      }
    });
  }
}