package com.medical.system.dto.patient;

public record PatientSummaryResponse(
        Long id,
        String fullName,
        String egn
) {
}