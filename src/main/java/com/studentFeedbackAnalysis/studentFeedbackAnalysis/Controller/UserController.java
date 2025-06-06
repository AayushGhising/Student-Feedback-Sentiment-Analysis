package com.studentFeedbackAnalysis.studentFeedbackAnalysis.Controller;

import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Dto.UserDto;
import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Model.User;
import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

//    @GetMapping("/csrf-token")
//    public String getCsrfToken(HttpServletRequest request) {
//        return ((CsrfToken) request.getAttribute(("_csrf")).getToken());
//    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody UserDto userDto){
        return userService.login(userDto);
    }

    @PostMapping("/refresh-token")
    public Map<String, String> refreshTokens(@RequestBody Map<String, String> requestBody) {
        String refreshToken = requestBody.get("refreshToken");
        return userService.refreshTokens(refreshToken);
    }
}
