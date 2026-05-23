# 📋 Product Backlog — OpenLib Market

**Sistema:** OpenLib Market (Gestión de Activos Digitales Académicos)  
**Versión:** Final — Entrega Final  
**Fecha:** Abril 2026

> Nota: Este backlog integra el estado final del proyecto y ya cumple con los requisitos de **Entrega 2** (Buyer, Seller, Admin) y **Entrega 3** (Métricas y reportes).

---

## Épicas

| ID | Épica | Módulo |
|----|-------|--------|
| EP-01 | Gestión de Identidad y Acceso | Transversal |
| EP-02 | Catálogo de Libros | Buyer |
| EP-03 | Carrito y Checkout | Buyer |
| EP-04 | Biblioteca Personal y Descargas | Buyer |
| EP-05 | Interacción Social (Favoritos y Reseñas) | Buyer |
| EP-06 | Publicación de Libros | Seller |
| EP-07 | Panel de Administración | Admin |
| EP-08 | Infraestructura y Seguridad | Transversal |

---

## EP-01 — Gestión de Identidad y Acceso

| ID | Historia de Usuario | Criterios de Aceptación | SP | Prioridad |
|----|---------------------|-------------------------|----|-----------|
| US-01 | **Como** visitante **quiero** registrarme como Buyer **para** acceder al catálogo | Email único, contraseña con mín. 8 chars + mayúscula + número, JWT retornado | 3 | 🔴 Alta |
| US-02 | **Como** visitante **quiero** registrarme como Seller **para** publicar libros | Igual que US-01 con rol SELLER, cuenta activa | 2 | 🔴 Alta |
| US-03 | **Como** usuario registrado **quiero** hacer login **para** obtener mis tokens JWT | Access token 15 min, refresh token 7 días en Redis | 3 | 🔴 Alta |
| US-04 | **Como** usuario autenticado **quiero** renovar mi token **para** no perder mi sesión | Refresh token válido retorna nuevo access token, rotación del refresh | 2 | 🔴 Alta |
| US-05 | **Como** admin **quiero** desactivar cuentas **para** gestionar usuarios problemáticos | Toggle activo/inactivo, usuario desactivado no puede autenticarse | 2 | 🟡 Media |

**Total EP-01: 12 SP**

---

## EP-02 — Catálogo de Libros

| ID | Historia de Usuario | Criterios de Aceptación | SP | Prioridad |
|----|---------------------|-------------------------|----|-----------|
| US-06 | **Como** visitante **quiero** ver el catálogo de libros **para** explorar el contenido disponible | Solo libros APPROVED, paginado (máx 50 por página), carga < 1.5s con caché Redis | 5 | 🔴 Alta |
| US-07 | **Como** visitante **quiero** buscar libros por título, autor o ISBN **para** encontrar lo que necesito | Búsqueda parcial case-insensitive, resultados relevantes | 3 | 🔴 Alta |
| US-08 | **Como** visitante **quiero** filtrar por categoría **para** navegar por tema de interés | Dropdown de categorías activas, combinable con búsqueda | 2 | 🔴 Alta |
| US-09 | **Como** visitante **quiero** filtrar por etiquetas **para** encontrar libros especializados | Multitag, combinable con búsqueda y categoría | 2 | 🟡 Media |
| US-10 | **Como** visitante **quiero** ver el detalle de un libro **para** conocer su descripción, autor y métricas | Título, autor, ISBN, descripción, cover, rating, nº reseñas, categoría, tags | 2 | 🔴 Alta |
| US-11 | **Como** buyer autenticado **quiero** ver recomendaciones **para** descubrir libros afines a mis compras | Basadas en categoría del historial, fallback a top descargados | 5 | 🟡 Media |
| US-12 | **Como** visitante **quiero** ordenar resultados **para** encontrar lo más relevante | Ordenar por: más reciente, más descargado, mejor calificado, título A-Z | 2 | 🟡 Media |

**Total EP-02: 21 SP**

---

## EP-03 — Carrito y Checkout

