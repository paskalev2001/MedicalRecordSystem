package com.medical.system.dto.report;

public record SickLeaveMonthCountResponse(
        Integer year,
        Integer month,
        Long sickLeaveCount
) {
}