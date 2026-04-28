package com.medical.system.dto.examination;

import com.medical.system.model.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExaminationSummaryResponse(
        Long id,
        LocalDate examinationDate,
        String doctorName,
        String patientName,
        String diagnosisName,
        BigDecimal price,
        PaymentType paymentType
) {
}