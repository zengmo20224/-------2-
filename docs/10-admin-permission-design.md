# 管理员细粒度权限设计

日期：2026-06-08

状态：已决定采用细粒度 RBAC，初始权限矩阵可在实施前继续调整

## 设计目标

- 权限粒度细到具体业务动作，而不是只判断是否登录。
- 使用 Spring Security 方法级授权。
- 默认拒绝未明确授予的权限。
- 高风险操作必须记录管理员操作日志。
- V1 保留 `SUPER_ADMIN`、`MANAGER`、`STAFF` 三种基础角色。

## 数据模型

保留：

- `admin_user.role`：保存管理员基础角色编码。

新增：

- `admin_role`：角色定义。
- `admin_permission`：权限码定义。
- `admin_role_permission`：角色和权限关系。

V1 一个管理员对应一个基础角色。后续如果需要一人多角色，再新增 `admin_user_role`。

### 表字段建议

#### admin_role

```text
id BIGINT PRIMARY KEY
role_code VARCHAR(64) UNIQUE
role_name VARCHAR(64)
description VARCHAR(255)
status VARCHAR(32)
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

#### admin_permission

```text
id BIGINT PRIMARY KEY
permission_code VARCHAR(128) UNIQUE
permission_name VARCHAR(100)
module VARCHAR(64)
description VARCHAR(255)
status VARCHAR(32)
create_time DATETIME
update_time DATETIME
deleted TINYINT
```

#### admin_role_permission

```text
id BIGINT PRIMARY KEY
role_id BIGINT
permission_id BIGINT
create_time DATETIME
```

唯一索引：

```text
role_id + permission_id
```

## 权限码规范

格式：

```text
模块:资源:动作
```

示例：

```text
store:config:read
store:config:update
booking:booking:confirm
booking:booking:reassign
community:post:hide
product:order:confirm-payment
admin:user:manage
```

## 初始权限清单

### 门店与服务

- `store:info:read`
- `store:info:update`
- `store:config:read`
- `store:config:update`
- `service:item:read`
- `service:item:create`
- `service:item:update`
- `service:item:disable`

### 员工与排班

- `staff:profile:read`
- `staff:profile:create`
- `staff:profile:update`
- `staff:profile:disable`
- `staff:skill:manage`
- `staff:schedule:read`
- `staff:schedule:manage`

### 预约

- `booking:booking:read`
- `booking:booking:confirm`
- `booking:booking:reject`
- `booking:booking:reassign`
- `booking:booking:start`
- `booking:booking:complete`
- `booking:booking:cancel`
- `booking:booking:mark-paid`

### 社区审核

- `community:post:read`
- `community:post:approve`
- `community:post:reject`
- `community:post:hide`
- `community:post:delete`
- `community:comment:hide`
- `community:comment:delete`
- `community:report:handle`
- `community:sensitive-word:manage`

### 商品与订单

- `product:item:read`
- `product:item:create`
- `product:item:update`
- `product:item:disable`
- `product:stock:update`
- `product:order:read`
- `product:order:confirm`
- `product:order:prepare`
- `product:order:ready`
- `product:order:confirm-payment`
- `product:order:complete`
- `product:order:cancel`

### 经营数据与 AI

- `analytics:dashboard:read`
- `analytics:report:export`
- `ai:analysis:generate`
- `ai:usage:read`

### 系统管理

- `admin:user:read`
- `admin:user:create`
- `admin:user:update`
- `admin:user:disable`
- `admin:role:manage`
- `admin:operation-log:read`

## 初始角色矩阵

### SUPER_ADMIN

- 拥有全部权限。
- 可以管理管理员账号、角色和权限。
- 高风险操作仍必须写操作日志。

### MANAGER

- 拥有门店、服务、员工、排班、预约、社区、商品订单、经营数据和 AI 分析权限。
- 默认不能管理 `SUPER_ADMIN`。
- 默认不能修改角色权限定义。
- 可以读取操作日志，但不能删除或修改操作日志。

### STAFF

默认只拥有日常执行权限：

- 读取服务、员工排班和预约。
- 开始、完成被分配的服务。
- 读取商品订单并执行备货、待自提操作。
- 读取待处理社区内容，但默认不能删除内容。
- 不能修改门店配置、服务项目、员工资料、角色权限。
- 不能确认收款，除非后续明确授予 `product:order:confirm-payment`。
- 不能生成或查看管理端 AI 经营分析。

## Spring Security 实现要求

- JWT 只保存管理员 id、基础角色和必要的 Token 元数据，不把完整权限列表长期写死在 Token 中。
- 每次认证后，从数据库加载角色对应权限。
- 使用 `@PreAuthorize("hasAuthority('booking:booking:confirm')")` 保护业务方法或 Controller 入口。
- Service 层关键动作必须再次校验资源范围和当前状态，不能只依赖前端隐藏按钮。
- 未授权返回 `403`。

## 操作日志要求

以下操作必须记录：

- 修改门店配置。
- 创建、禁用、修改员工。
- 修改排班。
- 确认、拒绝、改派、取消预约。
- 内容审核、隐藏、删除。
- 商品库存修改。
- 确认收款和取消订单。
- 创建、修改、禁用管理员。
- 修改角色权限。
- 生成 AI 经营分析报告。

日志至少包含：

- 管理员 id
- 权限码或操作类型
- 业务模块
- 目标资源 id
- 请求 id
- 结果
- 失败原因摘要
- 操作时间

禁止在日志中保存密码、JWT、API Key 或完整敏感请求体。

## 必测场景

- 未登录访问后台返回 `401`。
- 登录但缺少权限返回 `403`。
- `STAFF` 不能修改门店配置。
- `STAFF` 不能确认收款。
- `MANAGER` 不能修改 `SUPER_ADMIN`。
- `MANAGER` 不能修改角色权限。
- `SUPER_ADMIN` 可以管理角色权限。
- 高风险操作成功和失败都写入操作日志。
