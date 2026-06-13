# PetCare O2O 项目规划

本仓库用于规划和逐步实现“AI 增强型宠物门店 O2O 服务预约与客户运营平台”。

当前状态：阶段 `11-04R` 已通过 Review；阶段 `11-05` 首轮编码 Review 未通过。下一任务为 `11-05R` Authorization 失败关闭修复；当前只能在 `phase-11-user-prerequisites` 分支执行，禁止提前开始 `11-06`。

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
- [docs/29-frontend-figma-design-and-glm-long-running-plan.md](docs/29-frontend-figma-design-and-glm-long-running-plan.md)
- [docs/30-user-miniapp-frontend-design-spec.md](docs/30-user-miniapp-frontend-design-spec.md)
- [docs/31-phase-11-user-prerequisites-plan.md](docs/31-phase-11-user-prerequisites-plan.md)
- [docs/33-phase-11-01-glm5-test-login-implementation-brief.md](docs/33-phase-11-01-glm5-test-login-implementation-brief.md)
- [docs/34-phase-11-01-review-and-remediation-plan.md](docs/34-phase-11-01-review-and-remediation-plan.md)
- [docs/35-phase-11-02-glm5-user-profile-api-brief.md](docs/35-phase-11-02-glm5-user-profile-api-brief.md)
- [docs/36-phase-11-02-review-and-remediation-plan.md](docs/36-phase-11-02-review-and-remediation-plan.md)
- [docs/37-phase-11-03-glm5-pet-profile-api-brief.md](docs/37-phase-11-03-glm5-pet-profile-api-brief.md)
- [docs/38-phase-11-03-review-and-remediation-plan.md](docs/38-phase-11-03-review-and-remediation-plan.md)
- [docs/39-phase-11-04-glm5-address-api-brief.md](docs/39-phase-11-04-glm5-address-api-brief.md)
- [docs/40-phase-11-04-review-and-remediation-plan.md](docs/40-phase-11-04-review-and-remediation-plan.md)
- [docs/41-phase-11-05-glm5-public-read-access-brief.md](docs/41-phase-11-05-glm5-public-read-access-brief.md)
- [docs/42-phase-11-05-review-and-remediation-plan.md](docs/42-phase-11-05-review-and-remediation-plan.md)
- [docs/43-phase-11-06-glm5-demo-seed-data-brief.md](docs/43-phase-11-06-glm5-demo-seed-data-brief.md)

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

当前最近一步为阶段 11-05 Review 修复：

1. 11-04R 地址写入失败、身份错误语义、事务回滚和真实 MySQL 并发问题已通过 Review。
2. 11-05 已完成公开 GET 白名单、公开内容过滤和社区公开 DTO 隔离。
3. Review 发现非 Bearer Authorization 请求头会在公开 GET 降级为匿名，违反失败关闭规则。
4. GLM5.1 当前只能执行 [阶段 11-05 Review 结论与修复任务书](docs/42-phase-11-05-review-and-remediation-plan.md)。
5. [阶段 11-06 演示种子数据任务书](docs/43-phase-11-06-glm5-demo-seed-data-brief.md) 已准备，但必须等待 11-05R 全部门禁和 Review 通过。
6. 所有后续 Agent 必须遵守 [可持续编程与交接规则](docs/22-continuous-agent-development-rules.md)。
