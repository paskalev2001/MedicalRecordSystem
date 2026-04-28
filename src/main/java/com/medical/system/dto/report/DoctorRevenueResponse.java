package com.medical.system.dto.report;

import java.math.BigDecimal;

public record DoctorRevenueResponse(
        Long doctorId,
        String doctorName,
        BigDecimal totalPaidByPatients
) {
}