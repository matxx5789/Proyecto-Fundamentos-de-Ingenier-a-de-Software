package com.openlib.service;

import com.openlib.domain.Book;
import com.openlib.domain.BookStatus;
import com.openlib.domain.CartItem;
import com.openlib.domain.Category;
import com.openlib.domain.Download;
import com.openlib.domain.Order;
import com.openlib.domain.Review;
import com.openlib.domain.Tag;
import com.openlib.domain.User;
import com.openlib.domain.Wishlist;
import com.openlib.repository.BookRepository;
import com.openlib.repository.CartItemRepository;
import com.openlib.repository.CategoryRepository;
import com.openlib.repository.DownloadRepository;
import com.openlib.repository.OrderRepository;
import com.openlib.repository.ReviewRepository;
import com.openlib.repository.TagRepository;
import com.openlib.repository.UserRepository;
import com.openlib.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio central de acceso a datos que encapsula operaciones de lectura y escritura
 * sobre las entidades principales del proyecto OpenLib.
 *
 * Esta clase se puede usar desde controladores o servicios adicionales cuando se
 * necesita acceder de forma coordinada a varios repositorios.
 */
@Service
@RequiredArgsConstructor
public class DatabaseAccessService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final DownloadRepository downloadRepository;
    private final ReviewRepository reviewRepository;
    private final WishlistRepository wishlistRepository;

    // ---------- Usuarios ----------

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // ---------- Libros ----------

    public Optional<Book> findBookById(Long id) {
        return bookRepository.findById(id);
    }

    public Page<Book> searchApprovedBooks(String query, Long categoryId, Pageable pageable) {
        return bookRepository.searchApproved(query, categoryId, pageable);
    }

    public Page<Book> findBooksBySeller(Long sellerId, Pageable pageable) {
        return bookRepository.findBySellerId(sellerId, pageable);
    }

    public Page<Book> findBooksByStatus(String status, Pageable pageable) {
        return bookRepository.findByStatus(BookStatus.valueOf(status.toUpperCase()), pageable);
    }

    public List<Book> findPurchasedBooksByUser(Long userId) {
        return bookRepository.findPurchasedByUser(userId);
    }

    public Page<Book> findRecommendedBooks(Long userId, Pageable pageable) {
        return bookRepository.findRecommendationsForUser(userId, pageable);
    }

    @Transactional
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    @Transactional
    public void incrementBookDownloadCount(Long bookId) {
        bookRepository.incrementDownloadCount(bookId);
    }

    // ---------- Categorías ----------

    public Optional<Category> findCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public Optional<Category> findCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    // ---------- Etiquetas ----------

    public Optional<Tag> findTagById(Long id) {
        return tagRepository.findById(id);
    }

    public Optional<Tag> findTagBySlug(String slug) {
        return tagRepository.findBySlug(slug);
    }

    public List<Tag> findAllTags() {
        return tagRepository.findAll();
    }

    @Transactional
    public Tag saveTag(Tag tag) {
        return tagRepository.save(tag);
    }

    // ---------- Carrito ----------

    public List<CartItem> findCartItemsByUser(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    @Transactional
    public CartItem addCartItem(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    @Transactional
    public void removeCartItem(CartItem cartItem) {
        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void removeCartItemByUserAndBook(Long userId, Long bookId) {
        cartItemRepository.deleteByUserIdAndBookId(userId, bookId);
    }

    // ---------- Órdenes ----------

    public Optional<Order> findOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Page<Order> findOrdersByBuyer(Long buyerId, Pageable pageable) {
        return orderRepository.findByBuyerId(buyerId, pageable);
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    // ---------- Descargas ----------

    public Optional<Download> findDownloadById(Long id) {
        return downloadRepository.findById(id);
    }

    public Optional<Download> findDownloadByToken(String token) {
        return downloadRepository.findBySignedToken(token);
    }

    public List<Download> findDownloadsByUser(Long userId) {
        return downloadRepository.findByUserId(userId);
    }

    @Transactional
    public Download saveDownload(Download download) {
        return downloadRepository.save(download);
    }

    // ---------- Wishlist ----------

    public List<Wishlist> findWishlistByUser(Long userId) {
        return wishlistRepository.findByUserId(userId);
    }

    @Transactional
    public Wishlist saveWishlistItem(Wishlist wishlist) {
        return wishlistRepository.save(wishlist);
    }

    @Transactional
    public void removeWishlistItemByUserAndBook(Long userId, Long bookId) {
        wishlistRepository.deleteByUserIdAndBookId(userId, bookId);
    }

    // ---------- Reseñas ----------

    public Optional<Review> findReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    public Page<Review> findReviewsByBook(Long bookId, Pageable pageable) {
        return reviewRepository.findByBookIdAndIsVisibleTrue(bookId, pageable);
    }

    public Optional<Review> findReviewByUserAndBook(Long userId, Long bookId) {
        return reviewRepository.findByUserIdAndBookId(userId, bookId);
    }

    @Transactional
    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }
}
