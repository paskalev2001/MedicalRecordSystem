package com.medical.system.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // CRUD for doctors, patients and diagnosis - Only the Admin can access
                        .requestMatchers(HttpMethod.POST, "/api/doctors/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/doctors/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/doctors/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/patients/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/patients/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/patients/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/diagnoses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/diagnoses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/diagnoses/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/health-insurance-records/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/health-insurance-records/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/health-insurance-records/**").hasRole("ADMIN")


                        // Examinations can be created and edited by the admin and doctor
                        .requestMatchers(HttpMethod.POST, "/api/examinations/**").hasAnyRole("ADMIN", "DOCTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/examinations/**").hasAnyRole("ADMIN", "DOCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/examinations/**").hasAnyRole("ADMIN", "DOCTOR")

                        // Issuing Sick leaves for Admin and doctor
                        .requestMatchers(HttpMethod.POST, "/api/sick-leaves/**").hasAnyRole("ADMIN", "DOCTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/sick-leaves/**").hasAnyRole("ADMIN", "DOCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/sick-leaves/**").hasAnyRole("ADMIN", "DOCTOR")

                        //All can read their medical data
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")

                        .anyRequest().authenticated()
                )

                .httpBasic(Customizer.withDefaults())

                .formLogin(form -> form.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}