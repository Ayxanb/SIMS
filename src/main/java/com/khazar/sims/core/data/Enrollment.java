package com.khazar.sims.core.data;

public class Enrollment {
  private int id;
  private int studentId;
  private int courseId;
  private Double grade;

  public Enrollment(int id, int studentId, int courseId, Double grade) {
    this.id = id;
    this.studentId = studentId;
    this.courseId = courseId;
    this.grade = grade;
  }

  public int getId()                        { return id; }
  public void setId(int id)                 { this.id = id; }

  public int getStudentId()                 { return studentId; }
  public void setStudentId(int studentId)   { this.studentId = studentId; }

  public int getCourseId()                  { return courseId; }
  public void setCourseId(int courseId)     { this.courseId = courseId; }

  public Double getGrade()                  { return grade; }
  public void setGrade(Double grade)        { this.grade = grade; }
}