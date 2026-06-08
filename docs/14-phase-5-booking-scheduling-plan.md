# 阶段 5：服务预约与排班代码计划

日期：2026-06-08

负责人：GLM5.1

当前分支：`phase-5-booking-scheduling`

前置条件：

- 阶段 4 认证与授权已完成，提交为 `edfa4d4 feat: add authentication and authorization`。
- `staff_booking_lock`、`service_booking`、`booking_status_log`、`staff_schedule`、`staff_unavailable_time` 等表和实体已存在。
- 后台权限码和 `SecurityContextHelper` 已存在。

## 目标

本阶段实现项目 P0 核心能力：服务预约与真实排班。

必须完成：

- 服务分类和服务项目查询。
- 可预约时间计算。
- 用户创建预约的业务服务。
- 系统自动选择可用员工，用户不能指定员工。
- 上门服务地址和距离校验。
- 预约状态流转：确认、拒绝、取消、开始服务、完成。
- 管理员人工改派员工。
- 预约状态日志。
- 使用 `staff_booking_lock` 实现同一员工同一日期的并发防冲突。
- 使用阶段 4 权限码保护后台预约操作。

本阶段不实现微信支付、线上支付、前端页面、真实微信登录、AI、社区、商品订单。

## 必须先阅读

GLM5.1 开始编码前必须阅读：

1. `AGENTS.md`
2. `README.md`
3. `schema.sql`
4. `docs/00-project-boundary.md`
5. `docs/01-architecture-design.md`
6. `docs/02-task-breakdown.md`
7. `docs/03-glm5-implementation-plan.md`
8. `docs/04-code-standards.md`
9. `docs/05-testing-and-verification.md`
10. `docs/06-git-safety-workflow.md`
11. `docs/07-integration-gates.md`
12. `docs/08-pending-decisions.md`
13. `docs/09-booking-concurrency-control.md`
14. `docs/10-admin-permission-design.md`
15. `docs/13-phase-4-auth-authorization-plan.md`
16. 本文件

编码前必须先运行：

```powershell
git status --short --branch
git log --oneline -5
```

然后先输出：

```text
已阅读文档：
当前阶段：阶段 5 服务预约与排班
当前分支：phase-5-booking-scheduling
计划修改范围：
不会修改的范围：
待决策或阻塞项：
验证计划：
```

没有完成以上汇报前，不允许修改项目文件。

## 已决定事项

必须按以下决策执行：

- V1 不启用 Redis。
- 预约并发防冲突使用“员工日期锁定点 + 数据库事务 + 冲突二次检查 + 死锁有限重试”。
- `PENDING_CONFIRM`、`CONFIRMED`、`IN_SERVICE` 占用员工时间。
- `CANCELLED`、`REJECTED`、`COMPLETED` 不占用员工时间。
- 用户不能指定员工。
- 上门服务必须校验地址和服务半径。
- 服务半径来自 `store_config.home_service_radius_km`，不能写死。
- 微信登录仍为占位，不能伪造 openid 或生产用户身份。
- 后台操作必须使用阶段 4 的权限码授权。

## 阶段 5 允许做的事

允许新增：

- `booking/controller`
- `booking/dto`
- `booking/domain`
- `booking/service` 中的预约业务方法
- `booking/service/impl` 中的事务服务、重试服务
- `booking/mapper` 的必要自定义 SQL 方法
- 服务项目查询 Controller 和 DTO
- 预约、排班、距离、状态流转、并发冲突测试

允许调整：

- `ServiceBookingMapper` 增加冲突查询、自定义锁相关查询。
- `StaffBookingLockMapper` 增加锁定点 upsert 和 `SELECT ... FOR UPDATE`。
- `ErrorCode` 增加预约相关业务错误码。
- `GlobalExceptionHandler` 增加数据库冲突、死锁重试失败等业务错误映射。
- 现有基础 Service 增加业务方法，但 Controller 仍不能直接调用 Mapper。

## 阶段 5 禁止做的事

本阶段禁止：

- 不实现真实微信登录。
- 不在请求体中接受 `userId` 作为用户身份来源来绕过认证。
- 不允许用户选择或指定 `staffId`。
- 不实现微信支付或任何线上支付。
- 不引入 Redis。
- 不引入 Flyway 或 Liquibase。
- 不修改 `schema.sql`，除非发现阻塞级错误并先征得用户确认。
- 不实现社区、商品订单、AI、文件上传或前端。
- 不在事务中调用 AI、文件服务、第三方 API 或其他慢操作。
- 不把 SQL 异常、堆栈、Token、手机号完整明文等敏感信息返回给客户端。

