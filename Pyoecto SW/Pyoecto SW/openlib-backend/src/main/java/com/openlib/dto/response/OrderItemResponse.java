package com.openlib.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
    Long       id,
    Long       bookId,
    String     bookTitle,
    String     bookCoverUrl,
    BigDecimal price
) {}
