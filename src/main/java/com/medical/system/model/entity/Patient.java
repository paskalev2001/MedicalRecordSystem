package com.medical.system.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Patient extends BaseEntity{

    @NotBlank
    @Column(nullable = false, length = 120)
    private String fullName;

    @NotBlank
    @Pattern(regexp = "\\d{10}", message = "EGN must contain exactly 10 digits")
    @Column(nullable = false, unique = true, length = 10)
    private String egn;

    @ManyToOne(optional = false)
    @JoinColumn(name = "general_practitioner_id", nullable = false)
    private Doctor generalPractitioner;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @OneToMany(
            mappedBy = "patient",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<HealthInsuranceRecord> healthInsuranceRecords = new ArrayList<>();

    @OneToMany(mappedBy = "patient")
    @Builder.Default
    private List<Examination> examinations = new ArrayList<>();
}
