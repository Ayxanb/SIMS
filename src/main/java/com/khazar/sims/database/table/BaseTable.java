package com.khazar.sims.database.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.khazar.sims.core.Session;

/**
 * Interface defining the standard Data Access Object (DAO) operations.
 */
interface DAO<T> {
  T getById(int id) throws SQLException;
  T add(T t) throws SQLException;
  void update(T t) throws SQLException;
  void delete(int id) throws SQLException;
  List<T> getAll() throws SQLException;
}

/**
 * Abstract base class providing common functionality for all database tables.
 * Subclasses only need to implement map(), getTableName(), add(), and update().
 */
public abstract class BaseTable<T> implements DAO<T> {

  /* Methods to be implemented by concrete table classes */
  protected abstract String getTableName();
  protected abstract T map(ResultSet rs) throws SQLException;

  /**
   * Functional interface for setting parameters on a PreparedStatement.
   */
  @FunctionalInterface
  protected interface Params {
    void fill(PreparedStatement ps) throws SQLException;
  }

  /* ---------------------- READ OPERATIONS ---------------------- */

  @Override
  public T getById(int id) throws SQLException {
    String sql = "SELECT * FROM " + getTableName() + " WHERE id = ?";
    
    Connection conn = Session.getDatabaseConnection();
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, id);

      try (ResultSet rs = ps.executeQuery()) {
        /* Maps the first result found, or returns null if none found */
        return rs.next() ? map(rs) : null;
      }
    }
  }

  @Override
  public List<T> getAll() throws SQLException {
    String sql = "SELECT * FROM " + getTableName();
    List<T> list = new ArrayList<>();
    
    Connection conn = Session.getDatabaseConnection();
    try (Statement stmt = conn.createStatement();
          ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        list.add(map(rs));
      }
    }

    return list;
  }

  /* ---------------------- WRITE OPERATIONS ---------------------- */
  
  /* Required methods from the DAO interface for specific table implementation */
  @Override
  public abstract T add(T t) throws SQLException;

  @Override
  public abstract void update(T t) throws SQLException;

  @Override
  public void delete(int id) throws SQLException {
    String sql = "DELETE FROM " + getTableName() + " WHERE id = ?";
    /* Executes the DELETE statement, setting the ID parameter */
    executeUpdate(sql, ps -> ps.setInt(1, id));
  }
  
  /* ---------------------- SQL EXECUTION HELPERS ---------------------- */

  /**
   * Executes an INSERT statement and returns the auto-generated primary key (ID).
   * @param sql The SQL INSERT statement.
   * @param params A Params function to fill the PreparedStatement parameters.
   * @return The generated key (ID) of the new row.
   * @throws SQLException if the insert fails or no key is generated.
   */
  protected int executeInsert(String sql, Params params) throws SQLException {
    Connection conn = Session.getDatabaseConnection();
    try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      params.fill(ps);
      ps.executeUpdate();

      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next())
          return keys.getInt(1);
        else
          throw new SQLException("Insert failed, no generated key obtained.");
      }
    }
  }

  /**
   * Executes an UPDATE or DELETE statement.
   * @param sql The SQL UPDATE or DELETE statement.
   * @param params A Params function to fill the PreparedStatement parameters.
   * @throws SQLException if the execution fails.
   */
  protected void executeUpdate(String sql, Params params) throws SQLException {
    Connection conn = Session.getDatabaseConnection();
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      params.fill(ps);
      ps.executeUpdate();
    }
  }
}