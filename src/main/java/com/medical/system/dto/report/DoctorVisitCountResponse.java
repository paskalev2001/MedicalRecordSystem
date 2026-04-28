package com.medical.system.dto.report;

public record DoctorVisitCountResponse(
        Long doctorId,
        String doctorName,
        Long visitCount
) {
}