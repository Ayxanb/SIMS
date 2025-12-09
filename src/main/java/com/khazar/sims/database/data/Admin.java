package com.khazar.sims.database.data;

public class Admin {
  private int userId;
  private String accessLevel;
  private String officeLocation;

  public Admin(int userId, String accessLevel, String officeLocation) {
    this.userId = userId;
    this.accessLevel = accessLevel;
    this.officeLocation = officeLocation;
  }

  public int getUserId() { return userId; }
  public void setUserId(int userId) { this.userId = userId; }

  public String getAccessLevel() { return accessLevel; }
  public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }

  public String getOfficeLocation() { return officeLocation; }
  public void setOfficeLocation(String officeLocation) { this.officeLocation = officeLocation; }
}
