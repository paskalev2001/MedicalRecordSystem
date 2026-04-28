package com.medical.system.service.impl;

import com.medical.system.dto.healthinsurance.HealthInsuranceRecordRequest;
import com.medical.system.dto.healthinsurance.HealthInsuranceRecordResponse;
import com.medical.system.exception.ResourceNotFoundException;
import com.medical.system.mapper.HealthInsuranceRecordMapper;
import com.medical.system.model.entity.HealthInsuranceRecord;
import com.medical.system.model.entity.Patient;
import com.medical.system.repository.HealthInsuranceRecordRepository;
import com.medical.system.repository.PatientRepository;
import com.medical.system.service.HealthInsuranceRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
public class HealthInsuranceRecordServiceImpl implements HealthInsuranceRecordService {

    private final HealthInsuranceRecordRepository healthInsuranceRecordRepository;
    private final PatientRepository patientRepository;
    private final HealthInsuranceRecordMapper healthInsuranceRecordMapper;

    public HealthInsuranceRecordServiceImpl(
            HealthInsuranceRecordRepository healthInsuranceRecordRepository,
            PatientRepository patientRepository,
            HealthInsuranceRecordMapper healthInsuranceRecordMapper
    ) {
        this.healthInsuranceRecordRepository = healthInsuranceRecordRepository;
        this.patientRepository = patientRepository;
        this.healthInsuranceRecordMapper = healthInsuranceRecordMapper;
    }

    @Override
    @Transactional
    public HealthInsuranceRecordResponse create(HealthInsuranceRecordRequest request) {
        Patient patient = findPatientById(request.patientId());

        HealthInsuranceRecord record = healthInsuranceRecordMapper.toEntity(request, patient);
        HealthInsuranceRecord savedRecord = healthInsuranceRecordRepository.save(record);

        return healthInsuranceRecordMapper.toResponse(savedRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public HealthInsuranceRecordResponse getById(Long id) {
        HealthInsuranceRecord record = findRecordById(id);
        return healthInsuranceRecordMapper.toResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HealthInsuranceRecordResponse> getAll() {
        return healthInsuranceRecordRepository.findAll()
                .stream()
                .map(healthInsuranceRecordMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HealthInsuranceRecordResponse> getByPatientId(Long patientId) {
        return healthInsuranceRecordRepository.findByPatientIdOrderByMonthDesc(patientId)
                .stream()
                .map(healthInsuranceRecordMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public HealthInsuranceRecordResponse update(Long id, HealthInsuranceRecordRequest request) {
        HealthInsuranceRecord record = findRecordById(id);
        Patient patient = findPatientById(request.patientId());

        healthInsuranceRecordMapper.updateEntity(record, request, patient);
        HealthInsuranceRecord updatedRecord = healthInsuranceRecordRepository.save(record);

        return healthInsuranceRecordMapper.toResponse(updatedRecord);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        HealthInsuranceRecord record = findRecordById(id);
        healthInsuranceRecordRepository.delete(record);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPatientInsuredForLastSixMonths(Patient patient) {
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(5);

        List<HealthInsuranceRecord> records =
                healthInsuranceRecordRepository.findByPatientAndMonthBetween(
                        patient,
                        startMonth,
                        currentMonth
                );

        return records.size() == 6
                && records.stream().allMatch(HealthInsuranceRecord::isInsured);
    }

    private HealthInsuranceRecord findRecordById(Long id) {
        return healthInsuranceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Health insurance record not found with id: " + id
                ));
    }

    private Patient findPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + id
                ));
    }
}