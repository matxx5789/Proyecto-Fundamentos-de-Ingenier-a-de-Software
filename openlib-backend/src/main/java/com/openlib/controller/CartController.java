package com.openlib.controller;

import com.openlib.domain.User;
import com.openlib.dto.request.CartItemRequest;
import com.openlib.dto.response.BookResponse;
import com.openlib.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "Carrito de Compras")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Ver carrito del usuario")
    public List<BookResponse> getCart(@AuthenticationPrincipal User user) {
        return cartService.getCart(user.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Agregar libro al carrito")
    public void addItem(@AuthenticationPrincipal User user,
                        @Valid @RequestBody CartItemRequest req) {
        cartService.addItem(req, user.getId());
    }

    @DeleteMapping("/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar libro del carrito")
    public void removeItem(@AuthenticationPrincipal User user,
                           @PathVariable Long bookId) {
        cartService.removeItem(bookId, user.getId());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Vaciar carrito")
    public void clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user.getId());
    }
}
