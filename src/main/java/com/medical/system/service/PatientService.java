package com.medical.system.service;

import com.medical.system.dto.patient.PatientRequest;
import com.medical.system.dto.patient.PatientResponse;

import java.util.List;

public interface PatientService {

    PatientResponse create(PatientRequest request);

    PatientResponse getById(Long id);

    List<PatientResponse> getAll();

    List<PatientResponse> getByGeneralPractitioner(Long doctorId);

    PatientResponse getCurrentPatient(String username);

    PatientResponse update(Long id, PatientRequest request);

    void delete(Long id);
}