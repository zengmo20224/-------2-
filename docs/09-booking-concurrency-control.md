# 预约并发防冲突研究

日期：2026-06-08

状态：已批准采用

批准日期：2026-06-08

## 问题定义

系统必须保证同一员工在重叠时间段内只能存在一个有效预约。

占用排班的状态：

- `PENDING_CONFIRM`
- `CONFIRMED`
- `IN_SERVICE`

不占用排班的状态：

- `CANCELLED`
- `REJECTED`
- `COMPLETED`

需要防止的典型并发问题：

```text
事务 A 查询 10:00-11:00 无冲突
事务 B 查询 10:00-11:00 无冲突
事务 A 插入预约
事务 B 插入预约
最终同一员工产生两个重叠预约
```

普通唯一索引只能防止字段值完全重复，不能直接表达任意时间区间重叠，因此不能只依赖 `service_booking` 唯一索引。

## 方案比较

### 方案 A：只查询冲突后插入

结论：不可用。

原因：查询和插入之间存在竞争窗口，并发事务可能同时判断“无冲突”。

### 方案 B：仅使用预约范围查询加 `FOR UPDATE`

结论：不推荐单独使用。

原因：

- 当目标区间没有现有预约行时，缺少稳定且明确的锁定点。
- 正确性依赖隔离级别、索引范围和 InnoDB gap/next-key lock 行为。
- 后续索引或查询改动容易破坏并发正确性。

### 方案 C：锁定 `staff` 行后检查

结论：正确但粒度过粗。

优点：

- 实现简单。
- 同一员工的预约操作会串行执行。

缺点：

- 同一员工不同日期的预约也会互相阻塞。
- 员工资料修改可能与预约创建产生不必要竞争。

### 方案 D：Redis 分布式锁

结论：V1 不采用。

原因：

- V1 已决定不启用 Redis。
- Redis 锁不能替代数据库事务和数据库最终校验。
- 增加部署、续租、锁失效和故障处理复杂度。

### 方案 E：员工日期锁定点 + 数据库事务

结论：V1 已批准方案。

新增辅助表：

```text
staff_booking_lock
```

建议字段：

```text
id BIGINT PRIMARY KEY
staff_id BIGINT NOT NULL
booking_date DATE NOT NULL
create_time DATETIME NOT NULL
update_time DATETIME NOT NULL
UNIQUE KEY uk_staff_booking_date (staff_id, booking_date)
```

该表不保存业务结果，只为同一员工、同一日期提供稳定锁定点。

## 推荐事务流程

预约创建、管理员改派员工、修改预约时间都必须执行同一流程：

1. 校验请求参数、服务状态、员工技能、排班和上门距离。
2. 开启 Spring `@Transactional` 事务。
3. 使用 `INSERT ... ON DUPLICATE KEY UPDATE` 确保 `(staff_id, booking_date)` 锁定点存在。
4. 使用 `SELECT ... FOR UPDATE` 锁定该员工该日期的锁定点。
5. 查询有效预约是否与目标时间重叠。
6. 如果存在重叠，返回 `409 Conflict`，错误码 `booking_time_conflict`。
7. 如果无重叠，插入或更新预约，并写入状态日志。
8. 提交事务并释放锁。

区间重叠判断：

```sql
existing.start_time < :requested_end_time
AND existing.end_time > :requested_start_time
```

有效状态条件：

```sql
status IN ('PENDING_CONFIRM', 'CONFIRMED', 'IN_SERVICE')
AND deleted = 0
```

建议冲突查询索引：

```text
idx_booking_staff_date_status_time
(staff_id, booking_date, status, deleted, start_time, end_time)
```

## 事务要求

- 使用 InnoDB。
- Spring 事务放在具体 Service 类的公共方法上，避免同类内部调用导致 `@Transactional` 失效。
- 锁定点创建、锁定点查询、冲突检查、预约写入、状态日志必须在同一事务内。
- 默认使用 MySQL InnoDB `REPEATABLE READ`。
- 所有需要多个员工日期锁的操作必须按 `(staff_id, booking_date)` 升序加锁，降低死锁概率。
- 事务必须短小；锁持有期间不能调用 AI、文件服务或其他慢速外部接口。

## 死锁与超时处理

即使锁顺序一致，数据库事务仍可能发生死锁或锁等待超时。

要求：

- 捕获死锁和锁等待超时异常。
- 仅对整个预约事务进行有限重试，建议最多 2 次。
- 使用短暂随机退避。
- 重试仍失败时返回可重试的业务错误，不伪装成功。
- 日志记录 request id、staff id、booking date、时间段和重试次数，但不记录敏感信息。

## 必测并发场景

1. 两个并发请求预约同一员工、同一日期、完全相同时间，只有一个成功。
2. 两个并发请求时间部分重叠，只有一个成功。
3. 首尾相接但不重叠，例如 `10:00-11:00` 和 `11:00-12:00`，两个都成功。
4. 同一员工不同日期互不阻塞。
5. 不同员工同一日期同一时间都可成功。
6. 取消预约后原时间段可再次预约。
7. 管理员改派员工时与新预约并发，不能产生冲突。
8. 死锁或锁等待超时后，事务不会留下半成品预约或缺失状态日志。

## 不采用“时间槽占用表”的原因

另一种强约束方案是把预约拆成多个固定时间槽，并对 `(staff_id, date, slot_time)` 建唯一索引。

V1 暂不采用，原因：

- 服务时长不一定始终与时间槽完全整除。
- 修改 `time_slot_minutes` 会增加历史数据兼容复杂度。
- 预约取消、改期和改派需要维护额外占用记录。

如果未来高并发或强数据库唯一约束成为硬要求，可以重新评估。

## 研究依据

- MySQL InnoDB 默认隔离级别是 `REPEATABLE READ`，范围锁定读会使用 gap/next-key lock；`READ COMMITTED` 允许幻读，因此不能只依赖普通冲突查询。
  https://dev.mysql.com/doc/refman/8.0/en/innodb-transaction-isolation-levels.html
- `SELECT ... FOR UPDATE` 会对检查到的行加写锁，直到事务结束。
  https://dev.mysql.com/doc/refman/8.0/en/select.html
- InnoDB 锁在事务 `COMMIT` 或 `ROLLBACK` 时释放。
  https://dev.mysql.com/doc/refman/8.0/en/innodb-autocommit-commit-rollback.html
- Spring `@Transactional` 默认通过代理生效，同类内部调用可能导致事务注解被忽略。
  https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html

## 实施结论

用户已批准在 `schema.sql` 中新增 `staff_booking_lock` 表，并采用本文事务流程。

阶段 1 必须创建该表；阶段 5 必须实现事务锁定流程并完成本文列出的并发测试。
