#!/bin/bash
set -euo pipefail

: "${MARKETBOT_DB_NAME:?MARKETBOT_DB_NAME is not set}"
: "${MARKETBOT_DB_PASSWORD:?MARKETBOT_DB_PASSWORD is not set}"

echo "Initializing database: $MARKETBOT_DB_NAME"

export PGPASSWORD="$POSTGRES_PASSWORD"

psql -v ON_ERROR_STOP=1 -U admin -d postgres <<EOF
-- Create application user
CREATE USER marketbot WITH PASSWORD '${MARKETBOT_DB_PASSWORD}';

-- Create application database
CREATE DATABASE "${MARKETBOT_DB_NAME}"
  OWNER marketbot
  ENCODING = 'UTF8';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE "${MARKETBOT_DB_NAME}" TO marketbot;

-- Connect and grant schema rights
\c "${MARKETBOT_DB_NAME}"

GRANT ALL ON SCHEMA public TO marketbot;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT ALL ON TABLES TO marketbot;
EOF
