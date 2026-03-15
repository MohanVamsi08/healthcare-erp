package com.healthcare.erp.service;

import com.healthcare.erp.dto.AuthResponse;
import com.healthcare.erp.dto.LoginRequest;
import com.healthcare.erp.model.User;
import com.healthcare.erp.repository.UserRepository;
import com.healthcare.erp.security.AuditService;
import com.healthcare.erp.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;

    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException e) {
            auditService.logLogin(request.email(), ip, false);
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        auditService.logLogin(user.getEmail(), ip, true);

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getHospital() != null ? user.getHospital().getId() : null);

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getRole().name(),
                user.getHospital() != null ? user.getHospital().getId() : null);
    }
}
