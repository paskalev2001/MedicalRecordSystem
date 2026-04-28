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

                        .requestMatchers("/api/auth/**").permitAll()

                        // For H2 tests - possibly to revisit this implementation
                        // .requestMatchers("/h2-console/**").permitAll()

                        .requestMatchers("/api/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/doctors/**")
                        .hasAnyRole("ADMIN", "DOCTOR", "PATIENT")

                        .requestMatchers(HttpMethod.GET, "/api/diagnoses/**")
                        .hasAnyRole("ADMIN", "DOCTOR", "PATIENT")

                        .requestMatchers(HttpMethod.POST, "/api/**")
                        .hasAnyRole("ADMIN", "DOCTOR")

                        .requestMatchers(HttpMethod.PUT, "/api/**")
                        .hasAnyRole("ADMIN", "DOCTOR")

                        .requestMatchers(HttpMethod.DELETE, "/api/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/**")
                        .authenticated()

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