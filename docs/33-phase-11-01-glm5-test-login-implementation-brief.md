# 阶段 11-01：GLM5.1 用户测试登录与 USER JWT 实施任务书

日期：2026-06-13

状态：已批准，可执行

目标分支：`phase-11-user-prerequisites`

执行对象：GLM5.1

## 1. 任务目的

本任务不是只增加一个测试登录 Controller，而是补齐阶段 11 后续用户端接口所依赖的最小用户认证基础设施。

完成后系统必须同时支持：

- 现有管理员 `ADMIN` JWT 登录、鉴权和权限判断，行为不得回归。
- 新增用户 `USER` JWT 的签发、解析、数据库状态校验和安全上下文注入。
- 仅在 `test` Profile 存在的测试用户登录端点。
- 后续用户业务 Controller 可通过统一辅助方法安全获取当前用户 ID。

本任务结束时不得提前实现用户资料、宠物、地址、预约、购物车、订单或社区业务接口。

## 2. 开工前强制操作

按 `AGENTS.md` 的顺序阅读全部强制文档，重点阅读：

- `docs/08-pending-decisions.md` 中 D-013。
- `docs/13-phase-4-auth-authorization-plan.md`。
- `docs/22-continuous-agent-development-rules.md`。
- `docs/31-phase-11-user-prerequisites-plan.md`。
- 本任务书。

运行：

```powershell
git status --short --branch
git log --oneline -5
git branch --show-current
```

预期基线应包含提交 `dd58edf test(mysql): enforce Testcontainers schema and concurrency gate`。如果该提交不存在或工作区有不明修改，停止并汇报，不得覆盖。

如果目标分支不存在，在干净的 `phase-10-frontend` 最新基线上创建：

```powershell
git switch -c phase-11-user-prerequisites
```

修改前必须先汇报：

```text
已阅读文档：
当前阶段：11-01
当前分支：
当前提交：
计划修改范围：
不会修改的范围：
待决策或阻塞项：
验证计划：
```

## 3. 验收目标

必须同时满足以下条件：

1. `POST /api/v1/auth/test-login` 只在 `test` Profile 加载。
2. 只允许已存在、未删除且状态为 `ACTIVE` 的预置用户登录。
3. 用户不存在、禁用或已删除时登录失败，且绝不创建用户。
4. 签发 `tokenType=USER` 的 JWT，Token 中不包含用户隐私字段。
5. 认证过滤器能按 `tokenType` 正确路由 `ADMIN` 和 `USER` Token。
6. `USER` Token 不能被当成管理员 Token，`ADMIN` Token 不能被当成用户 Token。
7. 用户请求认证时必须重新读取数据库中的当前用户状态。
8. 提供统一的当前用户 ID 获取方法，禁止业务接口从请求参数接收当前用户 ID。
9. 现有管理员登录、管理员 `/me`、权限校验和微信登录存根测试保持通过。
10. 非 `test` Profile 下测试登录端点完全不存在，访问结果为 404。

## 4. 当前代码事实

开始实现前必须确认以下事实仍成立：

- `JwtTokenService` 当前只签发管理员 Token。
- `JwtAuthenticationFilter` 当前只加载 `AdminPrincipal`。
- `SecurityContextHelper` 当前只提供管理员身份辅助方法。
- `SecurityConfig` 当前仅放行健康检查、管理员登录和微信登录。
- 用户模块当前没有 `UserPrincipal` 或用户认证加载服务。
- 用户端现有业务 Controller 中仍存在临时 401 或空用户 ID 占位逻辑。
- `/api/v1/user/profile` 属于任务 11-02，当前不存在。

如果事实发生变化，先汇报差异和影响，再继续。

## 5. 允许修改范围

仅允许修改下列范围中完成任务所必需的文件：

```text
pom.xml（仅当测试依赖确实缺失时）
src/main/java/com/petcare/common/security/JwtTokenService.java
src/main/java/com/petcare/common/security/JwtAuthenticationFilter.java
src/main/java/com/petcare/common/security/SecurityContextHelper.java
src/main/java/com/petcare/common/config/SecurityConfig.java
src/main/java/com/petcare/user/auth/
src/main/java/com/petcare/user/controller/TestLoginController.java
src/main/java/com/petcare/user/dto/TestLoginRequest.java
src/main/java/com/petcare/user/dto/TestLoginResponse.java
src/main/java/com/petcare/user/security/
src/main/java/com/petcare/user/service/UserService.java
src/main/java/com/petcare/user/service/impl/UserServiceImpl.java
src/test/java/ 下与本任务直接相关的测试
src/test/resources/application-profile-isolation.yml
```

