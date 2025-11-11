package com.khazar.sims.database.table;

import com.khazar.sims.core.Session;
import com.khazar.sims.core.data.Department;

import java.sql.Types;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.List;
import java.util.ArrayList;

/**
 * DepartmentTable provides static methods to interact with the `departments` table
 * in the database. Supports adding, updating, deleting, and retrieving department records.
 */
public class DepartmentTable {

  /* ---------- Add a new department ---------- */
  public static void add(Department department) throws SQLException {
    final String sql = "INSERT INTO departments(name, head_id) VALUES (?, ?)";
    try (PreparedStatement statement = Session.getDatabaseConnection()
            .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, department.getName());
      if (department.getHeadId() != null) statement.setInt(2, department.getHeadId());
      else statement.setNull(2, Types.INTEGER);

      statement.executeUpdate();

      try (ResultSet rs = statement.getGeneratedKeys()) {
        if (rs.next()) department.setId(rs.getInt(1));
      }
    }
  }

  /* ---------- Retrieve a department by ID ---------- */
  public static Department getById(int id) throws SQLException {
    final String sql = "SELECT id, name, head_id FROM departments WHERE id = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setInt(1, id);
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) return mapResultSet(rs);
      }
    }
    return null;
  }

  /* ---------- Retrieve all departments ---------- */
  public static List<Department> getAll() throws SQLException {
    final String sql = "SELECT id, name, head_id FROM departments";
    List<Department> list = new ArrayList<>();
    try (Statement statement = Session.getDatabaseConnection().createStatement();
         ResultSet rs = statement.executeQuery(sql)) {
      while (rs.next()) list.add(mapResultSet(rs));
    }
    return list;
  }

  /* ---------- Update an existing department ---------- */
  public static void update(Department department) throws SQLException {
    final String sql = "UPDATE departments SET name = ?, head_id = ? WHERE id = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setString(1, department.getName());
      if (department.getHeadId() != null) statement.setInt(2, department.getHeadId());
      else statement.setNull(2, Types.INTEGER);
      statement.setInt(3, department.getId());

      statement.executeUpdate();
    }
  }

  /* ---------- Delete a department by ID ---------- */
  public static void delete(int id) throws SQLException {
    final String sql = "DELETE FROM departments WHERE id = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setInt(1, id);
      statement.executeUpdate();
    }
  }

  /* ---------- Helper method to map a ResultSet row to a Department object ---------- */
  private static Department mapResultSet(ResultSet rs) throws SQLException {
    Integer headId = rs.getObject("head_id") != null ? rs.getInt("head_id") : null;
    return new Department(rs.getInt("id"), rs.getString("name"), headId);
  }
}
