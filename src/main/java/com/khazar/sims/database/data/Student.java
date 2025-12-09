package com.khazar.sims.database.data;

public class Student {
  private int userId;
  private int enrollmentYear;
  private int departmentId;
  private Float gpa;

  public Student(int userId, int enrollmentYear, int departmentId, Float gpa) {
    this.userId = userId;
    this.enrollmentYear = enrollmentYear;
    this.departmentId = departmentId;
    this.gpa = gpa;
  }

  public int getUserId() { return userId; }
  public void setUserId(int userId) { this.userId = userId; }

  public int getEnrollmentYear() { return enrollmentYear; }
  public void setEnrollmentYear(int enrollmentYear) { this.enrollmentYear = enrollmentYear; }

  public int getDepartmentId() { return departmentId; }
  public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

  public Float getGpa() { return gpa; }
  public void setGpa(Float gpa) { this.gpa = gpa; }
}
