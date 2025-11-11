package com.khazar.sims.core.data;

import java.time.LocalDate;

public class Attendance {
  private int studentId;
  private int courseId;
  private LocalDate date;
  private boolean present;

  public Attendance(int studentId, int courseId, LocalDate date, boolean present) {
    this.studentId = studentId;
    this.courseId = courseId;
    this.date = date;
    this.present = present;
  }


  public int getStudentId() { return studentId; }
  public void setStudentId(int studentId) { this.studentId = studentId; }

  public int getCourseId() { return courseId; }
  public void setCourseId(int courseId) { this.courseId = courseId; }

  public LocalDate getDate() { return date; }
  public void setDate(LocalDate date) { this.date = date; }

  public boolean isPresent() { return present; }
  public void setPresent(boolean present) { this.present = present; }
  
  @Override
  public String toString() {
    return "Attendance{studentId=" + studentId + ", courseId=" + courseId + ", date=" + date + ", present=" + present + '}';
  }
}