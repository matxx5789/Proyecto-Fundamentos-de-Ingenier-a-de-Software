package com.openlib.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartItemRequest(
    @NotNull(message = "El bookId es obligatorio")
    @Positive
    Long bookId
) {}
