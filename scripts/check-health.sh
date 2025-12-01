#!/bin/bash

# PMS Ingestion - Check Service Health
# This script checks the health of all services in the PMS ingestion pipeline

echo "🏥 Checking PMS Ingestion Service Health..."
echo "=========================================="

# Function to check if a service is running
check_service() {
    local service_name=$1
    local port=$2
    local expected_response=$3

    if docker-compose ps | grep -q "$service_name.*Up"; then
        echo "✅ $service_name: RUNNING"

        # Additional health checks
        case $service_name in
            "pms-ingestion")
                if curl -f -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
                    echo "   📊 Health Check: PASSED"
                else
                    echo "   ❌ Health Check: FAILED"
                fi
                ;;
            "postgres")
                if docker exec postgres pg_isready -U pms-usr -d pms-db > /dev/null 2>&1; then
                    echo "   🗄️  Database: CONNECTED"
                else
                    echo "   ❌ Database: DISCONNECTED"
                fi
                ;;
            "redis")
                if docker exec redis redis-cli ping | grep -q "PONG"; then
                    echo "   🔴 Redis: CONNECTED"
                else
                    echo "   ❌ Redis: DISCONNECTED"
                fi
                ;;
            "kafka")
                if docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; then
                    echo "   📨 Kafka: CONNECTED"
                else
                    echo "   ❌ Kafka: DISCONNECTED"
                fi
                ;;
        esac
    else
        echo "❌ $service_name: NOT RUNNING"
    fi
    echo ""
}

# Check all services
check_service "pms-simulation" "4000"
check_service "pms-ingestion" "8081"
check_service "postgres" "5432"
check_service "redis" "6379"
check_service "kafka" "9092"

echo "=========================================="
echo "🏁 Health check completed!"