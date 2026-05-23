# Entrega Final — OpenLib Market

**Entrega:** Final — Backlog, Plan de Rollout, Desarrollo Final  
**Sistema:** OpenLib Market (Gestión de Activos Digitales Académicos)  
**Stack local MVP:** Java 21 · JavaFX · Persistencia en archivos JSON  
**Stack backend de referencia:** Java 21 · Spring Boot 3.4 · PostgreSQL 17 · Redis 7 · Docker

---

## ✅ Nota de la Entrega Final

El documento presenta la versión final del proyecto. El MVP local funciona con **JavaFX y datos JSON** y adicionalmente la solución completa ya cuenta con un **backend de referencia dockerizado** en `openlib-backend/docker-compose.yml` para PostgreSQL y Redis.

- El MVP local demuestra todos los flujos de Buyer, Seller y Admin.
- El backend de referencia incluye la **base de datos con Docker** y la configuración de infraestructura para PostgreSQL y Redis.
- La solución ya cubre los requisitos de **Entrega 2** (Buyer, Seller, Admin) y **Entrega 3** (Métricas y reportes).

La opción local se mantiene para portabilidad, y la arquitectura backend está preparada para activar el despliegue Docker cuando el entorno lo permita.

---

## 1. Alcance del MVP

El MVP implementa las funcionalidades de mayor valor y riesgo técnico:

| Módulo | Alcance MVP | Estado |
|--------|-------------|--------|
| **Auth** | Registro, login con roles (Buyer/Seller/Admin) | ✅ Funcional |
| **Catálogo** | Búsqueda, filtros por categoría y tag, detalle de libro | ✅ Funcional |
| **Carrito** | Agregar / ver / eliminar ítems, persistencia en JSON | ✅ Funcional |
| **Checkout** | Flujo completo: facturación → pago simulado → confirmación | ✅ Funcional |
| **Biblioteca** | Acceso a libros adquiridos + descarga con Signed URL | ✅ Funcional |
| **Seller** | Publicar, editar, archivar libros | ✅ Funcional |
| **Admin** | Dashboard, aprobar/rechazar libros, gestión de usuarios/categorías | ✅ Funcional |
| **Reseñas** | Crear reseña (solo si adquirió), ver reseñas, moderación admin | ✅ Funcional |
| **Favoritos** | Agregar / quitar de lista de favoritos | ✅ Funcional |

---

## 2. Arquitectura Implementada

### 2.1 Arquitectura Objetivo (Backend Spring Boot — referencia)

```
┌──────────────────────────────────────────────────────┐
│                  REST API (:8080/api)                 │
├──────────────┬───────────────┬────────────────────────┤
│  controller/ │  9 controllers│ Auth, Book, Cart,       │
│              │               │ Order, Library,         │
│              │               │ Wishlist, Review,       │
│              │               │ Seller, Admin           │
├──────────────┴───────────────┴────────────────────────┤
│                     service/                          │
│  AuthService  BookService   CartService  OrderService  │
│  DownloadService  WishlistService  ReviewService       │
│  AdminService                                         │
├───────────────────────────────────────────────────────┤
│                    repository/                        │
│     Spring Data JPA (9 repositorios)                  │
│     JPA Specifications + JPQL custom queries          │
├──────────────┬────────────────────────────────────────┤
│  PostgreSQL  │  Redis                                  │
│  (datos)     │  (sesiones refresh token + caché)       │
└──────────────┴────────────────────────────────────────┘
```

### 2.2 Arquitectura MVP Funcional (JavaFX + JSON)

```
┌──────────────────────────────────────────────────────┐
│              Aplicación de Escritorio JavaFX          │
├──────────────────────────────────────────────────────┤
│                    controller/                        │
│  Login  Catalog  Cart  Checkout  Library  Admin       │
│  Seller  Wishlist  Orders  BookDetail  Register       │
├──────────────────────────────────────────────────────┤
│                     service/                          │
│  ApiClient (router) → LocalDataService                │
│  (toda la lógica de negocio en una sola capa)         │
├──────────────────────────────────────────────────────┤
│                  Persistencia local                   │
│         Archivos JSON en carpeta ./data/              │
│  users · books · categories · cart · orders          │
│  library · wishlist · reviews                        │
└──────────────────────────────────────────────────────┘
```

### 2.3 Cambios respecto a la arquitectura original

