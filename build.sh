#!/bin/bash

# Exit on error
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Starting build process...${NC}"

# Load environment variables
if [ -f .env ]; then
    echo -e "${YELLOW}Loading environment variables from .env file${NC}"
    export $(grep -v '^#' .env | xargs)
else
    echo -e "${YELLOW}No .env file found, using defaults${NC}"
fi

# Step 1: Build the Java application JAR
echo -e "${GREEN}Step 1: Building JAR with Maven...${NC}"
mvn clean package -DskipTests

# Step 2: Build Docker images
echo -e "${GREEN}Step 2: Building Docker images...${NC}"
docker compose build --no-cache

# Step 3: Start services
echo -e "${GREEN}Step 3: Starting services...${NC}"
docker compose up -d

# Step 4: Wait for services to be healthy
echo -e "${GREEN}Step 4: Waiting for services to be ready...${NC}"

# Wait for PostgreSQL
echo "Waiting for PostgreSQL..."
until docker exec telegram-postgres pg_isready -U postgres > /dev/null 2>&1; do
    sleep 2
done
echo -e "${GREEN}PostgreSQL is ready!${NC}"

# Wait for RabbitMQ
echo "Waiting for RabbitMQ..."
until docker exec telegram-rabbitmq rabbitmq-diagnostics -q ping > /dev/null 2>&1; do
    sleep 2
done
echo -e "${GREEN}RabbitMQ is ready!${NC}"

# Wait for Application
echo "Waiting for Notification Service..."
until curl -s http://localhost:${APP_PORT:-8088}/actuator/health > /dev/null 2>&1; do
    sleep 2
done
echo -e "${GREEN}Notification Service is ready!${NC}"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Build and deployment complete!${NC}"
echo -e "${GREEN}Services are running:${NC}"
echo -e "  - PostgreSQL: localhost:${POSTGRES_HOST_PORT:-54322}"
echo -e "  - RabbitMQ UI: http://localhost:${RABBITMQ_HTTP_PORT:-15672}"
echo -e "  - RabbitMQ AMQP: localhost:${RABBITMQ_AMQP_PORT:-5672}"
echo -e "  - Notification Service: http://localhost:${APP_PORT:-8088}/api"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${YELLOW}Useful commands:${NC}"
echo -e "  View logs: docker compose logs -f"
echo -e "  Stop services: docker compose down"
echo -e "  View service status: docker compose ps"
echo -e "  Rebuild single service: docker compose up -d --build notification-service"