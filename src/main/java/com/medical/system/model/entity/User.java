package com.medical.system.model.entity;

import jakarta.persistence.Column;
import com.medical.system.model.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity{
    @NotBlank
    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Email
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;
}
