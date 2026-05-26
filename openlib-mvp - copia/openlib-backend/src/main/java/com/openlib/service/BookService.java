package com.openlib.service;

import com.openlib.domain.*;
import com.openlib.dto.request.BookRequest;
import com.openlib.dto.response.*;
import com.openlib.exception.ResourceNotFoundException;
import com.openlib.exception.UnauthorizedException;
import com.openlib.pattern.builder.BookBuilder;
import com.openlib.pattern.command.ArchiveBookCommand;
import com.openlib.pattern.command.ApproveBookCommand;
import com.openlib.pattern.command.BookModerationCommandInvoker;
import com.openlib.pattern.command.RejectBookCommand;
import com.openlib.repository.*;
import com.openlib.service.BookServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service("bookServiceCore")
@RequiredArgsConstructor
public class BookService implements BookServicePort {

    private final BookRepository     bookRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository      tagRepository;
    private final UserRepository     userRepository;
    private final BookModerationCommandInvoker commandInvoker;

    // ── Catálogo público ──────────────────────────────────────────────────────

        public Page<BookResponse> searchCatalog(String query, Long categoryId,
                                            int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);

        // If there's no search query, use repository search which is efficient
        if (query == null || query.isBlank()) {
            return bookRepository.searchApproved(null, categoryId, pageable).map(this::toResponse);
        }

        // For free-text search, fetch approved books and apply case-insensitive filtering in-memory
        // to avoid DB-specific lower() issues while still respecting category selection.
        Page<Book> basePage = bookRepository.findByStatus(BookStatus.APPROVED, Pageable.unpaged());

        List<Book> filtered = basePage.stream()
                .filter(b -> categoryId == null || (b.getCategory() != null && b.getCategory().getId().equals(categoryId)))
                .filter(b -> {
                    String q = query.toLowerCase();
                    return (b.getTitle() != null && b.getTitle().toLowerCase().contains(q))
                            || (b.getAuthor() != null && b.getAuthor().toLowerCase().contains(q))
                            || (b.getIsbn() != null && b.getIsbn().toLowerCase().contains(q));
                })
                .toList();

        int total = filtered.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<BookResponse> content = filtered.subList(from, to).stream().map(this::toResponse).toList();

        return new org.springframework.data.domain.PageImpl<>(content, pageable, total);
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
    public BookResponse publish(BookRequest req, Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", sellerId));

        BookBuilder builder = BookBuilder.create()
                .title(req.title())
                .subtitle(req.subtitle())
                .author(req.author())
                .isbn(req.isbn())
                .description(req.description())
                .coverUrl(req.coverUrl())
                .pages(req.pages())
                .language(req.language())
                .publishedYear(req.publishedYear())
                .price(req.price())
                .seller(seller)
                .status(BookStatus.PENDING);

        if (req.categoryId() != null) {
            builder.category(categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría", req.categoryId())));
        }

        if (req.tagIds() != null && !req.tagIds().isEmpty()) {
            builder.tags(new HashSet<>(tagRepository.findAllById(req.tagIds())));
        }

        Book book = builder.build();
        return toResponse(bookRepository.save(book));
    }

    @Transactional
    
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
    public void archive(Long bookId, Long sellerId) {
        Book book = getOwnedBook(bookId, sellerId);
        BookResponse response = commandInvoker.execute(new ArchiveBookCommand(book, bookRepository, this::toResponse));
        if (response == null) {
            throw new IllegalStateException("No se pudo archivar el libro");
        }
    }

    public Page<BookResponse> getMyBooks(Long sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return bookRepository.findBySellerId(sellerId, pageable).map(this::toResponse);
    }

    // ── Admin: aprobar / rechazar ─────────────────────────────────────────────

    @Transactional
    public BookResponse approve(Long bookId) {
        return commandInvoker.execute(new ApproveBookCommand(bookId, bookRepository, this::toResponse));
    }

    @Transactional
    public BookResponse reject(Long bookId) {
        return commandInvoker.execute(new RejectBookCommand(bookId, bookRepository, this::toResponse));
    }

    public Page<BookResponse> getAllForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return bookRepository.findAll(pageable).map(this::toResponse);
    }

    public List<BookResponse> getNotApprovedBooks() {
        return bookRepository.findByStatusNot(BookStatus.APPROVED).stream()
                .map(this::toResponse)
                .toList();
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
