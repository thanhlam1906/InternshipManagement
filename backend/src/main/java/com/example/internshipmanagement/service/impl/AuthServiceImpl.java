package com.example.internshipmanagement.service.impl;

import com.example.internshipmanagement.config.TokenBlacklist;
import com.example.internshipmanagement.dto.request.auth.LoginRequest;
import com.example.internshipmanagement.dto.request.auth.ChangePasswordRequest;
import com.example.internshipmanagement.dto.response.auth.LoginResponse;
import com.example.internshipmanagement.dto.response.user.UserResponse;
import com.example.internshipmanagement.entity.User;
import com.example.internshipmanagement.repository.IUserRepository;
import com.example.internshipmanagement.service.AuthService;
import com.example.internshipmanagement.ulti.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.internshipmanagement.exception.ResourceNotFoundException;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final IUserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final HttpServletRequest request;
    private final TokenBlacklist tokenBlacklist;

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));
        } catch (DisabledException e) {
            log.warn("Login failed - account disabled: username={}", request.getUsername());
            throw new IllegalArgumentException("Tai khoan da bi vo hieu hoa");
        } catch (BadCredentialsException e) {
            log.warn("Login failed - bad credentials: username={}", request.getUsername());
            throw new IllegalArgumentException("Ten dang nhap hoac mat khau khong chinh xac");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay user"));

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        log.info("Login successful: username={}, role={}", user.getUsername(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Override
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Nguoi dung chua dang nhap");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung: " + username));

        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mat khau moi va xac nhan mat khau khong khop");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung: " + username));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mat khau hien tai khong chinh xac");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed: username={}", username);
    }

    @Override
    public void logout() {
        String token = extractTokenFromRequest();
        if (token != null) {
            String tokenHash = com.example.internshipmanagement.config.JwtAuthenticationFilter.hashToken(token);
            Instant expiry = jwtUtil.getExpirationFromToken(token).toInstant();
            tokenBlacklist.blacklist(tokenHash, expiry);
            log.info("Token blacklisted until {}", expiry);
        }
        SecurityContextHolder.clearContext();
        log.info("User logged out successfully");
    }

    private String extractTokenFromRequest() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
