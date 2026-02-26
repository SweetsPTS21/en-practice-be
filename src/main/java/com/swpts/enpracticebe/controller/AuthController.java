package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.UserDto;
import com.swpts.enpracticebe.dto.request.LoginRequest;
import com.swpts.enpracticebe.dto.request.RegisterRequest;
import com.swpts.enpracticebe.dto.response.AuthResponse;
import com.swpts.enpracticebe.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        UserDto user = authService.getMe(userId);
        return ResponseEntity.ok(user);
    }
}
