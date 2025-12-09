package com.khazar.sims.database.table;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursesTable {
  /* ---------- Add a new course ---------- */
  public void add(Course course) throws SQLException {
    final String sql = "INSERT INTO courses(code, name, credits, department_id) VALUES (?, ?, ?, ?)";
    try (PreparedStatement statement = Session.getDatabaseConnection()
        .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, course.getCode());
      statement.setString(2, course.getName());
      statement.setInt(3, course.getCredits());
      statement.setInt(4, course.getDepartmentId());
      statement.executeUpdate();

      try (ResultSet rs = statement.getGeneratedKeys()) {
        if (rs.next()) course.setId(rs.getInt(1));
      }
    }
  }

  /* ---------- Retrieve a course by ID ---------- */
  public Course getById(int id) throws SQLException {
    final String sql = "SELECT id, code, name, credits, department_id FROM courses WHERE id = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setInt(1, id);
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          return new Course(
              rs.getInt("id"),
              rs.getString("code"),
              rs.getString("name"),
              rs.getInt("credits"),
              rs.getInt("department_id")
          );
        }
      }
    }
    return null;
  }

  /* ---------- Retrieve all courses ---------- */
  public List<Course> getAll() throws SQLException {
    List<Course> courses = new ArrayList<>();
    final String sql = "SELECT id, code, name, credits, department_id FROM courses";
    try (Statement statement = Session.getDatabaseConnection().createStatement();
       ResultSet rs = statement.executeQuery(sql)) {

      while (rs.next()) {
        courses.add(new Course(
            rs.getInt("id"),
            rs.getString("code"),
            rs.getString("name"),
            rs.getInt("credits"),
            rs.getInt("department_id")
        ));
      }
    }
    return courses;
  }

  /* ---------- Retrieve courses for a specific department ---------- */
  public List<Course> getCoursesForDepartment(int departmentId) throws SQLException {
    List<Course> courses = new ArrayList<>();
    final String sql = "SELECT id, code, name, credits, department_id FROM courses WHERE department_id = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setInt(1, departmentId);
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          courses.add(new Course(
              rs.getInt("id"),
              rs.getString("code"),
              rs.getString("name"),
              rs.getInt("credits"),
              rs.getInt("department_id")
          ));
        }
      }
    }
    return courses;
  }

  /* ---------- Retrieve courses assigned to a specific teacher ---------- */
  public List<Course> getCoursesForTeacher(int teacherId) throws SQLException {
    List<Course> courses = new ArrayList<>();
    final String sql = """
        SELECT c.id, c.code, c.name, c.credits, c.department_id
        FROM courses c
        JOIN course_offerings co ON c.id = co.course_id
        WHERE co.teacher_id = ?
        """;
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setInt(1, teacherId);
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          courses.add(new Course(
              rs.getInt("id"),
              rs.getString("code"),
              rs.getString("name"),
              rs.getInt("credits"),
              rs.getInt("department_id")
          ));
        }
      }
    }
    return courses;
  }

  /* ---------- Update an existing course ---------- */
  public void update(Course course) throws SQLException {
    final String sql = "UPDATE courses SET code = ?, name = ?, credits = ?, department_id = ? WHERE id = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setString(1, course.getCode());
      statement.setString(2, course.getName());
      statement.setInt(3, course.getCredits());
      statement.setInt(4, course.getDepartmentId());
      statement.setInt(5, course.getId());
      statement.executeUpdate();
    }
  }

  /* ---------- Delete a course by ID ---------- */
  public void delete(int id) throws SQLException {
    final String sql = "DELETE FROM courses WHERE id = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setInt(1, id);
      statement.executeUpdate();
    }
  }
}
