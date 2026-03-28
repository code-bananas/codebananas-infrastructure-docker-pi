#!/bin/bash

# Run marketbot-postgresdb dev environment
# PostgreSQL on port 5433
# pgAdmin on port 5051

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

docker compose --project-name marketbot-db-local --env-file "$SCRIPT_DIR/.env.local" -f "$SCRIPT_DIR/docker-compose.yml" -f "$SCRIPT_DIR/docker-compose.local.yml" up -d

echo "Local database environment started:"
echo "  PostgreSQL: localhost:5432"
echo "  pgAdmin: localhost:5050"