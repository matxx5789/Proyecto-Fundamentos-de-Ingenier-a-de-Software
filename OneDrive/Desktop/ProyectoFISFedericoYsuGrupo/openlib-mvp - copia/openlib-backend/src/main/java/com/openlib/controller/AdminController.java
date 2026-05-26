package com.openlib.controller;

import com.openlib.dto.request.CategoryRequest;
import com.openlib.dto.request.TagRequest;
import com.openlib.dto.response.*;
import com.openlib.service.AdminService;
import com.openlib.service.BookServicePort;
import com.openlib.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin — Panel de Control")
public class AdminController {

    private final AdminService    adminService;
    private final BookServicePort bookService;
    private final ReviewService   reviewService;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    @Operation(summary = "Métricas generales de la plataforma")
    public DashboardResponse dashboard() {
        return adminService.getDashboard();
    }

    // ── Usuarios ──────────────────────────────────────────────────────────────

    @GetMapping("/users")
    @Operation(summary = "Listar todos los usuarios")
    public Page<UserResponse> users(@RequestParam(defaultValue = "0")  int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return adminService.getUsers(page, size);
    }

    @PutMapping("/users/{id}/toggle-active")
    @Operation(summary = "Activar / desactivar usuario")
    public UserResponse toggleActive(@PathVariable Long id) {
        return adminService.toggleUserActive(id);
    }

    // ── Libros ────────────────────────────────────────────────────────────────

    @GetMapping("/books")
    @Operation(summary = "Listar todos los libros (cualquier estado)")
    public Page<BookResponse> books(@RequestParam(defaultValue = "0")  int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return bookService.getAllForAdmin(page, size);
    }

    @PutMapping("/books/{id}/approve")
    @Operation(summary = "Aprobar publicación de libro")
    public BookResponse approve(@PathVariable Long id) {
        return bookService.approve(id);
    }

    @PutMapping("/books/{id}/reject")
    @Operation(summary = "Rechazar publicación de libro")
    public BookResponse reject(@PathVariable Long id) {
        return bookService.reject(id);
    }

    // ── Reseñas ───────────────────────────────────────────────────────────────

    @GetMapping("/reviews")
    @Operation(summary = "Listar todas las reseñas")
    public Page<ReviewResponse> reviews(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return reviewService.getAllReviews(page, size);
    }

    @PutMapping("/reviews/{reviewId}/hide")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Ocultar reseña (moderación)")
    public void hideReview(@PathVariable Long reviewId) {
        reviewService.delete(reviewId, null);
    }

    @DeleteMapping("/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar reseña")
    public void deleteReview(@PathVariable Long reviewId) {
        reviewService.delete(reviewId, null);
    }

    // ── Categorías ────────────────────────────────────────────────────────────

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear categoría")
    public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest req) {
        return adminService.createCategory(req);
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Editar categoría")
    public CategoryResponse updateCategory(@PathVariable Long id,
                                           @Valid @RequestBody CategoryRequest req) {
        return adminService.updateCategory(id, req);
    }

    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar categoría")
    public void deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
    }

    // ── Tags ──────────────────────────────────────────────────────────────────

    @PostMapping("/tags")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear etiqueta")
    public TagResponse createTag(@Valid @RequestBody TagRequest req) {
        return adminService.createTag(req);
    }

    @DeleteMapping("/tags/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar etiqueta")
    public void deleteTag(@PathVariable Long id) {
        adminService.deleteTag(id);
    }
}
