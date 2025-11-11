package com.khazar.sims.database.table;

import com.khazar.sims.core.Session;
import com.khazar.sims.core.data.Course;

import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.List;
import java.util.ArrayList;

/**
 * CourseTable provides static methods to interact with the `courses` table
 * and related join tables (course_teachers) in the database.
 * Supports CRUD operations and retrieving courses for departments and teachers.
 */
public class CourseTable {

  /* ---------- Add a new course ---------- */
  public static void add(Course course) throws SQLException {
    final String sql = "INSERT INTO courses(code, name, department_id) VALUES (?, ?, ?)";
    try (PreparedStatement statement = Session.getDatabaseConnection()
        .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, course.getCode());
      statement.setString(2, course.getName());
      statement.setInt(3, course.getDepartmentId());
      statement.executeUpdate();

      try (ResultSet rs = statement.getGeneratedKeys()) {
        if (rs.next()) course.setId(rs.getInt(1));
      }
    }
  }

  /* ---------- Retrieve a course by ID ---------- */
  public static Course getById(int id) throws SQLException {
    final String sql = "SELECT id, code, name, department_id FROM courses WHERE id = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setInt(1, id);
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          return new Course(
              rs.getInt("id"),
              rs.getString("code"),
              rs.getString("name"),
              rs.getInt("department_id")
          );
        }
      }
    }
    return null;
  }

  /* ---------- Retrieve all courses ---------- */
  public static List<Course> getAll() throws SQLException {
    List<Course> courses = new ArrayList<>();
    final String sql = "SELECT id, code, name, department_id FROM courses";
    try (Statement statement = Session.getDatabaseConnection().createStatement();
         ResultSet rs = statement.executeQuery(sql)) {
      while (rs.next()) {
        courses.add(new Course(
            rs.getInt("id"),
            rs.getString("code"),
            rs.getString("name"),
            rs.getInt("department_id")
        ));
      }
    }
    return courses;
  }

  /* ---------- Retrieve courses for a specific department ---------- */
  public static List<Course> getCoursesForDepartment(int departmentId) throws SQLException {
    List<Course> courses = new ArrayList<>();
    final String sql = "SELECT id, code, name, department_id FROM courses WHERE department_id = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setInt(1, departmentId);
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          courses.add(new Course(
              rs.getInt("id"),
              rs.getString("code"),
              rs.getString("name"),
              rs.getInt("department_id")
          ));
        }
      }
    }
    return courses;
  }

  /* ---------- Retrieve courses assigned to a specific teacher ---------- */
  public static List<Course> getCoursesForTeacher(int teacherId) throws SQLException {
    List<Course> courses = new ArrayList<>();
    final String sql = """
        SELECT c.id, c.code, c.name, c.department_id
        FROM courses c
        JOIN course_teachers ct ON c.id = ct.course_id
        WHERE ct.teacher_id = ?
        """;
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setInt(1, teacherId);
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          courses.add(new Course(
              rs.getInt("id"),
              rs.getString("code"),
              rs.getString("name"),
              rs.getInt("department_id")
          ));
        }
      }
    }
    return courses;
  }

  /* ---------- Retrieve teacher IDs assigned to a specific course ---------- */
  public static List<Integer> getTeachersForCourse(int courseId) throws SQLException {
    List<Integer> teacherIds = new ArrayList<>();
    final String sql = "SELECT teacher_id FROM course_teachers WHERE course_id = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setInt(1, courseId);
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          teacherIds.add(rs.getInt("teacher_id"));
        }
      }
    }
    return teacherIds;
  }

  /* ---------- Update an existing course ---------- */
  public static void update(Course course) throws SQLException {
    final String sql = "UPDATE courses SET code = ?, name = ?, department_id = ? WHERE id = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setString(1, course.getCode());
      statement.setString(2, course.getName());
      statement.setInt(3, course.getDepartmentId());
      statement.setInt(4, course.getId());
      statement.executeUpdate();
    }
  }

  /* ---------- Delete a course by ID ---------- */
  public static void delete(int id) throws SQLException {
    final String sql = "DELETE FROM courses WHERE id = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setInt(1, id);
      statement.executeUpdate();
    }
  }
}
