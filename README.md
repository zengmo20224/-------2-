# PetCare O2O 项目规划

本仓库用于规划和逐步实现“AI 增强型宠物门店 O2O 服务预约与客户运营平台”。

当前状态：阶段 1 数据库 Schema 已完成并通过用户 review，当前启动阶段 2 后端骨架。不要在 `main` 分支直接加入业务代码；阶段 2 应在 `phase-2-backend-skeleton` 分支执行。

## AI Agent 强制入口

任何 AI Agent 接手本项目时，必须先完整阅读并遵守：

- [AGENTS.md](AGENTS.md)
- [docs/08-pending-decisions.md](docs/08-pending-decisions.md)

未完成强制阅读、Git 状态检查和开工前汇报前，不允许修改项目文件。

## 给 GLM5.1 的主要交接文档

- [docs/03-glm5-implementation-plan.md](docs/03-glm5-implementation-plan.md)
- [docs/11-phase-2-backend-skeleton-brief.md](docs/11-phase-2-backend-skeleton-brief.md)

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

第二阶段只做 Spring Boot 后端骨架：

1. 创建 Maven Spring Boot 3 后端工程。
2. 配置 MyBatis-Plus、MySQL 数据源和环境变量。
3. 建立统一响应、统一异常处理和健康检查接口。
4. 引入 Spring Security + JWT 依赖并建立最小安全骨架，不实现完整登录。
5. 通过 `mvn test` 和 `mvn clean package` 验证。
