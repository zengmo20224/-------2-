# 阶段 11-04 Review 结论与修复任务书

日期：2026-06-13

状态：11-04R Review 已通过，任务关闭

执行对象：GLM5.1

目标分支：`phase-11-user-prerequisites`

基线提交：

```text
fd10668 test: expose pet profile write failure and null safety contracts
eaabd39 fix: reject failed pet profile writes and stabilize null safety
e695205 test: define current user address api contract
e825208 feat: add current user address create and list api
44423b5 test: define default address transaction contract
310379f feat: add current user address mutation api
```

## 1. Review 结论

11-03R 已正确检查宠物创建写入结果、稳定处理服务层空请求，并使用 `Locale.ROOT` 处理 URL 协议，可以关闭。

11-04 已实现地址 CRUD、归属隔离、逻辑删除、默认地址基础事务和用户行锁，接口与真实 Schema 一致。但写入失败检查、禁用用户错误语义、事务回滚测试和真实 MySQL 并发验证未达到 `docs/39-phase-11-04-glm5-address-api-brief.md` 的退出门禁。

历史结论：11-04 首轮 Review 未通过；上述 Findings 已由 11-04R 关闭。当前阶段状态以 `docs/42-phase-11-05-review-and-remediation-plan.md` 为准。

## 2. 已验证结果

```text
定向回归：151 项通过
完整 mvn test：通过
构建：mvn clean package "-DskipTests" 通过
现有真实 MySQL 门禁：16 项通过
```

注意：当前 `tc-mysql` 测试集中没有地址默认值并发测试，因此上述 16 项不能证明 11-04 并发门禁通过。

工作区仅存在未跟踪 `.claude/`，禁止修改、暂存或提交。

## 3. 11-03R Review

| Finding | 结论 | 证据 |
|---|---|---|
| 创建写入失败伪造成功 | 已关闭 | `save=false` 抛出运行时异常 |
| 服务层空请求、空名称、空类型 NPE | 已关闭 | 统一返回 `validation_error` |
| URL 大小写 Locale 不稳定 | 已关闭 | 使用 `Locale.ROOT` |
| Controller 级宠物 `internal_error` 专项测试 | LOW 测试缺口 | 全局异常契约已有测试，不阻塞 11-03R 关闭 |

LOW 项可在 11-04R 补充，但不得修改宠物业务行为。

## 4. 11-04 Review Findings

### HIGH-1：取消旧默认地址时忽略写入失败

证据：

- `AddressApplicationServiceImpl.unsetOtherDefaults(...)` 调用 `addressService.update(wrapper)` 后直接忽略返回值。
- 创建或更新新默认地址时，后续写入仍可能成功并返回响应。

影响：

- 如果旧默认地址取消操作返回 `false`，新地址仍可能被设为默认，最终产生多个默认地址。
- 与“所有写步骤返回 false 时必须回滚”和“始终只有一个默认地址”冲突。

必须修复：

- 对预期存在旧默认地址的切换操作检查更新结果。
- 返回 `false` 时抛出非业务运行时异常，触发事务回滚并返回 500 `internal_error`。
- 不得在失败后继续保存或更新目标默认地址。
- 增加 Mock 服务测试，证明后续写操作未执行。

### MEDIUM-1：禁用用户竞态返回 404，而不是 401

证据：

- `UserServiceImpl.lockActiveUser(...)` 在 ACTIVE 用户不存在时抛出 `RESOURCE_NOT_FOUND`。
- 地址写操作在 JWT 校验后再次锁定 ACTIVE 用户；用户在此期间被禁用时会返回 404。

影响：

- 与 11-04 “禁用或删除用户 Token 返回 401”契约冲突。
- 同一身份状态在资料 API 和地址 API 中错误语义不一致。

必须修复：

- `lockActiveUser(...)` 失败统一抛出 `UNAUTHORIZED`。
- 错误信息不得区分不存在、禁用或删除。
- 增加服务层和 Controller 回归测试。

### MEDIUM-2：缺少默认地址事务回滚测试

当前没有 `AddressApplicationServiceImplTest` 或等价 Mock 服务测试，无法证明：

- 取消旧默认地址失败时事务停止。
- 新地址保存失败时旧默认地址恢复。
- 更新目标地址失败时旧默认地址恢复。
- 删除默认地址后提升下一地址失败时删除回滚。

必须补充 Mock 交互测试和至少一个 Spring 事务集成回滚测试。

