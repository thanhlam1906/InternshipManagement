package com.example.internshipmanagement.service.impl;

import com.example.internshipmanagement.dto.request.auth.LoginRequest;
import com.example.internshipmanagement.dto.request.auth.ChangePasswordRequest;
import com.example.internshipmanagement.dto.response.auth.LoginResponse;
import com.example.internshipmanagement.dto.response.user.UserResponse;
import com.example.internshipmanagement.entity.User;
import com.example.internshipmanagement.repository.IUserRepository;
import com.example.internshipmanagement.service.AuthService;
import com.example.internshipmanagement.ulti.JwtUtil;
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
import com.example.internshipmanagement.exception.ResourceNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final IUserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

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
        SecurityContextHolder.clearContext();
        log.info("User logged out successfully");
    }
}
