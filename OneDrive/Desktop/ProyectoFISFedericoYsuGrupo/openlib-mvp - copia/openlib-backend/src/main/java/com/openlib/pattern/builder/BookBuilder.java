package com.openlib.pattern.builder;

import com.openlib.domain.Book;
import com.openlib.domain.BookStatus;
import com.openlib.domain.Category;
import com.openlib.domain.Tag;
import com.openlib.domain.User;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class BookBuilder {

    private String title;
    private String subtitle;
    private String author;
    private String isbn;
    private String description;
    private String coverUrl;
    private Long fileSizeBytes;
    private Integer pages;
    private String language;
    private Integer publishedYear;
    private BigDecimal price;
    private BookStatus status;
    private User seller;
    private Category category;
    private Set<Tag> tags = new HashSet<>();

    private BookBuilder() {
        this.language = "es";
        this.price = BigDecimal.ZERO;
        this.status = BookStatus.PENDING;
    }

    public static BookBuilder create() {
        return new BookBuilder();
    }

    public BookBuilder title(String title) {
        this.title = title;
        return this;
    }

    public BookBuilder subtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public BookBuilder author(String author) {
        this.author = author;
        return this;
    }

    public BookBuilder isbn(String isbn) {
        this.isbn = isbn;
        return this;
    }

    public BookBuilder description(String description) {
        this.description = description;
        return this;
    }

    public BookBuilder coverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
        return this;
    }

    public BookBuilder fileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
        return this;
    }

    public BookBuilder pages(Integer pages) {
        this.pages = pages;
        return this;
    }

    public BookBuilder language(String language) {
        if (language != null && !language.isBlank()) {
            this.language = language;
        }
        return this;
    }

    public BookBuilder publishedYear(Integer publishedYear) {
        this.publishedYear = publishedYear;
        return this;
    }

    public BookBuilder price(BigDecimal price) {
        if (price != null) {
            this.price = price;
        }
        return this;
    }

    public BookBuilder status(BookStatus status) {
        if (status != null) {
            this.status = status;
        }
        return this;
    }

    public BookBuilder seller(User seller) {
        this.seller = seller;
        return this;
    }

    public BookBuilder category(Category category) {
        this.category = category;
        return this;
    }

    public BookBuilder tags(Set<Tag> tags) {
        if (tags != null) {
            this.tags = new HashSet<>(tags);
        }
        return this;
    }

    public Book build() {
        if (title == null || title.isBlank()) {
            throw new IllegalStateException("El título es obligatorio");
        }
        if (author == null || author.isBlank()) {
            throw new IllegalStateException("El autor es obligatorio");
        }
        if (seller == null) {
            throw new IllegalStateException("El vendedor es obligatorio");
        }

        Book book = new Book();
        book.setTitle(title);
        book.setSubtitle(subtitle);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setDescription(description);
        book.setCoverUrl(coverUrl);
        book.setFileSizeBytes(fileSizeBytes);
        book.setPages(pages);
        book.setLanguage(language);
        book.setPublishedYear(publishedYear);
        book.setPrice(price);
        book.setStatus(status);
        book.setSeller(seller);
        book.setCategory(category);
        book.setTags(tags);
        return book;
    }
}
