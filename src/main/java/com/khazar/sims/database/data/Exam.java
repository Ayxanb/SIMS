package com.khazar.sims.database.data;

import java.sql.Date;

public class Exam {
  private int offeringId;
  private Date examDate;
  private String type;
  private double maxScore;
  
  public Exam(int offeringId, Date examDate, String type, double maxScore) {
    this.offeringId = offeringId;
    this.examDate = examDate;
    this.type = type;
    this.maxScore = maxScore;
  }

  public Exam(String type, Date examDate, double maxScore) {
    this(1, examDate, type, maxScore);
  }

  public int getCourseOfferingId() { return offeringId; }
  public void setOfferingId(int offeringId) { this.offeringId = offeringId; }
  public Date getExamDate() { return examDate; }
  public void setExamDate(Date examDate) { this.examDate = examDate; }
  public String getType() { return type; }
  public void setType(String type) { this.type = type; }
  public double getMaxScore() { return maxScore; }
  public void setMaxScore(double maxScore) { this.maxScore = maxScore; }
}