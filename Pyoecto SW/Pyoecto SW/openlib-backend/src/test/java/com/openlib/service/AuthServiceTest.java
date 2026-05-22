package com.openlib.service;

import com.openlib.domain.Role;
import com.openlib.domain.User;
import com.openlib.dto.request.LoginRequest;
import com.openlib.dto.request.RegisterRequest;
import com.openlib.dto.response.AuthResponse;
import com.openlib.exception.BusinessException;
import com.openlib.repository.UserRepository;
import com.openlib.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository        userRepository;
    @Mock private JwtTokenProvider      jwtTokenProvider;
    @Mock private AuthenticationManager authManager;
    @Mock private StringRedisTemplate   redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;

    @InjectMocks
    private AuthService authService;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "passwordEncoder", encoder);
        ReflectionTestUtils.setField(authService, "accessTokenExpMs",  900_000L);
        ReflectionTestUtils.setField(authService, "refreshTokenExpMs", 86_400_000L);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        doNothing().when(valueOps).set(anyString(), anyString(), any());
    }

    // ── Registro ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register() — éxito con rol BUYER por defecto")
    void register_success() {
        RegisterRequest req = new RegisterRequest(
                "new@test.com", "newuser", "Secure1234!", "Nuevo Usuario", null);

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        User saved = User.builder()
                .id(1L).email("new@test.com").username("newuser")
                .fullName("Nuevo Usuario").role(Role.BUYER)
                .isActive(true).isVerified(true).build();
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("access-token");

        AuthResponse response = authService.register(req);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.user().role()).isEqualTo(Role.BUYER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register() — lanza excepción si email ya existe")
    void register_duplicateEmail() {
        RegisterRequest req = new RegisterRequest(
                "dup@test.com", "user1", "Pass1234!", "Dup", "BUYER");
        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("email");
    }

    @Test
    @DisplayName("register() — no permite registrar como ADMIN")
    void register_adminRoleForbidden() {
        RegisterRequest req = new RegisterRequest(
                "a@b.com", "adminUser", "Pass1234!", "Admin", "ADMIN");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ADMIN");
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login() — éxito con credenciales correctas")
    void login_success() {
        LoginRequest req = new LoginRequest("user@test.com", "Pass1234!");
        User user = User.builder()
                .id(2L).email("user@test.com").username("testuser")
                .role(Role.BUYER).isActive(true).isVerified(true).build();

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(req);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("login() — lanza excepción con credenciales incorrectas")
    void login_badCredentials() {
        LoginRequest req = new LoginRequest("user@test.com", "wrongpass");
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }
}