## 用户身份边界

阶段 4 只保留微信登录接口，尚未提供真实用户登录 Token。因此阶段 5 必须避免用假登录污染生产逻辑。

要求：

- 业务服务可以设计为 `createBooking(currentUserId, request)`。
- Controller 不能从请求体读取 `userId`。
- 如果生产环境没有当前用户身份，用户创建预约接口应返回 `401`。
- 测试可以通过测试专用安全上下文或直接调用 Service 传入 `userId`。
- 不允许新增“模拟登录”“测试登录”“后门 header user id”进入生产代码。

如果 GLM5.1 认为必须先实现用户 JWT，必须停止阶段 5 相关 Controller 实现，先报告冲突，由用户决定是否调整阶段 4 范围。

## 推荐包结构

```text
src/main/java/com/petcare/
  booking/
    controller/
      BookingController.java
      AdminBookingController.java
    dto/
      BookingAvailabilityRequest.java
      BookingAvailabilityResponse.java
      BookingCreateRequest.java
      BookingResponse.java
      BookingConfirmRequest.java
      BookingRejectRequest.java
      BookingCancelRequest.java
      BookingReassignRequest.java
    domain/
      TimeRange.java
      BookingAvailabilityCalculator.java
      BookingDistanceCalculator.java
      BookingStateMachine.java
      StaffAssignmentPolicy.java
    service/
      BookingApplicationService.java
      BookingTransactionService.java
      BookingRetryService.java
      BookingAvailabilityService.java
    service/impl/
      ...
  service/
    controller/
      ServiceCatalogController.java
    dto/
      ServiceCategoryResponse.java
      ServiceItemResponse.java
```

说明：

- `domain` 只放纯规则，不直接访问数据库。
- `BookingTransactionService` 负责单次事务尝试。
- `BookingRetryService` 负责死锁、锁等待超时的有限重试。
- 避免在同一个类内部调用 `@Transactional` 方法导致事务失效。

## API 设计

### 服务目录

```text
GET /api/v1/service-categories
GET /api/v1/service-items
GET /api/v1/service-items/{id}
```

查询要求：

- 只返回 `status = ON_SALE` 且 `deleted = 0` 的服务项目。
- 支持按 `categoryId`、`serviceMode`、`petType`、`petSize` 过滤。
- 列表接口支持分页或明确限制最大返回数量。

### 可预约时间

```text
GET /api/v1/bookings/availability?storeId=1&serviceItemId=2&bookingDate=2026-06-15&serviceMode=STORE
```

响应示例：

```json
{
  "success": true,
  "data": {
    "storeId": 1,
    "serviceItemId": 2,
    "bookingDate": "2026-06-15",
    "serviceMode": "STORE",
    "durationMinutes": 90,
    "timeSlotMinutes": 30,
    "slots": [
      {
        "startTime": "10:00",
        "endTime": "11:30",
        "availableStaffCount": 2
      }
    ]
  }
}
```

要求：

- 不返回 `staffId`，避免用户选择员工。
- 只展示至少有一个可用员工的时间段。
- 如果服务不可售、日期超出提前预约天数、服务模式不支持，返回业务错误。

### 创建预约

```text
POST /api/v1/bookings
```

请求示例：

```json
{
  "storeId": 1,
  "serviceItemId": 2,
  "petId": 3,
  "serviceMode": "HOME",
  "bookingDate": "2026-06-15",
  "startTime": "10:00",
  "addressId": 4,
  "contactName": "张三",
  "contactPhone": "13800000000",
  "paymentMethod": "OFFLINE_HOME",
  "remark": "宠物比较怕生"
}
```

创建规则：

- 当前用户身份来自安全上下文，不来自请求体。
- `endTime = startTime + service_item.duration_minutes`。
- 初始状态为 `PENDING_CONFIRM`。
- 初始支付状态为 `UNPAID`。
- `booking_no` 由后端生成。
- 系统自动分配一个可用员工。
- 写入 `booking_status_log`，`old_status = null`，`new_status = PENDING_CONFIRM`。
- 返回 `201 Created`。

### 用户预约查询和取消

```text
GET /api/v1/bookings/my
GET /api/v1/bookings/{id}
POST /api/v1/bookings/{id}/cancel
```

要求：

- 只能访问当前用户自己的预约。
- 用户取消必须校验取消提前时间 `store_config.booking_cancel_hours`。
- 已完成、已拒绝、已取消预约不能再次取消。

### 后台预约管理

