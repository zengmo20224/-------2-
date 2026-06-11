# 阶段 10F-R2 管理后台 API 契约清理代码计划

日期：2026-06-11

## 1. 执行目的

本文档是阶段 `10F-R2` 的直接编码计划。

阶段 `10F-R1` 已通过以下提交完成：

```text
f6463f9 test(admin-web): add backend status contract regression tests
6c5d6ef fix(admin-web): align status dictionaries and action guards with backend
```

当前验证结果：

- `npm run test:contract`：`74` 项通过。
- `npm run build`：通过。
- 管理后台仍存在 API 路径、DTO、ID 类型和门店上下文契约风险。

本阶段目标是让管理后台 API Client 只表达真实、精确、可安全传输的后端契约。未解决的后端缺口必须被明确阻塞，不能由前端猜测或伪造。

## 2. 执行边界

### 允许修改

- `frontend/admin-web/src/api/**`
- `frontend/admin-web/src/types/**`
- 与契约变更直接相关的管理后台页面和测试
- 本阶段新增的管理后台 API 契约清单
- 用户批准 D-011 或 D-012 后，对应独立后端契约任务明确允许的文件

### 禁止修改

- 小程序工程
- 预约、订单等后端业务状态机
- 数据库业务状态、权限码和核心业务规则
- D-007、D-008 未决能力
- 与 API 契约无关的页面视觉重构

### 当前受保护未跟踪文件

- `docs/risk-management-report.md`
- `frontend/package.json`
- `frontend/package-lock.json`

任何 Agent 不得擅自删除、修改、暂存或提交这些文件。

## 3. 已确认契约问题

### 3.1 CRITICAL：雪花 BIGINT ID 精度风险

项目使用 MyBatis-Plus 雪花 ID，后端 DTO 广泛使用 Java `Long`，但管理后台目前将所有 ID 定义为 TypeScript `number`。

雪花 ID 通常超过 JavaScript 的 `Number.MAX_SAFE_INTEGER`。风险包括：

- 浏览器解析 JSON 数字时 ID 已经失真。
- 详情、更新、禁用、审核等请求可能操作错误资源。
- 将前端类型改成 `string` 不能单独解决问题，因为后端仍可能输出 JSON 数字。

该问题必须通过 D-011 决策后，以后端和前端两个独立提交解决。

### 3.2 HIGH：固定门店 ID 假设

`frontend/admin-web/src/api/store.ts` 当前固定：

```ts
const STORE_ID = 1
```

这与雪花 ID 策略不兼容，也没有真实接口保证门店 ID 为 `1`。

当前管理员 `/api/v1/admin/auth/me` 响应不包含 `storeId`，后端也没有“当前门店”接口。该问题必须通过 D-012 决策解决。

### 3.3 HIGH：不存在的员工技能读取接口

前端当前定义：

```text
GET /api/v1/admin/staff/{id}/skills
```

真实后端只存在：

```text
PUT /api/v1/admin/staff/{id}/skills
```

前端不能在无法读取当前技能的情况下伪装完整技能编辑流程。

### 3.4 MEDIUM：查询参数与后端不一致

`ServiceItemQueryParams` 当前包含 `name`，但真实后端服务项目列表只接受：

```text
page
size
status
```

所有 API 查询类型必须删除后端不支持的字段，避免前端展示无效筛选。

### 3.5 MEDIUM：请求 DTO 必填性不一致

例如：

- 后端 `StoreUpdateRequest.storeName` 和 `status` 必填。
- 前端 `StoreUpdateParams` 将全部字段定义为可选。

前端请求类型必须与后端 Bean Validation 和 Controller 语义一致，不能允许构造后端必然拒绝的请求。

### 3.6 MEDIUM：响应字段与可空性不一致

示例：

- `PostReport` 后端返回 `reasonType`、`handlerId`，不返回前端定义的 `handleRemark`。
- 多个 DTO 中的 `petId`、`staffId`、`addressId`、时间和备注字段可能为空。
- 前端多个接口把可空字段定义为必有字符串或数字。

必须逐个对照真实 DTO、实体返回和数据库约束修正。

### 3.7 功能存在但页面未接入

以下接口真实存在，但暂未完整接入页面：

- 评论审核 API。
- 员工技能替换 API。
- 订单缺货 API。
- 预约改派 API。
- AI 分析报告和 AI 用量 API。

这些不是伪接口。R2 只记录契约和可用状态，不在同一任务中扩展全部页面。

## 4. 新增决策门禁

### D-011：雪花 ID 的 JSON 传输策略

可选方案：

1. 后端将所有对外 `Long` ID 序列化为 JSON 字符串，前端统一使用 `string`。推荐。
2. 仅对指定 DTO ID 字段使用字符串序列化。变更较分散，容易遗漏。
3. 前端使用特殊 JSON BigInt 解析器。Axios 默认 JSON 解析前可能已经丢失精度，复杂度高。
4. 限制 ID 小于 JavaScript 安全整数。与既定雪花 ID 策略冲突，不推荐。

