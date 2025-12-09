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
  protected String getTableName() { 
    return "course_offerings"; 
  }

  @Override
  protected CourseOffering map(ResultSet rs) throws SQLException {
    return new CourseOffering(
      rs.getInt("id"),
      rs.getInt("course_id"),
      rs.getInt("teacher_id"),
      rs.getInt("semester_id"),
      null,
      null,
      0,
      rs.getString("section"),
      rs.getInt("capacity")
    );
  }

  private CourseOffering mapWithCourse(ResultSet rs) throws SQLException {
    return new CourseOffering(
      rs.getInt("id"),
      rs.getInt("course_id"),
      rs.getInt("teacher_id"),
      rs.getInt("semester_id"),
      rs.getString("code"),     /* from courses */
      rs.getString("name"),     /* from courses */
      rs.getInt("credits"),     /* from courses */
      rs.getString("section"),
      rs.getInt("capacity")
    );
  }

  @Override
  public CourseOffering add(CourseOffering offering) throws SQLException {
    String sql = """
      INSERT INTO course_offerings 
      (course_id, teacher_id, semester_id, section, capacity)
      VALUES (?, ?, ?, ?, ?)
    """;

    int id = executeInsert(
      sql,
      ps -> {
        ps.setInt(1, offering.getCourseId());
        ps.setInt(2, offering.getTeacherId());
        ps.setInt(3, offering.getSemesterId());
        ps.setString(4, offering.getSection());
        ps.setInt(5, offering.getCapacity());
      }
    );

    offering.setId(id);
    return offering;
  }

  @Override
  public void update(CourseOffering offering) throws SQLException {
    executeUpdate(
      """
        UPDATE course_offerings 
        SET course_id=?, teacher_id=?, semester_id=?, section=?, capacity=? 
        WHERE id=?
      """,
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

  public List<CourseOffering> getByTeacherId(int teacherId) throws SQLException {
    String sql = """
      SELECT co.*, c.code, c.name, c.credits
      FROM course_offerings co
      INNER JOIN courses c ON co.course_id = c.id
      WHERE co.teacher_id = ?
    """;

    List<CourseOffering> list = new ArrayList<>();

    try (PreparedStatement ps = Session.getDatabaseConnection().prepareStatement(sql)) {
      ps.setInt(1, teacherId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(mapWithCourse(rs));
        }
      }
    }

    return list;
  }
}
