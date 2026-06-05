@echo off
echo.
echo ========================================
echo   OpenLib Market MVP - Iniciando...
echo ========================================
echo.

cd /d "%~dp0openlib-javafx"
echo Compilando...
call ..\openlib-backend\apache-maven-3.9.9\bin\mvn.cmd package -q -DskipTests
echo Lanzando aplicacion...
call ..\openlib-backend\apache-maven-3.9.9\bin\mvn.cmd javafx:run

echo Aplicacion cerrada.
pause
