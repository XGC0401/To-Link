package com.feature.neighbourHood_backend.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.feature.neighbourHood_backend.model.CustomUserDetails;
import com.feature.neighbourHood_backend.model.DTO.ApiResponse;
import com.feature.neighbourHood_backend.model.DTO.LoginRequestDTO;
import com.feature.neighbourHood_backend.model.DTO.RegisterRequestDTO;
import com.feature.neighbourHood_backend.service.UserService;
import com.feature.neighbourHood_backend.util.BusinessException;
import com.feature.neighbourHood_backend.util.ErrorCode;
import com.feature.neighbourHood_backend.util.jwtUtil;

@RestController
@RequestMapping("/api")
public class LoginController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public LoginController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody LoginRequestDTO dto) {
        try {
            // Validate inputs
            if (dto.getEmail() == null || dto.getEmail().isBlank()) {
                throw new BusinessException(ErrorCode.VALIDATION_EMAIL_INVALID, "Email is required");
            }

            if (dto.getPassword() == null || dto.getPassword().isBlank()) {
                throw new BusinessException(ErrorCode.VALIDATION_PASSWORD_WEAK, "Password is required");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String token = jwtUtil.createToken(userDetails);

            return new ApiResponse<>(ErrorCode.SUCCESS, true, token, "Login successful");
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS, "Invalid email or password");
        }
    }

    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody RegisterRequestDTO request) {
        try {
            // Validate that all required fields are present
            if (request.getName() == null || request.getName().isBlank() ||
                    request.getEmail() == null || request.getEmail().isBlank() ||
                    request.getPassword() == null || request.getPassword().isBlank()) {
                throw new BusinessException(ErrorCode.AUTH_MISSING_REQUIRED_FIELDS,
                        "Name, email, and password are required");
            }

            Integer age = null;
            if (request.getAge() != null && !request.getAge().isBlank()) {
                try {
                    age = Integer.parseInt(request.getAge());
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.VALIDATION_AGE_INVALID, "Age must be a valid number");
                }
            }

            boolean response = userService.register(
                    request.getName(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getHkid(),
                    age,
                    request.getAddress1(),
                    request.getAddress2(),
                    request.getAddress3(),
                    request.getPhone(),
                    request.getStatus()
            );

            if (response) {
                return new ApiResponse<>(ErrorCode.SUCCESS, true, null, "Registration successful");
            } else {
                return new ApiResponse<>(ErrorCode.AUTH_ROLE_NOT_FOUND, false, null,
                        "Registration failed: Role not found");
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Registration failed: " + ex.getMessage());
        }
    }
}
