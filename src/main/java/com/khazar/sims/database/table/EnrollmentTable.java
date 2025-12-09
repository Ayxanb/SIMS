package com.khazar.sims.database.table;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.Enrollment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles operations related to the 'enrollments' table.
 * Schema: enrollments(offering_id, student_id, final_grade)
 * - student_id references students(id)
 * - No primary key ID, composite unique key on (offering_id, student_id)
 */
public class EnrollmentTable {

  /**
   * Get all enrollments for a specific course offering.
   * Joins with users table to get student names.
   *
   * @param offeringId The course offering ID
   * @return List of enrollments with student details
   */
  public List<Enrollment> getByOfferingId(int offeringId) throws SQLException {
    String query = """
      SELECT
        e.offering_id,
        e.student_id, 
        e.final_grade, 
        u.first_name, 
        u.last_name
      FROM enrollments e
      JOIN users u ON e.student_id = u.id
      WHERE e.offering_id = ?
      ORDER BY u.last_name, u.first_name
      """;
    List<Enrollment> enrollments = new ArrayList<>();
    Connection conn = Session.getDatabaseConnection();
    try (PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setInt(1, offeringId);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          Double finalGrade = rs.getDouble("final_grade");
          if (rs.wasNull()) {
            finalGrade = null;
          }
          enrollments.add(new Enrollment(
            rs.getInt("student_id"),
            rs.getInt("offering_id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            finalGrade
          ));
        }
      }
    }
    return enrollments;
  }

  /**
   * Get all enrollments for a specific student.
   *
   * @param studentUserId The student's id
   * @return List of enrollments
   */
  public List<Enrollment> getByStudentId(int studentUserId) throws SQLException {
    String query = """
      SELECT 
        e.offering_id,
        e.student_id, 
        e.final_grade, 
        u.first_name, 
        u.last_name
      FROM enrollments e
      JOIN users u ON e.student_id = u.id
      WHERE e.student_id = ?
      ORDER BY e.offering_id
      """;
    List<Enrollment> enrollments = new ArrayList<>();
    Connection conn = Session.getDatabaseConnection();
    try (PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setInt(1, studentUserId);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          Double finalGrade = rs.getDouble("final_grade");
          if (rs.wasNull()) {
            finalGrade = null;
          }
          enrollments.add(new Enrollment(
            rs.getInt("student_id"),
            rs.getInt("offering_id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            finalGrade
          ));
        }
      }
    }
    return enrollments;
  }

  /**
   * Enroll a student in a course offering.
   *
   * @param offeringId The course offering ID
   * @param studentUserId The student's id
   */
  public void enroll(int offeringId, int studentUserId) throws SQLException {
    String query = """
      INSERT INTO enrollments (offering_id, student_id, final_grade)
      VALUES (?, ?, NULL)
      """;
    Connection conn = Session.getDatabaseConnection();
    try (PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setInt(1, offeringId);
      stmt.setInt(2, studentUserId);
      stmt.executeUpdate();
    }
  }

  /**
   * Remove a student from a course offering.
   *
   * @param offeringId The course offering ID
   * @param studentUserId The student's id
   */
  public void unenroll(int offeringId, int studentUserId) throws SQLException {
    String query = "DELETE FROM enrollments WHERE offering_id = ? AND student_id = ?";
    Connection conn = Session.getDatabaseConnection();
    try (PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setInt(1, offeringId);
      stmt.setInt(2, studentUserId);
      stmt.executeUpdate();
    }
  }

  /**
   * Update the final grade for a student.
   *
   * @param offeringId The course offering ID
   * @param studentUserId The student's id
   * @param finalGrade The new grade (can be null)
   */
  public void updateFinalGrade(int offeringId, int studentUserId, Double finalGrade) throws SQLException {
    String query = """
      UPDATE enrollments 
      SET final_grade = ? 
      WHERE offering_id = ? AND student_id = ?
      """;
    Connection conn = Session.getDatabaseConnection();
    try (PreparedStatement stmt = conn.prepareStatement(query)) {
      if (finalGrade != null) {
        stmt.setDouble(1, finalGrade);
      }
      else {
        stmt.setNull(1, java.sql.Types.DECIMAL);
      }
      stmt.setInt(2, offeringId);
      stmt.setInt(3, studentUserId);
      stmt.executeUpdate();
    }
  }

  /**
   * Get a specific enrollment record.
   *
   * @param offeringId The course offering ID
   * @param studentUserId The student's id
   * @return Enrollment object or null if not found
   */
  public Enrollment get(int offeringId, int studentUserId) throws SQLException {
    String query = """
      SELECT 
        e.offering_id,
        e.student_id, 
        e.final_grade, 
        u.first_name, 
        u.last_name
      FROM enrollments e
      JOIN users u ON e.student_id = u.id
      WHERE e.offering_id = ? AND e.student_id = ?
      """;
    Connection conn = Session.getDatabaseConnection();
    try (PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setInt(1, offeringId);
      stmt.setInt(2, studentUserId);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          Double finalGrade = rs.getDouble("final_grade");
          if (rs.wasNull()) {
            finalGrade = null;
          }
          return new Enrollment(
            rs.getInt("student_id"),
            rs.getInt("offering_id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            finalGrade
          );
        }
      }
    }
    return null;
  }

  /**
   * Check if a student is enrolled in a course offering.
   *
   * @param offeringId The course offering ID
   * @param studentUserId The student's id
   * @return true if enrolled, false otherwise
   */
  public boolean isEnrolled(int offeringId, int studentUserId) throws SQLException {
    String query = "SELECT 1 FROM enrollments WHERE offering_id = ? AND student_id = ? LIMIT 1";
    Connection conn = Session.getDatabaseConnection();
    try (PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setInt(1, offeringId);
      stmt.setInt(2, studentUserId);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next();
      }
    }
  }

  /**
   * Get the number of students enrolled in a course offering.
   *
   * @param offeringId The course offering ID
   * @return Number of enrolled students
   */
  public int getEnrollmentCount(int offeringId) throws SQLException {
    String query = "SELECT COUNT(*) as count FROM enrollments WHERE offering_id = ?";
    Connection conn = Session.getDatabaseConnection();
    try (PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setInt(1, offeringId);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt("count");
        }
      }
    }
    return 0;
  }

  /**
   * Get all enrollments (for admin purposes).
   *
   * @return List of all enrollments
   */
  public List<Enrollment> getAll() throws SQLException {
    String query = """
      SELECT 
        e.offering_id,
        e.student_id, 
        e.final_grade, 
        u.first_name, 
        u.last_name
      FROM enrollments e
      JOIN users u ON e.student_id = u.id
      ORDER BY e.offering_id, u.last_name, u.first_name
      """;
    List<Enrollment> enrollments = new ArrayList<>();
    Connection conn = Session.getDatabaseConnection();
    try (PreparedStatement stmt = conn.prepareStatement(query)) {
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          Double finalGrade = rs.getDouble("final_grade");
          if (rs.wasNull()) {
            finalGrade = null;
          }
          enrollments.add(new Enrollment(
            rs.getInt("student_id"),
            rs.getInt("offering_id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            finalGrade
          ));
        }
      }
    }

    return enrollments;
  }

  /**
   * Batch enroll multiple students in a course offering.
   *
   * @param offeringId The course offering ID
   * @param studentUserIds List of student user_ids to enroll
   */
  public void batchEnroll(int offeringId, List<Integer> studentUserIds) throws SQLException {
    String query = """
      INSERT INTO enrollments (offering_id, student_id, final_grade)
      VALUES (?, ?, NULL)
      """;
    Connection conn = Session.getDatabaseConnection();
    try (PreparedStatement stmt = conn.prepareStatement(query)) {
      for (int studentUserId : studentUserIds) {
        stmt.setInt(1, offeringId);
        stmt.setInt(2, studentUserId);
        stmt.addBatch();
      }
      stmt.executeBatch();
    }
  }

  /**
   * Delete all enrollments for a course offering.
   *
   * @param offeringId The course offering ID
   */
  public void deleteByOfferingId(int offeringId) throws SQLException {
    String query = "DELETE FROM enrollments WHERE offering_id = ?";
    Connection conn = Session.getDatabaseConnection();
    try (PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setInt(1, offeringId);
      stmt.executeUpdate();
    }
  }

  /**
   * Delete all enrollments for a student.
   *
   * @param studentUserId The student's id
   */
  public void deleteByStudentId(int studentUserId) throws SQLException {
    String query = "DELETE FROM enrollments WHERE student_id = ?";
    Connection conn = Session.getDatabaseConnection();
    try (PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setInt(1, studentUserId);
      stmt.executeUpdate();
    }
  }
}