# PetCare O2O AI Agent 强制规则

本文件是本项目所有 AI Agent 的最高优先级项目内规则。

任何 AI Agent 在分析、规划、生成 SQL、修改代码、运行命令或提交 Git 前，都必须先阅读并遵守本文件。

## 1. 开工前强制阅读

开始任何任务前，必须按顺序阅读：

1. `AGENTS.md`
2. `README.md`
3. `docs/requirements-source.md`
4. `docs/00-project-boundary.md`
5. `docs/01-architecture-design.md`
6. `docs/02-task-breakdown.md`
7. `docs/03-glm5-implementation-plan.md`
8. `docs/04-code-standards.md`
9. `docs/05-testing-and-verification.md`
10. `docs/06-git-safety-workflow.md`
11. `docs/07-integration-gates.md`
12. `docs/08-pending-decisions.md`
13. `docs/09-booking-concurrency-control.md`
14. `docs/10-admin-permission-design.md`
15. `docs/11-phase-2-backend-skeleton-brief.md`
16. `docs/12-phase-3-entities-mappers-plan.md`
17. `docs/13-phase-4-auth-authorization-plan.md`
18. `docs/14-phase-5-booking-scheduling-plan.md`
19. `docs/15-phase-6-community-moderation-plan.md`
20. `docs/16-phase-7-product-orders-plan.md`
21. `docs/17-phase-8-ai-provider-functions-plan.md`
22. `docs/19-phase-9-admin-api-handoff.md`
23. `docs/20-phase-10-frontend-integration-design.md`
24. `docs/21-remaining-development-roadmap.md`
25. `docs/22-continuous-agent-development-rules.md`
26. `docs/23-phase-10f-review-fix-and-10g-quality-plan.md`
27. `docs/24-phase-10f-r2-api-contract-cleanup-plan.md`
28. `docs/25-admin-web-api-contract.md`
29. `docs/26-phase-10f-r2b-frontend-contract-cleanup-plan.md`
30. `docs/27-glm5-risk-remediation-and-test-plan.md`
31. `docs/28-phase-10f-r2c-r2e-cross-layer-contract-plan.md`

如果任务只涉及某一模块，也不能跳过前五项和 `docs/08-pending-decisions.md`。如果任务涉及预约、排班、后台权限或后台接口，也必须阅读第 13、14 项。

## 2. 开工前强制检查

修改任何文件前必须运行：

```powershell
git status --short --branch
git log --oneline -5
```

然后先汇报：

```text
已阅读文档：
当前阶段：
当前分支：
计划修改范围：
不会修改的范围：
待决策或阻塞项：
验证计划：
```

没有完成以上汇报前，不允许修改项目文件。

## 3. 禁止自行决定的事项

`docs/08-pending-decisions.md` 中标记为“未决”的事项，任何 Agent 都不能自行选型或实现。

遇到未决事项时必须：

1. 停止相关实现。
2. 给出可选方案、优缺点和推荐方案。
3. 等待用户明确决定。
4. 用户决定后，先更新对应规划文档，再开始实现。

## 4. 项目边界

- V1 是面向单体宠物门店的模块化单体应用。
- 不允许擅自改成微服务。
- 不允许提前实现 V2、V3 功能。
- 不允许擅自加入微信支付、多门店、会员积分、优惠券、独立员工端。
- 不允许让 AI 直接访问数据库。
- 不允许让 AI 提供宠物疾病诊断、药物处方或治疗承诺。
- 不允许改变原始需求中的核心业务规则。

## 5. 阶段约束

- 当前阶段未通过门禁前，禁止开始依赖它的下一阶段。
- 阶段 1 只实现并验证 `schema.sql`，不写前端和完整业务代码。
- 任何阶段完成后，必须对照 `docs/07-integration-gates.md` 检查。
- 没有验证证据，不能声称“已完成”“已可用”或“已接入”。

## 6. Git 强制规则

- 禁止直接在 `main` 分支实现业务功能。
- 每个阶段使用 `docs/06-git-safety-workflow.md` 中规定的独立分支。
- 修改前检查工作区，不能覆盖或回退其他 Agent、用户已有的变更。
- 每个可验证节点都要提交 Git。
- 提交前必须运行 `git diff --check` 和对应验证命令。
- 禁止使用 `git reset --hard`、`git clean -fd`、`git checkout -- .`，除非用户明确授权。
- 禁止强制推送、重写历史或删除分支，除非用户明确授权。

## 7. 代码与测试强制规则

- 非平凡业务逻辑必须执行 TDD：RED、GREEN、重构。
- 业务逻辑目标覆盖率不低于 80%。
- 预约排班、距离校验、状态流转、内容风控、订单金额、AI 安全边界必须有直接测试。
- 所有外部输入必须校验。
- 密码、Token、API Key、数据库凭据不能硬编码或提交到 Git。
- Controller 不能直接调用 Mapper 完成业务流程。
- 多表状态变更必须使用事务。
- 错误响应不能泄露 SQL、堆栈、密钥或 Provider 原始错误。

## 8. 变更范围约束

- 只修改当前任务明确要求的文件和模块。
- 不做无关重构。
- 不擅自重命名业务状态、表、字段或 API。
- 如果必须调整原始设计，先说明原因、影响范围和迁移方案，等待用户批准。
- 如果发现需求冲突，停止冲突部分实现并报告，不能自行选择其中一方。

## 9. 完成任务前强制验证

任务结束前必须：

1. 运行当前阶段要求的构建、测试、数据库或 E2E 验证。
2. 运行 `git diff --check`。
3. 运行 `git status --short --branch`。
4. 对照集成门禁。
5. 明确列出未验证内容和已知风险。

## 10. 强制交接格式

每个 Agent 完成工作时必须使用以下格式：

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

## 11. 规则冲突处理

如果用户最新明确指令与本文档冲突，以用户最新明确指令为准，但 Agent 必须在执行前指出冲突和风险。

如果不同项目文档之间冲突，优先级如下：

```text
用户最新明确指令
> AGENTS.md
> docs/requirements-source.md
> docs/00-project-boundary.md
> docs/01-architecture-design.md
> docs/02-task-breakdown.md
> 当前阶段实施计划
> 其他项目文档
```

任何无法确定的冲突都必须停止相关实现并请求用户决策。

## 12. 可持续编程强制规则

- 后续阶段必须按 `docs/21-remaining-development-roadmap.md` 执行。
- 每个 Agent 一次只能领取一个可独立验收的任务包。
- 当前工作区存在其他 Agent 的未提交变更时，必须按 `docs/22-continuous-agent-development-rules.md` 保护和交接。
- 禁止跳过当前阶段门禁直接开始后续阶段。
- 没有测试、Review、验证和 Git 提交证据时，禁止声明任务包完成。
