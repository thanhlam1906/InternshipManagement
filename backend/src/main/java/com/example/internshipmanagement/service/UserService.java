package com.example.internshipmanagement.service;

import com.example.internshipmanagement.dto.request.user.UserCreateRequest;
import com.example.internshipmanagement.dto.response.user.UserResponse;
import com.example.internshipmanagement.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.internshipmanagement.dto.request.user.UserUpdateRequest;
import com.example.internshipmanagement.dto.request.user.UserStatusUpdateRequest;
import com.example.internshipmanagement.dto.request.user.UserRoleUpdateRequest;

public interface UserService {
    UserResponse createUser(UserCreateRequest request);
    Page<UserResponse> getUsers(Role role, Pageable pageable);
    UserResponse getUserById(Integer id);
    UserResponse updateUserInfo(Integer id, UserUpdateRequest request);
    UserResponse updateUserStatus(Integer id, UserStatusUpdateRequest request);
    UserResponse updateUserRole(Integer id, UserRoleUpdateRequest request);
    void deleteUser(Integer id);
}

