package com.medical.system.security;

import com.medical.system.TestDataFactory;
import com.medical.system.dto.examination.ExaminationRequest;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DoctorExaminationSecurityTest {

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
    @WithMockUser(username = "doctor_security_other", roles = "DOCTOR")
    void doctorShouldNotUpdateExaminationPerformedByAnotherDoctor() {
        User gpUser = userRepository.save(
                TestDataFactory.user("gp_security_update", Role.DOCTOR)
        );

        User doctorUserOwner = userRepository.save(
                TestDataFactory.user("doctor_security_owner", Role.DOCTOR)
        );

        User doctorUserOther = userRepository.save(
                TestDataFactory.user("doctor_security_other", Role.DOCTOR)
        );

        User patientUser = userRepository.save(
                TestDataFactory.user("patient_security_update", Role.PATIENT)
        );

        Doctor gp = doctorRepository.save(
                TestDataFactory.doctor(
                        "GP-SEC-UPDATE",
                        "Dr. GP Update",
                        Specialty.GENERAL_PRACTITIONER,
                        true,
                        gpUser
                )
        );

        Doctor ownerDoctor = doctorRepository.save(
                TestDataFactory.doctor(
                        "DOC-SEC-OWNER",
                        "Dr. Owner",
                        Specialty.CARDIOLOGY,
                        false,
                        doctorUserOwner
                )
        );

        Doctor otherDoctor = doctorRepository.save(
                TestDataFactory.doctor(
                        "DOC-SEC-OTHER",
                        "Dr. Other",
                        Specialty.NEUROLOGY,
                        false,
                        doctorUserOther
                )
        );

        Patient patient = patientRepository.save(
                TestDataFactory.patient(
                        "Security Patient",
                        "9202021111",
                        gp,
                        patientUser
                )
        );

        Diagnosis diagnosis = diagnosisRepository.save(
                TestDataFactory.diagnosis("SEC-UPD", "Update Security Diagnosis")
        );

        Examination examination = examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 2),
                        ownerDoctor,
                        patient,
                        diagnosis,
                        PaymentType.PATIENT
                )
        );

        ExaminationRequest request = new ExaminationRequest(
                LocalDate.of(2026, 5, 3),
                otherDoctor.getId(),
                patient.getId(),
                diagnosis.getId(),
                "Trying to update another doctor's examination",
                BigDecimal.valueOf(100)
        );

        assertThatThrownBy(() -> examinationService.update(examination.getId(), request))
                .isInstanceOf(AccessDeniedException.class);
    }
}