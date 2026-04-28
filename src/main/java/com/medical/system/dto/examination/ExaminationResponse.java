package com.medical.system.dto.examination;

import com.medical.system.dto.diagnosis.DiagnosisSummaryResponse;
import com.medical.system.dto.doctor.DoctorSummaryResponse;
import com.medical.system.dto.patient.PatientSummaryResponse;
import com.medical.system.dto.sickleave.SickLeaveResponse;
import com.medical.system.model.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExaminationResponse(
        Long id,
        LocalDate examinationDate,
        DoctorSummaryResponse doctor,
        PatientSummaryResponse patient,
        DiagnosisSummaryResponse diagnosis,
        String prescribedTreatment,
        BigDecimal price,
        PaymentType paymentType,
        SickLeaveResponse sickLeave
) {
}