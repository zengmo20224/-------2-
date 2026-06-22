# 更新日志（Changelog）

> 本文件记录 PetCare O2O 项目所有重要版本与变更，按 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/) 风格维护，遵循 [SemVer](https://semver.org/lang/zh-CN/)。
>
> 基线 tag 与本文件一一对应，详见 `docs/基线清单.md`。

---

## [Unreleased] / v1.0.0-rc1 — 2026-06-22

### 新增（配置管理 / CI/CD — 课程作业核心）

- **配置管理计划**：新增 `docs/03-configuration-management-plan.md`，按 IEEE 828 结构覆盖组织、配置项识别、版本控制、基线管理、变更控制、配置审计、状态报告。
- **配置项登记表**：新增 `docs/配置项登记表.md`，登记 7 类共 76 个配置项。
- **基线清单**：新增 `docs/基线清单.md`，记录功能基线、M1–M6 里程碑基线、产品基线。
- **变更申请单**：新增 `docs/变更申请单-模板.md` 及 2 个实例（CR-20260613-001 登录方式变更、CR-20260622-002 配置管理基线建立）。
- **构建指导书**：新增 `docs/06-build-guide.md`。
- **部署指南**：新增 `docs/07-deployment-guide.md`（Docker Compose 单机部署）。
- **Docker 化**：新增后端 `Dockerfile`（多阶段 maven→JRE）、`frontend/admin-web/Dockerfile`、`frontend/miniapp/Dockerfile`。
- **容器编排**：新增 `docker-compose.yml`（mysql + api + admin-web nginx + h5 nginx）与 `nginx/admin-web.conf`、`nginx/miniapp.conf`。
- **生产配置**：新增 `src/main/resources/application-prod.yml`、补全 `.env.example`。
- **Jenkins 流水线**：新增 `Jenkinsfile`（编译/测试/打包/Docker/部署/邮件通知）、`jenkins/README.md`、`jenkins/email-template.groovy`。
- **GitHub Actions**：新增 `.github/workflows/ci.yml`（push/PR 编译测试）、`.github/workflows/deploy.yml`（tag 触发自动部署）。
- **本 CHANGELOG**：从 git log 回填全部历史。

### 变更

- `.gitignore` 补充 Docker / CI 相关忽略项。

---

## [v1.0.0-m6] — 2026-06-14 · 里程碑基线：发布收口

### 新增

- 用户认证：手机号 + 密码登录，安全问题找回密码（CR-20260613-001）`cce58fd`
- 安全问题下拉选择，密码强度策略（8–32 字符 + 字母数字）`6fe0d29` `038da30`
- 服务卡片与详情封面图 `a3f7047`

### 修复

- 修正种子数据 BCrypt 哈希，补开发用 JWT secret `9cccea6`
- H5 安全问题下拉改用原生 select `c562119`
- M6 收尾编译错误、响应式与测试 `9e8d2b8`

---

## [v1.0.0-m5] — 2026-06-13 · 里程碑基线：社区互动

### 新增

- 社区发帖、点赞、评论、收藏 `b35fb07`
- 公开社区隐私契约测试 `c9c309b`
- 匿名公开目录只读 `3de8153` `dd7a1ff`

---

## [v1.0.0-m4] — 2026-06-13 · 里程碑基线：商品与订单

### 新增

- 购物车、结算、下单完整流程 `d31da27`
- 当前用户地址增删改查 API、默认地址并发事务 `e825208` `310379f` `f01c5bd`
- 宠物档案 CRUD API `143f5c7`

### 修复

- 默认地址切换 fail-closed `8463655`
- 禁用用户写地址返回 401 `310f358`
- 宠物档案写入失败拒绝与空指针 `eaabd39` `fd10668`
- 用户手机号脱敏 `aab5300`，活跃用户边界 `d0f9a95`

---

## [v1.0.0-m3] — 2026-06-13 · 里程碑基线：用户资料与真实预约

### 新增

- 用户 JWT 认证基础 `72a1975`，测试 profile 登录 `49dd45a`
- 当前用户资料读写 API `3399b83`
- 端到端预约流程 `9f8d647`

### 修复

- 用户与管理员 JWT 认证加固 `f8ac5b1`
- 测试登录服务隔离 `c9cef9f`

---

## [v1.0.0-m2] — 2026-06-13 · 里程碑基线：公开浏览与营销

### 新增

- H5 公开浏览集成 `cadc705`
- 营销活动后端、H5 页面与种子数据 `e0c9adf`

---

## [v1.0.0-m1] — 2026-06-13 · 里程碑基线：H5 基础与演示数据

### 新增

- H5-first 加速交付模型文档基线 `e9bb199`
- 公开浏览种子数据 `9b7cbcc`

---

## [v1.0.0-fb] — 2026-06-09 ~ 2026-06-12 · 功能基线

### 新增（管理端 H01–H12）

- PetCare 设计令牌与 App 外壳（H01）`2731729`
- 页面状态组件与反馈工具（H02）`8244bae`
- 表格/筛选/抽屉/确认对话框组件（H03）`2f334b0`
- 登录页与错误页重设计（H04）`283fde7`
- 预约管理共享组件迁移（H05）`a251fb9`
- 员工管理共享组件（H06）`92a52e9`
- 服务/商品迁移至共享组件（H07）`8469cd0`
- 商品订单迁移（H08）`226ce78`
- 社区贴子与举报迁移（H09）`62b9aa5`
- 门店与操作日志（H10）`70e2208`
- 契约测试与敏感词页面重构（H11）`cd79e84`
- 最终质量门禁测试（H12）`27b2bed`

### 新增（管理端骨架）

- Vue 3 + Vite + Element Plus 骨架、认证与路由 `9acf1e0`
- 门店信息与配置页 `267c7d1`
- 全部管理页面与权限控制 `076374a`

### 新增（用户端）

- uni-app 用户端脚手架 `f551e23`

### 修复（质量加固）

- 操作审计日志、并发加固、Snowflake DTO 修复 `7ba8941`
- 强制必需的操作审计日志 `42ccd83`
- 预约管理操作审计可靠性 `f432d3d` `8b4cd7c`
- 管理端开放重定向与错误信息脱敏 `8485c5c`
- API DTO 字段与可空性对齐后端 `d963ad4`
- 订单动作守卫、报表过滤、移除假 API `fb1ca59`
- 状态字典与动作守卫对齐 `6c5d6ef`

### 新增（预约并发与契约加固 — 风险修复 RM-B02/B03）

- 预约状态转换原子性与行锁 `bb5896f`，回归测试 `188eb9f`
- 预约人员重分配加锁 `08519db`，回归测试 `030232f`
- MySQL 安全的员工日期锁 upsert `01565ab`，锁回归测试 `d41c3a0`
- 外部 Snowflake ID 序列化为字符串 `f8e4b08` `b9c7e84`

### 测试

- Testcontainers schema 与并发门禁 `dd58edf`
- 管理端状态契约回归 `f6463f9`
- 管理端 API 契约回归 `08ab84b`
- Snowflake ID 序列化契约 `6533342` `f41b1e9`

### 文档

- AGENTS.md、决策记录、路线图更新 `119ef1e`
- 已批准项目决策记录 `e20a6a9`

---

## 版本号规则提示

- `vMAJOR.MINOR.PATCH-mN`：里程碑基线（M1–M6）
- `vMAJOR.MINOR.PATCH-rcN`：发布候选
- `vMAJOR.MINOR.PATCH`：正式发布
- `vMAJOR.MINOR.PATCH-hfN`：紧急修复

详见 `docs/03-configuration-management-plan.md` §4.3、§5.2。
