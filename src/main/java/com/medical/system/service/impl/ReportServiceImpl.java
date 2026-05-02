package com.medical.system.service.impl;

import com.medical.system.dto.examination.ExaminationResponse;
import com.medical.system.dto.patient.PatientResponse;
import com.medical.system.dto.patient.PatientSummaryResponse;
import com.medical.system.dto.report.*;
import com.medical.system.mapper.ExaminationMapper;
import com.medical.system.mapper.PatientMapper;
import com.medical.system.model.enums.PaymentType;
import com.medical.system.repository.ExaminationRepository;
import com.medical.system.repository.PatientRepository;
import com.medical.system.repository.SickLeaveRepository;
import com.medical.system.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private final ExaminationRepository examinationRepository;
    private final PatientRepository patientRepository;
    private final SickLeaveRepository sickLeaveRepository;
    private final ExaminationMapper examinationMapper;
    private final PatientMapper patientMapper;

    public ReportServiceImpl(
            ExaminationRepository examinationRepository,
            PatientRepository patientRepository,
            SickLeaveRepository sickLeaveRepository,
            ExaminationMapper examinationMapper,
            PatientMapper patientMapper
    ) {
        this.examinationRepository = examinationRepository;
        this.patientRepository = patientRepository;
        this.sickLeaveRepository = sickLeaveRepository;
        this.examinationMapper = examinationMapper;
        this.patientMapper = patientMapper;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public List<PatientSummaryResponse> getPatientsByDiagnosis(Long diagnosisId) {
        return examinationRepository.findPatientSummariesByDiagnosisId(diagnosisId);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public DiagnosisCountResponse getMostCommonDiagnosis() {
        return examinationRepository.findDiagnosisCounts()
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public List<PatientResponse> getPatientsByGeneralPractitioner(Long doctorId) {
        return patientRepository.findByGeneralPractitionerId(doctorId)
                .stream()
                .map(patientMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public BigDecimal getTotalPaidByPatients() {
        return examinationRepository.getTotalPaidByPatients();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<DoctorRevenueResponse> getPaidByPatientsGroupedByDoctor() {
        return examinationRepository.findPaidByPatientsGroupedByDoctor(PaymentType.PATIENT);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public List<DoctorPatientCountResponse> getPatientCountsByGeneralPractitioner() {
        return patientRepository.countPatientsByGeneralPractitioner();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public List<DoctorVisitCountResponse> getVisitCountsByDoctor() {
        return examinationRepository.findVisitCountsByDoctor();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.isCurrentPatient(#patientId)")
    public List<ExaminationResponse> getPatientVisitHistory(Long patientId) {
        return examinationRepository.findByPatientIdOrderByExaminationDateDesc(patientId)
                .stream()
                .map(examinationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public List<ExaminationResponse> getExaminationsByDoctorAndOrPeriod(
            Long doctorId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (doctorId != null && startDate != null && endDate != null) {
            return examinationRepository.findByDoctorIdAndExaminationDateBetween(
                            doctorId,
                            startDate,
                            endDate
                    )
                    .stream()
                    .map(examinationMapper::toResponse)
                    .toList();
        }

        if (doctorId != null) {
            return examinationRepository.findByDoctorIdOrderByExaminationDateDesc(doctorId)
                    .stream()
                    .map(examinationMapper::toResponse)
                    .toList();
        }

        if (startDate != null && endDate != null) {
            return examinationRepository.findByExaminationDateBetween(startDate, endDate)
                    .stream()
                    .map(examinationMapper::toResponse)
                    .toList();
        }

        return examinationRepository.findAll()
                .stream()
                .map(examinationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public SickLeaveMonthCountResponse getMonthWithMostSickLeaves() {
        return sickLeaveRepository.findSickLeaveCountsByMonth()
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public List<DoctorSickLeaveCountResponse> getDoctorsWithMostSickLeaves() {
        List<DoctorSickLeaveCountResponse> counts =
                sickLeaveRepository.findSickLeaveCountsByDoctor();

        if (counts.isEmpty()) {
            return List.of();
        }

        Long maxCount = counts.get(0).sickLeaveCount();

        return counts.stream()
                .filter(item -> item.sickLeaveCount().equals(maxCount))
                .toList();
    }
}