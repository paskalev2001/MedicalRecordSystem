package com.medical.system.controller;

import com.medical.system.dto.auth.UserResponse;
import com.medical.system.dto.user.UserCreateRequest;
import com.medical.system.dto.user.UserPasswordUpdateRequest;
import com.medical.system.dto.user.UserUpdateRequest;
import com.medical.system.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserManagementService userManagementService;

    public AdminUserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userManagementService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userManagementService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(userManagementService.update(id, request));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<UserResponse> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody UserPasswordUpdateRequest request
    ) {
        return ResponseEntity.ok(userManagementService.changePassword(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userManagementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}