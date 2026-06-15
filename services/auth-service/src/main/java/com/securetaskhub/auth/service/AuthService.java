package com.securetaskhub.auth.service;

import com.securetaskhub.auth.dto.AuthResponse;
import com.securetaskhub.auth.dto.LoginRequest;
import com.securetaskhub.auth.dto.RegisterRequest;
import com.securetaskhub.auth.model.Role;
import com.securetaskhub.auth.model.User;
import com.securetaskhub.auth.repository.UserRepository;
import com.securetaskhub.common.observability.AuditTrailService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditTrailService auditTrailService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuditTrailService auditTrailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditTrailService = auditTrailService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User user = new User();
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.ROLE_USER);

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser.getUsername(), savedUser.getRole());
        auditTrailService.record(
                "REGISTER_SUCCESS",
                savedUser.getUsername(),
                "USER",
                String.valueOf(savedUser.getId()),
                null);

        return new AuthResponse(token, "Bearer", savedUser.getUsername(), savedUser.getRole().name());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(user.getUsername(), user.getRole());
        auditTrailService.record(
                "LOGIN_SUCCESS",
                user.getUsername(),
                "USER",
                String.valueOf(user.getId()),
                null);
        return new AuthResponse(token, "Bearer", user.getUsername(), user.getRole().name());
    }
}
