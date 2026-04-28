package com.medical.system.dto.doctor;

import com.medical.system.model.enums.Specialty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DoctorRequest(

        @NotBlank(message = "Doctor unique identifier is required")
        @Size(max = 30, message = "Unique identifier must be up to 30 characters")
        String uniqueIdentifier,

        @NotBlank(message = "Doctor full name is required")
        @Size(max = 120, message = "Full name must be up to 120 characters")
        String fullName,

        @NotNull(message = "Specialty is required")
        Specialty specialty,

        boolean generalPractitioner,

        Long userId
) {
}