包路径应服从仓库现有结构。上方不存在的目录可以按职责创建，但不得移动无关文件。

## 6. 禁止修改与禁止行为

禁止修改：

- `schema.sql`、`schema-h2.sql`、Testcontainers MySQL Schema。
- `frontend/admin-web`、`frontend/miniapp` 和根前端依赖文件。
- 管理员登录响应、管理员权限模型和管理员 Token 既有行为。
- 微信登录 Provider、文件上传、AI、Redis。
- 预约、购物车、订单、社区等用户业务 Controller。
- 与本任务无关的代码、测试和文档。

禁止行为：

- 禁止自动创建测试用户。
- 禁止把固定用户 ID 写死在 Token 或 Controller 中。
- 禁止通过请求体、请求头或查询参数接收用户 ID。
- 禁止接受任意手机号并模拟真实微信用户。
- 禁止把 `USER` Token 解释为管理员身份。
- 禁止把 `ADMIN` Token 解释为用户身份。
- 禁止提前实现 `/api/v1/user/profile` 或其他 11-02 之后的接口。
- 禁止用宽泛的 `/api/v1/auth/**` 放行规则。

## 7. 目标设计

### 7.1 USER JWT 契约

USER JWT 只允许包含：

| Claim | 要求 |
|------|------|
| `sub` | 用户 ID 的字符串形式 |
| `tokenType` | 固定为 `USER` |
| `iss` | 沿用现有签发方 |
| `iat` | 签发时间 |
| `exp` | 过期时间 |

不得写入 Token：

- 手机号。
- `openid`、`unionid`。
- 昵称、头像。
- 管理员角色、权限码。
- 任何密钥或 Provider 信息。

`JwtTokenService` 建议补充以下职责，方法名可按现有风格调整：

```java
String signUserToken(Long userId);
Long getSubjectId(String token);
String getTokenType(String token);
Long getUserId(String token);
```

强制规则：

- 保留现有管理员签发和解析方法的兼容性。
- `getUserId` 必须拒绝 `ADMIN` Token。
- `getAdminId` 必须拒绝 `USER` Token。
- 未知、缺失或大小写不正确的 `tokenType` 必须拒绝。
- Token 解析失败时不能泄露 JWT 原文或内部异常细节。

### 7.2 UserPrincipal

在用户安全包中新增 `UserPrincipal`，最少包含：

- `Long userId`。
- `ROLE_USER` 权限。

不得存储：

- 明文密码。
- 手机号、openid、unionid 等本任务不需要的隐私信息。
- 管理员权限码。

如果实现 `UserDetails`，只实现 Spring Security 必需的稳定语义，不伪造密码登录能力。

### 7.3 用户认证加载服务

新增职责单一的用户认证加载服务，例如：

```java
UserPrincipal loadActiveUserById(Long userId);
```

要求：

- 通过 `UserService` 查询，不允许 Filter 或 Controller 直接调用 Mapper。
- 只接受未删除且状态为 `ACTIVE` 的用户。
- 用户不存在、禁用或已删除时认证失败。
- 不新增另一个会与管理员认证冲突的通用 `UserDetailsService` Bean。

### 7.4 JwtAuthenticationFilter 路由

过滤器应只解析 Token 一次，然后按 `tokenType` 分流：

```text
ADMIN -> AdminUserDetailsService -> AdminPrincipal
USER  -> 用户认证加载服务 -> UserPrincipal
其他  -> 不写入 SecurityContext，后续返回统一 401
```

要求：

- 每次请求根据 Token 中的 ID 读取数据库最新状态。
- 认证成功后再写入 `SecurityContext`。
- 认证失败不能留下部分认证状态。
- 不改变现有公开端点、CORS、异常处理和管理员权限逻辑。

### 7.5 SecurityContextHelper

在保持现有管理员方法兼容的前提下，新增用户身份辅助方法，例如：

```java
Optional<UserPrincipal> getUserPrincipal();
Optional<Long> getCurrentUserId();
```

要求：

- 当前身份为 `UserPrincipal` 时返回用户信息。
- 当前身份为 `AdminPrincipal`、匿名身份或其他类型时返回空。
- 不允许静默把管理员 ID 当成用户 ID。

### 7.6 测试登录端点

端点：