```text
GET /api/v1/admin/bookings
GET /api/v1/admin/bookings/{id}
POST /api/v1/admin/bookings/{id}/confirm
POST /api/v1/admin/bookings/{id}/reject
POST /api/v1/admin/bookings/{id}/start
POST /api/v1/admin/bookings/{id}/complete
POST /api/v1/admin/bookings/{id}/cancel
POST /api/v1/admin/bookings/{id}/reassign
```

权限要求：

```text
GET /api/v1/admin/bookings           booking:booking:read
POST /confirm                        booking:booking:confirm
POST /reject                         booking:booking:reject
POST /start                          booking:booking:start
POST /complete                       booking:booking:complete
POST /cancel                         booking:booking:cancel
POST /reassign                       booking:booking:reassign
```

要求：

- 使用 `@PreAuthorize("hasAuthority('...')")`。
- 管理员身份来自阶段 4 的 `SecurityContextHelper`。
- 后台关键操作必须写入 `booking_status_log`。
- 如果已有 `AdminOperationLogService` 可用，确认、拒绝、取消、改派、开始、完成也应写入 `admin_operation_log`；如果尚未实现细节，必须在交接中记录未完成风险。

## 可预约时间计算规则

输入：

- `storeId`
- `serviceItemId`
- `bookingDate`
- `serviceMode`

流程：

1. 读取服务项目，要求 `status = ON_SALE` 且 `deleted = 0`。
2. 校验服务模式：
   - `STORE` 服务只能预约到店。
   - `HOME` 服务只能预约上门。
   - `BOTH` 可以预约到店或上门。
3. 读取门店配置，校验 `bookingDate` 不超过 `booking_advance_days`。
4. 找到具备该服务分类技能的员工。
5. 过滤 `staff.status = ACTIVE` 且 `deleted = 0`。
6. 读取员工当天 `staff_schedule.status = AVAILABLE` 的排班。
7. 扣除 `staff_unavailable_time`。
8. 扣除有效预约：
   - `PENDING_CONFIRM`
   - `CONFIRMED`
   - `IN_SERVICE`
9. 以 `store_config.time_slot_minutes` 生成候选开始时间。
10. 保留 `start_time + duration_minutes <= 可用区间.end_time` 的时间段。
11. 按开始时间聚合，返回 `availableStaffCount`，不返回员工 id。

区间重叠判断统一使用：

```text
existing.start < requested.end
AND existing.end > requested.start
```

首尾相接不算重叠：

```text
10:00-11:00 与 11:00-12:00 可以同时存在
```

## 员工自动分配策略

用户创建预约时不能传 `staffId`。

推荐策略：

1. 根据可预约时间计算找出目标时间段可用员工。
2. 选择当天有效预约数最少的员工。
3. 如果数量相同，选择 `staff_id` 最小的员工。
4. 在事务内再次执行冲突检查，不能只信任前置计算结果。

该策略确定、可测试、不会暴露员工选择权。

## 上门服务距离校验

当 `serviceMode = HOME` 时必须：

1. 请求中有 `addressId`。
2. 地址属于当前用户。
3. 地址经纬度存在。
4. 门店经纬度存在。
5. 读取 `store_config.home_service_radius_km`。
6. 使用经纬度直线距离计算 `distance_km`。
7. 如果超出服务半径，返回 `422`，错误码 `booking_home_distance_exceeded`。

距离计算建议使用 Haversine 公式，结果保留 2 位小数。

错误消息示例：

```text
当前地址距离门店约 9.20 公里，超出本店 5.00 公里上门服务范围，请更换地址或选择到店服务。
```

## 状态流转规则

允许流转：

```text
null -> PENDING_CONFIRM
PENDING_CONFIRM -> CONFIRMED
PENDING_CONFIRM -> REJECTED
PENDING_CONFIRM -> CANCELLED
CONFIRMED -> IN_SERVICE
CONFIRMED -> CANCELLED
IN_SERVICE -> COMPLETED
```

管理员可额外执行：

```text
PENDING_CONFIRM -> CANCELLED
CONFIRMED -> CANCELLED
```

禁止：

- `COMPLETED` 后继续修改业务状态。
- `CANCELLED` 后确认、开始、完成。
- `REJECTED` 后确认、开始、完成。
- `IN_SERVICE` 直接拒绝。
- 用户取消别人的预约。

每次状态变化必须写 `booking_status_log`：

```text
booking_id
old_status
new_status
operator_type = USER / ADMIN / SYSTEM
operator_id
remark
create_time
```

## 并发防冲突实现要求

