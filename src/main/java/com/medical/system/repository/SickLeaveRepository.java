package com.medical.system.repository;


import com.medical.system.model.entity.SickLeave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SickLeaveRepository extends JpaRepository<SickLeave, Long> {

    List<SickLeave> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

    List<SickLeave> findByExaminationDoctorId(Long doctorId);

    List<SickLeave> findByExaminationPatientId(Long patientId);
}
