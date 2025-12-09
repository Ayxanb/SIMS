package com.khazar.sims.database.table;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.khazar.sims.database.data.Department;
import com.khazar.sims.core.Session;

import java.util.List;

/**
 * DepartmentTable — table access implemented on top of BaseTable helpers, 
 * with robust resource management.
 */
public class DepartmentTable extends BaseTable<Department> {
  @Override
  protected String getTableName() { return "departments"; }

  @Override
  protected Department map(ResultSet rs) throws SQLException {
    return new Department(
      rs.getInt("id"),
      rs.getInt("faculty_id"),
      rs.getString("name"),
      rs.getString("code")
    );
  }
  
  @Override
  public Department add(Department department) throws SQLException {
    int generatedId = executeInsert(
      "INSERT INTO departments (name, code, faculty_id) VALUES (?, ?, ?)", 
      ps -> {
        ps.setString(1, department.getName());
        ps.setString(2, department.getCode());

        ps.setInt(3, department.getFacultyId());
      }
    );
    department.setId(generatedId);
    return department;
  }

  @Override
  public Department getById(int id) throws SQLException { return super.getById(id); }

  @Override
  public List<Department> getAll() throws SQLException { return super.getAll(); }

  @Override
  public void update(Department department) throws SQLException {
    executeUpdate(
      "UPDATE departments SET name = ?, code = ? WHERE id = ?",
      ps -> {
        ps.setString(1, department.getName());
        ps.setString(2, department.getCode());
        ps.setInt(3, department.getId());
      }
    );
  }

  @Override
  public void delete(int id) throws SQLException { super.delete(id); }

  public Department getByName(String name) throws SQLException {
    final String sql = "SELECT * FROM departments WHERE name = ?";
    PreparedStatement ps = Session.getDatabaseConnection().prepareStatement(sql);
    ps.setString(1, name);
    ResultSet rs = ps.executeQuery();
    return rs.next() ? map(rs) : null;
  }

  public Department getByCode(String code) throws SQLException {
    final String sql = "SELECT * FROM departments WHERE code = ?";
    PreparedStatement ps = Session.getDatabaseConnection().prepareStatement(sql);
    ps.setString(1, code);
    ResultSet rs = ps.executeQuery();
    return rs.next() ? map(rs) : null;
  }
}