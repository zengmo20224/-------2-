# 阶段 10F-R2C 至 R2E 跨层契约修复编码计划

日期：2026-06-11

## 1. 文档目的

本文档定义 `10F-R2B` 完成后的下一阶段编码任务。

当前真实基线：

```text
fb1ca59 fix(admin-web): correct order action guards, report filters, and remove fake API
08ab84b test(admin-web): add API contract regression tests
d963ad4 fix(admin-web): align API DTO fields and nullability with backend
```

已验证：

- `npm run test:contract`：`98` 项通过。
- `npm run build`：通过。
- 构建仍有主包约 `1.046 MB` 的警告，该问题不属于本阶段。

下一阶段目标：

1. 解决雪花 ID 跨 JSON 和 JavaScript 传输时的精度风险。
2. 删除管理后台固定 `STORE_ID = 1` 假设。
3. 建立服务端受控的当前门店访问契约。
4. 完成跨后端、前端的契约回归与验收。

## 2. 决策结果与执行门禁

决策结果（2026-06-11）：

- 用户已批准 D-011 推荐方案。
- 用户已批准 D-012 推荐方案。
- 决策门禁已解除，下一任务只允许为 `10F-R2C1`。

历史约束：

- 用户决定前禁止执行 `10F-R2C`、`10F-R2D`、`10F-R2E`；该决策门禁现已解除。
- 仍禁止 Agent 采用全局 Long 序列化、BigInt 解析器或固定门店 ID。
- 禁止进入 `10F-R3` 或 `10G`。
- 必须继续按任务包顺序执行，每次只领取一个任务包。

## 3. 已批准决策

### 3.1 D-011 已批准方案

决定：

```text
所有对外 API 的雪花 ID 字段序列化为 JSON 字符串；
仅处理 ID 字段，不全局转换所有 Java Long；
前端统一使用 EntityId = string。
```

不允许使用“所有 `Long` 全局转字符串”的原因：

- `PageResponse.total` 是 `long`，它是数量，不是 ID。
- Token 数、统计值和其他非 ID Long 未来也可能存在。
- 全局转换会破坏分页、统计和数值计算契约。

实施方向：

- 后端为单个雪花 ID 字段建立统一 Jackson 注解或等效机制。
- 为 `List<Long>` ID 集合建立独立的内容序列化机制。
- 请求中的 JSON 字符串 ID、路径 ID 和查询 ID仍安全反序列化为 Java `Long`。
- 金额、数量、分页和统计字段保持 JSON 数字。

### 3.2 D-012 已批准方案

决定：

```text
新增服务端解析的当前门店契约；
管理后台前端不再传入固定门店 ID；
旧 {id} 管理接口必须校验目标 ID 等于当前门店。
```

批准的 API：

```text
GET   /api/v1/admin/stores/current
PATCH /api/v1/admin/stores/current
GET   /api/v1/admin/stores/current/config
PUT   /api/v1/admin/stores/current/config
```

V1 当前没有管理员与门店关联字段，因此当前门店解析规则必须是：

1. 查询未删除的门店。
2. 恰好一条时返回该门店。
3. 没有门店时返回明确的资源不存在错误。
4. 多于一条时返回明确的配置冲突错误。
5. 禁止静默选择第一条记录。

旧 `{id}` 管理接口的兼容策略：

- 可以暂时保留，避免一次性破坏已有调用。
- 每次访问必须验证 `{id}` 等于服务端解析出的当前门店 ID。
- 不相等时拒绝访问，不能依赖前端隐藏。
- 管理后台迁移完成后再单独决定是否移除旧接口。

## 4. 当前工作区保护

编写本计划时存在以下未提交工作：

```text
src/main/java/com/petcare/admin/service/impl/AdminManagementServiceImpl.java
src/test/java/com/petcare/admin/controller/AdminManagementControllerTest.java
docs/risk-management-report.md
frontend/package.json
frontend/package-lock.json
```

这些文件默认属于用户或其他 Agent。

强制规则：

- 每个任务包开工前重新运行 `git status --short --branch` 和 `git diff --stat`。
- `AdminManagementServiceImpl.java` 和 `AdminManagementControllerTest.java` 与 R2E 潜在重叠；未提交变更存在时，禁止覆盖，必须先理解并基于其继续。
- 未经用户确认归属，禁止暂存或提交三个未跟踪文件。
- 后端、前端必须使用独立提交。

## 5. 总体执行顺序

```text
用户决定 D-011、D-012
-> 更新 docs/08-pending-decisions.md 并提交
-> 10F-R2C1 ID 契约测试与字段清单
-> 10F-R2C2 后端 ID 序列化实现
-> 10F-R2D1 前端 EntityId 契约测试
-> 10F-R2D2 前端 ID 类型迁移
-> 10F-R2E1 当前门店后端契约
-> 10F-R2E2 管理后台当前门店迁移
-> 10F-R2F 跨层契约验收
```

约束：

- D-011 决定后，可以执行 R2C、R2D。
- D-012 决定后，可以执行 R2E。
- 如果只决定其中一项，只能执行对应任务链。
- R2C 后端提交完成并验证前，禁止执行 R2D。

