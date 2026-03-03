package com.healthcare.erp.service;

import com.healthcare.erp.dto.AuthResponse;
import com.healthcare.erp.dto.LoginRequest;
import com.healthcare.erp.model.User;
import com.healthcare.erp.repository.UserRepository;
import com.healthcare.erp.security.JwtService;
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

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

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
