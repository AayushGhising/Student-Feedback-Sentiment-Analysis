package com.studentFeedbackAnalysis.studentFeedbackAnalysis.Service;

import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Dto.CourseDto;
import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Model.Course;
import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Repo.CourseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseRepo courseRepo;

    // Get all courses
    public List<CourseDto> getAllCourses() {
        return courseRepo.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get course by id
    public CourseDto getCourseById(Long id) {
        Course course = courseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
        return convertToDto(course);
    }

    // Create new course
    @Transactional
    public CourseDto createCourse(CourseDto courseDto) {
        // Check if course code already exists
        if (courseRepo.findBycourseCode(courseDto.getCourseCode()).isPresent()) {
            throw new RuntimeException("Course with code " + courseDto.getCourseCode() + " already exists");
        }

        Course course = convertToEntity(courseDto);
        Course savedCourse = courseRepo.save(course);
        return convertToDto(savedCourse);
    }

    // Update course
    @Transactional
    public CourseDto updateCourse(Long id, CourseDto courseDto) {
        Course existingCourse = courseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));

        // Check if the new course code conflicts with another course
        if (!existingCourse.getCourseCode().equals(courseDto.getCourseCode()) &&
                courseRepo.findBycourseCode(courseDto.getCourseCode()).isPresent()) {
            throw new RuntimeException("Course with code " + courseDto.getCourseCode() + " already exists");
        }

        existingCourse.setCourseCode(courseDto.getCourseCode());
        existingCourse.setCourseName(courseDto.getCourseName());
        existingCourse.setDescription(courseDto.getDescription());

        Course updatedCourse = courseRepo.save(existingCourse);
        return convertToDto(updatedCourse);
    }

    // Delete course
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));

        // Clear relationships before deletion to prevent constraint violations
        if (course.getStudents() != null) {
            course.getStudents().forEach(student ->
                    student.getEnrolledCourses().remove(course));
            course.getStudents().clear();
        }

        if (course.getTeachers() != null) {
            course.getTeachers().forEach(teacher ->
                    teacher.getTeachingCourses().remove(course));
            course.getTeachers().clear();
        }

        courseRepo.delete(course);
    }

    // Helper methods for conversion between Entity and DTO
    private CourseDto convertToDto(Course course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setCourseCode(course.getCourseCode());
        dto.setCourseName(course.getCourseName());
        dto.setDescription(course.getDescription());
        return dto;
    }

    private Course convertToEntity(CourseDto dto) {
        Course course = new Course();
        // Don't set ID for new entities, let the DB assign it
        if (dto.getId() != null) {
            course.setId(dto.getId());
        }
        course.setCourseCode(dto.getCourseCode());
        course.setCourseName(dto.getCourseName());
        course.setDescription(dto.getDescription());
        return course;
    }
}