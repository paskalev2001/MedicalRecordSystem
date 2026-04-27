package com.medical.system.repository;

import com.medical.system.model.entity.HealthInsuranceRecord;
import com.medical.system.model.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.List;

public interface HealthInsuranceRecordRepository extends JpaRepository<HealthInsuranceRecord, Long> {

    List<HealthInsuranceRecord> findByPatientAndMonthBetween(
            Patient patient,
            YearMonth startMonth,
            YearMonth endMonth
    );
}
