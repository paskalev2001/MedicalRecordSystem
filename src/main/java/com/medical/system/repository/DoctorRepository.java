package com.medical.system.repository;

import com.medical.system.model.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUniqueIdentifier(String uniqueIdentifier);

    boolean existsByUniqueIdentifier(String uniqueIdentifier);

    List<Doctor> findByGeneralPractitionerTrue();

    List<Doctor> findByFullNameContainingIgnoreCase(String fullName);
}
