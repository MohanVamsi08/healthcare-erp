package com.healthcare.erp.security;

import com.healthcare.erp.model.User;
import com.healthcare.erp.model.Role;
import com.healthcare.erp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Validates that the authenticated user actually belongs to the hospital
 * they are trying to access in the path variables.
 */
@Component("tenantGuard")
@RequiredArgsConstructor
public class TenantGuard {

    private final UserRepository userRepository;

    public boolean canAccessTenant(Authentication authentication, UUID pathHospitalId) {
        if (authentication == null || pathHospitalId == null) {
            return false;
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getRole() == Role.SUPER_ADMIN) {
            return true; // SUPER_ADMIN has access to all hospitals
        }

        if (user.getHospital() == null) {
            return false; // Should not happen for normal users, but safe fallback
        }

        return user.getHospital().getId().equals(pathHospitalId);
    }
}
