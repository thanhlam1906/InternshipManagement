package com.example.internshipmanagement.controller;

import com.example.internshipmanagement.dto.request.auth.LoginRequest;
import com.example.internshipmanagement.dto.response.common.ApiDataResponse;
import com.example.internshipmanagement.dto.response.auth.LoginResponse;
import com.example.internshipmanagement.dto.response.user.UserResponse;
import com.example.internshipmanagement.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiDataResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);

        ApiDataResponse<LoginResponse> apiResponse = ApiDataResponse.<LoginResponse>builder()
                .success(true)
                .message("Dang nhap thanh cong")
                .data(loginResponse)
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiDataResponse<UserResponse>> getCurrentUser() {
        UserResponse userResponse = authService.getCurrentUser();

        ApiDataResponse<UserResponse> apiResponse = ApiDataResponse.<UserResponse>builder()
                .success(true)
                .message("Lay thong tin nguoi dung hien tai thanh cong")
                .data(userResponse)
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}

