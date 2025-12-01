#!/bin/bash

# PMS Ingestion - Check Data Flow
# This script checks the complete data flow: WebSocket → Redis → Database → Kafka

echo "🔄 Checking PMS Ingestion Data Flow..."
echo "======================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print status
print_status() {
    local status=$1
    local message=$2
    case $status in
        "PASS")
            echo -e "${GREEN}✅ $message${NC}"
            ;;
        "FAIL")
            echo -e "${RED}❌ $message${NC}"
            ;;
        "WARN")
            echo -e "${YELLOW}⚠️  $message${NC}"
            ;;
        "INFO")
            echo -e "${BLUE}ℹ️  $message${NC}"
            ;;
    esac
}

echo "1️⃣  Checking WebSocket Connection (Simulation → Ingestion)"
echo "-------------------------------------------------------"

# Check if simulation service is sending data
if curl -s http://localhost:4000/health > /dev/null 2>&1; then
    print_status "PASS" "PMS Simulation service is healthy"
else
    print_status "FAIL" "PMS Simulation service is not responding"
fi

echo ""
echo "2️⃣  Checking Redis Stream"
echo "------------------------"

# Check Redis stream length
STREAM_LENGTH=$(docker exec redis redis-cli XLEN trades 2>/dev/null || echo "ERROR")
if [ "$STREAM_LENGTH" != "ERROR" ]; then
    print_status "INFO" "Redis stream 'trades' has $STREAM_LENGTH messages"

    # Show recent messages
    echo "   📊 Recent messages in Redis stream:"
    docker exec redis redis-cli XRANGE trades - + COUNT 3 2>/dev/null | head -10
else
    print_status "FAIL" "Cannot connect to Redis"
fi

echo ""
echo "3️⃣  Checking Database Persistence"
echo "--------------------------------"

# Check database connection and data
DB_STATUS=$(docker exec postgres psql -U pms-usr -d pms-db -c "SELECT COUNT(*) as total_events FROM outbox_trade;" -t 2>/dev/null || echo "ERROR")
if [ "$DB_STATUS" != "ERROR" ]; then
    TOTAL_EVENTS=$(echo $DB_STATUS | tr -d ' ')
    print_status "INFO" "Database has $TOTAL_EVENTS total events in outbox_trade"

    # Check status distribution
    STATUS_COUNTS=$(docker exec postgres psql -U pms-usr -d pms-db -c "SELECT status, COUNT(*) FROM outbox_trade GROUP BY status ORDER BY status;" -t 2>/dev/null)
    echo "   📊 Event status distribution:"
    echo "$STATUS_COUNTS" | while read -r line; do
        if [ ! -z "$line" ]; then
            echo "      $line"
        fi
    done

    # Show recent events
    echo "   📋 Recent events:"
    docker exec postgres psql -U pms-usr -d pms-db -c "SELECT id, symbol, side, status, created_at FROM outbox_trade ORDER BY created_at DESC LIMIT 3;" -t 2>/dev/null
else
    print_status "FAIL" "Cannot connect to database"
fi

echo ""
echo "4️⃣  Checking Kafka Topics"
echo "------------------------"

# Check Kafka topics
TOPICS=$(docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list 2>/dev/null || echo "ERROR")
if [ "$TOPICS" != "ERROR" ]; then
    if echo "$TOPICS" | grep -q "raw-trades"; then
        print_status "PASS" "Kafka topic 'raw-trades' exists"

        # Check topic details
        TOPIC_INFO=$(docker exec kafka kafka-topics --bootstrap-server localhost:9092 --topic raw-trades --describe 2>/dev/null)
        echo "   📊 Topic details:"
        echo "$TOPIC_INFO"
    else
        print_status "WARN" "Kafka topic 'raw-trades' does not exist yet"
    fi
else
    print_status "FAIL" "Cannot connect to Kafka"
fi

echo ""
echo "5️⃣  Checking Application Logs"
echo "----------------------------"

# Check for recent application logs (if container is running)
if docker-compose ps | grep -q "pms-ingestion.*Up"; then
    print_status "INFO" "Checking recent application logs..."

    # Get container logs from last 2 minutes
    LOGS=$(docker-compose logs --tail=20 pms-ingestion 2>/dev/null | grep -E "(RedisBatchConsumer|OutboxDispatcher|Received|Wrote|Published)" | tail -10)

    if [ ! -z "$LOGS" ]; then
        echo "   📜 Recent processing logs:"
        echo "$LOGS"
    else
        print_status "WARN" "No recent processing logs found"
    fi
else
    print_status "FAIL" "PMS Ingestion application is not running"
fi

echo ""
echo "======================================"
print_status "INFO" "Data flow check completed!"

echo ""
echo "💡 Expected Data Flow:"
echo "   WebSocket (pms-simulation:4000) → Redis Stream → Database → Kafka Topic"
echo ""
echo "🔧 Troubleshooting:"
echo "   - If Redis has messages but DB is empty: Check RedisBatchConsumer logs"
echo "   - If DB has PENDING events: Check OutboxDispatcher and Kafka connection"
echo "   - If Kafka topic is empty: Check Kafka producer configuration"