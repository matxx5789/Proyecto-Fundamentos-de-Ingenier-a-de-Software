package com.openlib.fx.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.openlib.fx.model.Models;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Capa de persistencia basada en archivos JSON.
 * Reemplaza completamente el backend Spring Boot.
 *
 * Estructura de archivos (en ./data/):
 *   users.json       — usuarios registrados
 *   books.json       — catálogo de libros
 *   categories.json  — categorías
 *   cart.json        — carritos por usuario
 *   orders.json      — órdenes de compra
 *   library.json     — biblioteca personal (libros adquiridos)
 *   wishlist.json    — listas de favoritos
 *   reviews.json     — reseñas
 */
public class LocalDataService {

    // ── Directorio de datos ────────────────────────────────────────
    private static final Path DATA_DIR = Paths.get("data");
    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private static final Path USERS_FILE      = DATA_DIR.resolve("users.json");
    private static final Path BOOKS_FILE      = DATA_DIR.resolve("books.json");
    private static final Path CATEGORIES_FILE = DATA_DIR.resolve("categories.json");
    private static final Path CART_FILE       = DATA_DIR.resolve("cart.json");
    private static final Path ORDERS_FILE     = DATA_DIR.resolve("orders.json");
    private static final Path LIBRARY_FILE    = DATA_DIR.resolve("library.json");
    private static final Path WISHLIST_FILE   = DATA_DIR.resolve("wishlist.json");
    private static final Path REVIEWS_FILE    = DATA_DIR.resolve("reviews.json");

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // ── Inicialización ─────────────────────────────────────────────

