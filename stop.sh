#!/bin/bash

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"

cd "$PROJECT_DIR"

echo "======================================"
echo " Stopping Maieveen CRM"
echo "======================================"

echo ""
echo "[1/2] Stopping PostgreSQL..."

docker compose -f docker/docker-compose.yml stop postgres

echo ""
echo "[2/2] PostgreSQL stopped."

echo ""
echo "CRM and PostgreSQL have been stopped."
echo "Your PostgreSQL data has been preserved."
