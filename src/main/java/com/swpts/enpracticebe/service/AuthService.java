package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.UserDto;
import com.swpts.enpracticebe.dto.request.FirebaseTokenRequest;
import com.swpts.enpracticebe.dto.request.LoginRequest;
import com.swpts.enpracticebe.dto.request.RegisterRequest;
import com.swpts.enpracticebe.dto.response.AuthResponse;
import com.swpts.enpracticebe.entity.FcmToken;
import com.swpts.enpracticebe.entity.User;
import com.swpts.enpracticebe.repository.FcmTokenRepository;
import com.swpts.enpracticebe.repository.UserRepository;
import com.swpts.enpracticebe.security.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final FcmTokenRepository fcmTokenRepository;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .build();

        user = userRepository.save(user);
        String token = jwtUtil.generateToken(user.getId());

        return AuthResponse.builder()
                .token(token)
                .user(toDto(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId());

        return AuthResponse.builder()
                .token(token)
                .user(toDto(user))
                .build();
    }

    public UserDto getMe(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toDto(user);
    }

    @Transactional
    public void saveFcmToken(FirebaseTokenRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = (UUID) authentication.getPrincipal();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        fcmTokenRepository.findByUserId(user.getId())
                .ifPresentOrElse(
                    existingToken -> {
                        existingToken.setToken(request.getFcmToken());
                        fcmTokenRepository.save(existingToken);
                    },
                    () -> {
                        FcmToken newToken = new FcmToken();
                        newToken.setUserId(user.getId());
                        newToken.setToken(request.getFcmToken());
                        fcmTokenRepository.save(newToken);
                    }
                );
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .build();
    }
}