```http
POST /api/v1/auth/test-login
Content-Type: application/json
```

请求：

```json
{
  "phone": "13800138001"
}
```

校验：

```java
@NotBlank
@Pattern(regexp = "^1\\d{10}$")
```

成功响应应符合现有统一响应包装，业务数据建议为：

```json
{
  "tokenType": "Bearer",
  "accessToken": "<jwt>",
  "expiresInSeconds": 7200,
  "user": {
    "id": "1",
    "nickname": "测试用户"
  }
}
```

要求：

- ID 按项目现有前端契约以字符串返回。
- 响应不得返回手机号、openid、unionid。
- 只允许数据库中已经存在的测试种子用户。
- 登录查询必须复用 `UserService` 或专用认证服务，不得直接调用 Mapper。

错误语义：

| 场景 | HTTP | 错误类型 |
|------|------|----------|
| 手机号缺失或格式非法 | 400 | `validation_error` |
| 用户不存在、禁用或已删除 | 401 | `unauthorized` |
| 非 test Profile 访问 | 404 | `resource_not_found` |

如项目当前统一异常契约使用不同状态码，必须先指出冲突并按高优先级文档执行，不得自行创造第二套错误格式。

### 7.7 Profile 隔离

- `TestLoginController` 和只服务于测试登录的组件必须使用 `@Profile("test")`。
- `SecurityConfig` 可精确放行 `/api/v1/auth/test-login`，但不得放行通配路径。
- 非 `test` Profile 下 Controller Bean 不存在，因此端点必须返回 404，而不是 401、403 或伪成功。
- 生产代码不得依赖测试种子初始化逻辑。

## 8. 强制 TDD 执行顺序

每一批必须先 RED、再 GREEN、再重构。禁止先写完实现后补测试。

### 8.1 RED-1：USER JWT 与类型隔离

先扩展或新增 `JwtTokenServiceTest`，覆盖：

- USER Token 包含正确的 `sub` 和 `tokenType=USER`。
- USER Token 不包含手机号、openid、unionid、管理员角色或权限。
- `getUserId` 能解析 USER Token。
- `getUserId` 拒绝 ADMIN Token。
- `getAdminId` 拒绝 USER Token。
- 未知或缺失 `tokenType` 被拒绝。
- 过期、损坏 Token 被拒绝。

运行并保存 RED 证据：

```powershell
mvn "-Dtest=JwtTokenServiceTest" test
```

允许提交：

```powershell
git add <本批测试文件>
git commit -m "test: define user jwt authentication contract"
```

### 8.2 GREEN-1：用户认证基础设施

实现最小代码使 RED-1 通过，并增加直接测试覆盖：

- `UserPrincipal`。
- 用户认证加载服务。
- Filter 的 ADMIN/USER 分流。
- `SecurityContextHelper` 的用户身份读取。
- 禁用用户 Token 无法建立认证。
- 管理员 Token 仍建立管理员认证。

运行：

```powershell
mvn "-Dtest=JwtTokenServiceTest,SecurityAccessTest,*UserAuthentication*Test,*SecurityContextHelper*Test" test
```

### 8.3 RED-2：测试登录端点

先新增 `TestLoginControllerTest`，至少覆盖：

- `test` Profile 下 ACTIVE 预置用户登录成功。
- 返回 ID 为字符串。
- 响应和 Token 不暴露隐私字段。
- 不存在手机号返回 401，且数据库用户数量不变。
- 禁用或已删除用户返回 401。
- 手机号缺失和格式非法返回 400。
- 登录获得的 USER Token 能访问测试源码中的受保护用户认证探针。
- ADMIN Token 不能通过用户认证探针。
- 无 Token、无效 Token 和过期 Token 返回 401。

受保护探针只能定义在 `src/test/java`，例如：

```text
GET /api/v1/test/user-auth-probe
```

探针只验证 `SecurityContextHelper.getCurrentUserId()`，不得在生产源码中增加 `/me`、`/profile` 或临时业务接口。

### 8.4 RED-3：非 test Profile 隔离

新增 `TestLoginProfileIsolationTest`，使用与 `test` 不同的测试 Profile，例如 `profile-isolation`：

- 在 `src/test/resources/application-profile-isolation.yml` 中配置测试数据库、Schema 和 JWT 测试密钥。
- 使用 `@ActiveProfiles("profile-isolation")`。
- 断言 `POST /api/v1/auth/test-login` 返回 404。
- 断言 Spring Context 中不存在 `TestLoginController` Bean。
- 断言不存在仅用于测试登录的 Profile 限定 Bean。

