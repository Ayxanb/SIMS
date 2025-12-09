package com.khazar.sims.database.data;

public class Teacher {
  private int userId;
  private int facultyId;

  public Teacher(int userId, int facultyId) {
    this.userId = userId;
    this.facultyId = facultyId;
  }

  public int getUserId() { return userId; }
  public void setUserId(int userId) { this.userId = userId; }

  public int getFacultyId() { return facultyId; }
  public void setFacultyId(int facultyId) { this.facultyId = facultyId; }
}
