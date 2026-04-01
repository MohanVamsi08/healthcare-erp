package com.healthcare.erp.service;

import com.healthcare.erp.dto.CreateUserRequest;
import com.healthcare.erp.dto.UpdateUserRequest;
import com.healthcare.erp.dto.UserDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.Hospital;
import com.healthcare.erp.model.Role;
import com.healthcare.erp.model.User;
import com.healthcare.erp.repository.HospitalRepository;
import com.healthcare.erp.repository.UserRepository;
import com.healthcare.erp.security.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    /**
     * SUPER_ADMIN creates a HOSPITAL_ADMIN for a specific hospital.
     */
    public UserDTO createHospitalAdmin(CreateUserRequest request) {
        validateEmailNotTaken(request.email());

        Hospital hospital = hospitalRepository.findById(request.hospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", request.hospitalId()));

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(Role.HOSPITAL_ADMIN)
                .hospital(hospital)
                .build();

        User saved = userRepository.save(user);
        auditService.logCreate("User", saved.getId().toString(), hospital.getId(),
                "role=HOSPITAL_ADMIN");
        return UserDTO.fromEntity(saved);
    }

    /**
     * HOSPITAL_ADMIN creates staff users for their hospital.
     */
    public UserDTO createStaffUser(UUID hospitalId, CreateUserRequest request) {
        validateEmailNotTaken(request.email());

        if (request.role() == Role.SUPER_ADMIN || request.role() == Role.HOSPITAL_ADMIN) {
            throw new IllegalArgumentException("Hospital admins cannot create SUPER_ADMIN or HOSPITAL_ADMIN roles");
        }

        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(request.role())
                .hospital(hospital)
                .build();

        User saved = userRepository.save(user);
        auditService.logCreate("User", saved.getId().toString(), hospitalId,
                "role=" + request.role());
        return UserDTO.fromEntity(saved);
    }

    public List<UserDTO> getUsersByHospital(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital", hospitalId);
        }
        return userRepository.findByHospitalIdAndIsActiveTrue(hospitalId)
                .stream()
                .map(UserDTO::fromEntity)
                .toList();
    }

    public UserDTO getUserById(UUID hospitalId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (!user.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return UserDTO.fromEntity(user);
    }

    public UserDTO updateUser(UUID hospitalId, UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!user.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        // Check email uniqueness if changing email
        if (!user.getEmail().equals(request.email())) {
            validateEmailNotTaken(request.email());
        }

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        if (request.role() != Role.SUPER_ADMIN && request.role() != Role.HOSPITAL_ADMIN) {
            user.setRole(request.role());
        }

        User saved = userRepository.save(user);
        auditService.log("UPDATE", "User", userId.toString(), hospitalId, null, null);
        return UserDTO.fromEntity(saved);
    }

    public void deactivateUser(UUID hospitalId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!user.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        user.setActive(false);
        userRepository.save(user);
        auditService.log("DEACTIVATE", "User", userId.toString(), hospitalId, null, null);
    }

    private void validateEmailNotTaken(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use: " + email);
        }
    }
}
