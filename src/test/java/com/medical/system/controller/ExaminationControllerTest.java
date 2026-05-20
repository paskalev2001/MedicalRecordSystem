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
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ExaminationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExaminationRepository examinationRepository;

    @Autowired
    private HealthInsuranceRecordRepository healthInsuranceRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DiagnosisRepository diagnosisRepository;

    @Test
    void doctorShouldCreateExaminationWithOwnDoctorProfile() throws Exception {
        TestData data = createTestData("CREATE-OWN");

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
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.doctor.id").value(data.doctor1().getId()))
                .andExpect(jsonPath("$.patient.id").value(data.patient().getId()))
                .andExpect(jsonPath("$.diagnosis.id").value(data.diagnosis().getId()))
                .andExpect(jsonPath("$.paymentType").value("PATIENT"));

        assertThat(examinationRepository.findAll()).hasSize(1);
    }

    @Test
    void adminShouldCreateExaminationForInsuredPatientPaidByNhif() throws Exception {
        TestData data = createTestData("CREATE-NHIF");

        addSixInsuredMonths(data.patient(), LocalDate.of(2026, 5, 2));

        String body = """
                {
                  "examinationDate": "2026-05-02",
                  "doctorId": %d,
                  "patientId": %d,
                  "diagnosisId": %d,
                  "prescribedTreatment": "NHIF payment test.",
                  "price": 80.00
                }
                """.formatted(
                data.doctor1().getId(),
                data.patient().getId(),
                data.diagnosis().getId()
        );

        mockMvc.perform(
                        post("/api/examinations")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentType").value("NHIF"));
    }

    @Test
    void doctorShouldNotCreateExaminationWithAnotherDoctorProfile() throws Exception {
        TestData data = createTestData("CREATE-OTHER");

        String body = """
                {
                  "examinationDate": "2026-05-02",
                  "doctorId": %d,
                  "patientId": %d,
                  "diagnosisId": %d,
                  "prescribedTreatment": "Doctor tries another doctor profile.",
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
    void doctorShouldGetAllExaminations() throws Exception {
        TestData data = createTestData("GET-ALL");

        Examination examination = createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        mockMvc.perform(
                        get("/api/examinations")
                                .with(user(data.doctorUser1().getUsername()).roles("DOCTOR"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        assertThat(examinationRepository.existsById(examination.getId())).isTrue();
    }

    @Test
    void adminShouldGetExaminationById() throws Exception {
        TestData data = createTestData("GET-ID");

        Examination examination = createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        mockMvc.perform(
                        get("/api/examinations/" + examination.getId())
                                .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(examination.getId()))
                .andExpect(jsonPath("$.doctor.id").value(data.doctor1().getId()))
                .andExpect(jsonPath("$.patient.id").value(data.patient().getId()))
                .andExpect(jsonPath("$.diagnosis.id").value(data.diagnosis().getId()));
    }

    @Test
    void patientShouldGetOwnExaminationById() throws Exception {
        TestData data = createTestData("PATIENT-OWN-ID");

        Examination examination = createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        mockMvc.perform(
                        get("/api/examinations/" + examination.getId())
                                .with(user(data.patientUser().getUsername()).roles("PATIENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(examination.getId()));
    }

    @Test
    void patientShouldNotGetAnotherPatientExaminationById() throws Exception {
        TestData data1 = createTestData("PATIENT-OTHER-1");
        TestData data2 = createTestData("PATIENT-OTHER-2");

        Examination examinationForPatient2 = createExamination(
                data2.doctor1(),
                data2.patient(),
                data2.diagnosis()
        );

        mockMvc.perform(
                        get("/api/examinations/" + examinationForPatient2.getId())
                                .with(user(data1.patientUser().getUsername()).roles("PATIENT"))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void patientShouldGetOwnExaminationsViaMeEndpoint() throws Exception {
        TestData data = createTestData("ME");

        Examination examination = createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        mockMvc.perform(
                        get("/api/examinations/me")
                                .with(user(data.patientUser().getUsername()).roles("PATIENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(examination.getId()));
    }

    @Test
    void patientShouldGetOwnExaminationsByPatientId() throws Exception {
        TestData data = createTestData("BY-PATIENT-OWN");

        createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        mockMvc.perform(
                        get("/api/examinations?patientId=" + data.patient().getId())
                                .with(user(data.patientUser().getUsername()).roles("PATIENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void patientShouldNotGetAnotherPatientsExaminationsByPatientId() throws Exception {
        TestData data1 = createTestData("BY-PATIENT-OTHER-1");
        TestData data2 = createTestData("BY-PATIENT-OTHER-2");

        createExamination(
                data2.doctor1(),
                data2.patient(),
                data2.diagnosis()
        );

        mockMvc.perform(
                        get("/api/examinations?patientId=" + data2.patient().getId())
                                .with(user(data1.patientUser().getUsername()).roles("PATIENT"))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void doctorShouldGetExaminationsByDoctorId() throws Exception {
        TestData data = createTestData("BY-DOCTOR");

        createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        mockMvc.perform(
                        get("/api/examinations?doctorId=" + data.doctor1().getId())
                                .with(user(data.doctorUser1().getUsername()).roles("DOCTOR"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void doctorShouldGetExaminationsByDoctorAndPeriod() throws Exception {
        TestData data = createTestData("BY-DOCTOR-PERIOD");

        createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        mockMvc.perform(
                        get("/api/examinations")
                                .param("doctorId", String.valueOf(data.doctor1().getId()))
                                .param("startDate", "2026-05-01")
                                .param("endDate", "2026-05-31")
                                .with(user(data.doctorUser1().getUsername()).roles("DOCTOR"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void doctorShouldUpdateOwnExamination() throws Exception {
        TestData data = createTestData("UPDATE-OWN");

        Examination examination = createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        String body = """
                {
                  "examinationDate": "2026-05-03",
                  "doctorId": %d,
                  "patientId": %d,
                  "diagnosisId": %d,
                  "prescribedTreatment": "Updated treatment.",
                  "price": 90.00
                }
                """.formatted(
                data.doctor1().getId(),
                data.patient().getId(),
                data.diagnosis().getId()
        );

        mockMvc.perform(
                        put("/api/examinations/" + examination.getId())
                                .with(user(data.doctorUser1().getUsername()).roles("DOCTOR"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(examination.getId()))
                .andExpect(jsonPath("$.examinationDate").value("2026-05-03"))
                .andExpect(jsonPath("$.prescribedTreatment").value("Updated treatment."))
                .andExpect(jsonPath("$.price").value(90.00));
    }

    @Test
    void doctorShouldNotUpdateAnotherDoctorsExamination() throws Exception {
        TestData data = createTestData("UPDATE-OTHER");

        Examination examination = createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        String body = """
                {
                  "examinationDate": "2026-05-03",
                  "doctorId": %d,
                  "patientId": %d,
                  "diagnosisId": %d,
                  "prescribedTreatment": "Forbidden update.",
                  "price": 90.00
                }
                """.formatted(
                data.doctor1().getId(),
                data.patient().getId(),
                data.diagnosis().getId()
        );

        mockMvc.perform(
                        put("/api/examinations/" + examination.getId())
                                .with(user(data.doctorUser2().getUsername()).roles("DOCTOR"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldDeleteExamination() throws Exception {
        TestData data = createTestData("DELETE-ADMIN");

        Examination examination = createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        mockMvc.perform(
                        delete("/api/examinations/" + examination.getId())
                                .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isNoContent());

        assertThat(examinationRepository.existsById(examination.getId())).isFalse();
    }

    @Test
    void patientShouldNotDeleteExamination() throws Exception {
        TestData data = createTestData("DELETE-PATIENT-NO");

        Examination examination = createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        mockMvc.perform(
                        delete("/api/examinations/" + examination.getId())
                                .with(user(data.patientUser().getUsername()).roles("PATIENT"))
                )
                .andExpect(status().isForbidden());

        assertThat(examinationRepository.existsById(examination.getId())).isTrue();
    }

    @Test
    void shouldReturnNotFoundWhenExaminationDoesNotExist() throws Exception {
        mockMvc.perform(
                        get("/api/examinations/999999")
                                .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Examination not found with id: 999999"));
    }

    @Test
    void shouldReturnNotFoundWhenDoctorDoesNotExistOnCreate() throws Exception {
        TestData data = createTestData("NO-DOCTOR");

        String body = """
                {
                  "examinationDate": "2026-05-02",
                  "doctorId": 999999,
                  "patientId": %d,
                  "diagnosisId": %d,
                  "prescribedTreatment": "Missing doctor.",
                  "price": 80.00
                }
                """.formatted(
                data.patient().getId(),
                data.diagnosis().getId()
        );

        mockMvc.perform(
                        post("/api/examinations")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Doctor not found with id: 999999"));
    }

    @Test
    void shouldReturnNotFoundWhenPatientDoesNotExistOnCreate() throws Exception {
        TestData data = createTestData("NO-PATIENT");

        String body = """
                {
                  "examinationDate": "2026-05-02",
                  "doctorId": %d,
                  "patientId": 999999,
                  "diagnosisId": %d,
                  "prescribedTreatment": "Missing patient.",
                  "price": 80.00
                }
                """.formatted(
                data.doctor1().getId(),
                data.diagnosis().getId()
        );

        mockMvc.perform(
                        post("/api/examinations")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Patient not found with id: 999999"));
    }

    @Test
    void shouldReturnNotFoundWhenDiagnosisDoesNotExistOnCreate() throws Exception {
        TestData data = createTestData("NO-DIAGNOSIS");

        String body = """
                {
                  "examinationDate": "2026-05-02",
                  "doctorId": %d,
                  "patientId": %d,
                  "diagnosisId": 999999,
                  "prescribedTreatment": "Missing diagnosis.",
                  "price": 80.00
                }
                """.formatted(
                data.doctor1().getId(),
                data.patient().getId()
        );

        mockMvc.perform(
                        post("/api/examinations")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Diagnosis not found with id: 999999"));
    }

    private Examination createExamination(
            Doctor doctor,
            Patient patient,
            Diagnosis diagnosis
    ) {
        return examinationRepository.save(
                TestDataFactory.examination(
                        LocalDate.of(2026, 5, 2),
                        doctor,
                        patient,
                        diagnosis,
                        PaymentType.PATIENT
                )
        );
    }

    private void addSixInsuredMonths(Patient patient, LocalDate referenceDate) {
        YearMonth referenceMonth = YearMonth.from(referenceDate);

        for (int i = 0; i < 6; i++) {
            healthInsuranceRecordRepository.save(
                    TestDataFactory.insuranceRecord(
                            patient,
                            referenceMonth.minusMonths(i),
                            true
                    )
            );
        }
    }

    private TestData createTestData(String suffix) {
        String key = shortKey(suffix);

        User gpUser = userRepository.save(
                TestDataFactory.user("ex_gp_" + key, Role.DOCTOR)
        );

        User doctorUser1 = userRepository.save(
                TestDataFactory.user("ex_doc1_" + key, Role.DOCTOR)
        );

        User doctorUser2 = userRepository.save(
                TestDataFactory.user("ex_doc2_" + key, Role.DOCTOR)
        );

        User patientUser = userRepository.save(
                TestDataFactory.user("ex_pat_" + key, Role.PATIENT)
        );

        Doctor gp = doctorRepository.save(
                TestDataFactory.doctor(
                        "EX-GP-" + key,
                        "Dr. EX GP " + key,
                        Specialty.GENERAL_PRACTITIONER,
                        true,
                        gpUser
                )
        );

        Doctor doctor1 = doctorRepository.save(
                TestDataFactory.doctor(
                        "EX-D1-" + key,
                        "Dr. EX One " + key,
                        Specialty.CARDIOLOGY,
                        false,
                        doctorUser1
                )
        );

        Doctor doctor2 = doctorRepository.save(
                TestDataFactory.doctor(
                        "EX-D2-" + key,
                        "Dr. EX Two " + key,
                        Specialty.NEUROLOGY,
                        false,
                        doctorUser2
                )
        );

        Patient patient = patientRepository.save(
                TestDataFactory.patient(
                        "EX Patient " + key,
                        generateEgn(key),
                        gp,
                        patientUser
                )
        );

        Diagnosis diagnosis = diagnosisRepository.save(
                TestDataFactory.diagnosis(
                        "EX-DX-" + key,
                        "EX Diagnosis " + key
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

    private String shortKey(String value) {
        return String.valueOf(Math.abs(value.hashCode()));
    }

    private String generateEgn(String value) {
        int number = Math.abs(value.hashCode() % 1_000_000);
        return "88" + String.format("%08d", number).substring(0, 8);
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