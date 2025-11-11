package com.khazar.sims.core.data;

public class Department {
  private int id;
  private String name;
  private Integer headId; /* user ID of department head */

  public Department(int id, String name, int headId) {
    this.id = id;
    this.name = name;
    this.headId = headId;
  }

  public int getId()                    { return id; }
  public void setId(int id)             { this.id = id; }

  public String getName()               { return name; }
  public void setName(String name)      { this.name = name; }

  public Integer getHeadId()            { return headId; }
  public void setHeadId(Integer headId) { this.headId = headId; }
}