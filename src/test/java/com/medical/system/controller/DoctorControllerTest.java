package com.medical.system.controller;

import com.medical.system.TestDataFactory;
import com.medical.system.model.entity.Doctor;
import com.medical.system.model.entity.User;
import com.medical.system.model.enums.Role;
import com.medical.system.model.enums.Specialty;
import com.medical.system.repository.DoctorRepository;
import com.medical.system.repository.UserRepository;
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
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void adminShouldCreateDoctor() throws Exception {
        User doctorUser = userRepository.save(
                TestDataFactory.user("doctor_crud_create_user", Role.DOCTOR)
        );

        String body = """
                {
                  "uniqueIdentifier": "DOC-CRUD-001",
                  "fullName": "Dr. Crud Create",
                  "specialty": "CARDIOLOGY",
                  "generalPractitioner": false,
                  "userId": %d
                }
                """.formatted(doctorUser.getId());

        mockMvc.perform(
                        post("/api/doctors")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.uniqueIdentifier").value("DOC-CRUD-001"))
                .andExpect(jsonPath("$.fullName").value("Dr. Crud Create"))
                .andExpect(jsonPath("$.specialty").value("CARDIOLOGY"))
                .andExpect(jsonPath("$.generalPractitioner").value(false))
                .andExpect(jsonPath("$.userId").value(doctorUser.getId()));

        assertThat(doctorRepository.findByUniqueIdentifier("DOC-CRUD-001"))
                .isPresent();
    }

    @Test
    void adminShouldCreateGeneralPractitioner() throws Exception {
        User gpUser = userRepository.save(
                TestDataFactory.user("doctor_crud_gp_user", Role.DOCTOR)
        );

        String body = """
                {
                  "uniqueIdentifier": "GP-CRUD-001",
                  "fullName": "Dr. General Practitioner",
                  "specialty": "GENERAL_PRACTITIONER",
                  "generalPractitioner": true,
                  "userId": %d
                }
                """.formatted(gpUser.getId());

        mockMvc.perform(
                        post("/api/doctors")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uniqueIdentifier").value("GP-CRUD-001"))
                .andExpect(jsonPath("$.generalPractitioner").value(true));
    }

    @Test
    void authenticatedUserShouldGetAllDoctors() throws Exception {
        User doctorUser = userRepository.save(
                TestDataFactory.user("doctor_crud_get_user", Role.DOCTOR)
        );

        doctorRepository.save(
                TestDataFactory.doctor(
                        "DOC-CRUD-GET",
                        "Dr. Get All",
                        Specialty.CARDIOLOGY,
                        false,
                        doctorUser
                )
        );

        mockMvc.perform(
                        get("/api/doctors")
                                .with(user("patient").roles("PATIENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void authenticatedUserShouldGetDoctorById() throws Exception {
        User doctorUser = userRepository.save(
                TestDataFactory.user("doctor_crud_get_id_user", Role.DOCTOR)
        );

        Doctor doctor = doctorRepository.save(
                TestDataFactory.doctor(
                        "DOC-CRUD-ID",
                        "Dr. Get By Id",
                        Specialty.NEUROLOGY,
                        false,
                        doctorUser
                )
        );

        mockMvc.perform(
                        get("/api/doctors/" + doctor.getId())
                                .with(user("patient").roles("PATIENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(doctor.getId()))
                .andExpect(jsonPath("$.uniqueIdentifier").value("DOC-CRUD-ID"))
                .andExpect(jsonPath("$.fullName").value("Dr. Get By Id"))
                .andExpect(jsonPath("$.specialty").value("NEUROLOGY"));
    }

    @Test
    void authenticatedUserShouldGetGeneralPractitioners() throws Exception {
        User gpUser = userRepository.save(
                TestDataFactory.user("doctor_crud_gp_list_user", Role.DOCTOR)
        );

        User specialistUser = userRepository.save(
                TestDataFactory.user("doctor_crud_specialist_list_user", Role.DOCTOR)
        );

        doctorRepository.save(
                TestDataFactory.doctor(
                        "GP-CRUD-LIST",
                        "Dr. GP List",
                        Specialty.GENERAL_PRACTITIONER,
                        true,
                        gpUser
                )
        );

        doctorRepository.save(
                TestDataFactory.doctor(
                        "DOC-CRUD-LIST",
                        "Dr. Specialist List",
                        Specialty.CARDIOLOGY,
                        false,
                        specialistUser
                )
        );

        mockMvc.perform(
                        get("/api/doctors/general-practitioners")
                                .with(user("doctor").roles("DOCTOR"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void adminShouldUpdateDoctor() throws Exception {
        User doctorUser = userRepository.save(
                TestDataFactory.user("doctor_crud_update_user", Role.DOCTOR)
        );

        Doctor doctor = doctorRepository.save(
                TestDataFactory.doctor(
                        "DOC-CRUD-UPD-1",
                        "Dr. Before Update",
                        Specialty.CARDIOLOGY,
                        false,
                        doctorUser
                )
        );

        String body = """
                {
                  "uniqueIdentifier": "DOC-CRUD-UPD-2",
                  "fullName": "Dr. After Update",
                  "specialty": "NEUROLOGY",
                  "generalPractitioner": false,
                  "userId": %d
                }
                """.formatted(doctorUser.getId());

        mockMvc.perform(
                        put("/api/doctors/" + doctor.getId())
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(doctor.getId()))
                .andExpect(jsonPath("$.uniqueIdentifier").value("DOC-CRUD-UPD-2"))
                .andExpect(jsonPath("$.fullName").value("Dr. After Update"))
                .andExpect(jsonPath("$.specialty").value("NEUROLOGY"));

        Doctor updated = doctorRepository.findById(doctor.getId()).orElseThrow();

        assertThat(updated.getUniqueIdentifier()).isEqualTo("DOC-CRUD-UPD-2");
        assertThat(updated.getFullName()).isEqualTo("Dr. After Update");
        assertThat(updated.getSpecialty()).isEqualTo(Specialty.NEUROLOGY);
    }

    @Test
    void adminShouldDeleteDoctorWithoutDependencies() throws Exception {
        User doctorUser = userRepository.save(
                TestDataFactory.user("doctor_crud_delete_user", Role.DOCTOR)
        );

        Doctor doctor = doctorRepository.save(
                TestDataFactory.doctor(
                        "DOC-CRUD-DEL",
                        "Dr. Delete",
                        Specialty.CARDIOLOGY,
                        false,
                        doctorUser
                )
        );

        mockMvc.perform(
                        delete("/api/doctors/" + doctor.getId())
                                .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isNoContent());

        assertThat(doctorRepository.existsById(doctor.getId())).isFalse();
    }

    @Test
    void doctorShouldNotCreateDoctor() throws Exception {
        String body = """
                {
                  "uniqueIdentifier": "DOC-NO-CREATE",
                  "fullName": "Dr. Forbidden",
                  "specialty": "CARDIOLOGY",
                  "generalPractitioner": false,
                  "userId": null
                }
                """;

        mockMvc.perform(
                        post("/api/doctors")
                                .with(user("doctor").roles("DOCTOR"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void patientShouldNotCreateDoctor() throws Exception {
        String body = """
                {
                  "uniqueIdentifier": "DOC-PAT-NO",
                  "fullName": "Dr. Patient Forbidden",
                  "specialty": "CARDIOLOGY",
                  "generalPractitioner": false,
                  "userId": null
                }
                """;

        mockMvc.perform(
                        post("/api/doctors")
                                .with(user("patient").roles("PATIENT"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void doctorShouldNotUpdateDoctor() throws Exception {
        User doctorUser = userRepository.save(
                TestDataFactory.user("doctor_crud_no_update_user", Role.DOCTOR)
        );

        Doctor doctor = doctorRepository.save(
                TestDataFactory.doctor(
                        "DOC-NO-UPD",
                        "Dr. Cannot Update",
                        Specialty.CARDIOLOGY,
                        false,
                        doctorUser
                )
        );

        String body = """
                {
                  "uniqueIdentifier": "DOC-NO-UPD-2",
                  "fullName": "Forbidden Update",
                  "specialty": "NEUROLOGY",
                  "generalPractitioner": false,
                  "userId": null
                }
                """;

        mockMvc.perform(
                        put("/api/doctors/" + doctor.getId())
                                .with(user("doctor").roles("DOCTOR"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void patientShouldNotDeleteDoctor() throws Exception {
        User doctorUser = userRepository.save(
                TestDataFactory.user("doctor_crud_no_delete_user", Role.DOCTOR)
        );

        Doctor doctor = doctorRepository.save(
                TestDataFactory.doctor(
                        "DOC-NO-DEL",
                        "Dr. Cannot Delete",
                        Specialty.CARDIOLOGY,
                        false,
                        doctorUser
                )
        );

        mockMvc.perform(
                        delete("/api/doctors/" + doctor.getId())
                                .with(user("patient").roles("PATIENT"))
                )
                .andExpect(status().isForbidden());

        assertThat(doctorRepository.existsById(doctor.getId())).isTrue();
    }

    @Test
    void shouldReturnNotFoundWhenDoctorDoesNotExist() throws Exception {
        mockMvc.perform(
                        get("/api/doctors/999999")
                                .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Doctor not found with id: 999999"));
    }

    @Test
    void shouldReturnConflictWhenUniqueIdentifierAlreadyExists() throws Exception {
        User existingDoctorUser = userRepository.save(
                TestDataFactory.user("doctor_crud_dup_existing_user", Role.DOCTOR)
        );

        User newDoctorUser = userRepository.save(
                TestDataFactory.user("doctor_crud_dup_new_user", Role.DOCTOR)
        );

        doctorRepository.save(
                TestDataFactory.doctor(
                        "DOC-DUP-001",
                        "Dr. Existing",
                        Specialty.CARDIOLOGY,
                        false,
                        existingDoctorUser
                )
        );

        String body = """
                {
                  "uniqueIdentifier": "DOC-DUP-001",
                  "fullName": "Dr. Duplicate",
                  "specialty": "NEUROLOGY",
                  "generalPractitioner": false,
                  "userId": %d
                }
                """.formatted(newDoctorUser.getId());

        mockMvc.perform(
                        post("/api/doctors")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Doctor with unique identifier already exists: DOC-DUP-001"
                ));
    }

    @Test
    void shouldReturnBadRequestWhenLinkedUserIsNotDoctor() throws Exception {
        User patientUser = userRepository.save(
                TestDataFactory.user("doctor_crud_invalid_patient_user", Role.PATIENT)
        );

        String body = """
                {
                  "uniqueIdentifier": "DOC-BAD-USER",
                  "fullName": "Dr. Invalid User",
                  "specialty": "CARDIOLOGY",
                  "generalPractitioner": false,
                  "userId": %d
                }
                """.formatted(patientUser.getId());

        mockMvc.perform(
                        post("/api/doctors")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Selected user must have DOCTOR role"));
    }
}