# 阶段 11-02：GLM5.1 用户资料 API 实施任务书

日期：2026-06-13

状态：已规划，当前锁定

解锁条件：`docs/34-phase-11-01-review-and-remediation-plan.md` 全部门禁通过

执行对象：GLM5.1

目标分支：`phase-11-user-prerequisites`

## 1. 任务目的

实现当前登录用户查看和修改本人资料的最小后端能力，为后续宠物、地址和小程序用户中心提供可靠身份与资料契约。

本任务只处理当前用户自己的资料，不提供管理员用户管理、任意用户查询、手机号修改、头像文件上传或微信资料同步。

## 2. 前置门禁

开始编码前必须确认：

- 11-01R 全部 HIGH/MEDIUM Findings 已关闭。
- `mvn test` 通过。
- `mvn -Ptc-mysql clean test` 通过。
- USER Token 只能访问 USER 角色接口。
- 禁用或删除用户的 Token 返回 401。
- 工作区干净。

如果任一项不满足，停止，不得开始 11-02。

## 3. API 契约

### 3.1 获取当前用户资料

```http
GET /api/v1/user/profile
Authorization: Bearer <USER JWT>
```

成功业务数据：

```json
{
  "userId": "2065485434912788483",
  "nickname": "小宠主人",
  "phone": "138****8001",
  "avatarUrl": "https://example.com/avatar.png",
  "createdAt": "2026-06-13T01:00:00"
}
```

强制要求：

- `userId` 按字符串序列化，避免雪花 ID 精度丢失。
- 手机号必须脱敏，禁止返回完整手机号。
- 不返回 `openid`、`unionid`、状态、删除标记。
- 资料只能来自当前 USER Token 对应用户。

### 3.2 修改当前用户资料

```http
PUT /api/v1/user/profile
Authorization: Bearer <USER JWT>
Content-Type: application/json
```

请求：

```json
{
  "nickname": "新的昵称",
  "avatarUrl": "https://example.com/avatar.png"
}
```

强制要求：

- 请求体不得包含 `userId`、手机号、openid、unionid、状态。
- 只能修改昵称和头像 URL。
- 当前阶段不上传文件，只保存已存在的 URL 字符串。
- 修改后返回最新的脱敏资料响应。

## 4. 授权与归属规则

- Controller 必须明确要求 `ROLE_USER`，不能只依赖全局 `authenticated()`。
- 当前用户 ID 只能来自 `SecurityContextHelper.getCurrentUserId()`。
- ADMIN Token 访问 GET 或 PUT 必须返回 403。
- 无 Token、失效 Token、禁用或删除用户 Token 必须返回 401。
- API 不接受路径参数或请求体中的目标用户 ID，因此不存在查询或修改他人资料的入口。
- 不能把管理员 ID、请求参数 ID 或固定 ID 当成当前用户 ID。

## 5. DTO 与校验

建议新增：

```text
src/main/java/com/petcare/user/dto/UserProfileResponse.java
src/main/java/com/petcare/user/dto/UpdateUserProfileRequest.java
```

响应字段：

```java
String userId;
String nickname;
String maskedPhone;
String avatarUrl;
LocalDateTime createdAt;
```

为兼容阶段总计划，对外 JSON 字段可命名为 `phone`，但其值必须是脱敏值。实现前应在测试中固定该契约，禁止同时返回 `phone` 和 `maskedPhone` 两套字段。

修改请求校验：

```java
@NotBlank
@Size(max = 64)
String nickname;

@Size(max = 255)
String avatarUrl;
```

额外规则：

- 昵称写入前去除首尾空白；去除后为空返回 400 `validation_error`。
- 昵称长度必须符合 Schema 的 `VARCHAR(64)`。
- 头像 URL 长度必须符合 Schema 的 `VARCHAR(255)`。
- `avatarUrl` 允许为 `null` 或空字符串，用于清除头像；不得在本阶段强制要求公网可访问。
- 非空头像 URL 只允许 `http` 或 `https` 协议，拒绝 `javascript:`、`data:` 等危险协议。
- 后端只校验并保存 URL，不主动请求该地址，避免引入 SSRF。
- 不接受 HTML 清洗后的伪成功；昵称按普通文本保存和返回。

## 6. 服务设计

建议新增职责明确的应用服务：

```java
UserProfileResponse getCurrentProfile(Long currentUserId);
UserProfileResponse updateCurrentProfile(Long currentUserId, UpdateUserProfileRequest request);
```

要求：

- Controller 只负责获取当前用户 ID、调用服务和包装响应。
- 服务通过 `UserService` 查询和更新，不直接调用 Mapper。
- 用户不存在、禁用或已删除时返回安全的认证/资源错误，不泄露数据库细节。
- 更新必须使用只更新允许字段的显式对象或 UpdateWrapper，禁止把请求 DTO 整体映射到实体。
- 不修改手机号、openid、unionid、状态、创建时间和删除标记。
- 更新失败不能伪造成功。

