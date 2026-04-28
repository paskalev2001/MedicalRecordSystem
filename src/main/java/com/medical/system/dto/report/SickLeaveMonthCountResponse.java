package com.medical.system.dto.report;

public record SickLeaveMonthCountResponse(
        int year,
        int month,
        Long sickLeaveCount
) {
}