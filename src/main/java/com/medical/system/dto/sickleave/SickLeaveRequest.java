package com.medical.system.dto.sickleave;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SickLeaveRequest(

        @NotNull(message = "Examination is required")
        Long examinationId,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @Min(value = 1, message = "Number of days must be at least 1")
        int numberOfDays
) {
}