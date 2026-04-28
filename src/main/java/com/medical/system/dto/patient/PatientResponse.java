package com.medical.system.dto.patient;

import com.medical.system.dto.doctor.DoctorSummaryResponse;
import com.medical.system.dto.healthinsurance.HealthInsuranceRecordResponse;

import java.util.List;

public record PatientResponse(
        Long id,
        String fullName,
        String egn,
        DoctorSummaryResponse generalPractitioner,
        Long userId,
        List<HealthInsuranceRecordResponse> healthInsuranceRecords
) {
}
