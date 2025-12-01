#!/bin/bash

# PMS Ingestion - Cleanup Services
# This script stops and removes all Docker services and volumes

set -e

echo "🧹 Cleaning up PMS Ingestion Services..."
echo "========================================"

# Change to project directory
cd "$(dirname "$0")/.."

# Stop all services
echo "⏹️  Stopping all services..."
docker-compose down

# Remove volumes (optional - ask user)
echo ""
read -p "🗑️  Remove data volumes? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Removing volumes..."
    docker-compose down -v
    echo "✅ Volumes removed"
else
    echo "ℹ️  Volumes preserved"
fi

# Remove dangling images
echo ""
echo "🖼️  Cleaning up dangling Docker images..."
DANGLING=$(docker images -f "dangling=true" -q)
if [ ! -z "$DANGLING" ]; then
    docker rmi $DANGLING
    echo "✅ Dangling images removed"
else
    echo "ℹ️  No dangling images found"
fi

# Show disk usage
echo ""
echo "💾 Current Docker disk usage:"
docker system df

echo ""
echo "✅ Cleanup completed!"
echo ""
echo "🚀 To restart services, run: ./scripts/start-services.sh"