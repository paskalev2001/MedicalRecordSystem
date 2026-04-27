package com.medical.system.model.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "diagnoses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diagnosis extends BaseEntity{

    @Column(unique = true, length = 20)
    private String code;

    @NotBlank
    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @OneToMany(mappedBy = "diagnosis")
    @Builder.Default
    private List<Examination> examinations = new ArrayList<>();
}
