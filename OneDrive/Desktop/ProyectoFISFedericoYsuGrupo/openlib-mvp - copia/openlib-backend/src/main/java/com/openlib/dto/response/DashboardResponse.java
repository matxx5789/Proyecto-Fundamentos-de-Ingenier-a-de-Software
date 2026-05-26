package com.openlib.dto.response;

import java.util.List;

public record DashboardResponse(
    long              totalUsers,
    long              totalBooks,
    long              totalOrders,
    long              totalDownloads,
    List<BookResponse>  topDownloadedBooks,
    List<CategoryResponse> popularCategories,
    long              activeUsersLast30Days
) {}
