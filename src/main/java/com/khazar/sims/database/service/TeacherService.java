package com.khazar.sims.database.service;

import com.khazar.sims.core.data.Course;
import com.khazar.sims.core.data.Attendance;
import com.khazar.sims.core.data.Enrollment;

import com.khazar.sims.database.table.CourseTable;
import com.khazar.sims.database.table.AttendanceTable;
import com.khazar.sims.database.table.EnrollmentTable;

import java.sql.SQLException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TeacherService provides convenient access to teacher-related operations,
 * such as retrieving courses, students, and attendance records.
 */
public class TeacherService {

  /**
   * Retrieves all courses taught by a given teacher.
   *
   * @param teacherId the teacher's ID
   * @return list of Course objects
   * @throws SQLException if a database access error occurs
   */
  public static List<Course> getCourses(int teacherId) throws SQLException {
    return CourseTable.getCoursesForTeacher(teacherId);
  }

  /**
   * Retrieves all students enrolled in a given course.
   * 
   * Note: This currently fetches all enrollments from the database and filters in-memory.
   * For large datasets, consider implementing a direct query in {@link EnrollmentTable}.
   *
   * @param courseId the course ID
   * @return list of Enrollment objects for that course
   * @throws SQLException if a database access error occurs
   */
  public static List<Enrollment> getStudentsForCourse(int courseId) throws SQLException {
    return EnrollmentTable.getAll().stream()
        .filter(e -> e.getCourseId() == courseId)
        .collect(Collectors.toList());
  }

  /**
   * Retrieves all attendance records for a given course.
   *
   * @param courseId the course ID
   * @return list of Attendance objects for that course
   * @throws SQLException if a database access error occurs
   */
  public static List<Attendance> getAttendanceForCourse(int courseId) throws SQLException {
    return AttendanceTable.getForCourse(courseId);
  }
}
