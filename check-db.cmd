@echo off
echo Checking database data...

echo.
echo Running database check from container...
docker-compose exec pms-ingestion java -cp /app/app.jar com.pms.ingestion.util.DatabaseDataChecker

echo.
echo Done!