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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DeleteProtectionTest {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DiagnosisService diagnosisService;

    @Autowired
    private ExaminationService examinationService;

    @Autowired
    private SickLeaveRepository sickLeaveRepository;

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
    @WithMockUser(username = "admin", roles = "ADMIN")
    void diagnosisUsedInExaminationShouldNotBeDeleted() {
        TestData data = createBasicMedicalData("DEL-DIAG");

        examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 2),
                        data.doctor(),
                        data.patient(),
                        data.diagnosis(),
                        PaymentType.PATIENT
                )
        );

        assertThatThrownBy(() -> diagnosisService.delete(data.diagnosis().getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Diagnosis cannot be deleted");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void doctorWithExaminationsShouldNotBeDeleted() {
        TestData data = createBasicMedicalData("DEL-DOC-EXAM");

        examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 2),
                        data.doctor(),
                        data.patient(),
                        data.diagnosis(),
                        PaymentType.PATIENT
                )
        );

        assertThatThrownBy(() -> doctorService.delete(data.doctor().getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Doctor cannot be deleted");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void generalPractitionerWithRegisteredPatientsShouldNotBeDeleted() {
        TestData data = createBasicMedicalData("DEL-GP");

        assertThatThrownBy(() -> doctorService.delete(data.gp().getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("patients registered");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void patientWithExaminationsShouldNotBeDeleted() {
        TestData data = createBasicMedicalData("DEL-PATIENT");

        examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 2),
                        data.doctor(),
                        data.patient(),
                        data.diagnosis(),
                        PaymentType.PATIENT
                )
        );

        assertThatThrownBy(() -> patientService.delete(data.patient().getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Patient cannot be deleted");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void examinationWithSickLeaveShouldNotBeDeleted() {
        TestData data = createBasicMedicalData("DEL-EXAM");

        Examination examination = examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 2),
                        data.doctor(),
                        data.patient(),
                        data.diagnosis(),
                        PaymentType.PATIENT
                )
        );

        SickLeave sickLeave = TestDataFactory.sickLeave(examination);
        sickLeaveRepository.save(sickLeave);

        assertThatThrownBy(() -> examinationService.delete(examination.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Examination cannot be deleted");
    }

    private TestData createBasicMedicalData(String suffix) {
        User gpUser = userRepository.save(
                TestDataFactory.user("gp_" + suffix, Role.DOCTOR)
        );

        User doctorUser = userRepository.save(
                TestDataFactory.user("doctor_" + suffix, Role.DOCTOR)
        );

        User patientUser = userRepository.save(
                TestDataFactory.user("patient_" + suffix, Role.PATIENT)
        );

        Doctor gp = doctorRepository.save(
                TestDataFactory.doctor(
                        "GP-" + suffix,
                        "Dr. GP " + suffix,
                        Specialty.GENERAL_PRACTITIONER,
                        true,
                        gpUser
                )
        );

        Doctor doctor = doctorRepository.save(
                TestDataFactory.doctor(
                        "DOC-" + suffix,
                        "Dr. Specialist " + suffix,
                        Specialty.CARDIOLOGY,
                        false,
                        doctorUser
                )
        );

        Patient patient = patientRepository.save(
                TestDataFactory.patient(
                        "Patient " + suffix,
                        generateEgnFromSuffix(suffix),
                        gp,
                        patientUser
                )
        );

        Diagnosis diagnosis = diagnosisRepository.save(
                TestDataFactory.diagnosis(
                        "D-" + suffix,
                        "Diagnosis " + suffix
                )
        );

        return new TestData(gp, doctor, patient, diagnosis);
    }

    private String generateEgnFromSuffix(String suffix) {
        int number = Math.abs(suffix.hashCode() % 1_000_000);
        return "94" + String.format("%08d", number).substring(0, 8);
    }

    private record TestData(
            Doctor gp,
            Doctor doctor,
            Patient patient,
            Diagnosis diagnosis
    ) {
    }
}