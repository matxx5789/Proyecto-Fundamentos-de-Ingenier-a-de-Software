package com.openlib.service;

import com.openlib.domain.*;
import com.openlib.dto.request.BookRequest;
import com.openlib.dto.response.*;
import com.openlib.exception.ResourceNotFoundException;
import com.openlib.exception.UnauthorizedException;
import com.openlib.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository     bookRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository      tagRepository;
    private final UserRepository     userRepository;

    // ── Catálogo público ──────────────────────────────────────────────────────

    @Cacheable(value = "books", key = "#query + '_' + #categoryId + '_' + #page + '_' + #size")
    public Page<BookResponse> searchCatalog(String query, Long categoryId,
                                            int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        return bookRepository
                .searchApproved(query, categoryId, pageable)
                .map(this::toResponse);
    }

    public BookResponse findById(Long id) {
        Book book = bookRepository.findById(id)
                .filter(b -> b.getStatus() == BookStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", id));
        return toResponse(book);
    }

    public List<BookResponse> getRecommendations(Long userId, int limit) {
        Page<Book> page = bookRepository.findRecommendationsForUser(
                userId, PageRequest.of(0, limit));
        if (page.isEmpty()) {
            // fallback: top descargados
            page = bookRepository.findTopDownloaded(PageRequest.of(0, limit));
        }
        return page.stream().map(this::toResponse).toList();
    }

    // ── Seller: publicar ──────────────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = "books", allEntries = true)
    public BookResponse publish(BookRequest req, Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", sellerId));

        Book book = Book.builder()
                .title(req.title())
                .subtitle(req.subtitle())
                .author(req.author())
                .isbn(req.isbn())
                .description(req.description())
                .coverUrl(req.coverUrl())
                .pages(req.pages())
                .language(req.language() != null ? req.language() : "es")
                .publishedYear(req.publishedYear())
                .price(req.price() != null ? req.price() : BigDecimal.ZERO)
                .status(BookStatus.PENDING)
                .seller(seller)
                .build();

        if (req.categoryId() != null) {
            book.setCategory(categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría", req.categoryId())));
        }

        if (req.tagIds() != null && !req.tagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(req.tagIds()));
            book.setTags(tags);
        }

        return toResponse(bookRepository.save(book));
    }

    @Transactional
    @CacheEvict(value = "books", allEntries = true)
    public BookResponse update(Long bookId, BookRequest req, Long sellerId) {
        Book book = getOwnedBook(bookId, sellerId);

        book.setTitle(req.title());
        book.setSubtitle(req.subtitle());
        book.setAuthor(req.author());
        book.setIsbn(req.isbn());
        book.setDescription(req.description());
        book.setCoverUrl(req.coverUrl());
        book.setPages(req.pages());
        if (req.language() != null)      book.setLanguage(req.language());
        if (req.publishedYear() != null) book.setPublishedYear(req.publishedYear());
        if (req.price() != null)         book.setPrice(req.price());

        if (req.categoryId() != null) {
            book.setCategory(categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría", req.categoryId())));
        }
        if (req.tagIds() != null) {
            book.setTags(new HashSet<>(tagRepository.findAllById(req.tagIds())));
        }

        return toResponse(bookRepository.save(book));
    }

    @Transactional
    @CacheEvict(value = "books", allEntries = true)
    public void archive(Long bookId, Long sellerId) {
        Book book = getOwnedBook(bookId, sellerId);
        book.setStatus(BookStatus.ARCHIVED);
        bookRepository.save(book);
    }

    public Page<BookResponse> getMyBooks(Long sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return bookRepository.findBySellerId(sellerId, pageable).map(this::toResponse);
    }

    // ── Admin: aprobar / rechazar ─────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = "books", allEntries = true)
    public BookResponse approve(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", bookId));
        book.setStatus(BookStatus.APPROVED);
        return toResponse(bookRepository.save(book));
    }

    @Transactional
    @CacheEvict(value = "books", allEntries = true)
    public BookResponse reject(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", bookId));
        book.setStatus(BookStatus.REJECTED);
        return toResponse(bookRepository.save(book));
    }

    public Page<BookResponse> getAllForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return bookRepository.findAll(pageable).map(this::toResponse);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    public BookResponse toResponse(Book b) {
        return new BookResponse(
            b.getId(), b.getTitle(), b.getSubtitle(), b.getAuthor(), b.getIsbn(),
            b.getDescription(), b.getCoverUrl(), b.getFileSizeBytes(), b.getPages(),
            b.getLanguage(), b.getPublishedYear(), b.getPrice(), b.getStatus(),
            b.getCategory() != null
                ? new CategoryResponse(b.getCategory().getId(), b.getCategory().getName(),
                                       b.getCategory().getSlug(), b.getCategory().getDescription())
                : null,
            b.getTags().stream()
                .map(t -> new TagResponse(t.getId(), t.getName(), t.getSlug()))
                .toList(),
            b.getDownloadCount(), b.getAverageRating(), b.getReviewCount(),
            b.getSeller() != null
                ? new UserResponse(b.getSeller().getId(), b.getSeller().getEmail(),
                                   b.getSeller().getUsername(), b.getSeller().getFullName(),
                                   b.getSeller().getRole(), b.getSeller().getAvatarUrl(),
                                   b.getSeller().getIsActive(), b.getSeller().getIsVerified(),
                                   b.getSeller().getCreatedAt())
                : null,
            b.getCreatedAt()
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Book getOwnedBook(Long bookId, Long sellerId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", bookId));
        if (!book.getSeller().getId().equals(sellerId)) {
            throw new UnauthorizedException("No tienes permiso para modificar este libro");
        }
        return book;
    }

    private Pageable buildPageable(int page, int size, String sort) {
        Sort s = switch (sort == null ? "recent" : sort) {
            case "downloads" -> Sort.by("downloadCount").descending();
            case "rating"    -> Sort.by("averageRating").descending();
            case "title"     -> Sort.by("title").ascending();
            default          -> Sort.by("createdAt").descending();
        };
        return PageRequest.of(page, Math.min(size, 50), s);
    }
}
