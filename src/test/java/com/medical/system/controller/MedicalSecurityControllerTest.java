package com.medical.system.controller;

import com.medical.system.TestDataFactory;
import com.medical.system.model.entity.*;
import com.medical.system.model.enums.PaymentType;
import com.medical.system.model.enums.Role;
import com.medical.system.model.enums.Specialty;
import com.medical.system.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class MedicalSecurityControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Autowired
    private SickLeaveRepository sickLeaveRepository;

    @Test
    void patientShouldNotCreateExamination() throws Exception {
        TestData data = createTestData("PATIENT-NO-CREATE");

        String body = """
                {
                  "examinationDate": "2026-05-02",
                  "doctorId": %d,
                  "patientId": %d,
                  "diagnosisId": %d,
                  "prescribedTreatment": "Patient should not create this.",
                  "price": 80.00
                }
                """.formatted(
                data.doctor1().getId(),
                data.patient().getId(),
                data.diagnosis().getId()
        );

        mockMvc.perform(
                        post("/api/examinations")
                                .with(user(data.patientUser().getUsername()).roles("PATIENT"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void doctorShouldCreateExaminationWithOwnDoctorProfile() throws Exception {
        TestData data = createTestData("DOCTOR-CREATE-OWN");

        String body = """
                {
                  "examinationDate": "2026-05-02",
                  "doctorId": %d,
                  "patientId": %d,
                  "diagnosisId": %d,
                  "prescribedTreatment": "Valid doctor examination.",
                  "price": 80.00
                }
                """.formatted(
                data.doctor1().getId(),
                data.patient().getId(),
                data.diagnosis().getId()
        );

        mockMvc.perform(
                        post("/api/examinations")
                                .with(user(data.doctorUser1().getUsername()).roles("DOCTOR"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.doctor.id").value(data.doctor1().getId()))
                .andExpect(jsonPath("$.patient.id").value(data.patient().getId()))
                .andExpect(jsonPath("$.paymentType").value("PATIENT"));
    }

    @Test
    void doctorShouldNotCreateExaminationWithAnotherDoctorProfile() throws Exception {
        TestData data = createTestData("DOCTOR-CREATE-OTHER");

        String body = """
                {
                  "examinationDate": "2026-05-02",
                  "doctorId": %d,
                  "patientId": %d,
                  "diagnosisId": %d,
                  "prescribedTreatment": "Doctor tries to create as another doctor.",
                  "price": 80.00
                }
                """.formatted(
                data.doctor2().getId(),
                data.patient().getId(),
                data.diagnosis().getId()
        );

        mockMvc.perform(
                        post("/api/examinations")
                                .with(user(data.doctorUser1().getUsername()).roles("DOCTOR"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void doctorShouldNotDeleteSickLeaveForAnotherDoctorsExamination() throws Exception {
        TestData data = createTestData("SICK-LEAVE-DELETE-OTHER");

        Examination examination = examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 2),
                        data.doctor1(),
                        data.patient(),
                        data.diagnosis(),
                        PaymentType.PATIENT
                )
        );

        SickLeave sickLeave = TestDataFactory.sickLeave(examination);
        sickLeave = sickLeaveRepository.save(sickLeave);

        mockMvc.perform(
                        delete("/api/sick-leaves/" + sickLeave.getId())
                                .with(user(data.doctorUser2().getUsername()).roles("DOCTOR"))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldDeleteSickLeave() throws Exception {
        TestData data = createTestData("SICK-LEAVE-ADMIN-DELETE");

        Examination examination = examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 2),
                        data.doctor1(),
                        data.patient(),
                        data.diagnosis(),
                        PaymentType.PATIENT
                )
        );

        SickLeave sickLeave = TestDataFactory.sickLeave(examination);
        sickLeave = sickLeaveRepository.save(sickLeave);

        mockMvc.perform(
                        delete("/api/sick-leaves/" + sickLeave.getId())
                                .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isNoContent());
    }

    private String shortKey(String value) {
        return String.valueOf(Math.abs(value.hashCode()));
    }

    private TestData createTestData(String suffix) {
        String key = shortKey(suffix);

        User gpUser = userRepository.save(
                TestDataFactory.user("gp_sec_" + key, Role.DOCTOR)
        );

        User doctorUser1 = userRepository.save(
                TestDataFactory.user("doc1_sec_" + key, Role.DOCTOR)
        );

        User doctorUser2 = userRepository.save(
                TestDataFactory.user("doc2_sec_" + key, Role.DOCTOR)
        );

        User patientUser = userRepository.save(
                TestDataFactory.user("pat_sec_" + key, Role.PATIENT)
        );

        Doctor gp = doctorRepository.save(
                TestDataFactory.doctor(
                        "GP-" + key,
                        "Dr. GP " + key,
                        Specialty.GENERAL_PRACTITIONER,
                        true,
                        gpUser
                )
        );

        Doctor doctor1 = doctorRepository.save(
                TestDataFactory.doctor(
                        "D1-" + key,
                        "Dr. One " + key,
                        Specialty.CARDIOLOGY,
                        false,
                        doctorUser1
                )
        );

        Doctor doctor2 = doctorRepository.save(
                TestDataFactory.doctor(
                        "D2-" + key,
                        "Dr. Two " + key,
                        Specialty.NEUROLOGY,
                        false,
                        doctorUser2
                )
        );

        Patient patient = patientRepository.save(
                TestDataFactory.patient(
                        "Patient " + key,
                        generateEgn(key),
                        gp,
                        patientUser
                )
        );

        Diagnosis diagnosis = diagnosisRepository.save(
                TestDataFactory.diagnosis(
                        "DX-" + key,
                        "Diagnosis " + key
                )
        );

        return new TestData(
                doctorUser1,
                doctorUser2,
                patientUser,
                doctor1,
                doctor2,
                patient,
                diagnosis
        );
    }


    private String generateEgn(String value) {
        int number = Math.abs(value.hashCode() % 1_000_000);
        return "96" + String.format("%08d", number).substring(0, 8);
    }

    private record TestData(
            User doctorUser1,
            User doctorUser2,
            User patientUser,
            Doctor doctor1,
            Doctor doctor2,
            Patient patient,
            Diagnosis diagnosis
    ) {
    }
}