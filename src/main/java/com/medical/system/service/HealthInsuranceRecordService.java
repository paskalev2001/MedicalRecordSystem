package com.medical.system.service;

import com.medical.system.dto.healthinsurance.HealthInsuranceRecordRequest;
import com.medical.system.dto.healthinsurance.HealthInsuranceRecordResponse;
import com.medical.system.model.entity.Patient;

import java.time.LocalDate;
import java.util.List;

public interface HealthInsuranceRecordService {

    HealthInsuranceRecordResponse create(HealthInsuranceRecordRequest request);

    HealthInsuranceRecordResponse getById(Long id);

    List<HealthInsuranceRecordResponse> getAll();

    List<HealthInsuranceRecordResponse> getByPatientId(Long patientId);

    HealthInsuranceRecordResponse update(Long id, HealthInsuranceRecordRequest request);

    void delete(Long id);

    boolean isPatientInsuredForLastSixMonths(Patient patient, LocalDate referenceDate);
}