| Componente | Planificado | MVP Funcional | Razón del cambio |
|------------|-------------|---------------|------------------|
| Base de datos | PostgreSQL 17 (Docker) | Archivos JSON locales | Docker no disponible en el entorno |
| Caché | Redis 7 (Docker) | Sin caché (datos en memoria) | Docker no disponible en el entorno |
| Refresh tokens | Redis con TTL | Sesión en memoria (SessionManager) | Sin Redis disponible |
| Frontend | JavaFX conectado a API REST | JavaFX con lógica local integrada | Sin servidor backend activo |
| Autenticación | JWT stateless | Validación local con BCrypt | Adaptado al entorno sin servidor |
| Signed URLs | HMAC-SHA256 con expiración | Lógica equivalente en LocalDataService | Adaptado al entorno local |

---

## 3. Decisiones Técnicas Clave

### 3.1 Persistencia en Archivos JSON
En lugar de PostgreSQL, todos los datos se almacenan en archivos JSON dentro de la carpeta `data/` que se crea automáticamente al primer inicio:

```
data/
  users.json       — usuarios registrados
  books.json       — catálogo de libros
  categories.json  — categorías
  cart.json        — carritos por usuario
  orders.json      — historial de órdenes
  library.json     — biblioteca personal
  wishlist.json    — listas de favoritos
  reviews.json     — reseñas
```

**Ventaja:** el sistema es completamente portable — basta con tener Java instalado, sin necesidad de servidores externos.  
**Limitación:** no escala para múltiples usuarios concurrentes (aceptable para MVP de demostración).

### 3.2 Patrones de Diseño Implementados
La solución final incorpora patrones de diseño clave que mejoran la mantenibilidad y la migración al backend completo:

- **Builder**: `BookBuilder`, `OrderBuilder`, `OrderItemBuilder` y los `@Builder` de las entidades permiten crear objetos complejos de forma legible y segura.
- **Command**: `BookModerationCommand`, `ApproveBookCommand`, `RejectBookCommand`, `ArchiveBookCommand` y `BookModerationCommandInvoker` separan las acciones de moderación y facilitan la extensión.
- **Proxy**: `BookServiceCacheProxy` y `ApiClient` actúan como proxies para interponer lógica de cacheo, validación y redirección entre UI y servicios.
- **Singleton**: `SessionManager` y el singleton de refresh tokens en la capa de autenticación mantienen instancias únicas para sesión y token management.
- **Decorator**: `BookServiceLoggingDecorator` envuelve al servicio principal para agregar logging y trazabilidad sin modificar la lógica central.

### 3.3 Soporte Docker en el Backend de Referencia
El repositorio incluye el backend completo con Docker para la base de datos:

- `openlib-backend/docker-compose.yml` levanta **PostgreSQL 17** y **Redis 7**.
- `src/main/resources/db/migration/V1__init_schema.sql` y `V2__seed_data.sql` versionan la base de datos con Flyway.
- El backend de referencia ya está preparado para ejecutar la solución completa con Docker cuando el entorno lo permita.

Esta configuración demuestra que la arquitectura final ya contempla la **base de datos dockerizada** y la infraestructura necesaria para el despliegue real.

### 3.4 LocalDataService como capa de lógica
Toda la lógica de negocio que en el backend Spring Boot estaba distribuida en 8 servicios separados fue consolidada en `LocalDataService.java`. El `ApiClient.java` actúa como router que redirige las llamadas de los controladores hacia `LocalDataService`, manteniendo la misma interfaz que tendría si se llamara a la API REST real. Esto facilita la migración futura al backend completo.

### 3.5 Autenticación y Seguridad
- Las contraseñas se encriptan con **BCrypt** antes de guardarse en el JSON de usuarios.
- La sesión del usuario autenticado se mantiene en `SessionManager` durante la ejecución de la app.
- La lógica de **Signed URLs** para descargas (HMAC-SHA256, uso único, expiración de 5 minutos) está implementada en `LocalDataService`, equivalente a la implementación del backend.

### 3.6 Datos de Demostración
Al primer inicio se cargan automáticamente 3 usuarios demo, 7 categorías, 11 tags y 5 libros aprobados. Para reiniciar desde cero basta con borrar la carpeta `data/`.

### 3.7 Manejo de Errores RFC 7807 (Backend)
El backend de referencia implementa el estándar RFC 7807 para todas las respuestas de error:
```json
{
  "type":      "about:blank",
  "status":    404,
  "title":     "Not Found",
  "detail":    "Libro no encontrado con id: 99",
  "timestamp": "2026-04-23T23:00:00Z"
}
```

---

## 4. Estructura de Archivos

### 4.1 MVP Funcional (JavaFX)

