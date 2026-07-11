package com.example.internshipmanagement.service.impl;

import com.example.internshipmanagement.config.CustomUserDetails;
import com.example.internshipmanagement.dto.request.mentor.MentorCreateRequest;
import com.example.internshipmanagement.dto.request.mentor.MentorUpdateRequest;
import com.example.internshipmanagement.dto.response.mentor.MentorResponse;
import com.example.internshipmanagement.dto.response.mentor.MentorSummaryResponse;
import com.example.internshipmanagement.entity.Mentor;
import com.example.internshipmanagement.entity.User;
import com.example.internshipmanagement.entity.enums.Role;
import com.example.internshipmanagement.constant.ErrorMessages;
import com.example.internshipmanagement.mapper.MentorMapper;
import com.example.internshipmanagement.repository.IInternshipAssignmentRepository;
import com.example.internshipmanagement.repository.IMentorRepository;
import com.example.internshipmanagement.repository.IStudentRepository;
import com.example.internshipmanagement.repository.IUserRepository;
import com.example.internshipmanagement.service.MentorService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.stereotype.Service;

import com.example.internshipmanagement.exception.ResourceConflictException;
import com.example.internshipmanagement.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentorServiceImpl implements MentorService {

    private final IMentorRepository mentorRepository;
    private final IStudentRepository studentRepository;
    private final IUserRepository userRepository;
    private final IInternshipAssignmentRepository internshipAssignmentRepository;
    private final MentorMapper mentorMapper;

    @Override
    public List<MentorResponse> getAllMentors() {
        List<Mentor> mentors = mentorRepository.findAll();
        return mentors.stream()
                .map(mentorMapper::toMentorResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MentorSummaryResponse> getAllMentorsSummary() {
        List<Mentor> mentors = mentorRepository.findAll();
        return mentors.stream()
                .map(mentorMapper::toMentorSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<MentorResponse> getAllMentors(Pageable pageable) {
        Page<Mentor> mentorPage = mentorRepository.findAll(pageable);
        return mentorPage.map(mentorMapper::toMentorResponse);
    }

    @Override
    public Page<MentorSummaryResponse> getAllMentorsSummary(Pageable pageable) {
        Page<Mentor> mentorPage = mentorRepository.findAll(pageable);
        return mentorPage.map(mentorMapper::toMentorSummaryResponse);
    }

    @Override
    public MentorResponse getMentorById(Integer id) {
        CustomUserDetails userDetails = CustomUserDetails.getCurrentUser();
        Role role = userDetails.getRole();
        Integer userId = userDetails.getUserId();

        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay mentor voi id: " + id));

        if (role == Role.STUDENT) {
            boolean isAssigned = studentRepository.isStudentAssignedToMentor(userId, id);
            if (!isAssigned) {
                throw new AccessDeniedException("Ban khong duoc phep xem thong tin mentor nay");
            }
        } else if (role == Role.MENTOR) {
            if (!userId.equals(id)) {
                throw new AccessDeniedException("Ban chi duoc phep xem thong tin cua chinh minh");
            }
        } else if (role == Role.ADMIN) {
            // ADMIN được phép xem mọi mentor
        } else {
            throw new AccessDeniedException("Khong co quyen truy cap");
        }

        return mentorMapper.toMentorResponse(mentor);
    }

    @Override
    @Transactional
    public MentorResponse createMentor(MentorCreateRequest request) {
        User user = userRepository.findById(request.getUserId()).orElseThrow(()-> new ResourceNotFoundException("Khong tim thay nguoi dung voi id: " + request.getUserId()));
        if(user.getRole() != Role.MENTOR){
            throw new ResourceConflictException("Nguoi dung khong phai giang vien");
        }
        if(mentorRepository.existsById(request.getUserId())){
            throw new ResourceConflictException("Nguoi dung da duoc tao thong tin giang vien");
        }
        Mentor mentor  = Mentor.builder()
                .user(user)
                .department(request.getDepartment())
                .academicRank(request.getAcademicRank())
                .build();
        mentor = mentorRepository.save(mentor);
        log.info("Mentor profile created: userId={}, department={}", mentor.getId(), mentor.getDepartment());
        return mentorMapper.toMentorResponse(mentor);
    }

    @Override
    @Transactional
    public MentorResponse updateMentor(Integer mentorId,MentorUpdateRequest request) {
        CustomUserDetails userDetails = CustomUserDetails.getCurrentUser();
        Role role = userDetails.getRole();
        Integer userId = userDetails.getUserId();
        if (role == Role.MENTOR && !mentorId.equals(userId) ){
            throw new AccessDeniedException("Ban chi co the cap nhat thong tin chinh minh");
        }
        Mentor mentor = mentorRepository.findById(mentorId).orElseThrow(() -> new ResourceNotFoundException("Khong tim thay mentor voi id: " + mentorId));
        mentor.setDepartment(request.getDepartment());
        mentor.setAcademicRank(request.getAcademicRank());
        mentor = mentorRepository.save(mentor);
        log.info("Mentor profile updated: id={}", mentorId);
        return mentorMapper.toMentorResponse(mentor);
    }

    @Override
    @Transactional
    public void deleteMentor(Integer id) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay mentor voi id: " + id));

        if (internshipAssignmentRepository.existsByMentorId(id)) {
            throw new ResourceConflictException("Khong the xoa mentor dang co lich su phan cong thuc tap");
        }

        User user = mentor.getUser();
        try {
            mentorRepository.delete(mentor);
            mentorRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResourceConflictException(ErrorMessages.REFERENCED_DATA_DELETE);
        }

        user.setIsActive(false);
        userRepository.save(user);
        log.info("Mentor profile deleted: id={}, user deactivated", id);
    }
}
