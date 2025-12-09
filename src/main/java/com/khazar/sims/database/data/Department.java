package com.khazar.sims.database.data;

/**
 * Represents the Department entity, linked to a Faculty.
 */
public class Department {
  private int id;
  private String name;
  private String code;
  private int facultyId;

  public Department(int id, int facultyId, String name, String code) {
    this.id = id;
    this.name = name;
    this.code = code;
    this.facultyId = facultyId;
  }

  public Department(int facultyId, String name, String code) {
    this.name = name;
    this.code = code;
    this.facultyId = facultyId;
  }

  public int getId() { return id; }
  public String getName() { return name; }
  public String getCode() { return code; }
  public int getFacultyId() { return facultyId; }

  public void setId(int id) { this.id = id; }
  public void setName(String name) { this.name = name; }
  public void setCode(String code) { this.code = code; }
  public void setFacultyId(int facultyId) { this.facultyId = facultyId; }
}