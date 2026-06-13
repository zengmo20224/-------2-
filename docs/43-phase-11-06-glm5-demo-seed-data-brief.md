# 阶段 11-06：GLM5.1 可复现演示种子数据任务书

日期：2026-06-13

状态：详细计划已完成，当前锁定

解锁条件：`docs/42-phase-11-05-review-and-remediation-plan.md` 全部门禁通过并完成 Review

执行对象：GLM5.1

目标分支：`phase-11-user-prerequisites`

## 1. 任务目的

为小程序公开浏览和合法测试用户流程提供一组可重复创建、重复清理、内容确定且不污染默认环境的演示数据。

本任务只建立演示数据装载能力，不修改生产业务逻辑，不把演示账号或数据写入 `schema.sql`，不自动在默认 `test`、`dev` 或 `prod` 环境加载。

## 2. 前置门禁

- 11-05R 已通过 Review。
- `mvn test`、构建和 `mvn -Ptc-mysql clean test` 通过。
- 当前分支为 `phase-11-user-prerequisites`。
- 工作区除 `.claude/` 外无不明修改。

任一条件不满足时停止。

## 3. 已决定的实现方案

采用显式启用的独立 `demo-seed` Profile，并与 `test` Profile 组合使用：

```text
test,demo-seed
```

默认 `test` Profile 不得自动加载演示数据，避免污染现有测试和掩盖测试隔离问题。`dev`、`prod` 或未指定 Profile 时也不得加载。

种子数据使用固定保留 ID、先按固定 ID 清理再插入的确定性方案。禁止使用依赖数据库方言的 upsert 作为唯一幂等机制。

推荐文件：

```text
src/main/resources/demo/seed-data.sql
src/main/resources/demo/cleanup-data.sql
src/main/resources/application-demo-seed.yml
```

如果同一 SQL 无法同时兼容 H2 和 MySQL，允许拆分为明确对应数据库的脚本，但两套脚本必须表达同一数据集并由同一集成测试矩阵验证。

## 4. 最小演示数据集

必须包含：

1. 一个 ACTIVE 测试用户：
   - 使用 `application-test.yml` 已允许的合成手机号，例如 `13800138001`。
   - 使用假的 openid/unionid，不使用真实个人数据。
   - 不需要用户密码，不新增管理员账号。
2. 一个属于该用户的宠物。
3. 一个属于该用户的默认地址，包含可用于上门距离流程的有效坐标。
4. 至少一个 ACTIVE 服务分类和一个 ON_SALE 服务项目。
5. 至少一个 ACTIVE 商品分类和一个 ON_SALE 商品。
6. 至少一个 ACTIVE 话题。

可选数据：

- 一个 PUBLISHED 帖子和一个 PUBLISHED 评论，仅在公开社区演示确有需要时加入。
- 可选社区数据仍必须符合 11-05 公开 DTO 隐私边界。

所有数据使用项目现有真实状态值和字段，不擅自新增 Schema 字段。

## 5. 固定 ID 与清理规则

- 为演示数据划定独立固定 ID 区间，例如 `9100` 至 `9199`。
- 清理脚本只能删除该固定 ID 集合或带有明确演示标识的数据。
- 必须按外键/逻辑依赖安全顺序清理，再按父到子顺序插入。
- 禁止使用无范围 `DELETE FROM <table>`、`TRUNCATE` 或按普通业务状态批量删除。
- 执行两次种子脚本后，数据数量和内容必须与执行一次相同。
- 执行两次清理脚本后，第二次仍成功且不删除其他数据。

## 6. 装载边界

允许使用一个最小初始化组件执行脚本，但必须同时满足：

- 使用 `@Profile("demo-seed")` 或等效显式 Profile 门控。
- 额外校验当前同时启用了 `test` Profile；只有 `demo-seed` 而没有 `test` 时必须失败关闭或不装载。
- 使用 Spring 结构化 SQL 执行能力，不手写字符串拆分 SQL。
- 失败时启动失败并给出不含秘密的明确日志，不允许静默部分装载。
- 装载和清理方案有直接集成测试。

不得复用 `schema.sql` 自动初始化入口加载演示数据，也不得改变正常生产启动行为。

## 7. 允许修改范围

