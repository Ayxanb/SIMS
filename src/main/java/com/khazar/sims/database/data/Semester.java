package com.khazar.sims.database.data;

import java.sql.Date;

public class Semester {
  private int id;
  private String name;
  private Date startDate;
  private Date endDate;

  public Semester(String name, Date startDate, Date endDate) {
    this.name = name;
    this.startDate = startDate;
    this.endDate = endDate;
  }
  public Semester(int id, String name, Date startDate, Date endDate) {
    this(name, startDate, endDate);
    this.id = id;
  }

  public int getId() { return id; }
  public void setId(int id) { this.id = id; }
  
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public Date getStartDate() { return startDate; }
  public void setStartDate(Date startDate) { this.startDate = startDate; }

  public Date getEndDate() { return endDate; }
  public void setEndDate(Date endDate) { this.endDate = endDate; }
}