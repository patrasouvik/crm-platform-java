#!/bin/bash

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"

cd "$PROJECT_DIR"

echo "======================================"
echo " Starting Maieveen CRM"
echo "======================================"

echo ""
echo "[1/3] Checking Java..."
java -version

echo ""
echo "[2/3] Starting PostgreSQL..."
docker compose -f docker/docker-compose.yml up -d postgres

echo ""
echo "Waiting for PostgreSQL..."
sleep 3

if ! docker ps --format '{{.Names}}' | grep -q '^docker-postgres-1$'; then
    echo "ERROR: PostgreSQL container is not running."
    exit 1
fi

echo "PostgreSQL is running."

echo ""
echo "[3/3] Starting Spring Boot CRM..."
echo ""
echo "CRM URL:    http://localhost:8080"
echo "Health:     http://localhost:8080/actuator/health"
echo ""
echo "Press Ctrl+C to stop the Spring Boot application."
echo ""

mvn spring-boot:run
