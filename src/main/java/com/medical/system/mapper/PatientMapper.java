package com.medical.system.mapper;


import com.medical.system.dto.healthinsurance.HealthInsuranceRecordResponse;
import com.medical.system.dto.patient.PatientRequest;
import com.medical.system.dto.patient.PatientResponse;
import com.medical.system.dto.patient.PatientSummaryResponse;
import com.medical.system.model.entity.Doctor;
import com.medical.system.model.entity.Patient;
import com.medical.system.model.entity.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class PatientMapper {

    private final DoctorMapper doctorMapper;
    private final HealthInsuranceRecordMapper healthInsuranceRecordMapper;

    public PatientMapper(
            DoctorMapper doctorMapper,
            HealthInsuranceRecordMapper healthInsuranceRecordMapper
    ) {
        this.doctorMapper = doctorMapper;
        this.healthInsuranceRecordMapper = healthInsuranceRecordMapper;
    }

    public PatientResponse toResponse(Patient patient) {
        if (patient == null) {
            return null;
        }

        List<HealthInsuranceRecordResponse> insuranceRecords =
                patient.getHealthInsuranceRecords() == null
                        ? Collections.emptyList()
                        : patient.getHealthInsuranceRecords()
                        .stream()
                        .map(healthInsuranceRecordMapper::toResponse)
                        .toList();

        return new PatientResponse(
                patient.getId(),
                patient.getFullName(),
                patient.getEgn(),
                doctorMapper.toSummary(patient.getGeneralPractitioner()),
                patient.getUser() != null ? patient.getUser().getId() : null,
                insuranceRecords
        );
    }

    public PatientSummaryResponse toSummary(Patient patient) {
        if (patient == null) {
            return null;
        }

        return new PatientSummaryResponse(
                patient.getId(),
                patient.getFullName(),
                patient.getEgn()
        );
    }

    public Patient toEntity(PatientRequest request, Doctor generalPractitioner, User user) {
        Patient patient = new Patient();

        patient.setFullName(request.fullName());
        patient.setEgn(request.egn());
        patient.setGeneralPractitioner(generalPractitioner);
        patient.setUser(user);

        return patient;
    }

    public void updateEntity(
            Patient patient,
            PatientRequest request,
            Doctor generalPractitioner,
            User user
    ) {
        patient.setFullName(request.fullName());
        patient.setEgn(request.egn());
        patient.setGeneralPractitioner(generalPractitioner);
        patient.setUser(user);
    }
}