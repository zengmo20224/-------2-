# 阶段 10F-R 验收修复与 10G 质量基建代码计划

日期：2026-06-09

## 1. 执行目的

本文档是下一位编码 Agent 的直接执行计划。

当前管理后台核心页面已在提交 `076374a` 中形成，但“可以构建”不等于“业务可用”。下一步必须先完成阶段 10F-R 的契约与行为修复，再进入阶段 10G 的测试、Lint、覆盖率和 E2E 基建。

禁止直接开始小程序、后端新功能或发布工作。

## 2. 当前基线与验证证据

当前分支：`phase-10-frontend`

当前管理后台代码提交：

```text
076374a feat(admin-web): add all admin management pages with permission controls
```

当前工作区：

- `frontend/admin-web` 已无未提交代码变更。
- `frontend/package.json` 和 `frontend/package-lock.json` 仍为未跟踪文件，归属和用途未确认。

已验证：

```powershell
cd frontend/admin-web
npm run build
```

结果：

- TypeScript 和 Vite 构建通过。
- 当前没有 `lint`、`typecheck`、`test`、`test:coverage`、`e2e` 脚本。
- 构建提示主包约 `1.0 MB`，存在大于 `500 kB` 的警告。

## 3. 已确认的高优先级问题

### 3.1 状态契约不一致

前端状态字典和操作条件必须与后端状态完全一致。

| 领域 | 当前前端错误值 | 后端真实值 |
| --- | --- | --- |
| 预约待确认 | `PENDING` | `PENDING_CONFIRM` |
| 预约服务中 | `IN_PROGRESS` | `IN_SERVICE` |
| 支付未支付 | `PENDING` | `UNPAID` |
| 支付已支付 | `PAID` | `OFFLINE_PAID` |
| 服务项目启用/禁用 | `ACTIVE` / `DISABLED` | `ON_SALE` / `OFF_SALE` |
| 员工禁用 | `DISABLED` | `INACTIVE` |
| 商品上架/下架 | `ACTIVE` / `DISABLED` | `ON_SALE` / `OFF_SALE` |
| 订单待确认 | `PENDING` | `PENDING_CONFIRM` |
| 订单备货中 | `CONFIRMED` | `PREPARING` |
| 订单待自提 | `READY` | `READY_FOR_PICKUP` |
| 自提初始状态 | `PENDING` | `WAIT_PREPARE` |

影响：

- 预约确认、拒绝、完成和取消按钮无法在正确状态展示。
- 商品、服务项目下架按钮无法在真实 `ON_SALE` 状态展示。
- 自提订单确认、备货、支付和完成操作条件错误。
- 状态筛选会向后端发送无效值。

### 3.2 接口契约不一致或未完成

- 前端定义了 `GET /api/v1/admin/staff/{id}/skills`，后端没有该接口。
- 后端只有员工技能替换接口，当前前端没有完整技能管理流程。
- 前端定义了评论审核 API，但没有评论审核页面和路由。
- 前端定义了订单缺货接口，但当前页面没有对应操作。
- 后端完成自提订单要求 `READY_FOR_PICKUP + OFFLINE_PAID + PICKED_UP`，但当前后台流程需要核对如何形成 `PICKED_UP`。
- AI 分析报告和 AI 用量接口存在，但当前管理后台没有页面。
- Dashboard 当前仅为占位页面，不得声明经营仪表盘已完成。

### 3.3 工程质量问题

- `frontend/package.json` 和 `frontend/package-lock.json` 与 `frontend/admin-web/package.json` 重复，必须先确认是否作为 workspace 根目录使用。
- 没有自动化前端测试。
- 没有 Lint 和独立类型检查命令。
- 多个页面依赖全局拦截器提示后使用空 `catch`，没有页面级错误状态和重试入口。
- `request.ts` 使用 `as never` 绕过 Axios 类型约束，需要测试保护后再改进。
- 仍存在 Vite 模板文件 `HelloWorld.vue`、`vite.svg` 和未确认使用的资源。
- 主包体积超过警告阈值，需要在功能正确后处理。

## 4. 强制执行顺序

```text
10F-R1 状态契约回归测试与修复
-> 10F-R2 API 契约清理与阻塞标记
-> 10F-R3 页面权限和失败状态修复
-> 10F-R4 管理后台功能范围补齐或明确暂缓
-> 10F-R5 工程文件归属与死代码清理
-> 10F-R6 验收 Review 与提交
-> 10G-01 至 10G-07 质量基建
```

阶段 10F-R 未通过前，不允许开始 10G。

