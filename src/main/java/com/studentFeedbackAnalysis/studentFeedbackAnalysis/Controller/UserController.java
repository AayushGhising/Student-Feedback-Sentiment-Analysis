package com.studentFeedbackAnalysis.studentFeedbackAnalysis.Controller;

import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Dto.*;
import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<StandardResponse<Map<String, String>>> login(@RequestBody UserLoginDto userLoginDto) {
        Map<String, String> tokens = userService.login(userLoginDto);
        StandardResponse<Map<String, String>> response = new StandardResponse<>(
                HttpStatus.OK.value(),
                "Login successful",
                tokens
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<StandardResponse<Map<String, String>>> refreshTokens(@RequestBody Map<String, String> requestBody) {
        String refreshToken = requestBody.get("refreshToken");
        Map<String, String> tokens = userService.refreshTokens(refreshToken);
        StandardResponse<Map<String, String>> response = new StandardResponse<>(
                HttpStatus.OK.value(),
                "Tokens refreshed successfully",
                tokens
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<StandardResponse<String>> registerUser(@RequestBody UserRegisterDto userRegisterDto) {
        String result = userService.registerUser(userRegisterDto);
        StandardResponse<String> response = new StandardResponse<>(
                HttpStatus.CREATED.value(),
                "User registered successfully",
                result
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<StandardResponse<String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        StandardResponse<String> response = new StandardResponse<>(
                HttpStatus.OK.value(),
                "User deleted successfully",
                null
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/updateUser/{id}")
    public ResponseEntity<StandardResponse<String>> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateDto userUpdateDto) {
        userUpdateDto.setId(id); // Ensure ID is set correctly
        String result = userService.updateUser(userUpdateDto);
        StandardResponse<String> response = new StandardResponse<>(
                HttpStatus.OK.value(),
                "User updated successfully",
                result
        );
        return ResponseEntity.ok(response);
    }

    // Get current user's details
    @GetMapping("/users/me")
    public ResponseEntity<StandardResponse<UserDetailsDto>> getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        UserDetailsDto userDetails = userService.getUserDetailsByEmail(email);

        StandardResponse<UserDetailsDto> response = new StandardResponse<>(
                HttpStatus.OK.value(),
                "User details retrieved successfully",
                userDetails
        );
        return ResponseEntity.ok(response);
    }

    // Admin: Get a specific user's details
    @GetMapping("/users/{id}")
    public ResponseEntity<StandardResponse<UserDetailsDto>> getUserDetailsById(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("Admin"))) {
            StandardResponse<UserDetailsDto> errorResponse = new StandardResponse<>(
                    HttpStatus.FORBIDDEN.value(),
                    "Access denied. Only admins can view other users' details",
                    null
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        UserDetailsDto userDetails = userService.getUserDetailsById(id);

        StandardResponse<UserDetailsDto> response = new StandardResponse<>(
                HttpStatus.OK.value(),
                "User details retrieved successfully",
                userDetails
        );
        return ResponseEntity.ok(response);
    }

    // Admin: Get all users
    @GetMapping("/users")
    public ResponseEntity<StandardResponse<List<UserDetailsDto>>> getAllUsers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("Admin"))) {
            StandardResponse<List<UserDetailsDto>> errorResponse = new StandardResponse<>(
                    HttpStatus.FORBIDDEN.value(),
                    "Access denied. Only admins can view all users",
                    null
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        List<UserDetailsDto> allUsers = userService.getAllUsers();

        StandardResponse<List<UserDetailsDto>> response = new StandardResponse<>(
                HttpStatus.OK.value(),
                "All users retrieved successfully",
                allUsers
        );
        return ResponseEntity.ok(response);
    }

    // Admin: Get all students
    @GetMapping("/users/students")
    public ResponseEntity<StandardResponse<List<UserDetailsDto>>> getAllStudents() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("Admin"))) {
            StandardResponse<List<UserDetailsDto>> errorResponse = new StandardResponse<>(
                    HttpStatus.FORBIDDEN.value(),
                    "Access denied. Only admins can view all students",
                    null
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        List<UserDetailsDto> students = userService.getUsersByRole(3); // 3 is student role ID

        StandardResponse<List<UserDetailsDto>> response = new StandardResponse<>(
                HttpStatus.OK.value(),
                "All students retrieved successfully",
                students
        );
        return ResponseEntity.ok(response);
    }

    // Admin: Get all teachers
    @GetMapping("/users/teachers")
    public ResponseEntity<StandardResponse<List<UserDetailsDto>>> getAllTeachers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("Admin"))) {
            StandardResponse<List<UserDetailsDto>> errorResponse = new StandardResponse<>(
                    HttpStatus.FORBIDDEN.value(),
                    "Access denied. Only admins can view all teachers",
                    null
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        List<UserDetailsDto> teachers = userService.getUsersByRole(2); // 2 is teacher role ID

        StandardResponse<List<UserDetailsDto>> response = new StandardResponse<>(
                HttpStatus.OK.value(),
                "All teachers retrieved successfully",
                teachers
        );
        return ResponseEntity.ok(response);
    }

    // Admin: Get all admins
    @GetMapping("/users/admins")
    public ResponseEntity<StandardResponse<List<UserDetailsDto>>> getAllAdmins() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("Admin"))) {
            StandardResponse<List<UserDetailsDto>> errorResponse = new StandardResponse<>(
                    HttpStatus.FORBIDDEN.value(),
                    "Access denied. Only admins can view all admins",
                    null
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        List<UserDetailsDto> admins = userService.getUsersByRole(1); // 1 is admin role ID

        StandardResponse<List<UserDetailsDto>> response = new StandardResponse<>(
                HttpStatus.OK.value(),
                "All admins retrieved successfully",
                admins
        );
        return ResponseEntity.ok(response);
    }

}