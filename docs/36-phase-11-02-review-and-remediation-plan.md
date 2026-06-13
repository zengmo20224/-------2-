# 阶段 11-02 Review 结论与修复任务书

日期：2026-06-13

状态：11-02R Review 已通过，任务关闭

执行对象：GLM5.1

目标分支：`phase-11-user-prerequisites`

基线提交：`3399b83 feat: add current user profile read and update api`

## 1. Review 结论

11-01R 的认证加固已达到当前门禁要求，可以关闭。

11-02 已实现用户资料读取与修改 API，授权、字段白名单和基础契约正确，但服务层状态一致性和手机号隐私处理存在问题，禁止直接开始 11-03。

已验证通过：

- 认证与用户资料定向测试：86 项通过。
- 完整 `mvn test`：通过。
- `mvn clean package "-DskipTests"`：通过。
- `mvn -Ptc-mysql clean test`：16 项通过。
- `git diff --check`：通过。

工作区注意事项：

- 存在未跟踪文件 `.claude/scheduled_tasks.lock`。
- 该文件不属于 11-01R 或 11-02 已提交代码，本任务禁止暂存、修改或提交它。

## 2. 11-01R Review 关闭情况

| Finding | 结论 | 证据 |
|---|---|---|
| 认证加载异常外溢 | 已关闭 | Filter 捕获认证异常，禁用/删除身份测试通过 |
| ADMIN Token 访问 USER 探针 | 已关闭 | 探针要求 `ROLE_USER`，ADMIN 返回 403 |
| 测试登录允许任意 ACTIVE 用户 | 已关闭 | 增加 test Profile 允许名单 |
| 测试登录业务位于 Controller | 已关闭 | 已提取 `TestLoginService` |
| JWT issuer 与重复解析 | 已关闭 | issuer 验证和 Filter 单次解析已实现 |
| 完整测试门禁失败 | 已关闭 | 本次 `mvn test` 通过 |

## 3. 11-02 Review Findings

### HIGH-1：资料服务未真正强制 ACTIVE 用户

证据：

- `UserProfileServiceImpl.getActiveUser` 名称声明读取 ACTIVE 用户。
- 实际实现仅调用 `userService.getById(userId)`，没有检查 `status=ACTIVE`。
- `updateCurrentProfile` 的更新条件仅包含用户 ID，没有包含 `status=ACTIVE`。

影响：

- 服务被内部代码直接调用时，可以读取或修改 DISABLED 用户资料。
- 请求在 JWT Filter 验证通过后、资料更新 SQL 执行前发生用户禁用时，仍可能修改已禁用用户。
- 与 11-02 “禁用或删除用户不能读取和修改资料”的业务边界冲突。

必须修复：

- 服务层查询必须同时限制 `id` 和 `status=ACTIVE`。
- 更新 SQL 必须同时限制 `id` 和 `status=ACTIVE`。
- 逻辑删除继续交由 MyBatis-Plus 自动条件处理，不修改 Schema。
- 条件更新失败不能返回成功；应返回安全的未认证错误，不泄露用户状态。
- 增加服务层直接调用测试和状态变化测试。

### MEDIUM-1：异常长度手机号脱敏可能暴露原始号码

证据：

- `maskPhone` 只拒绝长度小于 7 的值。
- 对 7 位号码，返回的前三位和后四位覆盖了全部原始数字。
- 对 8 至 10 位异常号码，仍暴露大部分原始数字。
- 11-02 任务书要求短于合法手机号的数据不能返回原值。

影响：

- 异常或脏数据可能通过资料 API 泄露。
- 当前单元测试将 7 位号码完全可恢复的结果当成正确行为。

必须修复：

- 仅对符合当前中国大陆手机号格式 `^1\d{10}$` 的值执行 `前三位 + **** + 后四位`。
- 其他长度、非数字、空白或格式异常数据统一返回 `null`。
- 修正单元测试，覆盖 7、8、10、12 位、非数字和带空白输入。

### MEDIUM-2：缺少服务层失败与竞态测试

证据：

- 当前只有 Controller 集成测试和手机号纯函数测试。
- 11-02 任务书要求服务层直接测试，以及“数据库更新失败不能伪造成功”。
- 当前测试无法证明更新条件包含 ACTIVE 状态。

必须补充：

- `UserProfileServiceImplTest` 或等价服务层测试。
- DISABLED 用户直接调用读取服务失败。
- DISABLED 用户直接调用更新服务失败。
- 用户在首次读取后变为 DISABLED，条件更新失败且不修改资料。
- `userService.update(...)` 返回 false 时不能返回成功响应。
- 只更新昵称和头像，不触碰手机号、openid、unionid、status。

