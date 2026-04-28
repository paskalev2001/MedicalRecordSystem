package com.medical.system.dto.patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PatientRequest(

        @NotBlank(message = "Patient full name is required")
        @Size(max = 120, message = "Full name must be up to 120 characters")
        String fullName,

        @NotBlank(message = "EGN is required")
        @Pattern(regexp = "\\d{10}", message = "EGN must contain exactly 10 digits")
        String egn,

        @NotNull(message = "General practitioner is required")
        Long generalPractitionerId,

        Long userId
) {
}