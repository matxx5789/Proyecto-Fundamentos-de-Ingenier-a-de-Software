# 📢 Enunciado proyecto

No estamos construyendo una simple página web; estamos diseñando una plataforma transaccional. Aunque los libros son "gratis" (Open Source), el sistema debe comportarse como un E-commerce real (Amazon/Shopify) para garantizar la trazabilidad de las descargas, la seguridad de los archivos y la gestión de derechos de autor.

## 1. Contexto del Proyecto

**Nombre del Sistema:** OpenLib Market (Sistema de Gestión de Activos Digitales Académicos)

### El Problema

Las instituciones educativas y fundaciones open-source actualmente distribuyen sus materiales mediante listas de enlaces estáticos o carpetas compartidas (Google Drive, FTP). Esto genera:

- **Pérdida de data:** No se sabe quién descarga qué, ni desde dónde.
- **Falta de Valor Percibido:** Al ser "links sueltos", el usuario no percibe el valor del material.
- **Seguridad Nula:** Los enlaces pueden ser compartidos indiscriminadamente sin control de acceso o cuotas.
- **Experiencia de Usuario Pobre:** No hay búsqueda avanzada, recomendaciones ni historial de "compras".

### La Solución Propuesta

Desarrollar una **tienda de libros digitales** completa que simule una transacción monetaria real (aunque el monto a pagar sea $0.00 o simbólico/donación). Esto profesionaliza la experiencia y permite recolectar métricas valiosas.

## 2. Alcance Funcional 🏁

### Módulo de Buyer (Storefront)

- **Registro y login:** Registro y login de usuarios.
- **Catálogo Interactivo:** Búsqueda por título, autor, ISBN, categoría y etiquetas.
- **Filtros Avanzados:** Búsqueda por autor, ISBN, categoría y etiquetas.
- **Recomendaciones:** Recomendaciones basadas en el historial de compras.
- **Historial de Compras:** Historial de compras del usuario.
- **Lista de Favoritos:** Lista de libros favoritos del usuario.
- **Carrito de Compras:** Persistencia de ítems seleccionados.
- **Checkout Simulado:** Flujo de pago completo (Dirección de facturación -> Selección de método de pago -> Confirmación).
- **Biblioteca Personal:** Área donde el usuario ve sus libros adquiridos y puede descargarlos.
- **Motor de Descargas Seguro:** Los enlaces de descarga deben ser temporales (signed URLs) y únicos por usuario.
- **Reviews de las compras:** Reseñas y calificaciones de los libros.

### Módulo Seller (Storefront)

- **Registro y login:** Registro y login de usuarios.
- **Gestión de publicación:** Gestión de publicación de libros.

### Módulo Admin (Storeback)

- **Dashboard de Métricas:** Libros más descargados, usuarios activos, categorías populares.
- **Curaruría y calidad de Datos:** Validación de datos de usuarios y libros, reseñas y calificaciones.
- **Gestión de usuarios:** Gestión de usuarios.
- **Gestión de libros:** Gestión de libros.
- **Gestión de categorías:** Gestión de categorías.
- **Gestión de etiquetas:** Gestión de etiquetas.

## 3. Atributos de calidad :rocket:

- **Escalabilidad:** Arquitectura preparada para soportar estacionalidad (ej. inicio de semestre).
- **Seguridad:** Encriptación de datos sensibles de usuarios.
- **Performance:** Carga de catálogo < 1.5s.

## 4. Stack Tecnológico Sugerido (Referencia)

- **Backend:** Java 25, Spring Boot 4x :java:
- **Frontend:** JavaFX *
- **Base de Datos:** Relacional (PostgreSQL/MySQL) para transacciones y NoSQL (MongoDB/Redis) para caché/sesiones.
- **Almacenamiento:** Ambiente local

## 5. IA

- **IDE Agentico OpenSource:** Antigravity, OpenCode, KiloCode, Windsurf

## 6. Entregas

### Entrega 1

Backlog, Plan de Rollout, Desarrollo MVP

### Entrega 2

Funcionalidades del módulo Buyer, Seller y Admin

### Entrega 3

Métricas y reportes
