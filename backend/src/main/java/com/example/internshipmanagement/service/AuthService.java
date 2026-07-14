package com.example.internshipmanagement.service;

import com.example.internshipmanagement.dto.request.auth.LoginRequest;
import com.example.internshipmanagement.dto.request.auth.RegisterRequest;
import com.example.internshipmanagement.dto.request.auth.ChangePasswordRequest;
import com.example.internshipmanagement.dto.response.auth.LoginResponse;
import com.example.internshipmanagement.dto.response.user.UserResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse register(RegisterRequest request);
    UserResponse getCurrentUser();
    void changePassword(ChangePasswordRequest request);
    void logout();
}

