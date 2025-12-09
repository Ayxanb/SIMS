package com.khazar.sims.database.table;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.khazar.sims.database.data.ExamResult;

public class ExamResultsTable {
  private final Connection connection;

  public ExamResultsTable(Connection connection) {
    this.connection = connection;
  }
  
  /**
   * Retrieves exam results for all students in a given course offering and exam date.
   * Joins exam_results, students, users, and exams.
   */
  public List<ExamResult> getResultsByOfferingAndExamDate(int offeringId, Date examDate) throws SQLException {
    List<ExamResult> results = new ArrayList<>();
    
    // Joins results with student/user details and exam details for display
    String query = """
      SELECT 
        er.offering_id, er.student_id, er.exam_date, er.score,
        u.first_name, u.last_name, e.max_score
      FROM exam_results er
      JOIN students s ON er.student_id = s.user_id
      JOIN users u ON s.user_id = u.id
      JOIN exams e ON er.offering_id = e.offering_id AND er.exam_date = e.exam_date
      WHERE er.offering_id = ? AND er.exam_date = ?
      ORDER BY u.last_name, u.first_name
    """;
    
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setInt(1, offeringId);
      statement.setDate(2, examDate);
      
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          String studentName = rs.getString("first_name") + " " + rs.getString("last_name");
          // MOCK: Setting 'Type' based on a simple date check for demonstration
          String examType = rs.getDate("exam_date").toString().contains("-05-") ? "Midterm" : "Final"; 
          
          ExamResult result = new ExamResult(
            rs.getInt("offering_id"),
            rs.getInt("student_id"),
            rs.getDate("exam_date"),
            rs.getDouble("score"),
            studentName,
            examType,
            rs.getDouble("max_score")
          );
          results.add(result);
        }
      }
    }
    catch (SQLException e) {
      return null;
    }
    return results;
  }

  /**
   * Updates an existing exam score or inserts a new one if it doesn't exist (UPSERT logic).
   */
  public void saveScore(ExamResult result) throws SQLException {
    // Simple update first, then insert if no rows were affected.
    String updateQuery = "UPDATE exam_results SET score = ? WHERE offering_id = ? AND student_id = ? AND exam_date = ?";
    String insertQuery = "INSERT INTO exam_results (offering_id, student_id, exam_date, score) VALUES (?, ?, ?, ?)";

    try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
      updateStmt.setDouble(1, result.getScore());
      updateStmt.setInt(2, result.getCourseOfferingId());
      updateStmt.setInt(3, result.getStudentId());
      updateStmt.setDate(4, result.getExamDate());
      
      int rowsAffected = updateStmt.executeUpdate();

      if (rowsAffected == 0) {
        // No update performed, so insert
        try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
          insertStmt.setInt(1, result.getCourseOfferingId());
          insertStmt.setInt(2, result.getStudentId());
          insertStmt.setDate(3, result.getExamDate());
          insertStmt.setDouble(4, result.getScore());
          insertStmt.executeUpdate();
        }
      }
    } catch (SQLException e) {
      System.err.println(e);
    }
  }
}