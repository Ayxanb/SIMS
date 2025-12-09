package com.khazar.sims.database.data;

public class Course {
  private int id;
  private String code;
  private String name;
  private int credits;
  private int departmentId;

  public Course(int id, String code, String name, int credits, int departmentId) {
    this.id = id;
    this.code = code;
    this.name = name;
    this.credits = credits;
    this.departmentId = departmentId;
  }

  public int getId()                            { return id; }
  public void setId(int id)                     { this.id = id; }

  public String getCode()                       { return code; }
  public void setCode(String code)              { this.code = code; }

  public String getName()                       { return name; }
  public void setName(String name)              { this.name = name; }

  public int getCredits()                       { return credits; }
  public void setCredits(int credits)           { this.credits = credits; }

  public int getDepartmentId()                  { return departmentId; }
  public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

}