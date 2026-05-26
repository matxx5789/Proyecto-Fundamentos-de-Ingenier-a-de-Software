package com.openlib.service;

import com.openlib.domain.Book;
import com.openlib.dto.request.BookRequest;
import com.openlib.dto.response.BookResponse;
import com.openlib.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BookServicePort {

    Page<BookResponse> searchCatalog(String query, Long categoryId, int page, int size, String sort);

    BookResponse findById(Long id);

    List<BookResponse> getRecommendations(Long userId, int limit);

    BookResponse publish(BookRequest req, Long sellerId);

    BookResponse update(Long bookId, BookRequest req, Long sellerId);

    void archive(Long bookId, Long sellerId);

    BookResponse approve(Long bookId);

    BookResponse reject(Long bookId);

    Page<BookResponse> getMyBooks(Long sellerId, int page, int size);

    Page<BookResponse> getAllForAdmin(int page, int size);

    List<BookResponse> getNotApprovedBooks();

    BookResponse toResponse(Book book);
}
