#!/bin/bash

# PMS Ingestion - View Service Logs
# This script shows logs from all services or a specific service

show_help() {
    echo "Usage: $0 [service-name] [options]"
    echo ""
    echo "Services:"
    echo "  pms-simulation    - WebSocket simulation service"
    echo "  pms-ingestion     - Main ingestion application"
    echo "  postgres          - PostgreSQL database"
    echo "  redis             - Redis cache/stream"
    echo "  kafka             - Kafka message broker"
    echo "  all               - Show logs from all services"
    echo ""
    echo "Options:"
    echo "  -f, --follow      - Follow log output (like tail -f)"
    echo "  -n, --lines N     - Show last N lines (default: 50)"
    echo "  --since TIME      - Show logs since timestamp (e.g., '1h', '30m')"
    echo "  -h, --help        - Show this help"
    echo ""
    echo "Examples:"
    echo "  $0 pms-ingestion -f                    # Follow ingestion logs"
    echo "  $0 redis --since 10m                   # Redis logs from last 10 minutes"
    echo "  $0 all -n 20                           # Last 20 lines from all services"
}

# Default values
SERVICE="all"
FOLLOW=""
LINES="50"
SINCE=""

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -f|--follow)
            FOLLOW="--follow"
            shift
            ;;
        -n|--lines)
            LINES="$2"
            shift 2
            ;;
        --since)
            SINCE="--since $2"
            shift 2
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            if [ "$SERVICE" = "all" ]; then
                SERVICE="$1"
            else
                echo "Error: Multiple services specified. Use 'all' or specify one service."
                exit 1
            fi
            shift
            ;;
    esac
done

# Change to project directory
cd "$(dirname "$0")/.."

echo "📜 Viewing logs for service: $SERVICE"
echo "==================================="

# Show logs based on service selection
case $SERVICE in
    "pms-simulation")
        docker-compose logs pms-simulation --tail="$LINES" $FOLLOW $SINCE
        ;;
    "pms-ingestion")
        docker-compose logs pms-ingestion --tail="$LINES" $FOLLOW $SINCE
        ;;
    "postgres")
        docker-compose logs postgres --tail="$LINES" $FOLLOW $SINCE
        ;;
    "redis")
        docker-compose logs redis --tail="$LINES" $FOLLOW $SINCE
        ;;
    "kafka")
        docker-compose logs kafka --tail="$LINES" $FOLLOW $SINCE
        ;;
    "all")
        docker-compose logs --tail="$LINES" $FOLLOW $SINCE
        ;;
    *)
        echo "❌ Unknown service: $SERVICE"
        echo ""
        show_help
        exit 1
        ;;
esac