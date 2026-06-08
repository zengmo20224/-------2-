# 阶段 4：认证与授权代码计划

日期：2026-06-08

负责人：GLM5.1

当前分支：`phase-4-auth-authorization`

前置条件：阶段 3 核心实体、Mapper 与基础 CRUD Service 已完成，提交为 `41c6756 feat: add entities mappers and base services`。

## 目标

本阶段实现后端安全基线：

- 管理员账号密码登录。
- BCrypt 密码校验。
- JWT 签发、解析和认证过滤。
- Spring Security 无状态认证配置。
- 基于 `admin_role`、`admin_permission`、`admin_role_permission` 的细粒度权限加载。
- 使用权限码进行后台接口和方法授权。
- 为后续微信登录保留接口和适配器边界，但不接入真实微信。
- 提供当前登录管理员身份获取能力。
- 为后续用户资源归属校验提供可复用安全上下文工具。

本阶段只建立认证与授权基础，不实现预约、订单、社区、AI、文件上传和前端业务流程。

## 必须先阅读

GLM5.1 开始编码前必须阅读：

1. `AGENTS.md`
2. `README.md`
3. `schema.sql`
4. `docs/00-project-boundary.md`
5. `docs/01-architecture-design.md`
6. `docs/02-task-breakdown.md`
7. `docs/03-glm5-implementation-plan.md`
8. `docs/04-code-standards.md`
9. `docs/05-testing-and-verification.md`
10. `docs/07-integration-gates.md`
11. `docs/08-pending-decisions.md`
12. `docs/10-admin-permission-design.md`
13. `docs/12-phase-3-entities-mappers-plan.md`
14. 本文件

编码前必须先运行：

```powershell
git status --short --branch
git log --oneline -5
```

然后先输出：

```text
已阅读文档：
当前阶段：阶段 4 认证与授权
当前分支：phase-4-auth-authorization
计划修改范围：
不会修改的范围：
待决策或阻塞项：
验证计划：
```

没有完成以上汇报前，不允许修改项目文件。

## 已决定事项

必须按以下项目决策执行：

- 使用 Spring Security + JWT。
- 使用 MyBatis-Plus。
- 当前仅维护 `schema.sql`，不引入 Flyway 或 Liquibase。
- V1 不启用 Redis，因此本阶段不做 Redis Token 黑名单。
- 微信登录 V1 只保留接口和适配器边界，不接入真实 AppID，不实现模拟登录。
- JWT 不长期写入完整权限列表；权限从数据库加载。
- 细粒度 RBAC 权限码遵守 `docs/10-admin-permission-design.md`。

## 阶段 4 允许做的事

允许新增：

- `admin/controller/AdminAuthController`
- `admin/dto` 下的登录请求和响应 DTO
- `admin/security` 或 `common/security` 下的 JWT、Principal、过滤器、认证服务
- `admin/service` 中与登录、权限加载相关的业务方法
- `user/auth` 或 `user/controller` 下的微信登录占位入口
- `user/auth/WechatLoginProvider` 和 disabled 实现
- Spring Security 异常入口点和拒绝处理器
- 认证、授权、JWT、密码、微信占位相关测试

允许调整：

- `SecurityConfig` 从阶段 2 骨架升级为真实 JWT 安全配置。
- `SecurityProperties` 增加 JWT 过期时间、issuer、secret 最小长度等配置。
- `application.yml`、`application-test.yml` 增加安全配置占位值，但不能提交真实密钥。
- `ErrorCode` 增加必要错误码。
- `GlobalExceptionHandler` 增加 Spring Security 相关异常兜底，但认证入口应优先由 Security Handler 返回统一结构。

## 阶段 4 禁止做的事

本阶段禁止：

- 不实现真实微信 `code2session`。
- 不伪造生产可用 openid。
- 不新增真实第三方 AppID、AppSecret、Token、API Key。
- 不引入 Redis、Flyway、Liquibase。
- 不修改 `schema.sql`，除非发现阻塞级错误并先征得用户确认。
- 不实现预约创建、排班算法、订单状态流转、社区审核、AI Provider 调用。
- 不新增前端工程。
- 不把完整权限列表长期写入 JWT。
- 不在日志、异常响应或测试快照中输出密码、JWT、API Key。
- 不为测试方便降低生产安全规则。

