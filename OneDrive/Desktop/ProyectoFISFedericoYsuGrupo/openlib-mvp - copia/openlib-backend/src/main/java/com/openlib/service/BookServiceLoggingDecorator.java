package com.openlib.service;

import com.openlib.domain.Book;
import com.openlib.dto.request.BookRequest;
import com.openlib.dto.response.BookResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
@Slf4j
public class BookServiceLoggingDecorator implements BookServicePort {

    private final BookServicePort delegate;

    public BookServiceLoggingDecorator(@Qualifier("bookServiceCacheProxy") BookServicePort delegate) {
        this.delegate = delegate;
    }

    @Override
    public Page<BookResponse> searchCatalog(String query, Long categoryId, int page, int size, String sort) {
        log.info("BookService.searchCatalog called: query='{}' categoryId={} page={} size={} sort={}",
                query, categoryId, page, size, sort);
        Page<BookResponse> result = delegate.searchCatalog(query, categoryId, page, size, sort);
        log.info("BookService.searchCatalog returned {} books", result.getNumberOfElements());
        return result;
    }

    @Override
    public BookResponse findById(Long id) {
        log.info("BookService.findById called: id={}", id);
        BookResponse response = delegate.findById(id);
        log.info("BookService.findById returned book id={}", response.id());
        return response;
    }

    @Override
    public List<BookResponse> getRecommendations(Long userId, int limit) {
        log.info("BookService.getRecommendations called for userId={} limit={}", userId, limit);
        return delegate.getRecommendations(userId, limit);
    }

    @Override
    public BookResponse publish(BookRequest req, Long sellerId) {
        log.info("BookService.publish called by sellerId={} title={}", sellerId, req.title());
        BookResponse response = delegate.publish(req, sellerId);
        log.info("BookService.publish created book id={}", response.id());
        return response;
    }

    @Override
    public BookResponse update(Long bookId, BookRequest req, Long sellerId) {
        log.info("BookService.update called for bookId={} sellerId={}", bookId, sellerId);
        BookResponse response = delegate.update(bookId, req, sellerId);
        log.info("BookService.update saved book id={}", response.id());
        return response;
    }

    @Override
    public void archive(Long bookId, Long sellerId) {
        log.info("BookService.archive called for bookId={} sellerId={}", bookId, sellerId);
        delegate.archive(bookId, sellerId);
        log.info("BookService.archive completed for bookId={}", bookId);
    }

    @Override
    public BookResponse approve(Long bookId) {
        log.info("BookService.approve called for bookId={}", bookId);
        BookResponse response = delegate.approve(bookId);
        log.info("BookService.approve completed for bookId={}", bookId);
        return response;
    }

    @Override
    public BookResponse reject(Long bookId) {
        log.info("BookService.reject called for bookId={}", bookId);
        BookResponse response = delegate.reject(bookId);
        log.info("BookService.reject completed for bookId={}", bookId);
        return response;
    }

    @Override
    public Page<BookResponse> getMyBooks(Long sellerId, int page, int size) {
        log.info("BookService.getMyBooks called for sellerId={} page={} size={}", sellerId, page, size);
        return delegate.getMyBooks(sellerId, page, size);
    }

    @Override
    public Page<BookResponse> getAllForAdmin(int page, int size) {
        log.info("BookService.getAllForAdmin called page={} size={}", page, size);
        return delegate.getAllForAdmin(page, size);
    }

    @Override
    public BookResponse toResponse(Book book) {
        return delegate.toResponse(book);
    }
}
