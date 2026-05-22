# 🚀 Plan de Rollout — OpenLib Market

**Proyecto:** OpenLib Market  
**Metodología:** Scrum (sprints de 2 semanas)  
**Duración total estimada:** 8 semanas (4 sprints)  
**Fecha de inicio:** Mayo 2026

---

## Visión General de Sprints

```
Sprint 1 ──────── Sprint 2 ──────── Sprint 3 ──────── Sprint 4
Sem 1-2            Sem 3-4            Sem 5-6            Sem 7-8
Infraestructura    Buyer Core         Seller + Admin     Pulido + Entrega
+ Auth + Catálogo  Checkout + Lib.    Dashboard + Tests  Docs + Métricas
```

---

## Sprint 1 — Fundación e Identidad (Sem 1-2)

**Objetivo:** El sistema puede registrar usuarios, autenticarlos y mostrar el catálogo básico.

### Tareas

| # | Tarea | HU Asociadas | Responsable | Estado |
|---|-------|-------------|-------------|--------|
| T-01 | Configurar repositorio Git + estructura Maven | — | Backend | ✅ |
| T-02 | `docker-compose.yml` PostgreSQL + Redis | US-39 | Backend | ✅ |
| T-03 | Migración V1: esquema completo de BD | US-39 | Backend | ✅ |
| T-04 | Migración V2: datos semilla (usuarios, libros demo) | US-39 | Backend | ✅ |
| T-05 | Entidades JPA + Repositorios | — | Backend | ✅ |
| T-06 | JWT: `JwtTokenProvider` + `JwtAuthenticationFilter` | US-03, US-04 | Backend | ✅ |
| T-07 | `SecurityConfig` (roles, CORS, stateless) | US-37 | Backend | ✅ |
| T-08 | `AuthService` + `AuthController` (register/login/refresh) | US-01..US-04 | Backend | ✅ |
| T-09 | `BookService` + `BookController` (catálogo público) | US-06..US-10 | Backend | ✅ |
| T-10 | `CategoryRepository` + endpoints de categorías y tags | US-08, US-09 | Backend | ✅ |
| T-11 | Configurar SpringDoc / Swagger UI | US-40 | Backend | ✅ |
| T-12 | Tests unitarios: `AuthServiceTest` | — | Backend | ✅ |

### Criterio de Éxito del Sprint
- `POST /auth/register` y `POST /auth/login` retornan JWT
- `GET /books` retorna catálogo paginado < 1.5 s
- Swagger UI accesible en `/api/swagger-ui.html`

---

## Sprint 2 — Compra y Biblioteca (Sem 3-4)

**Objetivo:** Un Buyer puede agregar libros al carrito, comprar y descargar de forma segura.

### Tareas

| # | Tarea | HU Asociadas | Estado |
|---|-------|-------------|--------|
| T-13 | `CartService` + `CartController` | US-13..US-15 | ✅ |
| T-14 | `OrderService` + `OrderController` (checkout completo) | US-16..US-18 | ✅ |
| T-15 | `DownloadService` + `LibraryController` | US-19..US-22 | ✅ |
| T-16 | `SignedUrlGenerator` (HMAC-SHA256, 5 min, 1 uso) | US-20..US-22 | ✅ |
| T-17 | `WishlistService` + `WishlistController` | US-23, US-24 | ✅ |
| T-18 | Caché Redis sobre `BookService.searchCatalog()` | US-38 | ✅ |
| T-19 | `GlobalExceptionHandler` (RFC 7807 ProblemDetail) | — | ✅ |
| T-20 | Tests unitarios: `BookServiceTest`, `SignedUrlGeneratorTest` | — | ✅ |

### Criterio de Éxito del Sprint
- Flujo completo: login → carrito → checkout → biblioteca → descarga funciona end-to-end
- Signed URL expira a los 5 min y se invalida tras descarga

---

## Sprint 3 — Seller, Admin y Reseñas (Sem 5-6)

**Objetivo:** Sellers pueden publicar libros. Admins moderan contenido. Buyers pueden reseñar.

### Tareas

