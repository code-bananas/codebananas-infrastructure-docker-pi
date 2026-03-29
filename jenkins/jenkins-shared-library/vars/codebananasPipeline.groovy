/**
 * Reusable pipeline for CodeBananas Spring Boot services.
 *
 * Branch routing:
 *   develop → dev environment
 *   master / main → prod environment
 *   all other branches → build + test only, no deployment
 *
 * Usage in project Jenkinsfile:
 *
 *   @Library('codebananas-shared-library') _
 *   codebananasPipeline(
 *       tests:       true,           // run tests (default: true)
 *       deployDev:   true,           // deploy on develop branch (default: true)
 *       deployProd:  true,           // deploy on master/main branch (default: true)
 *       notifyEmail: 'you@example.com', // failure email (default: none)
 *   )
 */
def call(Map config = [:]) {
    def projectName  = (config.projectName ?: env.JOB_BASE_NAME).toLowerCase().replaceAll(/[^a-z0-9-]/, '-')
    def runTests     = config.containsKey('tests')      ? config.tests      : true
    def deployToDev  = config.containsKey('deployDev')  ? config.deployDev  : true
    def deployToProd = config.containsKey('deployProd') ? config.deployProd : true
    def notifyEmail  = config.notifyEmail ?: ''

    pipeline {
        agent any

        options {
            timestamps()
            disableConcurrentBuilds()
            buildDiscarder(logRotator(numToKeepStr: '10'))
        }

        stages {
            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Test') {
                when {
                    expression { return runTests }
                }
                steps {
                    script {
                        if (fileExists('mvnw')) {
                            sh 'chmod +x mvnw && ./mvnw clean test'
                        } else if (fileExists('gradlew')) {
                            sh 'chmod +x gradlew && ./gradlew test'
                        } else {
                            sh 'mvn clean test'
                        }
                    }
                }
                post {
                    always {
                        junit(
                            allowEmptyResults: true,
                            testResults: '**/target/surefire-reports/*.xml,**/build/test-results/**/*.xml'
                        )
                    }
                }
            }

            stage('Build') {
                steps {
                    script {
                        if (fileExists('mvnw')) {
                            sh 'chmod +x mvnw && ./mvnw clean package -DskipTests'
                        } else if (fileExists('gradlew')) {
                            sh 'chmod +x gradlew && ./gradlew bootJar -x test'
                        } else {
                            sh 'mvn clean package -DskipTests'
                        }
                        // Tag with both build number (immutable) and branch name (floating)
                        sh "docker build -t ${projectName}:${env.BUILD_NUMBER} -t ${projectName}:${env.BRANCH_NAME} ."
                    }
                }
            }

            stage('Deploy → dev') {
                when {
                    allOf {
                        expression { return deployToDev }
                        branch 'develop'
                    }
                }
                steps {
                    script {
                        deployApp(projectName, 'dev')
                    }
                }
            }

            stage('Deploy → prod') {
                when {
                    allOf {
                        expression { return deployToProd }
                        anyOf { branch 'master'; branch 'main' }
                    }
                }
                steps {
                    script {
                        deployApp(projectName, 'prod')
                    }
                }
            }
        }

        post {
            always {
                cleanWs()
            }
            failure {
                script {
                    if (notifyEmail) {
                        emailext(
                            subject: "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER} [${env.BRANCH_NAME}]",
                            mimeType: 'text/html',
                            to: notifyEmail,
                            body: """
                                <h3 style="color:red">Pipeline Failed</h3>
                                <table>
                                  <tr><td><b>Job</b></td><td>${env.JOB_NAME}</td></tr>
                                  <tr><td><b>Build</b></td><td>#${env.BUILD_NUMBER}</td></tr>
                                  <tr><td><b>Branch</b></td><td>${env.BRANCH_NAME}</td></tr>
                                  <tr><td><b>Commit</b></td><td>${env.GIT_COMMIT?.take(8)}</td></tr>
                                </table>
                                <p><a href="${env.BUILD_URL}console">View Console Output</a></p>
                            """
                        )
                    } else {
                        echo 'Email not configured — add notifyEmail to Jenkinsfile to enable failure alerts'
                    }
                }
            }
        }
    }
}

/**
 * Copies the project's docker-compose.yml + env file to the deployment directory
 * on the host and restarts the service with the freshly-built image.
 *
 * Deployment layout on the Pi:
 *   /srv/docker/application/<project>/
 *     docker-compose.yml    ← copied from repo
 *     .env                  ← copied from .env.<deployEnv> in repo
 */
def deployApp(String projectName, String deployEnv) {
    def deployDir = "/srv/docker/application/${projectName}"
    sh """
        set -e
        mkdir -p ${deployDir}

        cp docker-compose.yml ${deployDir}/docker-compose.yml

        if [ -f .env.${deployEnv} ]; then
            cp .env.${deployEnv} ${deployDir}/.env
        fi

        cd ${deployDir}
        IMAGE_TAG=${env.BUILD_NUMBER} PROJECT_NAME=${projectName} DEPLOY_ENV=${deployEnv} \\
            docker compose up -d --pull never --remove-orphans
    """
}
