package com.openlib.controller;

import com.openlib.domain.User;
import com.openlib.dto.response.BookResponse;
import com.openlib.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
@Tag(name = "Lista de Favoritos")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "Ver lista de favoritos")
    public List<BookResponse> getWishlist(@AuthenticationPrincipal User user) {
        return wishlistService.getWishlist(user.getId());
    }

    @PostMapping("/{bookId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Agregar libro a favoritos")
    public void add(@AuthenticationPrincipal User user, @PathVariable Long bookId) {
        wishlistService.add(bookId, user.getId());
    }

    @DeleteMapping("/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Quitar libro de favoritos")
    public void remove(@AuthenticationPrincipal User user, @PathVariable Long bookId) {
        wishlistService.remove(bookId, user.getId());
    }
}
