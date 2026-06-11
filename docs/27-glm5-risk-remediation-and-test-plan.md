# GLM-5.1 项目风险修复与测试执行计划

日期：2026-06-11

## 1. 文档目的

本文档将项目总体扫描发现的确定性缺陷、工程风险和缺失能力拆分为可独立验收的任务包，供后续 GLM-5.1 严格按 TDD 执行。

本文档不是一次性修改整个项目的授权。GLM-5.1 每次只能领取一个任务包，并且必须先满足当前阶段门禁、工作区保护和未决事项约束。

执行时必须同时遵守：

- `AGENTS.md`
- `docs/05-testing-and-verification.md`
- `docs/06-git-safety-workflow.md`
- `docs/07-integration-gates.md`
- `docs/08-pending-decisions.md`
- `docs/09-booking-concurrency-control.md`
- `docs/21-remaining-development-roadmap.md`
- `docs/22-continuous-agent-development-rules.md`
- 当前阶段实施计划

如果本文档与上述文件冲突，按 `AGENTS.md` 中的优先级处理。发现冲突时停止相关实现，不得自行选择。

## 2. 扫描基线与限制

扫描基线：

- 分支：`phase-10-frontend`
- 扫描时提交：`08ab84b`
- 当前阶段：`10F-R2B`
- D-011：雪花 ID 的 JSON 传输策略，状态为未决
- D-012：单门店后台如何获得当前门店 ID，状态为未决

扫描验证结果：

| 验证项 | 结果 | 说明 |
| --- | --- | --- |
| `mvn test` | PASS | 519 个默认测试通过 |
| `mvn package "-DskipTests"` | PASS | 后端可打包 |
| `npm run test:contract` | PASS | 98 个前端契约测试通过 |
| `npm run build` | PASS | 主 JS 包约 1,046 KB，存在大包警告 |
| `npm audit --json` | PASS | 扫描时未发现 npm 漏洞 |
| 后端覆盖率 | FAIL | 指令约 76.32%，分支约 61.82%，低于 80% 目标 |
| 真实 MySQL 验证 | 未验证 | 本地 MySQL 凭据不可用 |

重要限制：

- 默认 `mvn test` 不会执行命名为 `*IT` 的测试。
- `BookingConcurrencyIT` 固定使用 `@ActiveProfiles("test")`，其注释中的 MySQL 运行命令不能将它真正切换到 MySQL。
- H2 的 MySQL 兼容模式不能证明 SQL 在真实 MySQL 8 中可执行，也不能证明 InnoDB 行锁行为正确。
- 扫描时工作区存在其他 Agent 或用户的未提交变更。后续 Agent 必须重新运行 `git status --short --branch` 和 `git diff --stat`，不能依赖本文档中的旧快照。
- `docs/risk-management-report.md`、`frontend/package.json`、`frontend/package-lock.json` 在扫描时为未跟踪文件，禁止擅自删除、覆盖或提交。

## 3. GLM-5.1 强制执行规则

### 3.1 开工门禁

任何任务包开始前，GLM-5.1 必须先输出：

```text
已阅读文档：
任务包 ID：
当前阶段：
当前分支：
当前提交：
已有未提交变更及归属：
本次允许修改：
本次不会修改：
依赖和阻塞：
RED 测试或失败证据计划：
验证计划：
预期提交：
```

未完成上述汇报前，不允许修改文件。

### 3.2 单任务包规则

- 一次只能执行一个任务包。
- 不允许一次同时修改后端、管理后台、小程序、数据库 Schema 和部署文件。
- 跨层问题必须拆成后端契约任务、前端迁移任务和联调任务。
- 当前 `10F-R2B` 未完成前，禁止直接开始后端缺陷修复、10G、小程序或发布任务，除非用户明确批准调整阶段顺序。
- D-011、D-012 未决定前，禁止实现其相关代码。

### 3.3 TDD 强制规则

每个缺陷修复必须按以下顺序执行：

1. 新增能够稳定复现缺陷的测试。
2. 运行测试并证明因目标缺陷失败，形成 RED 证据。
3. 提交 RED 测试检查点。
4. 实现最小修复，不做无关重构。
5. 重跑同一测试并形成 GREEN 证据。
6. 运行模块测试和完整门禁。
7. Review 正确性、安全、事务、并发和测试有效性。
8. 提交修复检查点。

禁止通过以下方式获得绿色结果：

