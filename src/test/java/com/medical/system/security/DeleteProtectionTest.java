package com.medical.system.service;

import com.medical.system.TestDataFactory;
import com.medical.system.exception.BadRequestException;
import com.medical.system.model.entity.*;
import com.medical.system.model.enums.PaymentType;
import com.medical.system.model.enums.Role;
import com.medical.system.model.enums.Specialty;
import com.medical.system.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DeleteProtectionTest {

    @Autowired
    private DiagnosisService diagnosisService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DiagnosisRepository diagnosisRepository;

    @Autowired
    private ExaminationRepository examinationRepository;

    @Test
    void diagnosisUsedInExaminationShouldNotBeDeleted() {
        User gpUser = userRepository.save(TestDataFactory.user("gp_delete_1", Role.DOCTOR));
        User doctorUser = userRepository.save(TestDataFactory.user("doctor_delete_1", Role.DOCTOR));
        User patientUser = userRepository.save(TestDataFactory.user("patient_delete_1", Role.PATIENT));

        Doctor gp = doctorRepository.save(
                TestDataFactory.doctor(
                        "GP-DEL-001",
                        "Dr. GP Delete",
                        Specialty.GENERAL_PRACTITIONER,
                        true,
                        gpUser
                )
        );

        Doctor doctor = doctorRepository.save(
                TestDataFactory.doctor(
                        "DOC-DEL-001",
                        "Dr. Delete",
                        Specialty.CARDIOLOGY,
                        false,
                        doctorUser
                )
        );

        Patient patient = patientRepository.save(
                TestDataFactory.patient(
                        "Delete Patient",
                        "9303031111",
                        gp,
                        patientUser
                )
        );

        Diagnosis diagnosis = diagnosisRepository.save(
                TestDataFactory.diagnosis("DEL-001", "Delete Protection Diagnosis")
        );

        examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 2),
                        doctor,
                        patient,
                        diagnosis,
                        PaymentType.PATIENT
                )
        );

        assertThatThrownBy(() -> diagnosisService.delete(diagnosis.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Diagnosis cannot be deleted");
    }
}