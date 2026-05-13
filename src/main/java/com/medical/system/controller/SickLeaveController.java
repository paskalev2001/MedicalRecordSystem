package com.medical.system.controller;

import com.medical.system.dto.sickleave.SickLeaveRequest;
import com.medical.system.dto.sickleave.SickLeaveResponse;
import com.medical.system.service.SickLeaveService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/sick-leaves")
public class SickLeaveController {

    private final SickLeaveService sickLeaveService;

    public SickLeaveController(SickLeaveService sickLeaveService) {
        this.sickLeaveService = sickLeaveService;
    }

    @PostMapping
    public ResponseEntity<SickLeaveResponse> create(
            @Valid @RequestBody SickLeaveRequest request
    ) {
        SickLeaveResponse response = sickLeaveService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<SickLeaveResponse>> getCurrentPatientSickLeaves(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                sickLeaveService.getForCurrentPatient(authentication.getName())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SickLeaveResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sickLeaveService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<SickLeaveResponse>> getAll(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long patientId
    ) {
        if (doctorId != null) {
            return ResponseEntity.ok(sickLeaveService.getByDoctorId(doctorId));
        }

        if (patientId != null) {
            return ResponseEntity.ok(sickLeaveService.getByPatientId(patientId));
        }

        return ResponseEntity.ok(sickLeaveService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SickLeaveResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SickLeaveRequest request
    ) {
        return ResponseEntity.ok(sickLeaveService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sickLeaveService.delete(id);
        return ResponseEntity.noContent().build();
    }
}