package com.openlib.service;

import com.openlib.dto.response.BookResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component("bookServiceCacheProxy")
@Slf4j
public class BookServiceCacheProxy implements BookServicePort {

    private final BookServicePort delegate;

    public BookServiceCacheProxy(@Qualifier("bookServiceCore") BookServicePort delegate) {
        this.delegate = delegate;
    }

    private final ConcurrentMap<String, org.springframework.data.domain.Page<BookResponse>> searchCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, BookResponse> byIdCache = new ConcurrentHashMap<>();

    @Override
    public org.springframework.data.domain.Page<BookResponse> searchCatalog(String query, Long categoryId, int page, int size, String sort) {
        String key = query + "_" + categoryId + "_" + page + "_" + size + "_" + sort;
        return searchCache.computeIfAbsent(key, k -> {
            log.debug("Cache miss for book search: {}", k);
            return delegate.searchCatalog(query, categoryId, page, size, sort);
        });
    }

    @Override
    public BookResponse findById(Long id) {
        return byIdCache.computeIfAbsent(id, k -> {
            log.debug("Cache miss for book by id: {}", k);
            return delegate.findById(k);
        });
    }

    @Override
    public java.util.List<BookResponse> getRecommendations(Long userId, int limit) {
        return delegate.getRecommendations(userId, limit);
    }

    @Override
    public BookResponse publish(com.openlib.dto.request.BookRequest req, Long sellerId) {
        BookResponse response = delegate.publish(req, sellerId);
        invalidateCache();
        return response;
    }

    @Override
    public BookResponse update(Long bookId, com.openlib.dto.request.BookRequest req, Long sellerId) {
        BookResponse response = delegate.update(bookId, req, sellerId);
        invalidateCache();
        return response;
    }

    @Override
    public void archive(Long bookId, Long sellerId) {
        delegate.archive(bookId, sellerId);
        invalidateCache();
    }

    @Override
    public BookResponse approve(Long bookId) {
        BookResponse response = delegate.approve(bookId);
        invalidateCache();
        return response;
    }

    @Override
    public BookResponse reject(Long bookId) {
        BookResponse response = delegate.reject(bookId);
        invalidateCache();
        return response;
    }

    @Override
    public org.springframework.data.domain.Page<BookResponse> getMyBooks(Long sellerId, int page, int size) {
        return delegate.getMyBooks(sellerId, page, size);
    }

    @Override
    public org.springframework.data.domain.Page<BookResponse> getAllForAdmin(int page, int size) {
        return delegate.getAllForAdmin(page, size);
    }

    @Override
    public BookResponse toResponse(com.openlib.domain.Book book) {
        return delegate.toResponse(book);
    }

    private void invalidateCache() {
        log.debug("Invalidating book search and book-by-id cache");
        searchCache.clear();
        byIdCache.clear();
    }
}