在用户决定前：

- 可以完成接口盘点、删除伪接口和修正非 ID 字段。
- 禁止声称管理后台资源操作已经安全联调。
- 禁止只把 TypeScript `number` 改为 `string` 后宣称解决。

### D-012：单门店后台如何获得当前门店 ID

可选方案：

1. 新增“当前门店”后台接口，例如 `/api/v1/admin/store/current`。推荐，避免前端携带任意门店 ID。
2. 在 `/api/v1/admin/auth/me` 中返回 `storeId`。需要明确管理员与门店关系。
3. 使用环境变量提供门店 ID。适合临时演示，但部署容易配置错误。
4. 保持固定 `STORE_ID = 1`。与雪花 ID 冲突，禁止。

在用户决定前：

- 门店信息和配置页面必须标记为“门店上下文未确定”。
- 禁止继续依赖固定 ID `1` 完成联调声明。

## 5. 强制执行顺序

```text
10F-R2A 建立真实管理后台 API 契约清单
-> 10F-R2B 清理不依赖决策的前端契约问题
-> 用户决定 D-011 和 D-012
-> 10F-R2C 后端 ID 传输契约修复
-> 10F-R2D 前端 ID 契约迁移
-> 10F-R2E 当前门店上下文契约修复
-> 10F-R2F 契约回归、Review 与交接
```

`10F-R2A` 与 `10F-R2B` 可以立即执行。`10F-R2C` 至 `10F-R2E` 必须等待对应决策。

进度更新（2026-06-11）：

- `10F-R2A` 已由提交 `edbf2a4` 完成。
- 真实契约清单已生成：`docs/25-admin-web-api-contract.md`。
- 当前只允许按 `docs/26-phase-10f-r2b-frontend-contract-cleanup-plan.md` 执行 `10F-R2B`。

## 6. 任务包 10F-R2A：建立真实 API 契约清单

目标：生成管理后台真实 API 的唯一核对清单。

交付物：

```text
docs/25-admin-web-api-contract.md
```

每个接口必须记录：

- 模块和页面。
- HTTP 方法和路径。
- 后端 Controller。
- 权限码。
- 查询参数和请求 DTO。
- 响应 DTO。
- ID 字段。
- 可空字段。
- 当前前端 API 函数。
- 状态：已接入、未接入、阻塞、伪接口。

必须覆盖：

- 管理员认证。
- 门店信息和配置。
- 服务项目。
- 员工、技能和排班。
- 预约。
- 商品和库存。
- 自提订单。
- 帖子、评论和举报。
- 敏感词。
- 操作日志。
- AI 分析报告和用量。

验证方式：

- 所有清单项必须能定位到真实 Controller。
- 清单中的权限码必须与 `@PreAuthorize` 一致。
- 不得从前端代码反推并假设后端接口存在。

建议提交：

```text
docs(admin-web): add verified admin API contract inventory
```

## 7. 任务包 10F-R2B：清理不依赖决策的前端契约

目标：删除伪接口并修正可以直接确认的类型问题。

必须完成：

1. 删除 `getStaffSkills` 伪接口。
2. 保留真实 `updateStaffSkills`，但标记为“缺少读取接口，暂不开放完整编辑流程”。
3. 删除 `ServiceItemQueryParams.name` 等后端不支持的查询字段。
4. 修正 `StoreUpdateParams` 等请求 DTO 的必填性。
5. 修正 `PostReport` 字段：
   - 增加 `reasonType`。
   - 增加 `handlerId`。
   - 删除不存在的 `handleRemark`。
6. 按真实 DTO 修正非 ID 字段的可空性。
7. 为所有保留 API 添加真实方法、路径和权限注释。
8. 将只定义未接入的真实 API 标记为未接入，不删除。

测试要求：

- 增加 API 路径和方法契约测试。
- 增加关键请求 DTO 形状测试。
- 测试必须证明不存在员工技能读取请求。

禁止事项：

- 不修改后端。
- 不实现新页面。
- 不处理 ID 字符串迁移。
- 不处理门店上下文。

退出门禁：

```powershell
npm run test:contract
npm run build
git diff --check
```

建议提交：

```text
test(admin-web): add API contract regression tests
fix(admin-web): remove invalid API assumptions
```

## 8. 任务包 10F-R2C：后端 ID 传输契约修复

开始条件：用户决定 D-011。

目标：保证雪花 ID 在 JSON 传输中不丢失精度。

若用户批准推荐方案，实施要求：

- 后端所有对外 ID 序列化为 JSON 字符串。
- 请求仍可安全反序列化为 Java `Long`。
- 统一处理嵌套 DTO、列表和 `List<Long>`。
- 不把金额、数量、分页数值错误转换为字符串。
- 更新统一 API 契约文档。

