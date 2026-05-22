-- =========================================================
-- V2: Datos semilla (seed data) para desarrollo
-- =========================================================

-- ── Admin por defecto (password: Admin1234!) ──
INSERT INTO users (email, username, password_hash, full_name, role, is_active, is_verified)
VALUES (
    'admin@openlib.com',
    'admin',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewLx.b/V9hJ5Kqeq',
    'Administrador OpenLib',
    'ADMIN',
    TRUE,
    TRUE
);

-- ── Seller demo (password: Seller1234!) ──
INSERT INTO users (email, username, password_hash, full_name, role, is_active, is_verified)
VALUES (
    'seller@openlib.com',
    'demo_seller',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewLx.b/V9hJ5Kqeq',
    'Seller Demo',
    'SELLER',
    TRUE,
    TRUE
);

-- ── Buyer demo (password: Buyer1234!) ──
INSERT INTO users (email, username, password_hash, full_name, role, is_active, is_verified)
VALUES (
    'buyer@openlib.com',
    'demo_buyer',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewLx.b/V9hJ5Kqeq',
    'Buyer Demo',
    'BUYER',
    TRUE,
    TRUE
);

-- ── Categorías ──
INSERT INTO categories (name, slug, description) VALUES
    ('Programación',        'programacion',        'Libros sobre lenguajes, algoritmos y patrones de diseño'),
    ('Ciencias de Datos',   'ciencias-de-datos',   'Machine learning, estadística y análisis de datos'),
    ('Matemáticas',         'matematicas',          'Álgebra, cálculo, estadística y matemática discreta'),
    ('Ingeniería',          'ingenieria',           'Libros de ingeniería de software y sistemas'),
    ('Bases de Datos',      'bases-de-datos',       'SQL, NoSQL, modelado y administración de BD'),
    ('Redes y Seguridad',   'redes-y-seguridad',    'Redes de computadoras, ciberseguridad y criptografía'),
    ('Sistemas Operativos', 'sistemas-operativos',  'Linux, Windows, kernels y administración de sistemas');

-- ── Etiquetas ──
INSERT INTO tags (name, slug) VALUES
    ('Java',       'java'),
    ('Python',     'python'),
    ('JavaScript', 'javascript'),
    ('Spring',     'spring'),
    ('SQL',        'sql'),
    ('NoSQL',      'nosql'),
    ('Docker',     'docker'),
    ('Git',        'git'),
    ('Open Source','open-source'),
    ('Gratuito',   'gratuito'),
    ('Académico',  'academico');

-- ── Libros de muestra (aprobados) ──
INSERT INTO books (title, author, isbn, description, language, published_year, price, status, category_id, seller_id, average_rating, review_count)
VALUES
(
    'Clean Code: A Handbook of Agile Software Craftsmanship',
    'Robert C. Martin',
    '9780132350884',
    'Guía definitiva para escribir código limpio, mantenible y profesional. Aprende las mejores prácticas y principios que todo desarrollador debe conocer.',
    'en', 2008, 0.00, 'APPROVED',
    (SELECT id FROM categories WHERE slug = 'programacion'),
    (SELECT id FROM users WHERE username = 'demo_seller'),
    4.80, 42
),
(
    'Designing Data-Intensive Applications',
    'Martin Kleppmann',
    '9781491903124',
    'El libro definitivo para entender sistemas distribuidos, bases de datos y arquitecturas modernas de datos.',
    'en', 2017, 0.00, 'APPROVED',
    (SELECT id FROM categories WHERE slug = 'ciencias-de-datos'),
    (SELECT id FROM users WHERE username = 'demo_seller'),
    4.90, 78
),
(
    'Spring Boot en Acción',
    'Craig Walls',
    '9781617292545',
    'Aprende a construir aplicaciones Spring Boot desde cero hasta producción. Incluye ejemplos prácticos con microservicios.',
    'es', 2022, 0.00, 'APPROVED',
    (SELECT id FROM categories WHERE slug = 'programacion'),
    (SELECT id FROM users WHERE username = 'demo_seller'),
    4.70, 35
),
(
    'The Pragmatic Programmer',
    'David Thomas, Andrew Hunt',
    '9780135957059',
    'Tu viaje hacia la maestría en el desarrollo de software. Un clásico actualizado con nuevas prácticas para el desarrollo moderno.',
    'en', 2019, 0.00, 'APPROVED',
    (SELECT id FROM categories WHERE slug = 'programacion'),
    (SELECT id FROM users WHERE username = 'demo_seller'),
    4.85, 91
),
(
    'Introduction to Algorithms',
    'Thomas H. Cormen',
    '9780262033848',
    'El libro de referencia más completo sobre algoritmos y estructuras de datos. Esencial para cualquier ingeniero en sistemas.',
    'en', 2022, 0.00, 'APPROVED',
    (SELECT id FROM categories WHERE slug = 'matematicas'),
    (SELECT id FROM users WHERE username = 'demo_seller'),
    4.60, 120
);

-- ── Tags para libros ──
INSERT INTO book_tags (book_id, tag_id)
SELECT b.id, t.id FROM books b, tags t
WHERE b.isbn = '9780132350884' AND t.name IN ('Java', 'Open Source', 'Académico');

INSERT INTO book_tags (book_id, tag_id)
SELECT b.id, t.id FROM books b, tags t
WHERE b.isbn = '9781491903124' AND t.name IN ('NoSQL', 'SQL', 'Docker', 'Académico');

INSERT INTO book_tags (book_id, tag_id)
SELECT b.id, t.id FROM books b, tags t
WHERE b.isbn = '9781617292545' AND t.name IN ('Java', 'Spring', 'Docker');

INSERT INTO book_tags (book_id, tag_id)
SELECT b.id, t.id FROM books b, tags t
WHERE b.isbn = '9780135957059' AND t.name IN ('Open Source', 'Git', 'Académico');

INSERT INTO book_tags (book_id, tag_id)
SELECT b.id, t.id FROM books b, tags t
WHERE b.isbn = '9780262033848' AND t.name IN ('Académico', 'Gratuito');