预约创建、管理员改派员工、修改预约时间都必须走同一锁定流程。

单次事务流程：

1. 开启 `@Transactional`。
2. 使用 `INSERT ... ON DUPLICATE KEY UPDATE` 确保 `(staff_id, booking_date)` 锁定点存在。
3. 使用 `SELECT ... FOR UPDATE` 锁定该员工该日期的锁定点。
4. 查询目标时间段是否存在有效预约重叠。
5. 冲突则抛出业务异常，返回 `409`，错误码 `booking_time_conflict`。
6. 无冲突则插入或更新预约。
7. 写入 `booking_status_log`。
8. 提交事务。

自定义 Mapper 建议：

```java
int upsertStaffBookingLock(@Param("staffId") Long staffId, @Param("bookingDate") LocalDate bookingDate);

StaffBookingLock selectStaffBookingLockForUpdate(@Param("staffId") Long staffId, @Param("bookingDate") LocalDate bookingDate);

List<ServiceBooking> selectConflictingBookings(...);
```

SQL 要求：

- 不使用字符串拼接。
- 参数必须绑定。
- 冲突查询必须带 `deleted = 0`。
- 冲突状态只包含 `PENDING_CONFIRM`、`CONFIRMED`、`IN_SERVICE`。
- 改派时冲突查询必须排除当前预约 id。

## 死锁和锁等待重试

要求：

- 捕获 MySQL 死锁和锁等待超时。
- 只重试整个预约事务，不重试事务内部片段。
- 最多重试 2 次。
- 使用短随机退避。
- 重试仍失败，返回可重试业务错误，不伪装成功。
- 日志可记录 request id、staff id、booking date、时间段、重试次数。
- 日志不能记录 Token、完整手机号、敏感请求体。

实现结构建议：

```text
BookingRetryService.createBookingWithRetry(...)
  -> BookingTransactionService.createBookingOnce(...) @Transactional
```

避免同类内部调用导致事务注解失效。

## 错误码建议

在 `ErrorCode` 中新增：

```text
booking_time_conflict
booking_slot_unavailable
booking_status_invalid
booking_service_unavailable
booking_date_out_of_range
booking_home_distance_exceeded
booking_address_required
booking_address_not_found
booking_staff_unavailable
booking_retry_exhausted
```

HTTP 状态建议：

- `400`：参数格式错误。
- `401`：需要用户或管理员认证但缺失。
- `403`：无权限或资源不属于当前用户。
- `404`：预约、服务、地址等资源不存在。
- `409`：时间冲突、状态冲突。
- `422`：服务模式不支持、超出距离、日期超范围等业务语义错误。

## TDD 实施顺序

必须按 RED、GREEN、重构推进。

建议顺序：

1. 写 `TimeRangeTest`，覆盖重叠、相邻、不合法时间段。
2. 写 `BookingDistanceCalculatorTest`，覆盖 0 公里、范围内、范围外。
3. 写 `BookingStateMachineTest`，覆盖允许和禁止状态流转。
4. 写 `BookingAvailabilityCalculatorTest`，覆盖排班扣减、不可用时间扣减、已有预约扣减、时间槽生成。
5. 写 `BookingApplicationServiceTest`，覆盖服务模式、上门地址、员工自动分配。
6. 写 `BookingTransactionServiceTest`，覆盖创建预约、状态日志、冲突检测。
7. 写 `BookingConcurrencyIT`，用真实 MySQL 验证并发重叠只有一个成功。
8. 写 `AdminBookingControllerTest`，覆盖 `401`、`403` 和权限码。
9. 写 `BookingControllerTest`，覆盖用户资源归属和取消规则。
10. 实现最小代码让测试变绿。
11. 重构并运行完整验证。

## 最低测试清单

单元测试必须覆盖：

- 时间段重叠判断。
- 首尾相接不冲突。
- 服务时长超过可用区间时不能生成时间槽。
- 午休、请假、临时不可用时间扣减。
- `PENDING_CONFIRM`、`CONFIRMED`、`IN_SERVICE` 占用时间。
- `CANCELLED`、`REJECTED`、`COMPLETED` 不占用时间。
- Haversine 距离计算。
- 上门服务超出半径被拒绝。
- 状态流转合法和非法路径。

集成测试必须覆盖：

