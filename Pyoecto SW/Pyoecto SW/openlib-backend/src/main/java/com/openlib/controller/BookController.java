package com.openlib.controller;

import com.openlib.domain.User;
import com.openlib.dto.response.BookResponse;
import com.openlib.dto.response.CategoryResponse;
import com.openlib.dto.response.TagResponse;
import com.openlib.repository.CategoryRepository;
import com.openlib.repository.TagRepository;
import com.openlib.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Tag(name = "Catálogo de Libros")
public class BookController {

    private final BookService        bookService;
    private final CategoryRepository categoryRepository;
    private final TagRepository      tagRepository;

    @GetMapping
    @Operation(summary = "Buscar y filtrar catálogo de libros aprobados")
    public Page<BookResponse> search(
            @RequestParam(required = false)            String query,
            @RequestParam(required = false)            Long   categoryId,
            @RequestParam(defaultValue = "0")          int    page,
            @RequestParam(defaultValue = "20")         int    size,
            @RequestParam(defaultValue = "recent")     String sort) {
        return bookService.searchCatalog(query, categoryId, page, size, sort);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un libro")
    public BookResponse getById(@PathVariable Long id) {
        return bookService.findById(id);
    }

    @GetMapping("/recommendations")
    @Operation(summary = "Recomendaciones personalizadas (requiere login)")
    public List<BookResponse> recommendations(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "10") int limit) {
        return bookService.getRecommendations(user.getId(), limit);
    }

    @GetMapping("/top")
    @Operation(summary = "Libros más descargados")
    public Page<BookResponse> topDownloaded(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return bookService.searchCatalog(null, null, page, size, "downloads");
    }

    @GetMapping("/categories")
    @Operation(summary = "Listado de categorías")
    public List<CategoryResponse> categories() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getSlug(), c.getDescription()))
                .toList();
    }

    @GetMapping("/tags")
    @Operation(summary = "Listado de etiquetas")
    public List<TagResponse> tags() {
        return tagRepository.findAll().stream()
                .map(t -> new TagResponse(t.getId(), t.getName(), t.getSlug()))
                .toList();
    }
}
