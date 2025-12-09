package com.khazar.sims.database.data;

public class Grade {
  private int studentId;
  private int offeringId; /* Corresponds to course_offerings(id) */
  private String assessmentName;
  private int score;
  private int maxScore;
  private String dateSubmitted;

  public Grade(int studentId, int offeringId, String assessmentName, int score, int maxScore, String dateSubmitted) {
    this.studentId = studentId;
    this.offeringId = offeringId;
    this.assessmentName = assessmentName;
    this.score = score;
    this.maxScore = maxScore;
    this.dateSubmitted = dateSubmitted;
  }

  public int getStudentId() { return studentId; }
  public void setStudentId(int studentId) { this.studentId = studentId; }

  public int getCourseOfferingId() { return offeringId; }
  public void setOfferingId(int offeringId) { this.offeringId = offeringId; }

  public String getAssessmentName() { return assessmentName; }
  public void setAssessmentName(String assessmentName) { this.assessmentName = assessmentName; }

  public int getScore() { return score; }
  public void setScore(int score) { this.score = score; }

  public int getMaxScore() { return maxScore; }
  public void setMaxScore(int maxScore) { this.maxScore = maxScore; }

  public String getDateSubmitted() { return dateSubmitted; }
  public void setDateSubmitted(String dateSubmitted) { this.dateSubmitted = dateSubmitted; }
}