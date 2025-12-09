package com.khazar.sims.database.data;

public class Program {
  private int id;
  private int departmentId;
  private String name;
  private String code;

  public Program(int id, int departmentId, String name, String code) {
    this.id = id;
    this.departmentId = departmentId;
    this.name = name;
    this.code = code;
  }

  public int getId() { return id; }
  public void setId(int id) { this.id = id; }

  public int getDepartmentId() { return departmentId; }
  public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getCode() { return code; }
  public void setCode(String code) { this.code = code; }
}
