package com.studentFeedbackAnalysis.studentFeedbackAnalysis.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CourseDto {


    private Long id;

    @NotBlank(message = "Course code cannot be empty")
    @Size(min = 2, max = 10, message = "Course code must be between 2 and 10 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Course code should contain only uppercase letters and numbers")
    private String courseCode;

    @NotBlank(message = "Course name cannot be empty")
    @Size(max = 100, message = "Course name cannot exceed 100 characters")
    private String courseName;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}