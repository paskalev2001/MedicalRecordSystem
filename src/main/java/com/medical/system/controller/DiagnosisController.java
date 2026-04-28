package com.medical.system.controller;

import com.medical.system.dto.diagnosis.DiagnosisRequest;
import com.medical.system.dto.diagnosis.DiagnosisResponse;
import com.medical.system.service.DiagnosisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diagnoses")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @PostMapping
    public ResponseEntity<DiagnosisResponse> create(@Valid @RequestBody DiagnosisRequest request) {
        DiagnosisResponse response = diagnosisService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiagnosisResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(diagnosisService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<DiagnosisResponse>> getAll() {
        return ResponseEntity.ok(diagnosisService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiagnosisResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DiagnosisRequest request
    ) {
        return ResponseEntity.ok(diagnosisService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        diagnosisService.delete(id);
        return ResponseEntity.noContent().build();
    }
}