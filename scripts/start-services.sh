#!/bin/bash

# PMS Ingestion - Start All Services
# This script starts all Docker services for the PMS ingestion pipeline

set -e

echo "🚀 Starting PMS Ingestion Services..."

# Change to the project directory
cd "$(dirname "$0")/.."

# Start all services
docker-compose up -d

echo "⏳ Waiting for services to be healthy..."
sleep 30

# Check if services are running
echo "📊 Service Status:"
docker-compose ps

echo "✅ All services started successfully!"
echo ""
echo "🌐 Service URLs:"
echo "  - PMS Simulation: http://localhost:4000"
echo "  - PMS Ingestion: http://localhost:8081"
echo "  - Redis: localhost:6379"
echo "  - PostgreSQL: localhost:5432"
echo "  - Kafka: localhost:9092"
echo ""
echo "📝 To check data flow, run: ./scripts/check-data-flow.sh"