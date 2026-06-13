# 阶段 11-03：GLM5.1 宠物档案 API 实施任务书

日期：2026-06-13

状态：首轮编码已完成，Review 未通过；下一步执行 11-03R

Review 修复任务：`docs/38-phase-11-03-review-and-remediation-plan.md`

执行对象：GLM5.1

目标分支：`phase-11-user-prerequisites`

## 1. 任务目的

实现当前登录用户的宠物档案创建、列表、查看、修改和软删除能力，为预约、社区发帖和后续小程序用户中心提供真实宠物数据。

本任务必须严格按照当前 `schema.sql` 和 `Pet` 实体实现，不得沿用旧计划中不存在的 `birthday`、`species`、`notes` 字段，也不得修改 Schema 适配旧 DTO 草案。

## 2. 前置门禁

开始编码前必须确认：

- 11-02R 全部 HIGH/MEDIUM Findings 已关闭。
- `mvn test` 通过。
- `mvn -Ptc-mysql clean test` 通过。
- 当前分支为 `phase-11-user-prerequisites`。
- 工作区除已知 `.claude/scheduled_tasks.lock` 外无不明修改。

任一条件不满足时停止，不得开始 11-03。

## 3. 当前真实数据模型

宠物档案现有字段：

| 字段 | 类型 | 规则 |
|---|---|---|
| `id` | BIGINT | 雪花 ID，对外必须字符串 |
| `user_id` | BIGINT | 必须由当前 USER 身份决定 |
| `name` | VARCHAR(64) | 业务必填 |
| `type` | VARCHAR(32) | `DOG`、`CAT`、`OTHER` |
| `breed` | VARCHAR(64) | 可选 |
| `gender` | TINYINT | `0` 未知、`1` 公、`2` 母 |
| `age` | DECIMAL(4,1) | 年龄，非负，最大 `999.9` |
| `weight` | DECIMAL(5,2) | kg，非负，最大 `999.99` |
| `size` | VARCHAR(32) | `SMALL`、`MEDIUM`、`LARGE` |
| `sterilized` | TINYINT | `0` 否、`1` 是 |
| `avatar_url` | VARCHAR(255) | 可选，仅保存 HTTP(S) URL |
| `remark` | VARCHAR(500) | 可选普通文本 |
| `deleted` | TINYINT | MyBatis-Plus 逻辑删除 |

## 4. API 契约

所有端点必须明确要求 `ROLE_USER`：

```http
GET    /api/v1/user/pets
GET    /api/v1/user/pets/{petId}
POST   /api/v1/user/pets
PUT    /api/v1/user/pets/{petId}
DELETE /api/v1/user/pets/{petId}
```

### 4.1 创建与修改请求

```json
{
  "name": "团子",
  "type": "CAT",
  "breed": "英国短毛猫",
  "gender": 2,
  "age": 2.5,
  "weight": 4.20,
  "size": "SMALL",
  "sterilized": 1,
  "avatarUrl": "https://example.com/pet.png",
  "remark": "性格温和"
}
```

请求中禁止出现或生效：

- `id`、`petId`。
- `userId`。
- `deleted`、`createTime`、`updateTime`。

### 4.2 响应

```json
{
  "petId": "2065606349801717762",
  "name": "团子",
  "type": "CAT",
  "breed": "英国短毛猫",
  "gender": 2,
  "age": 2.5,
  "weight": 4.20,
  "size": "SMALL",
  "sterilized": 1,
  "avatarUrl": "https://example.com/pet.png",
  "remark": "性格温和",
  "createdAt": "2026-06-13T09:00:00",
  "updatedAt": "2026-06-13T09:00:00"
}
```

强制要求：

- `petId` 为字符串。
- 不返回 `userId` 和 `deleted`。
- 列表只返回当前用户未删除的宠物，按创建时间倒序、ID 倒序稳定排序。
- 单条查看、修改和删除他人宠物统一返回 404，避免泄露资源是否存在。

## 5. 授权与归属规则

- 当前用户 ID 只能来自 `SecurityContextHelper.getCurrentUserId()`。
- Controller 和服务均不得接受请求中的 `userId` 作为归属依据。
- 查询、更新和删除条件必须同时包含 `petId + currentUserId`。
- ADMIN Token 访问所有宠物档案端点返回 403。
- 无 Token、无效 Token、禁用或删除用户 Token 返回 401。
- 用户 A 操作用户 B 宠物返回 404，且数据库不发生变化。
- 软删除后，列表、详情、更新和重复删除均视为不存在。

## 6. DTO 校验

建议新增：

```text
PetUpsertRequest
PetResponse
```

请求校验：

```text
name:        @NotBlank, @Size(max=64)，保存前 trim
type:        @NotNull, DOG|CAT|OTHER
breed:       @Size(max=64)
gender:      0..2
age:         0..999.9，最多 1 位小数
weight:      0..999.99，最多 2 位小数
size:        SMALL|MEDIUM|LARGE
sterilized:  0..1
avatarUrl:   @Size(max=255)，非空时仅 HTTP(S)，后端不得主动请求
remark:      @Size(max=500)
```

规则：

- 除 `name` 和 `type` 外，其余字段可选。
- 字符串可选字段的空字符串统一转换为 `null`，避免脏数据。
- `remark` 按普通文本保存，不执行 HTML。
- 数值使用 `BigDecimal`，禁止 DTO 使用 `Double`。
- 校验错误统一返回 400 `validation_error`。

