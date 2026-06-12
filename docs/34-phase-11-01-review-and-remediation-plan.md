# 阶段 11-01 Review 结论与修复任务书

日期：2026-06-13

状态：Review 未通过，必须先执行本任务

执行对象：GLM5.1

目标分支：`phase-11-user-prerequisites`

基线提交：`49dd45a feat: add test profile user login`

## 1. Review 结论

阶段 11-01 已完成 USER JWT、测试登录端点、Profile 隔离和基础测试，但尚未通过集成门禁，禁止开始 11-02 编码。

已验证通过：

- 11-01 定向测试：50 项通过。
- `mvn clean package "-DskipTests"`：通过。
- `mvn -Ptc-mysql clean test`：16 项通过。
- `git diff --check`：通过。
- 工作区检查：干净。

未通过：

- `mvn test`：失败。
- 安全 Review：存在认证异常外溢和角色隔离假阳性。
- 代码职责 Review：测试登录业务逻辑仍位于 Controller。

## 2. Review Findings

### HIGH-1：禁用或删除用户的有效 Token 可能导致过滤器异常外溢

证据：

- `JwtAuthenticationFilter` 仅捕获 `JwtException | IllegalArgumentException`。
- `UserAuthLoadingService.loadActiveUserById` 会抛出 `UsernameNotFoundException`。
- `UsernameNotFoundException` 属于 `AuthenticationException`，当前不会被过滤器捕获。
- 该异常发生在 Spring MVC Controller 之前，不能依赖 `GlobalExceptionHandler` 转换为 401。

影响：

- 用户在 Token 签发后被禁用或删除，再访问受保护接口时，可能得到 500 或未处理异常，而不是统一 401。
- 管理员 Token 对应管理员被禁用时存在同类风险。

必须修复：

- 认证过滤器捕获认证加载失败并清空 `SecurityContext`。
- 对用户禁用、用户删除、管理员禁用三类 Token 状态变化增加请求级回归测试。
- 响应必须为统一 401，不能泄露内部异常。

### HIGH-2：ADMIN Token “不能访问用户探针”的测试是假阳性

证据：

- `UserAuthProbeController` 没有 `ROLE_USER` 授权约束。
- 当前测试对 ADMIN Token 断言 HTTP 200，并仅检查 `userId=none`。
- 测试名称声称“不能访问”，但实际允许管理员调用用户接口。

影响：

- 现有测试不能证明 ADMIN 与 USER 的接口角色边界。
- 后续用户资料接口若只要求 `authenticated()`，ADMIN Token 也能进入 Controller。

必须修复：

- 测试探针增加明确的 `ROLE_USER` 约束。
- USER Token 访问返回 200。
- ADMIN Token 访问返回 403。
- 无 Token、失效 Token 返回 401。
- 11-02 及后续用户专属 Controller 必须使用明确的 USER 角色约束。

### HIGH-3：测试登录未限制为预定义测试种子用户

证据：

- D-013 明确要求只允许登录预先定义的测试种子用户。
- 当前实现只按手机号和 `ACTIVE` 状态查询，数据库中任意 ACTIVE 用户都可登录。
- 仓库当前没有测试登录允许名单，也没有可复现的预定义用户种子。

影响：

- 不满足已批准的 D-013 安全边界。
- 如果 test 环境误接入包含其他用户的数据，测试登录可为任意 ACTIVE 用户签发 Token。

必须修复：

- 增加仅在 `test` Profile 加载的测试登录允许名单配置。
- 只允许“手机号在允许名单中且数据库用户已存在且 ACTIVE”的用户登录。
- 允许名单不得包含生产秘密，不得自动创建用户。
- ACTIVE 但不在允许名单中的用户必须返回统一 401。
- 允许名单中的用户不存在时仍返回 401，不能创建。
- 阶段 11-06 再负责可重复种子数据；本任务只建立允许名单边界。

### MEDIUM-1：测试登录业务逻辑位于 Controller

证据：

- `TestLoginController` 直接构造查询条件、判断用户状态、签发 Token 和组装响应。

影响：

- 与项目“Controller 只做协议适配”的规则冲突。
- 查询、签发和错误语义难以进行独立单元测试。

必须修复：

