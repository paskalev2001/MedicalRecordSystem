package com.medical.system.dto.doctor;


import com.medical.system.model.enums.Specialty;

public record DoctorSummaryResponse(
        Long id,
        String fullName,
        Specialty specialty,
        boolean generalPractitioner
) {
}