- 删除、跳过或禁用失败测试。
- 把精确断言改为宽泛范围断言。
- 添加 `Thread.sleep` 掩盖竞态。
- 使用 H2 结果代替真实 MySQL 结果。
- 捕获异常后静默忽略。
- 使用固定 ID、假 Token、假接口或假成功响应。
- 降低覆盖率阈值。

### 3.4 完成门禁

任务包只有同时满足以下条件才能声明完成：

- RED 证据和 GREEN 证据均存在。
- 目标测试、模块测试和规定的完整测试通过。
- 没有未解决的 CRITICAL 或 HIGH Review 问题。
- `git diff --check` 通过。
- 工作区中其他 Agent 的变更仍然存在且未被纳入本任务提交。
- 变更已使用独立 Conventional Commit 提交。

## 4. 总体优先级与执行顺序

### 4.1 当前阶段必须先处理

| 顺序 | 任务 | 状态 |
| --- | --- | --- |
| 1 | 按 `docs/26-phase-10f-r2b-frontend-contract-cleanup-plan.md` 完成并验收 `10F-R2B` | 当前任务 |
| 2 | 等待用户决定 D-011 和 D-012 | 阻塞 |
| 3 | 完成 `10F-R2C` 至 `10F-R6` | 依赖 D-011、D-012 |
| 4 | 执行 10G 管理后台质量基建 | 依赖 10F-R 通过 |

### 4.2 P0 后端稳定性修复

以下缺陷会阻塞预约模块和候选发布，但它们属于后端范围，与当前 `10F-R2B` 阶段约束冲突。

GLM-5.1 不得自行开始。用户必须明确批准将这些任务作为独立稳定性批次插入路线图后才能执行：

1. `RM-B01`：修复真实 MySQL 预约锁 SQL 和锁 ID。
2. `RM-B02`：修复预约状态流转事务、并发和错误状态日志。
3. `RM-B03`：修复预约改派事务竞态。
4. `RM-Q01`：建立真实 MySQL 并发验证门禁。

### 4.3 后续质量和能力任务

在当前阶段门禁通过后，按依赖执行：

1. `RM-F01`：管理后台错误状态和请求层测试。
2. `RM-C01`：社区评论计数并发一致性。
3. `RM-AI01`：AI 数据接地安全加固。
4. `RM-S01`：认证配置和登录防护设计。
5. `RM-Q02`：覆盖率与自动化质量门禁。
6. 按第 8 节补齐缺失能力。

## 5. P0 后端稳定性任务包

## 5.1 RM-B01：真实 MySQL 预约锁 SQL 与锁 ID 修复

优先级：P0

问题证据：

- `src/main/resources/mapper/StaffBookingLockMapper.xml` 使用 `MERGE INTO`，MySQL 8 不支持该语法。
- XML 注释错误声称 `MERGE INTO` 同时支持 H2 和 MySQL 8。
- `BookingTransactionServiceImpl.generateLockId` 使用 `staffId * 100000 + dayOfYear`。
- 该算法忽略年份，同一员工跨年相同 day-of-year 会生成相同 ID。
- 雪花 ID 参与乘法存在 `long` 溢出和绝对值碰撞风险。
- 当前 `BookingConcurrencyIT` 创建两个员工，并允许成功数为 1 到 2，不能证明同一员工重叠预约只允许一个成功。

允许修改：

- `src/main/resources/mapper/StaffBookingLockMapper.xml`
- `src/main/java/com/petcare/booking/mapper/StaffBookingLockMapper.java`
- `src/main/java/com/petcare/booking/service/impl/BookingTransactionServiceImpl.java`
- 预约锁和预约并发直接相关测试
- 必要时新增独立 MySQL 集成测试类

禁止修改：

- `schema.sql`，除非先证明当前唯一约束无法支持既定 D-004，并获得用户批准
- 前端代码
- D-011、D-012 相关代码
- 预约业务状态和 API 契约

必须先写的 RED 测试：

| 测试名建议 | 测试层级 | 必须断言 |
| --- | --- | --- |
| `upsertStaffBookingLock_executesOnMysql8` | 真实 MySQL 集成 | 第一次插入成功，第二次相同员工日期 upsert 成功，最终只有一条锁记录 |
| `upsertStaffBookingLock_preservesExistingPrimaryKey` | 真实 MySQL 集成 | 重复 upsert 不替换已有锁记录主键 |
| `lockRowsForSameStaffAcrossYears_haveDifferentIds` | 集成 | 同一员工不同年份相同 day-of-year 可同时存在，不发生主键冲突 |
| `largeSnowflakeStaffId_doesNotOverflowLockId` | 集成 | 大型雪花员工 ID 不产生负数、碰撞或异常 |
| `sameStaffSameOverlappingTime_onlyOneBookingSucceeds` | 真实 MySQL 并发 | 精确断言 1 成功、1 个 `BOOKING_TIME_CONFLICT`、数据库只有 1 条有效预约 |
| `sameStaffAdjacentTime_bothBookingsSucceed` | 真实 MySQL 并发 | 相邻时间段精确断言 2 个成功 |

