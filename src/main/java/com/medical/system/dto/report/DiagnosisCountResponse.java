package com.medical.system.dto.report;

public record DiagnosisCountResponse(
        Long diagnosisId,
        String diagnosisName,
        Long count
) {
}