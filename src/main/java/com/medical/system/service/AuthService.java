package com.medical.system.service;

import com.medical.system.dto.auth.RegisterRequest;
import com.medical.system.dto.auth.UserResponse;

public interface AuthService {

    UserResponse registerPatient(RegisterRequest request);

    UserResponse getCurrentUser(String username);
}