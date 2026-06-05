package com.openlib.dto.response;

import java.math.BigDecimal;

public record SellerReportResponse(
        long totalBooksPublished,
        long totalApprovedBooks,
        long totalPendingBooks,
        long totalCompletedSales,
        BigDecimal totalRevenue
) {}