不得通过启动真实 `dev` 或 `prod` 配置来完成此测试。

### 8.5 GREEN-2 与重构

实现最小端点和 Profile 隔离代码，全部测试通过后再重构：

- 去除重复 Token 解析。
- 保持方法短小、职责单一。
- 检查日志不包含 Token 和隐私字段。
- 检查没有 Controller 直连 Mapper。
- 检查没有无关业务接口变更。

## 9. 必须保留的回归行为

至少确认：

- 管理员登录仍签发 ADMIN Token。
- 管理员 `/me` 和权限判断仍正常。
- ADMIN Token 不能访问用户身份探针。
- USER Token 不能获得管理员身份。
- 微信登录存根继续按既有契约返回未实现状态。
- 无效、损坏、过期 Token 继续返回统一 401。
- 公开端点不受影响。

## 10. 代码与安全规范

- 所有 DTO 使用 Bean Validation。
- Controller 只负责协议适配，不承载 Token 或数据库业务逻辑。
- Filter 不直接访问 Mapper。
- 不硬编码 JWT 密钥、数据库凭据、真实手机号或生产隐私数据。
- 不记录完整 Token、手机号、openid、unionid。
- 错误响应不泄露堆栈、SQL、JWT 解析异常或用户是否属于真实生产账号。
- 新增公开方法必须有清晰命名；仅在复杂安全判断处添加简短注释。
- 不使用空 catch，不静默吞掉认证异常。
- 非平凡逻辑直接测试覆盖率目标不低于 80%。

## 11. 完整验证门禁

按顺序执行：

```powershell
mvn "-Dtest=JwtTokenServiceTest,SecurityAccessTest,WechatLoginControllerTest,TestLoginControllerTest,TestLoginProfileIsolationTest" test
mvn test
mvn clean package "-DskipTests"
mvn -Ptc-mysql clean test
git diff --check
git status --short --branch
```

补充人工检查：

```powershell
rg "test-login|tokenType|UserPrincipal|getCurrentUserId" src/main src/test
rg "phone|openid|unionid" src/main/java/com/petcare/user src/main/java/com/petcare/common/security
rg "api/v1/auth/\\*\\*|permitAll" src/main/java/com/petcare/common/config/SecurityConfig.java
```

检查目标：

- 没有隐私字段进入 JWT 或测试登录响应。
- 没有宽泛认证放行。
- 没有生产业务 Controller 被修改。
- 没有自动创建用户。
- MySQL Testcontainers 门禁仍通过。

如完整 `mvn test` 或 `mvn -Ptc-mysql clean test` 因环境问题失败，必须提供完整失败命令、首个根因和已验证范围，不得声称任务完成。

## 12. Git 提交规则

禁止使用 `git add .`。每次只暂存当前批次文件，并先检查：

```powershell
git diff --check
git diff --cached --check
git status --short
```

建议提交顺序：

```text
test: define user jwt authentication contract
feat: add user jwt authentication foundation
test: define test login profile isolation
feat: add test profile user login
```

如果实现规模较小，可以合并相邻 GREEN 提交，但 RED 测试证据必须在交接中保留。不得重写、压缩或删除其他 Agent 的提交。

## 13. 必须停止并汇报的情况

遇到以下任一情况，停止相关实现：

- 需要修改 Schema 或测试种子 Schema。
- 需要改变管理员 Token 契约。
- 现有用户状态枚举与 `ACTIVE` 规则冲突。
- 统一异常状态码与本任务书冲突。
- 非 test Profile 404 无法在现有安全配置下实现。
- 需要提前修改业务 Controller 才能验证 USER Token。
- 工作区出现不属于本任务的未提交修改。
- MySQL Testcontainers 基线在修改前已经失败。

汇报必须包含：事实、影响、可选方案、优缺点、推荐方案和等待用户决定的事项。

## 14. 强制交接格式

```text
任务：11-01 用户测试登录与 USER JWT
阶段：11
分支：
提交：
已完成：
未完成：
变更文件：
RED 证据：
验证命令：
验证结果：
覆盖率：
安全检查结果：
已知风险：
待决策事项：
下一步允许执行的任务：仅在全部门禁通过后允许开始 11-02
```

没有提交、测试、Profile 隔离和 MySQL 门禁证据时，不得声明 11-01 已完成。
