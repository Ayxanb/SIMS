package com.khazar.sims.ui.system_admin;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.util.List;

import com.khazar.sims.core.Session;
import com.khazar.sims.ui.SceneTransition;
import com.khazar.sims.ui.UIManager;

/**
 * Controller class for the main System Admin Dashboard layout.
 * Manages sidebar navigation and loading of child FXML views into the content area.
 */
public class SystemAdminController {
  /* UI FXML Elements */
  @FXML private StackPane contentArea;
  @FXML private Button btnCourses;
  @FXML private Button btnEnrollmentsClicked;
  @FXML private Button btnUsers;

  /* List of navigation buttons for easy style management */
  private List<Button> navButtons;

  /**
   * Initializes the controller. This method is called automatically after the 
   * FXML file has been loaded.
   */
  @FXML
  public void initialize() {
    /* Initialize the list of navigation buttons */
    navButtons = List.of(btnCourses, btnEnrollmentsClicked, btnUsers);
    /* Load the default view (Faculties) on initialization */
    handleFacultiesClicked();
  }

  /**
   * Helper method to manage the active state of the sidebar buttons.
   * This ensures only one button has the 'active-nav' style class at a time,
   * maintaining a clear user state.
   */
  private void setActiveButton(Button activeBtn) {
    /* 1. Remove active state from all buttons */
    navButtons.forEach(btn -> btn.getStyleClass().remove("active-nav"));
    /* 2. Add active state to the selected button */
    activeBtn.getStyleClass().add("active-nav");
  }

  /**
   * Handles the navigation to the Faculties management screen.
   */
  @FXML
  private void handleFacultiesClicked() {
    setActiveButton(btnCourses);
    /* Load the faculties FXML into the content area with no transition */
    UIManager.setView(contentArea, "/ui/system_admin/faculties.fxml", SceneTransition.Type.NONE, 0.0);
  }

  /**
   * Handles the navigation to the Departments management screen.
   */
  @FXML
  private void handleDepartmentsClicked() {
    setActiveButton(btnEnrollmentsClicked);
    /* Load the departments FXML into the content area with no transition */
    UIManager.setView(contentArea, "/ui/system_admin/departments.fxml", SceneTransition.Type.NONE, 0.0);
  }

  /**
   * Handles the navigation to the User Management screen.
   */
  @FXML
  private void handleUsersClicked() {
    setActiveButton(btnUsers);
    /* Load the user management FXML into the content area with no transition */
    UIManager.setView(contentArea, "/ui/system_admin/user_management.fxml", SceneTransition.Type.NONE, 0.0);
  }

  @FXML
  private void handleLogout() {
    Session.logout();
  }
}