测试实现约束：

- 新增真正使用 `@ActiveProfiles("mysql-test")` 的 MySQL 并发测试类，不得继续依赖固定为 `test` Profile 的 `BookingConcurrencyIT`。
- 并发测试使用 `CountDownLatch` 或 `CyclicBarrier` 同时放行，不得用 `Thread.sleep` 制造时序。
- 同一员工冲突测试只能准备一个具备技能且有排班的员工，不能让自动分配选择第二名员工。
- 失败请求必须断言明确业务错误码，不能只断言抛出任意异常。
- 测试结束后清理自己创建的数据，不能依赖其他测试执行顺序。

实现约束：

- MySQL 路径必须使用 MySQL 8 可执行的 upsert 语句，例如基于唯一键的 `INSERT ... ON DUPLICATE KEY UPDATE`。
- 新锁记录 ID 必须使用项目既定雪花 ID 生成机制，不得通过员工 ID 和日期算术拼接。
- 重复 upsert 必须保留已有锁记录主键。
- 必须保留并验证 `(staff_id, booking_date)` 唯一约束和后续 `SELECT ... FOR UPDATE`。
- 如果 H2 和 MySQL 需要不同 SQL，必须显式建立可验证的数据库方言路径，不得再次使用未经验证的“兼容语法”。

RED 命令建议：

```powershell
$env:DB_URL = 'jdbc:mysql://127.0.0.1:3306/petcare_o2o_test?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
$env:DB_USERNAME = '<mysql-test-user>'
$env:DB_PASSWORD = '<mysql-test-password>'
mvn "-Dtest=MySqlMapperIntegrationIT,BookingConcurrencyMySqlIT" test
```

GREEN 和回归命令：

```powershell
mvn "-Dtest=BookingApplicationServiceTest,BookingConcurrencyIT" test
mvn "-Dtest=MySqlMapperIntegrationIT,BookingConcurrencyMySqlIT" test
mvn test
mvn package "-DskipTests"
git diff --check
```

完成标准：

- 真实 MySQL 8 不再出现 `MERGE INTO` 语法错误。
- 同一员工重叠时间的两个并发请求精确只有一个成功。
- 跨年日期和大型雪花员工 ID 不发生锁主键碰撞。
- H2 默认测试和真实 MySQL 专项测试均通过。

建议提交：

```text
test(booking): add mysql booking lock regression tests
fix(booking): use mysql-safe staff date lock upsert
```

## 5.2 RM-B02：预约状态流转事务与日志一致性

优先级：P0

问题证据：

- `BookingApplicationServiceImpl` 中取消、确认、拒绝、开始、完成和管理员取消使用“读取状态 -> 校验 -> 更新 -> 写日志”。
- 上述流程没有统一事务和预约行锁，并发操作可能同时基于旧状态通过校验。
- 更新预约成功后，如果状态日志保存失败，预约状态不会自动回滚。
- 用户取消预约先设置 `CANCELLED`，随后把修改后的状态作为日志旧状态，可能写出 `CANCELLED -> CANCELLED`。

允许修改：

- `BookingApplicationServiceImpl`
- `BookingTransactionService` 及其实现
- `ServiceBookingMapper` 和对应 XML
- 预约状态流转直接相关测试

禁止修改：

- `BookingStateMachine` 中已决定的合法状态规则，除非测试证明其与需求文档冲突并获得用户批准
- Controller API 路径和 DTO
- 前端代码
- 无关预约创建和可用时间计算逻辑

必须先写的 RED 测试：

| 测试名建议 | 测试层级 | 必须断言 |
| --- | --- | --- |
| `userCancel_writesActualOldStatus` | 集成 | 日志为 `PENDING_CONFIRM -> CANCELLED`，不能是 `CANCELLED -> CANCELLED` |
| `statusLogFailure_rollsBackBookingStatus` | 事务集成 | 日志持久化失败后，预约状态、取消时间和取消原因保持原值 |
| `concurrentConfirmAndReject_onlyOneTransitionWins` | 真实 MySQL 并发 | 只有一个操作成功，最终状态与唯一成功状态日志一致 |
| `concurrentStartAndCancel_neverProducesInvalidFinalState` | 真实 MySQL 并发 | 最终状态符合状态机，状态日志顺序与最终状态一致 |
| `repeatedSameTransition_isRejectedWithoutExtraLog` | 集成 | 第二次相同状态操作失败，日志数量不增加 |

