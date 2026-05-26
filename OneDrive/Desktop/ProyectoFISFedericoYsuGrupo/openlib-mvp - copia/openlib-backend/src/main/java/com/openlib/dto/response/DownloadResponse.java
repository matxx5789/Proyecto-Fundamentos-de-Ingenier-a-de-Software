package com.openlib.dto.response;

import java.time.LocalDateTime;

public record DownloadResponse(
    Long          bookId,
    String        bookTitle,
    String        downloadUrl,   // URL firmada temporal
    LocalDateTime expiresAt
) {}
