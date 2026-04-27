package com.medical.system.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "sick_leaves")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class SickLeave extends BaseEntity{
    @OneToOne(optional = false)
    @JoinColumn(name = "examination_id", nullable = false, unique = true)
    private Examination examination;

    @NotNull
    @Column(nullable = false)
    private LocalDate startDate;

    @Min(1)
    @Column(nullable = false)
    private int numberOfDays;
}
