package com.khazar.sims.database.table;

import com.khazar.sims.core.Session;
import com.khazar.sims.core.data.Attendance;

import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.List;
import java.util.ArrayList;

/**
 * AttendanceTable provides static methods to interact with the `attendance` table
 * in the database. Supports adding, updating, deleting, and retrieving attendance records.
 */
public class AttendanceTable {

  /* ---------- Add a new attendance record ---------- */
  public static void add(Attendance attendance) throws SQLException {
    final String sql = "INSERT INTO attendance(student_id, course_id, date, present) VALUES (?, ?, ?, ?)";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setInt(1, attendance.getStudentId());
      statement.setInt(2, attendance.getCourseId());
      statement.setDate(3, Date.valueOf(attendance.getDate()));
      statement.setBoolean(4, attendance.isPresent());
      statement.executeUpdate();
    }
  }

  /* ---------- Update an existing attendance record ---------- */
  public static void update(Attendance attendance) throws SQLException {
    final String sql = "UPDATE attendance SET present = ? WHERE student_id = ? AND course_id = ? AND date = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setBoolean(1, attendance.isPresent());
      statement.setInt(2, attendance.getStudentId());
      statement.setInt(3, attendance.getCourseId());
      statement.setDate(4, Date.valueOf(attendance.getDate()));
      statement.executeUpdate();
    }
  }

  /* ---------- Delete an attendance record ---------- */
  public static void delete(int studentId, int courseId, LocalDate date) throws SQLException {
    final String sql = "DELETE FROM attendance WHERE student_id = ? AND course_id = ? AND date = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setInt(1, studentId);
      statement.setInt(2, courseId);
      statement.setDate(3, Date.valueOf(date));
      statement.executeUpdate();
    }
  }

  /* ---------- Retrieve all attendance records for a specific student ---------- */
  public static List<Attendance> getForStudent(int studentId) throws SQLException {
    final String sql = "SELECT student_id, course_id, date, present FROM attendance WHERE student_id = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setInt(1, studentId);
      return fetchList(statement);
    }
  }

  /* ---------- Retrieve all attendance records for a specific course ---------- */
  public static List<Attendance> getForCourse(int courseId) throws SQLException {
    final String sql = "SELECT student_id, course_id, date, present FROM attendance WHERE course_id = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setInt(1, courseId);
      return fetchList(statement);
    }
  }

  /* ---------- Retrieve all attendance records for a specific course on a specific date ---------- */
  public static List<Attendance> getForCourseOnDate(int courseId, LocalDate date) throws SQLException {
    final String sql = "SELECT student_id, course_id, date, present FROM attendance WHERE course_id = ? AND date = ?";
    try (PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql)) {
      statement.setInt(1, courseId);
      statement.setDate(2, Date.valueOf(date));
      return fetchList(statement);
    }
  }

  /* ---------- Helper method to map ResultSet to a List of Attendance objects ---------- */
  private static List<Attendance> fetchList(PreparedStatement statement) throws SQLException {
    List<Attendance> list = new ArrayList<>();
    try (ResultSet rs = statement.executeQuery()) {
      while (rs.next()) {
        list.add(new Attendance(
            rs.getInt("student_id"),
            rs.getInt("course_id"),
            rs.getDate("date").toLocalDate(),
            rs.getBoolean("present")
        ));
      }
    }
    return list;
  }
}
