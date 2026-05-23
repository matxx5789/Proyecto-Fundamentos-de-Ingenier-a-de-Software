package com.openlib.service;

import com.openlib.domain.*;
import com.openlib.dto.request.CategoryRequest;
import com.openlib.dto.request.TagRequest;
import com.openlib.dto.response.*;
import com.openlib.exception.BusinessException;
import com.openlib.exception.ResourceNotFoundException;
import com.openlib.repository.*;
import com.openlib.service.BookServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository     userRepository;
    private final BookRepository     bookRepository;
    private final DownloadRepository downloadRepository;
    private final OrderRepository    orderRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository      tagRepository;
    private final BookServicePort    bookService;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    public DashboardResponse getDashboard() {
        long totalUsers     = userRepository.count();
        long totalBooks     = bookRepository.count();
        long totalOrders    = orderRepository.count();
        long totalDownloads = downloadRepository.count();

        List<BookResponse> topBooks = bookRepository
                .findTopDownloaded(PageRequest.of(0, 10))
                .stream().map(bookService::toResponse).toList();

        return new DashboardResponse(
                totalUsers, totalBooks, totalOrders, totalDownloads,
                topBooks, List.of(), 0L);
    }

    // ── Usuarios ──────────────────────────────────────────────────────────────

    public Page<UserResponse> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findAll(pageable).map(this::toUserResponse);
    }

    @Transactional
    public UserResponse toggleUserActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));
        user.setIsActive(!user.getIsActive());
        return toUserResponse(userRepository.save(user));
    }

    // ── Categorías ────────────────────────────────────────────────────────────

    @Transactional
    public CategoryResponse createCategory(CategoryRequest req) {
        if (categoryRepository.existsByName(req.name())) {
            throw new BusinessException("La categoría ya existe: " + req.name());
        }
        Category cat = Category.builder()
                .name(req.name()).slug(req.slug()).description(req.description()).build();
        cat = categoryRepository.save(cat);
        return new CategoryResponse(cat.getId(), cat.getName(), cat.getSlug(), cat.getDescription());
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest req) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", id));
        cat.setName(req.name());
        cat.setSlug(req.slug());
        cat.setDescription(req.description());
        cat = categoryRepository.save(cat);
        return new CategoryResponse(cat.getId(), cat.getName(), cat.getSlug(), cat.getDescription());
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoría", id);
        }
        categoryRepository.deleteById(id);
    }

    // ── Tags ──────────────────────────────────────────────────────────────────

    @Transactional
    public TagResponse createTag(TagRequest req) {
        if (tagRepository.existsByName(req.name())) {
            throw new BusinessException("La etiqueta ya existe: " + req.name());
        }
        Tag tag = Tag.builder().name(req.name()).slug(req.slug()).build();
        tag = tagRepository.save(tag);
        return new TagResponse(tag.getId(), tag.getName(), tag.getSlug());
    }

    @Transactional
    public void deleteTag(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new ResourceNotFoundException("Etiqueta", id);
        }
        tagRepository.deleteById(id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private UserResponse toUserResponse(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getUsername(),
                u.getFullName(), u.getRole(), u.getAvatarUrl(),
                u.getIsActive(), u.getIsVerified(), u.getCreatedAt());
    }
}
