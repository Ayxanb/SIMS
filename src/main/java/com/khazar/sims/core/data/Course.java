package com.khazar.sims.core.data;

public class Course {
  private int id;
  private String code;
  private String name;
  private int departmentId;

  public Course(int id, String code, String name, int departmentId) {
    this.id = id;
    this.code = code;
    this.name = name;
    this.departmentId = departmentId;
  }

  public int getId()                              { return id; }
  public void setId(int id)                       { this.id = id; }

  public String getCode()                         { return code; }
  public void setCode(String code)                { this.code = code; }

  public String getName()                         { return name; }
  public void setName(String name)                { this.name = name; }

  public int getDepartmentId()                    { return departmentId; }
  public void setDepartmentId(int departmentId)   { this.departmentId = departmentId; }

}