package com.khazar.sims.database.data;

import java.time.LocalDate;
import java.time.LocalTime;

public class Schedule {
  private int id;
  private int offeringId;
  private String dayOfWeek;
  private LocalDate date;
  private LocalTime startTime;
  private LocalTime endTime;
  private String room;

  public Schedule(int offeringId, String dayOfWeek, LocalDate date, LocalTime startTime, LocalTime endTime, String room) {
    this.offeringId = offeringId;
    this.dayOfWeek = dayOfWeek;
    this.date = date;
    this.startTime = startTime;
    this.endTime = endTime;
    this.room = room;
  }
  
  public Schedule(int id, int offeringId, String dayOfWeek, LocalDate date, LocalTime startTime, LocalTime endTime, String room) {
    this.id = id;
    this.offeringId = offeringId;
    this.dayOfWeek = dayOfWeek;
    this.date = date;
    this.startTime = startTime;
    this.endTime = endTime;
    this.room = room;
  }

  public int getId() { return id; }

  public int getCourseOfferingId() { return offeringId; }
  public void setOfferingId(int offeringId) { this.offeringId = offeringId; }

  public String getDayOfWeek() { return dayOfWeek; }
  public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

  public LocalDate getDate() { return date; }
  public void setDate(LocalDate date) { this.date = date; } 

  public LocalTime getStartTime() { return startTime; }
  public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

  public LocalTime getEndTime() { return endTime; }
  public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

  public String getRoom() { return room; }
  public void setRoom(String room) { this.room = room; }
}