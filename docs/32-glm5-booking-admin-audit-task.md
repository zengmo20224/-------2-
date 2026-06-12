# GLM-5.1 单任务包：预约后台高风险操作审计补齐

日期：2026-06-12

任务包 ID：`QA-B01`

任务性质：阶段 11 开始前的后端质量门禁修复

## 1. 任务目标

为预约后台的全部写操作补齐可靠的管理员操作日志，并通过直接测试证明：

- 成功操作与业务修改处于同一事务，成功日志写入失败时业务必须回滚。
- 失败操作使用独立事务记录 `FAIL` 日志。
- 失败日志写入失败不能覆盖原始业务异常。
- 未知异常不能把 SQL、堆栈、密钥或原始敏感信息写入操作日志。
- 用户侧预约取消不写管理员操作日志。

本任务只处理预约后台审计，不处理社区审计，不进入阶段 11，不修改未决事项。

## 2. 开工基线

开始前必须阅读：

1. `AGENTS.md`
2. `docs/05-testing-and-verification.md`
3. `docs/06-git-safety-workflow.md`
4. `docs/07-integration-gates.md`
5. `docs/08-pending-decisions.md`
6. `docs/09-booking-concurrency-control.md`
7. `docs/10-admin-permission-design.md`
8. `docs/22-continuous-agent-development-rules.md`
9. 本文档

必须先运行：

```powershell
git status --short --branch
git log --oneline -5
git branch --show-current
```

建议从 `phase-10-frontend` 最新提交创建独立分支：

```powershell
git switch -c fix/booking-admin-audit
```

如果工作区存在其他 Agent 或用户的未提交修改，停止编辑并先汇报归属，不允许覆盖、回退或混入提交。

## 3. 必须覆盖的后台操作

以下 `AdminBookingController` 写操作必须记录管理员操作日志：

| 操作 | 权限码 | 请求路径 | 审计 operation 建议值 |
| --- | --- | --- | --- |
| 确认预约 | `booking:booking:confirm` | `POST /api/v1/admin/bookings/{id}/confirm` | `confirm-booking` |
| 拒绝预约 | `booking:booking:reject` | `POST /api/v1/admin/bookings/{id}/reject` | `reject-booking` |
| 开始服务 | `booking:booking:start` | `POST /api/v1/admin/bookings/{id}/start` | `start-booking` |
| 完成服务 | `booking:booking:complete` | `POST /api/v1/admin/bookings/{id}/complete` | `complete-booking` |
| 管理员取消预约 | `booking:booking:cancel` | `POST /api/v1/admin/bookings/{id}/cancel` | `cancel-booking` |
| 改派员工 | `booking:booking:reassign` | `POST /api/v1/admin/bookings/{id}/reassign` | `reassign-booking` |

统一审计字段：

- `module`：`booking`
- `requestMethod`：`POST`
- `requestUrl`：使用不含查询参数的真实后台路径
- `adminId`：使用服务入口接收的 `operatorId`
- `result`：只允许 `SUCCESS` 或 `FAIL`
- `requestParams`：只保存目标预约 ID；改派可额外保存新员工 ID。禁止保存完整请求体、备注和取消原因
- `errorMessage`：业务异常最多保留 1000 字符；未知异常固定写 `unexpected_error`

按 D-017，V1 不扩展 `admin_operation_log` Schema。目标预约 ID 放入脱敏后的 `requestParams`，请求 ID 字段缺口只在交接中报告，不得自行扩表。

## 4. 允许修改范围

- `src/main/java/com/petcare/booking/service/impl/BookingApplicationServiceImpl.java`
- `src/main/java/com/petcare/booking/service/impl/BookingTransactionServiceImpl.java`
- 为预约审计新增的、范围最小的后端辅助类
- `src/test/java/com/petcare/booking/` 下与预约管理员审计直接相关的测试

确有编译需要时，允许最小修改：

- `src/main/java/com/petcare/booking/service/BookingTransactionService.java`

新增辅助类前，必须证明它用于消除成功日志与失败日志的真实重复逻辑；禁止为本任务建立通用事件总线、AOP 审计框架或新的基础设施层。

## 5. 禁止修改范围

- `schema.sql`、数据库表、索引和迁移策略
- Controller 路径、请求 DTO、响应 DTO、权限码和预约状态名称
- 预约状态机、预约并发锁顺序、排班规则和距离规则
- 用户侧预约取消行为
- 社区、订单、AI、认证和前端代码
- `docs/08-pending-decisions.md` 中的任何决策状态
- 已有测试断言的强度、测试跳过配置和覆盖率排除项

不得通过在 Controller 中直接写日志完成任务。审计必须位于能够覆盖真实业务结果和事务状态的 Service 层。

## 6. 事务与错误处理设计要求

### 6.1 成功日志

- `SUCCESS` 日志必须与预约状态更新、预约状态日志处于同一事务。
- 改派成功日志必须与员工改派和预约状态日志处于同一事务。
- `AdminOperationLogService.save(...)` 返回 `false` 或抛出异常时，必须让业务事务失败并回滚。
- 不允许捕获成功日志异常后仅打印警告并继续返回成功。

### 6.2 失败日志

- 失败日志必须在后台公开服务入口统一捕获后写入，确保事务内异常已经回滚。
- 使用现有 `AdminOperationLogService.saveFailLog(...)` 的 `REQUIRES_NEW` 行为。
- 失败日志写入异常只能记录警告，随后重新抛出原始业务异常。
- 每次后台操作失败最多写一条 `FAIL` 日志，禁止应用层和事务层重复记录。

### 6.3 敏感信息

