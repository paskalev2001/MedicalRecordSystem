package com.medical.system.repository;


import com.medical.system.model.entity.Examination;
import com.medical.system.model.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.medical.system.model.entity.Patient;

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
            WHERE e.paymentType = com.example.medicalrecord.model.enums.PaymentType.PATIENT
            """)
    BigDecimal getTotalPaidByPatients();

    @Query("""
            SELECT COALESCE(SUM(e.price), 0)
            FROM Examination e
            WHERE e.paymentType = com.example.medicalrecord.model.enums.PaymentType.PATIENT
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
}