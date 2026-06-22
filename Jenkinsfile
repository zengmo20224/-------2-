// PetCare O2O — Jenkins 声明式流水线
// 关联文档：docs/06-build-guide.md §3、docs/07-deployment-guide.md §6.1、jenkins/README.md
// CI：CI-CI-001
//
// 流水线阶段：Checkout → Backend Build → Backend Test → Backend Package
//           → Docker Build → Deploy → Health Check → Report & Email
//
// 触发方式：
//   - 手动 Build Now
//   - GitHub Webhook（push 事件）
//   - 轮询 SCM（默认开启，每 5 分钟）

pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    // 流水线级环境变量（节点级机密请在 Jenkins Credentials 中配置后引用）
    environment {
        MAVEN_OPTS      = '-Dmaven.repo.local=.m2-repo'
        IMAGE_TAG       = "${env.GIT_BRANCH ? env.GIT_BRANCH.replaceAll('[^a-zA-Z0-9.-]', '_') : 'local'}-${env.BUILD_NUMBER}"
        COMPOSE_PROJECT = 'petcare'
    }

    triggers {
        // 轮询兜底（建议同时配 GitHub Webhook）
        pollSCM('H/5 * * * *')
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                script {
                    // 记录 commit SHA 用于镜像标签追溯（CMP §5 基线可追溯）
                    env.GIT_COMMIT_SHORT = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    echo "Building commit: ${env.GIT_COMMIT_SHORT}"
                }
            }
        }

        stage('Backend Build') {
            steps {
                sh 'mvn -B -V -ntp clean compile'
            }
        }

        stage('Backend Test') {
            steps {
                // 跑单元测试 + H2 集成测试（默认排除 tc-mysql 分组，不依赖 Docker daemon 之外的 MySQL）
                sh 'mvn -B -ntp test'
            }
            post {
                always {
                    // 收集 JUnit 测试报告
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                    // 收集 JaCoCo 覆盖率报告（CI-BL-009）
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: 'target/classes',
                        sourcePattern: 'src/main/java',
                        exclusionPattern: '**/jsqlparser/**'
                    )
                }
            }
        }

        stage('Backend Package') {
            steps {
                sh 'mvn -B -ntp package -DskipTests'
            }
            post {
                success {
                    // 归档构建产物（CI-BL-011）
                    archiveArtifacts artifacts: 'target/petcare-o2o-api-*.jar',
                                     fingerprint: true,
                                     allowEmptyArchive: false
                }
            }
        }

        stage('Docker Build') {
            steps {
                // 构建全部镜像（后端 + 两个前端）
                sh 'docker compose build'
            }
        }

        stage('Deploy') {
            when {
                // 只在主分支或手动指定 DEPLOY=true 时部署
                anyOf {
                    branch 'main'
                    branch 'develop'
                    expression { env.DEPLOY == 'true' }
                }
            }
            steps {
                // 滚动重建并等待健康检查通过
                sh 'docker compose up -d --wait --remove-orphans'
                echo "Deployment complete. Services:"
                sh 'docker compose ps'
            }
        }

        stage('Health Check') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    expression { env.DEPLOY == 'true' }
                }
            }
            steps {
                script {
                    def attempts = 12
                    def ok = false
                    for (int i = 0; i < attempts; i++) {
                        sleep(time: 5, unit: 'SECONDS')
                        def status = sh(script: 'curl -fsS http://localhost:8080/api/v1/public/services || echo "PENDING"',
                                        returnStdout: true).trim()
                        echo "Health check attempt ${i + 1}/${attempts}: ${status}"
                        if (status != 'PENDING' && status != '') {
                            ok = true
                            break
                        }
                    }
                    if (!ok) {
                        error('后端健康检查失败，部署未在预期时间内就绪')
                    }
                    echo '系统已就绪：http://localhost:8080 (admin) / http://localhost:8081 (h5)'
                }
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline SUCCESS: 编译 / 测试 / 打包 / 部署全部通过'
        }
        failure {
            echo '❌ Pipeline FAILED — 请查看上方日志定位失败阶段'
        }
        always {
            // 归档覆盖率报告 HTML（CI-BL-009）供审计回溯
            publishHTML(target: [
                reportDir: 'target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'JaCoCo Coverage Report',
                keepAll: false,
                allowMissing: true,
                alwaysLinkToLastBuild: true
            ])
            // 发送测试报告邮件给组员（加分项 — 课程明确点名）
            emailext(
                subject: "[PetCare] ${currentBuild.currentResult}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    <h2>PetCare O2O 构建报告</h2>
                    <p><b>构建状态：</b>${currentBuild.currentResult}</p>
                    <p><b>提交：</b>${env.GIT_COMMIT_SHORT ?: 'N/A'}</p>
                    <p><b>分支：</b>${env.GIT_BRANCH ?: 'N/A'}</p>
                    <p><b>持续时间：</b>${currentBuild.durationString}</p>
                    <p><b>构建链接：</b><a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                    <p>详细测试覆盖率报告见 Jenkins 构建页面左侧 "JaCoCo Coverage Report"。</p>
                """.stripIndent(),
                to: "${env.TEAM_EMAIL ?: ''}",
                attachmentsPattern: 'target/site/jacoco/index.html'
            )
            // 清理工作区（可选，保留 .m2-repo 缓存）
            sh 'docker compose logs --tail=100 || true'
        }
    }
}
