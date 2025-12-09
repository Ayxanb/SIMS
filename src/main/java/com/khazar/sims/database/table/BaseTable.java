package com.khazar.sims.database.table;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import com.khazar.sims.core.Session;

import java.util.List;
import java.util.ArrayList;

interface DAO<T> {
  T getById(int id) throws SQLException;
  T add(T t) throws SQLException;  /* Returns the added object with the generated ID */
  void update(T t) throws SQLException;
  void delete(int id) throws SQLException;
  List<T> getAll() throws SQLException;
}

/**
 * Base DAO implementation providing common CRUD operations.
 * T should be a class with a standard 'id' field for identification.
 */
public abstract class BaseTable<T> implements DAO<T> {
  /** The database table name (e.g., "users") */
  protected abstract String getTableName();

  /** Converts a single ResultSet row into an object of type T */
  protected abstract T map(ResultSet rs) throws SQLException;
  
  /** For setting PreparedStatement parameters */
  @FunctionalInterface
  protected interface Params {
    void fill(PreparedStatement ps) throws SQLException;
  }

  @Override
  public T getById(int id) throws SQLException {
    String sql = "SELECT * FROM " + getTableName() + " WHERE id = ?";
    PreparedStatement ps = Session.getDatabaseConnection().prepareStatement(sql);
    ps.setInt(1, id);
    ResultSet rs = ps.executeQuery();
    return rs.next() ? map(rs) : null;
  }

  @Override
  public List<T> getAll() throws SQLException {
    String sql = "SELECT * FROM " + getTableName();
    List<T> list = new ArrayList<>();
    Statement stmt = Session.getDatabaseConnection().createStatement();
    ResultSet rs = stmt.executeQuery(sql);
    while (rs.next()) {
      list.add(map(rs));
    }
    return list;
  }

  /**
   * Executes an INSERT and returns the auto-generated ID.
   */
  protected int executeInsert(String sql, Params params) throws SQLException {
    PreparedStatement ps = Session.getDatabaseConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    params.fill(ps);
    ResultSet keys = ps.getGeneratedKeys();
    if (keys.next())
      return keys.getInt(1);
    else
      throw new SQLException("Insert failed, no generated key obtained.");
  }

  /**
   * Executes an UPDATE or DELETE.
   */
  protected void executeUpdate(String sql, Params params) throws SQLException {
    PreparedStatement ps = Session.getDatabaseConnection().prepareStatement(sql);      
    params.fill(ps);
    ps.executeUpdate();
  }
  
  @Override
  public abstract T add(T t) throws SQLException;

  @Override
  public abstract void update(T t) throws SQLException;
  
  @Override
  public void delete(int id) throws SQLException {
    String sql = "DELETE FROM " + getTableName() + " WHERE id = ?";
    executeUpdate(sql, ps -> ps.setInt(1, id));
  }
}