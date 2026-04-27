package com.medical.system.model.entity;


import com.medical.system.model.enums.Specialty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor extends BaseEntity {

    @NotBlank
    @Column(nullable = false, unique = true, length = 30)
    private String uniqueIdentifier;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String fullName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Specialty specialty;

    @Column(nullable = false)
    private boolean generalPractitioner;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @OneToMany(mappedBy = "generalPractitioner")
    @Builder.Default
    private List<Patient> registeredPatients = new ArrayList<>();

    @OneToMany(mappedBy = "doctor")
    @Builder.Default
    private List<Examination> examinations = new ArrayList<>();
}