实现约束：

- 状态校验、状态更新和状态日志写入必须位于同一个事务中。
- 必须在事务内锁定预约记录，或使用带旧状态条件的原子更新并验证影响行数。
- 状态校验必须在获得并发控制后执行，不能只在事务外预检查。
- 状态日志必须保存真实旧状态和新状态。
- 日志保存失败必须使预约更新回滚。
- Application Service 不应继续直接拼装重复的“更新 + 日志”流程；应复用清晰的事务服务边界。
- 返回响应前必须读取或使用事务内的最终真实状态。

验证命令：

```powershell
mvn "-Dtest=BookingApplicationServiceTest,BookingStatusTransitionTransactionTest" test
mvn "-Dtest=BookingStatusTransitionMySqlIT" test
mvn test
mvn package "-DskipTests"
git diff --check
```

完成标准：

- 所有状态流转满足事务原子性。
- 并发冲突不会产生两个互相矛盾的成功响应。
- 状态日志与最终状态一致。
- 用户取消日志记录真实旧状态。

建议提交：

```text
test(booking): add transactional status transition regressions
fix(booking): make status transitions atomic
```

## 5.3 RM-B03：预约改派并发一致性

优先级：P0

问题证据：

- `BookingApplicationServiceImpl.reassignBooking` 在事务外读取并校验预约状态。
- `BookingTransactionServiceImpl.reassignBookingOnce` 只锁定新员工日期，没有先锁定并重新读取预约。
- 并发取消、完成或改派可能使改派逻辑使用过期的预约状态、员工和时间数据。

允许修改：

- `BookingApplicationServiceImpl` 的改派入口
- `BookingTransactionService` 及其实现
- `ServiceBookingMapper` 和对应 XML
- 改派直接相关测试

禁止修改：

- 改派 API 契约
- 员工技能、排班和预约状态业务规则
- 前端代码

必须先写的 RED 测试：

| 测试名建议 | 测试层级 | 必须断言 |
| --- | --- | --- |
| `reassignAfterTerminalTransition_isRejected` | 真实 MySQL 并发 | 如果取消或完成先获得锁，后续改派失败且员工不变 |
| `reassignUsesLockedBookingSnapshot` | 集成 | 冲突检查使用事务内重新读取的日期、时间和员工，不使用入口传入的过期快照 |
| `reassignToBusyStaff_isRejectedWithoutMutation` | 集成 | 目标员工冲突时，预约员工和日志均不改变 |
| `concurrentReassigns_keepConsistentFinalStaffAndLogs` | 真实 MySQL 并发 | 最终员工无时间冲突，成功改派日志反映真实前后员工 |

实现约束：

- 改派事务必须先读取并锁定预约记录，再校验是否允许改派。
- 日期、开始时间、结束时间和旧员工必须来自锁定后的预约记录，不能由事务外参数决定。
- 目标员工技能、状态和时间冲突必须在最终事务路径中重新验证。
- 所有锁必须采用固定顺序，避免两个改派请求互相死锁。
- 冲突或非法状态失败时，预约和日志均不能发生部分更新。

验证命令：

```powershell
mvn "-Dtest=BookingReassignTransactionTest" test
mvn "-Dtest=BookingReassignMySqlIT" test
mvn test
mvn package "-DskipTests"
git diff --check
```

完成标准：

- 改派不再使用事务外过期快照。
- 改派与状态流转并发时不会把终态预约重新分配。
- 失败改派不产生部分更新或虚假日志。

建议提交：

```text
test(booking): add reassign concurrency regressions
fix(booking): lock booking during staff reassignment
```

## 5.4 RM-Q01：真实 MySQL 集成测试门禁

优先级：P0

问题证据：

- 默认 Surefire 测试不会运行 `*IT`。
- `BookingConcurrencyIT` 允许 1 到 2 个成功，断言不能证明防冲突规则。
- 真实 MySQL 测试依赖手工环境，当前没有可复现门禁。

允许修改：

- `pom.xml`
- MySQL 集成测试配置
- 测试说明文档或现有验证文档中与该门禁直接相关的部分
- `*IT` 测试类

禁止修改：

- 业务实现代码
- 生产数据库凭据
- 通过让默认开发环境强依赖本地 MySQL 来获得门禁

实施要求：

