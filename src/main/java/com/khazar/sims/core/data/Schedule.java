package com.khazar.sims.core.data;

import java.time.LocalTime;

public class Schedule {
  private int id;
  private int courseId;
  private String dayOfWeek;
  private LocalTime startTime;
  private LocalTime endTime;
  private String room;

  public Schedule(int id, int courseId, String dayOfWeek, LocalTime startTime, LocalTime endTime, String room) {
    this.id = id;
    this.courseId = courseId;
    this.dayOfWeek = dayOfWeek;
    this.startTime = startTime;
    this.endTime = endTime;
    this.room = room;
  }

  public int getId()                            { return id; }
  public void setId(int id)                     { this.id = id; }

  public int getCourseId()                      { return courseId; }
  public void setCourseId(int courseId)         { this.courseId = courseId; }

  public String getDayOfWeek()                  { return dayOfWeek; }
  public void setDayOfWeek(String dayOfWeek)    { this.dayOfWeek = dayOfWeek; }

  public LocalTime getStartTime()               { return startTime; }
  public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

  public LocalTime getEndTime()                 { return endTime; }
  public void setEndTime(LocalTime endTime)     { this.endTime = endTime; }
  
  public String getRoom()                       { return room; }
  public void setRoom(String room)              { this.room = room; }

}