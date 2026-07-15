package com.example.internshipmanagement.controller;

import com.example.internshipmanagement.dto.response.common.ApiDataResponse;
import com.example.internshipmanagement.dto.response.dashboard.DashboardResponse;
import com.example.internshipmanagement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<ApiDataResponse<DashboardResponse>> getDashboardStats() {
        DashboardResponse stats = dashboardService.getDashboardStats();

        ApiDataResponse<DashboardResponse> response = ApiDataResponse.<DashboardResponse>builder()
                .success(true)
                .message("Lay du lieu thong ke dashboard thanh cong")
                .data(stats)
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
