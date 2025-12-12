package com.khazar.sims.database.table;

import com.khazar.sims.core.Session;
import com.khazar.sims.database.data.Attendance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceTable {

  /* ---------- Add or update attendance in batch ---------- */
  public void saveAttendance(List<Attendance> attendanceRecords) throws SQLException {
    if (attendanceRecords == null || attendanceRecords.isEmpty())
      return;
      
    /* * Uses ON DUPLICATE KEY UPDATE to handle both inserts 
     * (if attendance hasn't been taken) and updates (if it has).
     */
    final String sql = """
      INSERT INTO attendances (session_id, student_id, present)
      VALUES (?, ?, ?)
      ON DUPLICATE KEY UPDATE present = VALUES(present)
    """;
    
    Connection conn = Session.getDatabaseConnection();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      for (Attendance a : attendanceRecords) {
        stmt.setInt(1, a.getSessionId());
        stmt.setInt(2, a.getStudentId());
        stmt.setBoolean(3, a.isPresent());
        stmt.addBatch();
      }
      stmt.executeBatch();
    }
  }

  /* ---------- Retrieve attendance for a specific schedule session (used by Teacher) ---------- */
  public List<Attendance> getForSchedule(int sessionId) throws SQLException {
    final String sql = "SELECT session_id, student_id, present FROM attendances WHERE session_id = ?";
    Connection conn = Session.getDatabaseConnection();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, sessionId);
      return fetchList(stmt);
    }
  }

  /* * ---------- Retrieve attendance for a specific student (used by Student) ---------- 
   * Renamed from getForStudent to follow getBy...Id convention.
   */
  public List<Attendance> getByStudentId(int studentId) throws SQLException {
    final String sql = "SELECT session_id, student_id, present FROM attendances WHERE student_id = ?";
    Connection conn = Session.getDatabaseConnection();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, studentId);
      return fetchList(stmt);
    }
  }

  /* ---------- Retrieve a single attendance record by session and student ID ---------- */
  public Attendance get(int sessionId, int studentId) throws SQLException {
    final String sql = "SELECT session_id, student_id, present FROM attendances WHERE session_id = ? AND student_id = ?";
    try (Connection conn = Session.getDatabaseConnection();
      PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, sessionId);
      stmt.setInt(2, studentId);

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return new Attendance(
            rs.getInt("session_id"),
            rs.getInt("student_id"),
            rs.getBoolean("present")
          );
        }
      }
    }
    return null;
  }

  /* ---------- Delete a specific attendance record ---------- */
  public void delete(int sessionId, int studentId) throws SQLException {
    final String sql = "DELETE FROM attendances WHERE session_id = ? AND student_id = ?";
    try (Connection conn = Session.getDatabaseConnection();
      PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, sessionId);
      stmt.setInt(2, studentId);
      stmt.executeUpdate();
    }
  }

  /* ---------- Helper method to map ResultSet to List<Attendance> ---------- */
  private List<Attendance> fetchList(PreparedStatement stmt) throws SQLException {
    List<Attendance> list = new ArrayList<>();
    try (ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        list.add(new Attendance(
          rs.getInt("session_id"),
          rs.getInt("student_id"),
          rs.getBoolean("present")
        ));
      }
    }
    return list;
  }
}