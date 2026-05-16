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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class PatientControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Test
    void doctorCanAccessPatientsList() throws Exception {
        mockMvc.perform(
                        get("/api/patients")
                                .with(user("doctor_controller_1").roles("DOCTOR"))
                )
                .andExpect(status().isOk());
    }

    @Test
    void patientCannotAccessPatientsListButCanAccessOwnProfile() throws Exception {
        User doctorUser = userRepository.save(
                TestDataFactory.user("doctor_controller_for_patient", Role.DOCTOR)
        );

        User patientUser = userRepository.save(
                TestDataFactory.user("patient_controller_1", Role.PATIENT)
        );

        Doctor gp = doctorRepository.save(
                TestDataFactory.doctor(
                        "GP-CTRL-001",
                        "Dr. Controller GP",
                        Specialty.GENERAL_PRACTITIONER,
                        true,
                        doctorUser
                )
        );

        Patient patient = patientRepository.save(
                TestDataFactory.patient(
                        "Controller Patient",
                        "9404041111",
                        gp,
                        patientUser
                )
        );

        mockMvc.perform(
                        get("/api/patients")
                                .with(user("patient_controller_1").roles("PATIENT"))
                )
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        get("/api/patients/" + patient.getId())
                                .with(user("patient_controller_1").roles("PATIENT"))
                )
                .andExpect(status().isOk());
    }
}