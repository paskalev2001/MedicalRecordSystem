package com.medical.system.controller;

import com.medical.system.TestDataFactory;
import com.medical.system.model.entity.Doctor;
import com.medical.system.model.entity.HealthInsuranceRecord;
import com.medical.system.model.entity.Patient;
import com.medical.system.model.entity.User;
import com.medical.system.model.enums.Role;
import com.medical.system.model.enums.Specialty;
import com.medical.system.repository.DoctorRepository;
import com.medical.system.repository.HealthInsuranceRecordRepository;
import com.medical.system.repository.PatientRepository;
import com.medical.system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
class HealthInsuranceRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HealthInsuranceRecordRepository healthInsuranceRecordRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void adminShouldCreateHealthInsuranceRecord() throws Exception {
        Patient patient = createPatient("CREATE");

        String body = """
                {
                  "patientId": %d,
                  "month": "2026-05",
                  "insured": true
                }
                """.formatted(patient.getId());

        mockMvc.perform(
                        post("/api/health-insurance-records")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.patientId").value(patient.getId()))
                .andExpect(jsonPath("$.month").value("2026-05"))
                .andExpect(jsonPath("$.insured").value(true));

        assertThat(
                healthInsuranceRecordRepository
                        .findByPatientIdOrderByMonthDesc(patient.getId())
        ).hasSize(1);
    }

