# Infrastructure Docker Pi

Docker Compose infrastructure running on a Raspberry Pi (ARM64) for the CodeBananas project. Each subdirectory is an independent service stack.

## Services

### cloudfare-ddns/
Cloudflare Dynamic DNS updater using `favonia/cloudflare-ddns`. Automatically keeps DNS records for `andrewlester.dev`, `codebananas.com`, and `codebananas.dev` (and their `www`/`api` subdomains) pointed at the host's current public IP. Runs with `network_mode: host`. IPv6 disabled.

### jenkins/
Jenkins LTS CI/CD server. Web UI on port `8085`, agent communication on `50000`. Persistent data in `./jenkins_home`. Setup wizard disabled.

### minio/
MinIO S3-compatible object storage. S3 API on port `9000`, web console on `9001`. Serves file/article storage for multiple applications (marketbot, newsbot) across three environment tiers (local/dev/prod). Policy JSON files in this directory define per-app, per-environment IAM bucket policies.

### nginx-proxy-manager/
Nginx Proxy Manager (`jc21/nginx-proxy-manager`) — reverse proxy with Let's Encrypt SSL. HTTP on `80`, admin UI on `81`, HTTPS on `443`. Uses an external Docker network named `proxy` to communicate with other services. SSL certs persisted in `./letsencrypt`, config in `./data`.

### observability/
Three-service logging and monitoring stack:
- **Loki** (port `3100`) — log aggregation
- **Promtail** — ships Docker container logs to Loki (reads `/var/lib/docker/containers` and the Docker socket)
- **Grafana** (port `3000`) — visualization/dashboards, connected to Loki; uses external `proxy` network

Config templates live in `./grafana/provisioning`. Loki config is created at runtime (not in repo).

### ollama-webui/
Local LLM stack:
- **ollama** (port `11434`) — inference engine, models stored at `/srv/docker/ollama-webui/ollama`
- **webui** (port `9005`) — Open WebUI frontend pointing at the ollama service

Runs fully offline; no external AI API calls.

### pihole/
Pi-hole network-wide DNS ad-blocker and optional DHCP server. Runs with `network_mode: host`. Web UI on port `8888`. Upstream DNS: Cloudflare (1.1.1.1) and Google (8.8.8.8). Config persisted in `./etc-pihole` and `./etc-dnsmasq.d`.

### sqlite-web-dev/
Development-only SQLite browser (`sqrt3/sqlite-web`). Web UI on port `8100`, pointed at the marketbot dev database at `/srv/docker/market_bot/dev/stock_data.db`.

### wireguard/
WireGuard VPN server (`linuxserver/wireguard`). UDP port `51820`. Configured for `codebananas.com` as the endpoint with full-tunnel routing (`0.0.0.0/0`). Client configs auto-generated in `./config`. Requires kernel module access and IP forwarding sysctls.

### marketbot-postgresdb/
PostgreSQL 16 + pgAdmin stack for the marketbot application. Supports three environments via override files:

| Environment | Compose file | PG port | pgAdmin port |
|---|---|---|---|
| local | `docker-compose.local.yml` | 5432 | 5050 |
| dev | `docker-compose.dev.yml` | 5432 | 5050 |
| prod | `docker-compose.prod.yml` | 5434 | 5052 |

Use the `run-*.sh` / `stop-*.sh` / `remove-*.sh` scripts to manage each environment. Secrets come from `.env` files (not in repo). The `init/01-init.sh` script creates the marketbot DB user and database on first startup.

## Networking

Most services that need to be reverse-proxied connect to an external Docker network named `proxy` (created separately). PiHole and Cloudflare DDNS use `network_mode: host`.

## Port Reference

| Service | Port |
|---|---|
| Grafana | 3000 |
| Loki | 3100 |
| pgAdmin (local/dev) | 5050 |
| pgAdmin (prod) | 5052 |
| PostgreSQL (local/dev) | 5432 |
| PostgreSQL (prod) | 5434 |
| WireGuard | 51820/udp |
| PiHole UI | 8888 |
| Nginx admin | 81 |
| SQLite Web | 8100 |
| Jenkins | 8085 |
| MinIO S3 API | 9000 |
| MinIO Console | 9001 |
| Ollama WebUI | 9005 |
| Ollama API | 11434 |