| ID | Historia de Usuario | Criterios de Aceptación | SP | Prioridad |
|----|---------------------|-------------------------|----|-----------|
| US-13 | **Como** buyer **quiero** agregar libros al carrito **para** acumular mis selecciones | Sin duplicados, solo libros APPROVED, persistido en BD | 3 | 🔴 Alta |
| US-14 | **Como** buyer **quiero** ver mi carrito **para** revisar qué tengo seleccionado | Lista de libros con cover, título, precio; total calculado | 2 | 🔴 Alta |
| US-15 | **Como** buyer **quiero** eliminar ítems del carrito **para** ajustar mi selección | Eliminación individual o total del carrito | 1 | 🔴 Alta |
| US-16 | **Como** buyer **quiero** completar el checkout **para** adquirir los libros | Flujo: datos de facturación → método de pago → confirmación. Orden CONFIRMED creada. | 8 | 🔴 Alta |
| US-17 | **Como** buyer **quiero** ver el historial de mis órdenes **para** tener registro de mis adquisiciones | Lista paginada con estado, fecha, ítems y total | 3 | 🔴 Alta |
| US-18 | **Como** buyer **quiero** ver el detalle de una orden **para** revisar su contenido | Datos de facturación completos + lista de libros con precio | 2 | 🟡 Media |

**Total EP-03: 19 SP**

---

## EP-04 — Biblioteca Personal y Descargas Seguras

| ID | Historia de Usuario | Criterios de Aceptación | SP | Prioridad |
|----|---------------------|-------------------------|----|-----------|
| US-19 | **Como** buyer **quiero** ver mi biblioteca personal **para** acceder a mis libros adquiridos | Lista de libros de órdenes CONFIRMED, con cover y título | 3 | 🔴 Alta |
| US-20 | **Como** buyer **quiero** obtener un enlace de descarga **para** bajar mi libro | Signed URL válida 5 min, única por usuario+libro+momento (HMAC-SHA256) | 8 | 🔴 Alta |
| US-21 | **Como** sistema **quiero** registrar cada descarga **para** tener trazabilidad completa | IP del usuario, timestamp, token usado → Download record en BD | 5 | 🔴 Alta |
| US-22 | **Como** sistema **quiero** que los links de descarga sean de un solo uso **para** evitar compartición | Token se invalida tras descarga exitosa, incrementa contador del libro | 5 | 🔴 Alta |

**Total EP-04: 21 SP**

---

## EP-05 — Interacción Social

| ID | Historia de Usuario | Criterios de Aceptación | SP | Prioridad |
|----|---------------------|-------------------------|----|-----------|
| US-23 | **Como** buyer **quiero** agregar libros a favoritos **para** guardar mis intereses | Sin duplicados, visible en lista de favoritos, sin necesidad de compra previa | 2 | 🟡 Media |
| US-24 | **Como** buyer **quiero** quitar libros de favoritos **para** mantener mi lista relevante | Eliminación inmediata, reflejo en UI | 1 | 🟡 Media |
| US-25 | **Como** buyer **quiero** reseñar un libro adquirido **para** compartir mi opinión | Solo libros adquiridos, máx 1 reseña por usuario/libro, rating 1-5 + texto | 5 | 🟡 Media |
| US-26 | **Como** visitante **quiero** ver las reseñas de un libro **para** informar mi decisión | Solo reseñas visibles (no moderadas), paginadas, ordenadas por fecha | 2 | 🟡 Media |

**Total EP-05: 10 SP**

---

## EP-06 — Publicación de Libros (Seller)

