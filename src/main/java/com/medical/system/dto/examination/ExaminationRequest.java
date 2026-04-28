package com.medical.system.dto.examination;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExaminationRequest(

        @NotNull(message = "Examination date is required")
        LocalDate examinationDate,

        @NotNull(message = "Doctor is required")
        Long doctorId,

        @NotNull(message = "Patient is required")
        Long patientId,

        @NotNull(message = "Diagnosis is required")
        Long diagnosisId,

        @NotBlank(message = "Prescribed treatment is required")
        @Size(max = 2000, message = "Prescribed treatment must be up to 2000 characters")
        String prescribedTreatment,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.00", message = "Price cannot be negative")
        BigDecimal price
) {
}