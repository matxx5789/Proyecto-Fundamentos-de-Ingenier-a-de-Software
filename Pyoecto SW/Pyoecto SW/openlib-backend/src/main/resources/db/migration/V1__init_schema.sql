-- =========================================================
-- V1: Esquema inicial OpenLib Market
-- =========================================================

-- ── Enum tipos de rol ──
CREATE TYPE user_role AS ENUM ('BUYER', 'SELLER', 'ADMIN');

-- ── Enum estado de libro ──
CREATE TYPE book_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'ARCHIVED');

-- ── Enum estado de orden ──
CREATE TYPE order_status AS ENUM ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED');

-- ── Enum método de pago ──
CREATE TYPE payment_method AS ENUM ('FREE', 'DONATION', 'CREDIT_CARD', 'PAYPAL');

-- =========================================================
-- USUARIOS
-- =========================================================
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    username      VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255),
    role          user_role    NOT NULL DEFAULT 'BUYER',
    avatar_url    VARCHAR(500),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    is_verified   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email    ON users (email);
CREATE INDEX idx_users_role     ON users (role);
CREATE INDEX idx_users_username ON users (username);

-- =========================================================
-- CATEGORÍAS
-- =========================================================
CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    slug        VARCHAR(120) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- =========================================================
-- ETIQUETAS
-- =========================================================
CREATE TABLE tags (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE
);

-- =========================================================
-- LIBROS
-- =========================================================
CREATE TABLE books (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(300)   NOT NULL,
    subtitle        VARCHAR(300),
    author          VARCHAR(255)   NOT NULL,
    isbn            VARCHAR(20)    UNIQUE,
    description     TEXT,
    cover_url       VARCHAR(500),
    file_path       VARCHAR(500),          -- ruta local al PDF/EPUB
    file_size_bytes BIGINT,
    pages           INTEGER,
    language        VARCHAR(10)    DEFAULT 'es',
    published_year  INTEGER,
    price           NUMERIC(10,2)  NOT NULL DEFAULT 0.00,
    status          book_status    NOT NULL DEFAULT 'PENDING',
    category_id     BIGINT         REFERENCES categories(id) ON DELETE SET NULL,
    seller_id       BIGINT         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    download_count  BIGINT         NOT NULL DEFAULT 0,
    average_rating  NUMERIC(3,2)   DEFAULT 0.00,
    review_count    INTEGER        NOT NULL DEFAULT 0,
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_books_status      ON books (status);
CREATE INDEX idx_books_category    ON books (category_id);
CREATE INDEX idx_books_seller      ON books (seller_id);
CREATE INDEX idx_books_title       ON books USING gin(to_tsvector('spanish', title));
CREATE INDEX idx_books_author      ON books (author);
CREATE INDEX idx_books_isbn        ON books (isbn);

-- ── Relación libros ↔ etiquetas ──
CREATE TABLE book_tags (
    book_id BIGINT NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    tag_id  BIGINT NOT NULL REFERENCES tags(id)  ON DELETE CASCADE,
    PRIMARY KEY (book_id, tag_id)
);

-- =========================================================
-- CARRITO
-- =========================================================
CREATE TABLE cart_items (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id    BIGINT    NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    added_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, book_id)
);

CREATE INDEX idx_cart_user ON cart_items (user_id);

-- =========================================================
-- ÓRDENES (checkout)
-- =========================================================
CREATE TABLE orders (
    id                BIGSERIAL PRIMARY KEY,
    buyer_id          BIGINT         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status            order_status   NOT NULL DEFAULT 'PENDING',
    payment_method    payment_method NOT NULL DEFAULT 'FREE',
    total_amount      NUMERIC(10,2)  NOT NULL DEFAULT 0.00,
    billing_full_name VARCHAR(255),
    billing_email     VARCHAR(255),
    billing_address   VARCHAR(500),
    billing_city      VARCHAR(100),
    billing_country   VARCHAR(100),
    notes             TEXT,
    created_at        TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_buyer  ON orders (buyer_id);
CREATE INDEX idx_orders_status ON orders (status);

-- ── Líneas de orden ──
CREATE TABLE order_items (
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT        NOT NULL REFERENCES orders(id)  ON DELETE CASCADE,
    book_id    BIGINT        NOT NULL REFERENCES books(id)   ON DELETE RESTRICT,
    price      NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    UNIQUE (order_id, book_id)
);

CREATE INDEX idx_order_items_order ON order_items (order_id);

-- =========================================================
-- BIBLIOTECA PERSONAL + DESCARGAS
-- =========================================================
CREATE TABLE downloads (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id         BIGINT       NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    order_item_id   BIGINT       REFERENCES order_items(id)   ON DELETE SET NULL,
    signed_token    VARCHAR(500) UNIQUE,              -- token temporal de descarga
    token_expires   TIMESTAMP,
    downloaded_at   TIMESTAMP,
    ip_address      VARCHAR(45),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_downloads_user      ON downloads (user_id);
CREATE INDEX idx_downloads_book      ON downloads (book_id);
CREATE INDEX idx_downloads_token     ON downloads (signed_token);

-- =========================================================
-- LISTA DE FAVORITOS (WISHLIST)
-- =========================================================
CREATE TABLE wishlists (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id    BIGINT    NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    added_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, book_id)
);

CREATE INDEX idx_wishlist_user ON wishlists (user_id);

-- =========================================================
-- RESEÑAS Y CALIFICACIONES
-- =========================================================
CREATE TABLE reviews (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id    BIGINT    NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    rating     SMALLINT  NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title      VARCHAR(200),
    body       TEXT,
    is_visible BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, book_id)
);

CREATE INDEX idx_reviews_book ON reviews (book_id);
CREATE INDEX idx_reviews_user ON reviews (user_id);
