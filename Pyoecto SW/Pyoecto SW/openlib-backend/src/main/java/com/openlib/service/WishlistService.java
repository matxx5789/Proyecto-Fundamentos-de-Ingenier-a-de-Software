package com.openlib.service;

import com.openlib.domain.*;
import com.openlib.dto.response.BookResponse;
import com.openlib.exception.BusinessException;
import com.openlib.exception.ResourceNotFoundException;
import com.openlib.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final BookRepository     bookRepository;
    private final UserRepository     userRepository;
    private final BookService        bookService;

    public List<BookResponse> getWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId)
                .stream()
                .map(w -> bookService.toResponse(w.getBook()))
                .toList();
    }

    @Transactional
    public void add(Long bookId, Long userId) {
        if (wishlistRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new BusinessException("El libro ya está en tus favoritos");
        }
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", bookId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));
        wishlistRepository.save(Wishlist.builder().user(user).book(book).build());
    }

    @Transactional
    public void remove(Long bookId, Long userId) {
        if (!wishlistRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new ResourceNotFoundException("Favorito", bookId);
        }
        wishlistRepository.deleteByUserIdAndBookId(userId, bookId);
    }
}