## 推荐包结构

在现有包结构基础上最小扩展：

```text
src/main/java/com/petcare/
  admin/
    controller/
      AdminAuthController.java
    dto/
      AdminLoginRequest.java
      AdminLoginResponse.java
      AdminMeResponse.java
    security/
      AdminPrincipal.java
      AdminUserDetailsService.java
      AdminAuthorityService.java
    service/
      AdminAuthService.java
    service/impl/
      AdminAuthServiceImpl.java
  common/
    security/
      JwtTokenService.java
      JwtAuthenticationFilter.java
      SecurityContextHelper.java
      RestAuthenticationEntryPoint.java
      RestAccessDeniedHandler.java
  user/
    auth/
      WechatLoginProvider.java
      DisabledWechatLoginProvider.java
    controller/
      UserAuthController.java
    dto/
      WechatLoginRequest.java
```

如果 GLM5.1 发现现有包命名更适合放在 `common/security`，可以调整，但必须保持 Controller 薄、Service 承载业务、Mapper 不被 Controller 直接调用。

## API 设计

### 管理员登录

```text
POST /api/v1/admin/auth/login
```

请求：

```json
{
  "username": "admin",
  "password": "password-from-user-input"
}
```

校验：

- `username` 必填，长度建议 3 到 64。
- `password` 必填，长度建议 8 到 128。
- 不区分“用户名不存在”和“密码错误”，统一返回认证失败。

成功响应：

```json
{
  "success": true,
  "data": {
    "tokenType": "Bearer",
    "accessToken": "jwt",
    "expiresInSeconds": 7200,
    "admin": {
      "id": 1,
      "username": "admin",
      "nickname": "管理员",
      "role": "SUPER_ADMIN"
    }
  }
}
```

失败响应：

- 用户名或密码错误：`401`，错误码 `unauthorized`。
- 管理员被禁用或逻辑删除：`401`，错误码 `unauthorized`，不要暴露账号状态细节。
- 参数错误：`400`，错误码 `validation_error`。

### 当前管理员

```text
GET /api/v1/admin/auth/me
```

要求：

- 必须携带有效 Bearer Token。
- 返回当前管理员基础信息、基础角色和权限码列表。
- 不返回密码字段。

### 用户微信登录占位

```text
POST /api/v1/auth/wechat-login
```

请求：

```json
{
  "code": "temporary-code-from-wechat-mini-program"
}
```

阶段 4 行为：

- Controller 和 DTO 可以存在。
- 调用 `WechatLoginProvider` 接口。
- 默认实现 `DisabledWechatLoginProvider`。
- 返回 `422`，错误码 `wechat_login_not_enabled`，消息“微信登录暂未启用”。
- 不生成用户、不写 openid、不签发用户 Token。

## JWT 实现要求

必须实现：

- `JwtTokenService`
  - 签发管理员访问 Token。
  - 校验签名、过期时间、issuer、tokenType。
  - 从 Token 中读取管理员 id、username、基础角色。
- `JwtAuthenticationFilter`
  - 继承 `OncePerRequestFilter`。
  - 只处理 `Authorization: Bearer <token>`。
  - Token 缺失时不主动报错，交给 Spring Security 判断是否需要认证。
  - Token 非法或过期时返回统一 `401`。
- `AdminPrincipal`
  - 包含 `adminId`、`username`、`role`、`authorities`。
  - 不包含密码。
- `AdminUserDetailsService`
  - 根据管理员 id 或 username 加载管理员。
  - 只允许 `status = ACTIVE` 且 `deleted = 0` 的管理员认证。
  - 从角色权限表加载启用状态的权限码。

JWT Claim 要求：

```text
sub = adminId
username = admin username
role = admin_user.role
tokenType = ADMIN
iss = petcare-o2o-api
iat = issued time
exp = expiration time
```

