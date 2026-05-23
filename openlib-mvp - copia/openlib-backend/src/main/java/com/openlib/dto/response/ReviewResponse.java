package com.openlib.dto.response;

import java.time.LocalDateTime;

public record ReviewResponse(
    Long          id,
    Long          userId,
    String        reviewerName,
    Short         rating,
    String        title,
    String        body,
    Boolean       isVisible,
    LocalDateTime createdAt
) {}
