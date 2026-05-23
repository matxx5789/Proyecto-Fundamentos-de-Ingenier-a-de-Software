package com.openlib.service;

import com.openlib.domain.Role;
import com.openlib.domain.User;
import com.openlib.dto.request.LoginRequest;
import com.openlib.dto.request.RegisterRequest;
import com.openlib.dto.response.AuthResponse;
import com.openlib.dto.response.UserResponse;
import com.openlib.exception.BusinessException;
import com.openlib.repository.UserRepository;
import com.openlib.security.JwtTokenProvider;
import com.openlib.security.RefreshTokenStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Servicio de autenticación.
 * La persistencia de refresh tokens está delegada a un singleton explícito.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtTokenProvider      jwtTokenProvider;
    private final AuthenticationManager authManager;
    private final RefreshTokenStore     refreshTokenStore;

    @Value("${openlib.jwt.access-token-expiration}")
    private long accessTokenExpMs;

    @Value("${openlib.jwt.refresh-token-expiration}")
    private long refreshTokenExpMs;

    // ── Registro ──────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new BusinessException("El email ya está registrado: " + req.email());
        }
        if (userRepository.existsByUsername(req.username())) {
            throw new BusinessException("El username ya está en uso: " + req.username());
        }

        Role role = parseRole(req.role());

        User user = User.builder()
                .email(req.email())
                .username(req.username())
                .passwordHash(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .role(role)
                .isActive(true)
                .isVerified(true)   // MVP: auto-verificado
                .build();

        userRepository.save(user);
        log.info("Nuevo usuario registrado: {} [{}]", user.getEmail(), user.getRole());

        return buildAuthResponse(user);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    public AuthResponse login(LoginRequest req) {
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        return buildAuthResponse(user);
    }

    // ── Refresh Token ─────────────────────────────────────────────────────────

    public AuthResponse refresh(String refreshToken) {
        refreshTokenStore.cleanupExpired();

        RefreshTokenStore.RefreshTokenEntry entry = refreshTokenStore.find(refreshToken)
                .orElseThrow(() -> {
                    throw new BusinessException("Refresh token inválido o expirado");
                });

        if (entry.isExpired()) {
            refreshTokenStore.remove(refreshToken);
            throw new BusinessException("Refresh token inválido o expirado");
        }

        User user = userRepository.findByEmail(entry.email())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        // Rotar el refresh token (invalidar el anterior)
        refreshTokenStore.remove(refreshToken);
        return buildAuthResponse(user);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = UUID.randomUUID().toString();

        // Guardar refresh token en el singleton de refresh tokens
        refreshTokenStore.save(refreshToken, user.getEmail(), Instant.now().plusMillis(refreshTokenExpMs));

        UserResponse userResp = toUserResponse(user);
        return AuthResponse.of(accessToken, refreshToken, accessTokenExpMs / 1000L, userResp);
    }

    private Role parseRole(String roleStr) {
        if (roleStr == null) return Role.BUYER;
        return switch (roleStr.toUpperCase()) {
            case "SELLER" -> Role.SELLER;
            case "ADMIN"  -> throw new BusinessException("No se puede registrar como ADMIN");
            default       -> Role.BUYER;
        };
    }

    public UserResponse toUserResponse(User u) {
        return new UserResponse(
            u.getId(), u.getEmail(), u.getUsername(), u.getFullName(),
            u.getRole(), u.getAvatarUrl(), u.getIsActive(), u.getIsVerified(), u.getCreatedAt()
        );
    }

}
