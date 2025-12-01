# PMS Ingestion - Docker Setup and Data Flow Scripts

This document lists all available shell scripts for managing the PMS ingestion pipeline with Docker.

## 📋 Available Scripts

### 🎬 Demo Workflow

#### `demo-workflow.sh`
Complete demonstration of the PMS ingestion workflow.
```bash
./scripts/demo-workflow.sh
```
**What it does:**
- Starts all services
- Checks service health
- Waits for data flow
- Verifies complete pipeline
- Shows next steps

### 🚀 Service Management

#### `start-services.sh`
Starts all Docker services for the PMS ingestion pipeline.
```bash
./scripts/start-services.sh
```
**What it does:**
- Starts all services (pms-simulation, pms-ingestion, postgres, redis, kafka)
- Waits for services to be healthy
- Shows service status and URLs

### 🏥 Health Monitoring

#### `check-health.sh`
Checks the health status of all services.
```bash
./scripts/check-health.sh
```
**What it checks:**
- Service running status
- Application health endpoints
- Database connectivity
- Redis connectivity
- Kafka connectivity

### 🔄 Data Flow Verification

#### `check-data-flow.sh`
Comprehensive check of the complete data flow pipeline.
```bash
./scripts/check-data-flow.sh
```
**Data Flow Checked:**
1. **WebSocket Connection** - Simulation service health
2. **Redis Stream** - Message count and recent entries
3. **Database Persistence** - Event counts and status distribution
4. **Kafka Topics** - Topic existence and message counts
5. **Application Logs** - Recent processing activity

**Expected Flow:**
```
WebSocket (pms-simulation:4000) → Redis Stream → Database → Kafka Topic
```

### 📊 Real-time Monitoring

#### `monitor-flow.sh`
Continuous monitoring of data flow metrics.
```bash
./scripts/monitor-flow.sh
```
**Monitors:**
- Redis stream message count
- Database event counts (total, pending, sent)
- Kafka topic message count
- Application health status

**Features:**
- Updates every 5 seconds
- Color-coded status indicators
- Press Ctrl+C to stop

### 📜 Log Management

#### `view-logs.sh`
View logs from services with flexible options.
```bash
# View logs from specific service
./scripts/view-logs.sh pms-ingestion

# Follow logs in real-time
./scripts/view-logs.sh pms-ingestion --follow

# View logs from last hour
./scripts/view-logs.sh all --since 1h

# View last 20 lines from all services
./scripts/view-logs.sh all --lines 20
```
**Supported Services:**
- `pms-simulation` - WebSocket simulation service
- `pms-ingestion` - Main ingestion application
- `postgres` - PostgreSQL database
- `redis` - Redis cache/stream
- `kafka` - Kafka message broker
- `all` - All services

**Options:**
- `-f, --follow` - Follow log output (tail -f)
- `-n, --lines N` - Show last N lines (default: 50)
- `--since TIME` - Show logs since timestamp (e.g., '1h', '30m')

### 🧹 Cleanup

#### `cleanup.sh`
Stops and cleans up all Docker services and resources.
```bash
./scripts/cleanup.sh
```
**Actions:**
- Stops all services
- Optionally removes data volumes
- Cleans up dangling Docker images
- Shows disk usage

## 🏗️ Docker Configuration

### Dockerfile
Multi-stage build for the Spring Boot application:
- **Build Stage:** Maven compilation with Java 17
- **Runtime Stage:** OpenJDK 17 JRE with health checks

### docker-compose.yml
Complete orchestration with:
- **pms-simulation:** WebSocket trade simulator (port 4000)
- **pms-ingestion:** Main application with Docker profile (port 8081)
- **postgres:** Database with timezone configuration
- **redis:** Stream storage (port 6379)
- **kafka:** Message broker (port 9092)

### Application Profiles
- **application.yaml:** Local development configuration
- **application-docker.yaml:** Docker environment configuration

## 🔧 Usage Workflow

### Quick Start (Recommended)
```bash
# Run the complete demo workflow
./scripts/demo-workflow.sh
```

### Manual Setup
```bash
# 1. Start all services
./scripts/start-services.sh

# 2. Check service health
./scripts/check-health.sh

# 3. Monitor data flow in real-time
./scripts/monitor-flow.sh
```

### Troubleshooting
```bash
# Check complete data flow
./scripts/check-data-flow.sh

# View application logs
./scripts/view-logs.sh pms-ingestion --follow

# View database logs
./scripts/view-logs.sh postgres --since 10m

# Check all service logs
./scripts/view-logs.sh all --lines 100
```

### Maintenance
```bash
# Clean up resources
./scripts/cleanup.sh

# Restart services
./scripts/start-services.sh
```

## 📊 Data Flow Architecture

```
┌─────────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  WebSocket      │ -> │   Redis     │ -> │  Database   │ -> │   Kafka     │
│  Simulator      │    │   Stream    │    │  (Outbox)   │    │   Topic     │
│  (Port 4000)    │    │  (Port 6379)│    │  (Port 5432)│    │  (Port 9092)│
└─────────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        │                       │                │                │
        └─ Trade Events ────────┼────────────────┼────────────────┘
                                └─ Batch Consumer┼─ Outbox Dispatcher
                                                 └─ Event Persistence
```

## 🚨 Troubleshooting Guide

### Common Issues

#### Redis has messages but database is empty
- **Cause:** RedisBatchConsumer not running or @Service annotation missing
- **Check:** `./scripts/view-logs.sh pms-ingestion | grep RedisBatchConsumer`

#### Database has PENDING events
- **Cause:** OutboxDispatcher failing to publish to Kafka
- **Check:** `./scripts/view-logs.sh pms-ingestion | grep OutboxDispatcher`

#### Kafka topic is empty
- **Cause:** Kafka producer configuration or connectivity issues
- **Check:** `./scripts/view-logs.sh kafka` and `./scripts/check-health.sh`

#### Services not starting
- **Cause:** Port conflicts or resource issues
- **Check:** `docker-compose ps` and `docker-compose logs`

### Quick Diagnostics
```bash
# Complete health check
./scripts/check-health.sh

# Data flow verification
./scripts/check-data-flow.sh

# Real-time monitoring
./scripts/monitor-flow.sh
```

## 📁 File Structure
```
pms-ingestion/
├── Dockerfile                    # Multi-stage build
├── docker-compose.yml           # Service orchestration
├── scripts/
│   ├── demo-workflow.sh        # Complete workflow demo
│   ├── start-services.sh       # Start all services
│   ├── check-health.sh         # Health monitoring
│   ├── check-data-flow.sh      # Data flow verification
│   ├── monitor-flow.sh         # Real-time monitoring
│   ├── view-logs.sh           # Log management
│   └── cleanup.sh             # Resource cleanup
└── src/main/resources/
    ├── application.yaml        # Local config
    └── application-docker.yaml # Docker config
```