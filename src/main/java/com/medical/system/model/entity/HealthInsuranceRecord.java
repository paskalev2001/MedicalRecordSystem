package com.medical.system.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.YearMonth;

@Entity
@Table(
        name = "health_insurance_records",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"patient_id", "insurance_month"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthInsuranceRecord extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull
    @Column(name = "insurance_month", nullable = false, length = 7)
    private YearMonth month;

    @Column(nullable = false)
    private boolean insured;
}
