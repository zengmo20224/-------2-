# 任务拆解

日期：2026-06-08

## 阶段 0：规划基线

状态：已完成初版

交付物：

- 项目边界文档
- 架构设计文档
- GLM5.1 实施计划
- 代码规范
- 测试与验证门禁
- Git 防护流程
- AI Agent 强制规则
- 前期待决策事项清单
- 预约并发防冲突研究
- 管理员细粒度权限设计

退出标准：

- 文档已提交到 Git。
- GLM5.1 可以在不猜测项目范围的情况下开始阶段 1。
- 所有执行 Agent 在修改文件前阅读根目录 `AGENTS.md`。
- 涉及未决事项时，必须先获得用户决策。
- 涉及预约并发和后台权限时，必须遵守对应研究和设计文档。

## 阶段 1：数据库 Schema

负责人：GLM5.1

交付物：

- `schema.sql`
- 如需解释字段微调，可新增 `docs/schema-notes.md`

必做任务：

1. 为原始需求中的每张表生成完整 MySQL 8 DDL。
2. 表名和字段名保持与需求一致，除非有文档化的修正理由。
3. 添加表注释和重要字段注释。
4. 添加合理的状态默认值。
5. 添加 `create_time DEFAULT CURRENT_TIMESTAMP`。
6. 添加 `update_time DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`。
7. 添加 `deleted TINYINT DEFAULT 0`。
8. 为 `openid`、`username`、`booking_no`、`order_no` 和关系唯一性添加唯一索引。
9. 为高频查询添加索引。
10. 根据细粒度 RBAC 决策新增 `admin_role`、`admin_permission`、`admin_role_permission`。
11. 根据预约并发方案新增 `staff_booking_lock`，并添加 `(staff_id, booking_date)` 唯一索引。
12. 导入 MySQL 8 验证。

退出标准：

- SQL 可以导入一个干净数据库。
- `SHOW TABLES` 确认所有表存在。
- 代表性 `SHOW CREATE TABLE` 检查确认注释、默认值和索引存在。
- 验证后的 Schema 已提交 Git。

## 阶段 2：后端骨架

负责人：GLM5.1

启动说明：`docs/11-phase-2-backend-skeleton-brief.md`

交付物：

- Spring Boot 3 项目
- Maven 或 Gradle 构建文件
- `application.yml`
- 统一响应和错误结构
- 基础包结构和模块目录
- MyBatis-Plus 配置
- 通过环境变量配置数据库连接

退出标准：

- 应用可以本地启动。
- 健康检查接口可用。
- 构建通过。
- 没有硬编码密钥。
- 已提交 Git。

## 阶段 3：核心实体与 Mapper

负责人：GLM5.1

启动说明：`docs/12-phase-3-entities-mappers-plan.md`

交付物：

- 与 `schema.sql` 匹配的实体类
- Mapper 接口
- 各模块基础 CRUD Service
- 状态字段对应的常量或枚举

退出标准：

- Mapper 集成测试在测试数据库上通过。
- 状态常量与数据库默认值一致。
- 业务流程中没有 Controller 直接调用 Mapper 的捷径。
- 已提交 Git。

## 阶段 4：认证与授权

负责人：GLM5.1

启动说明：`docs/13-phase-4-auth-authorization-plan.md`

交付物：

- 管理员登录
- 为后续微信登录预留的用户登录占位实现
- Token 认证
- 后台角色校验
- 用户资源归属校验

退出标准：

- 未认证请求返回 `401`。
- 无权限角色访问返回 `403`。
- 权限码从数据库加载，并通过 Spring Security 方法级授权生效。
- 微信登录占位接口返回“暂未启用”，不伪造 openid。
- 密码哈希测试存在。
- Token 和密码不会被日志记录。
- 已提交 Git。

## 阶段 5：服务预约与排班

负责人：GLM5.1

启动说明：`docs/14-phase-5-booking-scheduling-plan.md`

交付物：

- 服务列表
- 可预约时间计算
- 预约创建
- 商家确认、拒绝、取消、完成
- 状态日志记录
- 人工改派员工
- 上门服务半径校验

退出标准：

- 单元测试覆盖时间段扣减和时间槽生成。
- 集成测试覆盖预约冲突。
- 真实 MySQL 并发测试覆盖重叠预约只有一个成功。
- 用户不能指定员工。
- 上门服务半径校验通过。
- 后台预约操作使用权限码保护。
- 取消和拒绝的预约不占用时间。
- 待确认、已确认、服务中预约占用时间。
- 已提交 Git。

