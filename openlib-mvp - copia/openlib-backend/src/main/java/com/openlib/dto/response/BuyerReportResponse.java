package com.openlib.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record BuyerReportResponse(
        long totalCompletedOrders,
        long totalBooksPurchased,
        BigDecimal totalSpent,
        List<BookItem> recentPurchases
) {
    public record BookItem(Long bookId, String title, String author, BigDecimal price) {}
}
