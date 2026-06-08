# 阶段 2：后端骨架编程启动说明

日期：2026-06-08

负责人：GLM5.1

当前分支：`phase-2-backend-skeleton`

前置条件：阶段 1 数据库 Schema 已完成并由用户 review 通过，提交为 `52315d8 feat: add validated mysql schema`。

## 目标

本阶段只建立 Spring Boot 3 后端工程骨架，让项目可以启动、连接 MySQL、具备统一响应与统一异常处理，并为后续实体、Mapper、认证、预约、社区、商品、AI 模块开发提供稳定基础。

不要在本阶段实现完整业务功能。

## 必须先阅读

GLM5.1 开始编码前必须阅读：

1. `AGENTS.md`
2. `schema.sql`
3. `docs/01-architecture-design.md`
4. `docs/03-glm5-implementation-plan.md`
5. `docs/04-code-standards.md`
6. `docs/05-testing-and-verification.md`
7. `docs/07-integration-gates.md`
8. `docs/08-pending-decisions.md`
9. `docs/09-booking-concurrency-control.md`
10. `docs/10-admin-permission-design.md`
11. 本文件

编码前必须先输出：

```text
已阅读文档：
当前阶段：阶段 2 后端骨架
当前分支：phase-2-backend-skeleton
计划修改范围：
不会修改的范围：
待决策或阻塞项：
验证计划：
```

## 阶段 2 允许做的事

允许新增：

- Maven Spring Boot 3 工程
- `pom.xml`
- `src/main/java/.../PetCareApplication.java`
- `src/main/resources/application.yml`
- `src/main/resources/application-dev.yml`
- `src/main/resources/application-test.yml`
- `.env.example`
- `common` 基础包
- 全局响应结构
- 全局异常处理
- 基础错误码
- 基础分页对象
- MyBatis-Plus 配置
- 数据源环境变量配置
- 健康检查接口
- 最小 Spring Security 配置骨架
- 基础测试和启动测试

允许新增包结构：

```text
src/main/java/.../petcare/
  PetCareApplication.java
  common/
    api/
    config/
    exception/
    pagination/
    validation/
  user/
  store/
  service/
  staff/
  booking/
  community/
  moderation/
  product/
  marketing/
  ai/
  admin/
```

业务模块目录可以先放 `.gitkeep` 或 package-info，避免写空业务实现。

## 阶段 2 禁止做的事

本阶段禁止：

- 不生成全部实体类、Mapper、Service、Controller。
- 不实现管理员登录和 JWT 签发。
- 不实现微信登录。
- 不实现预约创建、排班计算、内容审核、商品订单、AI 调用。
- 不引入 Redis。
- 不引入 Flyway 或 Liquibase。
- 不修改 `schema.sql`，除非发现阶段 1 阻塞级错误并先征得用户确认。
- 不新增前端工程。
- 不硬编码数据库密码、JWT 密钥、DeepSeek Key。

## 技术约束

后端必须使用：

- Java 17 或更高版本
- Spring Boot 3
- Maven
- MyBatis-Plus
- Spring Security + JWT 依赖预留
- MySQL Driver
- Bean Validation
- JUnit 5
- Spring Boot Test
- JaCoCo 覆盖率插件

不要使用：

- Sa-Token
- Redis
- Flyway
- Liquibase
- 在线支付 SDK
- 真实微信 SDK
- 真实 DeepSeek 调用代码

## Maven 要求

`pom.xml` 必须至少包含：

- `spring-boot-starter-web`
- `spring-boot-starter-validation`
- `spring-boot-starter-security`
- `mybatis-plus-spring-boot3-starter`
- `mysql-connector-j`
- `spring-boot-starter-test`
- JaCoCo Maven 插件

JWT 依赖可以加入，但本阶段只做配置预留，不实现完整登录签发流程。

如果引入其他依赖，必须说明用途，不能为未来可能用到的功能提前堆依赖。