    /** Crea el directorio de datos y los archivos de seed si no existen. */
    public static void init() {
        try {
            Files.createDirectories(DATA_DIR);
            if (!Files.exists(USERS_FILE))      seedUsers();
            if (!Files.exists(BOOKS_FILE))      seedBooks();
            if (!Files.exists(CATEGORIES_FILE)) seedCategories();
            if (!Files.exists(CART_FILE))       write(CART_FILE, new HashMap<>());
            if (!Files.exists(ORDERS_FILE))     write(ORDERS_FILE, new ArrayList<>());
            if (!Files.exists(LIBRARY_FILE))    write(LIBRARY_FILE, new HashMap<>());
            if (!Files.exists(WISHLIST_FILE))   write(WISHLIST_FILE, new HashMap<>());
            if (!Files.exists(REVIEWS_FILE))    write(REVIEWS_FILE, new ArrayList<>());
        } catch (Exception e) {
            throw new RuntimeException("No se pudo inicializar el directorio de datos: " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // AUTH
    // ══════════════════════════════════════════════════════════════

    /** Intenta login. Retorna AuthResponse o lanza excepción con mensaje legible. */
    public static Models.AuthResponse login(String email, String password) {
        List<Map<String,Object>> users = readUsers();
        for (var u : users) {
            if (email.equalsIgnoreCase((String) u.get("email"))) {
                String storedHash = (String) u.get("passwordHash");
                if (!checkPassword(password, storedHash)) {
                    throw new RuntimeException("Contraseña incorrecta.");
                }
                Boolean active   = (Boolean) u.getOrDefault("isActive", true);
                Boolean verified = (Boolean) u.getOrDefault("isVerified", true);
                if (!active)   throw new RuntimeException("La cuenta está desactivada.");
                if (!verified) throw new RuntimeException("La cuenta no está verificada.");

                Models.AuthResponse auth = new Models.AuthResponse();
                auth.accessToken  = "local-token-" + u.get("id");
                auth.refreshToken = UUID.randomUUID().toString();
                auth.tokenType    = "Bearer";
                auth.email        = (String) u.get("email");
                auth.role         = (String) u.get("role");
                auth.fullName     = (String) u.get("fullName");
                auth.userId       = toLong(u.get("id"));
                return auth;
            }
        }
        throw new RuntimeException("No existe ningún usuario con ese email.");
    }

    /** Registra un nuevo usuario. */
    public static Models.AuthResponse register(String fullName, String username,
                                                String email, String password, String role) {
        List<Map<String,Object>> users = readUsers();
        for (var u : users) {
            if (email.equalsIgnoreCase((String) u.get("email")))
                throw new RuntimeException("El email ya está registrado: " + email);
            if (username.equalsIgnoreCase((String) u.get("username")))
                throw new RuntimeException("El nombre de usuario ya está en uso: " + username);
        }
        if (password.length() < 8)
            throw new RuntimeException("La contraseña debe tener al menos 8 caracteres.");

        long newId = users.stream().mapToLong(u -> toLong(u.get("id"))).max().orElse(0) + 1;
        Map<String,Object> user = new LinkedHashMap<>();
        user.put("id",           newId);
        user.put("email",        email);
        user.put("username",     username);
        user.put("passwordHash", hashPassword(password));
        user.put("fullName",     fullName);
        user.put("role",         "ADMIN".equals(role) ? "BUYER" : role); // no se puede registrar como ADMIN
        user.put("isActive",     true);
        user.put("isVerified",   true);
        user.put("createdAt",    now());
        users.add(user);
        write(USERS_FILE, users);

        Models.AuthResponse auth = new Models.AuthResponse();
        auth.accessToken  = "local-token-" + newId;
        auth.refreshToken = UUID.randomUUID().toString();
        auth.tokenType    = "Bearer";
        auth.email        = email;
        auth.role         = (String) user.get("role");
        auth.fullName     = fullName;
        auth.userId       = newId;
        return auth;
    }

    // ══════════════════════════════════════════════════════════════
    // BOOKS / CATÁLOGO
    // ══════════════════════════════════════════════════════════════

    public static Models.PagedBooks getBooks(String query, String category, String sort, int page, int size) {
        List<Map<String,Object>> all = readBooks();

        // Filtrar solo aprobados
        var filtered = all.stream()
                .filter(b -> "APPROVED".equals(b.get("status")))
                .collect(Collectors.toList());

        // Filtrar por búsqueda
        if (query != null && !query.isBlank()) {
            String q = query.toLowerCase();
            filtered = filtered.stream().filter(b ->
                    contains(b, "title",  q) ||
                    contains(b, "author", q) ||
                    contains(b, "isbn",   q) ||
                    contains(b, "description", q)
            ).collect(Collectors.toList());
        }

        // Filtrar por categoría
        if (category != null && !category.isBlank() && !"Todas".equals(category)) {
            filtered = filtered.stream()
                    .filter(b -> category.equals(b.get("category")))
                    .collect(Collectors.toList());
        }

        // Ordenar
        if ("Más descargado".equals(sort)) {
            filtered.sort((a, b) -> Long.compare(toLong(b.get("downloadCount")), toLong(a.get("downloadCount"))));
        } else if ("Mejor calificado".equals(sort)) {
            filtered.sort((a, b) -> Double.compare(toDouble(b.get("averageRating")), toDouble(a.get("averageRating"))));
        } else if ("Título A-Z".equals(sort)) {
            filtered.sort(Comparator.comparing(b -> str(b.get("title")).toLowerCase()));
        } else {
            // Más reciente: orden inverso de id
            filtered.sort((a, b) -> Long.compare(toLong(b.get("id")), toLong(a.get("id"))));
        }

        // Paginar
        int total = filtered.size();
        int from  = Math.min(page * size, total);
        int to    = Math.min(from + size, total);
        List<Models.BookResponse> content = filtered.subList(from, to).stream()
                .map(LocalDataService::toBookResponse)
                .collect(Collectors.toList());

        Models.PagedBooks paged = new Models.PagedBooks();
        paged.content       = content;
        paged.totalElements = total;
        paged.totalPages    = (int) Math.ceil((double) total / size);
        paged.number        = page;
        paged.size          = size;
        return paged;
    }

    public static Models.BookResponse getBook(long id) {
        return readBooks().stream()
                .filter(b -> toLong(b.get("id")) == id)
                .findFirst()
                .map(LocalDataService::toBookResponse)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado: " + id));
    }

    /** Libros del seller actual. */
    public static List<Models.BookResponse> getSellerBooks(long sellerId) {
        return readBooks().stream()
                .filter(b -> toLong(b.get("sellerId")) == sellerId)
                .map(LocalDataService::toBookResponse)
                .collect(Collectors.toList());
    }

    /** Publica un nuevo libro (status PENDING para revisión admin). */
    public static Models.BookResponse publishBook(long sellerId, String sellerName,
                                                   String title, String author, String isbn,
                                                   String description, String category,
                                                   double price, String language, int year) {
        List<Map<String,Object>> books = readBooks();
        long newId = books.stream().mapToLong(b -> toLong(b.get("id"))).max().orElse(0) + 1;

        Map<String,Object> book = new LinkedHashMap<>();
        book.put("id",            newId);
        book.put("title",         title);
        book.put("author",        author);
        book.put("isbn",          isbn);
        book.put("description",   description);
        book.put("category",      category);
        book.put("price",         price);
        book.put("language",      language);
        book.put("publishedYear", year);
        book.put("status",        "PENDING");
        book.put("sellerId",      sellerId);
        book.put("sellerName",    sellerName);
        book.put("averageRating", 0.0);
        book.put("reviewCount",   0);
        book.put("downloadCount", 0L);
        book.put("tags",          new ArrayList<>());
        book.put("createdAt",     now());
        books.add(book);
        write(BOOKS_FILE, books);
        return toBookResponse(book);
    }

    /** Admin: aprobar/rechazar libro. */
    public static void updateBookStatus(long bookId, String status) {
        List<Map<String,Object>> books = readBooks();
        books.stream().filter(b -> toLong(b.get("id")) == bookId).findFirst()
                .ifPresent(b -> b.put("status", status));
        write(BOOKS_FILE, books);
    }

    // ══════════════════════════════════════════════════════════════
    // CATEGORÍAS
    // ══════════════════════════════════════════════════════════════

    public static List<Models.CategoryResponse> getCategories() {
        List<Map<String,Object>> cats = readList(CATEGORIES_FILE);
        return cats.stream().map(c -> {
            Models.CategoryResponse r = new Models.CategoryResponse();
            r.id   = toLong(c.get("id"));
            r.name = str(c.get("name"));
            r.slug = str(c.get("slug"));
            r.description = str(c.get("description"));
            return r;
        }).collect(Collectors.toList());
    }

    public static Models.CategoryResponse addCategory(String name, String description) {
        List<Map<String,Object>> cats = readList(CATEGORIES_FILE);
        long newId = cats.stream().mapToLong(c -> toLong(c.get("id"))).max().orElse(0) + 1;
        Map<String,Object> cat = new LinkedHashMap<>();
        cat.put("id",          newId);
        cat.put("name",        name);
        cat.put("slug",        name.toLowerCase().replaceAll("\\s+", "-"));
        cat.put("description", description);
        cats.add(cat);
        write(CATEGORIES_FILE, cats);
        Models.CategoryResponse r = new Models.CategoryResponse();
        r.id = newId; r.name = name; r.slug = str(cat.get("slug")); r.description = description;
        return r;
    }

    // ══════════════════════════════════════════════════════════════
    // CARRITO
    // ══════════════════════════════════════════════════════════════

    public static List<Models.CartItemResponse> getCart(long userId) {
        Map<String,List<Long>> cart = readCart();
        List<Long> bookIds = cart.getOrDefault(String.valueOf(userId), new ArrayList<>());
        return bookIds.stream()
                .map(id -> readBooks().stream().filter(b -> toLong(b.get("id")) == id).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .map(b -> {
                    Models.CartItemResponse ci = new Models.CartItemResponse();
                    ci.id        = toLong(b.get("id"));
                    ci.bookId    = toLong(b.get("id"));
                    ci.bookTitle  = str(b.get("title"));
                    ci.bookAuthor = str(b.get("author"));
                    ci.price      = toDouble(b.get("price"));
                    return ci;
                }).collect(Collectors.toList());
    }

    public static void addToCart(long userId, long bookId) {
        Map<String,List<Long>> cart = readCart();
        String key = String.valueOf(userId);
        cart.computeIfAbsent(key, k -> new ArrayList<>());
        if (!cart.get(key).contains(bookId)) cart.get(key).add(bookId);
        writeMap(CART_FILE, cart);
    }

    public static void removeFromCart(long userId, long bookId) {
        Map<String,List<Long>> cart = readCart();
        String key = String.valueOf(userId);
        if (cart.containsKey(key)) cart.get(key).remove(bookId);
        writeMap(CART_FILE, cart);
    }

    public static void clearCart(long userId) {
        Map<String,List<Long>> cart = readCart();
        cart.put(String.valueOf(userId), new ArrayList<>());
        writeMap(CART_FILE, cart);
    }

    // ══════════════════════════════════════════════════════════════
    // ÓRDENES / CHECKOUT
    // ══════════════════════════════════════════════════════════════

    public static Models.OrderResponse checkout(long userId, String fullName, String email,
                                                 String address, String paymentMethod) {
        List<Models.CartItemResponse> cartItems = getCart(userId);
        if (cartItems.isEmpty()) throw new RuntimeException("El carrito está vacío.");

        List<Map<String,Object>> orders = readList(ORDERS_FILE);
        long newId = orders.stream().mapToLong(o -> toLong(o.get("id"))).max().orElse(0) + 1;

        double total = cartItems.stream().mapToDouble(ci -> ci.price == null ? 0 : ci.price).sum();

        List<Map<String,Object>> items = new ArrayList<>();
        for (var ci : cartItems) {
            Map<String,Object> item = new LinkedHashMap<>();
            item.put("bookId",    ci.bookId);
            item.put("bookTitle", ci.bookTitle);
            item.put("bookAuthor",ci.bookAuthor);
            item.put("price",     ci.price);
            items.add(item);
        }

        Map<String,Object> order = new LinkedHashMap<>();
        order.put("id",            newId);
        order.put("buyerId",       userId);
        order.put("status",        "COMPLETED");
        order.put("paymentMethod", paymentMethod);
        order.put("totalAmount",   total);
        order.put("billingName",   fullName);
        order.put("billingEmail",  email);
        order.put("billingAddress",address);
        order.put("items",         items);
        order.put("createdAt",     now());
        orders.add(order);
        write(ORDERS_FILE, orders);

        // Agregar a biblioteca personal
        addToLibrary(userId, cartItems.stream().map(ci -> ci.bookId).collect(Collectors.toList()));
        clearCart(userId);

        return toOrderResponse(order);
    }

    public static List<Models.OrderResponse> getOrders(long userId) {
        return readList(ORDERS_FILE).stream()
                .filter(o -> toLong(o.get("buyerId")) == userId)
                .sorted((a, b) -> str(b.get("createdAt")).compareTo(str(a.get("createdAt"))))
                .map(LocalDataService::toOrderResponse)
                .collect(Collectors.toList());
    }

    public static List<Models.OrderResponse> getAllOrders() {
        return readList(ORDERS_FILE).stream()
                .sorted((a, b) -> str(b.get("createdAt")).compareTo(str(a.get("createdAt"))))
                .map(LocalDataService::toOrderResponse)
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════
    // BIBLIOTECA PERSONAL
    // ══════════════════════════════════════════════════════════════

    private static void addToLibrary(long userId, List<Long> bookIds) {
        Map<String,List<Long>> lib = readLibrary();
        String key = String.valueOf(userId);
        lib.computeIfAbsent(key, k -> new ArrayList<>());
        for (long id : bookIds) {
            if (!lib.get(key).contains(id)) lib.get(key).add(id);
        }
        writeMap(LIBRARY_FILE, lib);
    }

    public static List<Models.BookResponse> getLibrary(long userId) {
        Map<String,List<Long>> lib = readLibrary();
        List<Long> bookIds = lib.getOrDefault(String.valueOf(userId), new ArrayList<>());
        return bookIds.stream()
                .map(id -> readBooks().stream().filter(b -> toLong(b.get("id")) == id).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .map(LocalDataService::toBookResponse)
                .collect(Collectors.toList());
    }

    public static boolean isInLibrary(long userId, long bookId) {
        Map<String,List<Long>> lib = readLibrary();
        return lib.getOrDefault(String.valueOf(userId), new ArrayList<>()).contains(bookId);
    }

    // ══════════════════════════════════════════════════════════════
    // WISHLIST
    // ══════════════════════════════════════════════════════════════

    public static List<Models.BookResponse> getWishlist(long userId) {
        Map<String,List<Long>> wish = readWishlist();
        List<Long> ids = wish.getOrDefault(String.valueOf(userId), new ArrayList<>());
        return ids.stream()
                .map(id -> readBooks().stream().filter(b -> toLong(b.get("id")) == id).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .map(LocalDataService::toBookResponse)
                .collect(Collectors.toList());
    }

    public static void addToWishlist(long userId, long bookId) {
        Map<String,List<Long>> wish = readWishlist();
        String key = String.valueOf(userId);
        wish.computeIfAbsent(key, k -> new ArrayList<>());
        if (!wish.get(key).contains(bookId)) wish.get(key).add(bookId);
        writeMap(WISHLIST_FILE, wish);
    }

    public static void removeFromWishlist(long userId, long bookId) {
        Map<String,List<Long>> wish = readWishlist();
        String key = String.valueOf(userId);
        if (wish.containsKey(key)) wish.get(key).remove(bookId);
        writeMap(WISHLIST_FILE, wish);
    }

    public static boolean isInWishlist(long userId, long bookId) {
        return readWishlist().getOrDefault(String.valueOf(userId), new ArrayList<>()).contains(bookId);
    }

    // ══════════════════════════════════════════════════════════════
    // RESEÑAS
    // ══════════════════════════════════════════════════════════════

    public static List<Models.ReviewResponse> getReviews(long bookId) {
        return readList(REVIEWS_FILE).stream()
                .filter(r -> toLong(r.get("bookId")) == bookId && Boolean.TRUE.equals(r.get("isVisible")))
                .map(r -> {
                    Models.ReviewResponse rv = new Models.ReviewResponse();
                    rv.id           = toLong(r.get("id"));
                    rv.rating       = (Integer) r.get("rating");
                    rv.title        = str(r.get("title"));
                    rv.body         = str(r.get("body"));
                    rv.reviewerName = str(r.get("reviewerName"));
                    rv.createdAt    = str(r.get("createdAt"));
                    rv.isVisible    = true;
                    return rv;
                }).collect(Collectors.toList());
    }

    public static void addReview(long bookId, long userId, String reviewerName,
                                  int rating, String title, String body) {
        List<Map<String,Object>> reviews = readList(REVIEWS_FILE);
        // Solo una reseña por usuario por libro
        reviews.removeIf(r -> toLong(r.get("bookId")) == bookId && toLong(r.get("userId")) == userId);

        long newId = reviews.stream().mapToLong(r -> toLong(r.get("id"))).max().orElse(0) + 1;
        Map<String,Object> review = new LinkedHashMap<>();
        review.put("id",           newId);
        review.put("bookId",       bookId);
        review.put("userId",       userId);
        review.put("reviewerName", reviewerName);
        review.put("rating",       rating);
        review.put("title",        title);
        review.put("body",         body);
        review.put("isVisible",    true);
        review.put("createdAt",    now());
        reviews.add(review);
        write(REVIEWS_FILE, reviews);

        // Recalcular rating del libro
        List<Map<String,Object>> bookReviews = reviews.stream()
                .filter(r -> toLong(r.get("bookId")) == bookId).collect(Collectors.toList());
        double avg = bookReviews.stream().mapToInt(r -> (Integer) r.get("rating")).average().orElse(0);
        List<Map<String,Object>> books = readBooks();
        books.stream().filter(b -> toLong(b.get("id")) == bookId).findFirst().ifPresent(b -> {
            b.put("averageRating", Math.round(avg * 100.0) / 100.0);
            b.put("reviewCount",   bookReviews.size());
        });
        write(BOOKS_FILE, books);
    }

    // ══════════════════════════════════════════════════════════════
    // ADMIN
    // ══════════════════════════════════════════════════════════════

    public static Models.DashboardResponse getDashboard() {
        List<Map<String,Object>> users   = readUsers();
        List<Map<String,Object>> books   = readBooks();
        List<Map<String,Object>> orders  = readList(ORDERS_FILE);
        List<Map<String,Object>> reviews = readList(REVIEWS_FILE);

        Models.DashboardResponse dash = new Models.DashboardResponse();
        dash.totalUsers     = (long) users.size();
        dash.totalBooks     = (long) books.size();
        dash.totalOrders    = (long) orders.size();
        dash.totalDownloads = 0L;
        dash.pendingBooks   = books.stream().filter(b -> "PENDING".equals(b.get("status"))).count();
        dash.topDownloaded  = books.stream()
                .filter(b -> "APPROVED".equals(b.get("status")))
                .sorted((a, b) -> Long.compare(toLong(b.get("downloadCount")), toLong(a.get("downloadCount"))))
                .limit(5)
                .map(LocalDataService::toBookResponse)
                .collect(Collectors.toList());
        return dash;
    }

    public static List<Models.UserResponse> getAllUsers() {
        return readUsers().stream().map(u -> {
            Models.UserResponse r = new Models.UserResponse();
            r.id         = toLong(u.get("id"));
            r.email      = str(u.get("email"));
            r.username   = str(u.get("username"));
            r.fullName   = str(u.get("fullName"));
            r.role       = str(u.get("role"));
            r.isActive   = (Boolean) u.getOrDefault("isActive", true);
            r.isVerified = (Boolean) u.getOrDefault("isVerified", true);
            return r;
        }).collect(Collectors.toList());
    }

    public static List<Models.BookResponse> getPendingBooks() {
        return readBooks().stream()
                .filter(b -> "PENDING".equals(b.get("status")))
                .map(LocalDataService::toBookResponse)
                .collect(Collectors.toList());
    }

    public static void toggleUserActive(long userId) {
        List<Map<String,Object>> users = readUsers();
        users.stream().filter(u -> toLong(u.get("id")) == userId).findFirst().ifPresent(u -> {
            boolean cur = (Boolean) u.getOrDefault("isActive", true);
            u.put("isActive", !cur);
        });
        write(USERS_FILE, users);
    }

    // ══════════════════════════════════════════════════════════════
    // SEED DATA
    // ══════════════════════════════════════════════════════════════

    private static void seedUsers() {
        List<Map<String,Object>> users = new ArrayList<>();

        users.add(makeUser(1L, "admin@openlib.com",  "admin",       "Admin1234!",  "Administrador OpenLib", "ADMIN"));
        users.add(makeUser(2L, "seller@openlib.com", "demo_seller", "Seller1234!", "Seller Demo",           "SELLER"));
        users.add(makeUser(3L, "buyer@openlib.com",  "demo_buyer",  "Buyer1234!",  "Buyer Demo",            "BUYER"));

        write(USERS_FILE, users);
    }

    private static Map<String,Object> makeUser(long id, String email, String username,
                                                String password, String fullName, String role) {
        Map<String,Object> u = new LinkedHashMap<>();
        u.put("id",           id);
        u.put("email",        email);
        u.put("username",     username);
        u.put("passwordHash", hashPassword(password));
        u.put("fullName",     fullName);
        u.put("role",         role);
        u.put("isActive",     true);
        u.put("isVerified",   true);
        u.put("createdAt",    now());
        return u;
    }

    private static void seedCategories() {
        List<Map<String,Object>> cats = new ArrayList<>();
        Object[][] data = {
            {1L, "Programación",        "programacion",        "Libros sobre lenguajes, algoritmos y patrones de diseño"},
            {2L, "Ciencias de Datos",   "ciencias-de-datos",   "Machine learning, estadística y análisis de datos"},
            {3L, "Matemáticas",         "matematicas",          "Álgebra, cálculo, estadística y matemática discreta"},
            {4L, "Ingeniería",          "ingenieria",           "Libros de ingeniería de software y sistemas"},
            {5L, "Bases de Datos",      "bases-de-datos",       "SQL, NoSQL, modelado y administración de BD"},
            {6L, "Redes y Seguridad",   "redes-y-seguridad",    "Redes de computadoras, ciberseguridad y criptografía"},
            {7L, "Sistemas Operativos", "sistemas-operativos",  "Linux, Windows, kernels y administración de sistemas"},
        };
        for (var d : data) {
            Map<String,Object> c = new LinkedHashMap<>();
            c.put("id", d[0]); c.put("name", d[1]); c.put("slug", d[2]); c.put("description", d[3]);
            cats.add(c);
        }
        write(CATEGORIES_FILE, cats);
    }

    private static void seedBooks() {
        List<Map<String,Object>> books = new ArrayList<>();

        books.add(makeBook(1L, "Clean Code: A Handbook of Agile Software Craftsmanship",
                "Robert C. Martin", "9780132350884",
                "Guía definitiva para escribir código limpio, mantenible y profesional.",
                "Programación", 0.0, "en", 2008, 2L, "Seller Demo", 4.80, 42));

        books.add(makeBook(2L, "Designing Data-Intensive Applications",
                "Martin Kleppmann", "9781491903124",
                "El libro definitivo para entender sistemas distribuidos y arquitecturas modernas de datos.",
                "Ciencias de Datos", 0.0, "en", 2017, 2L, "Seller Demo", 4.90, 78));

        books.add(makeBook(3L, "Spring Boot en Acción",
                "Craig Walls", "9781617292545",
                "Aprende a construir aplicaciones Spring Boot desde cero hasta producción.",
                "Programación", 0.0, "es", 2022, 2L, "Seller Demo", 4.70, 35));

        books.add(makeBook(4L, "The Pragmatic Programmer",
                "David Thomas, Andrew Hunt", "9780135957059",
                "Tu viaje hacia la maestría en el desarrollo de software.",
                "Programación", 0.0, "en", 2019, 2L, "Seller Demo", 4.85, 91));

        books.add(makeBook(5L, "Introduction to Algorithms",
                "Thomas H. Cormen", "9780262033848",
                "El libro de referencia más completo sobre algoritmos y estructuras de datos.",
                "Matemáticas", 0.0, "en", 2022, 2L, "Seller Demo", 4.60, 120));

        write(BOOKS_FILE, books);
    }

    private static Map<String,Object> makeBook(long id, String title, String author, String isbn,
                                                String description, String category, double price,
                                                String language, int year, long sellerId, String sellerName,
                                                double rating, int reviewCount) {
        Map<String,Object> b = new LinkedHashMap<>();
        b.put("id",            id);
        b.put("title",         title);
        b.put("author",        author);
        b.put("isbn",          isbn);
        b.put("description",   description);
        b.put("category",      category);
        b.put("price",         price);
        b.put("language",      language);
        b.put("publishedYear", year);
        b.put("status",        "APPROVED");
        b.put("sellerId",      sellerId);
        b.put("sellerName",    sellerName);
        b.put("averageRating", rating);
        b.put("reviewCount",   reviewCount);
        b.put("downloadCount", 0L);
        b.put("tags",          List.of());
        b.put("createdAt",     now());
        return b;
    }

    // ══════════════════════════════════════════════════════════════
    // I/O HELPERS
    // ══════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private static List<Map<String,Object>> readUsers() {
        return (List<Map<String,Object>>) (List<?>) readList(USERS_FILE);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String,Object>> readBooks() {
        return (List<Map<String,Object>>) (List<?>) readList(BOOKS_FILE);
    }

    @SuppressWarnings("unchecked")
    private static Map<String,List<Long>> readCart() { return readMapOfLists(CART_FILE); }

    @SuppressWarnings("unchecked")
    private static Map<String,List<Long>> readLibrary() { return readMapOfLists(LIBRARY_FILE); }

    @SuppressWarnings("unchecked")
    private static Map<String,List<Long>> readWishlist() { return readMapOfLists(WISHLIST_FILE); }

    private static List<Map<String,Object>> readList(Path path) {
        try {
            if (!Files.exists(path)) return new ArrayList<>();
            return JSON.readValue(path.toFile(), new TypeReference<>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String,List<Long>> readMapOfLists(Path path) {
        try {
            if (!Files.exists(path)) return new HashMap<>();
            Map<String,Object> raw = JSON.readValue(path.toFile(), new TypeReference<>() {});
            Map<String,List<Long>> result = new HashMap<>();
            for (var entry : raw.entrySet()) {
                List<Integer> ints = (List<Integer>) entry.getValue();
                result.put(entry.getKey(), ints.stream().map(Long::valueOf).collect(Collectors.toList()));
            }
            return result;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private static void write(Path path, Object data) {
        try {
            JSON.writeValue(path.toFile(), data);
        } catch (Exception e) {
            throw new RuntimeException("Error escribiendo " + path + ": " + e.getMessage(), e);
        }
    }

    private static void writeMap(Path path, Map<String,List<Long>> data) {
        write(path, data);
    }

    // ══════════════════════════════════════════════════════════════
    // CONVERSORES
    // ══════════════════════════════════════════════════════════════

    private static Models.BookResponse toBookResponse(Map<String,Object> b) {
        Models.BookResponse r = new Models.BookResponse();
        r.id            = toLong(b.get("id"));
        r.title         = str(b.get("title"));
        r.author        = str(b.get("author"));
        r.isbn          = str(b.get("isbn"));
        r.description   = str(b.get("description"));
        r.language      = str(b.get("language"));
        r.publishedYear = b.get("publishedYear") != null ? (Integer) b.get("publishedYear") : null;
        r.price         = toDouble(b.get("price"));
        r.status        = str(b.get("status"));
        r.averageRating = toDouble(b.get("averageRating"));
        r.reviewCount   = b.get("reviewCount") != null ? (Integer) b.get("reviewCount") : 0;
        r.downloadCount = (int) toLong(b.get("downloadCount"));
        r.category      = str(b.get("category"));
        r.sellerName    = str(b.get("sellerName"));
        Object tags = b.get("tags");
        r.tags = tags instanceof List<?> ?
                ((List<?>) tags).stream().map(Object::toString).collect(Collectors.toList()) :
                new ArrayList<>();
        return r;
    }

    @SuppressWarnings("unchecked")
    private static Models.OrderResponse toOrderResponse(Map<String,Object> o) {
        Models.OrderResponse r = new Models.OrderResponse();
        r.id            = toLong(o.get("id"));
        r.status        = str(o.get("status"));
        r.paymentMethod = str(o.get("paymentMethod"));
        r.totalAmount   = toDouble(o.get("totalAmount"));
        r.billingName   = str(o.get("billingName"));
        r.billingEmail  = str(o.get("billingEmail"));
        r.billingAddress= str(o.get("billingAddress"));
        r.createdAt     = str(o.get("createdAt"));
        List<Map<String,Object>> items = (List<Map<String,Object>>) o.getOrDefault("items", new ArrayList<>());
        r.items = items.stream().map(i -> {
            Models.OrderItemResponse oi = new Models.OrderItemResponse();
            oi.bookId     = toLong(i.get("bookId"));
            oi.bookTitle  = str(i.get("bookTitle"));
            oi.bookAuthor = str(i.get("bookAuthor"));
            oi.price      = toDouble(i.get("price"));
            return oi;
        }).collect(Collectors.toList());
        return r;
    }

    // ══════════════════════════════════════════════════════════════
    // CRYPTO / UTILIDADES
    // ══════════════════════════════════════════════════════════════

    /**
     * Hash SHA-256 con salt fijo para persistencia simple.
     * No usa BCrypt para no requerir dependencias extra en el cliente.
     */
    static String hashPassword(String password) {
        try {
            // Salt fijo + password → SHA-256, guardado con prefijo para identificarlo
            String salted = "openlib-salt-2024:" + password;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(salted.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder("{sha256}");
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al hashear contraseña", e);
        }
    }

    static boolean checkPassword(String rawPassword, String storedHash) {
        if (storedHash == null) return false;
        return hashPassword(rawPassword).equals(storedHash);
    }

    private static long   toLong(Object v)   { if (v == null) return 0L; if (v instanceof Long l) return l; if (v instanceof Integer i) return i.longValue(); return Long.parseLong(v.toString()); }
    private static double toDouble(Object v) { if (v == null) return 0.0; if (v instanceof Double d) return d; if (v instanceof Integer i) return i.doubleValue(); if (v instanceof Long l) return l.doubleValue(); return Double.parseDouble(v.toString()); }
    private static String str(Object v)      { return v == null ? "" : v.toString(); }
    private static boolean contains(Map<String,Object> m, String key, String q) { return str(m.get(key)).toLowerCase().contains(q); }
    private static String now()              { return LocalDateTime.now().format(DT); }
}
