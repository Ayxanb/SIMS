package com.khazar.sims.core.data;

import java.time.LocalDate;

public class User {

  public enum Role {
    NONE,
    ADMIN,
    TEACHER,
    STUDENT
  }

  private int id;
  private User.Role role;
  private String firstName;
  private String lastName;
  private String email;
  private int passwordHash;
  private String phone;
  private String address;
  private LocalDate dateOfBirth;

  public User(
    int id, Role role, String firstName, String lastName, String email, int passwordHash, String phone, String address, LocalDate dateOfBirth
  ) {
    this.id = id;
    this.role = role;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.passwordHash= passwordHash;
    this.phone = phone;
    this.address = address;
    this.dateOfBirth = dateOfBirth;
  }

  public int getId()                                { return id; }
  public void setId(int id)                         { this.id = id; }

  public User.Role getRole()                             { return role; }
  public void setRole(User.Role role)                    { this.role = role; }

  public String getFirstName()                      { return firstName; }
  public void setFirstName(String firstName)        { this.firstName = firstName; }

  public String getLastName()                       { return lastName; }
  public void setLastName(String lastName)          { this.lastName = lastName; }

  public String getEmail()                          { return email; }
  public void setEmail(String email)                { this.email = email; }

  public int getPasswordHash()                      { return passwordHash; }
  public void setPasswordHash(int passwordHash)     { this.passwordHash = passwordHash; }

  public String getPhone()                          { return phone; }
  public void setPhone(String phone)                { this.phone = phone; }

  public String getAddress()                        { return address; }
  public void setAddress(String address)            { this.address = address; }

  public LocalDate getDateOfBirth()                 { return dateOfBirth; }
  public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

}
