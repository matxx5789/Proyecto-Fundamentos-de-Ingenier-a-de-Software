package com.openlib.service;

import com.openlib.domain.Book;
import com.openlib.domain.BookStatus;
import com.openlib.dto.response.BuyerReportResponse;
import com.openlib.dto.response.SellerReportResponse;
import com.openlib.repository.BookRepository;
import com.openlib.repository.OrderItemRepository;
import com.openlib.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BookRepository bookRepository;

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

    private BuyerReportResponse.BookItem toBookItem(Book book) {
        return new BuyerReportResponse.BookItem(book.getId(), book.getTitle(), book.getAuthor(), book.getPrice());
    }
}
