package com.medical.system.service.impl;

import com.medical.system.dto.auth.UserResponse;
import com.medical.system.dto.user.UserCreateRequest;
import com.medical.system.dto.user.UserPasswordUpdateRequest;
import com.medical.system.dto.user.UserUpdateRequest;
import com.medical.system.exception.BadRequestException;
import com.medical.system.exception.DuplicateResourceException;
import com.medical.system.exception.ResourceNotFoundException;
import com.medical.system.mapper.UserMapper;
import com.medical.system.model.entity.User;
import com.medical.system.repository.DoctorRepository;
import com.medical.system.repository.PatientRepository;
import com.medical.system.repository.UserRepository;
import com.medical.system.service.UserManagementService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserManagementServiceImpl(
            UserRepository userRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserResponse create(UserCreateRequest request) {
        validateUniqueUserForCreate(request.username(), request.email());

        User user = new User();

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setEnabled(request.enabled() == null || request.enabled());

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = findUserById(id);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = findUserById(id);

        validateUniqueUserForUpdate(id, request.username(), request.email());

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setRole(request.role());
        user.setEnabled(request.enabled() == null || request.enabled());

        User updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse changePassword(Long id, UserPasswordUpdateRequest request) {
        User user = findUserById(id);

        user.setPassword(passwordEncoder.encode(request.password()));

        User updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = findUserById(id);

        boolean usedByDoctor = doctorRepository.existsByUserId(id);
        boolean usedByPatient = patientRepository.existsByUserId(id);

        if (usedByDoctor || usedByPatient) {
            throw new BadRequestException(
                    "User is linked to a doctor or patient profile. Disable the user instead of deleting it."
            );
        }

        userRepository.delete(user);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id
                ));
    }

    private void validateUniqueUserForCreate(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException(
                    "User with username already exists: " + username
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException(
                    "User with email already exists: " + email
            );
        }
    }

    private void validateUniqueUserForUpdate(Long currentUserId, String username, String email) {
        userRepository.findByUsername(username)
                .filter(existing -> !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "User with username already exists: " + username
                    );
                });

        userRepository.findByEmail(email)
                .filter(existing -> !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "User with email already exists: " + email
                    );
                });
    }
}