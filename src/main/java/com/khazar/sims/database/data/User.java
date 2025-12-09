package com.khazar.sims.database.data;

import java.sql.Date;
import java.sql.Timestamp;
public class User {
  private int id;
  private String role;
  private String firstName;
  private String lastName;
  private String email;
  private String password;
  private Date dateOfBirth;
  private boolean isActive;
  private Timestamp lastLogin;

  public User() {}

  public User(String role, String firstName, String lastName, String email, String password, Date dateOfBirth) {
    this.role = role;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
    this.dateOfBirth = dateOfBirth;
    this.isActive = true;
  }
  public User(int id, String role, String firstName, String lastName, String email, String password, Date dateOfBirth) {
    this.id = id;
    this.role = role;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
    this.dateOfBirth = dateOfBirth;
    this.isActive = true;
  }

  public int getId() { return id; }
  public void setId(int id) { this.id = id; }

  public String getRole() { return role; }
  public void setRole(String role) { this.role = role; }

  public String getFirstName() { return firstName; }
  public void setFirstName(String firstName) { this.firstName = firstName; }

  public String getLastName() { return lastName; }
  public void setLastName(String lastName) { this.lastName = lastName; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }

  public Date getDateOfBirth() { return dateOfBirth; }
  public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }

  public void setIsActive(boolean isActive) { this.isActive = isActive; }
  public boolean isActive() { return isActive; }

  public Timestamp getLastLogin() { return lastLogin; }
  public void setLastLogin(Timestamp lastLogin) { this.lastLogin = lastLogin; }
}
