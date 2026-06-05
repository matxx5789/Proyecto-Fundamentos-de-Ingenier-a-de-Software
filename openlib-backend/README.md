# OpenLib Market — Backend

> Sistema de Gestión de Activos Digitales Académicos  
> **MVP — Entrega 1** | Java 25 · Spring Boot 3.4 · PostgreSQL 17 · Redis 7

---

## 🚀 Inicio rápido

### 1. Levantar infraestructura (Docker)

```bash
cd openlib-backend
docker-compose up -d
```

Servicios disponibles:

| Servicio   | URL                           | Credenciales                          |
|------------|-------------------------------|---------------------------------------|
| PostgreSQL | `localhost:5432`              | `openlib_user / openlib_pass`         |
| Redis      | `localhost:6379`              | —                                     |
| pgAdmin    | http://localhost:5050         | `admin@openlib.com / admin123`        |

### 2. Ejecutar la API

```bash
mvn spring-boot:run
```

### 3. Documentación interactiva (Swagger UI)

```
http://localhost:8080/api/swagger-ui.html
```

---

## 👥 Usuarios de prueba (seed data)

| Email                 | Password     | Rol    |
|-----------------------|--------------|--------|
| admin@openlib.com     | Admin1234!   | ADMIN  |
| seller@openlib.com    | Seller1234!  | SELLER |
| buyer@openlib.com     | Buyer1234!   | BUYER  |

---

## 📡 Endpoints principales

### Autenticación

```http
POST /api/auth/register     Registrar usuario (BUYER / SELLER)
POST /api/auth/login        Login → JWT access + refresh token
POST /api/auth/refresh      Renovar access token
```

### Catálogo (público)

```http
GET  /api/books                          Buscar libros (q, categoryId, page, size, sort)
GET  /api/books/{id}                     Detalle de libro
GET  /api/books/recommendations          Recomendaciones (requiere JWT)
GET  /api/books/{bookId}/reviews         Reseñas de un libro
GET  /api/books/categories               Listado categorías
GET  /api/books/tags                     Listado etiquetas
```

### Buyer (requiere JWT)

```http
GET/POST/DELETE  /api/cart               Carrito de compras
POST             /api/orders             Checkout
GET              /api/orders             Historial de órdenes
GET              /api/library            Biblioteca personal
GET              /api/library/{id}/download-url   Signed URL (válida 5 min)
GET              /api/library/download?token=...   Descarga real (1 uso)
GET/POST/DELETE  /api/wishlist           Favoritos
POST             /api/books/{id}/reviews Publicar reseña
```

### Seller (requiere JWT SELLER)

```http
GET/POST/PUT/DELETE  /api/seller/books   CRUD de publicaciones
```

### Admin (requiere JWT ADMIN)

```http
GET  /api/admin/dashboard                Métricas
GET/PUT  /api/admin/users                Gestión de usuarios
GET/PUT  /api/admin/books                Aprobar / rechazar libros
DELETE   /api/admin/reviews/{id}         Moderar reseñas
POST/PUT/DELETE  /api/admin/categories   CRUD categorías
POST/DELETE      /api/admin/tags         CRUD etiquetas
```

---

## 🔐 Autenticación

Usa **JWT Bearer Token** en el header:

```http
Authorization: Bearer <access_token>
```

- **Access token**: 15 minutos  
- **Refresh token**: 7 días (almacenado en Redis)
- **Signed URLs de descarga**: 5 minutos, uso único (HMAC-SHA256)

---

## 🏗️ Estructura del proyecto

```
src/main/java/com/openlib/
├── config/          Spring Security, OpenAPI
├── controller/      REST Controllers (8 controllers)
├── domain/          Entidades JPA + Enums
├── dto/             Records Request/Response
│   ├── request/
│   └── response/
├── exception/       Manejo global de errores (RFC 7807)
├── repository/      Spring Data JPA
├── security/        JWT Provider + Filter + UserDetails
├── service/         Lógica de negocio (6 services)
└── util/            SignedUrlGenerator
```

---

## 🧪 Ejecutar tests

```bash
mvn test
```

---

## 📦 Variables de configuración (`application.yml`)

| Propiedad | Descripción |
|---|---|
| `openlib.jwt.secret` | Clave HMAC para firmar JWTs |
| `openlib.jwt.access-token-expiration` | Expiración access token (ms) |
| `openlib.jwt.refresh-token-expiration` | Expiración refresh token (ms) |
| `openlib.signed-url.secret` | Clave para signed URLs de descarga |
| `openlib.signed-url.expiration-minutes` | Tiempo de vida del link de descarga |
| `openlib.storage.base-path` | Ruta local de archivos PDF/EPUB |

---

## 📋 Entregables

| Entrega | Estado | Descripción |
|---------|--------|-------------|
| **Entrega 1** | ✅ | Backlog, arquitectura, MVP completo |
| Entrega 2 | ⏳ | Funcionalidades avanzadas Buyer/Seller/Admin |
| Entrega 3 | ⏳ | Métricas y reportes |
