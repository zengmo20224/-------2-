# 阶段 10F-R2B 前端 API 契约清理编码计划

日期：2026-06-11

## 1. 文档目的

本文档是任务包 `10F-R2B` 的直接编码与交接计划。

前置任务 `10F-R2A` 已由以下提交完成：

```text
edbf2a4 docs(admin-web): add verified admin API contract inventory
```

真实契约基线：

```text
docs/25-admin-web-api-contract.md
```

本任务只清理不依赖 D-011、D-012 决策的前端 API 契约问题，不修改后端，不新增业务页面，不声称雪花 ID 精度问题或当前门店上下文问题已经解决。

进度更新（2026-06-11）：

- `10F-R2B` 已由提交 `fb1ca59`、`08ab84b`、`d963ad4` 完成。
- `npm run test:contract` 已通过 `98` 项测试。
- `npm run build` 已通过。
- 下一阶段被 D-011、D-012 阻塞，执行计划见 `docs/28-phase-10f-r2c-r2e-cross-layer-contract-plan.md`。

## 2. 当前并发工作区保护

编写本计划时，工作区存在其他 Agent 或用户的未提交变更：

```text
frontend/admin-web/src/api/community.ts
frontend/admin-web/src/api/staff.ts
frontend/admin-web/src/__tests__/api-contract.test.ts
frontend/admin-web/src/__tests__/status-contract.test.ts
frontend/admin-web/src/types/status.ts
frontend/admin-web/src/views/community/reports.vue
frontend/admin-web/src/views/product-order/index.vue
src/main/java/com/petcare/admin/service/impl/AdminManagementServiceImpl.java
src/test/java/com/petcare/admin/controller/AdminManagementControllerTest.java
docs/risk-management-report.md
frontend/package.json
frontend/package-lock.json
```

强制规则：

- 上述文件默认属于用户或其他 Agent。
- 清单是编写时快照；开工时若出现更多未提交文件，也必须按相同规则保护。
- R2B Agent 开工时必须重新运行 `git status --short --branch` 和 `git diff --stat`。
- 如果相关前端文件中的修改仍未提交，必须先理解并保留现有修改，禁止覆盖、回退或重复实现。
- 后端 Java 文件、`docs/risk-management-report.md` 和两个前端根级 npm 文件禁止暂存或提交。
- 必须使用路径限定的 `git add -- <paths>`。

## 3. 任务目标

完成后，管理后台前端 API 层必须满足：

1. 不再导出或调用后端不存在的接口。
2. 查询参数只包含真实 Controller 接受的字段。
3. 请求类型的必填性与后端 Bean Validation 一致。
4. 响应类型的字段和可空性与真实 DTO 一致。
5. 真实但暂未接入页面的接口被保留并明确标记，不能被误判为伪接口。
6. 契约回归测试可以阻止伪接口、错误路径和关键 DTO 形状重新出现。

## 4. 本任务允许与禁止范围

### 允许修改

- `frontend/admin-web/src/api/**`
- `frontend/admin-web/src/types/api.ts`
- `frontend/admin-web/src/__tests__/*-contract.test.ts`
- `frontend/admin-web/package.json` 中的 `test:contract` 脚本
- 因响应字段修正而无法编译的现有页面
- `docs/25-admin-web-api-contract.md` 中经代码实证发现的清单错误

### 禁止修改

- 所有后端 Java、`schema.sql`
- 小程序工程
- 路由、菜单和新业务页面
- 预约、订单和内容审核状态机
- 权限码与 API 路径
- AI 页面、评论审核页面、技能编辑页面等新功能
- `STORE_ID = 1` 的最终解决方案
- 所有实体 ID 的 `number` 到 `string` 迁移
- 未决 D-011、D-012 的实现

## 5. 已确认的立即清理项

### 5.1 员工技能伪接口

后端真实存在：

```text
PUT /api/v1/admin/staff/{id}/skills
```

后端不存在：

```text
GET /api/v1/admin/staff/{id}/skills
```

当前工作区已经出现移除 `getStaffSkills` 的未提交修改。R2B Agent 必须：

- 保留并核对该修改，不重复覆盖。
- 保留真实的 `updateStaffSkills`。
- 明确标注完整技能编辑流程被“缺少读取接口”阻塞。
- 增加测试，防止 `getStaffSkills` 再次导出。

### 5.2 服务项目查询参数

后端列表接口仅接受：

```text
page
size
status
```

必须从 `ServiceItemQueryParams` 删除无效的 `name` 字段，并检查页面是否展示或发送该无效筛选。

### 5.3 门店更新请求必填性

后端 `StoreUpdateRequest` 中：

```text
storeName：必填
status：必填
```

必须将 `StoreUpdateParams.storeName`、`StoreUpdateParams.status` 改为必填。

注意：

- 本任务只修正请求类型。
- 固定 `STORE_ID = 1` 属于 D-012，不得在本任务自行解决。
- 门店页面若因类型修正无法构建，只允许做最小兼容修改。

### 5.4 举报响应与处理请求必须分开理解

