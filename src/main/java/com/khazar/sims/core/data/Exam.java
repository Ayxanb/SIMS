package com.khazar.sims.core.data;

import java.util.Date;

public class Exam {
  private int id;
  private int courseId;
  private String name;
  private double maxScore;
  private Date date;

  public Exam(int id, int courseId, String name, double maxScore, Date date) {
    this.id = id;
    this.courseId = courseId;
    this.name = name;
    this.maxScore = maxScore;
    this.date = date;
  }

  public int getId()                        { return id; }
  public void setId(int id)                 { this.id = id; }

  public int getCourseId()                  { return courseId; }
  public void setCourseId(int courseId)     { this.courseId = courseId; }

  public String getName()                   { return name; }
  public void setName(String name)          { this.name = name; }

  public double getMaxScore()               { return maxScore; }
  public void setMaxScore(double maxScore)  { this.maxScore = maxScore; }

  public Date getExamDate()                 { return date; }
  public void setExamDate(Date date)        { this.date = date; }

}
