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
class SickLeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SickLeaveRepository sickLeaveRepository;

    @Autowired
    private ExaminationRepository examinationRepository;

    @Autowired
    private DiagnosisRepository diagnosisRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void doctorShouldCreateSickLeaveForOwnExamination() throws Exception {
        TestData data = createTestData("CREATE-DOCTOR");

        Examination examination = createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        String body = """
                {
                  "examinationId": %d,
                  "startDate": "2026-05-03",
                  "numberOfDays": 5
                }
                """.formatted(examination.getId());

        mockMvc.perform(
                        post("/api/sick-leaves")
                                .with(user(data.doctorUser1().getUsername()).roles("DOCTOR"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.examinationId").value(examination.getId()))
                .andExpect(jsonPath("$.startDate").value("2026-05-03"))
                .andExpect(jsonPath("$.numberOfDays").value(5));

        assertThat(sickLeaveRepository.existsByExaminationId(examination.getId()))
                .isTrue();
    }

    @Test
    void adminShouldCreateSickLeave() throws Exception {
        TestData data = createTestData("CREATE-ADMIN");

        Examination examination = createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        String body = """
                {
                  "examinationId": %d,
                  "startDate": "2026-05-03",
                  "numberOfDays": 7
                }
                """.formatted(examination.getId());

        mockMvc.perform(
                        post("/api/sick-leaves")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.examinationId").value(examination.getId()))
                .andExpect(jsonPath("$.numberOfDays").value(7));
    }

    @Test
    void patientShouldNotCreateSickLeave() throws Exception {
        TestData data = createTestData("PATIENT-NO-CREATE");

        Examination examination = createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        String body = """
                {
                  "examinationId": %d,
                  "startDate": "2026-05-03",
                  "numberOfDays": 5
                }
                """.formatted(examination.getId());

        mockMvc.perform(
                        post("/api/sick-leaves")
                                .with(user(data.patientUser().getUsername()).roles("PATIENT"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void doctorShouldGetAllSickLeaves() throws Exception {
        TestData data = createTestData("GET-ALL");

        Examination examination = createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        sickLeaveRepository.save(TestDataFactory.sickLeave(examination));

        mockMvc.perform(
                        get("/api/sick-leaves")
                                .with(user(data.doctorUser1().getUsername()).roles("DOCTOR"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void adminShouldGetSickLeaveById() throws Exception {
        TestData data = createTestData("GET-ID");

        Examination examination = createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        SickLeave sickLeave = sickLeaveRepository.save(
                TestDataFactory.sickLeave(examination)
        );

        mockMvc.perform(
                        get("/api/sick-leaves/" + sickLeave.getId())
                                .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sickLeave.getId()))
                .andExpect(jsonPath("$.examinationId").value(examination.getId()));
    }

    @Test
    void patientShouldGetOwnSickLeavesViaMeEndpoint() throws Exception {
        TestData data = createTestData("PATIENT-ME");

        Examination examination = createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        sickLeaveRepository.save(TestDataFactory.sickLeave(examination));

        mockMvc.perform(
                        get("/api/sick-leaves/me")
                                .with(user(data.patientUser().getUsername()).roles("PATIENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].examinationId").value(examination.getId()));
    }

    @Test
    void doctorShouldGetSickLeavesByDoctorId() throws Exception {
        TestData data = createTestData("BY-DOCTOR");

        Examination examination = createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        sickLeaveRepository.save(TestDataFactory.sickLeave(examination));

        mockMvc.perform(
                        get("/api/sick-leaves?doctorId=" + data.doctor1().getId())
                                .with(user(data.doctorUser1().getUsername()).roles("DOCTOR"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void doctorShouldGetSickLeavesByPatientId() throws Exception {
        TestData data = createTestData("BY-PATIENT");

        Examination examination = createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        sickLeaveRepository.save(TestDataFactory.sickLeave(examination));

        mockMvc.perform(
                        get("/api/sick-leaves?patientId=" + data.patient().getId())
                                .with(user(data.doctorUser1().getUsername()).roles("DOCTOR"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void doctorShouldUpdateOwnSickLeave() throws Exception {
        TestData data = createTestData("UPDATE-OWN");

        Examination examination = createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        SickLeave sickLeave = sickLeaveRepository.save(
                TestDataFactory.sickLeave(examination)
        );

        String body = """
                {
                  "examinationId": %d,
                  "startDate": "2026-05-04",
                  "numberOfDays": 10
                }
                """.formatted(examination.getId());

        mockMvc.perform(
                        put("/api/sick-leaves/" + sickLeave.getId())
                                .with(user(data.doctorUser1().getUsername()).roles("DOCTOR"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sickLeave.getId()))
                .andExpect(jsonPath("$.startDate").value("2026-05-04"))
                .andExpect(jsonPath("$.numberOfDays").value(10));
    }

    @Test
    void doctorShouldNotUpdateAnotherDoctorsSickLeave() throws Exception {
        TestData data = createTestData("UPDATE-OTHER");

        Examination examination = createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        SickLeave sickLeave = sickLeaveRepository.save(
                TestDataFactory.sickLeave(examination)
        );

        String body = """
                {
                  "examinationId": %d,
                  "startDate": "2026-05-04",
                  "numberOfDays": 10
                }
                """.formatted(examination.getId());

        mockMvc.perform(
                        put("/api/sick-leaves/" + sickLeave.getId())
                                .with(user(data.doctorUser2().getUsername()).roles("DOCTOR"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldDeleteSickLeave() throws Exception {
        TestData data = createTestData("DELETE-ADMIN");

        Examination examination = createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        SickLeave sickLeave = sickLeaveRepository.save(
                TestDataFactory.sickLeave(examination)
        );

        mockMvc.perform(
                        delete("/api/sick-leaves/" + sickLeave.getId())
                                .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isNoContent());

        assertThat(sickLeaveRepository.existsById(sickLeave.getId())).isFalse();
    }

    @Test
    void patientShouldNotDeleteSickLeave() throws Exception {
        TestData data = createTestData("DELETE-PATIENT-NO");

        Examination examination = createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        SickLeave sickLeave = sickLeaveRepository.save(
                TestDataFactory.sickLeave(examination)
        );

        mockMvc.perform(
                        delete("/api/sick-leaves/" + sickLeave.getId())
                                .with(user(data.patientUser().getUsername()).roles("PATIENT"))
                )
                .andExpect(status().isForbidden());

        assertThat(sickLeaveRepository.existsById(sickLeave.getId())).isTrue();
    }

    @Test
    void shouldReturnConflictWhenSickLeaveAlreadyExistsForExamination() throws Exception {
        TestData data = createTestData("DUPLICATE");

        Examination examination = createExamination(
                data.doctor1(),
                data.patient(),
                data.diagnosis()
        );

        sickLeaveRepository.save(TestDataFactory.sickLeave(examination));

        String body = """
                {
                  "examinationId": %d,
                  "startDate": "2026-05-03",
                  "numberOfDays": 5
                }
                """.formatted(examination.getId());

        mockMvc.perform(
                        post("/api/sick-leaves")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Sick leave already exists for examination id: " + examination.getId()
                ));
    }

    @Test
    void shouldReturnNotFoundWhenExaminationDoesNotExistOnCreate() throws Exception {
        String body = """
                {
                  "examinationId": 999999,
                  "startDate": "2026-05-03",
                  "numberOfDays": 5
                }
                """;

        mockMvc.perform(
                        post("/api/sick-leaves")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Examination not found with id: 999999"));
    }

    @Test
    void shouldReturnNotFoundWhenSickLeaveDoesNotExist() throws Exception {
        mockMvc.perform(
                        get("/api/sick-leaves/999999")
                                .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Sick leave not found with id: 999999"));
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

    private TestData createTestData(String suffix) {
        String key = shortKey(suffix);

        User gpUser = userRepository.save(
                TestDataFactory.user("sl_gp_" + key, Role.DOCTOR)
        );

        User doctorUser1 = userRepository.save(
                TestDataFactory.user("sl_doc1_" + key, Role.DOCTOR)
        );

        User doctorUser2 = userRepository.save(
                TestDataFactory.user("sl_doc2_" + key, Role.DOCTOR)
        );

        User patientUser = userRepository.save(
                TestDataFactory.user("sl_pat_" + key, Role.PATIENT)
        );

        Doctor gp = doctorRepository.save(
                TestDataFactory.doctor(
                        "SL-GP-" + key,
                        "Dr. SL GP " + key,
                        Specialty.GENERAL_PRACTITIONER,
                        true,
                        gpUser
                )
        );

        Doctor doctor1 = doctorRepository.save(
                TestDataFactory.doctor(
                        "SL-D1-" + key,
                        "Dr. SL One " + key,
                        Specialty.CARDIOLOGY,
                        false,
                        doctorUser1
                )
        );

        Doctor doctor2 = doctorRepository.save(
                TestDataFactory.doctor(
                        "SL-D2-" + key,
                        "Dr. SL Two " + key,
                        Specialty.NEUROLOGY,
                        false,
                        doctorUser2
                )
        );

        Patient patient = patientRepository.save(
                TestDataFactory.patient(
                        "SL Patient " + key,
                        generateEgn(key),
                        gp,
                        patientUser
                )
        );

        Diagnosis diagnosis = diagnosisRepository.save(
                TestDataFactory.diagnosis(
                        "SL-DX-" + key,
                        "SL Diagnosis " + key
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
        return "99" + String.format("%08d", number).substring(0, 8);
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