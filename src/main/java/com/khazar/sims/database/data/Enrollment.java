package com.khazar.sims.database.data;

/**
 * DTO representing a student enrolled in a course offering.
 * Combines data from enrollments, students, and users.
 * Used for listing students and finalizing their assessments.
 */
public class Enrollment {
  private int offeringId;
  private int studentId;
  
  private String firstName;
  private String lastName;
  
  private Double finalGrade;

  /**
   * Constructor for reconstructing an Enrollment from the database.
   * @param studentId The ID of the student user.
   * @param offeringId The ID of the course offering.
   * @param studentNo The student's unique number.
   * @param firstName Student's first name.
   * @param lastName Student's last name.
   * @param finalGrade Final score achieved (can be null).
   */
  public Enrollment(int studentId, int offeringId, String firstName, String lastName, Double finalGrade) {
    this.studentId = studentId;
    this.offeringId = offeringId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.finalGrade = finalGrade;
  }
  
  public String getFullName() {
    return firstName + " " + lastName;
  }

  public int getStudentId() { return studentId; }
  public void setStudentId(int studentId) { this.studentId = studentId; }

  public int getOfferingId() { return offeringId; }
  public void setOfferingId(int offeringId) { this.offeringId = offeringId; }

  public String getFirstName() { return firstName; }
  public void setFirstName(String firstName) { this.firstName = firstName; }
  
  public String getLastName() { return lastName; }
  public void setLastName(String lastName) { this.lastName = lastName; }
  
  public Double getFinalGrade() { return finalGrade; }
  public void setFinalGrade(Double finalGrade) { this.finalGrade = finalGrade; }
}