package com.openlib.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Petición de registro de nuevo usuario.
 */
public record RegisterRequest(

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    String email,

    @NotBlank(message = "El username es obligatorio")
    @Size(min = 3, max = 30, message = "Username debe tener entre 3 y 30 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username solo puede contener letras, números y guiones bajos")
    String username,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
             message = "La contraseña debe tener al menos una mayúscula, una minúscula y un número")
    String password,

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 100)
    String fullName,

    /** "BUYER" o "SELLER" — no se permite registrar ADMIN por este endpoint */
    String role
) {}
