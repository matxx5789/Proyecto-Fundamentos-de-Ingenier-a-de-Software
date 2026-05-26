package com.openlib.dto.response;

public record CategoryResponse(
    Long   id,
    String name,
    String slug,
    String description
) {}
