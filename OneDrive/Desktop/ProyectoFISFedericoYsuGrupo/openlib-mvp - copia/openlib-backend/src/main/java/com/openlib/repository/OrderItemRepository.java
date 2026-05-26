package com.openlib.repository;

import com.openlib.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT COALESCE(SUM(oi.price), 0) FROM OrderItem oi JOIN oi.order o WHERE o.buyer.id = :buyerId AND o.status = 'COMPLETED'")
    BigDecimal sumSpentByBuyer(@Param("buyerId") Long buyerId);

    @Query("SELECT COUNT(oi) FROM OrderItem oi JOIN oi.order o WHERE o.buyer.id = :buyerId AND o.status = 'COMPLETED'")
    long countItemsPurchasedByBuyer(@Param("buyerId") Long buyerId);

    @Query("SELECT COALESCE(SUM(oi.price), 0) FROM OrderItem oi JOIN oi.order o WHERE oi.book.seller.id = :sellerId AND o.status = 'COMPLETED'")
    BigDecimal sumRevenueBySeller(@Param("sellerId") Long sellerId);

    @Query("SELECT COUNT(oi) FROM OrderItem oi JOIN oi.order o WHERE oi.book.seller.id = :sellerId AND o.status = 'COMPLETED'")
    long countSalesBySeller(@Param("sellerId") Long sellerId);
}
