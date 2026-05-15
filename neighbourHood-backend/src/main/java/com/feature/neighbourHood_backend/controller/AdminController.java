package com.feature.neighbourHood_backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.feature.neighbourHood_backend.model.CustomUserDetails;
import com.feature.neighbourHood_backend.model.DTO.ApiResponse;
import com.feature.neighbourHood_backend.model.DTO.BlockUserRequestDTO;
import com.feature.neighbourHood_backend.model.DTO.ChangePasswordRequestDTO;
import com.feature.neighbourHood_backend.model.DTO.PreservedAppStateRequestDTO;
import com.feature.neighbourHood_backend.model.DTO.PreservedAppStateResponseDTO;
import com.feature.neighbourHood_backend.model.DTO.PublicUserProfileDTO;
import com.feature.neighbourHood_backend.model.DTO.UpdateAvatarRequestDTO;
import com.feature.neighbourHood_backend.model.DTO.UpdateEmailRequestDTO;
import com.feature.neighbourHood_backend.model.entity.User;
import com.feature.neighbourHood_backend.service.UserService;
import com.feature.neighbourHood_backend.util.jwtUtil;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    public final UserService userService;
    private final jwtUtil jwtUtil;

    public AdminController(UserService userService, jwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    // 只有 ADMIN 角色才能访问
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/info")
    public ApiResponse<String> adminInfo() {
        return new ApiResponse(true, "");
    }

    // 任意登录用户都能访问
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user")
    public ApiResponse<String> userInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userService.getUser(userDetails.getEmail());
        return new ApiResponse(true, user, "");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/users/discover")
    public ApiResponse<List<PublicUserProfileDTO>> discoverUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return new ApiResponse<>(true, userService.listPublicUsers(userDetails.getUuid()), "success");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/user/email")
    public ApiResponse<String> updateEmail(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateEmailRequestDTO request) {
        try {
            User updated = userService.updateEmail(userDetails.getUuid(), request.getEmail());
            CustomUserDetails refreshed = new CustomUserDetails(updated, updated.getRoles());
            String token = jwtUtil.createToken(refreshed);
            return new ApiResponse(true, token, "email updated");
        } catch (IllegalArgumentException ex) {
            return new ApiResponse(false, ex.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/user/avatar")
    public ApiResponse<Boolean> updateAvatar(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateAvatarRequestDTO request) {
        userService.updateAvatar(userDetails.getUuid(), request == null ? null : request.getAvatar());
        return new ApiResponse<>(true, true, "avatar updated");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/user/password")
    public ApiResponse<String> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ChangePasswordRequestDTO request) {
        try {
            userService.updatePassword(userDetails.getUuid(), request.getCurrentPassword(), request.getNewPassword());
            return new ApiResponse<>(true, null, "password updated");
        } catch (IllegalArgumentException ex) {
            return new ApiResponse<>(false, ex.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/app-state")
    public ApiResponse<PreservedAppStateResponseDTO> getAppState(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, String> state = userService.getAppState(userDetails.getUuid());
        return new ApiResponse<>(true, new PreservedAppStateResponseDTO(state), "success");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/user/app-state")
    public ApiResponse<Boolean> saveAppState(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PreservedAppStateRequestDTO request) {
        userService.saveAppState(userDetails.getUuid(), request == null ? null : request.getState());
        return new ApiResponse<>(true, true, "saved");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/blacklist")
    public ApiResponse<Object> getBlacklist(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return new ApiResponse<>(true, userService.getBlockedUsers(userDetails.getUuid()), "success");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/user/blacklist")
    public ApiResponse<Boolean> blockUser(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody BlockUserRequestDTO request) {
        if (request.getUserId() == null) {
            return new ApiResponse<>(false, false, "userId is required");
        }
        boolean blocked = userService.blockUser(userDetails.getUuid(), request.getUserId());
        return new ApiResponse<>(true, blocked, blocked ? "user blocked" : "already blocked");
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/user/blacklist")
    public ApiResponse<Boolean> unblockUser(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody BlockUserRequestDTO request) {
        if (request.getUserId() == null) {
            return new ApiResponse<>(false, false, "userId is required");
        }
        boolean unblocked = userService.unblockUser(userDetails.getUuid(), request.getUserId());
        return new ApiResponse<>(true, unblocked, unblocked ? "user unblocked" : "user not in blacklist");
    }
}