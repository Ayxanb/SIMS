package com.khazar.sims.database.table;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.Student;
import com.khazar.sims.database.data.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Types;

public class StudentTable extends UserChildTable<Student> {
  @Override
  protected String getTableName() { return "students"; }

  @Override
  protected Student map(ResultSet rs) throws SQLException {
    return new Student(
      rs.getInt("user_id"),
      rs.getInt("enrollment_year"),
      rs.getInt("department_id"),
      rs.getFloat("gpa")
    );
  }
  
  @Override
  protected User getDelegatedUser(Student s) throws SQLException { return Session.getUsersTable().getById(s.getUserId()); }
  
  @Override
  protected int getUserId(Student s) { return s.getUserId(); }

  public Student getByUserId(int id) throws SQLException {
    String sql = "SELECT * FROM " + getTableName() + " WHERE user_id = ?";
    PreparedStatement ps = Session.getDatabaseConnection().prepareStatement(sql);
    ps.setInt(1, id);
    ResultSet rs = ps.executeQuery();
    return rs.next() ? map(rs) : null;
  }

  @Override
  protected String getChildInsertSql() {
    return "INSERT INTO students(user_id, enrollment_year, department_id, gpa) VALUES (?, ?, ?, ?)";
  }

  @Override
  protected void fillChildInsertStatement(PreparedStatement ps, Student s) throws SQLException {
    ps.setInt(1, s.getUserId());
    ps.setInt(2, s.getEnrollmentYear());
    ps.setInt(3, s.getDepartmentId());
    if (s.getGpa() == null)
      ps.setNull(4, Types.FLOAT);
    else
      ps.setFloat(4, s.getGpa());
  }

  @Override
  protected String getChildUpdateSql() {
    return "UPDATE students SET enrollment_year=?, department_id=?, gpa=? WHERE user_id=?";
  }

  @Override
  protected void fillChildUpdateStatement(PreparedStatement ps, Student s) throws SQLException {
    ps.setInt(1, s.getEnrollmentYear());
    ps.setInt(2, s.getDepartmentId());
    if (s.getGpa() == null)
      ps.setNull(3, Types.FLOAT);
    else
      ps.setFloat(3, s.getGpa());
  }
  
  @Override
  protected int getChildUpdateParameterCount() {
    return 3;
  }
}