禁止放入：

- 密码哈希
- 手机号
- 完整权限列表
- API Key
- 数据库连接信息

## 安全配置要求

`SecurityConfig` 必须满足：

- `@EnableMethodSecurity` 启用方法级授权。
- `SessionCreationPolicy.STATELESS`。
- Bearer API 模式下禁用 CSRF。
- 使用统一认证失败入口，返回 `401` + `ApiResponse.error(...)`。
- 使用统一授权失败处理器，返回 `403` + `ApiResponse.error(...)`。
- 公开接口仅限：
  - `GET /api/v1/system/health`
  - `POST /api/v1/admin/auth/login`
  - `POST /api/v1/auth/wechat-login`
- 其他 `/api/v1/**` 默认需要认证。
- JWT Filter 放在 `UsernamePasswordAuthenticationFilter` 之前。

不要在本阶段放开所有 Swagger、Actuator 或静态资源路径，除非项目已经存在并有明确理由。

## 密码要求

- 使用 `PasswordEncoder` Bean。
- 推荐 `BCryptPasswordEncoder(12)`。
- 登录时只能使用 `passwordEncoder.matches(raw, hash)`。
- 禁止明文存储密码。
- 禁止手写哈希算法。
- 禁止在响应、日志、异常、`toString()` 中输出密码。

测试数据中的密码也应通过 `PasswordEncoder` 生成哈希后写入测试数据库。

## RBAC 权限加载要求

必须使用现有表：

- `admin_user`
- `admin_role`
- `admin_permission`
- `admin_role_permission`

加载逻辑：

1. 根据管理员 `role` 找到 `admin_role.role_code`。
2. 只使用 `admin_role.status = ACTIVE` 且 `deleted = 0` 的角色。
3. 通过 `admin_role_permission` 找到权限。
4. 只使用 `admin_permission.status = ACTIVE` 且 `deleted = 0` 的权限。
5. 把 `permission_code` 映射为 Spring Security `GrantedAuthority`。

授权方式：

```java
@PreAuthorize("hasAuthority('booking:booking:confirm')")
```

本阶段可以为了验证方法级权限新增测试专用 Controller 或最小受保护接口，但不能借机实现真实业务模块。

## 用户资源归属校验边界

阶段 4 的“用户资源归属校验”只建立可复用基础，不实现具体业务资源：

- 提供 `SecurityContextHelper` 或等效工具，能获取当前管理员 id、角色和权限。
- 为后续用户端登录预留当前用户身份模型，但微信登录未启用时不得签发用户 Token。
- 不实现宠物、地址、订单、帖子等资源归属判断的真实业务逻辑。

后续业务阶段在具体 Service 中校验：

- 当前用户只能操作自己的宠物、地址、预约、订单、帖子。
- STAFF 只能处理自己被分配或授权范围内的任务。

## 错误码建议

在 `ErrorCode` 中新增或复用：

```text
unauthorized
forbidden
wechat_login_not_enabled
invalid_token
expired_token
```

如果最终实现选择把非法和过期 Token 都映射为 `unauthorized`，也可以不暴露 `invalid_token`、`expired_token` 给前端，但测试必须覆盖非法 Token 和过期 Token 的 `401` 行为。

## 配置要求

`application.yml` 建议：

```yaml
petcare:
  security:
    jwt-secret: ${JWT_SECRET:}
    jwt-issuer: ${JWT_ISSUER:petcare-o2o-api}
    jwt-expiration-minutes: ${JWT_EXPIRATION_MINUTES:120}
```

要求：

- 生产和开发运行不能依赖硬编码真实密钥。
- `JWT_SECRET` 长度不足时应快速失败，测试 profile 可以使用测试专用假密钥。
- `.env.example` 可以增加示例变量，但值必须是占位符。
- 不提交真实密码、真实 Token、真实微信密钥。

## TDD 实施顺序

必须按 RED、GREEN、重构推进。建议顺序：

