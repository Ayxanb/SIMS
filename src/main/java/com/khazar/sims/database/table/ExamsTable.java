package com.khazar.sims.database.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.khazar.sims.database.data.Exam;

public class ExamsTable {
  private final Connection connection;

  public ExamsTable(Connection connection) {
    this.connection = connection;
  }
  
  /**
   * Adds a new exam record.
   * Note: The 'type' is not in the schema, but is inferred here for clarity in the UI. 
   * In the real database, the combination of offering_id and exam_date is the primary key.
   */
  public void add(Exam exam) throws SQLException {
    String query = """
      INSERT INTO exams (offering_id, exam_date, max_score) 
      VALUES (?, ?, ?)
    """;
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setInt(1, exam.getCourseOfferingId());
      statement.setDate(2, exam.getExamDate());
      // Using exam.getType() as a placeholder for display name if we had a dedicated field,
      // but sticking to the schema's core data for now.
      statement.setDouble(3, exam.getMaxScore());
      statement.executeUpdate();
    }
    catch (SQLException e) {
      System.out.println(e);
    }
  }

  /**
   * Retrieves all exams for a specific course offering.
   */
  public List<Exam> getByOfferingId(int offeringId) throws SQLException {
    List<Exam> exams = new ArrayList<>();
    // Note: For now, we use a placeholder for 'type' as it's not in the schema.
    String query = "SELECT offering_id, exam_date, max_score FROM exams WHERE offering_id = ? ORDER BY exam_date DESC";
    
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setInt(1, offeringId);
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          // MOCK: Setting 'Type' based on a simple date check for demonstration
          String examType = rs.getDate("exam_date").toString().contains("-05-") ? "Midterm" : "Final"; 
          
          Exam exam = new Exam(
            rs.getInt("offering_id"),
            rs.getDate("exam_date"),
            examType,
            rs.getDouble("max_score")
          );
          exams.add(exam);
        }
      }
    }
    catch (SQLException e) {
      System.err.println(e);
      return null;
    }
    return exams;
  }
  
  // Add update and delete methods as needed.
}