- `BusinessException` 可以记录截断后的用户安全消息。
- 其他 `RuntimeException` 只能记录 `unexpected_error`。
- 禁止记录 SQL、堆栈、JWT、密钥、完整备注、完整取消原因或完整请求体。

## 7. 强制 TDD 测试

先新增能够失败的测试并保存 RED 证据，再进行实现。

建议新增 `BookingAdminAuditTest`，至少覆盖：

| 测试名建议 | 必须断言 |
| --- | --- |
| `confirmSuccess_writesSuccessAudit` | 确认成功产生一条包含管理员 ID、预约 ID、路径和 `SUCCESS` 的日志 |
| `reassignSuccess_writesAuditInBusinessTransaction` | 改派成功日志包含预约 ID 和新员工 ID |
| `successAuditFailure_rollsBackBookingTransition` | 成功日志保存失败时，预约状态和预约状态日志均回滚 |
| `businessFailure_writesOneFailAudit` | 非法状态等业务失败只写一条 `FAIL` 日志，并保留原始业务异常 |
| `unexpectedFailure_writesSanitizedFailAudit` | 未知异常日志只写 `unexpected_error` |
| `failAuditFailure_preservesOriginalException` | `saveFailLog` 失败时仍抛出原始业务异常 |
| `userCancel_doesNotWriteAdminAudit` | 用户取消不会生成管理员操作日志 |

还必须为拒绝、开始、完成、管理员取消至少各提供一个参数化测试或等价直接测试，证明 operation 和路径映射正确。

测试要求：

- 优先使用 Mockito 单元测试验证失败路径和脱敏规则。
- 使用 Spring 事务集成测试验证成功日志失败导致状态更新回滚。
- 不允许只验证调用次数而不验证日志关键字段。
- 不允许使用 H2 测试声称已验证 MySQL 行锁行为。

## 8. 实施步骤

1. 输出 AGENTS.md 要求的开工汇报。
2. 创建并运行 RED 测试，记录失败原因。
3. 以最小改动实现成功与失败审计。
4. 运行预约专项测试并修复回归。
5. 运行完整后端测试、打包和覆盖率报告。
6. 使用 `java-reviewer` 和 `security-reviewer` 复核事务边界、日志脱敏和重复日志风险。
7. 修复所有 CRITICAL、HIGH 问题。
8. 精确暂存本任务文件，检查差异后独立提交。

禁止把测试和实现拆成两个最终不可独立通过的提交。最终提交必须在单独检出时可以构建并通过测试。

## 9. 验证命令

```powershell
mvn "-Dtest=BookingAdminAuditTest,BookingStatusTransitionTransactionTest,BookingStatusLogRollbackTest,BookingReassignTransactionTest,AdminBookingControllerTest" test
mvn test
mvn package "-DskipTests"
git diff --check
git status --short --branch
```

必须从 `target/site/jacoco/jacoco.xml` 或等价报告中汇报完整后端行覆盖率。当前基线约为 `78.20%`，本任务不得降低覆盖率；如果仍未达到 80%，必须明确报告，不得声称覆盖率门禁已通过。

如果环境已配置独立 MySQL 8 测试库，再额外运行：

```powershell
mvn "-Dtest=BookingStatusTransitionMySqlIT,BookingReassignMySqlIT" test
```

没有测试库凭据时，必须写明“真实 MySQL 未验证”，不能使用生产数据库，也不能提交数据库密码。

## 10. 完成标准

- 六个预约后台写操作成功和失败都有直接审计测试。
- 成功审计失败会回滚对应业务状态和预约状态日志。
- 失败审计异常不会覆盖原始异常。
- 未知异常内容不会泄露到操作日志。
- 用户取消不会产生管理员操作日志。
- 所有专项测试、`mvn test`、打包和 `git diff --check` 通过。
- 无 CRITICAL 或 HIGH Review 问题遗留。
- 变更只包含本任务允许范围，并形成一个独立提交。

建议提交：

```text
fix(booking): add reliable admin operation auditing
```

## 11. 强制交接内容

按 `AGENTS.md` 的交接格式输出，并额外提供：

- RED 测试最初失败证据。
- 六个后台操作与审计 operation 的映射表。
- 成功日志与失败日志的事务边界说明。
- 完整测试数量和覆盖率。
- 是否运行真实 MySQL 测试。
- `git show --stat --oneline HEAD` 输出摘要。

## 12. 可直接发送给 GLM-5.1 的指令

```text
执行 docs/32-glm5-booking-admin-audit-task.md 中的 QA-B01 单任务包。严格先阅读 AGENTS.md 和任务文档，先完成开工汇报，再按 TDD 写 RED 测试。只允许补齐预约后台六个写操作的管理员操作审计，不得修改 Schema、API、权限码、状态机、前端、社区模块或未决事项。成功审计必须与业务事务原子提交，失败审计使用 REQUIRES_NEW 且不能覆盖原始异常，未知异常必须脱敏。完成后运行文档规定的专项测试、mvn test、打包、覆盖率和 git diff 检查，执行 Java 与安全 Review，修复所有 CRITICAL/HIGH 问题，然后只提交本任务文件并按强制格式交接。
```

## 13. 推荐 Agent 链

```text
/orchestrate custom "tdd-guide,java-reviewer,security-reviewer" "[Plan: docs/32-glm5-booking-admin-audit-task.md#QA-B01] 按 TDD 补齐预约后台确认、拒绝、开始、完成、管理员取消和改派的可靠操作审计；成功日志与业务事务原子提交，失败日志独立事务保存且异常脱敏；Acceptance: 六个操作成功失败均有直接测试，成功审计失败回滚业务，完整 mvn test 与打包通过；Out of scope: Schema、API、状态机、前端、社区和阶段 11 功能"
```
