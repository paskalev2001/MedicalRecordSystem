package com.medical.system.service;

import com.medical.system.dto.diagnosis.DiagnosisRequest;
import com.medical.system.dto.diagnosis.DiagnosisResponse;

import java.util.List;

public interface DiagnosisService {

    DiagnosisResponse create(DiagnosisRequest request);

    DiagnosisResponse getById(Long id);

    List<DiagnosisResponse> getAll();

    DiagnosisResponse update(Long id, DiagnosisRequest request);

    void delete(Long id);
}