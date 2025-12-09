package com.khazar.sims.database.table;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.Teacher;
import com.khazar.sims.database.data.User; 
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

public class TeacherTable extends UserChildTable<Teacher> {
  @Override
  protected String getTableName() { return "teachers"; }
  
  @Override
  protected Teacher map(ResultSet rs) throws SQLException {
    return new Teacher(
      rs.getInt("user_id"),
      rs.getInt("department_id")
    );
  }
  
  @Override
  protected User getDelegatedUser(Teacher t) throws SQLException { return Session.getUsersTable().getById(t.getUserId()); }
  
  @Override
  protected int getUserId(Teacher t) { return t.getUserId(); }
  
  @Override
  protected String getChildInsertSql() {
    return "INSERT INTO teachers(user_id, department_id) VALUES (?, ?)";
  }

  @Override
  protected void fillChildInsertStatement(PreparedStatement ps, Teacher t) throws SQLException {
    ps.setInt(2, t.getFacultyId());
  }

  @Override
  protected String getChildUpdateSql() {
    return "UPDATE teachers SET department_id=? WHERE user_id=?";
  }

  @Override
  protected void fillChildUpdateStatement(PreparedStatement ps, Teacher t) throws SQLException {
    ps.setInt(1, t.getFacultyId());
  }
  
  @Override
  protected int getChildUpdateParameterCount() {
    return 1;
  }
}