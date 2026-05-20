package com.medical.system.controller;

import com.medical.system.model.entity.Diagnosis;
import com.medical.system.repository.DiagnosisRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
class DiagnosisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DiagnosisRepository diagnosisRepository;

    @Test
    void adminShouldCreateDiagnosis() throws Exception {
        String body = """
                {
                  "code": "CRUD-001",
                  "name": "Crud Diagnosis",
                  "description": "Created from controller test"
                }
                """;

        mockMvc.perform(
                        post("/api/diagnoses")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").value("CRUD-001"))
                .andExpect(jsonPath("$.name").value("Crud Diagnosis"))
                .andExpect(jsonPath("$.description").value("Created from controller test"));

        assertThat(diagnosisRepository.existsByCode("CRUD-001")).isTrue();
    }

    @Test
    void authenticatedUserShouldGetAllDiagnoses() throws Exception {
        diagnosisRepository.save(createDiagnosis("CRUD-GET-1", "Diagnosis One"));
        diagnosisRepository.save(createDiagnosis("CRUD-GET-2", "Diagnosis Two"));

        mockMvc.perform(
                        get("/api/diagnoses")
                                .with(user("doctor").roles("DOCTOR"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void authenticatedUserShouldGetDiagnosisById() throws Exception {
        Diagnosis diagnosis = diagnosisRepository.save(
                createDiagnosis("CRUD-GET-ID", "Diagnosis By Id")
        );

        mockMvc.perform(
                        get("/api/diagnoses/" + diagnosis.getId())
                                .with(user("patient").roles("PATIENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(diagnosis.getId()))
                .andExpect(jsonPath("$.code").value("CRUD-GET-ID"))
                .andExpect(jsonPath("$.name").value("Diagnosis By Id"));
    }

    @Test
    void adminShouldUpdateDiagnosis() throws Exception {
        Diagnosis diagnosis = diagnosisRepository.save(
                createDiagnosis("CRUD-UPD-1", "Before Update")
        );

        String body = """
                {
                  "code": "CRUD-UPD-2",
                  "name": "After Update",
                  "description": "Updated description"
                }
                """;

        mockMvc.perform(
                        put("/api/diagnoses/" + diagnosis.getId())
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(diagnosis.getId()))
                .andExpect(jsonPath("$.code").value("CRUD-UPD-2"))
                .andExpect(jsonPath("$.name").value("After Update"))
                .andExpect(jsonPath("$.description").value("Updated description"));

        Diagnosis updated = diagnosisRepository.findById(diagnosis.getId()).orElseThrow();

        assertThat(updated.getCode()).isEqualTo("CRUD-UPD-2");
        assertThat(updated.getName()).isEqualTo("After Update");
    }

    @Test
    void adminShouldDeleteDiagnosis() throws Exception {
        Diagnosis diagnosis = diagnosisRepository.save(
                createDiagnosis("CRUD-DEL-1", "Delete Diagnosis")
        );

        mockMvc.perform(
                        delete("/api/diagnoses/" + diagnosis.getId())
                                .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isNoContent());

        assertThat(diagnosisRepository.existsById(diagnosis.getId())).isFalse();
    }

    @Test
    void doctorShouldNotCreateDiagnosis() throws Exception {
        String body = """
                {
                  "code": "CRUD-DOC-NO",
                  "name": "Doctor Forbidden",
                  "description": "Doctor should not create diagnosis"
                }
                """;

        mockMvc.perform(
                        post("/api/diagnoses")
                                .with(user("doctor").roles("DOCTOR"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void patientShouldNotCreateDiagnosis() throws Exception {
        String body = """
                {
                  "code": "CRUD-PAT-NO",
                  "name": "Patient Forbidden",
                  "description": "Patient should not create diagnosis"
                }
                """;

        mockMvc.perform(
                        post("/api/diagnoses")
                                .with(user("patient").roles("PATIENT"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void doctorShouldNotUpdateDiagnosis() throws Exception {
        Diagnosis diagnosis = diagnosisRepository.save(
                createDiagnosis("CRUD-DOC-UPD", "Doctor Cannot Update")
        );

        String body = """
                {
                  "code": "CRUD-DOC-UPD-2",
                  "name": "Forbidden Update",
                  "description": "Doctor should not update diagnosis"
                }
                """;

        mockMvc.perform(
                        put("/api/diagnoses/" + diagnosis.getId())
                                .with(user("doctor").roles("DOCTOR"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void patientShouldNotDeleteDiagnosis() throws Exception {
        Diagnosis diagnosis = diagnosisRepository.save(
                createDiagnosis("CRUD-PAT-DEL", "Patient Cannot Delete")
        );

        mockMvc.perform(
                        delete("/api/diagnoses/" + diagnosis.getId())
                                .with(user("patient").roles("PATIENT"))
                )
                .andExpect(status().isForbidden());

        assertThat(diagnosisRepository.existsById(diagnosis.getId())).isTrue();
    }

    @Test
    void shouldReturnNotFoundWhenDiagnosisDoesNotExist() throws Exception {
        mockMvc.perform(
                        get("/api/diagnoses/999999")
                                .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Diagnosis not found with id: 999999"));
    }

    @Test
    void shouldReturnConflictWhenDiagnosisCodeAlreadyExists() throws Exception {
        diagnosisRepository.save(
                createDiagnosis("CRUD-DUP", "Original Diagnosis")
        );

        String body = """
                {
                  "code": "CRUD-DUP",
                  "name": "Duplicate Diagnosis",
                  "description": "Duplicate code"
                }
                """;

        mockMvc.perform(
                        post("/api/diagnoses")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Diagnosis with code already exists: CRUD-DUP"));
    }

    private Diagnosis createDiagnosis(String code, String name) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setCode(code);
        diagnosis.setName(name);
        diagnosis.setDescription(name + " description");
        return diagnosis;
    }
}