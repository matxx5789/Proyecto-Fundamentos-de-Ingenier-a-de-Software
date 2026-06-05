package com.openlib.controller;

import com.openlib.domain.User;
import com.openlib.dto.request.ReviewRequest;
import com.openlib.dto.response.ReviewResponse;
import com.openlib.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books/{bookId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Reseñas")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    @Operation(summary = "Listar reseñas visibles de un libro")
    public Page<ReviewResponse> list(@PathVariable Long bookId,
                                     @RequestParam(defaultValue = "0")  int page,
                                     @RequestParam(defaultValue = "10") int size) {
        return reviewService.getBookReviews(bookId, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Publicar reseña (solo libros adquiridos)")
    public ReviewResponse create(@AuthenticationPrincipal User user,
                                 @PathVariable Long bookId,
                                 @Valid @RequestBody ReviewRequest req) {
        return reviewService.create(bookId, req, user.getId());
    }
}
