package com.khazar.sims.database.data;

import java.sql.Date;

public class ExamResult {
  private int offeringId;
  private int studentId;
  private Date examDate;
  private double score;
  
  private String studentName; 
  private String examType; 
  private double maxScore;

  public ExamResult(int offeringId, int studentId, Date examDate, double score, String studentName, String examType, double maxScore) {
    this.offeringId = offeringId;
    this.studentId = studentId;
    this.examDate = examDate;
    this.score = score;
    this.studentName = studentName;
    this.examType = examType;
    this.maxScore = maxScore;
  }

  public int getCourseOfferingId() { return offeringId; }
  public void setOfferingId(int offeringId) { this.offeringId = offeringId; }

  public int getStudentId() { return studentId; }
  public void setStudentId(int studentId) { this.studentId = studentId; }

  public Date getExamDate() { return examDate; }
  public void setExamDate(Date examDate) { this.examDate = examDate; }

  public double getScore() { return score; }
  public void setScore(double score) { this.score = score; }

  public String getStudentName() { return studentName; }
  public void setStudentName(String studentName) { this.studentName = studentName; }

  public String getExamType() { return examType; }
  public void setExamType(String examType) { this.examType = examType; }
  
  public double getMaxScore() { return maxScore; }
  public void setMaxScore(double maxScore) { this.maxScore = maxScore; }
}