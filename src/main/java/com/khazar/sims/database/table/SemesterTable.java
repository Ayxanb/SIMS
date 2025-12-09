package com.khazar.sims.database.table;

import com.khazar.sims.database.data.Semester;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

/**
 * DAO implementation for the 'semesters' table.
 * Handles CRUD operations for Semester data objects.
 */
public class SemesterTable extends BaseTable<Semester> {

  @Override
  protected String getTableName() {
    return "semesters";
  }

  @Override
  protected Semester map(ResultSet rs) throws SQLException {
    Date startDate = rs.getDate("start_date");
    Date endDate = rs.getDate("end_date");
    return new Semester(rs.getInt("id"), rs.getString("name"), startDate, endDate);
  }

  /**
   * Adds a new Semester record to the database.
   * @param semester The Semester DTO to add.
   * @return The Semester DTO with the generated ID set.
   */
  @Override
  public Semester add(Semester semester) throws SQLException {
    String sql = "INSERT INTO semesters(name, start_date, end_date) VALUES (?, ?, ?)";
    
    int id = executeInsert(
      sql,
      ps -> {
        ps.setString(1, semester.getName());
        // Convert java.util.Date to java.sql.Date for persistence
        ps.setDate(2, semester.getStartDate() != null ? new Date(semester.getStartDate().getTime()) : null);
        ps.setDate(3, semester.getEndDate() != null ? new Date(semester.getEndDate().getTime()) : null);
      }
    );
    
    semester.setId(id);
    return semester;
  }

  /**
   * Updates an existing Semester record in the database.
   * @param semester The Semester DTO with updated fields.
   */
  @Override
  public void update(Semester semester) throws SQLException {
    String sql = "UPDATE semesters SET name=?, start_date=?, end_date=? WHERE id=?";
    
    executeUpdate(
      sql,
      ps -> {
        ps.setString(1, semester.getName());
        ps.setDate(2, semester.getStartDate());
        ps.setDate(3, semester.getEndDate());
        ps.setInt(4, semester.getId());
      }
    );
  }
}