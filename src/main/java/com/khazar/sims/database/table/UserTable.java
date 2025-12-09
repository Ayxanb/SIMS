package com.khazar.sims.database.table;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.User;


public class UserTable extends BaseTable<User> {
  @Override
  protected String getTableName() { return "users"; }

  @Override
  protected User map(ResultSet rs) throws SQLException {
    User user = new User(
      rs.getInt("id"),
      rs.getString("role"),
      rs.getString("first_name"),
      rs.getString("last_name"),
      rs.getString("email"),
      rs.getString("password"),
      rs.getDate("date_of_birth")
    );
    // Map additional fields used by the UserManagement controller
    user.setIsActive(rs.getBoolean("is_active"));
    // Timestamp lastLoginTs = rs.getTimestamp("last_login");
    // if (lastLoginTs != null) {
    //     user.setLastLogin(new java.util.Date(lastLoginTs.getTime()));
    // }
    return user;
  }
  
  protected void setId(User user, int id) { user.setId(id); }
  
  /* These fill methods are no longer used but are kept for context of the original file structure */
  protected void fillInsertStatement(PreparedStatement ps, User user) throws SQLException {
    ps.setString(1, user.getFirstName());
    ps.setString(2, user.getLastName());
    ps.setString(3, user.getEmail());
    ps.setString(4, user.getPassword());
    ps.setString(5, user.getRole());
    ps.setDate(6, new Date(user.getDateOfBirth().getTime()));
  }

  protected void fillUpdateStatement(PreparedStatement ps, User user) throws SQLException {
    /*
     * The order of parameters must match the SQL UPDATE statement's SET clause
     * Note: The ID is filled separately in the executeUpdate call for the WHERE clause
     */
    ps.setString(1, user.getFirstName());
    ps.setString(2, user.getLastName());
    ps.setString(3, user.getEmail());
    ps.setString(4, user.getPassword());
    ps.setString(5, user.getRole());
    ps.setDate(6, new Date(user.getDateOfBirth().getTime()));
  }

  /**
   * Adds a new user record to the database, including the password (which should be hashed).
   * This is the method used by the UserManagement controller.
   * @param user The user DTO containing all fields except ID.
   * @param password The plaintext or hashed password for the new user.
   * @return The User DTO with the generated ID.
   */
  public User add(User user, String password) throws SQLException {
    /*  In a real application, 'password' must be hashed before insertion here. */
    String sql = "INSERT INTO users(first_name, last_name, email, password, role, date_of_birth, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";
    int id = executeInsert(
      sql,
      ps -> {
        ps.setString(1, user.getFirstName());
        ps.setString(2, user.getLastName());
        ps.setString(3, user.getEmail());
        ps.setString(4, password); /* Use the provided password */
        ps.setString(5, user.getRole());
        
        // Handle potentially missing date_of_birth from UserManagement form
        ps.setDate(6, user.getDateOfBirth() != null ? new java.sql.Date(user.getDateOfBirth().getTime()) : null); 
        ps.setBoolean(7, user.isActive());
      }
    );
    user.setId(id);
    return user;
  }
  
  /* The original add(User) is replaced by the more secure add(User, String) overload */
  @Override
  public User add(User user) throws SQLException {
    throw new UnsupportedOperationException("Use UserTable.add(User user, String password) for user creation.");
  }

  /**
   * Performs a general update on user details, excluding sensitive fields like password
   * and date of birth (as they are not managed by the form or are handled separately).
   * Also updates the is_active state.
   * @param user The user DTO containing updated fields.
   */
  @Override
  public void update(User user) throws SQLException {
    // Exclude password and date_of_birth from this general update, include is_active
    executeUpdate(
      "UPDATE users SET first_name=?, last_name=?, email=?, role=?, is_active=? WHERE id=?",
      ps -> {
        ps.setString(1, user.getFirstName());
        ps.setString(2, user.getLastName());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getRole());
        ps.setBoolean(5, user.isActive()); // To update status
        ps.setInt(6, user.getId());
      }
    );
  }

  /**
   * Updates only the password field for a specific user ID.
   * This is the method requested and referenced in UserManagement.
   * @param userId The ID of the user to update.
   * @param newPassword The new password (should be hashed before call).
   */
  public void updatePassword(int userId, String newPassword) throws SQLException {
    // NOTE: In a production application, 'newPassword' must be hashed before execution.
    String sql = "UPDATE users SET password = ? WHERE id = ?";
    
    executeUpdate(
      sql,
      ps -> {
        ps.setString(1, newPassword); 
        ps.setInt(2, userId);
      }
    );
  }

  public User getByEmail(String email) throws SQLException {
    String sql = "SELECT * FROM users WHERE email = ?";
    PreparedStatement ps = Session.getDatabaseConnection().prepareStatement(sql);
    ps.setString(1, email);
    ResultSet rs = ps.executeQuery();
    return rs.next() ? map(rs) : null;
  }
}