package com.studentFeedbackAnalysis.studentFeedbackAnalysis.Service;

import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Dto.UserLoginDto;
import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Dto.UserRegisterDto;
import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Model.Role;
import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Model.User;
import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
        user.setFullName(userRegisterDto.getFullName());

        // Assign the role to the user
        Role role = new Role();
        role.setId(userRegisterDto.getRole()); // Ensure the role ID corresponds to "admin"
        user.setRole(role);

        // Save the user in the database
        userRepo.save(user);

        return "User registered successfully";
    }

}
