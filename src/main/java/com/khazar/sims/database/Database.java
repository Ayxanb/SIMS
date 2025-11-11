package com.khazar.sims.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Handles initialization of the Khazar University database schema.
 * 
 * Loads database credentials from an external configuration file (db.properties)
 * instead of hardcoding them. This prevents sensitive data from being pushed
 * to source control.
 */
public class Database {

  private static final Properties config = new Properties();

  static {
    try (FileInputStream fis = new FileInputStream("src/main/resources/db.properties")) {
      config.load(fis);
    }
    catch (IOException e) {
      throw new RuntimeException("❌ Failed to load database configuration file (db.properties).", e);
    }
  }

  /**
   * Initializes the database connection and ensures all required tables exist.
   *
   * @return the active SQL connection to be reused by Session
   * @throws SQLException if a database access error occurs
   */
  public static Connection init() throws SQLException {
    String host = config.getProperty("db.host");
    String port = config.getProperty("db.port");
    String name = config.getProperty("db.name");
    String user = config.getProperty("db.user");
    String pass = config.getProperty("db.pass");

    String url = String.format("jdbc:mysql://%s:%s/%s", host, port, name);

    Connection connection = DriverManager.getConnection(url, user, pass);

    try (Statement statement = connection.createStatement()) {
      /* ---------------- USERS ---------------- */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS users (
          id INT PRIMARY KEY AUTO_INCREMENT,
          role ENUM('ADMIN','TEACHER','STUDENT') NOT NULL,
          first_name VARCHAR(50) NOT NULL,
          last_name VARCHAR(50) NOT NULL,
          email VARCHAR(100) UNIQUE NOT NULL,
          password_hash INT NOT NULL,
          phone VARCHAR(20),
          address VARCHAR(255),
          date_of_birth DATE,
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
      """);

      /* ---------------- DEPARTMENTS ---------------- */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS departments (
          id INT PRIMARY KEY AUTO_INCREMENT,
          name VARCHAR(100) NOT NULL,
          head_id INT,
          FOREIGN KEY (head_id) REFERENCES users(id)
        );
      """);

      /* ---------------- COURSES ---------------- */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS courses (
          id INT PRIMARY KEY AUTO_INCREMENT,
          code VARCHAR(20) UNIQUE NOT NULL,
          name VARCHAR(100) NOT NULL,
          department_id INT,
          FOREIGN KEY (department_id) REFERENCES departments(id)
        );
      """);

      /* ---------------- COURSE_TEACHERS ---------------- */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS course_teachers (
          course_id INT,
          teacher_id INT,
          PRIMARY KEY(course_id, teacher_id),
          FOREIGN KEY(course_id) REFERENCES courses(id),
          FOREIGN KEY(teacher_id) REFERENCES users(id)
        );
      """);

      /* ---------------- ENROLLMENTS ---------------- */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS enrollments (
          id INT PRIMARY KEY AUTO_INCREMENT,
          student_id INT NOT NULL,
          course_id INT NOT NULL,
          grade DOUBLE,
          FOREIGN KEY(student_id) REFERENCES users(id),
          FOREIGN KEY(course_id) REFERENCES courses(id)
        );
      """);

      /* ---------------- SCHEDULE ---------------- */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS schedule (
          id INT PRIMARY KEY AUTO_INCREMENT,
          course_id INT NOT NULL,
          day_of_week ENUM('MON','TUE','WED','THU','FRI','SAT','SUN') NOT NULL,
          start_time TIME NOT NULL,
          end_time TIME NOT NULL,
          room VARCHAR(50),
          FOREIGN KEY(course_id) REFERENCES courses(id)
        );
      """);

      /* ---------------- EXAMS ---------------- */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS exams (
          id INT PRIMARY KEY AUTO_INCREMENT,
          course_id INT NOT NULL,
          name VARCHAR(100) NOT NULL,
          max_score DOUBLE NOT NULL,
          exam_date DATE,
          FOREIGN KEY(course_id) REFERENCES courses(id)
        );
      """);

      /* ---------------- EXAM_RESULTS ---------------- */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS exam_results (
          id INT PRIMARY KEY AUTO_INCREMENT,
          exam_id INT NOT NULL,
          student_id INT NOT NULL,
          score DOUBLE,
          FOREIGN KEY(exam_id) REFERENCES exams(id),
          FOREIGN KEY(student_id) REFERENCES users(id)
        );
      """);

      /* ---------------- ATTENDANCE ---------------- */
      statement.execute("""
        CREATE TABLE IF NOT EXISTS attendance (
          student_id INT NOT NULL,
          course_id INT NOT NULL,
          date DATE NOT NULL,
          present BOOLEAN NOT NULL,
          PRIMARY KEY(student_id, course_id, date),
          FOREIGN KEY(student_id) REFERENCES users(id),
          FOREIGN KEY(course_id) REFERENCES courses(id)
        );
      """);
    }

    return connection;
  }
}
