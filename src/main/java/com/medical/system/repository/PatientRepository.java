package com.medical.system.repository;

import com.medical.system.model.entity.Doctor;
import com.medical.system.model.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import com.medical.system.dto.report.DoctorPatientCountResponse;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByEgn(String egn);
    Optional<Patient> findByUserUsername(String username);

    boolean existsByEgn(String egn);
    boolean existsByUserId(Long userId);

    List<Patient> findByFullNameContainingIgnoreCase(String fullName);

    List<Patient> findByGeneralPractitioner(Doctor doctor);

    List<Patient> findByGeneralPractitionerId(Long doctorId);

    boolean existsByGeneralPractitionerId(Long doctorId);

    @Query("""
        SELECT new com.medical.system.dto.report.DoctorPatientCountResponse(
            d.id,
            d.fullName,
            COUNT(p.id)
        )
        FROM Doctor d
        LEFT JOIN d.registeredPatients p
        WHERE d.generalPractitioner = true
        GROUP BY d.id, d.fullName
        ORDER BY COUNT(p.id) DESC
        """)
    List<DoctorPatientCountResponse> countPatientsByGeneralPractitioner();
}
