# PetCare O2O H5

PetCare O2O 是面向单体宠物门店的模块化单体应用。当前目标是先交付一个可在手机和桌面浏览器使用的响应式 H5 Web 应用，再考虑微信登录和微信小程序适配。

## 当前方向

- 用户端：响应式 H5，覆盖公开浏览、预约、商品订单、社区和营销活动。
- 管理端：PC Web，管理服务、预约、商品、订单、内容和基础营销活动。
- 后端：Spring Boot 模块化单体，MySQL 持久化，真实执行业务规则。
- AI：当前禁用并延后，不阻塞核心业务上线。
- 微信：微信登录和小程序适配在 H5 稳定后实施。

`frontend/miniapp` 已具备 UniApp H5 构建能力。为避免无价值的目录迁移，当前继续使用该目录，并将 `build:h5` 作为用户端主构建目标。

## V1 核心范围

- 服务浏览、宠物档案、地址和预约。
- 商品浏览、购物车、下单和订单状态流转。
- 社区发帖、浏览、点赞、评论和收藏。
- 营销活动列表、详情、管理及商品/服务关联。
- 后台业务管理和必要的风控审核。

不在当前范围：在线支付、多门店、优惠券、积分、独立员工端、AI 助手、AI 营销、医疗诊断。

## 当前实际状态

- 后端已具备大量预约、商品订单、社区和后台能力及测试。
- 管理端已有主要页面和接口接入。
- 用户端仍需要按 H5 纵向流程完成真实联调。
- 营销活动已有后端基础模型，但仍需补齐可用的 API 和界面。
- AI Provider 保持禁用。
- 已知认证风险和后续里程碑见 `docs/02-task-breakdown.md`。

以上是代码库现状说明，不代表所有用户流程已完成验收。

## 快速启动

### 数据库初始化

```powershell
mysql -u root -p < schema.sql
mysql -u root -p petcare_o2o < src/main/resources/data-dev.sql
```

### 后端

```powershell
mvn spring-boot:run
```

默认管理员账号：`admin` / `admin123456`（仅开发环境）。

### 管理端

```powershell
cd frontend/admin-web
npm install
npm run dev
```

### 用户 H5

```powershell
cd frontend/miniapp
npm install
npm run dev:h5
```

构建 H5：

```powershell
cd frontend/miniapp
npm run build:h5
```

具体环境变量和数据库要求以现有配置示例为准，禁止提交真实凭据。

## 核心文档

| 文档 | 用途 |
|---|---|
| `AGENTS.md` | Agent 快速执行规则 |
| `docs/requirements-source.md` | 当前产品需求基线 |
| `docs/00-project-boundary.md` | V1 范围和不可突破的业务边界 |
| `docs/01-architecture-design.md` | 当前系统架构和关键技术约束 |
| `docs/02-task-breakdown.md` | 加速后的交付路线图和当前优先级 |
| `docs/04-code-standards.md` | 必须遵守的代码与安全规则 |
| `docs/05-testing-and-verification.md` | 风险驱动验证方式 |
| `docs/08-pending-decisions.md` | 只记录真正需要用户决定的事项 |

历史阶段计划、Review 文档和详细交接记录已从工作区移除，需要时从 Git 历史查询。