```
openlib-mvp/
├── run.bat                          ← ejecutar en Windows
├── run.sh                           ← ejecutar en Linux/Mac
├── pom.xml
└── openlib-javafx/
    └── src/main/java/com/openlib/fx/
        ├── OpenLibApp.java          ← punto de entrada
        ├── controller/              ← pantallas de la app
        │   ├── LoginController.java
        │   ├── RegisterController.java
        │   ├── CatalogController.java
        │   ├── BookDetailController.java
        │   ├── CartController.java
        │   ├── CheckoutController.java
        │   ├── LibraryController.java
        │   ├── OrdersController.java
        │   ├── OrderDetailController.java
        │   ├── WishlistController.java
        │   ├── SellerBooksController.java
        │   └── AdminPanelController.java
        ├── service/
        │   ├── LocalDataService.java  ← toda la lógica + persistencia JSON
        │   └── ApiClient.java         ← router hacia LocalDataService
        ├── model/
        │   └── Models.java            ← DTOs del cliente
        └── util/
            ├── NavigationManager.java
            ├── SessionManager.java
            └── UiHelper.java
```

### 4.2 Backend de Referencia (Spring Boot)

```
openlib-backend/
├── docker-compose.yml
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/openlib/
    │   │   ├── OpenLibApplication.java
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java
    │   │   │   └── OpenApiConfig.java
    │   │   ├── controller/        (9 archivos)
    │   │   ├── domain/            (10 entidades + 4 enums)
    │   │   ├── dto/
    │   │   │   ├── request/       (9 records)
    │   │   │   └── response/      (10 records)
    │   │   ├── exception/
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   ├── BusinessException.java
    │   │   │   └── UnauthorizedException.java
    │   │   ├── repository/        (9 interfaces)
    │   │   ├── security/
    │   │   │   ├── JwtTokenProvider.java
    │   │   │   ├── JwtAuthenticationFilter.java
    │   │   │   └── UserDetailsServiceImpl.java
    │   │   ├── service/           (7 clases)
    │   │   └── util/
    │   │       └── SignedUrlGenerator.java
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/
    │           ├── V1__init_schema.sql
    │           └── V2__seed_data.sql
    └── test/
        ├── java/com/openlib/
        │   ├── service/
        │   │   ├── AuthServiceTest.java
        │   │   └── BookServiceTest.java
        │   └── util/
        │       └── SignedUrlGeneratorTest.java
        └── resources/
            └── application-test.yml
```

---

## 5. Cómo Ejecutar el MVP

### Prerrequisitos
- **Java 21+** instalado
- **Maven 3.9+** instalado (o usar la ruta completa al ejecutable)

### Pasos — Windows

```bat
cd openlib-mvp
.\run.bat
```

### Pasos — Linux / Mac

```bash
cd openlib-mvp
chmod +x run.sh
./run.sh
```

### Si Maven no está en el PATH (Windows)

```bat
C:\Maven\apache-maven-3.9.15\bin\mvn -f openlib-javafx\pom.xml javafx:run
```

### Usuarios Demo (pre-cargados al primer inicio)

| Email | Contraseña | Rol | Acceso |
|-------|-----------|-----|--------|
| admin@openlib.com | Admin1234! | ADMIN | Panel admin completo |
| seller@openlib.com | Seller1234! | SELLER | Gestión de publicaciones |
| buyer@openlib.com | Buyer1234! | BUYER | Catálogo, carrito, biblioteca |

> Para reiniciar los datos demo: borrar la carpeta `data/` y volver a ejecutar.

---

## 6. Flujos de Prueba Principales

### Flujo Buyer completo
```
1. Login con buyer@openlib.com
2. Explorar catálogo → buscar por título, filtrar por categoría
3. Abrir detalle de un libro → Agregar al carrito
4. Ir al carrito → Checkout → Confirmar orden
5. Ir a Biblioteca → ver libro adquirido → Descargar
6. Ir al libro → Dejar reseña con rating
```

### Flujo Seller
```
1. Login con seller@openlib.com
2. Ir a Mis Publicaciones → Publicar nuevo libro
3. El libro queda en estado PENDING esperando aprobación del admin
```

### Flujo Admin
```
1. Login con admin@openlib.com
2. Ver Dashboard → métricas de la plataforma
3. Ir a gestión de libros → Aprobar libro del seller
4. El libro aparece en el catálogo público
5. Gestionar usuarios, categorías, moderar reseñas
```

---

## 7. Métricas de Calidad del MVP

| Atributo | Meta | Estrategia implementada |
|----------|------|------------------------|
| Portabilidad | Sin dependencias externas | Solo requiere Java 21, sin Docker ni BD |
| Seguridad contraseñas | Encriptado | BCrypt aplicado en LocalDataService |
| Trazabilidad descargas | 100% | Registro en library.json con timestamp y token |
| Seguridad archivos | Signed URLs | HMAC-SHA256, 5 min, 1 uso (equivalente al backend) |
| Mantenibilidad | Alta | Arquitectura en capas, mismo patrón controller/service/model |
| Escalabilidad futura | Preparada | ApiClient actúa como proxy → migración al backend sin cambiar controllers |