- 明确区分默认快速测试和真实 MySQL 集成测试。
- 提供显式 Maven Profile 或等效命令运行 `*IT`。
- CI 或本地门禁必须通过环境变量读取测试数据库连接信息。
- 不得提交数据库密码。
- MySQL 测试必须使用独立测试数据库，并提供可重复初始化和清理路径。
- 并发测试必须有超时，失败时输出成功数、失败错误码和最终数据库记录数。

必须验证：

```powershell
mvn test
mvn "-Dtest=MySqlMapperIntegrationIT,BookingConcurrencyMySqlIT,ProductInventoryConcurrencyIT" test
mvn package "-DskipTests"
git diff --check
```

完成标准：

- 默认测试仍可在没有 MySQL 的环境运行。
- 发布门禁能够明确运行所有真实 MySQL 集成测试。
- 预约冲突和库存竞争在 MySQL 8 中均精确只有一个成功。

## 6. P1 正确性、安全与前端质量任务包

## 6.1 RM-C01：社区评论计数并发一致性

优先级：P1

问题证据：

- 创建已发布评论、审核通过评论、隐藏评论和删除评论使用“读取 `commentCount` -> Java 加减 -> `updateById`”。
- 并发写入时可能覆盖其他事务的计数结果，导致帖子 `comment_count` 漂移。

必须先写的 RED 测试：

| 测试名建议 | 必须断言 |
| --- | --- |
| `concurrentPublishedComments_incrementCountExactly` | N 个并发已发布评论后，帖子计数精确增加 N |
| `concurrentHideAndDelete_neverDecrementsTwice` | 同一评论并发隐藏和删除后，计数最多减少一次且不小于 0 |
| `rejectedComment_neverChangesPublishedCount` | 拒绝评论不改变公开评论计数 |
| `moderationFailure_rollsBackCommentAndCount` | 审核状态更新失败时，评论和帖子计数同时回滚 |

实现约束：

- 计数变更必须使用数据库原子增减或受行锁保护的事务。
- 减少计数时必须保证不低于 0。
- 评论状态变更和帖子计数变更必须位于同一事务。
- 不允许通过定时全量重算掩盖实时一致性缺陷。

验证命令：

```powershell
mvn "-Dtest=CommunityPostApplicationServiceTest,CommunityAdminServiceTest,CommunityCommentConcurrencyMySqlIT" test
mvn test
mvn package "-DskipTests"
git diff --check
```

## 6.2 RM-AI01：AI 客服数据接地和用量日志安全

优先级：P1

执行约束：

- 真实 DeepSeek Provider 仍受 D-008 阻塞。
- 本任务只能加固已有领域安全策略、Mock Provider 测试和日志一致性。
- 不允许实现真实 Provider、重试、流式输出或限流。

问题证据：

- 当前接地策略主要判断“无服务数据但回答中出现价格”，无法阻止在已有服务上下文中编造另一项价格。
- 库存、营业时间、服务半径和取消规则等事实未与可信上下文逐项比较。
- AI 用量日志保存异常被捕获后仅记录警告，可能出现 Provider 已调用但无审计记录。

必须先写的 RED 测试：

| 测试名建议 | 必须断言 |
| --- | --- |
| `fabricatedPriceIsRejectedEvenWhenContextHasOtherServices` | 上下文存在服务时，编造另一价格仍被拒绝 |
| `fabricatedStockIsRejected` | 编造库存被拒绝 |
| `fabricatedBusinessHoursIsRejected` | 编造营业时间被拒绝 |
| `fabricatedServiceRadiusIsRejected` | 编造上门半径被拒绝 |
| `fabricatedCancellationRuleIsRejected` | 编造取消规则被拒绝 |
| `groundedFactsAreAllowed` | 与可信上下文一致的事实回答通过 |
| `usageLogUnavailable_preventsUnauditedProviderAttempt` | 无法建立审计记录时，不产生不可追踪 Provider 调用 |

实现约束：

- AI Provider 不能访问 Mapper 或数据库。
- 可信事实必须由应用层构造并传入领域策略或 Provider 上下文。
- 对价格、库存、时间、半径和规则类问题采用 fail-closed。
- Provider 原始错误、密钥和完整 Prompt 不能出现在客户端错误中。
- 每次实际 Provider 尝试必须有可追踪用量记录；如果需要改变日志生命周期，先写清事务和失败补偿设计。

验证命令：

```powershell
mvn "-Dtest=CustomerServiceGroundingPolicyTest,AiConversationApplicationServiceTest,AiProviderArchitectureTest" test
mvn test
mvn package "-DskipTests"
git diff --check
```

