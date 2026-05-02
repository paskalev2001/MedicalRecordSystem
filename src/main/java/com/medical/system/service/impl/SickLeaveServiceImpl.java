package com.medical.system.service.impl;

import com.medical.system.dto.sickleave.SickLeaveRequest;
import com.medical.system.dto.sickleave.SickLeaveResponse;
import com.medical.system.exception.DuplicateResourceException;
import com.medical.system.exception.ResourceNotFoundException;
import com.medical.system.mapper.SickLeaveMapper;
import com.medical.system.model.entity.Examination;
import com.medical.system.model.entity.SickLeave;
import com.medical.system.repository.ExaminationRepository;
import com.medical.system.repository.SickLeaveRepository;
import com.medical.system.service.SickLeaveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@Service
public class SickLeaveServiceImpl implements SickLeaveService {

    private final SickLeaveRepository sickLeaveRepository;
    private final ExaminationRepository examinationRepository;
    private final SickLeaveMapper sickLeaveMapper;

    public SickLeaveServiceImpl(
            SickLeaveRepository sickLeaveRepository,
            ExaminationRepository examinationRepository,
            SickLeaveMapper sickLeaveMapper
    ) {
        this.sickLeaveRepository = sickLeaveRepository;
        this.examinationRepository = examinationRepository;
        this.sickLeaveMapper = sickLeaveMapper;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public SickLeaveResponse create(SickLeaveRequest request) {
        Examination examination = findExaminationById(request.examinationId());

        if (sickLeaveRepository.existsByExaminationId(request.examinationId())) {
            throw new DuplicateResourceException(
                    "Sick leave already exists for examination id: " + request.examinationId()
            );
        }

        SickLeave sickLeave = sickLeaveMapper.toEntity(request, examination);

        SickLeave savedSickLeave = sickLeaveRepository.save(sickLeave);

        examination.setSickLeave(savedSickLeave);
        examinationRepository.save(examination);

        return sickLeaveMapper.toResponse(savedSickLeave);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.canAccessSickLeave(#id)")
    public SickLeaveResponse getById(Long id) {
        SickLeave sickLeave = findSickLeaveById(id);
        return sickLeaveMapper.toResponse(sickLeave);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SickLeaveResponse> getAll() {
        return sickLeaveRepository.findAll()
                .stream()
                .map(sickLeaveMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SickLeaveResponse> getByDoctorId(Long doctorId) {
        return sickLeaveRepository.findByExaminationDoctorId(doctorId)
                .stream()
                .map(sickLeaveMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SickLeaveResponse> getByPatientId(Long patientId) {
        return sickLeaveRepository.findByExaminationPatientId(patientId)
                .stream()
                .map(sickLeaveMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DOCTOR') and @securityService.isSickLeaveDoctor(#id))")
    public SickLeaveResponse update(Long id, SickLeaveRequest request) {
        SickLeave sickLeave = findSickLeaveById(id);
        Examination examination = findExaminationById(request.examinationId());

        sickLeaveRepository.findByExaminationId(request.examinationId())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Another sick leave already exists for examination id: "
                                    + request.examinationId()
                    );
                });

        sickLeaveMapper.updateEntity(sickLeave, request, examination);
        SickLeave updatedSickLeave = sickLeaveRepository.save(sickLeave);

        examination.setSickLeave(updatedSickLeave);
        examinationRepository.save(examination);

        return sickLeaveMapper.toResponse(updatedSickLeave);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DOCTOR') and @securityService.isSickLeaveDoctor(#id))")
    public void delete(Long id) {
        SickLeave sickLeave = findSickLeaveById(id);

        Examination examination = sickLeave.getExamination();
        if (examination != null) {
            examination.setSickLeave(null);
            examinationRepository.save(examination);
        }

        sickLeaveRepository.delete(sickLeave);
    }

    private SickLeave findSickLeaveById(Long id) {
        return sickLeaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sick leave not found with id: " + id
                ));
    }

    private Examination findExaminationById(Long id) {
        return examinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Examination not found with id: " + id
                ));
    }
}