package com.openlib.controller;

import com.openlib.domain.User;
import com.openlib.dto.request.BookRequest;
import com.openlib.dto.response.BookResponse;
import com.openlib.service.BookServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seller/books")
@RequiredArgsConstructor
@Tag(name = "Seller — Gestión de Publicaciones")
public class SellerController {

    private final BookServicePort bookService;

    @GetMapping
    @Operation(summary = "Mis publicaciones")
    public Page<BookResponse> myBooks(@AuthenticationPrincipal User user,
                                      @RequestParam(defaultValue = "0")  int page,
                                      @RequestParam(defaultValue = "20") int size) {
        return bookService.getMyBooks(user.getId(), page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Publicar nuevo libro")
    public BookResponse publish(@AuthenticationPrincipal User user,
                                @Valid @RequestBody BookRequest req) {
        return bookService.publish(req, user.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar libro publicado")
    public BookResponse update(@AuthenticationPrincipal User user,
                               @PathVariable Long id,
                               @Valid @RequestBody BookRequest req) {
        return bookService.update(id, req, user.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Retirar libro del catálogo (archivar)")
    public void archive(@AuthenticationPrincipal User user, @PathVariable Long id) {
        bookService.archive(id, user.getId());
    }
}
