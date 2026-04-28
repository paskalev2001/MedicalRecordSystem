package com.medical.system.service;

import com.medical.system.dto.examination.ExaminationRequest;
import com.medical.system.dto.examination.ExaminationResponse;

import java.time.LocalDate;
import java.util.List;

public interface ExaminationService {

    ExaminationResponse create(ExaminationRequest request);

    ExaminationResponse getById(Long id);

    List<ExaminationResponse> getAll();

    List<ExaminationResponse> getByPatientId(Long patientId);

    List<ExaminationResponse> getByDoctorId(Long doctorId);

    List<ExaminationResponse> getByDoctorAndPeriod(
            Long doctorId,
            LocalDate startDate,
            LocalDate endDate
    );

    ExaminationResponse update(Long id, ExaminationRequest request);

    void delete(Long id);
}