## 配置要求

`application.yml` 不允许出现真实密码或密钥。

建议配置形式：

```yaml
spring:
  application:
    name: petcare-o2o-api
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/petcare_o2o?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  global-config:
    db-config:
      id-type: assign_id
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

petcare:
  security:
    jwt-secret: ${JWT_SECRET:}
  ai:
    deepseek:
      base-url: ${DEEPSEEK_BASE_URL:https://api.deepseek.com}
      api-key: ${DEEPSEEK_API_KEY:}
```

要求：

- `JWT_SECRET` 为空时，不能在生产 profile 启动。
- `DEEPSEEK_API_KEY` 可以为空，因为阶段 2 不真实调用 AI。
- 本阶段不要写真实密钥。

## API 骨架要求

统一响应格式：

```json
{
  "success": true,
  "data": {},
  "error": null,
  "meta": {}
}
```

错误响应格式：

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "validation_error",
    "message": "请求参数不合法",
    "details": []
  },
  "meta": null
}
```

必须实现一个健康检查接口：

```text
GET /api/v1/system/health
```

返回：

```json
{
  "success": true,
  "data": {
    "status": "UP"
  },
  "error": null,
  "meta": null
}
```

健康检查接口不需要认证。

## 安全配置边界

本阶段只做最小安全骨架：

- 引入 Spring Security。
- 明确允许 `/api/v1/system/health`。
- 后续业务 API 默认要求认证或先不暴露。
- 不实现真实管理员登录。
- 不生成默认用户密码。
- 不记录 Token、密码、JWT secret。

阶段 4 才实现：

- 管理员登录
- JWT 签发和校验
- 权限码加载
- `@PreAuthorize`

## 数据库连接要求

阶段 2 只验证应用能连接数据库，不做完整业务读写。

可以新增一个启动或健康检查测试，确认：

- Spring Context 可启动。
- 数据源配置可加载。
- MyBatis-Plus 配置存在。

如果本地没有 MySQL，必须在交接报告中明确说明未执行数据库连接验证，并给出用户可运行命令。

## 测试要求

必须至少新增：

- Spring Context 启动测试
- 健康检查 MockMvc 测试
- 统一异常处理测试
- 配置绑定测试或配置加载测试

建议测试命令：

```powershell
mvn test
mvn clean package
```

如果 JaCoCo 配置了覆盖率门槛，本阶段可以只对已存在代码生效，不强行要求 80% 覆盖全项目；从阶段 3 起逐步严格。

## 验证命令

阶段 2 完成前必须运行：

```powershell
mvn test
mvn clean package
git diff --check
git status --short --branch
```

可选验证：

```powershell
mvn jacoco:report
```

## Git 提交要求

建议提交节奏：

```powershell
git add pom.xml src .env.example docs/11-phase-2-backend-skeleton-brief.md
git commit -m "feat: add spring boot backend skeleton"
```

提交前必须确认：

- 没有真实密钥。
- 没有 Redis、Flyway、Liquibase。
- 没有前端工程。
- 没有业务模块越界实现。

## 阶段 2 交接报告格式

完成后必须按以下格式汇报：

```text
任务：阶段 2 后端骨架
阶段：phase-2-backend-skeleton
分支：phase-2-backend-skeleton
提交：
已完成：
未完成：
变更文件：
验证命令：
验证结果：
覆盖率：
已知风险：
待决策事项：
下一步允许执行的任务：阶段 3 核心实体与 Mapper
```

## 阶段 2 退出标准

只有同时满足以下条件，才允许进入阶段 3：

- Spring Boot 应用可以启动。
- 健康检查接口可用。
- MyBatis-Plus 配置完成。
- 数据库连接通过环境变量配置。
- 全局响应和异常处理存在。
- Spring Security 骨架存在，但未越界实现完整认证。
- `mvn test` 通过。
- `mvn clean package` 通过。
- `git diff --check` 通过。
- 已提交 Git。
