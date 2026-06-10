#!/bin/bash

# Bash script to start BizFlow stack in Docker (Linux/macOS)

# Colors for output
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${CYAN}==========================================${NC}"
echo -e "${CYAN}🚀 BizFlow Stack Quick Starter (Unix)${NC}"
echo -e "${CYAN}==========================================${NC}"
echo ""

# 1. Check/copy .env file
if [ ! -f .env ]; then
    if [ -f .env.example ]; then
        echo -e "${YELLOW}📝 .env file not found. Copying from .env.example...${NC}"
        cp .env.example .env
        echo -e "${GREEN}✅ Created .env file successfully.${NC}"
    else
        echo -e "${RED}❌ Error: Neither .env nor .env.example was found!${NC}"
        exit 1
    fi
else
    echo -e "${GREEN}✅ .env file exists.${NC}"
fi

# 2. Check if Docker is running
echo "🐳 Checking if Docker is running..."
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Error: Docker daemon is not running!${NC}"
    echo -e "${YELLOW}👉 Please start Docker/Docker Desktop and try again.${NC}"
    echo ""
    exit 1
fi
echo -e "${GREEN}✅ Docker is running.${NC}"

# 3. Start the containers
echo -e "${CYAN}🔨 Launching Docker Compose stack (build and start)...${NC}"
if ! docker compose up -d --build; then
    echo -e "${RED}❌ Error: Failed to start Docker Compose!${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}==========================================================${NC}"
echo -e "${GREEN}🎉 BizFlow is starting up!${NC}"
echo -e "${GREEN}==========================================================${NC}"
echo ""
echo -e "🌐 Access URLS:"
echo -e "   - 🖥️  Frontend Website: ${YELLOW}http://localhost:3000${NC}"
echo -e "   - 🗄️  Database phpMyAdmin: ${YELLOW}http://localhost:8088${NC}"
echo -e "   - 🔌 Gateway API: ${YELLOW}http://localhost:8000${NC}"
echo ""
echo -e "💡 Note: Spring Boot microservices take about 45-60 seconds to fully initialize."
echo -e "   Enjoy coding! 🚀"
echo -e "${GREEN}==========================================================${NC}"
echo ""