    @Test
    void doctorShouldGetAllHealthInsuranceRecords() throws Exception {
        Patient patient = createPatient("GET-ALL");

        healthInsuranceRecordRepository.save(
                TestDataFactory.insuranceRecord(
                        patient,
                        YearMonth.of(2026, 5),
                        true
                )
        );

        mockMvc.perform(
                        get("/api/health-insurance-records")
                                .with(user("doctor").roles("DOCTOR"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void adminShouldGetHealthInsuranceRecordById() throws Exception {
        Patient patient = createPatient("GET-ID");

        HealthInsuranceRecord record = healthInsuranceRecordRepository.save(
                TestDataFactory.insuranceRecord(
                        patient,
                        YearMonth.of(2026, 5),
                        true
                )
        );

        mockMvc.perform(
                        get("/api/health-insurance-records/" + record.getId())
                                .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(record.getId()))
                .andExpect(jsonPath("$.patientId").value(patient.getId()))
                .andExpect(jsonPath("$.month").value("2026-05"))
                .andExpect(jsonPath("$.insured").value(true));
    }

    @Test
    void doctorShouldGetRecordsByPatientId() throws Exception {
        Patient patient = createPatient("BY-PATIENT");

        healthInsuranceRecordRepository.save(
                TestDataFactory.insuranceRecord(
                        patient,
                        YearMonth.of(2026, 5),
                        true
                )
        );

        healthInsuranceRecordRepository.save(
                TestDataFactory.insuranceRecord(
                        patient,
                        YearMonth.of(2026, 4),
                        false
                )
        );

        mockMvc.perform(
                        get("/api/health-insurance-records?patientId=" + patient.getId())
                                .with(user("doctor").roles("DOCTOR"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void patientShouldGetOwnHealthInsuranceRecordsViaMeEndpoint() throws Exception {
        Patient patient = createPatient("ME");

        healthInsuranceRecordRepository.save(
                TestDataFactory.insuranceRecord(
                        patient,
                        YearMonth.of(2026, 5),
                        true
                )
        );

        mockMvc.perform(
                        get("/api/health-insurance-records/me")
                                .with(user(patient.getUser().getUsername()).roles("PATIENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].patientId").value(patient.getId()))
                .andExpect(jsonPath("$[0].month").value("2026-05"))
                .andExpect(jsonPath("$[0].insured").value(true));
    }

    @Test
    void patientShouldGetOwnRecordsByPatientId() throws Exception {
        Patient patient = createPatient("OWN-BY-ID");

        healthInsuranceRecordRepository.save(
                TestDataFactory.insuranceRecord(
                        patient,
                        YearMonth.of(2026, 5),
                        true
                )
        );

        mockMvc.perform(
                        get("/api/health-insurance-records?patientId=" + patient.getId())
                                .with(user(patient.getUser().getUsername()).roles("PATIENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void patientShouldNotGetAnotherPatientsRecordsByPatientId() throws Exception {
        Patient patient1 = createPatient("OWN-1");
        Patient patient2 = createPatient("OWN-2");

        healthInsuranceRecordRepository.save(
                TestDataFactory.insuranceRecord(
                        patient2,
                        YearMonth.of(2026, 5),
                        true
                )
        );

        mockMvc.perform(
                        get("/api/health-insurance-records?patientId=" + patient2.getId())
                                .with(user(patient1.getUser().getUsername()).roles("PATIENT"))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldUpdateHealthInsuranceRecord() throws Exception {
        Patient patient = createPatient("UPDATE");

        HealthInsuranceRecord record = healthInsuranceRecordRepository.save(
                TestDataFactory.insuranceRecord(
                        patient,
                        YearMonth.of(2026, 5),
                        true
                )
        );

        String body = """
                {
                  "patientId": %d,
                  "month": "2026-06",
                  "insured": false
                }
                """.formatted(patient.getId());

        mockMvc.perform(
                        put("/api/health-insurance-records/" + record.getId())
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(record.getId()))
                .andExpect(jsonPath("$.patientId").value(patient.getId()))
                .andExpect(jsonPath("$.month").value("2026-06"))
                .andExpect(jsonPath("$.insured").value(false));

        HealthInsuranceRecord updated =
                healthInsuranceRecordRepository.findById(record.getId()).orElseThrow();

        assertThat(updated.getMonth()).isEqualTo(YearMonth.of(2026, 6));
        assertThat(updated.isInsured()).isFalse();
    }

    @Test
    void adminShouldDeleteHealthInsuranceRecord() throws Exception {
        Patient patient = createPatient("DELETE");

        HealthInsuranceRecord record = healthInsuranceRecordRepository.save(
                TestDataFactory.insuranceRecord(
                        patient,
                        YearMonth.of(2026, 5),
                        true
                )
        );

        mockMvc.perform(
                        delete("/api/health-insurance-records/" + record.getId())
                                .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isNoContent());

        assertThat(healthInsuranceRecordRepository.existsById(record.getId()))
                .isFalse();
    }

    @Test
    void doctorShouldNotCreateHealthInsuranceRecord() throws Exception {
        Patient patient = createPatient("DOC-NO-CREATE");

        String body = """
                {
                  "patientId": %d,
                  "month": "2026-05",
                  "insured": true
                }
                """.formatted(patient.getId());

        mockMvc.perform(
                        post("/api/health-insurance-records")
                                .with(user("doctor").roles("DOCTOR"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void patientShouldNotCreateHealthInsuranceRecord() throws Exception {
        Patient patient = createPatient("PAT-NO-CREATE");

        String body = """
                {
                  "patientId": %d,
                  "month": "2026-05",
                  "insured": true
                }
                """.formatted(patient.getId());

        mockMvc.perform(
                        post("/api/health-insurance-records")
                                .with(user("patient").roles("PATIENT"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void doctorShouldNotUpdateHealthInsuranceRecord() throws Exception {
        Patient patient = createPatient("DOC-NO-UPDATE");

        HealthInsuranceRecord record = healthInsuranceRecordRepository.save(
                TestDataFactory.insuranceRecord(
                        patient,
                        YearMonth.of(2026, 5),
                        true
                )
        );

        String body = """
                {
                  "patientId": %d,
                  "month": "2026-06",
                  "insured": false
                }
                """.formatted(patient.getId());

        mockMvc.perform(
                        put("/api/health-insurance-records/" + record.getId())
                                .with(user("doctor").roles("DOCTOR"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void patientShouldNotDeleteHealthInsuranceRecord() throws Exception {
        Patient patient = createPatient("PAT-NO-DELETE");

        HealthInsuranceRecord record = healthInsuranceRecordRepository.save(
                TestDataFactory.insuranceRecord(
                        patient,
                        YearMonth.of(2026, 5),
                        true
                )
        );

        mockMvc.perform(
                        delete("/api/health-insurance-records/" + record.getId())
                                .with(user("patient").roles("PATIENT"))
                )
                .andExpect(status().isForbidden());

        assertThat(healthInsuranceRecordRepository.existsById(record.getId()))
                .isTrue();
    }

    @Test
    void shouldReturnNotFoundWhenHealthInsuranceRecordDoesNotExist() throws Exception {
        mockMvc.perform(
                        get("/api/health-insurance-records/999999")
                                .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Health insurance record not found with id: 999999"
                ));
    }

    @Test
    void shouldReturnNotFoundWhenPatientDoesNotExistOnCreate() throws Exception {
        String body = """
                {
                  "patientId": 999999,
                  "month": "2026-05",
                  "insured": true
                }
                """;

        mockMvc.perform(
                        post("/api/health-insurance-records")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Patient not found with id: 999999"));
    }

    private Patient createPatient(String suffix) {
        Doctor gp = createGeneralPractitioner("GP-" + suffix);

        User patientUser = userRepository.save(
                TestDataFactory.user("hi_patient_" + shortKey(suffix), Role.PATIENT)
        );

        return patientRepository.save(
                TestDataFactory.patient(
                        "Health Insurance Patient " + suffix,
                        generateEgn(suffix),
                        gp,
                        patientUser
                )
        );
    }

    private Doctor createGeneralPractitioner(String suffix) {
        String key = shortKey(suffix);

        User gpUser = userRepository.save(
                TestDataFactory.user("hi_gp_" + key, Role.DOCTOR)
        );

        return doctorRepository.save(
                TestDataFactory.doctor(
                        "HI-GP-" + key,
                        "Dr. HI GP " + key,
                        Specialty.GENERAL_PRACTITIONER,
                        true,
                        gpUser
                )
        );
    }

    private String shortKey(String value) {
        return String.valueOf(Math.abs(value.hashCode()));
    }

    private String generateEgn(String value) {
        int number = Math.abs(value.hashCode() % 1_000_000);
        return "98" + String.format("%08d", number).substring(0, 8);
    }
}