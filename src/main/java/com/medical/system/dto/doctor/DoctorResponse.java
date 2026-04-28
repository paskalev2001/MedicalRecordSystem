package com.medical.system.dto.doctor;

import com.medical.system.model.enums.Specialty;

public record DoctorResponse(
        Long id,
        String uniqueIdentifier,
        String fullName,
        Specialty specialty,
        boolean generalPractitioner,
        Long userId
) {
}
