package com.example.internshipmanagement.service;

import com.example.internshipmanagement.dto.request.round.RoundCriterionCreateRequest;
import com.example.internshipmanagement.dto.request.round.RoundCriterionUpdateRequest;
import com.example.internshipmanagement.dto.response.round.RoundCriterionResponse;
import java.util.List;

public interface RoundCriterionService {
    List<RoundCriterionResponse> getCriteriaByRoundId(Integer roundId);
    RoundCriterionResponse getRoundCriterionById(Integer id);
    RoundCriterionResponse createRoundCriterion(RoundCriterionCreateRequest request);
    RoundCriterionResponse updateRoundCriterion(Integer id, RoundCriterionUpdateRequest request);
    void deleteRoundCriterion(Integer id);
}