| # | Tarea | HU Asociadas | Estado |
|---|-------|-------------|--------|
| T-21 | `SellerController` CRUD de publicaciones | US-27..US-30 | ✅ |
| T-22 | `BookService.approve()` / `reject()` | US-32, US-33 | ✅ |
| T-23 | `AdminController` gestión usuarios + libros | US-31..US-36 | ✅ |
| T-24 | `AdminService.getDashboard()` con métricas básicas | US-31 | ✅ |
| T-25 | `ReviewService` + `ReviewController` | US-25, US-26 | ✅ |
| T-26 | Recálculo automático de rating al crear/ocultar reseña | US-25, US-36 | ✅ |
| T-27 | Gestión de categorías y tags (Admin) | US-34, US-35 | ✅ |
| T-28 | Tests de integración básicos (H2 + MockMvc) | — | ⏳ |

### Criterio de Éxito del Sprint
- Seller puede publicar → Admin aprueba → libro aparece en catálogo público
- Buyer adquiere → puede reseñar → rating del libro se actualiza

---

## Sprint 4 — Pulido, Documentación y Entrega (Sem 7-8)

**Objetivo:** Sistema estable, documentado y listo para demostración.

### Tareas

| # | Tarea | Descripción |
|---|-------|-------------|
| T-29 | Subir archivos PDF de libros demo | Agregar PDFs a `./storage/books/` para probar descarga real |
| T-30 | Completar tests de integración | Controller tests con `@SpringBootTest` + `TestRestTemplate` |
| T-31 | Revisión de seguridad | Verificar que endpoints admin/seller no son accesibles sin JWT |
| T-32 | Optimización de queries N+1 | `@EntityGraph` en relaciones eager cuando sea necesario |
| T-33 | README final + guía de instalación | Instrucciones Docker, usuarios demo, flujos de prueba |
| T-34 | Preparar demo de la Entrega 1 | Flujo completo buyer + dashboard admin en Swagger UI |
| T-35 | Backlog para Entrega 2 | Refinamiento de historias US-41 en adelante |

### Criterio de Éxito del Sprint
- Demo funcional end-to-end ante el docente
- Cobertura de tests ≥ 70% en capa de servicios
- Documentación completa en Swagger UI

---

## Milestones

| Milestone | Sprint | Fecha estimada | Criterio |
|-----------|--------|----------------|----------|
| **M1 — Auth & Catálogo** | Sprint 1 | Sem 2 | Login + catálogo funcionan |
| **M2 — Compra completa** | Sprint 2 | Sem 4 | Checkout + descarga segura |
| **M3 — Plataforma completa** | Sprint 3 | Sem 6 | Seller + Admin activos |
| **🎯 Entrega 1** | Sprint 4 | Sem 8 | Demo + docs + MVP funcional |

---

## Gestión de Riesgos

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|-------------|---------|------------|
| Java 25 incompatible con alguna dependencia | Media | Alto | Usar `--enable-preview`, o fallback a Java 21 LTS |
| Redis no disponible en entorno del docente | Baja | Medio | Modo degradado: caché en memoria con `ConcurrentHashMap` |
| Postgres sin Docker | Media | Alto | Proveer script SQL alternativo para instalación manual |
| Falta de archivos PDF para demo | Alta | Bajo | Usar PDFs de prueba de dominio público (arxiv.org) |

---

## Entorno de Despliegue (Local)

```
┌─────────────────────────────────────────┐
│            Máquina del desarrollador     │
│                                          │
│  ┌─────────────┐    ┌────────────────┐  │
│  │ Spring Boot  │◄──►│   PostgreSQL    │  │
│  │  :8080/api  │    │   :5432        │  │
│  └──────┬──────┘    └────────────────┘  │
│         │                                │
│         ▼           ┌────────────────┐  │
│  ┌─────────────┐    │     Redis      │  │
│  │  Swagger UI │    │    :6379       │  │
│  │  :8080      │    └────────────────┘  │
│  └─────────────┘                        │
│                      ┌────────────────┐  │
│  ┌─────────────┐     │   pgAdmin 4    │  │
│  │  JavaFX     │     │   :5050        │  │
│  │  (frontend) │     └────────────────┘  │
│  └─────────────┘                        │
└─────────────────────────────────────────┘
```

**Comando de inicio:**
```bash
docker-compose up -d && mvn spring-boot:run
```
