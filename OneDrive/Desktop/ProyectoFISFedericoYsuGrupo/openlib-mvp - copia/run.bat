@echo off
echo.
echo ========================================
echo   OpenLib Market MVP - Iniciando...
echo ========================================
echo.

cd /d "%~dp0openlib-javafx"
echo Compilando...
call mvn package -q -DskipTests
echo Lanzando aplicacion...
call mvn javafx:run

echo Aplicacion cerrada.
pause
