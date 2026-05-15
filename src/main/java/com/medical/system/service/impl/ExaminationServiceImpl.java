package com.medical.system.service.impl;

import com.medical.system.dto.examination.ExaminationRequest;
import com.medical.system.dto.examination.ExaminationResponse;
import com.medical.system.exception.BadRequestException;
import com.medical.system.exception.ResourceNotFoundException;
import com.medical.system.mapper.ExaminationMapper;
import com.medical.system.model.entity.Diagnosis;
import com.medical.system.model.entity.Doctor;
import com.medical.system.model.entity.Examination;
import com.medical.system.model.entity.Patient;
import com.medical.system.model.enums.PaymentType;
import com.medical.system.repository.*;
import com.medical.system.service.ExaminationService;
import com.medical.system.service.HealthInsuranceRecordService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExaminationServiceImpl implements ExaminationService {

    private final ExaminationRepository examinationRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final HealthInsuranceRecordService healthInsuranceRecordService;
    private final ExaminationMapper examinationMapper;
    private final SickLeaveRepository sickLeaveRepository;

    public ExaminationServiceImpl(
            ExaminationRepository examinationRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            DiagnosisRepository diagnosisRepository,
            HealthInsuranceRecordService healthInsuranceRecordService,
            ExaminationMapper examinationMapper,
            SickLeaveRepository sickLeaveRepository
    ) {
        this.examinationRepository = examinationRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.healthInsuranceRecordService = healthInsuranceRecordService;
        this.examinationMapper = examinationMapper;
        this.sickLeaveRepository = sickLeaveRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DOCTOR') and @securityService.canCreateOrUpdateExamination(#request))")
    public ExaminationResponse create(ExaminationRequest request) {
        Doctor doctor = findDoctorById(request.doctorId());
        Patient patient = findPatientById(request.patientId());
        Diagnosis diagnosis = findDiagnosisById(request.diagnosisId());

        PaymentType paymentType = determinePaymentType(patient, request.examinationDate());

        Examination examination = examinationMapper.toEntity(
                request,
                doctor,
                patient,
                diagnosis,
                paymentType
        );

        Examination savedExamination = examinationRepository.save(examination);

        return examinationMapper.toResponse(savedExamination);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.canAccessExamination(#id)")
    public ExaminationResponse getById(Long id) {
        Examination examination = findExaminationById(id);
        return examinationMapper.toResponse(examination);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public List<ExaminationResponse> getAll() {
        return examinationRepository.findAll()
                .stream()
                .map(examinationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.isCurrentPatient(#patientId)")
    public List<ExaminationResponse> getByPatientId(Long patientId) {
        return examinationRepository.findByPatientIdOrderByExaminationDateDesc(patientId)
                .stream()
                .map(examinationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public List<ExaminationResponse> getByDoctorId(Long doctorId) {
        return examinationRepository.findByDoctorIdOrderByExaminationDateDesc(doctorId)
                .stream()
                .map(examinationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public List<ExaminationResponse> getByDoctorAndPeriod(
            Long doctorId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return examinationRepository.findByDoctorIdAndExaminationDateBetween(
                        doctorId,
                        startDate,
                        endDate
                )
                .stream()
                .map(examinationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @PreAuthorize("""
        hasRole('ADMIN') or 
        (hasRole('DOCTOR') and @securityService.isExaminationDoctor(#id) and @securityService.canCreateOrUpdateExamination(#request))
        """)
    public ExaminationResponse update(Long id, ExaminationRequest request) {
        Examination examination = findExaminationById(id);

        Doctor doctor = findDoctorById(request.doctorId());
        Patient patient = findPatientById(request.patientId());
        Diagnosis diagnosis = findDiagnosisById(request.diagnosisId());

        PaymentType paymentType = determinePaymentType(patient, request.examinationDate());

        examinationMapper.updateEntity(
                examination,
                request,
                doctor,
                patient,
                diagnosis,
                paymentType
        );

        Examination updatedExamination = examinationRepository.save(examination);

        return examinationMapper.toResponse(updatedExamination);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DOCTOR') and @securityService.isExaminationDoctor(#id))")
    public void delete(Long id) {
        Examination examination = findExaminationById(id);

        if (sickLeaveRepository.existsByExaminationId(id)) {
            throw new BadRequestException(
                    "Examination cannot be deleted because it has an issued sick leave. Delete the sick leave first."
            );
        }

        examinationRepository.delete(examination);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('PATIENT')")
    public List<ExaminationResponse> getForCurrentPatient(String username) {
        Patient patient = patientRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient profile not found for username: " + username
                ));

        return examinationRepository.findByPatientIdOrderByExaminationDateDesc(patient.getId())
                .stream()
                .map(examinationMapper::toResponse)
                .toList();
    }

    private PaymentType determinePaymentType(Patient patient, LocalDate examinationDate) {
        boolean insured = healthInsuranceRecordService.isPatientInsuredForLastSixMonths(
                patient,
                examinationDate
        );

        return insured ? PaymentType.NHIF : PaymentType.PATIENT;
    }

    private Examination findExaminationById(Long id) {
        return examinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Examination not found with id: " + id
                ));
    }

    private Doctor findDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + id
                ));
    }

    private Patient findPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + id
                ));
    }

    private Diagnosis findDiagnosisById(Long id) {
        return diagnosisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Diagnosis not found with id: " + id
                ));
    }
}