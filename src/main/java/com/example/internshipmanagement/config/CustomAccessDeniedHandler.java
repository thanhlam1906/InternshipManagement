package com.example.internshipmanagement.config;

import com.example.internshipmanagement.dto.response.common.ApiDataResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        log.warn("Access denied: URI={}, User={}, Reason={}",
                request.getRequestURI(),
                request.getRemoteUser() != null ? request.getRemoteUser() : "anonymous",
                accessDeniedException.getMessage());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ApiDataResponse<Void> apiResponse = ApiDataResponse.<Void>builder()
                .success(false)
                .message("Tai khoan khong co quyen thuc hien chuc nang nay")
                .httpStatus(HttpStatus.FORBIDDEN)
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}

