package com.medical.system.service;

import com.medical.system.dto.auth.UserResponse;
import com.medical.system.dto.user.UserCreateRequest;
import com.medical.system.dto.user.UserPasswordUpdateRequest;
import com.medical.system.dto.user.UserUpdateRequest;

import java.util.List;

public interface UserManagementService {

    UserResponse create(UserCreateRequest request);

    UserResponse getById(Long id);

    List<UserResponse> getAll();

    UserResponse update(Long id, UserUpdateRequest request);

    UserResponse changePassword(Long id, UserPasswordUpdateRequest request);

    void delete(Long id);
}