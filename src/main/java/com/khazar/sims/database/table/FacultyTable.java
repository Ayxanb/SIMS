package com.khazar.sims.database.table;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.Faculty;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

/**
 * Handles database access for the 'faculties' table.
 */
public class FacultyTable extends BaseTable<Faculty> {
  @Override
  protected String getTableName() {
    return "faculties";
  }

  @Override
  protected Faculty map(ResultSet rs) throws SQLException {
    return new Faculty(
      rs.getInt("id"),
      rs.getString("name"),
      rs.getString("code")
    );
  }

  @Override
  public Faculty add(Faculty faculty) throws SQLException {
    int generatedId = executeInsert(
      "INSERT INTO faculties (name, code) VALUES (?, ?)",
      ps -> {
        ps.setString(1, faculty.getName());
        ps.setString(2, faculty.getCode());
      }
    );
    faculty.setId(generatedId);
    return faculty;
  }

  @Override
  public void update(Faculty faculty) throws SQLException {
    // Use executeUpdate helper
    executeUpdate(
      "UPDATE faculties SET name = ?, code = ? WHERE id = ?",
      ps -> {
        ps.setString(1, faculty.getName());
        ps.setString(2, faculty.getCode());
        ps.setInt(3, faculty.getId());
      }
    );
  }

  /**
   * Retrieves a Faculty by its unique code.
   */
  public Faculty getByCode(String code) throws SQLException {
    final String sql = "SELECT * FROM faculties WHERE code = ?";
    PreparedStatement ps = Session.getDatabaseConnection().prepareStatement(sql);
    ps.setString(1, code);
    ResultSet rs = ps.executeQuery();
    return rs.next() ? map(rs) : null;
  }
}