## 6. 任务包 10F-R2C1：后端 ID 契约测试与字段清单

开始条件：用户已决定 D-011，并已更新决策文档。

目标：建立能证明雪花 ID 精度安全的后端 RED 契约测试。

允许修改：

- 后端 ID JSON 契约测试。
- `docs/25-admin-web-api-contract.md` 的 ID 清单。
- 必要的测试夹具。

禁止修改：

- 生产实现。
- 前端。
- 当前门店接口。

测试必须使用：

```text
9007199254740993
```

该值大于 JavaScript `Number.MAX_SAFE_INTEGER`。

代表性响应必须覆盖：

- 管理员登录和 `/admin/auth/me`。
- 门店、服务、员工与排班。
- 预约。
- 商品和订单，包含嵌套订单项。
- 帖子、评论、举报和敏感词。
- 操作日志。
- AI 分析和 AI 用量。
- `List<Long>` ID 集合，例如员工技能分类 ID。

必要断言：

- 所有 ID 在 JSON 中是精确字符串。
- `PageResponse.total`、页码、数量、金额、库存和 Token 数仍为 JSON 数字。
- 字符串路径 ID、查询 ID 和请求体 ID 可以进入 Java `Long` 参数。
- 非法 ID 字符串返回可控 `400`，不泄漏堆栈。

RED 证据：

- 当前大 ID 响应仍输出 JSON 数字，测试应失败。
- 不允许通过降低 ID 值使测试通过。

建议提交：

```text
test(api): add snowflake ID serialization contract tests
```

## 7. 任务包 10F-R2C2：后端 ID 字段序列化

开始条件：`10F-R2C1` RED 测试已提交。

目标：仅将对外雪花 ID 字段序列化为 JSON 字符串。

推荐实现边界：

- 建立统一的单 ID 序列化注解或等效机制。
- 建立 `List<Long>` ID 集合的内容序列化机制。
- 在响应 DTO、必要的只读响应模型和嵌套响应中显式标记 ID。
- 不对 Java `Long` 或 `long` 注册全局字符串序列化器。
- 不修改实体数据库字段类型。
- 不修改 Mapper 和 `schema.sql`。

必须覆盖的 ID：

- `id`、`userId`、`adminId`、`storeId`、`staffId`、`petId`。
- `serviceItemId`、`serviceCategoryId`、`categoryId`、`productId`。
- `postId`、`topicId`、`commentId`、`parentId`、`reporterId`、`handlerId`。
- `conversationId`、`createdBy`。
- 所有嵌套 DTO 和 ID 集合。

Review 要点：

- 是否遗漏公开用户端 DTO。
- 是否误将分页总数或统计数转为字符串。
- 是否对空 ID 保持 `null`。
- 是否影响 JWT 内部 subject 或数据库类型。

验证：

```powershell
mvn test
mvn package "-DskipTests"
git diff --check
```

建议提交：

```text
fix(api): serialize external snowflake IDs as strings
```

## 8. 任务包 10F-R2D1：前端 ID 保真契约测试

开始条件：R2C 后端提交已完成并验证。

目标：建立超过安全整数范围的 ID 字符串在前端请求路径和请求体中不发生变化的 RED 测试。

允许修改：

- `frontend/admin-web/src/__tests__/id-contract.test.ts`
- `frontend/admin-web/package.json` 的 `test:contract`
- 必要的测试辅助类型

禁止修改：

- 前端生产 API 类型和页面。
- 后端。
- 当前门店功能。

测试 ID：

```text
9007199254740993
```

必须覆盖：

- 详情、更新、停用、审核和状态动作路径。
- 员工技能 ID 数组。
- 员工、排班、预约改派和商品请求体中的外键 ID。
- 页面选择器或表单值不执行 `Number()`、`parseInt()` 或算术处理。

建议提交：

```text
test(admin-web): add snowflake ID preservation tests
```

## 9. 任务包 10F-R2D2：管理后台 EntityId 迁移

开始条件：`10F-R2D1` RED 测试已提交。

目标：管理后台所有实体 ID 使用安全字符串。

统一类型：

```ts
export type EntityId = string
```

实施要求：

- 所有实体 ID、外键 ID、路径 ID 和 ID 数组使用 `EntityId`。
- 页面表格、选择器、表单和临时状态保持字符串。
- API 路径插值保持原始 ID。
- 删除 ID 的 `Number()`、`parseInt()` 和算术转换。
- 分页、数量、价格、库存、统计数仍使用 `number`。
- 不把任意业务字符串误改为 `EntityId`。

最低搜索门禁：

```powershell
rg "id: number|Id: number|number\\[\\]" frontend/admin-web/src/api
rg "Number\\(|parseInt\\(" frontend/admin-web/src
```

搜索结果必须逐项人工判断，不能机械替换。

验证：

```powershell
cd frontend/admin-web
npm run test:contract
npm run build
```

建议提交：

```text
fix(admin-web): migrate API entity IDs to strings
```

## 10. 任务包 10F-R2E1：当前门店后端契约

