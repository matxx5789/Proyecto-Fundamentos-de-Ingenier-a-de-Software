package com.openlib.repository;

import com.openlib.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(Long userId);
    Optional<CartItem> findByUserIdAndBookId(Long userId, Long bookId);
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
    void deleteByUserId(Long userId);
    void deleteByUserIdAndBookId(Long userId, Long bookId);

    // Métricas de items en carrito en un período
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.addedAt >= :startDate AND ci.addedAt <= :endDate")
    long countCartItemsInPeriod(@org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate, @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);
}
