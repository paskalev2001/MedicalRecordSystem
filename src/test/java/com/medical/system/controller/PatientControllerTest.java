package com.medical.system.controller;

import com.medical.system.TestDataFactory;
import com.medical.system.model.entity.Doctor;
import com.medical.system.model.entity.Patient;
import com.medical.system.model.entity.User;
import com.medical.system.model.enums.Role;
import com.medical.system.model.enums.Specialty;
import com.medical.system.repository.DoctorRepository;
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
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void adminShouldCreatePatient() throws Exception {
        Doctor gp = createGeneralPractitioner("CREATE");
        User patientUser = userRepository.save(
                TestDataFactory.user("patient_crud_create_user", Role.PATIENT)
        );

        String body = """
                {
                  "fullName": "Patient Crud Create",
                  "egn": "9701010001",
                  "generalPractitionerId": %d,
                  "userId": %d
                }
                """.formatted(gp.getId(), patientUser.getId());

        mockMvc.perform(
                        post("/api/patients")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fullName").value("Patient Crud Create"))
                .andExpect(jsonPath("$.egn").value("9701010001"))
                .andExpect(jsonPath("$.generalPractitioner.id").value(gp.getId()))
                .andExpect(jsonPath("$.userId").value(patientUser.getId()));

        assertThat(patientRepository.findByEgn("9701010001")).isPresent();
    }

    @Test
    void doctorShouldGetAllPatients() throws Exception {
        Patient patient = createPatient("GET-ALL");

        mockMvc.perform(
                        get("/api/patients")
                                .with(user("doctor").roles("DOCTOR"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        assertThat(patientRepository.existsById(patient.getId())).isTrue();
    }

    @Test
    void adminShouldGetPatientById() throws Exception {
        Patient patient = createPatient("GET-ID");

        mockMvc.perform(
                        get("/api/patients/" + patient.getId())
                                .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patient.getId()))
                .andExpect(jsonPath("$.fullName").value(patient.getFullName()))
                .andExpect(jsonPath("$.egn").value(patient.getEgn()));
    }

    @Test
    void patientShouldGetOwnProfileById() throws Exception {
        Doctor gp = createGeneralPractitioner("OWN");
        User patientUser = userRepository.save(
                TestDataFactory.user("patient_own_profile_user", Role.PATIENT)
        );

        Patient patient = patientRepository.save(
                TestDataFactory.patient(
                        "Patient Own Profile",
                        "9701010002",
                        gp,
                        patientUser
                )
        );

        mockMvc.perform(
                        get("/api/patients/" + patient.getId())
                                .with(user(patientUser.getUsername()).roles("PATIENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patient.getId()))
                .andExpect(jsonPath("$.fullName").value("Patient Own Profile"));
    }

    @Test
    void patientShouldNotGetAnotherPatientProfileById() throws Exception {
        Doctor gp = createGeneralPractitioner("OTHER");

        User patientUser1 = userRepository.save(
                TestDataFactory.user("patient_other_1_user", Role.PATIENT)
        );

        User patientUser2 = userRepository.save(
                TestDataFactory.user("patient_other_2_user", Role.PATIENT)
        );

        Patient patient1 = patientRepository.save(
                TestDataFactory.patient(
                        "Patient One",
                        "9701010003",
                        gp,
                        patientUser1
                )
        );

        Patient patient2 = patientRepository.save(
                TestDataFactory.patient(
                        "Patient Two",
                        "9701010004",
                        gp,
                        patientUser2
                )
        );

        mockMvc.perform(
                        get("/api/patients/" + patient2.getId())
                                .with(user(patientUser1.getUsername()).roles("PATIENT"))
                )
                .andExpect(status().isForbidden());

        assertThat(patientRepository.existsById(patient1.getId())).isTrue();
        assertThat(patientRepository.existsById(patient2.getId())).isTrue();
    }

    @Test
    void patientShouldNotGetAllPatients() throws Exception {
        createPatient("PATIENT-NO-ALL");

        mockMvc.perform(
                        get("/api/patients")
                                .with(user("patient").roles("PATIENT"))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldGetPatientsByGeneralPractitioner() throws Exception {
        Doctor gp = createGeneralPractitioner("BY-GP");

        User patientUser1 = userRepository.save(
                TestDataFactory.user("patient_by_gp_1_user", Role.PATIENT)
        );

        User patientUser2 = userRepository.save(
                TestDataFactory.user("patient_by_gp_2_user", Role.PATIENT)
        );

        patientRepository.save(
                TestDataFactory.patient(
                        "Patient By GP One",
                        "9701010005",
                        gp,
                        patientUser1
                )
        );

        patientRepository.save(
                TestDataFactory.patient(
                        "Patient By GP Two",
                        "9701010006",
                        gp,
                        patientUser2
                )
        );

        mockMvc.perform(
                        get("/api/patients?generalPractitionerId=" + gp.getId())
                                .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void adminShouldUpdatePatient() throws Exception {
        Patient patient = createPatient("UPDATE");
        Doctor newGp = createGeneralPractitioner("NEW-GP");

        String body = """
                {
                  "fullName": "Patient After Update",
                  "egn": "9701010007",
                  "generalPractitionerId": %d,
                  "userId": %d
                }
                """.formatted(
                newGp.getId(),
                patient.getUser().getId()
        );

        mockMvc.perform(
                        put("/api/patients/" + patient.getId())
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patient.getId()))
                .andExpect(jsonPath("$.fullName").value("Patient After Update"))
                .andExpect(jsonPath("$.egn").value("9701010007"))
                .andExpect(jsonPath("$.generalPractitioner.id").value(newGp.getId()));

        Patient updated = patientRepository.findById(patient.getId()).orElseThrow();

        assertThat(updated.getFullName()).isEqualTo("Patient After Update");
        assertThat(updated.getEgn()).isEqualTo("9701010007");
        assertThat(updated.getGeneralPractitioner().getId()).isEqualTo(newGp.getId());
    }

    @Test
    void adminShouldDeletePatientWithoutDependencies() throws Exception {
        Patient patient = createPatient("DELETE");

        mockMvc.perform(
                        delete("/api/patients/" + patient.getId())
                                .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isNoContent());

        assertThat(patientRepository.existsById(patient.getId())).isFalse();
    }

    @Test
    void doctorShouldNotCreatePatient() throws Exception {
        Doctor gp = createGeneralPractitioner("DOC-NO-CREATE");

        String body = """
                {
                  "fullName": "Forbidden Patient",
                  "egn": "9701010008",
                  "generalPractitionerId": %d,
                  "userId": null
                }
                """.formatted(gp.getId());

        mockMvc.perform(
                        post("/api/patients")
                                .with(user("doctor").roles("DOCTOR"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void patientShouldNotCreatePatient() throws Exception {
        Doctor gp = createGeneralPractitioner("PAT-NO-CREATE");

        String body = """
                {
                  "fullName": "Forbidden Patient",
                  "egn": "9701010009",
                  "generalPractitionerId": %d,
                  "userId": null
                }
                """.formatted(gp.getId());

        mockMvc.perform(
                        post("/api/patients")
                                .with(user("patient").roles("PATIENT"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void doctorShouldNotUpdatePatient() throws Exception {
        Patient patient = createPatient("DOC-NO-UPD");

        String body = """
                {
                  "fullName": "Forbidden Update",
                  "egn": "9701010010",
                  "generalPractitionerId": %d,
                  "userId": null
                }
                """.formatted(patient.getGeneralPractitioner().getId());

        mockMvc.perform(
                        put("/api/patients/" + patient.getId())
                                .with(user("doctor").roles("DOCTOR"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void patientShouldNotDeletePatient() throws Exception {
        Patient patient = createPatient("PAT-NO-DEL");

        mockMvc.perform(
                        delete("/api/patients/" + patient.getId())
                                .with(user("patient").roles("PATIENT"))
                )
                .andExpect(status().isForbidden());

        assertThat(patientRepository.existsById(patient.getId())).isTrue();
    }

    @Test
    void shouldReturnNotFoundWhenPatientDoesNotExist() throws Exception {
        mockMvc.perform(
                        get("/api/patients/999999")
                                .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Patient not found with id: 999999"));
    }

    @Test
    void shouldReturnConflictWhenEgnAlreadyExists() throws Exception {
        Doctor gp = createGeneralPractitioner("DUP-EGN");

        User existingPatientUser = userRepository.save(
                TestDataFactory.user("patient_dup_existing_user", Role.PATIENT)
        );

        User newPatientUser = userRepository.save(
                TestDataFactory.user("patient_dup_new_user", Role.PATIENT)
        );

        patientRepository.save(
                TestDataFactory.patient(
                        "Existing Patient",
                        "9701010011",
                        gp,
                        existingPatientUser
                )
        );

        String body = """
                {
                  "fullName": "Duplicate Patient",
                  "egn": "9701010011",
                  "generalPractitionerId": %d,
                  "userId": %d
                }
                """.formatted(gp.getId(), newPatientUser.getId());

        mockMvc.perform(
                        post("/api/patients")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Patient with EGN already exists: 9701010011"
                ));
    }

    @Test
    void shouldReturnBadRequestWhenSelectedDoctorIsNotGeneralPractitioner() throws Exception {
        User specialistUser = userRepository.save(
                TestDataFactory.user("patient_bad_gp_specialist_user", Role.DOCTOR)
        );

        User patientUser = userRepository.save(
                TestDataFactory.user("patient_bad_gp_user", Role.PATIENT)
        );

        Doctor specialist = doctorRepository.save(
                TestDataFactory.doctor(
                        "DOC-BAD-GP",
                        "Dr. Not GP",
                        Specialty.CARDIOLOGY,
                        false,
                        specialistUser
                )
        );

        String body = """
                {
                  "fullName": "Patient Bad GP",
                  "egn": "9701010012",
                  "generalPractitionerId": %d,
                  "userId": %d
                }
                """.formatted(specialist.getId(), patientUser.getId());

        mockMvc.perform(
                        post("/api/patients")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Selected doctor is not a general practitioner"
                ));
    }

    @Test
    void shouldReturnBadRequestWhenLinkedUserIsNotPatient() throws Exception {
        Doctor gp = createGeneralPractitioner("BAD-USER");

        User doctorUser = userRepository.save(
                TestDataFactory.user("patient_bad_user_doctor_user", Role.DOCTOR)
        );

        String body = """
                {
                  "fullName": "Patient Invalid User",
                  "egn": "9701010013",
                  "generalPractitionerId": %d,
                  "userId": %d
                }
                """.formatted(gp.getId(), doctorUser.getId());

        mockMvc.perform(
                        post("/api/patients")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Selected user must have PATIENT role"));
    }

    private Patient createPatient(String suffix) {
        Doctor gp = createGeneralPractitioner("GP-" + suffix);

        User patientUser = userRepository.save(
                TestDataFactory.user("patient_crud_" + shortKey(suffix), Role.PATIENT)
        );

        return patientRepository.save(
                TestDataFactory.patient(
                        "Patient " + suffix,
                        generateEgn(suffix),
                        gp,
                        patientUser
                )
        );
    }

    private Doctor createGeneralPractitioner(String suffix) {
        String key = shortKey(suffix);

        User gpUser = userRepository.save(
                TestDataFactory.user("gp_patient_crud_" + key, Role.DOCTOR)
        );

        return doctorRepository.save(
                TestDataFactory.doctor(
                        "GP-PC-" + key,
                        "Dr. GP " + key,
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
        return "97" + String.format("%08d", number).substring(0, 8);
    }
}