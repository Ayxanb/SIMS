package com.khazar.sims.core.data;

public class ExamResult {
  private int id;
  private int examId;
  private int studentId;
  private double score;

  public ExamResult(int id, int examId, int studentId, double score) {
    this.id = id;
    this.examId = examId;
    this.studentId = studentId;
    this.score = score;
  }

  public int getId()                      { return id; }
  public void setId(int id)               { this.id = id; }
  
  public int getExamId()                  { return examId; }
  public void setExamId(int examId)       { this.examId = examId; }

  public int getStudentId()               { return studentId; }
  public void setStudentId(int studentId) { this.studentId = studentId; }
  
  public double getScore()                { return score; }
  public void setScore(double score)      { this.score = score; }
}
