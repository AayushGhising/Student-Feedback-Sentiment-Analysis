package com.studentFeedbackAnalysis.studentFeedbackAnalysis.Controller;

import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Dto.CourseDto;
import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Dto.StandardResponse;
import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Service.CourseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    // GET all courses - accessible to all authenticated users
    @GetMapping
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<StandardResponse<List<CourseDto>>> getAllCourses() {
        List<CourseDto> courses = courseService.getAllCourses();
        StandardResponse<List<CourseDto>> response = new StandardResponse<>(
                HttpStatus.OK.value(),
                "Courses retrieved successfully",
                courses
        );
        return ResponseEntity.ok(response);
    }

    // GET course by ID - accessible to all authenticated users
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<StandardResponse<CourseDto>> getCourseById(@PathVariable Long id) {
        CourseDto course = courseService.getCourseById(id);
        StandardResponse<CourseDto> response = new StandardResponse<>(
                HttpStatus.OK.value(),
                "Course retrieved successfully",
                course
        );
        return ResponseEntity.ok(response);
    }

    // POST create new course - only for Admin
    @PostMapping
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<StandardResponse<CourseDto>> createCourse(@Valid @RequestBody CourseDto courseDto,
                                                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            StandardResponse<CourseDto> response = new StandardResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    errorMessage,
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }

        CourseDto createdCourse = courseService.createCourse(courseDto);
        StandardResponse<CourseDto> response = new StandardResponse<>(
                HttpStatus.CREATED.value(),
                "Course created successfully",
                createdCourse
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT update course - only for Admin
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<StandardResponse<CourseDto>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseDto courseDto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            StandardResponse<CourseDto> response = new StandardResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    errorMessage,
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }

        CourseDto updatedCourse = courseService.updateCourse(id, courseDto);
        StandardResponse<CourseDto> response = new StandardResponse<>(
                HttpStatus.OK.value(),
                "Course updated successfully",
                updatedCourse
        );
        return ResponseEntity.ok(response);
    }

    // DELETE course - only for Admin
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<StandardResponse<Void>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        StandardResponse<Void> response = new StandardResponse<>(
                HttpStatus.OK.value(),
                "Course deleted successfully",
                null
        );
        return ResponseEntity.ok(response);
    }
}