#!/bin/bash
set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo ""
echo "========================================"
echo "  OpenLib Market MVP — Iniciando..."
echo "========================================"
echo ""
echo -e "${YELLOW}Compilando cliente JavaFX...${NC}"

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR/openlib-javafx"
mvn package -q -DskipTests

echo -e "${GREEN}Lanzando aplicación...${NC}"
echo ""
echo "  Datos en: $ROOT_DIR/openlib-javafx/data/"
echo ""

mvn javafx:run

echo "Aplicación cerrada."