手机号脱敏建议形成可直接单测的纯函数：

```text
13800138001 -> 138****8001
null/blank  -> null
短于合法手机号的数据 -> 返回安全脱敏值或 null，不返回原值
```

## 7. 允许修改范围

```text
src/main/java/com/petcare/user/controller/UserProfileController.java
src/main/java/com/petcare/user/dto/
src/main/java/com/petcare/user/service/
src/main/java/com/petcare/user/service/impl/
src/test/java/com/petcare/user/controller/UserProfileControllerTest.java
src/test/java/com/petcare/user/service/
```

仅在确有必要时允许修改：

```text
src/main/java/com/petcare/common/serialization/
```

不得为本任务修改 `SecurityConfig` 的公开路径规则；用户资料接口必须保持受保护。

## 8. 禁止修改

- Schema、数据库迁移、种子数据。
- 管理后台和小程序前端。
- 测试登录、JWT 和管理员认证逻辑，除非发现阻塞性回归并先汇报。
- 手机号修改、微信资料同步、头像上传。
- 宠物、地址、预约、订单、社区接口。
- 公开读取策略。
- Redis、AI Provider。

## 9. 强制 TDD 顺序

### RED-1：授权与读取契约

先写失败测试：

- USER Token 获取本人资料成功。
- 响应 `userId` 为字符串。
- 手机号已脱敏。
- 响应不含 openid、unionid、status、deleted。
- ADMIN Token 返回 403。
- 无 Token 返回 401。
- 禁用或删除用户 Token 返回 401。

### GREEN-1：最小读取实现

- 新增 Controller、响应 DTO 和应用服务。
- 使用当前 USER 身份，不接收目标用户 ID。
- 只返回允许字段。

### RED-2：更新契约

先写失败测试：

- USER 可以修改本人昵称和头像 URL。
- 修改后 GET 返回最新值。
- ADMIN Token 更新返回 403。
- 请求无法修改手机号、openid、unionid、status。
- 昵称为空、纯空白、超过 64 字符返回 400。
- 头像 URL 超过 255 字符返回 400。
- 头像 URL 使用非 HTTP(S) 协议返回 400。
- 数据库更新失败不能返回成功。

### GREEN-2 与重构

- 实现允许字段更新。
- 提取手机号脱敏纯函数并直接测试。
- 检查 Controller 无查询和更新业务逻辑。
- 检查没有新增任意用户资料查询入口。

## 10. 前后端契约注意事项

当前小程序占位调用为：

```text
frontend/miniapp/src/api/user.ts -> /api/user/profile
```

后端统一 API 基线为：

```text
/api/v1/user/profile
```

本任务禁止修改前端。必须在交接中记录该路径差异，留给后续前端联调任务修正，不能为了兼容占位代码增加第二个无版本后端路径。

## 11. 验证门禁

```powershell
mvn "-Dtest=UserProfileControllerTest,*UserProfile*ServiceTest,JwtTokenServiceTest,SecurityAccessTest,TestLoginControllerTest,TestLoginProfileIsolationTest" test
mvn test
mvn clean package "-DskipTests"
mvn -Ptc-mysql clean test
git diff --check
git status --short --branch
```

人工检查：

```powershell
rg -n "openid|unionid|phone|status|deleted" src/main/java/com/petcare/user
rg -n "userId" src/main/java/com/petcare/user/controller src/main/java/com/petcare/user/dto
rg -n "PreAuthorize|ROLE_USER|hasRole" src/main/java/com/petcare/user/controller
```

## 12. 建议提交顺序

```text
test: define current user profile api contract
feat: add current user profile read api
test: define current user profile update contract
feat: add current user profile update api
```

禁止 `git add .`。每个提交前运行 `git diff --check` 和对应定向测试。

## 13. 退出门禁

- 读取和修改资料接口契约全部通过。
- USER、ADMIN、匿名身份边界测试通过。
- 只能访问和修改本人资料。
- 手机号脱敏，敏感字段不返回。
- 请求不能越权修改受保护字段。
- 完整测试、构建、MySQL 门禁和 `git diff --check` 通过。
- 无 CRITICAL/HIGH Review 遗留。

通过后才允许开始 11-03 宠物档案 API。

## 14. 强制交接格式

```text
任务：11-02 用户资料 API
阶段：11
分支：
提交：
前置门禁证据：
已完成：
未完成：
变更文件：
RED 证据：
验证命令：
验证结果：
覆盖率：
授权与隐私检查：
已知风险：
前端契约差异：
待决策事项：
下一步允许执行的任务：门禁通过后仅允许 11-03
```
