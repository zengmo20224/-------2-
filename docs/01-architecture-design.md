# 架构设计

日期：2026-06-08

## 系统形态

V1 使用模块化单体架构。

推荐技术栈：

- 小程序端：uni-app + Vue 3 + Pinia
- 管理后台：Vue 3 + Vite + Element Plus
- 后端：Spring Boot 3 + MyBatis-Plus
- 认证：Spring Security + JWT
- 数据库：MySQL 8
- 主键：MyBatis-Plus 雪花 ID，数据库不使用 `AUTO_INCREMENT`
- 数据库初始化：仅维护 `schema.sql`
- 缓存：V1 不启用 Redis；Redis 作为 V2 可选能力
- 部署：Docker Compose + Nginx + MySQL
- AI：后端统一持有 AI Provider Client，按 OpenAI 兼容协议为 DeepSeek 接入预留

## 后端模块边界

后端包结构按业务能力拆分，不按 Controller、Service、Mapper 这种纯技术类型堆在一起。

推荐模块：

- `user`：用户账号、宠物档案、用户地址
- `store`：门店和门店配置
- `service`：服务分类和服务项目
- `staff`：员工、技能、排班、不可用时间
- `booking`：预约生命周期、状态日志、可预约时间计算
- `community`：话题、帖子、图片、评论、点赞、收藏、举报
- `moderation`：敏感词和内容审核记录
- `product`：商品分类、商品、图片、购物车、自提订单
- `marketing`：营销活动、商品关联、服务关联
- `ai`：会话、消息、用量日志、分析报告、FAQ 知识库、AI Provider Client
- `admin`：后台管理员和操作日志
- `common`：响应封装、异常、校验、安全上下文、分页

## 分层规则

依赖方向如下：

```text
Controller -> Application/Service -> Domain Rules -> Repository/Mapper -> Database
```

规则：

- Controller 负责请求形状校验和调用 Service。
- Service 负责业务事务和状态流转。
- Domain helper 负责距离计算、可预约时间计算等纯规则。
- Mapper 只负责持久化。
- Controller 不能直接调用另一个 Controller。
- AI Provider 不能从 SQL 或 Mapper 层直接调用。

## API 契约

REST API 统一放在 `/api/v1` 下。

成功响应结构：

```json
{
  "success": true,
  "data": {},
  "error": null,
  "meta": {}
}
```

错误响应结构：

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "validation_error",
    "message": "请求参数不合法",
    "details": []
  },
  "meta": null
}
```

HTTP 状态码规则：

- `200`：查询或更新成功
- `201`：创建成功
- `204`：删除成功且无响应体
- `400`：请求格式错误
- `401`：未认证
- `403`：无权限
- `404`：资源不存在
- `409`：重复数据或状态冲突
- `422`：业务语义不合法
- `500`：未预期服务端错误，不能泄露堆栈

## 数据模型方向

MySQL 8 规范：

- 使用 InnoDB
- 字符集使用 `utf8mb4`
- 主键使用 `BIGINT`
- 金额使用 `DECIMAL(10,2)`
- 经纬度使用 `DECIMAL(10,6)`
- 状态字段使用可读的 `VARCHAR(32)`
- 普通业务表包含 `create_time`、`update_time`、`deleted`
- 高频查询字段建立索引
- 业务编号和自然唯一字段建立唯一索引

第一项实现任务是 `schema.sql`，它是后续实体类和 Mapper 生成的基础。

## 核心算法

### 可预约时间计算

输入：

- `service_item_id`
- 预约日期
- 门店 id

过程：

1. 读取服务时长、分类、服务模式和状态。
2. 找出具备该服务分类技能的员工。
3. 加载这些员工当天的排班。
4. 排除不可用排班。
5. 扣除午休、请假、临时不可用等时间段。
6. 扣除 `PENDING_CONFIRM`、`CONFIRMED`、`IN_SERVICE` 状态的已占用预约。
7. 根据 `store_config.time_slot_minutes` 生成开始时间。
8. 只保留 `start_time + duration_minutes <= available_interval.end_time` 的时间段。

输出：

- 内部可以按员工分组保存可用时间。
- 用户侧只展示可预约时间，不暴露“用户选择员工”的能力。

### 上门服务距离校验

V1 使用经纬度直线距离。

必须校验：

- 服务模式允许 `HOME`。
- 地址存在且属于当前用户。
- 地址和门店都有经纬度。
- 距离不超过 `store_config.home_service_radius_km`。

### AI 数据接地

AI 调用必须接收后端明确整理过的上下文：

- AI 客服：门店、门店配置、服务、商品、FAQ。
- 管理端分析：后端 SQL 聚合结果。
- 发帖辅助：用户明确提供的事实。

AI 输出不能被当成系统事实来源。

### 预约并发控制

V1 不使用 Redis 分布式锁，采用“员工日期锁定点 + 数据库事务 + 冲突二次检查 + 死锁有限重试”。

用户已批准新增 `staff_booking_lock` 表，阶段 1 必须将其写入 `schema.sql`。详细方案见 `docs/09-booking-concurrency-control.md`。

### 后台权限

后台使用 Spring Security + JWT 和细粒度 RBAC 权限码。

权限设计见 `docs/10-admin-permission-design.md`。

## 安全设计

- 密码必须哈希存储，不能明文保存。
- AI API Key 必须来自环境变量或密钥管理。
- 后台 API 必须有角色校验。
- 用户 API 必须有资源归属校验。
- 文件上传必须校验扩展名、大小和 Content-Type。
- 单文件大小上限为 10 MB。
- 错误响应不能暴露 SQL、堆栈、密钥或 Provider 原始错误。
- 管理员关键操作需要写入操作日志。
