@echo off
echo Building and running PMS Ingestion in Docker...

echo.
echo 1. Building Docker image...
docker-compose build pms-ingestion

echo.
echo 2. Starting all services...
docker-compose up -d

echo.
echo 3. Checking service status...
docker-compose ps

echo.
echo 4. Following logs (Ctrl+C to stop)...
docker-compose logs -f pms-ingestion