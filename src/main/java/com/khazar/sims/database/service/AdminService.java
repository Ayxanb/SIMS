package com.khazar.sims.database.service;

import com.khazar.sims.core.data.Course;
import com.khazar.sims.core.data.Enrollment;
import com.khazar.sims.core.data.User;
import com.khazar.sims.database.table.CourseTable;
import com.khazar.sims.database.table.EnrollmentTable;
import com.khazar.sims.database.table.UserTable;

import java.sql.SQLException;

import java.util.List;

/**
 * Provides administrative operations for managing users, courses,
 * and enrollments in the system.
 *
 * This service acts as a bridge between the database layer (tables)
 * and the business logic layer for administrator-level actions.
 */
public class AdminService {

  /**
   * Adds a new course to the database.
   *
   * @param course the course to add
   * @throws SQLException if a database access error occurs
   */
  public static void addCourse(Course course) throws SQLException {
    CourseTable.add(course);
  }

  /**
   * Registers a new user in the system.
   *
   * @param user the user to register
   * @throws SQLException if a database access error occurs
   */
  public static void registerUser(User user) throws SQLException {
    UserTable.add(user);
  }

  /**
   * Enrolls a student in a specific course.
   *
   * @param enrollment the enrollment record
   * @throws SQLException if a database access error occurs
   */
  public static void enrollStudent(Enrollment enrollment) throws SQLException {
    EnrollmentTable.add(enrollment);
  }

  /**
   * Retrieves all users in the system.
   *
   * @return a list of all users
   * @throws SQLException if a database access error occurs
   */
  public static List<User> getAllUsers() throws SQLException {
    return UserTable.getAll();
  }

  /**
   * Retrieves all available courses.
   *
   * @return a list of all courses
   * @throws SQLException if a database access error occurs
   */
  public static List<Course> getAllCourses() throws SQLException {
    return CourseTable.getAll();
  }

  /**
   * Retrieves all student enrollments across all courses.
   *
   * @return a list of all enrollments
   * @throws SQLException if a database access error occurs
   */
  public static List<Enrollment> getAllEnrollments() throws SQLException {
    return EnrollmentTable.getAll();
  }
}
