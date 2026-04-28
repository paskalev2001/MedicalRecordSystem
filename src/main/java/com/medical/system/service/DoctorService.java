package com.medical.system.service;

import com.medical.system.dto.doctor.DoctorRequest;
import com.medical.system.dto.doctor.DoctorResponse;

import java.util.List;

public interface DoctorService {

    DoctorResponse create(DoctorRequest request);

    DoctorResponse getById(Long id);

    List<DoctorResponse> getAll();

    List<DoctorResponse> getGeneralPractitioners();

    DoctorResponse update(Long id, DoctorRequest request);

    void delete(Long id);
}