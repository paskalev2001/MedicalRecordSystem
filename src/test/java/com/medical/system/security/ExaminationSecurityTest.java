package com.medical.system.security;

import com.medical.system.TestDataFactory;
import com.medical.system.model.entity.*;
import com.medical.system.model.enums.PaymentType;
import com.medical.system.model.enums.Role;
import com.medical.system.model.enums.Specialty;
import com.medical.system.repository.*;
import com.medical.system.service.ExaminationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExaminationSecurityTest {

    @Autowired
    private ExaminationService examinationService;

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
    @WithMockUser(username = "patient_security_1", roles = "PATIENT")
    void patientShouldNotAccessOtherPatientExaminations() {
        User gpUser = userRepository.save(TestDataFactory.user("gp_security_1", Role.DOCTOR));
        User doctorUser = userRepository.save(TestDataFactory.user("doctor_security_1", Role.DOCTOR));

        User patientUser1 = userRepository.save(TestDataFactory.user("patient_security_1", Role.PATIENT));
        User patientUser2 = userRepository.save(TestDataFactory.user("patient_security_2", Role.PATIENT));

        Doctor gp = doctorRepository.save(
                TestDataFactory.doctor(
                        "GP-SEC-001",
                        "Dr. GP Security",
                        Specialty.GENERAL_PRACTITIONER,
                        true,
                        gpUser
                )
        );

        Doctor doctor = doctorRepository.save(
                TestDataFactory.doctor(
                        "DOC-SEC-001",
                        "Dr. Security",
                        Specialty.CARDIOLOGY,
                        false,
                        doctorUser
                )
        );

        Patient patient1 = patientRepository.save(
                TestDataFactory.patient("Patient One", "9101011111", gp, patientUser1)
        );

        Patient patient2 = patientRepository.save(
                TestDataFactory.patient("Patient Two", "9101012222", gp, patientUser2)
        );

        Diagnosis diagnosis = diagnosisRepository.save(
                TestDataFactory.diagnosis("SEC-1", "Security Diagnosis")
        );

        examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 2),
                        doctor,
                        patient2,
                        diagnosis,
                        PaymentType.PATIENT
                )
        );

        assertThatThrownBy(() -> examinationService.getByPatientId(patient2.getId()))
                .isInstanceOf(AccessDeniedException.class);
    }
}