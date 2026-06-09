# 阶段 7 Review 修复交接单

日期：2026-06-09

用途：本文件记录阶段 7 商品到店自提订单模块 review 问题及修复闭环状态。

## 0. 修复闭环状态

2026-06-09 复核结果：

- P1 重复确认线下收款：已修复，定向测试通过。
- P2 后台商品订单关键操作日志：已修复，成功、失败和日志写入失败容忍测试通过。
- P3 MySQL 库存并发门禁：测试类已补齐；本机 MySQL 需要 root 密码且 Docker 服务未运行，因此尚未获得真实 MySQL 通过证据。
- 标准 `mvn test`：通过。
- `mvn "-Djacoco.skip=true" test`：509 个测试全部通过。

阶段 7 仍保留的风险只有 P3 的真实 MySQL 执行证据缺失。进入后续阶段时必须保留该风险，不能声称真实 MySQL 库存并发门禁已通过。

---

## 1. 当前结论

阶段 7 代码已经具备商品目录、购物车、订单创建和后台订单流转的主体实现，当前 `mvn test` 与 `mvn clean package` 在 2026-06-09 的复核中可通过。

但阶段 7 仍有以下未闭环问题，**不建议直接宣称阶段 7 已完全通过门禁并进入阶段 8 持续开发**：

1. 重复确认线下收款仍可成功，违反阶段 7 状态规则。
2. 后台商品订单关键操作未写入 `admin_operation_log`，违反项目强制规则。
3. 报告中声称的 MySQL 库存并发测试类并不存在，真实并发门禁仍未落地。

此外，当前工作区分支是：

```text
phase-8-ai
```

而阶段 7 报告声称分支为：

```text
phase-7-product-orders
```

后续 Agent 在修复前应先确认当前分支策略是否仍符合 `docs/06-git-safety-workflow.md` 与 `AGENTS.md`。

---

## 2. 必修问题

### P1：禁止重复确认线下收款

#### 问题说明

当前实现中，后台对 `READY_FOR_PICKUP` 订单执行一次确认收款后：

- `payment_status = OFFLINE_PAID`
- `pickup_status = PICKED_UP`
- `status` 仍保持 `READY_FOR_PICKUP`

此时再次调用 `confirm-payment`，仍会通过 `validateCanConfirmPayment` 校验，因为当前只检查 `status == READY_FOR_PICKUP`，没有检查是否已付款、是否已自提。

这违反了阶段 7 规则中的：

```text
禁止重复确认收款。
```

#### 当前涉及文件

- `src/main/java/com/petcare/product/domain/ProductOrderStateMachine.java`
- `src/main/java/com/petcare/product/service/impl/ProductOrderTransactionServiceImpl.java`
- `src/test/java/com/petcare/product/service/ProductOrderTransactionServiceTest.java`
- 如有需要：
  `src/test/java/com/petcare/product/controller/AdminProductOrderControllerTest.java`

#### 修改要求

1. 补充“确认收款”专用校验规则：
   - 必须是 `READY_FOR_PICKUP`
   - `payment_status` 必须仍为 `UNPAID`
   - `pickup_status` 不能已经是 `PICKED_UP`
2. 第二次确认收款必须返回业务错误，不能幂等成功。
3. 错误码保持在现有产品订单错误码体系内，不要新增不必要的模糊错误。

#### 最低测试要求

必须新增测试覆盖：

1. 已 `OFFLINE_PAID` 的订单再次确认收款，应失败。
2. 已 `PICKED_UP` 的订单再次确认收款，应失败。
3. 失败后不得再次写库更新。

---

### P2：后台关键订单操作必须写入 `admin_operation_log`

#### 问题说明

根据项目规则和权限设计文档，以下动作必须写后台操作日志：

- 确认订单
- 备货完成 / 待自提
- 确认线下收款
- 完成订单
- 普通取消订单
- 缺货取消订单

当前商品订单后台链路中未接入 `AdminOperationLogService`，因此这些操作虽然完成了业务状态流转，但没有审计记录。

#### 当前涉及文件

- `src/main/java/com/petcare/product/service/impl/AdminProductOrderServiceImpl.java`
- `src/main/java/com/petcare/product/service/impl/ProductOrderTransactionServiceImpl.java`
- `src/main/java/com/petcare/admin/service/AdminOperationLogService.java`
- `src/main/java/com/petcare/admin/entity/AdminOperationLog.java`

