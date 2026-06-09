#!/bin/sh
# Wait script for Gateway to ensure backend services are ready

echo "Checking backend services..."

# Wait for MySQL only (fastest check)
until nc -z mysql 3306 2>/dev/null; do
  echo "Waiting for MySQL..."
  sleep 2
done

echo "MySQL ready, starting Gateway..."
exec java -jar /app/app.jar
