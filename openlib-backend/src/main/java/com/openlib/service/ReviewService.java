package com.openlib.service;

import com.openlib.domain.*;
import com.openlib.dto.request.ReviewRequest;
import com.openlib.dto.response.ReviewResponse;
import com.openlib.exception.BusinessException;
import com.openlib.exception.ResourceNotFoundException;
import com.openlib.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository  reviewRepository;
    private final BookRepository    bookRepository;
    private final UserRepository    userRepository;
    private final DownloadRepository downloadRepository;

    public Page<ReviewResponse> getBookReviews(Long bookId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return reviewRepository.findByBookIdAndIsVisibleTrue(bookId, pageable)
                .map(this::toResponse);
    }

    public Page<ReviewResponse> getAllReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return reviewRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public ReviewResponse create(Long bookId, ReviewRequest req, Long userId) {
        if (!downloadRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new BusinessException("Solo puedes reseñar libros que hayas adquirido");
        }
        if (reviewRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new BusinessException("Ya publicaste una reseña para este libro");
        }
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", bookId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));

        Review review = Review.builder()
                .user(user).book(book)
                .rating(req.rating())
                .title(req.title())
                .body(req.body())
                .build();
        reviewRepository.save(review);
        updateBookRating(bookId);
        return toResponse(review);
    }

    @Transactional
    public void delete(Long reviewId, Long adminId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña", reviewId));
        review.setIsVisible(false);
        reviewRepository.save(review);
        updateBookRating(review.getBook().getId());
    }

    private void updateBookRating(Long bookId) {
        Double avg   = reviewRepository.averageRatingByBook(bookId);
        Long   count = reviewRepository.countVisibleByBook(bookId);
        bookRepository.findById(bookId).ifPresent(b -> {
            b.setAverageRating(avg != null
                    ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            b.setReviewCount(count.intValue());
            bookRepository.save(b);
        });
    }

    private ReviewResponse toResponse(Review r) {
        return new ReviewResponse(
                r.getId(),
                r.getUser().getId(),
                r.getUser().getUsername(),
                r.getRating(),
                r.getTitle(),
                r.getBody(),
                r.getIsVisible(),
                r.getCreatedAt()
        );
    }
}
