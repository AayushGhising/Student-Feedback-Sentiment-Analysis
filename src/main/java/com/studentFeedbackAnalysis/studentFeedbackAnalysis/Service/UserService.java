package com.studentFeedbackAnalysis.studentFeedbackAnalysis.Service;

import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Dto.UserDto;
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

    public Map<String, String> login(UserDto userDto) {
        User user = userRepo.findByEmail(userDto.getEmail());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Check if the role matches
        if (!user.getRole().getId().equals(userDto.getRole())) {
            throw new RuntimeException("Invalid role for login");
        }

        // Authenticate the user
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDto.getEmail(), userDto.getPasswordHash())
        );

        if (auth.isAuthenticated()) {
            String accessToken = jwtService.generateAccessToken(userDto.getEmail());
            String refreshToken = jwtService.generateRefreshToken(userDto.getEmail());
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

        // Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(email);
        String newRefreshToken = jwtService.generateRefreshToken(email);

        // Return the new tokens
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", newAccessToken);
        tokens.put("refresh_token", newRefreshToken);
        return tokens;
    }

}
