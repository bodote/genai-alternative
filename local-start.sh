#!/bin/bash
# Start the local development environment (PostgreSQL + Spring Boot app)

set -e

echo "🚀 Starting local development environment..."

# Start PostgreSQL container
echo "📦 Starting PostgreSQL container..."
docker-compose -f docker-compose.local.yaml up -d

# Wait for PostgreSQL to be ready
echo "⏳ Waiting for PostgreSQL to be ready..."
until docker exec sherlock-postgres pg_isready -U sherlock -d sherlock_db > /dev/null 2>&1; do
  sleep 1
done
echo "✅ PostgreSQL is ready"

# Start Spring Boot application
echo "🌱 Starting Spring Boot application..."
 ./gradlew bootRun & 

