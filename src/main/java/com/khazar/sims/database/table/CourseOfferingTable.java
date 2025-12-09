package com.khazar.sims.database.table;

import com.khazar.sims.database.data.CourseOffering;
import com.khazar.sims.core.Session;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.ArrayList;

public class CourseOfferingTable extends BaseTable<CourseOffering> {
  
  @Override
  protected String getTableName() { return "course_offerings"; }

  /**
   * Maps a single row from the ResultSet to a CourseOffering object.
   * This method now expects a JOIN result set containing columns from both
   * 'course_offerings' and 'courses' tables.
   */
  @Override
  protected CourseOffering map(ResultSet rs) throws SQLException {
    // Read columns from course_offerings (normalized)
    int id = rs.getInt("id");
    int courseId = rs.getInt("course_id");
    int teacherId = rs.getInt("teacher_id");
    int semesterId = rs.getInt("semester_id");
    String section = rs.getString("section");
    int capacity = rs.getInt("capacity");
    
    // Read columns from the JOINED courses table (denormalized)
    String courseCode = rs.getString("code"); // 'code' from courses table
    String courseName = rs.getString("name"); // 'name' from courses table
    int credits = rs.getInt("credits");   // 'credits' from courses table
    
    return new CourseOffering(
      id, courseId, teacherId, semesterId, courseCode, courseName, credits, section, capacity
    );
  }

  /**
   * Adds a new CourseOffering record to the database.
   * FIX: Removed course_code, course_name, credits from INSERT SQL.
   */
  @Override
  public CourseOffering add(CourseOffering offering) throws SQLException {
    String sql = "INSERT INTO course_offerings(" +
      "course_id, teacher_id, semester_id, section, capacity) " +
      "VALUES (?, ?, ?, ?, ?)";
    
    int id = executeInsert(
      sql,
      ps -> {
        ps.setInt(1, offering.getCourseId());
        ps.setInt(2, offering.getTeacherId());
        ps.setInt(3, offering.getSemesterId());
        // Removed parameters 4, 5, 6 for course details
        ps.setString(4, offering.getSection());
        ps.setInt(5, offering.getCapacity());
      }
    );
    
    offering.setId(id);
    return offering;
  }

  /**
   * Updates an existing CourseOffering record in the database.
   * FIX: Removed course_code, course_name, credits from UPDATE SQL.
   */
  @Override
  public void update(CourseOffering offering) throws SQLException {
    executeUpdate(
      "UPDATE course_offerings SET course_id=?, teacher_id=?, semester_id=?, section=?, capacity=? WHERE id=?",
      ps -> {
        ps.setInt(1, offering.getCourseId());
        ps.setInt(2, offering.getTeacherId());
        ps.setInt(3, offering.getSemesterId());
        ps.setString(4, offering.getSection());
        ps.setInt(5, offering.getCapacity());
        ps.setInt(6, offering.getId());
      }
    );
  }

  /**
   * Retrieves all CourseOfferings associated with a specific teacher ID.
   * @param teacherId The ID of the teacher.
   * @return A list of CourseOffering objects.
   */
  public List<CourseOffering> getByTeacherId(int teacherId) throws SQLException {
    String sql = "SELECT co.*, c.code, c.name, c.credits " +
           "FROM course_offerings co " +
           "INNER JOIN courses c ON co.course_id = c.id " +
           "WHERE co.teacher_id = ?";
           
    List<CourseOffering> list = new ArrayList<>();
    try (PreparedStatement ps = Session.getDatabaseConnection().prepareStatement(sql)) {
      ps.setInt(1, teacherId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(map(rs));
        }
      }
    }
    return list;
  }
}