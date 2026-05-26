package com.openlib.service;

import com.openlib.domain.*;
import com.openlib.dto.request.BookRequest;
import com.openlib.dto.response.BookResponse;
import com.openlib.exception.ResourceNotFoundException;
import com.openlib.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService — Unit Tests")
class BookServiceTest {

    @Mock private BookRepository     bookRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private TagRepository      tagRepository;
    @Mock private UserRepository     userRepository;

    @InjectMocks
    private BookService bookService;

    private User buildSeller() {
        return User.builder()
                .id(1L).email("seller@test.com").username("seller")
                .role(Role.SELLER).isActive(true).isVerified(true).build();
    }

    private Book buildBook(User seller) {
        return Book.builder()
                .id(10L).title("Test Book").author("Author A")
                .status(BookStatus.APPROVED).price(BigDecimal.ZERO)
                .seller(seller).build();
    }

    @Test
    @DisplayName("findById() — retorna libro aprobado")
    void findById_approved() {
        User seller = buildSeller();
        Book book   = buildBook(seller);

        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

        BookResponse resp = bookService.findById(10L);

        assertThat(resp.id()).isEqualTo(10L);
        assertThat(resp.title()).isEqualTo("Test Book");
    }

    @Test
    @DisplayName("findById() — lanza excepción si libro no aprobado")
    void findById_notApproved_throws() {
        User seller = buildSeller();
        Book book   = buildBook(seller);
        book.setStatus(BookStatus.PENDING);

        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.findById(10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("publish() — crea libro en estado PENDING")
    void publish_success() {
        User seller = buildSeller();
        BookRequest req = new BookRequest(
                "New Book", null, "Author", null, "Desc",
                null, 200, "es", 2024, BigDecimal.ZERO, null, List.of());

        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
        Book saved = Book.builder().id(20L).title("New Book")
                .author("Author").status(BookStatus.PENDING)
                .seller(seller).price(BigDecimal.ZERO).build();
        when(bookRepository.save(any())).thenReturn(saved);

        BookResponse resp = bookService.publish(req, 1L);

        assertThat(resp.status()).isEqualTo(BookStatus.PENDING);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("approve() — cambia estado a APPROVED")
    void approve_success() {
        User seller = buildSeller();
        Book book   = buildBook(seller);
        book.setStatus(BookStatus.PENDING);

        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);

        BookResponse resp = bookService.approve(10L);

        assertThat(resp.status()).isEqualTo(BookStatus.APPROVED);
    }

    @Test
    @DisplayName("searchCatalog() — delega en repositorio con parámetros")
    void searchCatalog_delegatesCorrectly() {
        when(bookRepository.searchApproved(eq("spring"), isNull(), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<BookResponse> result = bookService.searchCatalog("spring", null, 0, 10, "recent");

        assertThat(result).isEmpty();
        verify(bookRepository).searchApproved(eq("spring"), isNull(), any(Pageable.class));
    }
}
