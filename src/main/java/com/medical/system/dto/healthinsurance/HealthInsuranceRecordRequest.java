package com.medical.system.dto.healthinsurance;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record HealthInsuranceRecordRequest(

        @NotNull(message = "Patient is required")
        Long patientId,

        @NotNull(message = "Month is required")
        @Pattern(
                regexp = "\\d{4}-\\d{2}",
                message = "Month must be in format yyyy-MM"
        )
        String month,

        boolean insured
) {
}