1. 写 `JwtTokenServiceTest`，覆盖签发、解析、过期、非法签名。
2. 写 `PasswordEncoderTest`，确认 BCrypt 匹配成功且明文不等于哈希。
3. 写 `AdminUserDetailsServiceTest`，构造管理员、角色、权限数据，验证权限码加载。
4. 写 `AdminAuthControllerTest`，验证登录成功、错误密码、禁用账号、响应不含密码。
5. 写 `SecurityAccessTest`，验证未认证 `401`、缺权限 `403`、有权限 `200`。
6. 写 `WechatLoginControllerTest`，验证微信登录占位返回 `422` 和 `wechat_login_not_enabled`。
7. 实现最小代码让测试变绿。
8. 重构并运行完整验证。

如果测试专用 Controller 用于验证 `@PreAuthorize`，必须放在 `src/test/java`，不能进入生产代码。

## 最低测试清单

必须新增或覆盖：

- 健康检查无需认证可访问。
- 后台受保护接口未登录返回 `401`。
- Bearer Token 缺失、非法、过期都不能访问受保护接口。
- 登录成功返回 Token、过期秒数、管理员摘要。
- 登录响应不包含 `password`。
- 错误密码返回 `401`。
- 禁用管理员不能登录。
- 管理员权限从数据库角色权限关系加载。
- 缺少权限访问方法级受保护接口返回 `403`。
- 拥有权限访问方法级受保护接口返回 `200`。
- `STAFF` 不具备 `store:config:update`。
- `MANAGER` 不具备 `admin:role:manage`。
- `SUPER_ADMIN` 可具备 `admin:role:manage`。
- 微信登录占位不会创建用户、不会生成 openid、不会签发 Token。

## 数据库和测试数据要求

- 不新增生产 seed SQL 作为自动初始化逻辑。
- 测试通过 H2 或测试 MySQL 构造必要管理员、角色、权限数据。
- 测试中插入 `admin_user.password` 时必须使用 BCrypt 哈希。
- 不依赖真实本地管理员账号。
- 不把测试账号当成生产默认账号写入 `schema.sql`。

## 验证命令

阶段完成前必须运行：

```powershell
mvn test
mvn clean package
git diff --check
git status --short --branch
```

如果某项验证未执行，必须在交接报告中写明：

```text
未执行命令：
原因：
替代验证：
风险：
```

## Git 提交要求

建议提交：

```powershell
git add src pom.xml .env.example docs/13-phase-4-auth-authorization-plan.md
git commit -m "feat: add authentication and authorization"
```

如果只提交计划文档，使用：

```powershell
git add AGENTS.md README.md docs
git commit -m "docs: add phase 4 auth authorization plan"
```

提交前必须确认：

- 没有真实密钥。
- 没有真实微信 AppID/AppSecret。
- 没有 Redis、Flyway、Liquibase。
- 没有前端代码。
- 没有越界实现预约、订单、社区、AI。
- `git diff --check` 通过。

## 阶段 4 交接报告格式

完成后必须按以下格式汇报：

```text
任务：阶段 4 认证与授权
阶段：phase-4-auth-authorization
分支：phase-4-auth-authorization
提交：
已完成：
未完成：
变更文件：
验证命令：
验证结果：
覆盖率：
认证验证：
授权验证：
微信登录占位验证：
已知风险：
待决策事项：
下一步允许执行的任务：阶段 5 服务预约与排班
```

## 阶段 4 退出标准

只有同时满足以下条件，才允许进入阶段 5：

- 管理员登录接口可用。
- 密码使用 BCrypt 校验。
- JWT 签发、解析、过期和非法 Token 测试通过。
- 未认证请求返回 `401`。
- 无权限请求返回 `403`。
- 权限码从数据库加载，并通过 `hasAuthority(...)` 生效。
- `STAFF`、`MANAGER`、`SUPER_ADMIN` 权限差异有测试。
- 微信登录占位接口存在且明确返回未启用。
- Token、密码、API Key 不出现在响应或日志中。
- `mvn test` 通过。
- `mvn clean package` 通过。
- `git diff --check` 通过。
- 已提交 Git。
