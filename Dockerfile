# 后端镜像 — 多阶段构建
# 关联文档：docs/06-build-guide.md §5.1、docs/07-deployment-guide.md
# CI：CI-BS-008

# ---------- 阶段 1：构建 ----------
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

# 先复制 pom，利用 Docker 层缓存加速依赖下载
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# 复制源码并打包（跳过测试，测试在 CI 流水线单独阶段运行）
COPY src ./src
RUN mvn -B -q clean package -DskipTests \
    && mv target/petcare-o2o-api-*.jar /workspace/app.jar

# ---------- 阶段 2：运行 ----------
FROM eclipse-temurin:17-jre AS runtime

LABEL org.opencontainers.image.title="petcare-o2o-api" \
      org.opencontainers.image.description="PetCare O2O 后端 API" \
      org.opencontainers.image.source="https://github.com/zengmo20224/-------2-"

WORKDIR /app

# 安装 curl 用于 HEALTHCHECK（eclipse-temurin:17-jre 不自带）
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# 创建非 root 用户运行应用
RUN groupadd -r petcare && useradd -r -g petcare -d /app -s /sbin/nologin petcare

# 拷贝构建产物
COPY --from=builder /workspace/app.jar /app/app.jar

# 健康检查：用 TCP 端口探测（不依赖 actuator 依赖）
# 启动 Spring Boot 后 8080 端口监听即视为就绪
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD curl -fsS http://localhost:8080/ || exit 1

USER petcare

EXPOSE 8080

# 生产 profile 由环境变量驱动，所有敏感配置通过环境变量注入
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
