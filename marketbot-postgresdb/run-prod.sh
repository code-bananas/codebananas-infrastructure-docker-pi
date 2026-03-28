#!/bin/bash

# Run marketbot-postgresdb prod environment
# PostgreSQL on port 5434
# pgAdmin on port 5052

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

docker compose --project-name marketbot-db-prod --env-file "$SCRIPT_DIR/.env.prod" -f "$SCRIPT_DIR/docker-compose.yml" -f "$SCRIPT_DIR/docker-compose.prod.yml" up -d

echo "Prod database environment started:"
echo "  PostgreSQL: localhost:5434"
echo "  pgAdmin: localhost:5052"
