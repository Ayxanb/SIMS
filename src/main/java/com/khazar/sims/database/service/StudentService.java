package com.khazar.sims.database.service;

import com.khazar.sims.core.data.Attendance;
import com.khazar.sims.core.data.Enrollment;
import com.khazar.sims.database.table.AttendanceTable;
import com.khazar.sims.database.table.EnrollmentTable;

import java.util.List;
import java.util.stream.Collectors;

import java.sql.SQLException;

/**
 * Provides data access methods for student-related operations.
 * 
 * Acts as a service layer between UI/controllers and the database tables.
 * Responsible for retrieving enrollments, attendance records, and
 * other student-related information.
 * 
 */
public class StudentService {

  /**
   * Returns all course enrollments for a specific student.
   *
   * @param studentId ID of the student
   * @return List of Enrollment objects
   * @throws SQLException if a database access error occurs
   */
  public static List<Enrollment> getEnrollments(int studentId) throws SQLException {
    return EnrollmentTable.getAll().stream()
      .filter(e -> e.getStudentId() == studentId)
      .collect(Collectors.toList());
  }

  /**
   * Returns all attendance records for a specific student.
   *
   * @param studentId ID of the student
   * @return List of Attendance objects
   * @throws SQLException if a database access error occurs
   */
  public static List<Attendance> getAttendance(int studentId) throws SQLException {
    return AttendanceTable.getForStudent(studentId);
  }
}
