package com.example.internshipmanagement.controller;

import com.example.internshipmanagement.dto.request.user.UserCreateRequest;
import com.example.internshipmanagement.dto.request.user.UserUpdateRequest;
import com.example.internshipmanagement.dto.request.user.UserStatusUpdateRequest;
import com.example.internshipmanagement.dto.request.user.UserRoleUpdateRequest;
import com.example.internshipmanagement.dto.response.common.ApiDataResponse;
import com.example.internshipmanagement.dto.response.user.UserResponse;
import com.example.internshipmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.internshipmanagement.entity.enums.Role;
import com.example.internshipmanagement.dto.response.common.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiDataResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.createUser(request);
        
        ApiDataResponse<UserResponse> apiResponse = ApiDataResponse.<UserResponse>builder()
                .success(true)
                .message("Tao user thanh cong")
                .data(response)
                .httpStatus(HttpStatus.CREATED)
                .build();
                
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiDataResponse<PaginatedResponse<UserResponse>>> getUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> userPage = userService.getUsers(role, pageable);
        
        PaginatedResponse<UserResponse> data = PaginatedResponse.<UserResponse>builder()
                .items(userPage.getContent())
                .pagination(PaginatedResponse.PaginationInfo.builder()
                        .currentPage(userPage.getNumber())
                        .pageSize(userPage.getSize())
                        .totalPages(userPage.getTotalPages())
                        .totalItems(userPage.getTotalElements())
                        .build())
                .build();

        ApiDataResponse<PaginatedResponse<UserResponse>> apiResponse = ApiDataResponse.<PaginatedResponse<UserResponse>>builder()
                .success(true)
                .message("Lay danh sach user thanh cong")
                .data(data)
                .httpStatus(HttpStatus.OK)
                .build();
                
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiDataResponse<UserResponse>> getUserById(@PathVariable Integer id){
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiDataResponse.<UserResponse>builder()
                                .success(true)
                                .message("Lay user thanh cong")
                                .data(userService.getUserById(id))
                                .httpStatus(HttpStatus.OK)
                                .build());

    }

    @PutMapping("/{user_id}")
    public ResponseEntity<ApiDataResponse<UserResponse>> updateUserInfo(
            @PathVariable("user_id") Integer userId,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateUserInfo(userId, request);
        ApiDataResponse<UserResponse> apiResponse = ApiDataResponse.<UserResponse>builder()
                .success(true)
                .message("Cap nhat thong tin thanh cong")
                .data(response)
                .httpStatus(HttpStatus.OK)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/{user_id}/status")
    public ResponseEntity<ApiDataResponse<UserResponse>> updateUserStatus(
            @PathVariable("user_id") Integer userId,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        UserResponse response = userService.updateUserStatus(userId, request);
        ApiDataResponse<UserResponse> apiResponse = ApiDataResponse.<UserResponse>builder()
                .success(true)
                .message("Cap nhat trang thai thanh cong")
                .data(response)
                .httpStatus(HttpStatus.OK)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/{user_id}/role")
    public ResponseEntity<ApiDataResponse<UserResponse>> updateUserRole(
            @PathVariable("user_id") Integer userId,
            @Valid @RequestBody UserRoleUpdateRequest request) {
        UserResponse response = userService.updateUserRole(userId, request);
        ApiDataResponse<UserResponse> apiResponse = ApiDataResponse.<UserResponse>builder()
                .success(true)
                .message("Cap nhat vai tro thanh cong")
                .data(response)
                .httpStatus(HttpStatus.OK)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{user_id}")
    public ResponseEntity<ApiDataResponse<Void>> deleteUser(
            @PathVariable("user_id") Integer userId) {
        userService.deleteUser(userId);
        ApiDataResponse<Void> apiResponse = ApiDataResponse.<Void>builder()
                .success(true)
                .message("Xoa user thanh cong")
                .data(null)
                .httpStatus(HttpStatus.OK)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}

