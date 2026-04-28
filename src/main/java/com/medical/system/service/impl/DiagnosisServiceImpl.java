package com.medical.system.service.impl;

import com.medical.system.dto.diagnosis.DiagnosisRequest;
import com.medical.system.dto.diagnosis.DiagnosisResponse;
import com.medical.system.exception.DuplicateResourceException;
import com.medical.system.exception.ResourceNotFoundException;
import com.medical.system.mapper.DiagnosisMapper;
import com.medical.system.model.entity.Diagnosis;
import com.medical.system.repository.DiagnosisRepository;
import com.medical.system.service.DiagnosisService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DiagnosisServiceImpl implements DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final DiagnosisMapper diagnosisMapper;

    public DiagnosisServiceImpl(
            DiagnosisRepository diagnosisRepository,
            DiagnosisMapper diagnosisMapper
    ) {
        this.diagnosisRepository = diagnosisRepository;
        this.diagnosisMapper = diagnosisMapper;
    }

    @Override
    @Transactional
    public DiagnosisResponse create(DiagnosisRequest request) {
        validateUniqueDiagnosis(request, null);

        Diagnosis diagnosis = diagnosisMapper.toEntity(request);
        Diagnosis savedDiagnosis = diagnosisRepository.save(diagnosis);

        return diagnosisMapper.toResponse(savedDiagnosis);
    }

    @Override
    @Transactional(readOnly = true)
    public DiagnosisResponse getById(Long id) {
        Diagnosis diagnosis = findDiagnosisById(id);
        return diagnosisMapper.toResponse(diagnosis);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiagnosisResponse> getAll() {
        return diagnosisRepository.findAll()
                .stream()
                .map(diagnosisMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DiagnosisResponse update(Long id, DiagnosisRequest request) {
        Diagnosis diagnosis = findDiagnosisById(id);

        validateUniqueDiagnosis(request, id);

        diagnosisMapper.updateEntity(diagnosis, request);
        Diagnosis updatedDiagnosis = diagnosisRepository.save(diagnosis);

        return diagnosisMapper.toResponse(updatedDiagnosis);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Diagnosis diagnosis = findDiagnosisById(id);
        diagnosisRepository.delete(diagnosis);
    }

    private Diagnosis findDiagnosisById(Long id) {
        return diagnosisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Diagnosis not found with id: " + id
                ));
    }

    private void validateUniqueDiagnosis(DiagnosisRequest request, Long currentId) {
        diagnosisRepository.findByName(request.name())
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Diagnosis with name already exists: " + request.name()
                    );
                });

        if (request.code() != null && !request.code().isBlank()) {
            diagnosisRepository.findByCode(request.code())
                    .filter(existing -> !existing.getId().equals(currentId))
                    .ifPresent(existing -> {
                        throw new DuplicateResourceException(
                                "Diagnosis with code already exists: " + request.code()
                        );
                    });
        }
    }
}