package com.medical.system.repository;

import com.medical.system.model.entity.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
    Optional<Diagnosis> findByName(String name);
    Optional<Diagnosis> findByCode(String code);

    boolean existsByCode(String code);
}