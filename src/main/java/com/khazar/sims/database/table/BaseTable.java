package com.khazar.sims.database.table;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.khazar.sims.core.Session;

interface DAO<T> {
  T getById(int id) throws SQLException;
  T add(T t) throws SQLException;
  void update(T t) throws SQLException;
  void delete(int id) throws SQLException;
  List<T> getAll() throws SQLException;
}

public abstract class BaseTable<T> implements DAO<T> {

  protected abstract String getTableName();
  protected abstract T map(ResultSet rs) throws SQLException;

  @FunctionalInterface
  protected interface Params {
    void fill(PreparedStatement ps) throws SQLException;
  }

  @Override
  public T getById(int id) throws SQLException {
    String sql = "SELECT * FROM " + getTableName() + " WHERE id = ?";

    try (PreparedStatement ps = Session.getDatabaseConnection().prepareStatement(sql)) {
      ps.setInt(1, id);

      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? map(rs) : null;
      }
    }
  }

  @Override
  public List<T> getAll() throws SQLException {
    String sql = "SELECT * FROM " + getTableName();
    List<T> list = new ArrayList<>();

    try (Statement stmt = Session.getDatabaseConnection().createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        list.add(map(rs));
      }
    }

    return list;
  }

  protected int executeInsert(String sql, Params params) throws SQLException {
    try (PreparedStatement ps = Session.getDatabaseConnection()
            .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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

  protected void executeUpdate(String sql, Params params) throws SQLException {
    try (PreparedStatement ps = Session.getDatabaseConnection().prepareStatement(sql)) {
      params.fill(ps);
      ps.executeUpdate();
    }
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
