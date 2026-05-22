package com.openlib.dto.response;

import java.time.LocalDateTime;

public record ReviewResponse(
    Long          id,
    Long          userId,
    String        username,
    Short         rating,
    String        title,
    String        body,
    LocalDateTime createdAt
) {}
