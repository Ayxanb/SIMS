package com.khazar.sims.database.table;

import com.khazar.sims.core.Session;
import com.khazar.sims.core.data.Schedule;

import java.sql.Time;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.List;
import java.util.ArrayList;

/**
 * ScheduleTable provides static methods to interact with the `schedule` table
 * in the database. Supports CRUD operations and retrieving schedules by course.
 */
public class ScheduleTable {

  /* ---------- Add a new schedule ---------- */
  public static void add(Schedule schedule) throws SQLException {
    final String sql = "INSERT INTO schedule(course_id, day_of_week, start_time, end_time, room) VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement statement = Session.getDatabaseConnection()
        .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setInt(1, schedule.getCourseId());
      statement.setString(2, schedule.getDayOfWeek());
      statement.setTime(3, Time.valueOf(schedule.getStartTime()));
      statement.setTime(4, Time.valueOf(schedule.getEndTime()));
      statement.setString(5, schedule.getRoom());
      statement.executeUpdate();

      try (ResultSet rs = statement.getGeneratedKeys()) {
        if (rs.next()) schedule.setId(rs.getInt(1));
      }
    }
  }

  /* ---------- Update an existing schedule ---------- */
  public static void update(Schedule schedule) throws SQLException {
    final String sql = "UPDATE schedule SET course_id = ?, day_of_week = ?, start_time = ?, end_time = ?, room = ? WHERE id = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setInt(1, schedule.getCourseId());
      statement.setString(2, schedule.getDayOfWeek());
      statement.setTime(3, Time.valueOf(schedule.getStartTime()));
      statement.setTime(4, Time.valueOf(schedule.getEndTime()));
      statement.setString(5, schedule.getRoom());
      statement.setInt(6, schedule.getId());
      statement.executeUpdate();
    }
  }

  /* ---------- Delete a schedule by ID ---------- */
  public static void delete(int id) throws SQLException {
    final String sql = "DELETE FROM schedule WHERE id = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setInt(1, id);
      statement.executeUpdate();
    }
  }

  /* ---------- Retrieve schedules for a specific course ---------- */
  public static List<Schedule> getSchedulesForCourse(int courseId) throws SQLException {
    List<Schedule> list = new ArrayList<>();
    final String sql = "SELECT id, course_id, day_of_week, start_time, end_time, room FROM schedule WHERE course_id = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setInt(1, courseId);
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          list.add(new Schedule(
              rs.getInt("id"),
              rs.getInt("course_id"),
              rs.getString("day_of_week"),
              rs.getTime("start_time").toLocalTime(),
              rs.getTime("end_time").toLocalTime(),
              rs.getString("room")
          ));
        }
      }
    }
    return list;
  }

  /* ---------- Retrieve all schedules ---------- */
  public static List<Schedule> getAllSchedules() throws SQLException {
    List<Schedule> list = new ArrayList<>();
    final String sql = "SELECT id, course_id, day_of_week, start_time, end_time, room FROM schedule";
    try (Statement statement = Session.getDatabaseConnection().createStatement();
         ResultSet rs = statement.executeQuery(sql)) {
      while (rs.next()) {
        list.add(new Schedule(
            rs.getInt("id"),
            rs.getInt("course_id"),
            rs.getString("day_of_week"),
            rs.getTime("start_time").toLocalTime(),
            rs.getTime("end_time").toLocalTime(),
            rs.getString("room")
        ));
      }
    }
    return list;
  }
}
