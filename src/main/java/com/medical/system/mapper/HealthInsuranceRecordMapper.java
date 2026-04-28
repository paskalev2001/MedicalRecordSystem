package com.medical.system.mapper;

import com.medical.system.dto.healthinsurance.HealthInsuranceRecordRequest;
import com.medical.system.dto.healthinsurance.HealthInsuranceRecordResponse;
import com.medical.system.model.entity.HealthInsuranceRecord;
import com.medical.system.model.entity.Patient;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
public class HealthInsuranceRecordMapper {

    public HealthInsuranceRecordResponse toResponse(HealthInsuranceRecord record) {
        if (record == null) {
            return null;
        }

        return new HealthInsuranceRecordResponse(
                record.getId(),
                record.getPatient() != null ? record.getPatient().getId() : null,
                record.getMonth() != null ? record.getMonth().toString() : null,
                record.isInsured()
        );
    }

    public HealthInsuranceRecord toEntity(HealthInsuranceRecordRequest request, Patient patient) {
        HealthInsuranceRecord record = new HealthInsuranceRecord();

        record.setPatient(patient);
        record.setMonth(YearMonth.parse(request.month()));
        record.setInsured(request.insured());

        return record;
    }

    public void updateEntity(
            HealthInsuranceRecord record,
            HealthInsuranceRecordRequest request,
            Patient patient
    ) {
        record.setPatient(patient);
        record.setMonth(YearMonth.parse(request.month()));
        record.setInsured(request.insured());
    }
}