package com.openlib.dto.response;

import com.openlib.domain.Role;

import java.time.LocalDateTime;

public record UserResponse(
    Long          id,
    String        email,
    String        username,
    String        fullName,
    Role          role,
    String        avatarUrl,
    Boolean       isActive,
    Boolean       isVerified,
    LocalDateTime createdAt
) {}
