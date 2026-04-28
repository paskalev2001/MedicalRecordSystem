package com.medical.system.dto.diagnosis;

public record DiagnosisResponse(
        Long id,
        String code,
        String name,
        String description
) {
}