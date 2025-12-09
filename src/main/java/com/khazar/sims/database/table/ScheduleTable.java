package com.khazar.sims.database.table;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.Schedule;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ScheduleTable {
  public void add(Schedule schedule) throws SQLException {
    final String sql = """
      INSERT INTO schedules (offering_id, day_of_week, start_time, end_time, room)
      VALUES (?, ?, ?, ?, ?)
    """;
    try (PreparedStatement ps = Session.getDatabaseConnection().prepareStatement(sql)) {
      ps.setInt(1, schedule.getCourseOfferingId());
      ps.setString(2, schedule.getDayOfWeek());
      ps.setTime(3, Time.valueOf(schedule.getStartTime()));
      ps.setTime(4, Time.valueOf(schedule.getEndTime()));
      ps.setString(5, schedule.getRoom());
      ps.executeUpdate();
    }
  }

  public List<Schedule> getSchedulesForOffering(int offeringId) throws SQLException {
    final String sql = """
      SELECT id, offering_id, day_of_week, start_time, end_time, room
      FROM schedules
      WHERE offering_id = ?
    """;
    List<Schedule> list = new ArrayList<>();
    try (PreparedStatement ps = Session.getDatabaseConnection().prepareStatement(sql)) {
      ps.setInt(1, offeringId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(map(rs));
        }
      }
    }
    return list;
  }

  private Schedule map(ResultSet rs) throws SQLException {
    return new Schedule(
        rs.getInt("id"),
        rs.getInt("offering_id"),
        rs.getString("day_of_week"),
        rs.getTime("start_time").toLocalTime(),
        rs.getTime("end_time").toLocalTime(),
        rs.getString("room")
    );
  }

  /**
   * Get the schedule session for a given offering and date.
   * Returns null if no session is scheduled.
   */
  public Schedule getByDate(int offeringId, LocalDate date) throws SQLException {
    String sql = "SELECT id, offering_id, day_of_week, start_time, end_time, room " +
                "FROM schedules WHERE offering_id = ? AND day_of_week = ?";
    Connection conn = Session.getDatabaseConnection();
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      String day = date.getDayOfWeek().name().substring(0, 3);
      ps.setInt(1, offeringId);
      ps.setString(2, day);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return new Schedule(
            rs.getInt("id"),
            rs.getInt("offering_id"),
            rs.getString("day_of_week"),
            rs.getTime("start_time").toLocalTime(),
            rs.getTime("end_time").toLocalTime(),
            rs.getString("room")
          );
        }
      }
    }
    return null;
  }

  /**
   * Get all course offering IDs taught by a teacher.
   */
  public List<Integer> getOfferingsByTeacher(int teacherId) throws SQLException {
    final String sql = """
      SELECT id
      FROM course_offerings
      WHERE teacher_id = ?
    """;
    List<Integer> ids = new ArrayList<>();
    try (PreparedStatement ps = Session.getDatabaseConnection().prepareStatement(sql)) {
      ps.setInt(1, teacherId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) ids.add(rs.getInt("id"));
      }
    }
    return ids;
  }

  /**
   * Get a human-readable course display string for the ComboBox.
   */
  public String getCourseDisplayName(int offeringId) throws SQLException {
    final String sql = """
      SELECT c.code, c.name, co.section
      FROM course_offerings co
      JOIN courses c ON co.course_id = c.id
      WHERE co.id = ?
    """;
    try (PreparedStatement ps = Session.getDatabaseConnection().prepareStatement(sql)) {
      ps.setInt(1, offeringId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return String.format("%s - %s (Section %s)",
              rs.getString("code"),
              rs.getString("name"),
              rs.getString("section"));
        }
      }
    }
    return null;
  }
}
