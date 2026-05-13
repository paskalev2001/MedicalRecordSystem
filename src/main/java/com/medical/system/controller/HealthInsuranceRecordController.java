package com.medical.system.controller;

import com.medical.system.dto.healthinsurance.HealthInsuranceRecordRequest;
import com.medical.system.dto.healthinsurance.HealthInsuranceRecordResponse;
import com.medical.system.service.HealthInsuranceRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/health-insurance-records")
public class HealthInsuranceRecordController {

    private final HealthInsuranceRecordService healthInsuranceRecordService;

    public HealthInsuranceRecordController(HealthInsuranceRecordService healthInsuranceRecordService) {
        this.healthInsuranceRecordService = healthInsuranceRecordService;
    }

    @PostMapping
    public ResponseEntity<HealthInsuranceRecordResponse> create(
            @Valid @RequestBody HealthInsuranceRecordRequest request
    ) {
        HealthInsuranceRecordResponse response = healthInsuranceRecordService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<HealthInsuranceRecordResponse>> getCurrentPatientHealthInsuranceRecords(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                healthInsuranceRecordService.getForCurrentPatient(authentication.getName())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<HealthInsuranceRecordResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(healthInsuranceRecordService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<HealthInsuranceRecordResponse>> getAll(
            @RequestParam(required = false) Long patientId
    ) {
        if (patientId != null) {
            return ResponseEntity.ok(healthInsuranceRecordService.getByPatientId(patientId));
        }

        return ResponseEntity.ok(healthInsuranceRecordService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<HealthInsuranceRecordResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody HealthInsuranceRecordRequest request
    ) {
        return ResponseEntity.ok(healthInsuranceRecordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        healthInsuranceRecordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}