# Docker Infrastructure

Shared docker-compose files for local services.

Tracked:
- docker-compose.yml
- README.md

Everything else is intentionally ignored.

To get minio client
curl -O https://dl.min.io/client/mc/release/linux-arm64/mc
uname -m

aarch64 → arm64
armv7l → arm

chmod +x mc
sudo mv mc /usr/local/bin/

mc alias set local http://localhost:9000 minioadmin minioadmin
mc ls local -- to test

mc mb local/mb-articles-dev
mc mb local/mb-articles-prod

mc admin user svcacct add local minioadmin --name marketbot

-- To update a policy
mc admin policy create local marketbot-local-policy marketbot-local-policy.json
mc admin policy create local marketbot-dev-policy marketbot-dev-policy.json
mc admin policy create local marketbot-prod-policy marketbot-prod-policy.json

mc admin policy create local marketbot-dev-policy marketbot-dev-policy.json
mc admin policy create local marketbot-prod-policy marketbot-prod-policy.json

mc admin user add local marketbot-local-user
mc admin user add local marketbot-dev-user
mc admin user add local marketbot-prod-user

mc admin policy attach local marketbot-local-policy --user marketbot-local-user
mc admin policy attach local marketbot-dev-policy --user marketbot-dev-user
mc admin policy attach local marketbot-prod-policy --user marketbot-prod-user

mc admin user svcacct add local marketbot-local-user --name marketbot-local --policy marketbot-local-policy.json
mc admin user svcacct add local marketbot-dev-user --name marketbot-dev --policy marketbot-dev-policy.json
mc admin user svcacct add local marketbot-prod-user --name marketbot-prod --policy marketbot-prod-policy.json

--- Java install

curl -s "https://get.sdkman.io" | bash
source ~/.sdkman/bin/sdkman-init.sh
sdk install java 17.0.10-tem

-- for pihole nginx
docker network create proxy

