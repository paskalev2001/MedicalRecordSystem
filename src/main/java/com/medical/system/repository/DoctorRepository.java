package com.medical.system.repository;

import com.medical.system.model.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUniqueIdentifier(String uniqueIdentifier);
}
