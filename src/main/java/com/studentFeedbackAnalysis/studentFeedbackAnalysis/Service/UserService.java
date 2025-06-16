package com.studentFeedbackAnalysis.studentFeedbackAnalysis.Service;

import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Dto.*;
import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Model.*;
import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Repo.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    @Autowired
    AuthenticationManager authManager;

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private StudentRepo studentRepo;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private TeacherRepo teacherRepo;

    @Autowired
    private AdminRepo adminRepo;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public Map<String, String> login(UserLoginDto userLoginDto) {
        User user = userRepo.findByEmail(userLoginDto.getEmail());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Check if the role matches
        if (!user.getRole().getId().equals(userLoginDto.getRole())) {
            throw new RuntimeException("Invalid role for login");
        }

        // Authenticate the user
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDto.getEmail(), userLoginDto.getPasswordHash())
        );

        if (auth.isAuthenticated()) {
            String accessToken = jwtService.generateAccessToken(userLoginDto.getEmail(), user.getRole().getName());
            String refreshToken = jwtService.generateRefreshToken(userLoginDto.getEmail());
            Map<String, String> tokens = new HashMap<>();
            tokens.put("access_token", accessToken);
            tokens.put("refresh_token", refreshToken);
            tokens.put("role", user.getRole().getName()); // Include role in response
            return tokens;
        }
        throw new RuntimeException("Authentication failed");
    }
    public Map<String, String> refreshTokens(String refreshToken) {
        // Validate the refresh token
        String email = jwtService.extractEmail(refreshToken);
        UserDetails userDetails = myUserDetailsService.loadUserByUsername(email);
        if (!jwtService.validateToken(refreshToken, userDetails, "refresh")) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        // Get the user's role
        User user = userRepo.findByEmail(email);
        String role = user.getRole().getName();

        // Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(email, role);
        String newRefreshToken = jwtService.generateRefreshToken(email);

        // Return the new tokens
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", newAccessToken);
        tokens.put("refresh_token", newRefreshToken);
        tokens.put("role", role);
        return tokens;
    }

    public String registerUser(UserRegisterDto userRegisterDto) {
        // Check if the user already exists
        if (userRepo.existsByEmail(userRegisterDto.getEmail())) {
            throw new RuntimeException("User already exists");
        }

        // Create a new user
        User user = new User();
        user.setEmail(userRegisterDto.getEmail());
        user.setPassword(encoder.encode(userRegisterDto.getPasswordHash()));
        user.setFirstName(userRegisterDto.getFirstName());
        user.setLastName(userRegisterDto.getLastName());

        // Assign the role to the user
        Role role = new Role();
        role.setId(userRegisterDto.getRole()); // Ensure the role ID corresponds to "admin"
        user.setRole(role);

        // Save the user in the database
        User savedUser = userRepo.save(user);
        switch (userRegisterDto.getRole()) {
            case 1: //Admin
                createAdmin(savedUser, userRegisterDto);
                break;

            case 2: // Teacher
                createTeacher(savedUser, userRegisterDto);
                break;
            case 3: // Student
                createStudent(savedUser, userRegisterDto);
                break;
            default:
                throw new RuntimeException("Invalid role specified");
        }


        return "User registered successfully";
    }

    private void createAdmin(User user, UserRegisterDto dto) {
        Admin admin = new Admin();
        admin.setUser(user);
        admin.setAdminId(dto.getAdminId() != null ? dto.getAdminId() : "ADM" + System.currentTimeMillis());
        // Set bidirectional relationship
        user.setAdmin(admin);
        adminRepo.save(admin);
    }
    private void createStudent(User user, UserRegisterDto dto) {
        Student student = new Student();
        student.setUser(user);
        student.setStudentId(dto.getStudentId() != null ? dto.getStudentId() : "STD" + System.currentTimeMillis());
        student.setIntakeYear(dto.getIntakeYear());
        student.setProgramme(dto.getProgramme());

        // Set bidirectional relationship
        user.setStudent(student);

        // Handle course enrollments if provided
        if (dto.getEnrolledCourseIds() != null && !dto.getEnrolledCourseIds().isEmpty()) {
            try{
                List<Course> courses = courseRepo.findAllById(dto.getEnrolledCourseIds());
                if (courses.isEmpty()) {
                    throw new RuntimeException("No valid courses found for enrollment");
                }
                student.setEnrolledCourses(courses);
            } catch (Exception e) {
                throw new RuntimeException("Error fetching courses: " + e.getMessage());
            }

        }

        studentRepo.save(student);
    }
    private void createTeacher(User user, UserRegisterDto dto) {
        Teacher teacher = new Teacher();
        teacher.setUser(user);
        teacher.setTeacherId(dto.getTeacherId() != null ? dto.getTeacherId() : "TCH" + System.currentTimeMillis());
        teacher.setDepartment(dto.getDepartment());

        // Set bidirectional relationship
        user.setTeacher(teacher);

        // Handle course assignments if provided
        if (dto.getTeachingCourseIds() != null && !dto.getTeachingCourseIds().isEmpty()) {
            try{
                List<Course> courses = courseRepo.findAllById(dto.getTeachingCourseIds());
                if (courses.isEmpty()) {
                    throw new RuntimeException("No valid courses found for enrollment");
                }
                teacher.setTeachingCourses(courses);
            } catch (Exception e) {
                throw new RuntimeException("Error fetching courses: " + e.getMessage());
            }
            teacherRepo.save(teacher);
        }

    }


    // Delete Users
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // First, handle role-specific cleanup
        if (user.getStudent() != null) {
            Student student = user.getStudent();
            if (student.getEnrolledCourses() != null) {
                // Clear the many-to-many relationships
                student.getEnrolledCourses().clear();
            }
            // Set user to null to break the foreign key reference
            student.setUser(null);
            studentRepo.save(student);
        }

        if (user.getTeacher() != null) {
            Teacher teacher = user.getTeacher();
            if (teacher.getTeachingCourses() != null) {
                // Clear the many-to-many relationships
                teacher.getTeachingCourses().clear();
            }
            // Set user to null to break the foreign key reference
            teacher.setUser(null);
            teacherRepo.save(teacher);
        }

        if (user.getAdmin() != null) {
            Admin admin = user.getAdmin();
            // Set user to null to break the foreign key reference
            admin.setUser(null);
            adminRepo.save(admin);
        }

        // Now delete the user
        userRepo.delete(user);
    }


    // Update Users
    @Transactional
    public String updateUser(UserUpdateDto dto) {
        User user = userRepo.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update basic user information
        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName());
        }

        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName());
        }

        // Update email only if it's changed and not already taken
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (userRepo.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(dto.getEmail());
        }

        // Update password if provided
        if (dto.getPasswordHash() != null && !dto.getPasswordHash().isEmpty()) {
            user.setPasswordHash(encoder.encode(dto.getPasswordHash()));
        }

        // Save the updated user
        userRepo.save(user);

        // Handle role-specific updates
        Integer roleId = user.getRole().getId();

        switch (roleId) {
            case 1: // Admin
                updateAdmin(user, dto);
                break;
            case 2: // Teacher
                updateTeacher(user, dto);
                break;
            case 3: // Student
                updateStudent(user, dto);
                break;
            default:
                throw new RuntimeException("Invalid role ID");
        }

        return "User updated successfully";
    }

    private void updateAdmin(User user, UserUpdateDto dto) {
        if (user.getAdmin() == null) {
            throw new RuntimeException("User is not an admin");
        }

        Admin admin = user.getAdmin();

        if (dto.getAdminId() != null) {
            admin.setAdminId(dto.getAdminId());
        }

        adminRepo.save(admin);
    }

    private void updateTeacher(User user, UserUpdateDto dto) {
        if (user.getTeacher() == null) {
            throw new RuntimeException("User is not a teacher");
        }

        Teacher teacher = user.getTeacher();

        if (dto.getTeacherId() != null) {
            teacher.setTeacherId(dto.getTeacherId());
        }

        if (dto.getDepartment() != null) {
            teacher.setDepartment(dto.getDepartment());
        }

        // Handle course assignments if provided
        if (dto.getTeachingCourseIds() != null) {
            if (teacher.getTeachingCourses() != null) {
                teacher.getTeachingCourses().clear();
            }

            List<Course> courses = courseRepo.findAllById(dto.getTeachingCourseIds());
            if (!courses.isEmpty()) {
                teacher.setTeachingCourses(courses);
            }
        }

        teacherRepo.save(teacher);
    }

    private void updateStudent(User user, UserUpdateDto dto) {
        if (user.getStudent() == null) {
            throw new RuntimeException("User is not a student");
        }

        Student student = user.getStudent();

        if (dto.getStudentId() != null) {
            student.setStudentId(dto.getStudentId());
        }

        if (dto.getIntakeYear() != null) {
            student.setIntakeYear(dto.getIntakeYear());
        }

        if (dto.getProgramme() != null) {
            student.setProgramme(dto.getProgramme());
        }

        // Handle course enrollments if provided
        if (dto.getEnrolledCourseIds() != null) {
            if (student.getEnrolledCourses() != null) {
                student.getEnrolledCourses().clear();
            }

            List<Course> courses = courseRepo.findAllById(dto.getEnrolledCourseIds());
            if (!courses.isEmpty()) {
                student.setEnrolledCourses(courses);
            }
        }

        studentRepo.save(student);
    }


    // Get Users
    // Add these methods to your UserService class

    public UserDetailsDto getUserDetailsByEmail(String email) {
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return convertToUserDetailsDto(user);
    }

    public UserDetailsDto getUserDetailsById(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToUserDetailsDto(user);
    }

    public List<UserDetailsDto> getAllUsers() {
        List<User> users = userRepo.findAll();
        return users.stream()
                .map(this::convertToUserDetailsDto)
                .toList();
    }

    public List<UserDetailsDto> getUsersByRole(Integer roleId) {
        // Create a role with the specified ID
        Role role = new Role();
        role.setId(roleId);

        List<User> users = userRepo.findByRole(role);
        return users.stream()
                .map(this::convertToUserDetailsDto)
                .toList();
    }

    private UserDetailsDto convertToUserDetailsDto(User user) {
        UserDetailsDto dto = new UserDetailsDto();

        // Set basic user information
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setRoleName(user.getRole().getName());
        dto.setCreatedAt(user.getCreatedAt());

        // Set role-specific information
        if (user.getStudent() != null) {
            Student student = user.getStudent();
            dto.setStudentId(student.getStudentId());
            dto.setIntakeYear(student.getIntakeYear());
            dto.setProgramme(student.getProgramme());

            // Set enrolled courses
            if (student.getEnrolledCourses() != null && !student.getEnrolledCourses().isEmpty()) {
                dto.setEnrolledCourses(student.getEnrolledCourses().stream()
                        .map(this::convertToCourseDto)
                        .toList());
            }
        }

        if (user.getTeacher() != null) {
            Teacher teacher = user.getTeacher();
            dto.setTeacherId(teacher.getTeacherId());
            dto.setDepartment(teacher.getDepartment());

            // Set teaching courses
            if (teacher.getTeachingCourses() != null && !teacher.getTeachingCourses().isEmpty()) {
                dto.setTeachingCourses(teacher.getTeachingCourses().stream()
                        .map(this::convertToCourseDto)
                        .toList());
            }
        }

        if (user.getAdmin() != null) {
            Admin admin = user.getAdmin();
            dto.setAdminId(admin.getAdminId());
        }

        return dto;
    }

    private CourseDto convertToCourseDto(Course course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setCourseCode(course.getCourseCode());
        dto.setCourseName(course.getCourseName());
        dto.setDescription(course.getDescription());
        return dto;
    }


}
