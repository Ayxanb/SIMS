package com.khazar.sims.database.table;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import com.khazar.sims.core.Session;
import com.khazar.sims.core.data.Exam;
import com.khazar.sims.core.data.ExamResult;

import java.util.List;
import java.util.ArrayList;

/**
 * ExamTable provides static methods to interact with the database tables
 * `exams` and `exam_results`. It supports CRUD operations for exams
 * and exam results.
 */
public class ExamTable {

  /* ---------- Exam CRUD Operations ---------- */

  /**
   * Adds a new exam to the database.
   * The generated ID is set on the provided Exam object.
   *
   * @param exam Exam object to add
   * @throws SQLException If a database error occurs
   */
  public static void add(Exam exam) throws SQLException {
    final String sql = "INSERT INTO exams(course_id, name, max_score) VALUES (?, ?, ?)";
    PreparedStatement statement = Session.getDatabaseConnection()
        .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    statement.setInt(1, exam.getCourseId());
    statement.setString(2, exam.getName());
    statement.setDouble(3, exam.getMaxScore());
    statement.executeUpdate();
    ResultSet rs = statement.getGeneratedKeys();
    if (rs.next()) exam.setId(rs.getInt(1));
  }

  /**
   * Retrieves all exams for a specific course.
   *
   * @param courseId ID of the course
   * @return List of exams associated with the course
   * @throws SQLException If a database error occurs
   */
  public static List<Exam> getForCourse(int courseId) throws SQLException {
    List<Exam> list = new ArrayList<>();
    final String sql = "SELECT id, course_id, name, max_score, date FROM exams WHERE course_id = ?";
    PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql);
    statement.setInt(1, courseId);
    ResultSet rs = statement.executeQuery();
    while (rs.next()) {
      list.add(new Exam(
        rs.getInt("id"),
        rs.getInt("course_id"),
        rs.getString("name"),
        rs.getDouble("max_score"),
        rs.getDate("date")
      ));
    }
    return list;
  }

  /**
   * Updates an existing exam in the database.
   *
   * @param exam Exam object with updated fields
   * @throws SQLException If a database error occurs
   */
  public static void update(Exam exam) throws SQLException {
    final String sql = "UPDATE exams SET course_id = ?, name = ?, max_score = ? WHERE id = ?";
    PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql);
    statement.setInt(1, exam.getCourseId());
    statement.setString(2, exam.getName());
    statement.setDouble(3, exam.getMaxScore());
    statement.setInt(4, exam.getId());
    statement.executeUpdate();
  }

  /**
   * Deletes an exam from the database by ID.
   *
   * @param examId ID of the exam to delete
   * @throws SQLException If a database error occurs
   */
  public static void deleteExam(int examId) throws SQLException {
    final String sql = "DELETE FROM exams WHERE id = ?";
    PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql);
    statement.setInt(1, examId);
    statement.executeUpdate();
  }

  /* ---------- ExamResult CRUD Operations ---------- */

  /**
   * Adds a new exam result to the database.
   * The generated ID is set on the provided ExamResult object.
   *
   * @param result ExamResult object to add
   * @throws SQLException If a database error occurs
   */
  public static void addResult(ExamResult result) throws SQLException {
    final String sql = "INSERT INTO exam_results(exam_id, student_id, score) VALUES (?, ?, ?)";
    PreparedStatement statement = Session.getDatabaseConnection()
        .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    statement.setInt(1, result.getExamId());
    statement.setInt(2, result.getStudentId());
    statement.setDouble(3, result.getScore());
    statement.executeUpdate();
    ResultSet rs = statement.getGeneratedKeys();
    if (rs.next()) result.setId(rs.getInt(1));
  }

  /**
   * Retrieves all exam results for a specific student.
   *
   * @param studentId ID of the student
   * @return List of ExamResult objects for the student
   * @throws SQLException If a database error occurs
   */
  public static List<ExamResult> getResultsForStudent(int studentId) throws SQLException {
    List<ExamResult> list = new ArrayList<>();
    final String sql = "SELECT id, exam_id, student_id, score FROM exam_results WHERE student_id = ?";
    PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql);
    statement.setInt(1, studentId);
    ResultSet rs = statement.executeQuery();
    while (rs.next()) {
      list.add(new ExamResult(
        rs.getInt("id"),
        rs.getInt("exam_id"),
        rs.getInt("student_id"),
        rs.getDouble("score")
      ));
    }
    return list;
  }

  /**
   * Updates an existing exam result in the database.
   *
   * @param result ExamResult object with updated fields
   * @throws SQLException If a database error occurs
   */
  public static void updateResult(ExamResult result) throws SQLException {
    final String sql = "UPDATE exam_results SET exam_id = ?, student_id = ?, score = ? WHERE id = ?";
    PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql);
    statement.setInt(1, result.getExamId());
    statement.setInt(2, result.getStudentId());
    statement.setDouble(3, result.getScore());
    statement.setInt(4, result.getId());
    statement.executeUpdate();
  }

  /**
   * Deletes an exam result from the database by ID.
   *
   * @param resultId ID of the exam result to delete
   * @throws SQLException If a database error occurs
   */
  public static void deleteResult(int resultId) throws SQLException {
    final String sql = "DELETE FROM exam_results WHERE id = ?";
    PreparedStatement statement = Session.getDatabaseConnection().prepareStatement(sql);
    statement.setInt(1, resultId);
    statement.executeUpdate();
  }
}
