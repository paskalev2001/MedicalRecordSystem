package com.medical.system.dto.auth;

import com.medical.system.model.enums.Role;

public record UserResponse(
        Long id,
        String username,
        String email,
        Role role,
        boolean enabled
) {
}