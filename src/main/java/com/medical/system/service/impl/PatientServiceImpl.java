package com.medical.system.service.impl;

import com.medical.system.dto.patient.PatientRequest;
import com.medical.system.dto.patient.PatientResponse;
import com.medical.system.exception.BadRequestException;
import com.medical.system.exception.DuplicateResourceException;
import com.medical.system.exception.ResourceNotFoundException;
import com.medical.system.mapper.PatientMapper;
import com.medical.system.model.entity.Doctor;
import com.medical.system.model.entity.Patient;
import com.medical.system.model.entity.User;
import com.medical.system.model.enums.Role;
import com.medical.system.repository.DoctorRepository;
import com.medical.system.repository.ExaminationRepository;
import com.medical.system.repository.PatientRepository;
import com.medical.system.repository.UserRepository;
import com.medical.system.service.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PatientMapper patientMapper;
    private final ExaminationRepository examinationRepository;

    public PatientServiceImpl(
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            UserRepository userRepository,
            PatientMapper patientMapper,
            ExaminationRepository examinationRepository
    ) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.patientMapper = patientMapper;
        this.examinationRepository = examinationRepository;
    }

    @Override
    @Transactional
    public PatientResponse create(PatientRequest request) {
        validateUniquePatient(request, null);

        Doctor generalPractitioner = findDoctorById(request.generalPractitionerId());

        if (!generalPractitioner.isGeneralPractitioner()) {
            throw new BadRequestException("Selected doctor is not a general practitioner");
        }

        User user = getUserIfPresent(request.userId());
        validatePatientUser(user);

        Patient patient = patientMapper.toEntity(request, generalPractitioner, user);
        Patient savedPatient = patientRepository.save(patient);

        return patientMapper.toResponse(savedPatient);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.isCurrentPatient(#id)")
    public PatientResponse getById(Long id) {
        Patient patient = findPatientById(id);
        return patientMapper.toResponse(patient);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public List<PatientResponse> getAll() {
        return patientRepository.findAll()
                .stream()
                .map(patientMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public List<PatientResponse> getByGeneralPractitioner(Long doctorId) {
        return patientRepository.findByGeneralPractitionerId(doctorId)
                .stream()
                .map(patientMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('PATIENT')")
    public PatientResponse getCurrentPatient(String username) {
        Patient patient = patientRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient profile not found for username: " + username
                ));

        return patientMapper.toResponse(patient);
    }

    @Override
    @Transactional
    public PatientResponse update(Long id, PatientRequest request) {
        Patient patient = findPatientById(id);

        validateUniquePatient(request, id);

        Doctor generalPractitioner = findDoctorById(request.generalPractitionerId());

        if (!generalPractitioner.isGeneralPractitioner()) {
            throw new BadRequestException("Selected doctor is not a general practitioner");
        }

        User user = getUserIfPresent(request.userId());
        validatePatientUser(user);

        patientMapper.updateEntity(patient, request, generalPractitioner, user);
        Patient updatedPatient = patientRepository.save(patient);

        return patientMapper.toResponse(updatedPatient);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Patient patient = findPatientById(id);

        if (examinationRepository.existsByPatientId(id)) {
            throw new BadRequestException(
                    "Patient cannot be deleted because there are examinations for this patient."
            );
        }

        patientRepository.delete(patient);
    }

    private Patient findPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + id
                ));
    }

    private Doctor findDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + id
                ));
    }

    private User getUserIfPresent(Long userId) {
        if (userId == null) {
            return null;
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId
                ));
    }

    private void validatePatientUser(User user) {
        if (user != null && user.getRole() != Role.PATIENT) {
            throw new BadRequestException("Selected user must have PATIENT role");
        }
    }

    private void validateUniquePatient(PatientRequest request, Long currentId) {
        patientRepository.findByEgn(request.egn())
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Patient with EGN already exists: " + request.egn()
                    );
                });
    }
}