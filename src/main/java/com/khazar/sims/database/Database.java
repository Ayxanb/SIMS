package com.khazar.sims.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class Database {

  private static final Properties config = new Properties();

  public static Connection connect() throws SQLException {
    final String host = config.getProperty("db.host");
    final String port = config.getProperty("db.port");
    final String name = config.getProperty("db.name");
    final String user = config.getProperty("db.user");
    final String pass = config.getProperty("db.pass");
    return DriverManager.getConnection(
      String.format("jdbc:mysql://%s:%s/%s",host, port, name), user, pass
    );
  }

  /**
   * Initializes the database connection and ensures all required tables exist.
   *
   * @return the active SQL connection to be reused by Session
   * @throws SQLException if a database access error occurs
   */
  public static Connection init() throws SQLException {
    Connection connection;
    try (FileInputStream fis = new FileInputStream("src/main/resources/db.properties")) {
      config.load(fis);
      connection = connect();
    }
    catch (IOException | SQLException e) {
      // throw new RuntimeException("❌ Failed to load database configuration file (db.properties).", e);
      return null;
    }

    /* TODO: REMOVE THIS IN THE RELEASE!!!! */
    try (Statement statement = connection.createStatement()) {
      /* USERS */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS users (
          id INT PRIMARY KEY AUTO_INCREMENT,
          role ENUM('SYSTEM_ADMIN','FACULTY_ADMIN','TEACHER','STUDENT') NOT NULL,
          first_name VARCHAR(100) NOT NULL,
          last_name VARCHAR(100) NOT NULL,
          email VARCHAR(255) UNIQUE NOT NULL,
          password VARCHAR(255) NOT NULL,
          date_of_birth DATE,
          is_active BOOLEAN NOT NULL
        );
      """);

      /* FACULTIES */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS faculties (
          id INT PRIMARY KEY AUTO_INCREMENT,
          name VARCHAR(150) UNIQUE NOT NULL,
          code VARCHAR(20) UNIQUE NOT NULL
        );
      """);

      /* DEPARTMENTS(child of faculty) */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS departments (
          id INT PRIMARY KEY AUTO_INCREMENT,
          faculty_id INT NOT NULL,
          name VARCHAR(150) NOT NULL,
          code VARCHAR(20) NOT NULL,
          UNIQUE(faculty_id, name),
          UNIQUE(faculty_id, code),
          FOREIGN KEY (faculty_id) REFERENCES faculties(id) ON DELETE CASCADE
        );        
      """);

      /* FACULTY ADMINS */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS faculty_admins (
          user_id INT PRIMARY KEY,
          faculty_id INT NOT NULL,
          FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
          FOREIGN KEY (faculty_id) REFERENCES faculties(id) ON DELETE CASCADE
        );        
      """);

      /* TEACHERS */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS teachers (
          user_id INT PRIMARY KEY,
          department_id INT NOT NULL,
          FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
          FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
        );
      """);

      /* STUDENTS */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS students (
          user_id INT PRIMARY KEY,
          department_id INT NOT NULL,
          enrollment_year YEAR NOT NULL,
          gpa DECIMAL(3,2),
          FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
          FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
        );
      """);

      /* PROGRAMS - Programs belong to departments. */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS programs (
          id INT PRIMARY KEY AUTO_INCREMENT,
          department_id INT NOT NULL,
          name VARCHAR(150) NOT NULL,
          code VARCHAR(20) UNIQUE NOT NULL,
          FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
        );
      """);

      /* COURSES */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS courses (
          id INT PRIMARY KEY AUTO_INCREMENT,
          code VARCHAR(50) UNIQUE NOT NULL,
          name VARCHAR(255) NOT NULL,
          credits INT NOT NULL,
          department_id INT NOT NULL,
          FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
        );
      """);

      /* SEMESTERS */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS semesters (
          id INT PRIMARY KEY AUTO_INCREMENT,
          name VARCHAR(50) UNIQUE NOT NULL,
          start_date DATE,
          end_date DATE
        );
      """);

      /* COURSE OFFERINGS - Course taught by one teacher in a specific semester. */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS course_offerings (
          id INT PRIMARY KEY AUTO_INCREMENT,
          course_id INT NOT NULL,
          teacher_id INT NULL,
          semester_id INT NOT NULL,
          section VARCHAR(10) DEFAULT '1',
          capacity INT DEFAULT 100,
          UNIQUE(course_id, semester_id, section),
          FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
          FOREIGN KEY (teacher_id) REFERENCES teachers(user_id) ON DELETE SET NULL,
          FOREIGN KEY (semester_id) REFERENCES semesters(id) ON DELETE CASCADE
        );
      """);

      /* ENROLLMENTS - Links students → course offerings */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS enrollments (
          offering_id INT NOT NULL,
          student_id INT NOT NULL,
          final_grade DECIMAL(5,2),
          UNIQUE(offering_id, student_id),
          FOREIGN KEY (offering_id) REFERENCES course_offerings(id) ON DELETE CASCADE,
          FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE
        );
      """);

      /* SCHEDULES */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS schedules (
          id INT PRIMARY KEY AUTO_INCREMENT,
          offering_id INT NOT NULL,
          day_of_week ENUM('MON','TUE','WED','THU','FRI','SAT','SUN') NOT NULL,
          start_time TIME NOT NULL,
          end_time TIME NOT NULL,
          room VARCHAR(50),
          FOREIGN KEY (offering_id) REFERENCES course_offerings(id) ON DELETE CASCADE
        );
      """);

      /* ATTENDANCE */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS attendances (
          session_id INT NOT NULL,
          student_id INT NOT NULL,
          present BOOLEAN NOT NULL,
          PRIMARY KEY (session_id, student_id),
          FOREIGN KEY (session_id) REFERENCES schedules(id) ON DELETE CASCADE,
          FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE
        );
      """);

      /* EXAMS */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS exams (
          offering_id INT NOT NULL,
          exam_date DATE NOT NULL,
          max_score DECIMAL(5,2) NOT NULL,
          PRIMARY KEY (offering_id, exam_date),
          FOREIGN KEY (offering_id) REFERENCES course_offerings(id) ON DELETE CASCADE
        );
      """);

      /* EXAM RESULTS */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS exam_results (
          offering_id INT NOT NULL,
          student_id INT NOT NULL,
          exam_date DATE NOT NULL,
          score DECIMAL(5,2),
          PRIMARY KEY (offering_id, exam_date, student_id),
          FOREIGN KEY (offering_id, exam_date) REFERENCES exams(offering_id, exam_date) ON DELETE CASCADE,
          FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE
        );
      """);

      /* GRADES */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS assessments (
          id INT PRIMARY KEY AUTO_INCREMENT,
          offering_id INT NOT NULL,
          student_id INT NOT NULL,
          assessment_name VARCHAR(255) NOT NULL,
          score INT NOT NULL,
          max_score INT NOT NULL,
          date_submitted DATETIME NOT NULL,
          UNIQUE(offering_id, student_id, assessment_name),
          FOREIGN KEY (offering_id) REFERENCES course_offerings(id) ON DELETE CASCADE,
          FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE
        );
      """);
    }
    catch (Exception e) {
      System.out.println(e);
    }

    return connection;
  }
}
