package com.openlib.dto.response;

import com.openlib.domain.BookStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookResponse(
    Long          id,
    String        title,
    String        subtitle,
    String        author,
    String        isbn,
    String        description,
    String        coverUrl,
    Long          fileSizeBytes,
    Integer       pages,
    String        language,
    Integer       publishedYear,
    BigDecimal    price,
    BookStatus    status,
    CategoryResponse category,
    List<TagResponse> tags,
    Long          downloadCount,
    BigDecimal    averageRating,
    Integer       reviewCount,
    UserResponse  seller,
    LocalDateTime createdAt
) {}