测试要求：

- 使用大于 `Number.MAX_SAFE_INTEGER` 的 ID。
- 验证登录、门店、员工、预约、订单、社区和 AI 代表性响应中的 ID 为精确字符串。
- 验证字符串 ID 请求可以正确进入后端 `Long` 参数。
- 后端全量测试通过。

Git 要求：

- 后端契约修复必须形成独立提交。
- 不能与前端迁移混入同一提交。

建议提交：

```text
test(api): add snowflake ID serialization contract tests
fix(api): serialize external snowflake IDs as strings
```

## 9. 任务包 10F-R2D：前端 ID 契约迁移

开始条件：10F-R2C 已提交并验证。

目标：管理后台统一使用安全的 ID 类型。

实施要求：

- 建立统一类型：

```ts
export type EntityId = string
```

- 所有实体 ID、外键 ID、路径 ID 和 ID 数组使用 `EntityId`。
- 禁止使用 `Number(id)`、`parseInt(id)` 或算术处理 ID。
- 表单选择器值和路由参数保持字符串。
- API Client 路径插值必须保持原始 ID。

测试要求：

- 使用超过 JavaScript 安全整数范围的 ID 字符串。
- 验证详情、更新、禁用、审核和状态动作请求路径不改变 ID。
- 验证 ID 数组提交保持字符串。

退出门禁：

- `rg` 检查管理后台 API 类型中不再存在实体 `id: number`。
- 契约测试和构建通过。

建议提交：

```text
test(admin-web): add snowflake ID preservation tests
fix(admin-web): migrate API entity IDs to strings
```

## 10. 任务包 10F-R2E：当前门店上下文契约

开始条件：用户决定 D-012。

目标：删除固定 `STORE_ID = 1`，让门店页面基于真实上下文工作。

实施要求由 D-012 决定，但必须满足：

- 前端不能允许管理员通过修改 URL 任意切换门店。
- 后端必须校验当前管理员访问范围。
- 门店上下文不可用时页面显示阻塞状态，不能请求 ID `1`。
- 门店信息和门店配置使用同一上下文来源。

测试要求：

- 不存在固定门店 ID。
- 无门店上下文时不发出更新请求。
- 非法门店访问被后端拒绝。
- 门店信息和配置读取、更新路径一致。

建议提交：

```text
test(api): add current store access contract tests
feat(api): expose authorized current store context
fix(admin-web): use authorized current store context
```

后端与前端必须拆成独立提交。

## 11. 任务包 10F-R2F：契约验收与交接

必须执行：

- 对照 `docs/25-admin-web-api-contract.md` 逐项检查。
- 搜索残留伪接口、固定门店 ID 和不安全 ID 类型。
- 运行前端契约测试和构建。
- 若执行了后端契约修复，运行后端全量测试。
- 执行正确性和安全 Review。
- 确认无 CRITICAL/HIGH 契约问题遗留。

最低验证命令：

```powershell
cd frontend/admin-web
npm run test:contract
npm run build

cd ../../
git diff --check
git status --short --branch
```

若执行了 10F-R2C 或 10F-R2E 的后端部分：

```powershell
mvn test
mvn package "-DskipTests"
```

## 12. 阶段完成定义

只有满足以下条件，才能声明 `10F-R2` 完成：

- API 契约清单存在并与真实 Controller 一致。
- 不存在确认的伪接口。
- 不存在固定 `STORE_ID = 1`。
- 雪花 ID 可以跨 JSON 和前端安全传输。
- 请求 DTO 必填性、查询参数和响应字段已对齐。
- 所有契约测试和构建通过。
- CRITICAL/HIGH 契约问题为零。
- 每个跨层变更均按后端、前端拆分提交。

如果 D-011 或 D-012 未决定，只能声明：

```text
10F-R2A/R2B 已完成，10F-R2 被决策门禁阻塞。
```

不能进入 10F-R3 或 10G。

## 13. 当前 Agent 开工指令

```text
任务包 ID：10F-R2B
目标：清理不依赖 D-011、D-012 的前端 API 契约问题
允许修改：frontend/admin-web/src/api/**、API 契约测试、因类型修正直接失败的现有页面、test:contract 脚本
禁止修改：后端、小程序、数据库、ID 字符串迁移、固定门店 ID 方案、未跟踪保护文件
依赖：提交 edbf2a4；docs/25-admin-web-api-contract.md
验证：npm run test:contract、npm run build、git diff --check
预期提交：见 docs/26-phase-10f-r2b-frontend-contract-cleanup-plan.md
```

`10F-R2B` 完成后必须等待 D-011、D-012 决策。未决定前，禁止执行 `10F-R2C` 至 `10F-R2E`。
