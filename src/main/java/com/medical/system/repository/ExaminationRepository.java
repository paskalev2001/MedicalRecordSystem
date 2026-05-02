package com.medical.system.repository;


import com.medical.system.model.entity.Examination;
import com.medical.system.model.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.medical.system.model.entity.Patient;

import com.medical.system.dto.report.DiagnosisCountResponse;
import com.medical.system.dto.report.DoctorRevenueResponse;
import com.medical.system.dto.report.DoctorVisitCountResponse;
import com.medical.system.dto.patient.PatientSummaryResponse;
import com.medical.system.model.enums.PaymentType;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExaminationRepository extends JpaRepository<Examination, Long> {

    List<Examination> findByPatientId(Long patientId);

    List<Examination> findByDoctorId(Long doctorId);

    List<Examination> findByDiagnosisId(Long diagnosisId);

    List<Examination> findByExaminationDateBetween(LocalDate startDate, LocalDate endDate);

    List<Examination> findByDoctorIdAndExaminationDateBetween(
            Long doctorId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Examination> findByPatientIdOrderByExaminationDateDesc(Long patientId);

    List<Examination> findByDoctorIdOrderByExaminationDateDesc(Long doctorId);

    List<Examination> findByPaymentType(PaymentType paymentType);

    @Query("""
            SELECT COALESCE(SUM(e.price), 0)
            FROM Examination e
            WHERE e.paymentType = com.medical.system.model.enums.PaymentType.PATIENT
            """)
    BigDecimal getTotalPaidByPatients();

    @Query("""
            SELECT COALESCE(SUM(e.price), 0)
            FROM Examination e
            WHERE e.paymentType = com.medical.system.model.enums.PaymentType.PATIENT
              AND e.doctor.id = :doctorId
            """)
    BigDecimal getTotalPaidByPatientsForDoctor(Long doctorId);

    @Query("""
            SELECT e.diagnosis.id
            FROM Examination e
            GROUP BY e.diagnosis.id
            ORDER BY COUNT(e.id) DESC
            """)
    List<Long> findMostCommonDiagnosisIds();

    @Query("""
            SELECT e.patient
            FROM Examination e
            WHERE e.diagnosis.id = :diagnosisId
            GROUP BY e.patient
            """)
    List<Patient> findPatientsByDiagnosisId(Long diagnosisId);

    @Query("""
        SELECT DISTINCT new com.medical.system.dto.patient.PatientSummaryResponse(
            p.id,
            p.fullName,
            p.egn
        )
        FROM Examination e
        JOIN e.patient p
        WHERE e.diagnosis.id = :diagnosisId
        """)
    List<PatientSummaryResponse> findPatientSummariesByDiagnosisId(
            @Param("diagnosisId") Long diagnosisId
    );

    @Query("""
        SELECT new com.medical.system.dto.report.DiagnosisCountResponse(
            d.id,
            d.name,
            COUNT(e.id)
        )
        FROM Examination e
        JOIN e.diagnosis d
        GROUP BY d.id, d.name
        ORDER BY COUNT(e.id) DESC
        """)
    List<DiagnosisCountResponse> findDiagnosisCounts();

    @Query("""
        SELECT new com.medical.system.dto.report.DoctorVisitCountResponse(
            d.id,
            d.fullName,
            COUNT(e.id)
        )
        FROM Doctor d
        LEFT JOIN d.examinations e
        GROUP BY d.id, d.fullName
        ORDER BY COUNT(e.id) DESC
        """)
    List<DoctorVisitCountResponse> findVisitCountsByDoctor();

    @Query("""
        SELECT new com.medical.system.dto.report.DoctorRevenueResponse(
            e.doctor.id,
            e.doctor.fullName,
            SUM(e.price)
        )
        FROM Examination e
        WHERE e.paymentType = :paymentType
        GROUP BY e.doctor.id, e.doctor.fullName
        ORDER BY SUM(e.price) DESC
        """)
    List<DoctorRevenueResponse> findPaidByPatientsGroupedByDoctor(
            @Param("paymentType") PaymentType paymentType
    );
}