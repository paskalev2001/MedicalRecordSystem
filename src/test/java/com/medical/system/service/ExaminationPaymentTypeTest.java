package com.medical.system.service;

import com.medical.system.TestDataFactory;
import com.medical.system.dto.examination.ExaminationRequest;
import com.medical.system.dto.examination.ExaminationResponse;
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
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExaminationPaymentTypeTest {

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
    private HealthInsuranceRecordRepository healthInsuranceRecordRepository;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createExaminationForInsuredPatientShouldBePaidByNhif() {
        User gpUser = userRepository.save(TestDataFactory.user("gp_payment_1", Role.DOCTOR));
        User doctorUser = userRepository.save(TestDataFactory.user("doctor_payment_1", Role.DOCTOR));
        User patientUser = userRepository.save(TestDataFactory.user("patient_payment_1", Role.PATIENT));

        Doctor gp = doctorRepository.save(
                TestDataFactory.doctor(
                        "GP-PAY-001",
                        "Dr. GP",
                        Specialty.GENERAL_PRACTITIONER,
                        true,
                        gpUser
                )
        );

        Doctor doctor = doctorRepository.save(
                TestDataFactory.doctor(
                        "DOC-PAY-001",
                        "Dr. Specialist",
                        Specialty.CARDIOLOGY,
                        false,
                        doctorUser
                )
        );

        Patient patient = patientRepository.save(
                TestDataFactory.patient(
                        "Ivan Patient",
                        "9001011111",
                        gp,
                        patientUser
                )
        );

        Diagnosis diagnosis = diagnosisRepository.save(
                TestDataFactory.diagnosis("I10-PAY", "Hypertension Payment Test")
        );

        LocalDate examinationDate = LocalDate.of(2026, 5, 2);
        YearMonth referenceMonth = YearMonth.from(examinationDate);

        for (int i = 0; i < 6; i++) {
            healthInsuranceRecordRepository.save(
                    TestDataFactory.insuranceRecord(
                            patient,
                            referenceMonth.minusMonths(i),
                            true
                    )
            );
        }

        ExaminationRequest request = new ExaminationRequest(
                examinationDate,
                doctor.getId(),
                patient.getId(),
                diagnosis.getId(),
                "Medication and monitoring",
                BigDecimal.valueOf(80)
        );

        ExaminationResponse response = examinationService.create(request);

        assertThat(response.paymentType()).isEqualTo(PaymentType.NHIF);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createExaminationForUninsuredPatientShouldBePaidByPatient() {
        User gpUser = userRepository.save(TestDataFactory.user("gp_payment_2", Role.DOCTOR));
        User doctorUser = userRepository.save(TestDataFactory.user("doctor_payment_2", Role.DOCTOR));
        User patientUser = userRepository.save(TestDataFactory.user("patient_payment_2", Role.PATIENT));

        Doctor gp = doctorRepository.save(
                TestDataFactory.doctor(
                        "GP-PAY-002",
                        "Dr. GP",
                        Specialty.GENERAL_PRACTITIONER,
                        true,
                        gpUser
                )
        );

        Doctor doctor = doctorRepository.save(
                TestDataFactory.doctor(
                        "DOC-PAY-002",
                        "Dr. Specialist",
                        Specialty.CARDIOLOGY,
                        false,
                        doctorUser
                )
        );

        Patient patient = patientRepository.save(
                TestDataFactory.patient(
                        "Maria Patient",
                        "9001012222",
                        gp,
                        patientUser
                )
        );

        Diagnosis diagnosis = diagnosisRepository.save(
                TestDataFactory.diagnosis("J10-PAY", "Influenza Payment Test")
        );

        ExaminationRequest request = new ExaminationRequest(
                LocalDate.of(2026, 5, 2),
                doctor.getId(),
                patient.getId(),
                diagnosis.getId(),
                "Rest and fluids",
                BigDecimal.valueOf(60)
        );

        ExaminationResponse response = examinationService.create(request);

        assertThat(response.paymentType()).isEqualTo(PaymentType.PATIENT);
    }
}