- 新增仅 `test` Profile 加载的测试登录应用服务。
- Controller 只执行参数接收、调用服务和统一响应包装。
- 服务负责查询 ACTIVE 用户、签发 Token 和组装响应。
- 不允许 Controller 或服务直接调用 Mapper，继续通过 `UserService`。

### MEDIUM-2：JWT 未验证 issuer，过滤器重复解析 Token

证据：

- `JwtTokenService.parseToken` 验证签名和过期时间，但未要求配置的 issuer。
- Filter 先调用 `getTokenType(token)`，再调用 `getSubjectId(token)`，同一请求解析两次。
- `getSubjectId` 不执行 Token 类型校验，公开方法容易被后续代码误用。

影响：

- 使用相同密钥但不同 issuer 签发的 Token 可能被接受。
- 与 11-01 任务书“单次解析、按类型路由”的要求不一致。

必须修复：

- 解析器验证配置的 issuer。
- 增加错误 issuer Token 被拒绝的测试。
- Filter 单次解析 Claims，并使用类型安全的 ADMIN/USER ID 提取流程。
- 未知、缺失或大小写错误的 `tokenType` 必须返回 401。

### MEDIUM-3：缺失删除用户与状态变化测试

证据：

- 提交说明声称拒绝 deleted 用户，但 `TestLoginControllerTest` 没有删除用户测试。
- 没有覆盖 Token 签发后用户被禁用或删除的请求。

必须补充：

- 已删除用户不能通过测试登录。
- Token 签发后用户被禁用，请求返回 401。
- Token 签发后用户被逻辑删除，请求返回 401。
- 对应用户不会被重新创建或恢复。

### BLOCKER-1：完整 Maven 测试门禁失败

命令：

```powershell
mvn test
```

首个明确失败：

```text
BookingStatusTransitionTransactionTest.userCancel_writesActualOldStatus
expected: "PENDING_CONFIRM"
but was: null
```

说明：

- 11-01 未修改预约模块，该失败不是本批认证代码直接引入。
- 但项目规则要求完整测试通过，因此阶段 11-01 仍不能声明完成。
- 修复预约测试或实现前，必须先确认测试期望与业务规则，不得仅修改断言让测试变绿。

另有 JaCoCo 对 `CCJSqlParserTokenManager` 的 `MethodTooLargeException` 警告。当前明确导致 Maven 失败的是预约状态测试，但后续应评估为 JaCoCo 排除第三方包，避免日志噪声和覆盖率不稳定。

### LOW-1：测试和生产注释仍残留 RED 阶段描述

完成 GREEN 后，`SecurityContextHelper` 等位置仍写有 “RED-1 stubs” 等过期注释。修复时应清理误导性注释，但不得做无关重构。

## 3. 下一项可执行任务

任务编号：`11-01R`

目标：修复上述认证问题，恢复完整测试门禁，并使 11-01 真正达到可集成状态。

11-02 已有详细任务书，但在本任务全部通过前保持锁定。

## 4. 开工前强制检查

```powershell
git status --short --branch
git log --oneline -8
git diff 4fdc746..HEAD --stat
```

必须确认：

- 当前分支为 `phase-11-user-prerequisites`。
- 工作区无不明修改。
- HEAD 包含 `49dd45a`。
- 不创建新阶段分支。

修改前汇报：

```text
已阅读文档：
当前阶段：11-01R
当前分支：
当前提交：
计划修改范围：
不会修改的范围：
Review Findings 对应测试计划：
待决策或阻塞项：
```

## 5. 执行顺序

### 11-01R-A：认证安全回归测试 RED

先增加失败测试：

1. USER Token 签发后用户改为 `DISABLED`，请求受保护用户探针返回 401。
2. USER Token 签发后用户逻辑删除，请求返回 401。
3. ADMIN Token 签发后管理员改为 `DISABLED`，请求受保护接口返回 401。
4. ADMIN Token 访问 USER 探针返回 403。
5. 错误 issuer Token 返回 401。
6. 缺失、未知、大小写错误的 `tokenType` 返回 401。
7. 测试登录拒绝已逻辑删除用户，且不会创建用户。
8. 允许名单中的 ACTIVE 用户登录成功。
9. ACTIVE 但不在允许名单中的用户返回 401。
10. 允许名单中的用户不存在时返回 401 且不会创建用户。

