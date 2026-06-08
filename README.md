# PetCare O2O Project Planning

This repository is the planning and implementation workspace for the AI-enhanced pet store O2O booking and customer operations platform.

Current status: planning baseline only. Do not add application code until the Phase 1 database task is started on a dedicated Git branch.

Primary handoff document for GLM5.1:

- [docs/03-glm5-implementation-plan.md](docs/03-glm5-implementation-plan.md)

Original requirement baseline:

- [docs/requirements-source.md](docs/requirements-source.md)

Core planning documents:

- [docs/00-project-boundary.md](docs/00-project-boundary.md)
- [docs/01-architecture-design.md](docs/01-architecture-design.md)
- [docs/02-task-breakdown.md](docs/02-task-breakdown.md)
- [docs/04-code-standards.md](docs/04-code-standards.md)
- [docs/05-testing-and-verification.md](docs/05-testing-and-verification.md)
- [docs/06-git-safety-workflow.md](docs/06-git-safety-workflow.md)
- [docs/07-integration-gates.md](docs/07-integration-gates.md)

## Project Goal

Build a resume-grade but realistic single-store pet service platform:

- WeChat mini program for users
- PC admin system for the merchant
- Spring Boot 3 REST API
- MySQL 8 primary database
- Optional Redis in V2
- Unified AI provider adapter for customer service, pet companion, content generation, and business analysis

## Immediate Next Step

Phase 1 is database initialization only:

1. Create `schema.sql`.
2. Cover all tables from the pasted requirement without omission.
3. Add comments, defaults, indexes, unique constraints, and MySQL 8 compatible DDL.
4. Validate SQL in a local MySQL 8 container or instance.
5. Commit the validated result using the Git workflow in [docs/06-git-safety-workflow.md](docs/06-git-safety-workflow.md).
