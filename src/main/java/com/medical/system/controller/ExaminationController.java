package com.medical.system.controller;

import com.medical.system.dto.examination.ExaminationRequest;
import com.medical.system.dto.examination.ExaminationResponse;
import com.medical.system.service.ExaminationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/examinations")
public class ExaminationController {

    private final ExaminationService examinationService;

    public ExaminationController(ExaminationService examinationService) {
        this.examinationService = examinationService;
    }

    @PostMapping
    public ResponseEntity<ExaminationResponse> create(
            @Valid @RequestBody ExaminationRequest request
    ) {
        ExaminationResponse response = examinationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<ExaminationResponse>> getCurrentPatientExaminations(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                examinationService.getForCurrentPatient(authentication.getName())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExaminationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(examinationService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ExaminationResponse>> getAll(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        if (patientId != null) {
            return ResponseEntity.ok(examinationService.getByPatientId(patientId));
        }

        if (doctorId != null && startDate != null && endDate != null) {
            return ResponseEntity.ok(
                    examinationService.getByDoctorAndPeriod(doctorId, startDate, endDate)
            );
        }

        if (doctorId != null) {
            return ResponseEntity.ok(examinationService.getByDoctorId(doctorId));
        }

        return ResponseEntity.ok(examinationService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExaminationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ExaminationRequest request
    ) {
        return ResponseEntity.ok(examinationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        examinationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}