开始条件：用户已决定 D-012，并已更新决策文档。

目标：由服务端解析当前唯一门店，禁止前端选择任意门店。

允许修改：

- 管理后台门店 Controller、Service 和相关测试。
- 当前门店解析器或等效小范围抽象。
- 管理后台 API 契约文档。

禁止修改：

- `admin_user`、`store`、`store_config` Schema。
- 多门店能力。
- JWT Claim 和管理员门店关联模型。
- 前端。

RED 测试必须覆盖：

- 恰好一个门店时，四个 `/stores/current` 接口正常工作。
- 没有门店时返回明确错误。
- 多于一个门店时返回配置冲突，不能选择第一条。
- 无权限管理员仍返回 `403`。
- 旧 `{id}` 接口访问非当前门店时被拒绝。
- 更新操作仍写入操作日志。

接口和权限：

| 接口 | 权限码 |
| --- | --- |
| `GET /api/v1/admin/stores/current` | `store:info:read` |
| `PATCH /api/v1/admin/stores/current` | `store:info:update` |
| `GET /api/v1/admin/stores/current/config` | `store:config:read` |
| `PUT /api/v1/admin/stores/current/config` | `store:config:update` |

实现约束：

- 当前门店解析必须在服务端完成。
- 不把门店 ID 加入前端可控请求参数。
- 不默认选择最小 ID、第一条或 `1`。
- 多门店属于 V1 配置冲突，不提前实现多门店切换。

建议提交：

```text
test(api): add current store access contract tests
feat(api): expose authorized current store context
```

## 11. 任务包 10F-R2E2：管理后台当前门店迁移

开始条件：R2E1 后端接口已提交并验证。

目标：删除固定 `STORE_ID = 1`，统一使用 `/stores/current`。

允许修改：

- `frontend/admin-web/src/api/store.ts`
- 门店信息和配置页面。
- 当前门店 API 契约测试。

禁止修改：

- 后端。
- 其他管理页面。
- 多门店选择 UI。
- 环境变量门店 ID。

测试必须覆盖：

- API Client 不再包含固定门店 ID。
- 门店信息和配置使用 `/stores/current`。
- 页面不允许通过 URL 或输入框切换门店。
- 当前门店不存在或配置冲突时显示明确错误状态。
- 更新请求不会发送任意门店 ID。

建议提交：

```text
test(admin-web): add current store API contract tests
fix(admin-web): use authorized current store context
```

## 12. 任务包 10F-R2F：跨层契约验收

必须执行：

- 对照 `docs/25-admin-web-api-contract.md` 更新接口状态。
- 搜索残留不安全 ID 类型、ID 数值转换和固定门店 ID。
- Review 后端 ID 字段覆盖范围。
- Review 当前门店权限和资源范围。
- 运行后端全量测试、前端契约测试和构建。
- 确认 CRITICAL/HIGH 契约问题为零。

最低验证：

```powershell
mvn test
mvn package "-DskipTests"

cd frontend/admin-web
npm run test:contract
npm run build

cd ../../
git diff --check
git status --short --branch
```

完成后才允许进入 `10F-R3`。

## 13. Git 提交边界

推荐提交顺序：

```text
docs: decide snowflake ID and current store contracts
test(api): add snowflake ID serialization contract tests
fix(api): serialize external snowflake IDs as strings
test(admin-web): add snowflake ID preservation tests
fix(admin-web): migrate API entity IDs to strings
test(api): add current store access contract tests
feat(api): expose authorized current store context
test(admin-web): add current store API contract tests
fix(admin-web): use authorized current store context
docs(admin-web): update verified API contract inventory
```

禁止：

- 将后端和前端实现混入同一提交。
- 将 D-011、D-012 两个决策隐式混入代码提交。
- 提交其他 Agent 的审计日志修改或未跟踪文件。

## 14. 完成定义

只有满足以下条件，才能声明 `10F-R2` 完成：

- D-011、D-012 已被用户明确决定并记录。
- 对外雪花 ID 在 JSON 中精确传输为字符串。
- 非 ID 数值仍保持 JSON 数字。
- 管理后台所有实体 ID 使用 `EntityId = string`。
- 不存在固定 `STORE_ID = 1`。
- 当前门店由服务端解析并受权限保护。
- 所有契约测试、后端测试和构建通过。
- CRITICAL/HIGH 契约问题为零。

## 15. 下一位 Agent 开工指令

当前状态：

```text
任务包 ID：10F-R2C1
目标：建立后端雪花 ID JSON 传输 RED 契约测试和字段清单
允许修改：后端 ID JSON 契约测试、docs/25-admin-web-api-contract.md、必要测试夹具
禁止修改：生产实现、前端、当前门店接口、数据库、未跟踪保护文件
依赖：D-011 已决定
RED 证据：使用 9007199254740993 证明当前 ID 输出仍为 JSON 数字
完成条件：ID 与非 ID Long 契约均有直接测试；测试按预期形成 RED；独立提交
```

禁止跳过 `10F-R2C1` 直接实现 R2C2、R2D 或 R2E。
