package com.khazar.sims.core.data;

public class Authentication {
  private final int userId;
  private final String email;
  private final int passwordHash;

  public Authentication(int userId, String email, int passwordHash) {
    this.userId = userId;
    this.email = email;
    this.passwordHash = passwordHash;
  }

  public int getUserId() { return userId; }
  public String getEmail() { return email; }
  public int getPasswordHash() { return passwordHash; }
}
