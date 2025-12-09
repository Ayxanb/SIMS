package com.khazar.sims.database.data;

public class CourseOffering {
  private int id;
  private int courseId;
  private int teacherId;
  private int semesterId;
  
  private String courseCode;
  private String courseName;
  private int credits;
  
  private String section;
  private int capacity;

  /**
   * Constructor for creating a new CourseOffering (no ID).
   * Only fields relevant to the 'course_offerings' table are required initially.
   * Note: courseCode, courseName, credits are passed for convenience/display in the UI model, 
   * but are defined by the Course ID.
   */
  public CourseOffering(int courseId, int teacherId, int semesterId, String courseCode, String courseName, int credits, String section, int capacity) {
    this.courseId = courseId;
    this.teacherId = teacherId;
    this.semesterId = semesterId;
    this.courseCode = courseCode;
    this.courseName = courseName;
    this.credits = credits;
    this.section = section;
    this.capacity = capacity;
  }
  
  /**
   * Constructor for reconstructing a CourseOffering from the database (includes ID and denormalized fields).
   */
  public CourseOffering(int id, int courseId, int teacherId, int semesterId, String courseCode, String courseName, int credits, String section, int capacity) {
    this(courseId, teacherId, semesterId, courseCode, courseName, credits, section, capacity);
    this.id = id;
  }

  public int getId() { return id; }
  public void setId(int id) { this.id = id; }
  public int getCourseId() { return courseId; }
  public void setCourseId(int courseId) { this.courseId = courseId; }
  public int getTeacherId() { return teacherId; }
  public void setTeacherId(int teacherId) { this.teacherId = teacherId; }
  public int getSemesterId() { return semesterId; }
  public void setSemesterId(int semesterId) { this.semesterId = semesterId; }
  public String getCourseCode() { return courseCode; }
  public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
  public String getCourseName() { return courseName; }
  public void setCourseName(String courseName) { this.courseName = courseName; }
  public int getCredits() { return credits; }
  public void setCredits(int credits) { this.credits = credits; }
  public String getSection() { return section; }
  public void setSection(String section) { this.section = section; }
  public int getCapacity() { return capacity; }
  public void setCapacity(int capacity) { this.capacity = capacity; }
}