## 6.3 RM-S01：认证配置与登录防护

优先级：P1

问题证据：

- `application.yml` 固定激活 `dev` Profile。
- JWT Secret 为空时缺少清晰、环境感知的启动失败策略。
- 管理员登录缺少失败次数限制、延迟、锁定或限流。
- D-009 已决定 V1 不使用 Redis，因此不得擅自引入 Redis 限流。

本任务必须先形成设计决定：

- 明确开发、测试和生产 Profile 的启动行为。
- 明确管理员登录防暴力破解策略及其状态存储方式。
- 如果方案需要新增表或改变 Schema，必须先更新相关规划并获得用户批准。

必须先写的测试：

| 测试名建议 | 必须断言 |
| --- | --- |
| `blankJwtSecret_failsStartupWithSafeMessage` | 非测试运行时空密钥明确失败，错误不泄露秘密 |
| `shortJwtSecret_isRejected` | 过短密钥被拒绝 |
| `validExternalJwtSecret_startsSuccessfully` | 环境变量提供合法密钥时可启动 |
| `repeatedFailedAdminLogin_isThrottledOrLocked` | 达到阈值后登录被保护 |
| `successfulLogin_resetsFailureStateAccordingToDesign` | 成功登录后的行为符合设计 |

禁止事项：

- 硬编码默认生产 JWT Secret。
- 在日志中输出密码、Token 或 Secret。
- 未经批准引入 Redis。
- 仅在前端限制登录次数。

## 6.4 RM-F01：管理后台错误状态、请求层与关键基础测试

优先级：P1

阶段归属：10G

问题证据：

- 多个页面使用空 `catch { /* handled */ }`，缺少页面级 Error 和 Retry 状态。
- `request.ts` 使用 `as never` 绕过 Axios 类型约束。
- Token 存储在 `localStorage`，需要在安全 Review 中明确接受范围或替代方案。
- 当前仅有契约测试，没有请求层、认证 Store、路由守卫、组件和 E2E 完整门禁。

执行前置：

- `10F-R` 必须完成。
- 必须先按 `docs/23-phase-10f-review-fix-and-10g-quality-plan.md` 建立 10G 命令和测试基础。
- 不得覆盖当前工作区其他 Agent 已修改的 `frontend/admin-web/package.json`。

必须先写的测试：

| 测试文件建议 | 必须覆盖 |
| --- | --- |
| `src/__tests__/request.test.ts` | 成功 envelope、网络失败、401、403、409、422、500、错误脱敏 |
| `src/__tests__/user-store.test.ts` | 登录、会话恢复、登出、无效 Token 清理 |
| `src/__tests__/router-guard.test.ts` | 未登录跳转、无权限阻止、合法访问 |
| 页面组件测试 | Loading、Empty、Error、Forbidden、Success、Retry |
| Playwright 登录 E2E | 登录、刷新恢复、登出、401 失效 |

实现约束：

- 页面不得静默吞掉失败。
- 全局 Toast 不能替代页面级 Error 和 Retry 状态。
- 请求类型必须与真实 `ApiResponse<T>` 契约一致，不得使用 `as never` 或双重断言隐藏错误。
- Token 存储方案如果改变，必须先进行认证影响 Review；不得只改存储位置导致刷新后会话失效。
- 用户和 AI 内容禁止使用未经净化的 `v-html`。

验证命令目标：

```powershell
cd frontend/admin-web
npm run lint
npm run typecheck
npm run test
npm run test:coverage
npm run build
npm run e2e
```

完成标准：

- 上述脚本存在并通过，或 E2E 外部阻塞有明确证据。
- 请求层、认证 Store、路由守卫和权限判断覆盖率不低于 80%。
- 核心页面均有失败状态和重试入口。

## 6.5 RM-Q02：覆盖率、构建体积和质量门禁

优先级：P1

问题证据：

- JaCoCo 当前仅生成报告，没有配置最低覆盖率检查。
- 后端当前覆盖率低于项目 80% 目标。
- 前端主 JS 包超过 Vite 默认 500 KB 警告阈值。
- 前端缺少完整 lint、typecheck、coverage 和 E2E 门禁。

执行顺序：

1. 先为关键业务补有意义测试，使覆盖率达到目标。
2. 再启用覆盖率失败门禁。
3. 最后进行路由级拆包和构建体积优化。

禁止事项：

- 排除关键业务包来虚增覆盖率。
- 删除分支测试或降低阈值。
- 只测试 Getter、Setter 或无业务价值代码来凑覆盖率。
- 为减小包体积删除真实功能。

