package com.example.authservice.service;

import com.example.authservice.dto.AuthResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.model.Role;
import com.example.authservice.model.UserAccount;
import com.example.authservice.observability.AuditTrailService;
import com.example.authservice.repository.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditTrailService auditTrailService;

    public AuthService(UserAccountRepository userAccountRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuditTrailService auditTrailService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditTrailService = auditTrailService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userAccountRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");
        }

        if (userAccountRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(request.username().trim());
        userAccount.setEmail(request.email().trim().toLowerCase());
        userAccount.setPasswordHash(passwordEncoder.encode(request.password()));
        userAccount.setRole(Role.ROLE_USER);

        UserAccount savedUser = userAccountRepository.save(userAccount);
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
        UserAccount userAccount = userAccountRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), userAccount.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(userAccount.getUsername(), userAccount.getRole());
        auditTrailService.record(
                "LOGIN_SUCCESS",
                userAccount.getUsername(),
                "USER",
                String.valueOf(userAccount.getId()),
                null);
        return new AuthResponse(token, "Bearer", userAccount.getUsername(), userAccount.getRole().name());
    }
}
