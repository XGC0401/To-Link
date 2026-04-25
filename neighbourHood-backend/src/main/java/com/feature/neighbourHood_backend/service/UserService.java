package com.feature.neighbourHood_backend.service;

import java.util.Optional;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.feature.neighbourHood_backend.model.CustomUserDetails;
import com.feature.neighbourHood_backend.model.entity.Role;
import com.feature.neighbourHood_backend.model.entity.User;
import com.feature.neighbourHood_backend.repository.RoleRepository;
import com.feature.neighbourHood_backend.repository.UserRepository;
import com.feature.neighbourHood_backend.util.BusinessException;
import com.feature.neighbourHood_backend.util.ErrorCode;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.encoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    public CustomUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        return new CustomUserDetails(user, user.getRoles());
    }

    public boolean findByUsername(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            return true;
        }
        return false;
    }

    public boolean findById(UUID uuid) {
        if (userRepository.findById(uuid).isPresent()) {
            return true;
        }
        return false;
    }

    public boolean register(String username, String email, String password) {
        // Validate inputs
        if (username == null || username.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_NAME_EMPTY, "Username is required");
        }

        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_EMAIL_INVALID, "Email is required");
        }

        if (!isValidEmail(email)) {
            throw new BusinessException(ErrorCode.VALIDATION_EMAIL_INVALID, "Invalid email format");
        }

        if (password == null || password.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_PASSWORD_WEAK, "Password is required");
        }

        if (password.length() < 6) {
            throw new BusinessException(ErrorCode.VALIDATION_PASSWORD_WEAK, "Password must be at least 6 characters");
        }

        // Check if email already exists
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS, "Email is already registered");
        }

        // Check if username already exists
        Optional<User> existingUsername = userRepository.findByUsername(username);
        if (existingUsername.isPresent()) {
            throw new BusinessException(ErrorCode.AUTH_USERNAME_ALREADY_EXISTS, "Username is already registered");
        }

        // Get USER role
        Optional<Role> role = roleRepository.findByName("USER");
        if (!role.isPresent()) {
            throw new BusinessException(ErrorCode.AUTH_ROLE_NOT_FOUND, "User role not found in system");
        }

        try {
            User rUser = new User(username, email, encoder.encode(password), role.get());
            userRepository.save(rUser);
            return true;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "Failed to register user: " + ex.getMessage(), ex);
        }
    }

    public User getUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        return user;
    }

    public User updateEmail(UUID userId, String newEmail) {
        if (newEmail == null || newEmail.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_EMAIL_INVALID, "Email is required");
        }

        if (!isValidEmail(newEmail)) {
            throw new BusinessException(ErrorCode.VALIDATION_EMAIL_INVALID, "Invalid email format");
        }

        Optional<User> existing = userRepository.findByEmail(newEmail);
        if (existing.isPresent() && !existing.get().getUuid().equals(userId)) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS, "Email already in use");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_PROFILE_NOT_FOUND, "User not found"));

        try {
            user.setEmail(newEmail);
            return userRepository.save(user);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.USER_UPDATE_FAILED, "Failed to update email: " + ex.getMessage(), ex);
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
}
