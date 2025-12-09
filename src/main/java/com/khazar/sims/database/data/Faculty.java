package com.khazar.sims.database.data;

/**
 * Represents the Faculty entity in the database.
 */
public class Faculty {
  private int id;
  private String name;
  private String code;

  /* used when retrieving data from the database */
  public Faculty(int id, String name, String code) {
    this.id = id;
    this.name = name;
    this.code = code;
  }

  /* used when creating a new faculty (ID will be set after insert) */
  public Faculty(String name, String code) {
    this.name = name;
    this.code = code;
  }

  public int getId() { return id; }
  public String getName() { return name; }
  public String getCode() { return code; }

  public void setId(int id) { this.id = id; }
  public void setName(String name) { this.name = name; }
  public void setCode(String code) { this.code = code; }
}