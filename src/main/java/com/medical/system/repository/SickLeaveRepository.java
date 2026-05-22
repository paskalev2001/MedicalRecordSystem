package com.medical.system.repository;


import com.medical.system.model.entity.SickLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import com.medical.system.dto.report.DoctorSickLeaveCountResponse;
import com.medical.system.dto.report.SickLeaveMonthCountResponse;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SickLeaveRepository extends JpaRepository<SickLeave, Long> {

    Optional<SickLeave> findById(Long id);

    List<SickLeave> findByExaminationDoctorId(Long doctorId);

    List<SickLeave> findByExaminationPatientId(Long patientId);

    Optional<SickLeave> findByExaminationId(Long examinationId);

    boolean existsByExaminationId(Long examinationId);

    @Query("""
        SELECT new com.medical.system.dto.report.SickLeaveMonthCountResponse(
            YEAR(s.startDate),
            MONTH(s.startDate),
            COUNT(s.id)
        )
        FROM SickLeave s
        GROUP BY YEAR(s.startDate), MONTH(s.startDate)
        ORDER BY COUNT(s.id) DESC
        """)
    List<SickLeaveMonthCountResponse> findSickLeaveCountsByMonth();

    @Query("""
        SELECT new com.medical.system.dto.report.DoctorSickLeaveCountResponse(
            d.id,
            d.fullName,
            COUNT(s.id)
        )
        FROM SickLeave s
        JOIN s.examination e
        JOIN e.doctor d
        GROUP BY d.id, d.fullName
        ORDER BY COUNT(s.id) DESC
        """)
    List<DoctorSickLeaveCountResponse> findSickLeaveCountsByDoctor();
}
