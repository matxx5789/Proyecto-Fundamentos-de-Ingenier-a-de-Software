@echo off
title OpenLib Market MVP - Iniciador Unificado
echo ========================================================
echo   OpenLib Market MVP - Iniciador de Entorno Completo
echo ========================================================
echo.

:: 1. Levantar contenedores Docker
echo [1/3] Levantando base de datos y servicios en Docker...
cd /d "%~dp0openlib-backend"
call docker-compose up -d
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] No se pudo levantar Docker. Por favor asegúrate de que Docker Desktop esté ejecutándose.
    pause
    exit /b %ERRORLEVEL%
)
echo [OK] Contenedores levantados exitosamente.
echo.

:: 2. Verificar si Maven está disponible
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ALERTA] No se pudo encontrar el comando 'mvn' en el PATH del sistema.
    echo.
    echo Pasos sugeridos:
    echo 1. Abre tu IDE de preferencia, por ejemplo, IntelliJ IDEA o Eclipse.
    echo 2. Importa el proyecto 'openlib-backend' y ejecuta la clase 'OpenLibApplication'.
    echo 3. Abre el proyecto 'openlib-javafx' y ejecuta la clase 'OpenLibApp'.
    echo.
    echo Si deseas ejecutarlo por consola, por favor asegúrate de que Maven está instalado y
    echo configurado correctamente en tus Variables de Entorno.
    echo.
    pause
    exit /b 1
)

:: 3. Iniciar el Backend en una ventana separada
echo [2/3] Iniciando Backend de Spring Boot (en ventana separada)...
start "OpenLib Backend API" cmd /k "cd /d "%~dp0openlib-backend" && title OpenLib Backend && mvn spring-boot:run"
echo.
echo Esperando 8 segundos para asegurar la inicialización y migración de la base de datos...
timeout /t 8 /nobreak > null
echo.

:: 4. Lanzar la aplicación Frontend JavaFX
echo [3/3] Iniciando Cliente JavaFX...
cd /d "%~dp0openlib-javafx"
call mvn javafx:run

echo.
echo ========================================================
echo   Aplicación cerrada.
echo ========================================================
pause
