package com.medical.system.dto.report;

public record DoctorSickLeaveCountResponse(
        Long doctorId,
        String doctorName,
        Long sickLeaveCount
) {
}