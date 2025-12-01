#!/bin/bash

# PMS Ingestion - Monitor Data Flow
# This script continuously monitors the data flow metrics

echo "📊 Monitoring PMS Ingestion Data Flow (Ctrl+C to stop)"
echo "======================================================"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Function to get metrics
get_metrics() {
    echo "📅 $(date '+%Y-%m-%d %H:%M:%S')"
    echo "─────────────────────────────────────────────────────"

    # Redis metrics
    REDIS_LENGTH=$(docker exec redis redis-cli XLEN trades 2>/dev/null || echo "ERROR")
    if [ "$REDIS_LENGTH" != "ERROR" ]; then
        echo -e "${BLUE}🔴 Redis Stream:${NC} $REDIS_LENGTH messages pending"
    else
        echo -e "${RED}🔴 Redis:${NC} Connection failed"
    fi

    # Database metrics
    DB_METRICS=$(docker exec postgres psql -U pms-usr -d pms-db -c "
        SELECT
            COUNT(*) as total,
            COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending,
            COUNT(CASE WHEN status = 'SENT' THEN 1 END) as sent
        FROM outbox_trade;" -t 2>/dev/null || echo "ERROR")

    if [ "$DB_METRICS" != "ERROR" ]; then
        TOTAL=$(echo $DB_METRICS | awk '{print $1}')
        PENDING=$(echo $DB_METRICS | awk '{print $2}')
        SENT=$(echo $DB_METRICS | awk '{print $3}')

        echo -e "${BLUE}🗄️  Database:${NC} $TOTAL total events"
        echo -e "   ${YELLOW}⏳ PENDING:${NC} $PENDING"
        echo -e "   ${GREEN}✅ SENT:${NC} $SENT"
    else
        echo -e "${RED}🗄️  Database:${NC} Connection failed"
    fi

    # Kafka metrics
    KAFKA_OFFSETS=$(docker exec kafka kafka-run-class kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic raw-trades --time -1 2>/dev/null || echo "ERROR")
    if [ "$KAFKA_OFFSETS" != "ERROR" ] && [ ! -z "$KAFKA_OFFSETS" ]; then
        # Parse the offset information
        PARTITION_OFFSET=$(echo $KAFKA_OFFSETS | grep -o '[0-9]*:[0-9]*' | tail -1)
        if [ ! -z "$PARTITION_OFFSET" ]; then
            OFFSET=$(echo $PARTITION_OFFSET | cut -d':' -f2)
            echo -e "${BLUE}📨 Kafka Topic:${NC} $OFFSET messages published"
        else
            echo -e "${BLUE}📨 Kafka Topic:${NC} 0 messages"
        fi
    else
        echo -e "${RED}📨 Kafka:${NC} Connection failed or topic empty"
    fi

    # Application health
    if curl -f -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}🚀 Application:${NC} Healthy"
    else
        echo -e "${RED}🚀 Application:${NC} Unhealthy"
    fi

    echo ""
}

# Clear screen and show initial metrics
clear
echo "🔄 PMS Data Flow Monitor"
echo "========================"
echo ""

# Main monitoring loop
while true; do
    get_metrics
    sleep 5
done