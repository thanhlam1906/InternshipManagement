package com.example.internshipmanagement.controller;

import com.example.internshipmanagement.dto.request.mentor.MentorCreateRequest;
import com.example.internshipmanagement.dto.request.mentor.MentorUpdateRequest;
import com.example.internshipmanagement.dto.response.common.ApiDataResponse;
import com.example.internshipmanagement.dto.response.mentor.MentorResponse;
import com.example.internshipmanagement.dto.response.mentor.MentorSummaryResponse;
import com.example.internshipmanagement.dto.response.student.StudentResponse;
import com.example.internshipmanagement.entity.Mentor;
import com.example.internshipmanagement.entity.Student;
import com.example.internshipmanagement.service.MentorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;

    @GetMapping
    public ResponseEntity<ApiDataResponse<?>> getAllMentors() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            List<MentorResponse> mentors = mentorService.getAllMentors();

            ApiDataResponse<List<MentorResponse>> apiResponse = ApiDataResponse.<List<MentorResponse>>builder()
                    .success(true)
                    .message("Lay danh sach giao vien huong dan thanh cong")
                    .data(mentors)
                    .httpStatus(HttpStatus.OK)
                    .build();

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } else {
            List<MentorSummaryResponse> mentors = mentorService.getAllMentorsSummary();

            ApiDataResponse<List<MentorSummaryResponse>> apiResponse = ApiDataResponse.<List<MentorSummaryResponse>>builder()
                    .success(true)
                    .message("Lay danh sach giao vien huong dan thanh cong")
                    .data(mentors)
                    .httpStatus(HttpStatus.OK)
                    .build();

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        }
    }

    @GetMapping("/{mentor_id}")
    public ResponseEntity<ApiDataResponse<MentorResponse>> getMentorById(
            @PathVariable("mentor_id") Integer mentorId) {
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
    public ResponseEntity<ApiDataResponse<MentorResponse>> createMentor(@Valid @RequestBody MentorCreateRequest request){
        MentorResponse response = mentorService.createMentor(request);
        ApiDataResponse<MentorResponse> apiDataResponse = ApiDataResponse.<MentorResponse>builder()
                .success(true).message("Tao thong tin huong dan thanh cong").data(response).httpStatus(HttpStatus.OK).build();

        return new ResponseEntity<> (apiDataResponse, HttpStatus.OK);
    }

    @PutMapping("/{mentor_id}")
    public ResponseEntity<ApiDataResponse<MentorResponse>> updateMentor(
            @PathVariable("mentor_id") Integer mentorId,
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
    public ResponseEntity<ApiDataResponse<Void>> deleteMentor(
            @PathVariable("mentor_id") Integer mentorId) {
        mentorService.deleteMentor(mentorId);

        ApiDataResponse<Void> apiResponse = ApiDataResponse.<Void>builder()
                .success(true)
                .message("Xoa thong tin mentor thanh cong")
                .httpStatus(HttpStatus.OK)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}

