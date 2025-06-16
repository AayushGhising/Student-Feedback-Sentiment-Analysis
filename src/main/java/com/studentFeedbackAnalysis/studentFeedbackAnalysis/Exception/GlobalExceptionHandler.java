package com.studentFeedbackAnalysis.studentFeedbackAnalysis.Exception;

import com.studentFeedbackAnalysis.studentFeedbackAnalysis.Dto.StandardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<StandardResponse<String>> handleRuntimeException(RuntimeException ex) {
        StandardResponse<String> response = new StandardResponse<>(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<String>> handleGlobalException(Exception ex) {
        StandardResponse<String> response = new StandardResponse<>(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StandardResponse<String>> handleAccessDeniedException(AccessDeniedException ex) {
        StandardResponse<String> response = new StandardResponse<>(
                HttpStatus.FORBIDDEN.value(),
                "You don't have permission to perform this action",
                null
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }


}