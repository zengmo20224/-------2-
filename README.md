# PetCare O2O 项目规划

本仓库用于规划和逐步实现“AI 增强型宠物门店 O2O 服务预约与客户运营平台”。

当前状态：D-010 已决定先完成管理后台再开发小程序。阶段 `10F-R2A`、`10F-R2B` 已完成，当前处于 D-011 雪花 ID JSON 传输策略和 D-012 当前门店上下文决策门禁；不要在 `main` 分支直接加入业务代码，当前阶段代码只能在 `phase-10-frontend` 分支执行。

## AI Agent 强制入口

任何 AI Agent 接手本项目时，必须先完整阅读并遵守：

- [AGENTS.md](AGENTS.md)
- [docs/08-pending-decisions.md](docs/08-pending-decisions.md)

未完成强制阅读、Git 状态检查和开工前汇报前，不允许修改项目文件。

## 给 GLM5.1 的主要交接文档

- [docs/03-glm5-implementation-plan.md](docs/03-glm5-implementation-plan.md)
- [docs/11-phase-2-backend-skeleton-brief.md](docs/11-phase-2-backend-skeleton-brief.md)
- [docs/12-phase-3-entities-mappers-plan.md](docs/12-phase-3-entities-mappers-plan.md)
- [docs/13-phase-4-auth-authorization-plan.md](docs/13-phase-4-auth-authorization-plan.md)
- [docs/14-phase-5-booking-scheduling-plan.md](docs/14-phase-5-booking-scheduling-plan.md)
- [docs/15-phase-6-community-moderation-plan.md](docs/15-phase-6-community-moderation-plan.md)
- [docs/16-phase-7-product-orders-plan.md](docs/16-phase-7-product-orders-plan.md)
- [docs/17-phase-8-ai-provider-functions-plan.md](docs/17-phase-8-ai-provider-functions-plan.md)
- [docs/19-phase-9-admin-api-handoff.md](docs/19-phase-9-admin-api-handoff.md)
- [docs/20-phase-10-frontend-integration-design.md](docs/20-phase-10-frontend-integration-design.md)
- [docs/21-remaining-development-roadmap.md](docs/21-remaining-development-roadmap.md)
- [docs/22-continuous-agent-development-rules.md](docs/22-continuous-agent-development-rules.md)
- [docs/23-phase-10f-review-fix-and-10g-quality-plan.md](docs/23-phase-10f-review-fix-and-10g-quality-plan.md)
- [docs/24-phase-10f-r2-api-contract-cleanup-plan.md](docs/24-phase-10f-r2-api-contract-cleanup-plan.md)
- [docs/25-admin-web-api-contract.md](docs/25-admin-web-api-contract.md)
- [docs/26-phase-10f-r2b-frontend-contract-cleanup-plan.md](docs/26-phase-10f-r2b-frontend-contract-cleanup-plan.md)
- [docs/27-glm5-risk-remediation-and-test-plan.md](docs/27-glm5-risk-remediation-and-test-plan.md)
- [docs/28-phase-10f-r2c-r2e-cross-layer-contract-plan.md](docs/28-phase-10f-r2c-r2e-cross-layer-contract-plan.md)

## 原始需求基线

- [docs/requirements-source.md](docs/requirements-source.md)

## 核心规划文档

- [docs/00-project-boundary.md](docs/00-project-boundary.md)
- [docs/01-architecture-design.md](docs/01-architecture-design.md)
- [docs/02-task-breakdown.md](docs/02-task-breakdown.md)
- [docs/04-code-standards.md](docs/04-code-standards.md)
- [docs/05-testing-and-verification.md](docs/05-testing-and-verification.md)
- [docs/06-git-safety-workflow.md](docs/06-git-safety-workflow.md)
- [docs/07-integration-gates.md](docs/07-integration-gates.md)
- [docs/08-pending-decisions.md](docs/08-pending-decisions.md)
- [docs/09-booking-concurrency-control.md](docs/09-booking-concurrency-control.md)
- [docs/10-admin-permission-design.md](docs/10-admin-permission-design.md)

## 项目目标

构建一个简历展示价值较高，同时具备真实单体宠物店试运营潜力的平台：

- 用户端微信小程序
- 商家 PC 管理后台
- Spring Boot 3 REST API
- MySQL 8 主数据库
- V2 可选引入 Redis
- 统一 AI Provider 适配层，用于 AI 客服、AI 宠物陪伴、AI 发帖辅助和 AI 经营分析

## 当前最近一步

第十阶段当前执行“管理后台优先”：

1. `10F-R2A` 已完成，真实契约基线见 [管理后台 API 契约清单](docs/25-admin-web-api-contract.md)。
2. `10F-R2B` 已完成并通过 `98` 项契约测试和管理后台构建。
3. 按 [阶段 10F-R2C 至 R2E 跨层契约修复编码计划](docs/28-phase-10f-r2c-r2e-cross-layer-contract-plan.md) 等待并记录 D-011、D-012 决策。
4. 完成阶段 10F-R 验收后，建立管理后台测试、类型检查、覆盖率和 E2E 门禁。
5. 完成管理后台核心流程后，再补齐小程序依赖的后端能力。
6. 所有后续 Agent 必须遵守 [可持续编程与交接规则](docs/22-continuous-agent-development-rules.md)。
