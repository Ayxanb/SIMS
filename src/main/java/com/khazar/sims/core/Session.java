package com.khazar.sims.core;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import com.khazar.sims.Main;
import com.khazar.sims.database.Database;
import com.khazar.sims.database.data.User;
import com.khazar.sims.database.table.AttendanceTable;
import com.khazar.sims.database.table.CourseOfferingTable;
import com.khazar.sims.database.table.CoursesTable;
import com.khazar.sims.database.table.DepartmentTable;
import com.khazar.sims.database.table.EnrollmentTable;
import com.khazar.sims.database.table.FacultyTable;
import com.khazar.sims.database.table.GradeTable;
import com.khazar.sims.database.table.ScheduleTable;
import com.khazar.sims.database.table.SemesterTable;
import com.khazar.sims.database.table.StudentTable;
import com.khazar.sims.database.table.TeacherTable;
import com.khazar.sims.database.table.UserTable;
import com.khazar.sims.ui.SceneTransition;
import com.khazar.sims.ui.UIManager;
import com.khazar.sims.ui.root.RootController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Session class manages the application's global state.
 * It handles database connection, active user, and the root controller.
 */
public class Session {
  private static User activeUser; /* the current logged-in user. */
  private static RootController rootController;
  private static Connection connection;

  /* ---------- Getters & Setters ---------- */
  public static User getActiveUser() { return activeUser; }
  public static Connection getDatabaseConnection() throws SQLException { return connection; }

  /* Database tables as objects */
  static private UserTable userTable;
  static private GradeTable gradeTable;
  static private CoursesTable courseTable;
  static private StudentTable studentTable;
  static private TeacherTable teachersTable;
  static private FacultyTable facultiesTable;
  static private SemesterTable semesterTable;
  static private ScheduleTable scheduleTable;
  static private EnrollmentTable enrollmentTable;
  static private AttendanceTable attendanceTable;
  static private DepartmentTable departmentsTable;
  static private CourseOfferingTable courseOfferingTable;

  /* Getters */
  static public UserTable getUsersTable() { return userTable; } 
  static public GradeTable getGradeTable() { return gradeTable; }
  static public CoursesTable getCourseTable() { return courseTable; } 
  static public StudentTable getStudentTable() { return studentTable; } 
  static public TeacherTable getTeacherTable() { return teachersTable; } 
  static public FacultyTable getFacultyTable() { return facultiesTable; } 
  static public SemesterTable getSemesterTable() { return semesterTable; }
  static public ScheduleTable getScheduleTable() { return scheduleTable; }
  static public EnrollmentTable getEnrollmentTable() { return enrollmentTable; }
  static public AttendanceTable getAttendanceTable() { return attendanceTable; }
  static public DepartmentTable getDepartmentTable() { return departmentsTable; } 
  static public CourseOfferingTable getCourseOfferingTable() { return courseOfferingTable; } 

  /* ---------- Initialization ---------- */
  public static void start(Stage primaryStage) throws IOException, SQLException {
    userTable = new UserTable();
    gradeTable = new GradeTable();
    courseTable = new CoursesTable();
    studentTable = new StudentTable();
    teachersTable = new TeacherTable();
    facultiesTable = new FacultyTable();
    semesterTable = new SemesterTable();
    scheduleTable = new ScheduleTable();
    enrollmentTable = new EnrollmentTable();
    attendanceTable = new AttendanceTable();
    departmentsTable = new DepartmentTable();
    courseOfferingTable = new CourseOfferingTable();

    connection = Database.init();  /* connect to database */

    /* Load root (Window) */
    FXMLLoader loader = new FXMLLoader(Main.class.getResource("/ui/root/root.fxml"));
    Parent root = loader.load();
    rootController = loader.getController();
    Scene scene = new Scene(root);
    
    /* load login view */
    UIManager.setView(rootController.getContentArea(), "/ui/login/login.fxml", SceneTransition.Type.NONE, 0.0);

    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void logout() {
    activeUser = null;
    UIManager.setView(rootController.getContentArea(), "/ui/login/login.fxml", SceneTransition.Type.FADE, 150.0);
  }

  public static void navigateUser(User user) throws RuntimeException {
    activeUser = user;
    String fxmlPath = switch (activeUser.getRole()) {
      case "SYSTEM_ADMIN"   ->  "/ui/system_admin/system_admin.fxml";
      case "FACULTY_ADMIN"  ->  "/ui/system_admin/faculty_admin.fxml";
      case "TEACHER"        ->  "/ui/teacher/teacher.fxml";
      case "STUDENT"        ->  "/ui/student/student.fxml";
      default               ->  throw new RuntimeException("No fxml path doesn't exist for such user.");
    };
    UIManager.setView(rootController.getContentArea(), fxmlPath, SceneTransition.Type.FADE, 500.0);
  }

}
