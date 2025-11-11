package com.khazar.sims.database.table;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.khazar.sims.core.Session;
import com.khazar.sims.core.data.User.Role;
import com.khazar.sims.core.data.User;

/**
 * UserTable provides static methods to interact with the `users` table in the database.
 * Supports CRUD operations and mapping between ResultSet and User objects.
 */
public class UserTable {
  /* ---------- Retrieve a user by ID ---------- */
  public static User getById(int id) throws SQLException {
    String sql = "SELECT * FROM users WHERE id = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setInt(1, id);
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) return mapUser(rs);
      }
    }
    return null;
  }

  /* ---------- Retrieve a user by email ---------- */
  public static User getByEmail(String email) throws SQLException {
    String sql = "SELECT * FROM users WHERE email = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setString(1, email);
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) return mapUser(rs);
      }
    }
    return null;
  }

  /* ---------- Add a new user ---------- */
  public static void add(User user) throws SQLException {
    final String sql = """
      INSERT INTO users(first_name, last_name, email, password_hash, role, phone, address, date_of_birth)
      VALUES(?,?,?,?,?,?,?,?)
      """;
    try (PreparedStatement statement = Session.getDatabaseConnection()
        .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, user.getFirstName());
      statement.setString(2, user.getLastName());
      statement.setString(3, user.getEmail());
      statement.setInt(4, user.getPasswordHash());
      statement.setString(5, user.getRole().name());
      statement.setString(6, user.getPhone());
      statement.setString(7, user.getAddress());
      statement.setDate(8, Date.valueOf(user.getDateOfBirth()));
      statement.executeUpdate();

      // Optionally set generated ID back to the user object
      try (ResultSet rs = statement.getGeneratedKeys()) {
        if (rs.next()) user.setId(rs.getInt(1));
      }
    }
  }

  /* ---------- Retrieve all users ---------- */
  public static List<User> getAll() throws SQLException {
    List<User> users = new ArrayList<>();
    final String sql = "SELECT id, role, first_name, last_name, email, password_hash, phone, address, date_of_birth FROM users";

    try (Statement statement = Session.getDatabaseConnection().createStatement();
         ResultSet rs = statement.executeQuery(sql)) {
      while (rs.next()) {
        users.add(mapUser(rs));
      }
    }
    return users;
  }

  /* ---------- Map a ResultSet row to a User object ---------- */
  public static User mapUser(ResultSet rs) throws SQLException {
    return new User(
      rs.getInt("id"),
      User.Role.valueOf(rs.getString("role")),
      rs.getString("first_name"),
      rs.getString("last_name"),
      rs.getString("email"),
      rs.getInt("password_hash"),
      rs.getString("phone"),
      rs.getString("address"),
      rs.getDate("date_of_birth").toLocalDate()
    );
  }
}
