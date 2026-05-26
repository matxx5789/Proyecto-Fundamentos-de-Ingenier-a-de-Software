package com.openlib.service;

import com.openlib.domain.*;
import com.openlib.dto.request.CartItemRequest;
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
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final BookRepository     bookRepository;
    private final UserRepository     userRepository;
    private final BookService        bookService;

    public List<BookResponse> getCart(Long userId) {
        return cartItemRepository.findByUserId(userId)
                .stream()
                .map(ci -> bookService.toResponse(ci.getBook()))
                .toList();
    }

    @Transactional
    public void addItem(CartItemRequest req, Long userId) {
        if (cartItemRepository.existsByUserIdAndBookId(userId, req.bookId())) {
            throw new BusinessException("El libro ya está en el carrito");
        }

        Book book = bookRepository.findById(req.bookId())
                .filter(b -> b.getStatus() == BookStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", req.bookId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));

        CartItem item = CartItem.builder().user(user).book(book).build();
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(Long bookId, Long userId) {
        if (!cartItemRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new ResourceNotFoundException("Ítem del carrito", bookId);
        }
        cartItemRepository.deleteByUserIdAndBookId(userId, bookId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    @Transactional
    public void cloneCart(Long sourceUserId, Long targetUserId) {
        List<CartItem> sourceItems = cartItemRepository.findByUserId(sourceUserId);
        
        Long finalTargetId = (targetUserId != null) ? targetUserId : sourceUserId;
        User targetUser = userRepository.findById(finalTargetId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", finalTargetId));

        for (CartItem item : sourceItems) {
            boolean exists = cartItemRepository.existsByUserIdAndBookId(finalTargetId, item.getBook().getId());
            if (!exists) {
                CartItem clonedItem = CartItem.builder()
                        .user(targetUser)
                        .book(item.getBook())
                        // addedAt will be populated automatically by @Builder.Default with LocalDateTime.now()
                        .build();
                cartItemRepository.save(clonedItem);
            }
        }
    }
}
