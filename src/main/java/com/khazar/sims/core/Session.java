package com.khazar.sims.core;

import java.sql.Connection;
import java.sql.SQLException;

import com.khazar.sims.controller.RootController;
import com.khazar.sims.core.data.User;
import com.khazar.sims.database.Database;
import com.khazar.sims.database.table.UserTable;

import javafx.scene.control.Alert;

/**
 * Session class manages the application's global state.
 * It handles database connection, active user, and the root controller.
 */
public class Session {

  /* ---------- Application State ---------- */

  /* Connection to the database */
  private static Connection databaseConnection;

  /* Currently authenticated user */
  private static User activeUser = null;

  /* RootController reference for global scene management */
  private static RootController rootController;

  /* ---------- Getters & Setters ---------- */

  /**
   * Returns the database connection.
   *
   * @return active database connection
   */
  public static Connection getDatabaseConnection() {
    return databaseConnection;
  }

  /**
   * Returns the currently active user.
   *
   * @return active User instance or null if not logged in
   */
  public static User getActiveUser() {
    return activeUser;
  }

  /**
   * Sets the currently active user.
   *
   * @param user the authenticated User instance
   */
  public static void setActiveUser(User user) {
    activeUser = user;
  }

  /**
   * Returns the application's root controller.
   *
   * @return RootController instance
   */
  public static RootController getRootController() {
    return rootController;
  }

  /**
   * Sets the application's root controller.
   *
   * @param controller the RootController instance
   */
  public static void setRootController(RootController controller) {
    rootController = controller;
  }

  /* ---------- Initialization ---------- */

  /**
   * Initializes the application session by connecting to the database.
   * Displays an error alert if the connection fails.
   */
  public static boolean start() {
    try {
      databaseConnection = Database.init();
      return true;
    }
    catch (SQLException e) {
      Alert alert = new Alert(Alert.AlertType.ERROR,
          "Failed to connect to the database: " + e.getMessage());
      alert.setTitle("Database Connection Error");
      alert.setHeaderText("Could not establish a connection to the database.");
      alert.setContentText(e.getMessage());
      alert.showAndWait();
      return false;
    }
  }

  /* ---------- Authentication ---------- */

  /**
   * Attempts to log in a user by their ID and password.
   *
   * @param id       User ID
   * @param password Plain-text password
   * @return User if authentication is successful, null otherwise
   */
  public static User loginById(int id, String password) {
    try {
      User user = UserTable.getById(id);
      /* Compare password hashes */
      return user != null && user.getPasswordHash() == password.hashCode() ? user : null;
    }
    catch (SQLException e) {
      Alert alert = new Alert(Alert.AlertType.ERROR,
          "Failed to login by ID: " + e.getMessage());
      alert.setTitle("Login Error");
      alert.setHeaderText("Could not login to the account using ID, try email instead.");
      alert.setContentText(e.getMessage());
      alert.showAndWait();
      return null;
    }
  }

  /**
   * Attempts to log in a user by their email and password.
   *
   * @param email    User email
   * @param password Plain-text password
   * @return User if authentication is successful, null otherwise
   */
  public static User loginByEmail(String email, String password) {
    try {
      User user = UserTable.getByEmail(email);
      /* Compare password hashes */
      return user != null && user.getPasswordHash() == password.hashCode() ? user : null;
    }
    catch (SQLException e) {
      Alert alert = new Alert(Alert.AlertType.ERROR,
          "Failed to login by email: " + e.getMessage());
      alert.setTitle("Login Error");
      alert.setHeaderText("Could not login to the account using email, try ID instead.");
      alert.setContentText(e.getMessage());
      alert.showAndWait();
      return null;
    }
  }
}