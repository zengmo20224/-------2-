# 配置项物理审计（PCA）一致性核对表

> 关联文档：docs/03-configuration-management-plan.md §7.2、docs/配置项登记表.md

## 审计项：CMP 声明的关键配置项 vs 仓库实际存在性

| CI ID | 声明路径 | 实际存在 | 状态 |
|---|---|---|---|
| CI-CI-001 | `Jenkinsfile` | ✓ | 受控 |
| CI-CI-002 | `jenkins/README.md` | ✓ | 受控 |
| CI-CI-003 | `jenkins/email-template.groovy` | ✓ | 受控 |
| CI-CI-004 | `.github/workflows/ci.yml` | ✓ | 受控 |
| CI-CI-005 | `.github/workflows/deploy.yml` | ✓ | 受控 |
| CI-BS-008 | `Dockerfile` | ✓ | 受控 |
| CI-BS-009 | `frontend/admin-web/Dockerfile` | ✓ | 受控 |
| CI-BS-010 | `frontend/miniapp/Dockerfile` | ✓ | 受控 |
| CI-BS-011 | `docker-compose.yml` | ✓ | 受控 |
| CI-RC-004 | `src/main/resources/application-prod.yml` | ✓ | 受控 |
| CI-RC-006 | `nginx/admin-web.conf` | ✓ | 受控 |
| CI-RC-007 | `nginx/miniapp.conf` | ✓ | 受控 |
| CI-RC-008 | `.env.example` | ✓ | 受控 |
| CI-RC-009 | `.gitignore` | ✓ | 受控 |
| CI-DOC-007 | `docs/03-configuration-management-plan.md` | ✓ | 受控 |
| CI-DOC-010 | `docs/06-build-guide.md` | ✓ | 受控 |
| CI-DOC-011 | `docs/07-deployment-guide.md` | ✓ | 受控 |
| CI-DOC-014 | `docs/配置项登记表.md` | ✓ | 受控 |
| CI-DOC-015 | `docs/基线清单.md` | ✓ | 受控 |
| CI-DOC-016 | `docs/变更申请单-模板.md` | ✓ | 受控 |
| CI-DB-001 | `schema.sql` | ✓ | 受控 |
| CI-DB-002 | `src/main/resources/data-dev.sql` | ✓ | 受控 |

## 基线 tag 一致性（git tag vs docs/基线清单.md）

| tag | git tag -l 存在 | 基线清单记录 |
|---|---|---|
| `v1.0.0-fb` | ✓ | ✓ |
| `v1.0.0-m1` | ✓ | ✓ |
| `v1.0.0-m2` | ✓ | ✓ |
| `v1.0.0-m3` | ✓ | ✓ |
| `v1.0.0-m4` | ✓ | ✓ |
| `v1.0.0-m5` | ✓ | ✓ |
| `v1.0.0-m6` | ✓ | ✓ |
| `v1.0.0-rc1` | ✓ | ✓ |

## 审计结论

- 所有 CMP 声明的关键配置项在仓库中均存在
- 8 个基线 tag 全部与 docs/基线清单.md 一致
- .gitignore 正确排除 target/dist/node_modules/.env*
- 无密钥泄露（.env.example 全部为占位值）
