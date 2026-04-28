package com.medical.system.dto.sickleave;

import java.time.LocalDate;

public record SickLeaveResponse(
        Long id,
        Long examinationId,
        LocalDate startDate,
        int numberOfDays
) {
}