```text
src/main/resources/demo/
src/main/resources/application-demo-seed.yml
src/main/java/com/petcare/ 下仅与 demo-seed 初始化直接相关的最小组件
src/test/java/com/petcare/ 下种子数据与 Profile 隔离测试
src/test/resources/ 下种子数据测试直接需要的配置
README.md 或现有运行文档中的演示数据运行说明
```

如果需要修改 `pom.xml` 以建立专项测试 Profile，必须先证明现有 Maven 配置无法表达门禁，并做最小修改。

## 8. 禁止修改

- `.claude/`、`schema.sql`、实体字段和业务状态。
- 默认 `application.yml`、生产凭据和真实个人信息。
- 用户认证规则、测试登录允许列表和 JWT 签发规则。
- 用户、宠物、地址、目录、社区等生产业务逻辑。
- 前端、文件上传、AI Provider 和 Redis。
- 创建管理员种子账号、硬编码密码、Token、API Key 或数据库密码。
- 通过关闭测试隔离或默认加载种子数据来简化实现。

## 9. 强制 TDD 顺序

### RED-1：Profile 隔离

先证明：

1. 默认测试上下文不加载演示数据。
2. `dev`、`prod` 和未指定 Profile 不加载演示数据。
3. `test,demo-seed` 才加载演示数据。
4. 单独启用 `demo-seed` 不得在非测试环境装载。

### RED-2：可重复创建和清理

先写 `SeedDataIntegrationTest` 或等价测试：

1. 第一次装载后最小数据集完整且关系正确。
2. 第二次装载后数量不增加、内容确定。
3. 清理后固定演示数据全部消失。
4. 第二次清理仍成功。
5. 清理不会删除测试预先插入的非演示数据。

### RED-3：用户与公开读取流程

- 使用演示手机号调用现有 test-login 成功。
- 登录后可以读取该用户的宠物和地址。
- 无 Token 可以读取演示服务、商品和话题。
- 如果加入社区数据，无 Token 只能读取公开社区字段。

### GREEN 与重构

- 使用固定 ID 和清理后插入实现确定性。
- 脚本和最小初始化组件保持小范围、可审查。
- 运行命令和清理命令写入现有项目文档。

## 10. 运行与清理说明要求

文档必须提供 PowerShell 可执行示例，至少说明：

```powershell
# 启用测试身份和演示数据
mvn spring-boot:run "-Dspring-boot.run.profiles=test,demo-seed"

# 运行专项验证
mvn "-Dtest=SeedDataIntegrationTest" test
```

清理命令必须使用项目实现的受限清理入口或明确数据库连接目标的脚本方式。禁止提供可能误清生产数据库的宽泛命令。

## 11. 强制验证

```powershell
mvn "-Dtest=SeedDataIntegrationTest,TestLoginControllerTest,PublicAccessIntegrationTest,AddressControllerTest,PetControllerTest" test
mvn test
mvn clean package "-DskipTests"
mvn -Ptc-mysql clean test
git diff --check
git status --short --branch
```

额外人工检查：

```powershell
rg -n "password|secret|token|api.?key|truncate|delete from" src/main/resources/demo src/main/resources/application-demo-seed.yml
rg -n "demo-seed|@Profile" src/main/java src/main/resources src/test
```

对 `DELETE FROM` 命中逐项确认必须包含固定演示数据范围；不得因搜索命中自动认定失败。

## 12. 建议提交顺序

```text
test: define reproducible demo seed contract
feat: add opt-in demo seed data
docs: document demo seed lifecycle
```

禁止 `git add .`，不得暂存 `.claude/`。

## 13. 退出门禁

- 演示数据只在显式 `test,demo-seed` 环境加载。
- 最小数据集支持 test-login、宠物、地址和公开目录演示。
- 重复创建、重复清理和非演示数据保护测试通过。
- 没有真实秘密、密码、Token 或个人数据进入 Git。
- 默认测试、开发和生产环境不受影响。
- 定向、完整、构建、MySQL 和 `git diff --check` 全部通过。
- `.claude/` 未进入提交。

通过并完成 Review 后，才允许根据阶段 11 路线图确定 11-07 的详细编码任务。

## 14. 强制交接格式

```text
任务：11-06 可复现演示种子数据
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
Profile 隔离证据：
重复装载与清理证据：
非演示数据保护证据：
秘密扫描结果：
.claude/ 处理情况：
已知风险：
待决策事项：
下一步允许执行的任务：门禁与 Review 通过后由 Codex 编写并批准 11-07 详细任务书
```
