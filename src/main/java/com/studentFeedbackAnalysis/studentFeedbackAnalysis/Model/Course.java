package com.studentFeedbackAnalysis.studentFeedbackAnalysis.Model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_code", unique = true, nullable = false)
    private String courseCode;

    @Column(name = "course_name", nullable = false)
    private String courseName;

    @Column(name = "description")
    private String description;

    @ManyToMany(mappedBy = "enrolledCourses", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Student> students;

    @ManyToMany(mappedBy = "teachingCourses", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Teacher> teachers;
}