保存 RED 命令和失败摘要，不得先修改实现。

### 11-01R-B：认证实现 GREEN

允许修改：

```text
src/main/java/com/petcare/common/security/JwtAuthenticationFilter.java
src/main/java/com/petcare/common/security/JwtTokenService.java
src/main/java/com/petcare/common/security/SecurityContextHelper.java
src/main/java/com/petcare/user/controller/TestLoginController.java
src/main/java/com/petcare/user/security/
src/main/java/com/petcare/user/auth/
src/main/java/com/petcare/common/config/（仅测试登录允许名单配置）
src/main/resources/application-test.yml（仅非秘密允许名单）
src/test/java/com/petcare/common/security/
src/test/java/com/petcare/user/controller/
```

实现要求：

- 捕获并安全处理 `AuthenticationException`。
- 认证失败后清空上下文并由统一入口返回 401。
- 不捕获所有 `Exception` 后静默吞掉真实编程错误。
- 用户探针明确要求 `ROLE_USER`。
- JWT 验证 issuer。
- Filter 单次解析 Token。
- 删除或限制容易误用的无类型 ID 解析路径。

### 11-01R-C：测试登录职责重构

要求：

- 创建 `@Profile("test")` 的测试登录应用服务。
- Controller 不再包含 MyBatis 查询条件和 Token 签发流程。
- 服务只接受允许名单中的已有 ACTIVE 用户，不创建、不恢复用户。
- 测试登录 Controller 和专用服务在非 test Profile 均不存在。
- Profile 隔离测试同时断言 Controller 和专用服务 Bean 不存在。

### 11-01R-D：恢复完整测试门禁

先单独复现：

```powershell
mvn "-Dtest=BookingStatusTransitionTransactionTest#userCancel_writesActualOldStatus" test
```

处理规则：

- 先核对 `docs/09-booking-concurrency-control.md`、状态流转文档及实现。
- 如果是实现错误，编写 RED 测试后修复实现。
- 如果是测试错误，必须提供证据后修改测试。
- 预约修复必须独立提交，不能混入认证提交。
- 不允许顺手重构预约模块。

## 6. 禁止修改

- Schema 和数据库迁移文件。
- 管理后台、小程序前端。
- 11-02 用户资料 API。
- 宠物、地址、公开读取、种子数据等后续任务。
- 真实微信登录、文件上传、AI、Redis。
- 与 Review Findings 无关的业务代码。

## 7. 强制验证

```powershell
mvn "-Dtest=JwtTokenServiceTest,SecurityAccessTest,WechatLoginControllerTest,TestLoginControllerTest,TestLoginProfileIsolationTest,SecurityContextHelperUserTest" test
mvn "-Dtest=BookingStatusTransitionTransactionTest" test
mvn test
mvn clean package "-DskipTests"
mvn -Ptc-mysql clean test
git diff --check
git status --short --branch
```

必须提供：

- 新增 RED 测试的失败证据。
- 定向测试结果。
- 完整 `mvn test` 结果。
- MySQL Testcontainers 结果。
- Review Findings 逐项关闭表。

## 8. 建议提交顺序

```text
test: expose phase 11-01 authentication review findings
fix: harden user and admin jwt authentication
refactor: isolate test login application service
fix: restore booking status transition test gate
```

禁止 `git add .`，禁止把预约修复混入认证提交。

## 9. 退出门禁

- HIGH 和 MEDIUM Findings 全部关闭。
- ADMIN Token 访问用户探针返回 403。
- 禁用或删除身份的 Token 返回 401，不产生 500。
- 非 test Profile 不存在测试登录 Controller 和专用服务。
- 完整 `mvn test` 通过。
- MySQL Testcontainers 门禁通过。
- 构建和 `git diff --check` 通过。
- 代码 Review 与安全 Review 无 CRITICAL/HIGH 遗留。

全部满足后，才允许执行 `docs/35-phase-11-02-glm5-user-profile-api-brief.md`。

## 10. 强制交接格式

```text
任务：11-01R Review 修复与门禁恢复
阶段：11
分支：
提交：
Review Findings 关闭情况：
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
下一步允许执行的任务：门禁全部通过后仅允许 11-02
```