## 7. 服务设计

建议新增职责明确的应用服务：

```java
List<PetResponse> listCurrentUserPets(Long currentUserId);
PetResponse getCurrentUserPet(Long currentUserId, Long petId);
PetResponse createCurrentUserPet(Long currentUserId, PetUpsertRequest request);
PetResponse updateCurrentUserPet(Long currentUserId, Long petId, PetUpsertRequest request);
void deleteCurrentUserPet(Long currentUserId, Long petId);
```

要求：

- Controller 只做协议适配、身份提取和响应包装。
- 应用服务通过 `PetService` 操作，不直接调用 Mapper。
- 创建时显式设置 `userId=currentUserId`。
- 更新使用显式字段白名单，禁止请求对象整体覆盖实体。
- 更新和删除必须使用 `petId + currentUserId` 条件。
- 更新或删除影响行数为 0 时返回统一 404。
- 所有写操作检查返回值，不伪造成功。

## 8. 允许修改范围

```text
src/main/java/com/petcare/user/controller/PetController.java
src/main/java/com/petcare/user/dto/ 宠物相关 DTO
src/main/java/com/petcare/user/service/PetApplicationService.java
src/main/java/com/petcare/user/service/impl/PetApplicationServiceImpl.java
src/test/java/com/petcare/user/controller/PetControllerTest.java
src/test/java/com/petcare/user/service/PetApplicationServiceImplTest.java
```

仅在确有必要时允许修改：

```text
src/main/java/com/petcare/user/service/PetService.java
src/main/java/com/petcare/user/service/impl/PetServiceImpl.java
```

## 9. 禁止修改

- `.claude/` 下任何文件。
- Schema、数据库迁移、`Pet` 实体字段。
- 用户资料、JWT、测试登录和管理员认证代码。
- 前端文件。
- 地址 API、预约 API、社区 API。
- 文件上传、AI、Redis。
- 提前增加 `birthday`、医疗记录、疫苗记录等 V1 范围外字段。

## 10. 强制 TDD 顺序

### RED-1：授权、创建与列表

先写失败测试：

- USER 创建宠物成功，数据库 `userId` 来自 Token。
- 请求伪造 `userId` 不生效。
- USER 列表只包含本人未删除宠物，并稳定排序。
- `petId` 为字符串，响应不含 `userId`、`deleted`。
- ADMIN Token 返回 403，无 Token 返回 401。
- 必填字段和枚举非法返回 400。
- 数值范围和小数位数非法返回 400。

### GREEN-1：创建与列表最小实现

- 新增 Controller、DTO 和应用服务。
- 不修改 Schema 和实体字段。
- 只实现创建与列表。

### RED-2：详情、更新、删除与归属

先写失败测试：

- 用户能查看、修改、软删除自己的宠物。
- 用户 A 查看、修改、删除用户 B 宠物均返回 404。
- 越权操作不修改数据库。
- 请求不能修改 `userId`、ID 和删除标记。
- 删除后详情、更新和重复删除返回 404。
- 更新或删除返回 false 时不能伪造成功。

### GREEN-2 与重构

- 实现详情、更新和软删除。
- 提取字段规范化和 DTO 映射。
- 检查所有写操作都有归属条件。
- 检查 Controller 无业务查询和更新逻辑。

## 11. 与现有模块的兼容要求

- 不修改预约和社区当前对 `petId` 的使用方式。
- 本任务不要求把宠物档案接入预约或社区页面。
- 软删除后，历史预约和帖子中的 `petId` 保持不变。
- 不新增物理删除。

## 12. 强制验证

```powershell
mvn "-Dtest=PetControllerTest,PetApplicationServiceImplTest,UserProfileControllerTest,TestLoginControllerTest,SecurityAccessTest" test
mvn test
mvn clean package "-DskipTests"
mvn -Ptc-mysql clean test
git diff --check
git status --short --branch
```

人工检查：

```powershell
rg -n "userId|petId|deleted" src/main/java/com/petcare/user/controller src/main/java/com/petcare/user/dto
rg -n "PetMapper" src/main/java/com/petcare/user/controller src/main/java/com/petcare/user/service
rg -n "birthday|species|notes" src/main/java/com/petcare/user
```

## 13. 建议提交顺序

```text
test: define current user pet profile contract
feat: add current user pet create and list api
test: define pet ownership and mutation contract
feat: add current user pet mutation api
```

禁止 `git add .`，不得暂存 `.claude/`。

## 14. 退出门禁

- 宠物 CRUD 与归属测试全部通过。
- ADMIN、匿名、失效用户身份边界通过。
- 用户无法查看、修改或删除他人宠物。
- 软删除行为正确。
- API 与真实 Schema 一致，无虚构字段。
- 定向测试、完整测试、构建、MySQL 门禁和 `git diff --check` 通过。
- `.claude/` 未进入提交。
- 无 CRITICAL/HIGH Review 遗留。

通过后才允许开始 11-04 地址管理 API。

## 15. 强制交接格式

```text
任务：11-03 宠物档案 API
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
授权与归属检查：
Schema 一致性检查：
.claude/ 处理情况：
已知风险：
待决策事项：
下一步允许执行的任务：门禁通过后仅允许 11-04
```