为了满足 TDD，10F-R1 允许建立仅用于纯状态契约测试的最小 Vitest 配置和 `test:contract` 命令。完整测试环境、组件测试、覆盖率和 E2E 仍属于 10G，不能借此扩大 10F-R 范围。

## 5. 任务包 10F-R1：状态契约修复

目标：让状态字典、筛选条件和操作按钮与后端真实状态一致。

允许修改：

- `frontend/admin-web/src/types/status.ts`
- 预约、服务、员工、商品、自提订单相关 API 类型和页面
- 本任务新增的状态契约测试文件
- 运行纯状态契约测试所需的最小 Vitest 配置和 `test:contract` 脚本

禁止修改：

- 后端状态机、数据库状态值、权限码
- 社区、AI 和小程序模块

RED 测试要求：

- 为预约状态、预约支付状态、服务状态、员工状态、商品状态、订单状态和自提状态建立精确值断言。
- 为“状态对应可用操作”建立纯函数测试。
- 测试必须先证明当前 `PENDING`、`IN_PROGRESS`、`READY` 等错误值导致失败。

实现要求：

- 状态常量使用后端真实值。
- 将页面内散落的状态判断提取到集中动作策略函数。
- 状态类型使用字符串联合类型，减少任意 `string`。
- 未知状态安全展示原值，但不能启用任何写操作。

退出门禁：

- 状态契约测试通过。
- 预约和订单页面的按钮条件与后端状态机一致。
- 状态筛选只发送后端接受的值。
- `npm run test:contract` 通过。
- `npm run build` 通过。

建议提交：

```text
test(admin-web): add backend status contract regression tests
fix(admin-web): align status dictionaries and action guards
```

## 6. 任务包 10F-R2：API 契约清理

目标：清除伪造或未完成接口，建立真实接口清单。

允许修改：

- `frontend/admin-web/src/api/**`
- `frontend/admin-web/src/types/**`
- `docs/24-admin-web-api-contract.md`

必须完成：

1. 对照真实 Controller 和 DTO 核对所有管理后台 API。
2. 删除或隔离不存在的 `GET /staff/{id}/skills`。
3. 标记只定义但未被页面使用的接口。
4. 核对每个请求体字段、可空性和后端校验规则。
5. 建立管理后台接口契约文档，记录路径、权限、页面和状态。

遇到后端缺口：

- 不在本任务中修改后端。
- 将缺口记录为后续独立后端任务包。
- 对应前端入口必须隐藏、禁用或显示“后端能力未完成”。

退出门禁：

- 前端 API Client 中不存在已确认的伪造接口。
- 接口文档与 Controller 一致。
- 构建通过。

建议提交：

```text
docs(admin-web): add verified API contract inventory
fix(admin-web): remove invalid API assumptions
```

## 7. 任务包 10F-R3：权限、错误和交互状态修复

目标：确保无权限和失败情况下页面行为明确且可恢复。

允许修改：

- 路由、布局、认证 Store、请求层
- 管理后台现有页面
- 对应测试文件

测试要求：

- 无 Token 跳转登录。
- 登录后无路由权限进入 `403`。
- 菜单不展示无权限模块。
- `401` 清理会话。
- `403` 不自动重试。
- `409` 和 `422` 展示业务提示。
- 列表请求失败显示错误状态和重试入口。

实现要求：

- 页面区分 Loading、Empty、Error、Forbidden、Success。
- 写操作提交期间禁止重复点击。
- 高影响操作保留二次确认。
- 不能只依赖空 `catch` 和全局消息作为页面错误状态。

退出门禁：

- 权限和错误映射测试通过。
- 页面失败后可以重试。
- 未授权入口不会短暂展示受限内容。

## 8. 任务包 10F-R4：功能范围核对

目标：准确区分“已实现”“需要补齐”“后端阻塞”“暂缓”。

必须核对：

| 模块 | 当前处理 |
| --- | --- |
| 评论审核页面 | 若只需现有 API 即可完成，则实现；否则记录阻塞 |
| 员工技能管理 | 后端缺少读取接口时，不得伪造当前技能 |
| 订单缺货操作 | 对照后端权限与状态后决定是否展示 |
| 自提完成流程 | 明确 `PICKED_UP` 如何形成；缺少后端路径则阻塞完成按钮 |
| AI 分析和用量 | 后端接口存在，可作为独立只读页面任务 |
| Dashboard | 保持占位并明确暂缓，不伪装为完成 |

退出门禁：

- 每个模块都有明确状态。
- 被阻塞功能不会显示为可用。
- 不需要后端修改的缺失管理页面已形成后续独立任务包。

## 9. 任务包 10F-R5：工程文件归属和死代码

