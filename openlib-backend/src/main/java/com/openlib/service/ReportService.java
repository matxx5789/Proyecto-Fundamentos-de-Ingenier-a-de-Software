package com.openlib.service;

import com.openlib.domain.Book;
import com.openlib.domain.BookStatus;
import com.openlib.domain.OrderStatus;
import com.openlib.dto.response.BuyerReportResponse;
import com.openlib.dto.response.MetricsResponse;
import com.openlib.dto.response.SellerReportResponse;
import com.openlib.repository.BookRepository;
import com.openlib.repository.CartItemRepository;
import com.openlib.repository.OrderItemRepository;
import com.openlib.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BookRepository bookRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional(readOnly = true)
    public BuyerReportResponse getBuyerReport(Long buyerId) {
        BigDecimal spent = orderItemRepository.sumSpentByBuyer(buyerId);
        long purchasedBooks = orderItemRepository.countItemsPurchasedByBuyer(buyerId);
        long completedOrders = orderRepository.countByBuyerIdAndStatus(buyerId, com.openlib.domain.OrderStatus.COMPLETED);
        List<BuyerReportResponse.BookItem> recentPurchases = bookRepository.findPurchasedByUser(buyerId).stream()
                .map(this::toBookItem)
                .limit(5)
                .collect(Collectors.toList());

        return new BuyerReportResponse(completedOrders, purchasedBooks, spent, recentPurchases);
    }

    @Transactional(readOnly = true)
    public SellerReportResponse getSellerReport(Long sellerId) {
        long published = bookRepository.countBySellerId(sellerId);
        long approved = bookRepository.countBySellerIdAndStatus(sellerId, BookStatus.APPROVED);
        long pending = bookRepository.countBySellerIdAndStatus(sellerId, BookStatus.PENDING);
        BigDecimal revenue = orderItemRepository.sumRevenueBySeller(sellerId);
        long sales = orderItemRepository.countSalesBySeller(sellerId);
        return new SellerReportResponse(published, approved, pending, sales, revenue);
    }

    /**
     * Calcula métricas de ventas en un período determinado
     */
    @Transactional(readOnly = true)
    public MetricsResponse getMetricsInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        // Libros vendidos (distintos) en el período
        long booksSoldInPeriod = orderItemRepository.countDistinctBooksSoldInPeriod(startDate, endDate);
        
        // Órdenes por estado en el período
        Map<String, Long> ordersByStatus = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            long count = orderRepository.countByStatusInPeriod(status, startDate, endDate);
            ordersByStatus.put(status.name(), count);
        }
        
        // Items vendidos en el período
        long totalItemsSold = orderItemRepository.countTotalItemsSoldInPeriod(startDate, endDate);
        
        // Items en carrito en el período
        long totalCartItems = cartItemRepository.countCartItemsInPeriod(startDate, endDate);
        
        // Calcular conversion rate (items vendidos / items en carrito)
        double conversionRate = calculateConversionRate(totalCartItems, totalItemsSold);
        
        return new MetricsResponse(
                booksSoldInPeriod,
                ordersByStatus,
                conversionRate,
                totalCartItems,
                totalItemsSold,
                startDate.toString(),
                endDate.toString()
        );
    }

    private double calculateConversionRate(long totalCartItems, long totalItemsSold) {
        if (totalCartItems == 0) {
            return 0.0;
        }
        return (double) totalItemsSold / totalCartItems * 100;
    }

    private BuyerReportResponse.BookItem toBookItem(Book book) {
        return new BuyerReportResponse.BookItem(book.getId(), book.getTitle(), book.getAuthor(), book.getPrice());
    }
}
