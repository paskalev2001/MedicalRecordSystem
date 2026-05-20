package com.medical.system.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ValidationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnBadRequestWhenPatientEgnIsInvalid() throws Exception {
        String body = """
                {
                  "fullName": "Invalid Patient",
                  "egn": "123",
                  "generalPractitionerId": 1,
                  "userId": null
                }
                """;

        mockMvc.perform(
                        post("/api/patients")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.egn").exists());
    }

    @Test
    void shouldReturnBadRequestWhenDoctorNameIsBlank() throws Exception {
        String body = """
                {
                  "uniqueIdentifier": "DOC-VALID-001",
                  "fullName": "",
                  "specialty": "CARDIOLOGY",
                  "generalPractitioner": false,
                  "userId": null
                }
                """;

        mockMvc.perform(
                        post("/api/doctors")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.fullName").exists());
    }

    @Test
    void shouldReturnBadRequestWhenDiagnosisNameIsBlank() throws Exception {
        String body = """
                {
                  "code": "VAL-001",
                  "name": "",
                  "description": "Invalid diagnosis"
                }
                """;

        mockMvc.perform(
                        post("/api/diagnoses")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }

    @Test
    void shouldReturnBadRequestWhenExaminationPriceIsNegative() throws Exception {
        String body = """
                {
                  "examinationDate": "2026-05-02",
                  "doctorId": 1,
                  "patientId": 1,
                  "diagnosisId": 1,
                  "prescribedTreatment": "Invalid price test",
                  "price": -10.00
                }
                """;

        mockMvc.perform(
                        post("/api/examinations")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.price").exists());
    }

    @Test
    void shouldReturnBadRequestWhenSickLeaveDaysAreZero() throws Exception {
        String body = """
                {
                  "examinationId": 1,
                  "startDate": "2026-05-03",
                  "numberOfDays": 0
                }
                """;

        mockMvc.perform(
                        post("/api/sick-leaves")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.numberOfDays").exists());
    }

    @Test
    void shouldReturnBadRequestWhenRegisterPasswordIsTooShort() throws Exception {
        String body = """
                {
                  "username": "shortpassuser",
                  "email": "shortpass@test.com",
                  "password": "123"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    void shouldReturnBadRequestWhenHealthInsuranceMonthFormatIsInvalid() throws Exception {
        String body = """
                {
                  "patientId": 1,
                  "month": "05-2026",
                  "insured": true
                }
                """;

        mockMvc.perform(
                        post("/api/health-insurance-records")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.month").exists());
    }
}