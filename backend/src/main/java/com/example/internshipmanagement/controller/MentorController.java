package com.example.internshipmanagement.controller;

import com.example.internshipmanagement.dto.request.mentor.MentorCreateRequest;
import com.example.internshipmanagement.dto.request.mentor.MentorUpdateRequest;
import com.example.internshipmanagement.dto.response.common.ApiDataResponse;
import com.example.internshipmanagement.dto.response.common.PaginatedResponse;
import com.example.internshipmanagement.dto.response.mentor.MentorResponse;
import com.example.internshipmanagement.dto.response.mentor.MentorSummaryResponse;
import com.example.internshipmanagement.service.MentorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
@Validated
public class MentorController {

    private final MentorService mentorService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiDataResponse<?>> getAllMentors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Pageable pageable = PageRequest.of(page, size);

        if (isAdmin) {
            Page<MentorResponse> mentorPage = mentorService.getAllMentors(pageable);

            PaginatedResponse<MentorResponse> data = PaginatedResponse.<MentorResponse>builder()
                    .items(mentorPage.getContent())
                    .pagination(PaginatedResponse.PaginationInfo.builder()
                            .currentPage(mentorPage.getNumber())
                            .pageSize(mentorPage.getSize())
                            .totalPages(mentorPage.getTotalPages())
                            .totalItems(mentorPage.getTotalElements())
                            .build())
                    .build();

            ApiDataResponse<PaginatedResponse<MentorResponse>> apiResponse = ApiDataResponse.<PaginatedResponse<MentorResponse>>builder()
                    .success(true)
                    .message("Lay danh sach giao vien huong dan thanh cong")
                    .data(data)
                    .httpStatus(HttpStatus.OK)
                    .build();

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } else {
            Page<MentorSummaryResponse> mentorPage = mentorService.getAllMentorsSummary(pageable);

            PaginatedResponse<MentorSummaryResponse> data = PaginatedResponse.<MentorSummaryResponse>builder()
                    .items(mentorPage.getContent())
                    .pagination(PaginatedResponse.PaginationInfo.builder()
                            .currentPage(mentorPage.getNumber())
                            .pageSize(mentorPage.getSize())
                            .totalPages(mentorPage.getTotalPages())
                            .totalItems(mentorPage.getTotalElements())
                            .build())
                    .build();

            ApiDataResponse<PaginatedResponse<MentorSummaryResponse>> apiResponse = ApiDataResponse.<PaginatedResponse<MentorSummaryResponse>>builder()
                    .success(true)
                    .message("Lay danh sach giao vien huong dan thanh cong")
                    .data(data)
                    .httpStatus(HttpStatus.OK)
                    .build();

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        }
    }

    @GetMapping("/{mentor_id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'MENTOR', 'ADMIN')")
    public ResponseEntity<ApiDataResponse<MentorResponse>> getMentorById(
            @PathVariable("mentor_id") @Positive(message = "ID must be positive") Integer mentorId) {
        MentorResponse mentor = mentorService.getMentorById(mentorId);

        ApiDataResponse<MentorResponse> apiResponse = ApiDataResponse.<MentorResponse>builder()
                .success(true)
                .message("Lay thong tin giao vien huong dan thanh cong")
                .data(mentor)
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiDataResponse<MentorResponse>> createMentor(@Valid @RequestBody MentorCreateRequest request){
        MentorResponse response = mentorService.createMentor(request);
        ApiDataResponse<MentorResponse> apiDataResponse = ApiDataResponse.<MentorResponse>builder()
                .success(true).message("Tao thong tin huong dan thanh cong").data(response).httpStatus(HttpStatus.OK).build();

        return new ResponseEntity<> (apiDataResponse, HttpStatus.OK);
    }

    @PutMapping("/{mentor_id}")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseEntity<ApiDataResponse<MentorResponse>> updateMentor(
            @PathVariable("mentor_id") @Positive(message = "ID must be positive") Integer mentorId,
            @Valid @RequestBody MentorUpdateRequest request) {
        MentorResponse response = mentorService.updateMentor(mentorId, request);

        ApiDataResponse<MentorResponse> apiResponse = ApiDataResponse.<MentorResponse>builder()
                .success(true)
                .message("Cap nhat thong tin giao vien huong dan thanh cong")
                .data(response)
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{mentor_id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiDataResponse<Void>> deleteMentor(
            @PathVariable("mentor_id") @Positive(message = "ID must be positive") Integer mentorId) {
        mentorService.deleteMentor(mentorId);

        ApiDataResponse<Void> apiResponse = ApiDataResponse.<Void>builder()
                .success(true)
                .message("Xoa thong tin mentor thanh cong")
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}

