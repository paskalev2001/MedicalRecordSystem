package com.medical.system.dto.user;

import com.medical.system.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 80, message = "Username must be between 3 and 80 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 120, message = "Email must be up to 120 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        String password,

        @NotNull(message = "Role is required")
        Role role,

        Boolean enabled
) {
}