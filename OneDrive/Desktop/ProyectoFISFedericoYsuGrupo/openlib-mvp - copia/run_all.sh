#!/bin/bash
set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}========================================================${NC}"
echo -e "${GREEN}  OpenLib Market MVP - Iniciador de Entorno Completo    ${NC}"
echo -e "${GREEN}========================================================${NC}"
echo ""

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

# 1. Levantar contenedores Docker
echo -e "${YELLOW}[1/3] Levantando base de datos y servicios en Docker...${NC}"
cd "$ROOT_DIR/openlib-backend"
docker-compose up -d
echo -e "${GREEN}[OK] Contenedores levantados.${NC}"
echo ""

# 2. Iniciar el Backend en segundo plano
echo -e "${YELLOW}[2/3] Iniciando Backend de Spring Boot...${NC}"
mvn spring-boot:run > "$ROOT_DIR/backend.log" 2>&1 &
BACKEND_PID=$!

# Función para detener el backend al salir
cleanup() {
    echo ""
    echo -e "${YELLOW}Deteniendo Backend (PID $BACKEND_PID)...${NC}"
    kill $BACKEND_PID 2>/dev/null || true
    echo "Hecho."
}
trap cleanup EXIT

echo "Esperando 8 segundos para asegurar la inicialización y migración de la base de datos..."
sleep 8
echo ""

# 3. Lanzar la aplicación Frontend JavaFX
echo -e "${YELLOW}[3/3] Iniciando Cliente JavaFX...${NC}"
cd "$ROOT_DIR/openlib-javafx"
mvn javafx:run

echo ""
echo "Aplicación cerrada."
