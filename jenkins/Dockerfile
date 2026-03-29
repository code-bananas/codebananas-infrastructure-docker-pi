FROM jenkins/jenkins:lts

USER root

# Install Docker CLI (deploy containers from pipeline) and Maven (fallback for projects without mvnw)
RUN apt-get update \
    && apt-get install -y ca-certificates curl gnupg maven \
    && install -m 0755 -d /etc/apt/keyrings \
    && curl -fsSL https://download.docker.com/linux/debian/gpg \
       | gpg --dearmor -o /etc/apt/keyrings/docker.gpg \
    && chmod a+r /etc/apt/keyrings/docker.gpg \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
       https://download.docker.com/linux/debian \
       $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
       > /etc/apt/sources.list.d/docker.list \
    && apt-get update \
    && apt-get install -y docker-ce-cli docker-compose-plugin \
    && rm -rf /var/lib/apt/lists/*

# Pre-install required Jenkins plugins
RUN jenkins-plugin-cli --plugins \
    git \
    workflow-aggregator \
    workflow-multibranch \
    pipeline-github-lib \
    github \
    github-branch-source \
    email-ext \
    junit \
    credentials-binding

USER jenkins
