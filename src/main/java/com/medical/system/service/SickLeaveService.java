package com.medical.system.service;

import com.medical.system.dto.sickleave.SickLeaveRequest;
import com.medical.system.dto.sickleave.SickLeaveResponse;

import java.util.List;

public interface SickLeaveService {

    SickLeaveResponse create(SickLeaveRequest request);

    SickLeaveResponse getById(Long id);

    List<SickLeaveResponse> getAll();

    List<SickLeaveResponse> getByDoctorId(Long doctorId);

    List<SickLeaveResponse> getByPatientId(Long patientId);

    List<SickLeaveResponse> getForCurrentPatient(String username);

    SickLeaveResponse update(Long id, SickLeaveRequest request);

    void delete(Long id);
}