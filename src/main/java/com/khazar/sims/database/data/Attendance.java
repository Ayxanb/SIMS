package com.khazar.sims.database.data;

public class Attendance {
  private int sessionId;
  private int studentId;
  private boolean present;

  public Attendance(int sessionId, int studentId, boolean present) {
    this.sessionId = sessionId;
    this.studentId = studentId;
    this.present = present;
  }

  public int getSessionId() { return sessionId; }
  public void setSessionId(int sessionId) { this.sessionId = sessionId; }

  public int getStudentId() { return studentId; }
  public void setStudentId(int studentId) { this.studentId = studentId; }

  public boolean isPresent() { return present; }
  public void setPresent(boolean present) { this.present = present; }
}
