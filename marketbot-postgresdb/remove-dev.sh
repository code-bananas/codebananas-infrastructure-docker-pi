#!/bin/bash

# Run marketbot-postgresdb dev environment
# PostgreSQL on port 5433
# pgAdmin on port 5051

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

docker compose --project-name marketbot-db-dev --env-file "$SCRIPT_DIR/.env.dev" -f "$SCRIPT_DIR/docker-compose.yml" -f "$SCRIPT_DIR/docker-compose.dev.yml" down -v

echo "Dev database environment removed"
