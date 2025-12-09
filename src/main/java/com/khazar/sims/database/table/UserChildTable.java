package com.khazar.sims.database.table;

import java.sql.PreparedStatement;

import com.khazar.sims.database.data.User;
import com.khazar.sims.core.Session;

import java.sql.SQLException;

/**
 * Base class for entities that use a foreign key 'user_id' (Composition/Delegation).
 * Handles the two-step ADD, UPDATE, and DELETE operations.
 */
public abstract class UserChildTable<T> extends BaseTable<T> {
  protected abstract User getDelegatedUser(T t) throws SQLException;

  /** Must return the FK user_id from object T. */
  protected abstract int getUserId(T t);
  
  /** Returns the SQL string for the child table INSERT statement. */
  protected abstract String getChildInsertSql();

  /** Fills the child INSERT PreparedStatement, starting after the user_id (param index 2). */
  protected abstract void fillChildInsertStatement(PreparedStatement ps, T t) throws SQLException;
  
  /** Returns the SQL string for the child table UPDATE statement (WHERE clause included). */
  protected abstract String getChildUpdateSql();

  /** Fills the child UPDATE PreparedStatement (excluding the user_id in the WHERE clause). */
  protected abstract void fillChildUpdateStatement(PreparedStatement ps, T t) throws SQLException;
  
  @Override
  public T add(T t) throws SQLException {
    User user = getDelegatedUser(t);
    Session.getUsersTable().add(user); 
    int userId = getUserId(t); 
    executeUpdate(getChildInsertSql(), ps -> {
      ps.setInt(1, userId); /* user_id is always the first parameter */
      fillChildInsertStatement(ps, t);
    });
    return t;
  }

  @Override
  public void update(T t) throws SQLException {
    Session.getUsersTable().update(getDelegatedUser(t));
    int userId = getUserId(t);
    executeUpdate(getChildUpdateSql(), ps -> {
      fillChildUpdateStatement(ps, t);
      ps.setInt(getChildUpdateParameterCount() + 1, userId);
    });
  }

  /** Helper method to get the number of parameters in the UPDATE SET clause. */
  protected abstract int getChildUpdateParameterCount();
  
  @Override
  public void delete(int id) throws SQLException {
    String sql = "DELETE FROM " + getTableName() + " WHERE user_id = ?";
    executeUpdate(sql, ps -> ps.setInt(1, id));
    Session.getUsersTable().delete(id);
  }
  
  public void delete(T t) throws SQLException {
    delete(getUserId(t));
  }
}