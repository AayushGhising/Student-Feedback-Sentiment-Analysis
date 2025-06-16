package com.studentFeedbackAnalysis.studentFeedbackAnalysis.Dto;

public class UserRegisterDto {
    private String fullName;
    private String email;
    private String passwordHash;
    private Integer role;

    public Integer getRole() {
        return role;
    }
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
