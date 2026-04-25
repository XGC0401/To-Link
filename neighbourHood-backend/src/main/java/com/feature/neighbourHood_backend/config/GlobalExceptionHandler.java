package com.feature.neighbourHood_backend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.feature.neighbourHood_backend.model.DTO.ApiResponse;
import com.feature.neighbourHood_backend.util.BusinessException;
import com.feature.neighbourHood_backend.util.ErrorCode;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException ex) {
        ApiResponse<Object> response = new ApiResponse<>(ex.getCode(), false, null, ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(BadCredentialsException ex) {
        ApiResponse<Object> response = new ApiResponse<>(ErrorCode.AUTH_INVALID_CREDENTIALS, false, null,
                "Invalid email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        ApiResponse<Object> response = new ApiResponse<>(ErrorCode.AUTH_USER_NOT_FOUND, false, null,
                "User not found");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ApiResponse<Object>> handleInternalAuthenticationServiceException(
            InternalAuthenticationServiceException ex) {
        ApiResponse<Object> response = new ApiResponse<>(ErrorCode.AUTH_INVALID_CREDENTIALS, false, null,
                "Invalid email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        ApiResponse<Object> response = new ApiResponse<>(ErrorCode.AUTH_INVALID_CREDENTIALS, false, null,
                "Authentication failed");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        String code = ErrorCode.VALIDATION_EMAIL_INVALID;
        if (ex.getMessage().contains("Email")) {
            if (ex.getMessage().contains("already in use")) {
                code = ErrorCode.AUTH_EMAIL_ALREADY_EXISTS;
            } else {
                code = ErrorCode.VALIDATION_EMAIL_INVALID;
            }
        }
        ApiResponse<Object> response = new ApiResponse<>(code, false, null, ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception ex) {
        ex.printStackTrace();
        ApiResponse<Object> response = new ApiResponse<>(ErrorCode.INTERNAL_SERVER_ERROR, false, null,
                "An internal server error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
