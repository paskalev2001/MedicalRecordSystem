package com.medical.system.controller;

import com.medical.system.model.entity.User;
import com.medical.system.model.enums.Role;
import com.medical.system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AuthUserManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registerShouldCreatePatientUser() throws Exception {
        String body = """
                {
                  "username": "register_patient_test",
                  "email": "register_patient_test@test.com",
                  "password": "123456"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("register_patient_test"))
                .andExpect(jsonPath("$.email").value("register_patient_test@test.com"))
                .andExpect(jsonPath("$.role").value("PATIENT"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void registerShouldReturnConflictWhenUsernameAlreadyExists() throws Exception {
        User existingUser = new User();
        existingUser.setUsername("duplicate_username_test");
        existingUser.setEmail("duplicate_username_existing@test.com");
        existingUser.setPassword(passwordEncoder.encode("123456"));
        existingUser.setRole(Role.PATIENT);
        existingUser.setEnabled(true);

        userRepository.save(existingUser);

        String body = """
                {
                  "username": "duplicate_username_test",
                  "email": "duplicate_username_new@test.com",
                  "password": "123456"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User with username already exists: duplicate_username_test"));
    }

    @Test
    void registerShouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        User existingUser = new User();
        existingUser.setUsername("duplicate_email_existing_user");
        existingUser.setEmail("duplicate_email_test@test.com");
        existingUser.setPassword(passwordEncoder.encode("123456"));
        existingUser.setRole(Role.PATIENT);
        existingUser.setEnabled(true);

        userRepository.save(existingUser);

        String body = """
                {
                  "username": "duplicate_email_new_user",
                  "email": "duplicate_email_test@test.com",
                  "password": "123456"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User with email already exists: duplicate_email_test@test.com"));
    }

    @Test
    void adminShouldCreateDoctorUser() throws Exception {
        String body = """
                {
                  "username": "admin_created_doctor",
                  "email": "admin_created_doctor@test.com",
                  "password": "123456",
                  "role": "DOCTOR",
                  "enabled": true
                }
                """;

        mockMvc.perform(
                        post("/api/admin/users")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("admin_created_doctor"))
                .andExpect(jsonPath("$.role").value("DOCTOR"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void doctorShouldNotAccessAdminUsersEndpoint() throws Exception {
        mockMvc.perform(
                        get("/api/admin/users")
                                .with(user("doctor_user_management_test").roles("DOCTOR"))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void patientShouldNotAccessAdminUsersEndpoint() throws Exception {
        mockMvc.perform(
                        get("/api/admin/users")
                                .with(user("patient_user_management_test").roles("PATIENT"))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldChangeUserPassword() throws Exception {
        User targetUser = new User();
        targetUser.setUsername("password_change_target");
        targetUser.setEmail("password_change_target@test.com");
        targetUser.setPassword(passwordEncoder.encode("oldpassword"));
        targetUser.setRole(Role.PATIENT);
        targetUser.setEnabled(true);

        targetUser = userRepository.save(targetUser);

        String body = """
                {
                  "password": "newpassword123"
                }
                """;

        mockMvc.perform(
                        patch("/api/admin/users/" + targetUser.getId() + "/password")
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("password_change_target"));

        User updatedUser = userRepository.findById(targetUser.getId()).orElseThrow();

        assertThat(passwordEncoder.matches("newpassword123", updatedUser.getPassword()))
                .isTrue();
    }

    @Test
    void adminShouldDisableUser() throws Exception {
        User targetUser = new User();
        targetUser.setUsername("disable_user_target");
        targetUser.setEmail("disable_user_target@test.com");
        targetUser.setPassword(passwordEncoder.encode("123456"));
        targetUser.setRole(Role.PATIENT);
        targetUser.setEnabled(true);

        targetUser = userRepository.save(targetUser);

        String body = """
                {
                  "username": "disable_user_target",
                  "email": "disable_user_target@test.com",
                  "role": "PATIENT",
                  "enabled": false
                }
                """;

        mockMvc.perform(
                        put("/api/admin/users/" + targetUser.getId())
                                .with(user("admin").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        User updatedUser = userRepository.findById(targetUser.getId()).orElseThrow();

        assertThat(updatedUser.isEnabled()).isFalse();
    }

    @Test
    void disabledUserShouldNotAuthenticateWithBasicAuth() throws Exception {
        User disabledUser = new User();
        disabledUser.setUsername("disabled_auth_user");
        disabledUser.setEmail("disabled_auth_user@test.com");
        disabledUser.setPassword(passwordEncoder.encode("password123"));
        disabledUser.setRole(Role.PATIENT);
        disabledUser.setEnabled(false);

        userRepository.save(disabledUser);

        mockMvc.perform(
                        get("/api/auth/me")
                                .with(httpBasic("disabled_auth_user", "password123"))
                )
                .andExpect(status().isUnauthorized());
    }
}