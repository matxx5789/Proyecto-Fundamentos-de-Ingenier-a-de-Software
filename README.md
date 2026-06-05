# OpenLib Market MVP

Sistema de Gestión de Activos Digitales Académicos.

## Qué se hizo

Este proyecto integra dos partes:

- **Backend** en `openlib-backend/` con Spring Boot, PostgreSQL y Redis.
- **Frontend de escritorio** en `openlib-javafx/` usando JavaFX.

La base de datos ahora se levanta con **Docker Compose**, por lo que ya no funciona solo con archivos JSON.

## Requisitos

- Java 21 (JDK)
- Maven 3.9+
- Docker y Docker Compose
- Sistema operativo compatible con JavaFX

## Cómo ejecutar

### 1. Levantar la base de datos y servicios con Docker

```bash
cd openlib-backend
docker-compose up -d
```

Esto inicia:

- PostgreSQL en `localhost:5432`
- Redis en `localhost:6379`
- pgAdmin en `http://localhost:5050`

### 2. Ejecutar la API del backend

Desde `openlib-backend/`:

```bash
mvn spring-boot:run
```

La API quedará disponible en `http://localhost:8080`.

### 3. Ejecutar la aplicación JavaFX

Desde el directorio raíz del proyecto:

```bat
run.bat
```

O en Linux/macOS:

```bash
chmod +x run.sh
./run.sh
```

## Usuarios de prueba (cargados automáticamente al primer inicio)

| Rol    | Email                 | Contraseña   |
|--------|-----------------------|--------------|
| Admin  | admin@openlib.com     | Admin1234!   |
| Seller | seller@openlib.com    | Seller1234!  |
| Buyer  | buyer@openlib.com     | Buyer1234!   |

## Persistencia de datos

Los datos se almacenan en PostgreSQL y se acceden a través del backend Spring Boot.

- PostgreSQL guarda usuarios, libros, categorías, carritos, órdenes, biblioteca, lista de deseos y reseñas.
- Redis se usa para refresh tokens y sesiones.

## Reiniciar la base de datos

Para reiniciar los datos, detén `docker-compose`, elimina los volúmenes y vuelve a levantar los servicios:

```bash
cd openlib-backend
docker-compose down -v
docker-compose up -d
```

## Arquitectura

```
openlib-mvp/
├── openlib-backend/    # API Spring Boot + PostgreSQL + Redis
│   ├── docker-compose.yml
│   ├── pom.xml
│   └── src/main/java/com/openlib/
└── openlib-javafx/     # App de escritorio JavaFX que consume el backend
    ├── pom.xml
    └── src/main/java/com/openlib/fx/
        ├── controller/
        ├── service/
        └── util/
```

## Nota

- La aplicación JavaFX depende del backend y la base de datos.
- Arranca primero Docker Compose y luego el backend.
- Después ejecuta `run.bat` o `run.sh` para abrir la interfaz.
