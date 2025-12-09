package com.khazar.sims.database.table;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.Grade;

public class GradeTable extends BaseTable<Grade> {

  @Override
  protected String getTableName() {
    return "assessments";
  }

  @Override
  protected Grade map(ResultSet rs) throws SQLException {
    return new Grade(
      rs.getInt("student_id"),
      rs.getInt("offering_id"),
      rs.getString("assessment_name"),
      rs.getInt("score"),
      rs.getInt("max_score"),
      rs.getString("date_submitted")
    );
  }

  /**
   * Not supported because assessments do NOT have a single-column primary key.
   */
  @Override
  public Grade getById(int id) throws SQLException {
    throw new UnsupportedOperationException(
        "Grades use a composite key (offering_id, student_id, assessment_name).");
  }

  @Override
  public List<Grade> getAll() throws SQLException {
    String sql = "SELECT * FROM " + getTableName();
    List<Grade> assessments = new ArrayList<>();

    try (var stmt = Session.getDatabaseConnection().createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        assessments.add(map(rs));
      }
    }

    return assessments;
  }

  @Override
  public Grade add(Grade grade) throws SQLException {
    String sql = """
      INSERT INTO assessments
      (offering_id, student_id, assessment_name, score, max_score, date_submitted)
      VALUES (?, ?, ?, ?, ?, ?)
    """;

    int rows = executeUpdateWithCount(sql, ps -> {
      ps.setInt(1, grade.getCourseOfferingId());
      ps.setInt(2, grade.getStudentId());
      ps.setString(3, grade.getAssessmentName());
      ps.setInt(4, grade.getScore());
      ps.setInt(5, grade.getMaxScore());
      ps.setString(6, grade.getDateSubmitted());
    });

    if (rows > 0) return grade;
    throw new SQLException("Failed to insert grade.");
  }

  @Override
  public void update(Grade grade) throws SQLException {
    String sql = """
      UPDATE assessments
      SET score = ?, max_score = ?, date_submitted = ?
      WHERE offering_id = ?
        AND student_id = ?
        AND assessment_name = ?
    """;

    executeUpdate(sql, ps -> {
      ps.setInt(1, grade.getScore());
      ps.setInt(2, grade.getMaxScore());
      ps.setString(3, grade.getDateSubmitted());
      ps.setInt(4, grade.getCourseOfferingId());
      ps.setInt(5, grade.getStudentId());
      ps.setString(6, grade.getAssessmentName());
    });
  }

  /**
   * Retrieves all assessments for a specific offering and assessment.
   */
  public List<Grade> getByOfferingAndAssessment(int offeringId, String assessmentName)
      throws SQLException {

    String sql = """
      SELECT * FROM assessments
      WHERE offering_id = ? AND assessment_name = ?
    """;

    List<Grade> assessments = new ArrayList<>();

    try (PreparedStatement ps =
             Session.getDatabaseConnection().prepareStatement(sql)) {

      ps.setInt(1, offeringId);
      ps.setString(2, assessmentName);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          assessments.add(map(rs));
        }
      }
    }

    return assessments;
  }

  /**
   * Retrieves all assessments for a course offering (used by TEACHER as READ-ONLY).
   */
  public List<Grade> getByOfferingId(int offeringId) throws SQLException {
    String sql = "SELECT * FROM assessments WHERE offering_id = ?";
    List<Grade> assessments = new ArrayList<>();

    try (PreparedStatement ps =
             Session.getDatabaseConnection().prepareStatement(sql)) {

      ps.setInt(1, offeringId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          assessments.add(map(rs));
        }
      }
    }

    return assessments;
  }

  /**
   * Returns all unique assessment names for an offering.
   */
  public List<String> getAssessmentNamesByOffering(int offeringId)
      throws SQLException {

    String sql = """
      SELECT DISTINCT assessment_name
      FROM assessments
      WHERE offering_id = ?
    """;

    List<String> names = new ArrayList<>();

    try (PreparedStatement ps =
             Session.getDatabaseConnection().prepareStatement(sql)) {

      ps.setInt(1, offeringId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          names.add(rs.getString("assessment_name"));
        }
      }
    }

    return names;
  }

  /**
   * Internal helper used by BaseTable.
   */
  private int executeUpdateWithCount(String sql, Params params)
      throws SQLException {

    try (PreparedStatement ps =
             Session.getDatabaseConnection().prepareStatement(sql)) {

      params.fill(ps);
      return ps.executeUpdate();
    }
  }
}
