package com.medical.system.dto.diagnosis;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DiagnosisRequest(

        @Size(max = 20, message = "Diagnosis code must be up to 20 characters")
        String code,

        @NotBlank(message = "Diagnosis name is required")
        @Size(max = 150, message = "Diagnosis name must be up to 150 characters")
        String name,

        @Size(max = 1000, message = "Description must be up to 1000 characters")
        String description
) {
}