| ID | Historia de Usuario | Criterios de Aceptación | SP | Prioridad |
|----|---------------------|-------------------------|----|-----------|
| US-27 | **Como** seller **quiero** publicar un libro **para** distribuirlo en la plataforma | Título, autor obligatorios; estado inicial PENDING; asignar categoría y tags | 5 | 🔴 Alta |
| US-28 | **Como** seller **quiero** editar mi publicación **para** corregir datos o actualizar contenido | Solo puedo editar mis propios libros; mantiene estado actual | 3 | 🟡 Media |
| US-29 | **Como** seller **quiero** retirar un libro **para** sacarlo del catálogo | Cambia a ARCHIVED, deja de aparecer en catálogo público | 2 | 🟡 Media |
| US-30 | **Como** seller **quiero** ver mis publicaciones **para** gestionar mi catálogo | Lista paginada con estado (PENDING/APPROVED/REJECTED/ARCHIVED) | 2 | 🟡 Media |

**Total EP-06: 12 SP**

---

## EP-07 — Panel de Administración

| ID | Historia de Usuario | Criterios de Aceptación | SP | Prioridad |
|----|---------------------|-------------------------|----|-----------|
| US-31 | **Como** admin **quiero** ver un dashboard **para** conocer el estado de la plataforma | Total usuarios, libros, órdenes, descargas; top 10 libros descargados | 5 | 🟡 Media |
| US-32 | **Como** admin **quiero** aprobar libros **para** publicarlos en el catálogo | Cambio de PENDING → APPROVED, libro visible públicamente | 3 | 🔴 Alta |
| US-33 | **Como** admin **quiero** rechazar libros **para** mantener la calidad del catálogo | Cambio de PENDING → REJECTED, libro no visible | 2 | 🔴 Alta |
| US-34 | **Como** admin **quiero** gestionar categorías **para** organizar el catálogo | CRUD completo: crear, editar nombre/slug/desc, eliminar | 3 | 🟡 Media |
| US-35 | **Como** admin **quiero** gestionar etiquetas **para** categorizar mejor los libros | Crear nueva etiqueta, eliminar etiqueta sin libros asociados | 2 | 🟡 Media |
| US-36 | **Como** admin **quiero** moderar reseñas **para** mantener la calidad del contenido | Ocultar reseña (is_visible = false), rating del libro se recalcula | 3 | 🟡 Media |

**Total EP-07: 18 SP**

---

## EP-08 — Infraestructura y Seguridad

| ID | Historia de Usuario | Criterios de Aceptación | SP | Prioridad |
|----|---------------------|-------------------------|----|-----------|
| US-37 | **Como** sistema **quiero** encriptar contraseñas **para** proteger datos sensibles | BCrypt con factor 12, nunca almacenar en texto plano | 1 | 🔴 Alta |
| US-38 | **Como** sistema **quiero** cachear el catálogo en Redis **para** cumplir el SLA de 1.5s | Caché de 10 min, invalidación automática al modificar libros | 3 | 🟡 Media |
| US-39 | **Como** sistema **quiero** migraciones de BD versionadas **para** control de cambios | Flyway, naming V{n}__{desc}.sql, aplicación automática al startup | 2 | 🔴 Alta |
| US-40 | **Como** desarrollador **quiero** documentación de la API **para** integrar el frontend | Swagger UI en /api/swagger-ui.html, esquema Bearer JWT | 2 | 🔴 Alta |

**Total EP-08: 8 SP**

---

## Resumen del Backlog

| Épica | SP | Prioridad |
|-------|----|-----------|
| EP-01 Identidad y Acceso | 12 | Alta |
| EP-02 Catálogo | 21 | Alta |
| EP-03 Carrito y Checkout | 19 | Alta |
| EP-04 Biblioteca y Descargas | 21 | Alta |
| EP-05 Interacción Social | 10 | Media |
| EP-06 Seller | 12 | Alta |
| EP-07 Admin | 18 | Media |
| EP-08 Infraestructura | 8 | Alta |
| **TOTAL** | **121 SP** | |

---

## Criterios de Definition of Done (DoD)

- [ ] Código revisado y sin errores de compilación
- [ ] Tests unitarios con cobertura ≥ 70% en servicios
- [ ] Endpoint documentado en Swagger UI
- [ ] Migraciones SQL aplicadas en entorno de prueba
- [ ] Respuestas de error en formato RFC 7807 ProblemDetail
- [ ] Sin credenciales hardcodeadas en el código
