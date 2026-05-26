package com.openlib.config;

import com.openlib.domain.*;
import com.openlib.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Inicializa datos de demo en la base de datos H2 al arrancar la aplicación.
 * Reemplaza el seed SQL de Flyway (V2) para garantizar que los hashes BCrypt
 * sean generados correctamente por el PasswordEncoder de Spring Security.
 *
 * Usuarios demo:
 *   admin@openlib.com  / Admin1234!   (ADMIN)
 *   seller@openlib.com / Seller1234!  (SELLER)
 *   buyer@openlib.com  / Buyer1234!   (BUYER)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository     userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository      tagRepository;
    private final BookRepository     bookRepository;
    private final PasswordEncoder    passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("DataInitializer: verificando datos de demo...");

        boolean hasAdmin  = userRepository.existsByEmail("admin@openlib.com");
        boolean hasSeller = userRepository.existsByEmail("seller@openlib.com");
        boolean hasBuyer  = userRepository.existsByEmail("buyer@openlib.com");

        if (!hasAdmin || !hasSeller || !hasBuyer || categoryRepository.count() == 0 || tagRepository.count() == 0 || bookRepository.count() == 0) {
            log.info("DataInitializer: inicializando datos de demo...");
        } else {
            log.info("DataInitializer: datos ya presentes. Verificando actualizaciones de libros de ejemplo...");
        }

        // ── Usuarios ──────────────────────────────────────────────────────────
        User admin = userRepository.findByEmail("admin@openlib.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("admin@openlib.com")
                        .username("admin")
                        .passwordHash(passwordEncoder.encode("Admin1234!"))
                        .fullName("Administrador OpenLib")
                        .role(Role.ADMIN)
                        .isActive(true)
                        .isVerified(true)
                        .build()));

        User seller = userRepository.findByEmail("seller@openlib.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("seller@openlib.com")
                        .username("demo_seller")
                        .passwordHash(passwordEncoder.encode("Seller1234!"))
                        .fullName("Seller Demo")
                        .role(Role.SELLER)
                        .isActive(true)
                        .isVerified(true)
                        .build()));

        User buyer = userRepository.findByEmail("buyer@openlib.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("buyer@openlib.com")
                        .username("demo_buyer")
                        .passwordHash(passwordEncoder.encode("Buyer1234!"))
                        .fullName("Buyer Demo")
                        .role(Role.BUYER)
                        .isActive(true)
                        .isVerified(true)
                        .build()));

        log.info("DataInitializer: usuarios asegurados: {}, {}, {}",
                admin.getEmail(), seller.getEmail(), buyer.getEmail());

        // ── Categorías ────────────────────────────────────────────────────────
        Category catProg = saveCategory("Programación",        "programacion",        "Libros sobre lenguajes, algoritmos y patrones de diseño");
        Category catData = saveCategory("Ciencias de Datos",   "ciencias-de-datos",   "Machine learning, estadística y análisis de datos");
        Category catMath = saveCategory("Matemáticas",         "matematicas",          "Álgebra, cálculo, estadística y matemática discreta");
                           saveCategory("Ingeniería",          "ingenieria",           "Libros de ingeniería de software y sistemas");
                           saveCategory("Bases de Datos",      "bases-de-datos",       "SQL, NoSQL, modelado y administración de BD");
                           saveCategory("Redes y Seguridad",   "redes-y-seguridad",    "Redes de computadoras, ciberseguridad y criptografía");
                           saveCategory("Sistemas Operativos", "sistemas-operativos",  "Linux, Windows, kernels y administración de sistemas");

        // ── Etiquetas ─────────────────────────────────────────────────────────
        Tag tagJava    = saveTag("Java",        "java");
        Tag tagPython  = saveTag("Python",      "python");
        Tag tagJs      = saveTag("JavaScript",  "javascript");
        Tag tagSpring  = saveTag("Spring",      "spring");
        Tag tagSql     = saveTag("SQL",         "sql");
        Tag tagNoSql   = saveTag("NoSQL",       "nosql");
        Tag tagDocker  = saveTag("Docker",      "docker");
        Tag tagGit     = saveTag("Git",         "git");
        Tag tagOpen    = saveTag("Open Source", "open-source");
        Tag tagFree    = saveTag("Gratuito",    "gratuito");
        Tag tagAcad    = saveTag("Académico",   "academico");

        // ── Libros de muestra ─────────────────────────────────────────────────
        saveBookIfMissing("Clean Code: A Handbook of Agile Software Craftsmanship",
                "Robert C. Martin", "9780132350884",
                "Guía definitiva para escribir código limpio, mantenible y profesional.",
                "en", 2008, catProg, seller,
                new BigDecimal("29.90"), 4.80, 42, 464,
                "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=600&q=80",
                Set.of(tagJava, tagOpen, tagAcad));

        saveBookIfMissing("Designing Data-Intensive Applications",
                "Martin Kleppmann", "9781491903124",
                "El libro definitivo para entender sistemas distribuidos, bases de datos y arquitecturas modernas de datos.",
                "en", 2017, catData, seller,
                new BigDecimal("34.50"), 4.90, 78, 616,
                "https://images.unsplash.com/photo-1518779578993-ec3579fee39f?auto=format&fit=crop&w=600&q=80",
                Set.of(tagNoSql, tagSql, tagDocker, tagAcad));

        saveBookIfMissing("Spring Boot en Acción",
                "Craig Walls", "9781617292545",
                "Aprende a construir aplicaciones Spring Boot desde cero hasta producción.",
                "es", 2022, catProg, seller,
                new BigDecimal("24.00"), 4.70, 35, 448,
                "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=600&q=80",
                Set.of(tagJava, tagSpring, tagDocker));

        saveBookIfMissing("The Pragmatic Programmer",
                "David Thomas, Andrew Hunt", "9780135957059",
                "Tu viaje hacia la maestría en el desarrollo de software.",
                "en", 2019, catProg, seller,
                new BigDecimal("32.00"), 4.85, 91, 352,
                "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=600&q=80",
                Set.of(tagOpen, tagGit, tagAcad));

        saveBookIfMissing("Introduction to Algorithms",
                "Thomas H. Cormen", "9780262033848",
                "El libro de referencia más completo sobre algoritmos y estructuras de datos.",
                "en", 2022, catMath, seller,
                new BigDecimal("39.95"), 4.60, 120, 1312,
                "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=600&q=80",
                Set.of(tagAcad, tagFree));

        log.info("DataInitializer: seed completado exitosamente.");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Category saveCategory(String name, String slug, String description) {
        return categoryRepository.findBySlug(slug)
                .or(() -> categoryRepository.findByName(name))
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name).slug(slug).description(description).build()));
    }

    private Tag saveTag(String name, String slug) {
        return tagRepository.findBySlug(slug)
                .or(() -> tagRepository.findByName(name))
                .orElseGet(() -> tagRepository.save(Tag.builder().name(name).slug(slug).build()));
    }

    private void saveBookIfMissing(String title, String author, String isbn, String description,
                                   String language, int year, Category category, User seller,
                                   BigDecimal price, double rating, int reviewCount, int pages,
                                   String coverUrl, Set<Tag> tags) {
        if (isbn != null) {
            bookRepository.findByIsbn(isbn).ifPresentOrElse(book -> {
                book.setPrice(price);
                book.setPages(pages);
                book.setCoverUrl(coverUrl);
                book.setDescription(description);
                book.setLanguage(language);
                book.setPublishedYear(year);
                book.setStatus(BookStatus.APPROVED);
                book.setCategory(category);
                book.setSeller(seller);
                book.setTags(new HashSet<>(tags));
                bookRepository.save(book);
            }, () -> saveBook(title, author, isbn, description, language, year, category, seller, price, rating, reviewCount, pages, coverUrl, tags));
        } else {
            saveBook(title, author, isbn, description, language, year, category, seller, price, rating, reviewCount, pages, coverUrl, tags);
        }
    }

    private void saveBook(String title, String author, String isbn, String description,
                          String language, int year, Category category, User seller,
                          BigDecimal price, double rating, int reviewCount, int pages,
                          String coverUrl, Set<Tag> tags) {
        Book book = Book.builder()
                .title(title)
                .author(author)
                .isbn(isbn)
                .description(description)
                .language(language)
                .publishedYear(year)
                .price(price)
                .pages(pages)
                .coverUrl(coverUrl)
                .status(BookStatus.APPROVED)
                .category(category)
                .seller(seller)
                .averageRating(new BigDecimal(String.valueOf(rating)))
                .reviewCount(reviewCount)
                .downloadCount(0L)
                .tags(new HashSet<>(tags))
                .build();
        bookRepository.save(book);
    }
}