目标：清理工程边界，但不误删其他 Agent 文件。

必须先决定：

- `frontend/package.json` 是否作为 workspace 根配置。
- `frontend/package-lock.json` 是否与根配置配套。
- 当前项目统一从仓库根、`frontend/` 还是 `frontend/admin-web/` 执行 npm 命令。

处理规则：

- 未确认前，不删除两个未跟踪文件。
- 如果不采用 workspace，保留 `frontend/admin-web/package.json` 作为唯一管理后台依赖入口。
- 如果采用 workspace，必须补充明确的 workspace 配置和运行文档。
- 删除未使用模板资源前，先通过引用搜索证明无使用方。

退出门禁：

- 依赖安装路径和锁文件唯一且可复现。
- 不再包含确认无用的 Vite 模板代码。
- 新 Agent 能根据 README 执行构建。

## 10. 任务包 10F-R6：验收与 Review

必须执行：

- 正确性 Review：状态、请求体、动作条件。
- 安全 Review：Token、权限、错误泄漏。
- 可维护性 Review：重复状态判断、空错误处理、类型逃逸。
- 构建验证。
- `git diff --check`。
- 路径限定 Git 提交。

阶段 10F-R 完成标准：

- CRITICAL 和 HIGH 问题为零。
- 状态契约错误已修复。
- 伪造接口已移除或隔离。
- 被阻塞功能明确不可用。
- 构建通过。
- 形成独立 Git 提交和标准交接。

## 11. 阶段 10G：质量基建详细任务

阶段 10F-R 完成后，按以下顺序执行。

### 10G-01：固定运行环境

- 固定 Node.js 主版本。
- 明确 npm 工作目录。
- 增加 `.nvmrc` 或等效版本说明。
- 确保只提交必要锁文件。

### 10G-02：建立命令门禁

在 `frontend/admin-web/package.json` 建立：

```json
{
  "scripts": {
    "lint": "...",
    "typecheck": "vue-tsc --noEmit",
    "test": "vitest run",
    "test:coverage": "vitest run --coverage",
    "e2e": "playwright test",
    "build": "vue-tsc -b && vite build"
  }
}
```

命令必须真实可运行，不能设置为空命令或始终成功命令。

### 10G-03：Vitest 与组件测试基础

- 配置 Vitest、jsdom、Vue Test Utils。
- 建立统一测试初始化。
- 为状态组件和权限组件提供首批示例测试。

### 10G-04：基础关键逻辑测试

必须覆盖：

- `request.ts` 响应解包和错误映射。
- Token 注入和 `401` 清理。
- 用户 Store 登录、恢复和退出。
- 路由守卫和权限判断。
- 状态字典和动作策略。

### 10G-05：统一页面状态组件

建立并测试：

- Loading
- Empty
- Error + Retry
- Forbidden
- Feature Disabled

页面逐步接入，禁止一次性无测试改写全部页面。

### 10G-06：Playwright E2E 基础

- 建立 Playwright 配置。
- 明确后端、前端启动方式和测试 Base URL。
- 实现管理员登录与会话恢复首个 E2E。
- 测试账号和凭据通过测试环境配置提供，不硬编码生产秘密。

### 10G-07：覆盖率与构建体积

- 配置覆盖率报告。
- 请求层、认证、权限、状态策略目标覆盖率不低于 80%。
- 记录构建体积基线。
- 在不破坏功能的前提下处理主包大于 `500 kB` 的警告。

## 12. 阶段 10G 退出门禁

```powershell
cd frontend/admin-web
npm ci
npm run lint
npm run typecheck
npm run test
npm run test:coverage
npm run build
npm run e2e
git diff --check
git status --short --branch
```

完成要求：

- 所有命令存在并通过，或 E2E 外部依赖阻塞有明确证据。
- 基础关键逻辑覆盖率达到 80%。
- 不存在伪造测试成功。
- 文档、脚本和实际运行方式一致。
- 形成阶段 10G 独立 Git 提交。

## 13. 下一位 Agent 开工指令

```text
任务包 ID：10F-R1
目标：修复管理后台状态契约和动作条件
允许修改：状态字典、预约/服务/员工/商品/订单相关前端文件、对应测试
禁止修改：后端、小程序、数据库、社区和 AI 页面
依赖：提交 076374a；后端真实状态枚举与 schema.sql
RED 测试：证明错误状态值导致动作条件失效
验证：状态测试、npm run build、git diff --check
预期提交：
  test(admin-web): add backend status contract regression tests
  fix(admin-web): align status dictionaries and action guards
```

下一位 Agent 不允许跳过 10F-R1 直接搭建 E2E 或开始小程序。