### MEDIUM-3：缺少真实 MySQL 默认地址并发测试

证据：

- `src/test/java` 中不存在 `AddressDefaultConcurrencyMySqlIT` 或等价测试。
- 当前 `mvn -Ptc-mysql clean test` 只运行现有 16 项数据库测试。

必须补充：

- 两个并发请求为同一用户设置不同默认地址。
- 两个并发请求同时创建该用户第一条地址。
- 并发结束后存在地址时默认地址数量严格等于 1。
- 测试使用真实 MySQL、独立事务和超时保护，不使用隐藏 sleep。

### LOW-1：可选区县非空字符串未 trim

`normalizeBlank` 只将空白转换为 `null`，但保留非空字符串前后空白。建议统一 trim 后保存。本项不单独阻塞。

## 5. 下一项可执行任务

任务编号：`11-04R`

目标：关闭默认地址写入失败、身份错误语义、事务回滚和真实并发验证问题。

## 6. 允许修改范围

```text
src/main/java/com/petcare/user/service/impl/AddressApplicationServiceImpl.java
src/main/java/com/petcare/user/service/impl/UserServiceImpl.java
src/test/java/com/petcare/user/controller/AddressControllerTest.java
src/test/java/com/petcare/user/service/AddressApplicationServiceImplTest.java
src/test/java/com/petcare/user/service/AddressTransactionRollbackTest.java
src/test/java/com/petcare/user/mapper/AddressDefaultConcurrencyMySqlIT.java
```

仅为关闭 11-03R LOW 测试缺口时允许修改：

```text
src/test/java/com/petcare/user/controller/PetControllerTest.java
```

## 7. 禁止修改

- `.claude/` 下任何文件。
- Schema、实体字段、现有 API 路径。
- 前端、预约距离算法、社区公开读取、商品、AI、文件上传和 Redis。
- 提前实施 11-05。
- 与 Findings 无关的重构。

## 8. 强制 TDD 顺序

### RED-1：写入失败与回滚

先写失败测试：

1. 取消旧默认地址返回 `false` 时，目标地址不保存或不更新。
2. 新默认地址保存失败时，事务回滚，旧默认仍存在。
3. 目标地址更新失败时，事务回滚，旧默认仍存在。
4. 删除默认地址后提升失败时，删除操作回滚。

### GREEN-1：检查所有必要写操作

- 检查取消默认、保存、更新、删除和提升默认地址的返回值。
- 技术写入失败抛出非业务运行时异常。
- 资源归属不存在继续返回安全 404。

### RED-2：禁用用户错误语义

- 锁定不存在、禁用或删除用户时统一 `unauthorized`。
- 用户在首次认证后被禁用，地址写操作返回 401 且数据库不变。

### RED-3：真实 MySQL 并发

- 在 `tc-mysql` Profile 中新增独立并发测试。
- 使用 `ExecutorService`、同步起跑屏障、独立事务和明确超时。
- 每轮结束查询默认地址数量和地址总数。

### GREEN-3 与重构

- 保持用户行锁方案。
- 不引入 Redis 或 JVM 本地锁。
- 可同步 trim 可选区县。

## 9. 强制验证

```powershell
mvn "-Dtest=AddressControllerTest,AddressApplicationServiceImplTest,AddressTransactionRollbackTest,PetControllerTest,PetApplicationServiceImplUnitTest,UserProfileControllerTest,SecurityAccessTest" test
mvn test
mvn clean package "-DskipTests"
mvn -Ptc-mysql clean test
git diff --check
git status --short --branch
```

不存在的测试类名在创建前不得从最终门禁中删除；必须按实际测试类名更新本任务书交接证据。

## 10. 退出门禁

- HIGH 和 MEDIUM Findings 全部关闭。
- 所有必要写步骤返回 `false` 时停止并回滚。
- 禁用或删除用户地址写操作返回 401。
- 真实 MySQL 并发测试证明同一用户最多且必须有一个默认地址。
- 定向、完整、构建、MySQL 和 `git diff --check` 全部通过。
- `.claude/` 未进入提交。

11-04R 已满足本节退出门禁并通过 Review；11-05 已执行。当前不得重复领取 11-04R。

## 11. 强制交接格式

```text
任务：11-04R 地址默认值与并发 Review 修复
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
事务回滚证据：
真实 MySQL 并发证据：
.claude/ 处理情况：
已知风险：
待决策事项：
下一步允许执行的任务：门禁通过后仅允许 11-05
```