## 阶段 6：社区与内容审核

负责人：GLM5.1

启动说明：`docs/15-phase-6-community-moderation-plan.md`

交付物：

- 话题、帖子、图片、评论 API
- 点赞、收藏、举报 API
- 敏感词匹配
- 审核记录创建
- 后台审核 API

退出标准：

- 测试覆盖无风险、轻度风险、中度风险、严重风险内容路径。
- 严重风险内容被拒绝。
- 轻度和中度风险内容进入待审核。
- 评论同样经过内容审核。
- 后台审核、隐藏、删除、举报处理使用权限码保护。
- 图片上传未决事项没有被擅自实现。
- 已提交 Git。

## 阶段 7：商品到店自提订单

负责人：GLM5.1

启动说明：`docs/16-phase-7-product-orders-plan.md`

交付物：

- 商品列表
- 购物车管理
- 自提订单创建
- 商家确认
- 备货、待自提、确认收款、完成、取消流程

退出标准：

- 库存和订单金额在事务中处理。
- 防止购物车重复行。
- 订单状态流转有测试。
- 客户端价格和总金额不作为可信数据。
- 两个并发订单竞争最后库存时只有一个成功。
- 后台订单操作使用权限码保护。
- 未实现在线支付、配送和复杂营销计价。
- 已提交 Git。

## 阶段 8：AI Provider 与 AI 功能

负责人：GLM5.1

启动说明：`docs/17-phase-8-ai-provider-functions-plan.md`

交付物：

- 统一 AI Provider Client
- 会话和消息持久化
- 用量日志
- AI 客服上下文构造器
- 宠物陪伴安全边界
- 发帖辅助
- 基于后端聚合数据生成管理端分析报告

退出标准：

- AI Provider 可以在测试中 Mock。
- 启用真实 Provider 时，API Key 或其他必填 Provider 配置缺失会快速失败；Provider 默认关闭时不阻塞非 AI 业务启动。
- AI 客服回答基于传入上下文。
- 高风险宠物症状会触发建议就医。
- AI 不能直接查询数据库。
- Provider 层不能依赖 Mapper、DataSource 或数据库连接。
- AI 不生成并执行 SQL，不直接修改核心业务状态。
- Provider 原始错误、Prompt 和 API Key 不会泄露。
- 使用 Mock Provider 的自动化测试通过。
- D-008 已决定，但阶段 14B 未完成真实 Provider 验收前，不声称真实 DeepSeek 已接入。
- 已提交 Git。

## 阶段 9：后台 API 完成

负责人：GLM5.1

交付物：

- 门店配置管理
- 服务管理
- 员工、技能、排班管理
- 预约管理
- 商品管理
- 内容审核管理
- 操作日志

退出标准：

- 后台接口需要角色权限。
- 重要后台操作写入操作日志。
- 列表接口支持分页。
- 已提交 Git。

## 阶段 10：前端集成

负责人：后续前端实现者

实施顺序：D-010 已决定先管理后台，再小程序。

详细剩余阶段、任务包、依赖关系和退出门禁见：

- `docs/21-remaining-development-roadmap.md`
- `docs/22-continuous-agent-development-rules.md`

交付物：

- 小程序用户流程
- 管理后台流程
- API 接入
- 浏览器或小程序模拟器验证

退出标准：

- 关键流程有 E2E 证据。
- UI 覆盖加载、空数据、错误和成功状态。
- 已提交 Git。

当前下一步：

- 先执行阶段 10F，保护、审查并收口当前未提交管理后台代码。
- 阶段 10F 未形成独立验证提交前，不启动小程序或无关后端开发。

## 阶段 11 至阶段 16：剩余开发与发布

阶段 11 至阶段 16 的详细拆解统一维护在 `docs/21-remaining-development-roadmap.md`：

- 阶段 11：小程序前置后端能力。
- 阶段 12：小程序基础与公开浏览。
- 阶段 13：小程序身份与核心业务流程。
- 阶段 14：文件上传和真实 DeepSeek 等决策门控扩展能力。
- 阶段 15：全链路联调与真实数据库验证。
- 阶段 16：候选发布与最终交接。

所有阶段必须使用 `docs/22-continuous-agent-development-rules.md` 的任务包、验证、Git 和交接规则。