最低直接测试范围：

- 预约排班、距离校验、状态流转和并发。
- 内容风控和评论计数。
- 订单金额、库存和状态流转。
- AI 安全边界。
- 后台授权。
- 前端请求层、认证、权限和关键页面状态。

完成标准：

- 后端业务逻辑行覆盖率不低于 80%，关键规则有直接测试。
- 前端关键基础逻辑覆盖率不低于 80%。
- 构建体积警告有明确预算或完成合理拆包。
- 完整门禁可重复运行。

## 7. D-011 与 D-012 决策门禁

## 7.1 D-011：雪花 ID JSON 传输

状态：未决，禁止实现。

推荐方案仍为：

- 后端对外 ID 统一序列化为 JSON 字符串。
- 前端所有实体 ID 统一使用 `string`。

用户决定后，必须拆成三个任务包：

1. 后端 ID 序列化契约和回归测试。
2. 前端 ID 类型迁移和契约测试。
3. 前后端真实雪花 ID 联调测试。

必须测试：

- 使用大于 `Number.MAX_SAFE_INTEGER` 的真实 ID。
- 列表、详情、更新、删除和动作接口传回的 ID 完全一致。
- 分页、嵌套 DTO 和日志 DTO 中的 ID 不遗漏。
- 前端不得对 ID 执行数值运算或 `Number(id)`。

禁止事项：

- 只修改 TypeScript 类型后声称完成。
- 在后端仍发送 JSON Number 时由前端再转字符串。
- 使用小 ID 测试掩盖精度问题。

## 7.2 D-012：当前门店上下文

状态：未决，禁止实现。

推荐方案仍为：

- 新增受权限保护的“当前门店”后台接口。

用户决定后，必须拆成三个任务包：

1. 后端当前门店契约、权限和测试。
2. 前端门店上下文 Store 和页面迁移。
3. 门店信息、配置、员工和排班真实联调。

必须测试：

- 管理员只能获取被授权的当前门店。
- 无门店上下文时返回明确错误，前端显示阻塞状态。
- 门店 ID 使用真实雪花 ID，不允许固定为 `1`。
- 页面刷新和会话恢复后门店上下文仍正确。

禁止事项：

- 使用环境变量或固定 `STORE_ID = 1` 声称完成。
- 由前端请求参数决定管理员可访问门店。
- 绕过后端权限校验。

## 8. 缺失能力与路线图映射

以下内容属于缺失能力，不应与缺陷修复混在同一个任务包中。

| 缺失能力 | 建议阶段 | 执行约束 |
| --- | --- | --- |
| 前端 lint、typecheck、组件测试、E2E、覆盖率 | 10G | 先完成 10F-R |
| 评论审核页面、AI 分析和用量页面、真实仪表盘 | 10H | 只能接入真实已有 API |
| 管理员账号、角色和权限管理 API/页面 | 用户批准后的独立后台任务 | 必须遵守细粒度 RBAC 和操作日志 |
| 服务分类、商品分类、员工不可用时间 API | 11-07 | 后端先完成契约与测试，前端后接入 |
| 用户资料、宠物档案、地址管理 API | 11 | 必须有资源归属测试 |
| 合法用户端 E2E 认证方案 | 11-01 | 未决定前禁止模拟生产登录 |
| OpenAPI 或真实接口清单 | 11-08 | 必须由真实 Controller/DTO 生成或核对 |
| 可复现演示种子数据 | 11-06 | 不得包含生产秘密 |
| 小程序工程与公开浏览 | 12 | 依赖前置后端和匿名访问策略 |
| 小程序预约、订单、社区和 AI 流程 | 13 | 依赖合法用户认证 |
| 文件上传 | 14A | 依赖 D-007 补全 |
| 真实 DeepSeek Provider | 14B | 依赖 D-008 补全 |
| 真实 MySQL 全链路联调 | 15 | P0 预约和库存并发门禁必须通过 |
| Docker Compose、环境示例和发布回滚 | 16 | 候选发布前完成 |
| 营销活动展示和管理能力 | 用户确认范围后的独立任务 | 先核对 V1 原始需求和已有 API |

缺失能力实施通用测试要求：

- 新 API：Controller 权限测试、Service 业务测试、Mapper/数据库集成测试。
- 用户资源：必须测试跨用户访问被拒绝。
- 管理后台页面：必须测试 Loading、Empty、Error、Forbidden、Success。
- 关键写操作：必须测试重复提交、冲突、失败回滚和权限拒绝。
- 用户核心流程：必须有合法身份的 E2E，不得使用伪造生产 Token。

