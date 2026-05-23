package com.openlib.controller;

import com.openlib.dto.request.*;
import com.openlib.dto.response.AuthResponse;
import com.openlib.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar nuevo usuario (BUYER o SELLER)")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    @Operation(summary = "Login y obtención de tokens JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token usando refresh token")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return authService.refresh(req.refreshToken());
    }
}
