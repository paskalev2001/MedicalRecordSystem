package com.medical.system.dto.report;

public record DoctorPatientCountResponse(
        Long doctorId,
        String doctorName,
        Long patientCount
) {
}