## 9. GLM-5.1 每个任务包的 Review 清单

代码完成后必须逐项检查：

### 正确性

- 测试是否真实复现原缺陷，而不是只覆盖新实现。
- 是否保留原业务规则和错误码。
- 是否存在边界值、空值、跨年、大 ID 和重复请求问题。

### 事务与并发

- 状态校验是否在锁或原子更新之后执行。
- 多表修改是否在同一事务。
- 失败是否完整回滚。
- 锁顺序是否固定。
- 并发测试是否真正同时运行，并且精确断言结果。

### 安全

- 是否校验输入、权限和资源归属。
- 是否泄露 SQL、堆栈、Token、密钥或 Provider 原始错误。
- 是否引入硬编码秘密、固定 ID 或测试后门。
- 是否新增未经批准的外部依赖或网络调用。

### 测试有效性

- RED 是否因目标缺陷失败。
- GREEN 是否运行了同一测试。
- 是否出现宽泛断言、随机成功、睡眠等待或测试顺序依赖。
- 真实 MySQL 问题是否在真实 MySQL 中验证。
- 覆盖率是否来自有意义的业务测试。

### 范围与 Git

- 是否只修改当前任务允许的文件。
- 是否保护其他 Agent 的未提交变更。
- 是否执行 `git diff --check`。
- 提交是否只包含当前任务。

## 10. 完整验证矩阵

### 后端默认门禁

```powershell
mvn test
mvn package "-DskipTests"
git diff --check
git status --short --branch
```

### 真实 MySQL 门禁

```powershell
$env:DB_URL = 'jdbc:mysql://127.0.0.1:3306/petcare_o2o_test?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
$env:DB_USERNAME = '<mysql-test-user>'
$env:DB_PASSWORD = '<mysql-test-password>'
mvn "-Dtest=MySqlMapperIntegrationIT,BookingConcurrencyMySqlIT,BookingStatusTransitionMySqlIT,BookingReassignMySqlIT,ProductInventoryConcurrencyIT,CommunityCommentConcurrencyMySqlIT" test
```

如果某个测试类尚未创建，GLM-5.1 必须在对应任务包中先创建并完成 RED，不能从命令中删除该测试来宣称门禁通过。

### 管理后台门禁

```powershell
cd frontend/admin-web
npm run lint
npm run typecheck
npm run test
npm run test:coverage
npm run build
npm run e2e
```

在 10G 尚未建立这些脚本前，必须明确标记为“尚未具备，阶段门禁未通过”，不能只运行 `test:contract` 后声称质量基建完成。

### 安全与依赖门禁

```powershell
cd frontend/admin-web
npm audit
```

如果后续配置 Maven 依赖安全扫描，再运行：

```powershell
mvn org.owasp:dependency-check-maven:check
```

## 11. 给 GLM-5.1 的开工指令模板

用户后续指定任务包时，将以下内容作为 GLM-5.1 的执行入口：

```text
你现在只执行任务包 <TASK_ID>。

开始前必须按顺序阅读 AGENTS.md、README.md、项目强制文档、当前阶段计划，以及
docs/27-glm5-risk-remediation-and-test-plan.md 中该任务包的全部内容。

先运行：
git status --short --branch
git log --oneline -5
git diff --stat

先输出开工汇报，不允许立即修改文件。

严格执行 TDD：
1. 先新增能稳定复现目标缺陷的测试。
2. 运行并保存 RED 证据。
3. 不得修改或削弱测试断言。
4. 实现最小修复。
5. 重跑同一测试形成 GREEN。
6. 运行任务包规定的模块测试、完整测试、构建和 git diff --check。
7. Review 正确性、安全、事务、并发和测试有效性。
8. 只提交当前任务包文件，不得包含其他 Agent 的变更。

遇到 D-011、D-012、其他未决事项、阶段冲突或无法确认的需求冲突时立即停止相关实现，
列出方案和证据，等待用户决定。
```

## 12. GLM-5.1 强制交接格式

```text
任务：
任务包 ID：
阶段：
分支：
提交：
已完成：
未完成：
变更文件：
未触碰的已有变更：
接口契约变化：
RED 测试命令与失败原因：
GREEN 测试命令与结果：
完整验证命令：
完整验证结果：
覆盖率：
真实 MySQL 验证：
Review 结果：
已知风险：
待决策事项：
下一步允许执行的任务包：
```

没有真实验证证据时，只能写“已实现但未验证”或“部分完成”，禁止写“已完成”。
