#!/bin/bash

# PMS Ingestion - Demo Workflow
# This script demonstrates the complete PMS ingestion workflow

set -e

echo "🎬 PMS Ingestion Demo Workflow"
echo "=============================="
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

cd "$(dirname "$0")/.."

echo -e "${BLUE}Step 1: Starting all services...${NC}"
./scripts/start-services.sh

echo ""
echo -e "${BLUE}Step 2: Checking service health...${NC}"
./scripts/check-health.sh

echo ""
echo -e "${YELLOW}Step 3: Waiting for data to flow (30 seconds)...${NC}"
sleep 30

echo ""
echo -e "${BLUE}Step 4: Checking data flow...${NC}"
./scripts/check-data-flow.sh

echo ""
echo -e "${GREEN}🎉 Demo completed!${NC}"
echo ""
echo "💡 Next steps:"
echo "   • Run './scripts/monitor-flow.sh' for real-time monitoring"
echo "   • Run './scripts/view-logs.sh pms-ingestion --follow' to watch logs"
echo "   • Run './scripts/cleanup.sh' to stop all services"