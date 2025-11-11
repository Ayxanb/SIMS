package com.khazar.sims.database.table;

import com.khazar.sims.core.Session;
import com.khazar.sims.core.data.Enrollment;

import java.sql.Types;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.List;
import java.util.ArrayList;

/**
 * EnrollmentTable provides static methods to interact with the `enrollments` table
 * in the database. Supports adding, retrieving, and listing enrollments.
 */
public class EnrollmentTable {

  /**
   * Adds a new enrollment to the database.
   *
   * @param enrollment the Enrollment object to add
   * @throws SQLException if a database access error occurs
   */
  public static void add(Enrollment enrollment) throws SQLException {
    final String sql = "INSERT INTO enrollments(student_id, course_id, grade) VALUES (?, ?, ?)";
    try (PreparedStatement statement = Session.getDatabaseConnection()
            .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setInt(1, enrollment.getStudentId());
      statement.setInt(2, enrollment.getCourseId());
      if (enrollment.getGrade() != null)
        statement.setDouble(3, enrollment.getGrade());
      else
        statement.setNull(3, Types.DOUBLE);

      statement.executeUpdate();

      try (ResultSet rs = statement.getGeneratedKeys()) {
        if (rs.next())
          enrollment.setId(rs.getInt(1));
      }
    }
  }

  /**
   * Retrieves an enrollment by its ID.
   *
   * @param id the enrollment ID
   * @return the Enrollment object or null if not found
   * @throws SQLException if a database access error occurs
   */
  public static Enrollment getById(int id) throws SQLException {
    final String sql = "SELECT id, student_id, course_id, grade FROM enrollments WHERE id = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setInt(1, id);

      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          Double grade = rs.getObject("grade") != null ? rs.getDouble("grade") : null;
          return new Enrollment(rs.getInt("id"), rs.getInt("student_id"), rs.getInt("course_id"), grade);
        }
      }
    }
    return null;
  }

  /**
   * Retrieves all enrollments from the database.
   *
   * @return a List of all Enrollment objects
   * @throws SQLException if a database access error occurs
   */
  public static List<Enrollment> getAll() throws SQLException {
    final String sql = "SELECT id, student_id, course_id, grade FROM enrollments";
    List<Enrollment> list = new ArrayList<>();

    try (Statement statement = Session.getDatabaseConnection().createStatement();
         ResultSet rs = statement.executeQuery(sql)) {

      while (rs.next()) {
        Double grade = rs.getObject("grade") != null ? rs.getDouble("grade") : null;
        list.add(new Enrollment(rs.getInt("id"), rs.getInt("student_id"), rs.getInt("course_id"), grade));
      }
    }
    return list;
  }
}
