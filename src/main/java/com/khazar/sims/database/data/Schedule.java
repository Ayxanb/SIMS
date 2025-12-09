package com.khazar.sims.database.data;

import java.time.LocalTime;

public class Schedule {
  private int id;
  private int offeringId;
  private String dayOfWeek;
  private LocalTime startTime;
  private LocalTime endTime;
  private String room;

  public Schedule(int offeringId, String dayOfWeek, LocalTime startTime, LocalTime endTime, String room) {
    this.offeringId = offeringId;
    this.dayOfWeek = dayOfWeek;
    this.startTime = startTime;
    this.endTime = endTime;
    this.room = room;
  }
  
  public Schedule(int id, int offeringId, String dayOfWeek, LocalTime startTime, LocalTime endTime, String room) {
    this.id = id;
    this.offeringId = offeringId;
    this.dayOfWeek = dayOfWeek;
    this.startTime = startTime;
    this.endTime = endTime;
    this.room = room;
  }

  public int getId() { return id; }

  public int getOfferingId() { return offeringId; }
  public void setOfferingId(int offeringId) { this.offeringId = offeringId; }

  public String getDayOfWeek() { return dayOfWeek; }
  public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

  public LocalTime getStartTime() { return startTime; }
  public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

  public LocalTime getEndTime() { return endTime; }
  public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

  public String getRoom() { return room; }
  public void setRoom(String room) { this.room = room; }
}
