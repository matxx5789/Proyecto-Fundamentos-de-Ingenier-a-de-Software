package com.openlib.fx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Clases de modelo (POJO) que mapean las respuestas del backend.
 */
public class Models {

    // ── Auth ───────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AuthResponse {
        public String accessToken;
        public String refreshToken;
        public String tokenType;
        public String email;
        public String role;
        public String fullName;
        public Long   userId;
    }

    // ── Book ───────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookResponse {
        public Long   id;
        public String title;
        public String author;
        public String isbn;
        public String description;
        public String language;
        public Integer publishedYear;
        public Double price;
        public String status;
        public String coverImageUrl;
        public Double averageRating;
        public Integer reviewCount;
        public Integer downloadCount;
        public String category;
        public List<String> tags;
        public String sellerName;

        public String displayPrice() {
            return (price == null || price == 0) ? "Gratis" : String.format("$%.2f", price);
        }
        public String displayRating() {
            return averageRating == null ? "Sin calificar" : String.format("★ %.1f (%d)", averageRating, reviewCount);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PagedBooks {
        public List<BookResponse> content;
        public int totalPages;
        public long totalElements;
        public int number;
        public int size;
    }

    // ── Cart ───────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CartItemResponse {
        public Long   id;
        public Long   bookId;
        public String bookTitle;
        public String bookAuthor;
        public String bookCover;
        public Double price;
    }

    // ── Order ──────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderResponse {
        public Long   id;
        public String status;
        public String paymentMethod;
        public Double totalAmount;
        public String billingName;
        public String billingEmail;
        public String billingAddress;
        public String createdAt;
        public List<OrderItemResponse> items;

        public String displayTotal() {
            return totalAmount == null ? "$0.00" : String.format("$%.2f", totalAmount);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderItemResponse {
        public Long   bookId;
        public String bookTitle;
        public String bookAuthor;
        public Double price;
    }

    // ── Review ─────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReviewResponse {
        public Long   id;
        public Integer rating;
        public String title;
        public String body;
        public String reviewerName;
        public String createdAt;
        public Boolean isVisible;
    }

    // ── Category ───────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryResponse {
        public Long   id;
        public String name;
        public String slug;
        public String description;
    }

    // ── User ───────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserResponse {
        public Long   id;
        public String email;
        public String username;
        public String fullName;
        public String role;
        public Boolean isActive;
        public Boolean isVerified;
    }

    // ── Dashboard ──────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DashboardResponse {
        public Long totalUsers;
        public Long totalBooks;
        public Long totalOrders;
        public Long totalDownloads;
        public Long pendingBooks;
        public List<BookResponse> topDownloaded;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BuyerReportResponse {
        public Long totalCompletedOrders;
        public Long totalBooksPurchased;
        public Double totalSpent;
        public List<BookItem> recentPurchases;

        public String displayTotalSpent() {
            return totalSpent == null ? "$0.00" : String.format("$%.2f", totalSpent);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookItem {
        public Long bookId;
        public String title;
        public String author;
        public Double price;

        public String displayPrice() {
            return price == null || price == 0 ? "Gratis" : String.format("$%.2f", price);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SellerReportResponse {
        public Long totalBooksPublished;
        public Long totalApprovedBooks;
        public Long totalPendingBooks;
        public Long totalCompletedSales;
        public Double totalRevenue;

        public String displayTotalRevenue() {
            return totalRevenue == null ? "$0.00" : String.format("$%.2f", totalRevenue);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetricsResponse {
        public Long booksSoldInPeriod;
        public java.util.Map<String, Long> ordersByStatus;
        public Double conversionRate;
        public Long totalCartItems;
        public Long totalItemsSold;
        public String periodStart;
        public String periodEnd;

        public String displayConversionRate() {
            return conversionRate == null ? "0.00%" : String.format("%.2f%%", conversionRate);
        }

        public String getPeriodDisplay() {
            if (periodStart == null || periodEnd == null) {
                return "N/A";
            }
            try {
                String start = periodStart.substring(0, 10);
                String end = periodEnd.substring(0, 10);
                return start + " a " + end;
            } catch (Exception e) {
                return "N/A";
            }
        }
    }

    // ── Download ───────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DownloadResponse {
        public String signedUrl;
        public String expiresAt;
    }
}
