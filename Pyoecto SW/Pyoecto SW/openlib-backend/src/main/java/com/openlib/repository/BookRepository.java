package com.openlib.repository;

import com.openlib.domain.Book;
import com.openlib.domain.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>,
                                        JpaSpecificationExecutor<Book> {

    Page<Book> findByStatus(BookStatus status, Pageable pageable);

    Page<Book> findBySellerIdAndStatus(Long sellerId, BookStatus status, Pageable pageable);

    Page<Book> findBySellerId(Long sellerId, Pageable pageable);

    @Query("""
           SELECT b FROM Book b
           WHERE b.status = 'APPROVED'
             AND (:query IS NULL
                  OR LOWER(b.title)  LIKE LOWER(CONCAT('%', :query, '%'))
                  OR LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%'))
                  OR LOWER(b.isbn)   LIKE LOWER(CONCAT('%', :query, '%')))
             AND (:categoryId IS NULL OR b.category.id = :categoryId)
           """)
    Page<Book> searchApproved(@Param("query")      String  query,
                              @Param("categoryId") Long    categoryId,
                              Pageable pageable);

    @Query("""
           SELECT b FROM Book b
           JOIN b.tags t
           WHERE b.status = 'APPROVED'
             AND t.slug IN :tagSlugs
           """)
    Page<Book> findApprovedByTags(@Param("tagSlugs") List<String> tagSlugs, Pageable pageable);

    @Query("""
           SELECT b FROM Book b
           JOIN OrderItem oi ON oi.book = b
           JOIN Order o      ON oi.order = o
           WHERE o.buyer.id = :userId
             AND o.status    = 'COMPLETED'
           """)
    List<Book> findPurchasedByUser(@Param("userId") Long userId);

    @Query("""
           SELECT b FROM Book b
           JOIN OrderItem oi ON oi.book = b
           JOIN Order o      ON oi.order = o
           JOIN Order o2     ON o2.buyer.id = :userId AND o2.status = 'COMPLETED'
           JOIN OrderItem oi2 ON oi2.order = o2
           WHERE b.status = 'APPROVED'
             AND b.category = (SELECT b2.category FROM Book b2 WHERE b2 = oi2.book)
             AND b NOT IN (SELECT oi3.book FROM OrderItem oi3 JOIN Order o3 ON oi3.order = o3
                           WHERE o3.buyer.id = :userId AND o3.status = 'COMPLETED')
           GROUP BY b
           ORDER BY b.downloadCount DESC
           """)
    Page<Book> findRecommendationsForUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.status = 'APPROVED' ORDER BY b.downloadCount DESC")
    Page<Book> findTopDownloaded(Pageable pageable);

    @Modifying
    @Query("UPDATE Book b SET b.downloadCount = b.downloadCount + 1 WHERE b.id = :id")
    void incrementDownloadCount(@Param("id") Long id);
}
