package com.medical.system.dto.healthinsurance;

public record HealthInsuranceRecordResponse(
        Long id,
        Long patientId,
        String month,
        boolean insured
) {
}
