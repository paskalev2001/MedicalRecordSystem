package com.medical.system.repository;


import com.medical.system.model.entity.SickLeave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SickLeaveRepository extends JpaRepository<SickLeave, Long> {

    List<SickLeave> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

    Optional<SickLeave> findById(Long id);

    List<SickLeave> findByExaminationDoctorId(Long doctorId);

    List<SickLeave> findByExaminationPatientId(Long patientId);

    Optional<SickLeave> findByExaminationId(Long examinationId);

    boolean existsByExaminationId(Long examinationId);
}
