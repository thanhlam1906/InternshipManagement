package com.example.internshipmanagement.mapper;

import com.example.internshipmanagement.dto.request.user.UserCreateRequest;
import com.example.internshipmanagement.dto.response.user.UserResponse;
import com.example.internshipmanagement.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "mentor", ignore = true)
    User toUser(UserCreateRequest request);

    UserResponse toUserResponse(User user);
}