如现有日志封装工具已存在，也可复用对应公共能力。

#### 修改要求

1. 为以下后台动作补充操作日志写入：
   - `confirm`
   - `ready`
   - `confirm-payment`
   - `complete`
   - `cancel`
   - `out-of-stock`
2. 日志至少应包含：
   - `admin_id`
   - `module`
   - `operation`
   - `request_method` 或等价动作标识
   - `request_url` 或等价资源路径
   - `result`
   - `error_message`（失败时）
   - `create_time`
3. 不要记录密码、JWT、API Key、完整敏感请求体。
4. 日志写入失败如何处理需要明确：
   - 若项目已有统一策略，按统一策略执行。
   - 若无统一策略，至少要在实现中说明是“日志失败不影响主业务”还是“日志失败回滚事务”，不能隐式决定后无说明。

#### 最低测试要求

至少补充以下一种验证方式：

1. Service 集成测试验证成功操作写入日志。
2. Service 集成测试验证失败操作也写入失败日志。

如果本轮无法完整覆盖失败日志，也必须在交接报告里明确剩余缺口。

---

### P3：补齐真实 MySQL 库存并发门禁测试

#### 问题说明

阶段 7 报告中写到用户可执行：

```powershell
mvn "-Dtest=ProductInventoryConcurrencyIT" test
```

但当前仓库中不存在 `ProductInventoryConcurrencyIT`。现有商品订单测试主要是：

- 纯规则单元测试
- Mockito Service 测试
- H2/Spring MVC 控制器测试

因此“两个并发订单竞争最后库存时只有一个成功”这条阶段 7 硬门禁，目前**没有真实 MySQL 8 证据**。

#### 当前涉及文件

- `src/test/java/com/petcare/product/`
- 如需新增 MySQL profile 或测试配置：
  - `src/test/resources/`
  - `pom.xml`
  - 现有测试 profile 配置文件

#### 修改要求

1. 新增真实 MySQL 并发集成测试类，类名可用：
   - `ProductInventoryConcurrencyIT`
2. 必须验证：
   - 两个并发订单竞争最后一件库存时，只有一个成功。
   - 失败的一方返回明确业务错误，而不是脏数据成功。
   - 库存最终不为负数。
   - 订单主表和订单项不会留下半成品数据。
3. 如需专用测试数据准备逻辑，必须可重复执行。
4. 不要用 H2 代替这条门禁。

#### 最低测试要求

至少完成：

1. 新增可运行的 MySQL 并发测试类。
2. 在交接报告中提供实际执行命令。
3. 若本机无 MySQL，必须明确写：
   - 未执行原因
   - 替代验证
   - 剩余风险

---

## 3. 建议修改顺序

后续 Agent 建议按以下顺序修复：

1. 修 P1：重复确认收款状态漏洞。
2. 修 P2：后台订单操作日志。
3. 补 P3：MySQL 并发门禁测试类与执行说明。
4. 完整复跑阶段 7 验证命令。

这样可以先修业务规则缺陷，再补审计，再补门禁证据。

---

## 4. 建议验收命令

至少执行：

```powershell
git status --short --branch
git log --oneline -5
mvn test
mvn clean package
git diff --check
```

若已补齐 MySQL 并发测试类，再执行：

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/petcare_o2o?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的本地密码"
mvn "-Dtest=ProductInventoryConcurrencyIT" test
```

---

## 5. 后续 Agent 交接格式要求

后续 Agent 完成修复后，建议在回复中明确说明：

```text
任务：
阶段：
分支：
提交：
已完成：
未完成：
变更文件：
验证命令：
验证结果：
覆盖率：
已知风险：
待决策事项：
下一步允许执行的任务：
```

并额外单列：

```text
阶段 7 review 修复项闭环情况：
- P1 重复确认收款：已修复 / 未修复
- P2 后台操作日志：已修复 / 未修复
- P3 MySQL 并发门禁：已补齐并执行 / 已补齐未执行 / 未补齐
```

---

## 6. 本轮 review 结论摘要

本轮 review 不要求立刻重写阶段 7，大部分主体实现可保留；后续 Agent 需要聚焦修复以下 3 个点：

1. 收款状态机漏洞。
2. 后台操作日志缺失。
3. MySQL 并发门禁缺失。

在这 3 项未闭环前，不建议把阶段 7 视为完全通过。
