package com.medical.system.security;

import com.medical.system.dto.examination.ExaminationRequest;
import com.medical.system.model.entity.Doctor;
import com.medical.system.model.entity.Examination;
import com.medical.system.model.entity.Patient;
import com.medical.system.model.entity.SickLeave;
import com.medical.system.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.medical.system.model.entity.HealthInsuranceRecord;
import com.medical.system.repository.HealthInsuranceRecordRepository;

@Component("securityService")
public class SecurityService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ExaminationRepository examinationRepository;
    private final SickLeaveRepository sickLeaveRepository;
    private final HealthInsuranceRecordRepository healthInsuranceRecordRepository;

    public SecurityService(
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            ExaminationRepository examinationRepository,
            SickLeaveRepository sickLeaveRepository,
            HealthInsuranceRecordRepository healthInsuranceRecordRepository

    ) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.examinationRepository = examinationRepository;
        this.sickLeaveRepository = sickLeaveRepository;
        this.healthInsuranceRecordRepository = healthInsuranceRecordRepository;
    }

    public boolean isCurrentPatient(Long patientId) {
        String username = getCurrentUsername();

        return patientRepository.findByUserUsername(username)
                .map(Patient::getId)
                .map(id -> id.equals(patientId))
                .orElse(false);
    }

    public boolean isCurrentDoctor(Long doctorId) {
        String username = getCurrentUsername();

        return doctorRepository.findByUserUsername(username)
                .map(Doctor::getId)
                .map(id -> id.equals(doctorId))
                .orElse(false);
    }

    public boolean canAccessExamination(Long examinationId) {
        return examinationRepository.findById(examinationId)
                .map(Examination::getPatient)
                .map(Patient::getId)
                .map(this::isCurrentPatient)
                .orElse(false);
    }

    public boolean isExaminationDoctor(Long examinationId) {
        return examinationRepository.findById(examinationId)
                .map(Examination::getDoctor)
                .map(Doctor::getId)
                .map(this::isCurrentDoctor)
                .orElse(false);
    }

    public boolean canCreateOrUpdateExamination(ExaminationRequest request) {
        if (request == null || request.doctorId() == null) {
            return false;
        }

        return isCurrentDoctor(request.doctorId());
    }

    public boolean canAccessSickLeave(Long sickLeaveId) {
        return sickLeaveRepository.findById(sickLeaveId)
                .map(SickLeave::getExamination)
                .map(Examination::getPatient)
                .map(Patient::getId)
                .map(this::isCurrentPatient)
                .orElse(false);
    }

    public boolean isSickLeaveDoctor(Long sickLeaveId) {
        return sickLeaveRepository.findById(sickLeaveId)
                .map(SickLeave::getExamination)
                .map(Examination::getDoctor)
                .map(Doctor::getId)
                .map(this::isCurrentDoctor)
                .orElse(false);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        return authentication.getName();
    }

    public boolean canAccessHealthInsuranceRecord(Long recordId) {
        return healthInsuranceRecordRepository.findById(recordId)
                .map(HealthInsuranceRecord::getPatient)
                .map(Patient::getId)
                .map(this::isCurrentPatient)
                .orElse(false);
    }
}