- 创建预约写入 `service_booking`。
- 创建预约写入 `booking_status_log`。
- 系统自动分配员工，用户不能指定员工。
- 两个并发请求预约同一员工同一日期完全相同时间，只有一个成功。
- 两个并发请求时间部分重叠，只有一个成功。
- 首尾相接两个预约都成功。
- 同一员工不同日期互不阻塞。
- 不同员工同一日期同一时间都可成功。
- 取消预约后原时间段可再次预约。
- 管理员改派员工时与新预约并发不能产生冲突。
- 死锁或锁等待超时不会留下半成品预约或缺失状态日志。
- 后台无权限访问返回 `403`。
- 未认证访问后台预约接口返回 `401`。

并发锁测试必须使用真实 MySQL 8。H2 不能替代 MySQL 行锁和 InnoDB 行为验证。

## 测试数据库策略

普通快速测试：

```powershell
mvn test
```

真实 MySQL 并发门禁测试建议：

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/petcare_o2o?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的本地密码"
mvn "-Dtest=BookingConcurrencyIT" test
```

如果没有真实 MySQL，不能声称阶段 5 已完成。必须在交接报告中写明：

```text
未执行 MySQL 并发测试，原因：
已执行的替代测试：
风险：
用户需要执行的命令：
```

## 实施顺序

建议按以下顺序做：

1. 新增阶段 5 测试骨架，先让测试 RED。
2. 实现 `TimeRange`、距离计算、状态机等纯规则。
3. 实现可预约时间计算。
4. 实现服务目录查询接口。
5. 实现预约创建 Service，不先写复杂 Controller。
6. 实现 `staff_booking_lock` 自定义 Mapper SQL。
7. 实现事务锁定和冲突二次检查。
8. 实现死锁和锁等待有限重试。
9. 实现用户预约查询和取消。
10. 实现后台确认、拒绝、开始、完成、取消、改派。
11. 加上 `@PreAuthorize` 权限码。
12. 补齐并发 MySQL IT。
13. 运行完整验证。
14. 提交 Git。

## 验证命令

阶段完成前必须运行：

```powershell
mvn test
mvn clean package
mvn "-Dtest=BookingConcurrencyIT" test
git diff --check
git status --short --branch
```

如果 `BookingConcurrencyIT` 需要真实 MySQL 而本机未启动，必须明确记录未验证风险。

## Git 提交建议

阶段 5 代码建议拆成多个小提交：

```powershell
git add src/test/java/com/petcare/booking
git commit -m "test: add booking scheduling rule tests"

git add src/main/java/com/petcare/booking
git commit -m "feat: implement booking availability calculation"

git add src/main/java/com/petcare/booking src/test/java/com/petcare/booking
git commit -m "feat: add booking creation concurrency control"

git add src/main/java/com/petcare/booking src/test/java/com/petcare/booking
git commit -m "feat: add booking admin state transitions"
```

如果只提交计划文档，使用：

```powershell
git add AGENTS.md README.md docs/02-task-breakdown.md docs/03-glm5-implementation-plan.md docs/07-integration-gates.md docs/14-phase-5-booking-scheduling-plan.md
git commit -m "docs: add phase 5 booking scheduling plan"
```

提交前必须确认：

- 没有真实密钥。
- 没有真实微信 AppID/AppSecret。
- 没有 Redis、Flyway、Liquibase。
- 没有前端代码。
- 没有越界实现社区、商品订单、AI。
- 没有把用户身份从请求体 `userId` 传入生产创建预约接口。
- `git diff --check` 通过。

## 阶段 5 交接报告格式

完成后必须按以下格式汇报：

```text
任务：阶段 5 服务预约与排班
阶段：phase-5-booking-scheduling
分支：phase-5-booking-scheduling
提交：
已完成：
未完成：
变更文件：
验证命令：
验证结果：
覆盖率：
可预约时间计算验证：
预约并发验证：
上门距离验证：
状态流转验证：
后台权限验证：
MySQL 并发门禁：
已知风险：
待决策事项：
下一步允许执行的任务：阶段 6 社区与内容审核
```

## 阶段 5 退出标准

只有同时满足以下条件，才允许进入阶段 6：

- 服务目录查询可用。
- 可预约时间计算正确，不暴露员工选择权。
- 预约创建业务服务可用。
- 上门服务距离校验通过。
- 状态流转和状态日志通过测试。
- 后台确认、拒绝、取消、开始、完成、改派接口有权限保护。
- 同一员工同一日期重叠预约并发时只有一个成功。
- 取消和拒绝的预约不再占用时间。
- 待确认、已确认、服务中预约占用时间。
- 真实 MySQL 8 并发测试通过，或未验证风险已明确并由用户接受。
- `mvn test` 通过。
- `mvn clean package` 通过。
- `git diff --check` 通过。
- 已提交 Git。
