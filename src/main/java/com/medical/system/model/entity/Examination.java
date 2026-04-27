package com.medical.system.model.entity;

import com.medical.system.model.enums.PaymentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "examinations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Examination {

    @NotNull
    @Column(nullable = false)
    private LocalDate examinationDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(optional = false)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    private Diagnosis diagnosis;

    @Column(nullable = false, length = 2000)
    private String prescribedTreatment;

    @NotNull
    @DecimalMin(value = "0.00")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // PATIENT, ако пациентът няма осигуровки
    // NHIF, ако пациентът има осигуровки
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentType paymentType;

    @OneToOne(
            mappedBy = "examination",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private SickLeave sickLeave;
}