真实 `PostReport` 响应包含：

```text
reasonType
handlerId
```

真实 `PostReport` 响应不包含：

```text
handleRemark
```

但真实 `AdminReportHandleRequest` 请求仍然包含可选字段：

```text
handleRemark
```

因此必须：

- 从 `PostReport` 响应类型删除 `handleRemark`。
- 为 `PostReport` 增加 `reasonType` 和 `handlerId`。
- 保留 `AdminReportHandleParams.handleRemark`。
- 保留举报处理页面提交处理说明的能力。
- 禁止因为响应没有 `handleRemark` 而误删合法请求字段。

## 6. 非 ID 字段可空性修正矩阵

本任务不得改变 ID 类型，只修正非 ID 字段和已确认可空外键的可空性。

| 文件 | 类型 | 必须修正为可空的字段 |
| --- | --- | --- |
| `api/auth.ts` | `AdminLoginResult.admin`、`AdminUserInfo` | `nickname`、`role`、`permissions` 按真实响应可空性修正 |
| `api/store.ts` | `StoreInfo` | `phone`、`address`、`longitude`、`latitude`、`businessHours`、`description` |
| `api/service.ts` | `ServiceItem` | `petType`、`petSize`、`description`、`coverUrl`、`sort` |
| `api/staff.ts` | `StaffMember` | `phone`、`avatarUrl`、`description` |
| `api/staff.ts` | `StaffSchedule` | `remark` |
| `api/booking.ts` | `Booking` | `petId`、`staffId`、`addressId`、`distanceKm`、`remark`、`merchantRemark` |
| `api/product.ts` | `Product` | `coverUrl`、`description`、`sort` |
| `api/product-order.ts` | `ProductOrder` | `remark`、`confirmTime`、`completeTime`、`cancelTime` |
| `api/product-order.ts` | `ProductOrderDetail` | `remark`、`merchantRemark` |
| `api/product-order.ts` | `ProductOrderItem` | `productCoverUrl` |
| `api/community.ts` | `Post` | `petId`、`topicId`、`publishTime` |
| `api/community.ts` | `Comment` | `parentId` |
| `api/community.ts` | `PostReport` | `reasonType`、`handleResult`、`handlerId`、`handleTime` |
| `api/moderation.ts` | `SensitiveWord` | `category` |
| `api/operation-log.ts` | `OperationLog` | `errorMessage` 已正确可空，保持不变 |

执行规则：

- 以 `docs/25-admin-web-api-contract.md` 和真实 DTO 为准。
- Java DTO 未使用 `@JsonInclude` 不代表数据库字段一定非空，必须按真实构造与文档核对。
- 页面因可空类型出现编译错误时，使用明确占位显示或安全分支，不使用非空断言掩盖风险。
- 禁止在本任务中把所有字段粗暴改成可选。

## 7. 真实但未完整接入的接口

以下接口是真实接口，R2B 不得删除：

```text
PUT  /api/v1/admin/staff/{id}/skills
POST /api/v1/admin/bookings/{id}/reassign
POST /api/v1/admin/product-orders/{id}/out-of-stock
GET/POST /api/v1/admin/community/comments/**
```

处理要求：

- 保留 API Client 函数。
- 添加简短注释，说明真实 HTTP 方法、路径、权限码和“页面未完整接入”状态。
- 不在 R2B 中新增入口、按钮、表单或页面。
- AI 分析与用量 API 当前没有前端文件，本任务不新增。

## 8. 测试驱动执行顺序

### 10F-R2B-01：保护并发工作区

目标：明确已有未提交变更归属，避免覆盖。

必须执行：

```powershell
git status --short --branch
git diff --stat
git diff -- frontend/admin-web/src/api/community.ts frontend/admin-web/src/api/staff.ts frontend/admin-web/src/types/status.ts frontend/admin-web/src/views/community/reports.vue
```

完成条件：

- 已理解相关前端未提交修改。
- 已明确本任务允许继续修改的文件。
- 未触碰后端和未跟踪保护文件。

### 10F-R2B-02：建立 RED API 契约测试

新增建议文件：

```text
frontend/admin-web/src/__tests__/api-contract.test.ts
```

测试必须覆盖：

1. `staff` API 不导出 `getStaffSkills`。
2. `updateStaffSkills` 使用 `PUT /v1/admin/staff/{id}/skills`。
3. `getServiceItems` 只传递 `page`、`size`、`status`。
4. 预约改派和订单缺货使用真实方法与路径。
5. 举报处理请求允许传递 `handleRemark`。
6. `PostReport` 响应类型不再声明 `handleRemark`。
7. 关键请求必填性与响应可空性有类型断言保护。

测试方式：

- 使用 Vitest mock 统一 `request` 模块，验证方法、路径和请求参数。
- 使用 `expectTypeOf` 或受 `vue-tsc` 检查的类型用例验证 DTO 形状。
- 不调用真实后端。
- 不通过读取并匹配源码字符串代替主要行为断言。

`test:contract` 必须同时运行已有状态契约测试和新增 API 契约测试。

