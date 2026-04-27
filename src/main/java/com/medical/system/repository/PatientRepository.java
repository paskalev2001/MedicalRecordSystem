package com.medical.system.repository;

import com.medical.system.model.entity.Doctor;
import com.medical.system.model.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByEgn(String egn);
    boolean existsByEgn(String egn);

    List<Patient> findByFullNameContainingIgnoreCase(String fullName);

    List<Patient> findByGeneralPractitioner(Doctor doctor);

    List<Patient> findByGeneralPractitionerId(Long doctorId);
}
