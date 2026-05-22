# 📚 OpenLib Market

> **Sistema de Gestión de Activos Digitales Académicos**  
> Proyecto de Fundamentos de Ingeniería de Software — Pontificia Universidad Javeriana

Una plataforma transaccional de libros digitales open-source que simula un e-commerce completo (al estilo Amazon/Shopify), con trazabilidad de descargas, control de acceso por roles y motor de descargas seguro mediante Signed URLs.

---

## 🧩 Problema que resuelve

Las instituciones educativas distribuyen sus materiales mediante listas de enlaces estáticos o carpetas compartidas, lo que genera pérdida de métricas, falta de seguridad y una experiencia de usuario pobre. OpenLib Market profesionaliza esa experiencia.

---

## 🏗️ Arquitectura

El proyecto tiene dos implementaciones paralelas:

### Backend objetivo (Spring Boot + PostgreSQL + Redis)

```
REST API (:8080/api)
├── controller/   → 9 controllers (Auth, Book, Cart, Order, Library, Wishlist, Review, Seller, Admin)
├── service/      → Lógica de negocio
├── repository/   → Spring Data JPA + JPQL
├── security/     → JWT Provider + Filter
└── util/         → SignedUrlGenerator (HMAC-SHA256)

Infraestructura:
  PostgreSQL 17 — datos transaccionales
  Redis 7       — refresh tokens + caché de catálogo
```

### MVP funcional (JavaFX + JSON local) — Entrega 1

```
Aplicación de escritorio JavaFX
├── controller/         → Login, Catalog, Cart, Checkout, Library, Admin, Seller, Wishlist
├── service/            → ApiClient (router) → LocalDataService
└── data/ (persistencia local)
      users.json · books.json · categories.json · cart.json
      orders.json · library.json · wishlist.json · reviews.json
```

> El backend Spring Boot se mantiene como referencia arquitectónica y será activado en la Entrega 2.

---

## ✅ Funcionalidades implementadas (Entrega 1)

| Módulo | Funcionalidad | Estado |
|--------|--------------|--------|
| **Auth** | Registro y login con roles (Buyer / Seller / Admin) | ✅ |
| **Catálogo** | Búsqueda por título, autor, ISBN, categoría y etiquetas | ✅ |
| **Carrito** | Agregar, ver y eliminar ítems con persistencia | ✅ |
| **Checkout** | Flujo completo: facturación → pago simulado → confirmación | ✅ |
| **Biblioteca** | Libros adquiridos + descarga con Signed URL (HMAC-SHA256) | ✅ |
| **Seller** | Publicar, editar y archivar libros | ✅ |
| **Admin** | Dashboard de métricas, aprobación de libros, gestión de usuarios/categorías | ✅ |
| **Reseñas** | Crear reseña (solo si adquirió el libro), moderación admin | ✅ |
| **Favoritos** | Agregar y quitar de lista de deseos | ✅ |

---

## 🛠️ Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Backend (objetivo) | Java 21 · Spring Boot 3.4 · Spring Security · Spring Data JPA |
| Frontend / MVP | JavaFX |
| Base de datos | PostgreSQL 17 |
| Caché / Sesiones | Redis 7 |
| Almacenamiento | Local (archivos PDF/EPUB) |
| Documentación API | SpringDoc / Swagger UI |
| Build | Maven |
| Contenedores | Docker / Docker Compose |

---

## 🚀 Inicio rápido (Backend Spring Boot)

### 1. Levantar infraestructura

```bash
cd openlib-backend
docker-compose up -d
```

| Servicio | URL | Credenciales |
|----------|-----|-------------|
| PostgreSQL | `localhost:5432` | `openlib_user / openlib_pass` |
| Redis | `localhost:6379` | — |
| pgAdmin | `http://localhost:5050` | `admin@openlib.com / admin123` |

### 2. Ejecutar la API

```bash
mvn spring-boot:run
```

### 3. Documentación interactiva

```
http://localhost:8080/api/swagger-ui.html
```

---

## 👥 Usuarios de prueba (seed data)

| Email | Contraseña | Rol |
|-------|-----------|-----|
| admin@openlib.com | Admin1234! | ADMIN |
| seller@openlib.com | Seller1234! | SELLER |
| buyer@openlib.com | Buyer1234! | BUYER |

---

## 📡 API Reference

### Autenticación

```http
POST /api/auth/register     # Registrar usuario (BUYER / SELLER)
POST /api/auth/login        # Login → JWT access + refresh token
POST /api/auth/refresh      # Renovar access token
```

### Catálogo (público)

```http
GET  /api/books                         # Buscar libros (q, categoryId, page, size, sort)
GET  /api/books/{id}                    # Detalle de libro
GET  /api/books/recommendations         # Recomendaciones (requiere JWT)
GET  /api/books/{bookId}/reviews        # Reseñas de un libro
GET  /api/books/categories              # Listado de categorías
GET  /api/books/tags                    # Listado de etiquetas
```

### Buyer (requiere JWT)

```http
GET/POST/DELETE  /api/cart
POST             /api/orders
GET              /api/orders
GET              /api/library
GET              /api/library/{id}/download-url    # Signed URL (5 min)
GET              /api/library/download?token=...   # Descarga (1 uso)
GET/POST/DELETE  /api/wishlist
POST             /api/books/{id}/reviews
```

### Seller (requiere JWT SELLER)

```http
GET/POST/PUT/DELETE  /api/seller/books
```

### Admin (requiere JWT ADMIN)

```http
GET      /api/admin/dashboard
GET/PUT  /api/admin/users
GET/PUT  /api/admin/books
DELETE   /api/admin/reviews/{id}
POST/PUT/DELETE  /api/admin/categories
POST/DELETE      /api/admin/tags
```

---

## 🔐 Seguridad

- **JWT Bearer Token** en header `Authorization: Bearer <token>`
- Access token: 15 minutos
- Refresh token: 7 días (almacenado en Redis)
- Signed URLs de descarga: 5 minutos, uso único (HMAC-SHA256)

---

## 📁 Estructura del repositorio

```
openlib-backend/
├── docker-compose.yml
├── pom.xml
└── src/main/java/com/openlib/
    ├── config/          # Spring Security, OpenAPI
    ├── controller/      # 8 REST Controllers
    ├── domain/          # Entidades JPA + Enums
    ├── dto/
    │   ├── request/
    │   └── response/
    ├── exception/       # GlobalExceptionHandler (RFC 7807)
    ├── repository/      # Spring Data JPA
    ├── security/        # JWT Provider + Filter + UserDetails
    ├── service/         # Lógica de negocio
    └── util/            # SignedUrlGenerator
```

---

## 🗓️ Plan de entregas

| Entrega | Sprint | Contenido | Estado |
|---------|--------|-----------|--------|
| **Entrega 1** | Sprint 1-2 | Backlog, arquitectura, MVP funcional (JavaFX + JSON) | ✅ Completa |
| **Entrega 2** | Sprint 3-4 | Funcionalidades avanzadas Buyer / Seller / Admin con Spring Boot activo | ⏳ |
| **Entrega 3** | Sprint 4 | Métricas, reportes y pulido final | ⏳ |

---

## 🧪 Tests

```bash
mvn test
```

---

## 📋 Atributos de calidad

- **Escalabilidad:** Arquitectura preparada para picos de tráfico (inicio de semestre)
- **Seguridad:** Encriptación de datos sensibles + tokens de tiempo limitado
- **Performance:** Carga de catálogo objetivo < 1.5 s (con caché Redis)
