package com.example.internshipmanagement.service.impl;

import com.example.internshipmanagement.dto.request.user.UserCreateRequest;
import com.example.internshipmanagement.dto.request.user.UserUpdateRequest;
import com.example.internshipmanagement.dto.request.user.UserStatusUpdateRequest;
import com.example.internshipmanagement.dto.request.user.UserRoleUpdateRequest;
import com.example.internshipmanagement.dto.response.user.UserResponse;
import com.example.internshipmanagement.entity.User;
import com.example.internshipmanagement.mapper.UserMapper;
import com.example.internshipmanagement.repository.IUserRepository;
import com.example.internshipmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.internshipmanagement.entity.enums.Role;
import com.example.internshipmanagement.constant.ErrorMessages;
import com.example.internshipmanagement.exception.ResourceConflictException;
import com.example.internshipmanagement.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserImpl implements UserService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceConflictException("Ten dang nhap da ton tai");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("Email da ton tai");
        }

        User user = userMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        log.info("User created: id={}, username={}, role={}", savedUser.getUserId(), savedUser.getUsername(), savedUser.getRole());
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(Role role, Pageable pageable) {
        Page<User> userPage;
        if (role != null) {
            userPage = userRepository.findByRole(role, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        return userPage.map(userMapper::toUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Integer id) {
        User user =  userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung voi id: "+ id));
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserInfo(Integer id, UserUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung voi id: " + id));
        
        if (userRepository.existsByEmailAndUserIdNot(request.getEmail(), id)) {
            throw new ResourceConflictException("Email da ton tai");
        }
        
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        
        User savedUser = userRepository.save(user);
        log.info("User info updated: id={}", id);
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Integer id, UserStatusUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung voi id: " + id));
        
        user.setIsActive(request.getIsActive());
        
        User savedUser = userRepository.save(user);
        log.info("User status updated: id={}, isActive={}", id, request.getIsActive());
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Integer id, UserRoleUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung voi id: " + id));
        
        user.setRole(request.getRole());
        
        User savedUser = userRepository.save(user);
        log.info("User role updated: id={}, newRole={}", id, request.getRole());
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Integer id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung voi id: " + id));
        try {
            userRepository.delete(user);
            userRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResourceConflictException(ErrorMessages.REFERENCED_DATA_DELETE);
        }
        log.info("User deleted: id={}", id);
    }
}