### LOW-1：头像 URL 校验建议使用结构化 URI 解析

当前 `startsWith("http://")` / `startsWith("https://")` 已能阻止明确危险协议，且后端不会请求 URL，因此不是当前阻塞项。

建议：

- 使用 `URI` 解析并通过 scheme 白名单校验。
- 使用 `Locale.ROOT` 处理大小写。
- 保持“不主动请求 URL”的 SSRF 边界。

该项可在 11-02R 同步完成，但不得扩大为上传功能或网络可达性检查。

## 4. 下一项可执行任务

任务编号：`11-02R`

目标：修复资料服务 ACTIVE 状态边界、手机号脱敏和缺失服务层测试，使 11-02 达到可集成状态。

11-03 已规划，但在本任务通过前保持锁定。

## 5. 允许修改范围

```text
src/main/java/com/petcare/user/service/impl/UserProfileServiceImpl.java
src/main/java/com/petcare/user/dto/UserProfileResponse.java
src/test/java/com/petcare/user/service/
src/test/java/com/petcare/user/dto/UserProfileResponseTest.java
src/test/java/com/petcare/user/controller/UserProfileControllerTest.java（仅必要回归测试）
```

仅在结构化 URL 校验需要时允许修改：

```text
src/main/java/com/petcare/user/dto/UpdateUserProfileRequest.java
```

## 6. 禁止修改

- `.claude/` 下任何文件。
- Schema、数据库迁移和种子数据。
- JWT、测试登录和管理员认证代码。
- 前端文件。
- 11-03 宠物档案 API。
- 地址、公开读取、后台 API、文件上传、AI、Redis。
- 与 Review Findings 无关的业务模块。

## 7. 强制 TDD 顺序

### RED-1：ACTIVE 服务层边界

先写失败测试：

1. ACTIVE 用户可直接通过服务读取资料。
2. DISABLED 用户直接调用读取服务返回 `unauthorized`。
3. DISABLED 用户直接调用更新服务返回 `unauthorized`，数据库不变。
4. 查询后、更新前用户变为 DISABLED，条件更新失败，资料不变。
5. 更新返回 false 时抛出安全业务异常，不返回响应 DTO。

不得使用只经过 JWT Filter 的 Controller 测试替代服务层测试。

### GREEN-1：状态条件修复

要求：

- 查询使用 `id + ACTIVE` 条件。
- 更新使用 `id + ACTIVE` 条件。
- 失败错误不区分不存在、禁用或删除，避免状态枚举。
- 不改动受保护字段。

### RED-2：手机号异常数据隐私测试

先修改并扩充 `UserProfileResponseTest`：

```text
13800138001 -> 138****8001
null/blank -> null
7/8/10/12 位数字 -> null
字母或混合字符 -> null
前后包含空白 -> null
```

### GREEN-2：严格手机号脱敏

- 仅接受 `^1\d{10}$`。
- 任何异常值返回 `null`。
- 不记录原始手机号。

## 8. 强制验证

```powershell
mvn "-Dtest=UserProfileControllerTest,UserProfileServiceImplTest,UserProfileResponseTest,TestLoginControllerTest,JwtTokenServiceTest,SecurityAccessTest" test
mvn test
mvn clean package "-DskipTests"
mvn -Ptc-mysql clean test
git diff --check
git status --short --branch
```

检查 Git 状态时，`.claude/scheduled_tasks.lock` 可以继续保持未跟踪，但不得进入暂存区或提交。

## 9. 建议提交顺序

```text
test: expose user profile review findings
fix: enforce active user profile boundaries
fix: harden user phone masking
```

禁止 `git add .`。必须显式暂存本任务文件。

## 10. 退出门禁

- HIGH 和 MEDIUM Findings 全部关闭。
- 服务层直接调用无法读取或修改 DISABLED 用户。
- 状态变化竞态不能修改已禁用用户。
- 异常手机号不返回可恢复的原始数字。
- 定向测试、完整测试、构建和 MySQL 门禁通过。
- `.claude/` 未被提交。
- 无 CRITICAL/HIGH Review 遗留。

全部通过后，才允许执行 `docs/37-phase-11-03-glm5-pet-profile-api-brief.md`。

## 11. 强制交接格式

```text
任务：11-02R 用户资料 Review 修复
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
安全与隐私检查：
.claude/ 处理情况：
已知风险：
待决策事项：
下一步允许执行的任务：门禁通过后仅允许 11-03
```
