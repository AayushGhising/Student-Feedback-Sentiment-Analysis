package com.studentFeedbackAnalysis.studentFeedbackAnalysis.Controller;

import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Dto.StandardResponse;
import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Dto.UserLoginDto;
import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Dto.UserRegisterDto;
import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Dto.UserUpdateDto;
import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @DeleteMapping("/users/{id}")
    public ResponseEntity<StandardResponse<String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        StandardResponse<String> response = new StandardResponse<>(
                HttpStatus.OK.value(),
                "User deleted successfully",
                null
        );
        return ResponseEntity.ok(response);
    }

//    @PutMapping("/users/{id}")
//    public ResponseEntity<StandardResponse<String>> updateUser(
//            @PathVariable Long id,
//            @RequestBody UserUpdateDto userUpdateDto) {
//        userUpdateDto.setId(id); // Ensure ID is set correctly
//        String result = userService.updateUser(userUpdateDto);
//        StandardResponse<String> response = new StandardResponse<>(
//                HttpStatus.OK.value(),
//                "User updated successfully",
//                result
//        );
//        return ResponseEntity.ok(response);
//    }
}