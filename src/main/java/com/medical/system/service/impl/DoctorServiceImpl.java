package com.medical.system.service.impl;

import com.medical.system.dto.doctor.DoctorRequest;
import com.medical.system.dto.doctor.DoctorResponse;
import com.medical.system.exception.BadRequestException;
import com.medical.system.exception.DuplicateResourceException;
import com.medical.system.exception.ResourceNotFoundException;
import com.medical.system.mapper.DoctorMapper;
import com.medical.system.model.entity.Doctor;
import com.medical.system.model.entity.User;
import com.medical.system.model.enums.Role;
import com.medical.system.repository.DoctorRepository;
import com.medical.system.repository.UserRepository;
import com.medical.system.service.DoctorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DoctorMapper doctorMapper;

    public DoctorServiceImpl(
            DoctorRepository doctorRepository,
            UserRepository userRepository,
            DoctorMapper doctorMapper
    ) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.doctorMapper = doctorMapper;
    }

    @Override
    @Transactional
    public DoctorResponse create(DoctorRequest request) {
        validateUniqueDoctor(request, null);

        User user = getUserIfPresent(request.userId());
        validateDoctorUser(user);

        Doctor doctor = doctorMapper.toEntity(request, user);
        Doctor savedDoctor = doctorRepository.save(doctor);

        return doctorMapper.toResponse(savedDoctor);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getById(Long id) {
        Doctor doctor = findDoctorById(id);
        return doctorMapper.toResponse(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponse> getAll() {
        return doctorRepository.findAll()
                .stream()
                .map(doctorMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponse> getGeneralPractitioners() {
        return doctorRepository.findByGeneralPractitionerTrue()
                .stream()
                .map(doctorMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DoctorResponse update(Long id, DoctorRequest request) {
        Doctor doctor = findDoctorById(id);

        validateUniqueDoctor(request, id);

        User user = getUserIfPresent(request.userId());
        validateDoctorUser(user);

        doctorMapper.updateEntity(doctor, request, user);
        Doctor updatedDoctor = doctorRepository.save(doctor);

        return doctorMapper.toResponse(updatedDoctor);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Doctor doctor = findDoctorById(id);
        doctorRepository.delete(doctor);
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

    private void validateDoctorUser(User user) {
        if (user != null && user.getRole() != Role.DOCTOR) {
            throw new BadRequestException("Selected user must have DOCTOR role");
        }
    }

    private void validateUniqueDoctor(DoctorRequest request, Long currentId) {
        doctorRepository.findByUniqueIdentifier(request.uniqueIdentifier())
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Doctor with unique identifier already exists: "
                                    + request.uniqueIdentifier()
                    );
                });
    }
}