### 10F-R2B-03：清理路径、查询和请求 DTO

目标：用最小实现使路径、查询参数和请求类型测试通过。

允许修改：

```text
frontend/admin-web/src/api/store.ts
frontend/admin-web/src/api/service.ts
frontend/admin-web/src/api/staff.ts
frontend/admin-web/src/__tests__/api-contract.test.ts
frontend/admin-web/package.json
```

必须完成：

- 核对并保留员工技能伪接口移除结果。
- 删除 `ServiceItemQueryParams.name`。
- 将 `StoreUpdateParams.storeName`、`status` 改为必填。
- 保留真实但未完整接入接口。

建议提交：

```text
test(admin-web): add API contract regression tests
fix(admin-web): remove invalid API assumptions
```

### 10F-R2B-04：修正响应字段与可空性

目标：按第 6 节矩阵对齐响应类型，并保持页面可构建。

允许修改：

- `frontend/admin-web/src/api/**`
- 因类型修正直接失败的现有页面
- API 契约测试

必须完成：

- 修正 `PostReport` 响应字段，同时保留处理请求中的 `handleRemark`。
- 修正非 ID 字段可空性。
- 修正 API 文件中的错误状态注释，例如服务和商品状态必须使用真实值。
- 页面明确处理空值，不使用伪造默认业务数据。

禁止：

- 修改 ID 的 `number` 类型。
- 扩展页面功能。
- 顺手重构页面布局。

建议提交：

```text
fix(admin-web): align API DTO fields and nullability
```

### 10F-R2B-05：Review 与验收

正确性 Review：

- 是否仍存在不存在的 API。
- 请求字段是否与 Bean Validation 一致。
- 响应字段和可空性是否与真实 DTO 一致。
- 是否错误删除真实但未接入接口。

安全与范围 Review：

- 是否触碰 D-011、D-012。
- 是否误提交后端或其他 Agent 文件。
- 是否将 ID 转为 JavaScript 数字以外的新形式并声称解决。
- 是否在页面伪造成功或业务数据。

## 9. 验证门禁

最低验证命令：

```powershell
cd frontend/admin-web
npm run test:contract
npm run build

cd ../../
git diff --check
git status --short --branch
```

附加搜索：

```powershell
rg "getStaffSkills|ServiceItemQueryParams" frontend/admin-web/src
rg "STORE_ID = 1|id: number" frontend/admin-web/src/api
```

解释：

- 第一条搜索用于确认伪接口已消失，并人工检查服务查询类型。
- 第二条搜索必须仍能发现 D-011、D-012 风险；R2B 不得把它们伪装为已解决。

## 10. Git 提交规则

- 测试与最小契约修复可以拆成两个提交。
- 类型可空性修正建议形成独立提交。
- 每次提交前运行 `git diff --check` 和对应测试。
- 只暂存 R2B 明确修改的前端文件。
- 禁止暂存当前后端 Java 变更和受保护未跟踪文件。

建议提交序列：

```text
test(admin-web): add API contract regression tests
fix(admin-web): remove invalid API assumptions
fix(admin-web): align API DTO fields and nullability
```

## 11. 完成定义

只有满足以下条件，才能声明 `10F-R2B` 完成：

- API 契约回归测试已建立并通过。
- `getStaffSkills` 不再导出或调用。
- 无效服务项目 `name` 查询已移除。
- 门店更新请求必填性已对齐。
- `PostReport` 响应和举报处理请求被正确区分。
- 非 ID 字段可空性已按真实契约修正。
- 真实但未接入接口被保留并标记。
- `npm run test:contract`、`npm run build`、`git diff --check` 通过。
- 提交未包含其他 Agent 或用户的现有变更。

以下问题在完成 R2B 后仍然存在：

- D-011：雪花 ID JSON 安全传输策略未决定。
- D-012：当前门店上下文获取方式未决定。
- 固定 `STORE_ID = 1` 仍是已知阻塞。
- 前端实体 ID 仍为 `number`，不能声明安全联调。

## 12. 下一位 Agent 开工指令

```text
任务包 ID：10F-R2B-01 至 10F-R2B-05
目标：清理不依赖 D-011、D-012 的管理后台前端 API 契约
允许修改：frontend/admin-web/src/api/**、API 契约测试、因类型修正直接失败的现有页面、test:contract 脚本
禁止修改：后端、小程序、数据库、ID 字符串迁移、固定门店 ID 方案、未跟踪保护文件
依赖：提交 edbf2a4；docs/25-admin-web-api-contract.md
RED 测试：伪接口、错误查询参数、请求必填性和响应字段形状
验证：npm run test:contract、npm run build、git diff --check
预期提交：test(admin-web): add API contract regression tests；fix(admin-web): remove invalid API assumptions；fix(admin-web): align API DTO fields and nullability
```

`10F-R2B` 完成后，必须等待用户决定 D-011、D-012。未决定前，禁止开始 `10F-R2C`、`10F-R2D`、`10F-R2E`、`10F-R3` 或 `10G`。
