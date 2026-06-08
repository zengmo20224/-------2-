# 阶段 3：核心实体、Mapper 与基础 CRUD Service 代码计划

日期：2026-06-08

负责人：GLM5.1

当前分支：`phase-3-entities-mappers`

前置条件：阶段 2 后端骨架已完成，提交为 `abe64bc feat: add spring boot backend skeleton`。

## 目标

本阶段把 `schema.sql` 落到 Java 代码层：

- 为所有表创建实体类。
- 为所有表创建 MyBatis-Plus Mapper。
- 为所有表创建基础 CRUD Service 接口与实现。
- 为状态字段创建统一常量或枚举。
- 增加 Mapper 集成测试，验证实体、Mapper、表名、字段映射和基础读写可用。

本阶段不实现业务流程，不写业务 Controller，不实现登录、预约、审核、订单、AI 调用。

## 必须先阅读

GLM5.1 开始编码前必须阅读：

1. `AGENTS.md`
2. `schema.sql`
3. `docs/01-architecture-design.md`
4. `docs/03-glm5-implementation-plan.md`
5. `docs/04-code-standards.md`
6. `docs/05-testing-and-verification.md`
7. `docs/07-integration-gates.md`
8. `docs/08-pending-decisions.md`
9. `docs/09-booking-concurrency-control.md`
10. `docs/10-admin-permission-design.md`
11. `docs/11-phase-2-backend-skeleton-brief.md`
12. 本文件

编码前必须先输出：

```text
已阅读文档：
当前阶段：阶段 3 核心实体与 Mapper
当前分支：phase-3-entities-mappers
计划修改范围：
不会修改的范围：
待决策或阻塞项：
验证计划：
```

## 阶段 3 允许做的事

允许新增：

- `entity` 包
- `mapper` 包
- `service` 包
- `service.impl` 包
- `constant` 或 `enums` 包
- MyBatis-Plus `@MapperScan`
- MyBatis-Plus 自动填充处理器
- Mapper 集成测试
- 测试数据构造工具

允许调整：

- `PetCareApplication` 增加 `@MapperScan("com.petcare.**.mapper")`
- `common/config` 增加 MyBatis-Plus 元数据自动填充配置
- `application-test.yml` 增加测试数据库或 H2 兼容配置

## 阶段 3 禁止做的事

本阶段禁止：

- 不写业务 Controller。
- 不实现管理员登录、JWT 签发、JWT 校验过滤器。
- 不实现服务预约创建和排班算法。
- 不实现内容敏感词审核算法。
- 不实现商品订单状态流转。
- 不实现 AI Provider 调用。
- 不引入 Redis。
- 不引入 Flyway 或 Liquibase。
- 不新增前端工程。
- 不修改 `schema.sql`，除非发现阶段 1 阻塞级错误并先征得用户确认。

## 包结构要求

每个模块按业务能力组织：

```text
src/main/java/com/petcare/
  user/
    entity/
    mapper/
    service/
    service/impl/
    constant/ 或 enums/
  store/
  service/
  staff/
  booking/
  community/
  moderation/
  product/
  marketing/
  ai/
  admin/
```

其中 `service` 模块名称与业务“服务项目”一致，保留当前规划命名；如果 Java 包名与常见 Service 层语义混淆，禁止自行改名，最多在文档中说明。

## 实体类要求

每张表一个实体类，必须与 `schema.sql` 严格对齐。

要求：

- 使用 `@TableName("table_name")`。
- 主键字段使用 `@TableId(value = "id", type = IdType.ASSIGN_ID)`。
- 逻辑删除字段使用 `@TableLogic`。
- 字段名和列名不一致时使用 `@TableField("column_name")`。
- 日期使用 `LocalDate`、`LocalTime`、`LocalDateTime`。
- 金额、距离、经纬度使用 `BigDecimal`。
- 主键和关联 id 使用 `Long`。
- 计数、排序、状态级别使用 `Integer`。
- `TINYINT` 逻辑布尔字段先使用 `Integer`，不要用 `Boolean`，避免 0/1/NULL 语义丢失。
- 不从 Controller 返回实体类；虽然本阶段不写 Controller，也要保持该约束。
- 不在实体类中写业务方法。

推荐使用 Lombok：

- 可以使用 `@Getter`、`@Setter`、`@NoArgsConstructor`。
- 不使用 `@Data`，避免意外生成不合适的 `equals`、`hashCode`、`toString`。
- 禁止在 `toString` 中输出密码、Token、API Key 等敏感字段。

## 必须创建的实体

按 `schema.sql` 创建以下实体：

### 用户与宠物

- `User`
- `Pet`
- `UserAddress`

### 门店与配置

- `Store`
- `StoreConfig`

### 服务与员工

- `ServiceCategory`
- `ServiceItem`
- `Staff`
- `StaffSkill`

### 排班与预约

- `StaffSchedule`
- `StaffUnavailableTime`
- `StaffBookingLock`
- `ServiceBooking`
- `BookingStatusLog`

### 社区与内容

- `Topic`
- `Post`
- `PostImage`
- `PostComment`
- `PostLike`
- `PostFavorite`
- `PostReport`

### 敏感词与审核

- `SensitiveWord`
- `ContentReviewRecord`

### 商品与订单

- `ProductCategory`
- `Product`
- `ProductImage`
- `CartItem`
- `ProductOrder`
- `ProductOrderItem`

### 营销活动

- `MarketingActivity`
- `ActivityProduct`
- `ActivityService`

### AI

- `AiConversation`
- `AiMessage`
- `AiUsageLog`
- `AiAnalysisReport`
- `FaqKnowledge`

### 后台管理

- `AdminUser`
- `AdminRole`
- `AdminPermission`
- `AdminRolePermission`
- `AdminOperationLog`

## Mapper 要求

每个实体创建一个 Mapper：

```java
public interface UserMapper extends BaseMapper<User> {
}
```

要求：

- Mapper 只继承 `BaseMapper<T>`。
- 本阶段不写复杂 SQL。
- 本阶段不写 XML Mapper，除非 MyBatis-Plus 无法覆盖必要的基础测试。
- 如果确实必须写 XML，先说明原因。
- Mapper 包必须被 `@MapperScan` 扫描。

## 基础 Service 要求

每个实体创建基础 Service：

```java
public interface UserService extends IService<User> {
}
```

实现：

```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
```

要求：

- 本阶段只提供基础 CRUD 能力。
- 不添加业务流程方法。
- 不写状态流转方法。
- 不在 Service 里调用其他模块 Service。
- 后续阶段在这些基础 Service 上扩展业务方法。

## 状态常量与枚举要求

必须为核心状态创建统一定义，避免魔法字符串散落。

建议包：

```text
common/constant/
```

或各业务模块：

```text
booking/enums/
product/enums/
community/enums/
```

必须覆盖：

- 用户状态：`ACTIVE`、`DISABLED`
- 门店状态：`OPEN`、`CLOSED`
- 服务项目状态：`ON_SALE`、`OFF_SALE`
- 员工状态：`ACTIVE`、`INACTIVE`
- 排班状态：`AVAILABLE`、`UNAVAILABLE`
- 预约状态：`PENDING_CONFIRM`、`CONFIRMED`、`IN_SERVICE`、`COMPLETED`、`CANCELLED`、`REJECTED`
- 支付状态：`UNPAID`、`OFFLINE_PAID`、`REFUNDED`
- 付款方式：`OFFLINE_STORE`、`OFFLINE_HOME`、`ONLINE_WECHAT`、`FREE`
- 内容状态：`PUBLISHED`、`PENDING_REVIEW`、`REJECTED`、`HIDDEN`、`DELETED`
- 商品订单状态：`PENDING_CONFIRM`、`PREPARING`、`READY_FOR_PICKUP`、`COMPLETED`、`CANCELLED`、`OUT_OF_STOCK`
- 自提状态：`WAIT_PREPARE`、`READY_FOR_PICKUP`、`PICKED_UP`
- 营销活动状态：`DRAFT`、`ACTIVE`、`ENDED`、`CANCELLED`
- 管理员角色：`SUPER_ADMIN`、`MANAGER`、`STAFF`
- AI 会话类型：`CUSTOMER_SERVICE`、`PET_CHAT`、`ADMIN_ANALYSIS`

要求：

- 状态定义必须与 `schema.sql` 默认值一致。
- 如果使用 enum，必须提供 `getCode()`。
- 不要在数据库中使用 enum 类型，数据库仍保持 `VARCHAR(32)`。

## 自动填充要求

如果实现 MyBatis-Plus 自动填充：

- `create_time` 插入时填充。
- `update_time` 插入和更新时填充。
- 不覆盖数据库默认值也可以，但代码侧行为要稳定。
- 逻辑删除仍通过 `@TableLogic` 和 MyBatis-Plus 配置处理。

不得自动填充：

- `confirm_time`
- `complete_time`
- `cancel_time`
- `review_time`
- `handle_time`
- 这些业务时间必须由后续业务流程显式设置。

## 测试要求

必须采用 TDD 思路：先写 Mapper 集成测试，确认实体与 Mapper 缺失时测试失败，再补实现。

最低测试要求：

1. Spring Context 能加载所有 Mapper。
2. 每个模块至少一个代表性 Mapper 完成插入和查询。
3. 所有实体的 `@TableName` 覆盖 `schema.sql` 中全部表。
4. `@TableId` 使用 `ASSIGN_ID`。
5. 有 `deleted` 字段的实体使用 `@TableLogic`。
6. 状态常量默认值与 `schema.sql` 默认值一致。
7. `staff_booking_lock` 的 `(staff_id, booking_date)` 唯一约束通过测试验证。
8. `admin_role_permission` 的 `(role_id, permission_id)` 唯一约束通过测试验证。

推荐新增测试类：

```text
src/test/java/com/petcare/common/persistence/EntityMappingTest.java
src/test/java/com/petcare/common/persistence/MapperSmokeTest.java
src/test/java/com/petcare/booking/mapper/StaffBookingLockMapperTest.java
src/test/java/com/petcare/admin/mapper/AdminRolePermissionMapperTest.java
src/test/java/com/petcare/common/constant/StatusConstantTest.java
```

## 测试数据库策略

优先方案：

- 使用本地 MySQL 8 测试库，执行 `schema.sql` 后运行 Mapper 集成测试。

可接受方案：

- 如果本地环境暂时无法连接 MySQL，可先使用 H2 运行非数据库映射测试。
- 但阶段 3 不能只靠 H2 宣称完成；最终必须在 MySQL 8 上验证 Mapper 基础读写。

不要引入：

- Flyway
- Liquibase
- Redis

## MySQL 验证建议

如果本地已有 MySQL：

```powershell
Get-Content -Raw .\schema.sql | mysql -uroot -p
mvn test
```

如果使用 Docker：

```powershell
$env:PETCARE_MYSQL_ROOT_PASSWORD = "replace-with-local-test-password"
docker run --name petcare-mysql -e "MYSQL_ROOT_PASSWORD=$env:PETCARE_MYSQL_ROOT_PASSWORD" -e MYSQL_DATABASE=petcare_o2o -p 3306:3306 -d mysql:8.0
Get-Content -Raw .\schema.sql | docker exec -i petcare-mysql mysql -uroot "-p$env:PETCARE_MYSQL_ROOT_PASSWORD" petcare_o2o
mvn test
```

测试 profile 通过环境变量连接：

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/petcare_o2o?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的本地密码"
mvn test
```

## 实施顺序

建议按以下顺序做：

1. 添加 `@MapperScan`。
2. 设计实体基类或公共字段策略。
3. 先写 2 到 3 个代表性 Mapper 测试，例如 `UserMapper`、`ServiceBookingMapper`、`AdminRolePermissionMapper`。
4. 创建对应实体和 Mapper，让测试变绿。
5. 扩展到全部实体和 Mapper。
6. 添加基础 Service 和 ServiceImpl。
7. 添加状态常量或枚举。
8. 补齐映射覆盖测试和唯一约束测试。
9. 运行完整验证命令。
10. 提交 Git。

## 实体基类建议

可以创建：

```text
common/entity/BaseEntity.java
```

包含：

- `id`
- `createTime`
- `updateTime`
- `deleted`

但不是所有表都有 `update_time`、`deleted`，因此不要强行让所有实体继承同一个基类。

建议：

- 对包含 `id/create_time/update_time/deleted` 的常规业务表使用 `BaseEntity`。
- 对关系表、日志表、消息表等字段不同的表单独声明字段。

如果基类导致映射不清晰，优先放弃基类，逐表显式声明字段。

## 阶段 3 验证命令

阶段完成前必须运行：

```powershell
mvn test
mvn clean package
git diff --check
git status --short --branch
```

如果测试需要真实 MySQL 但本机不可用，必须明确说明：

```text
未执行 MySQL Mapper 集成测试，原因：
已执行的替代测试：
用户需要执行的命令：
风险：
```

## Git 提交要求

建议提交：

```powershell
git add src pom.xml docs/12-phase-3-entities-mappers-plan.md
git commit -m "feat: add entities mappers and base services"
```

提交前必须确认：

- 没有修改 `schema.sql`，除非用户批准。
- 没有真实数据库密码或密钥。
- 没有 Redis、Flyway、Liquibase。
- 没有业务 Controller。
- 没有越界实现认证、预约、订单、AI。

## 阶段 3 交接报告格式

完成后必须按以下格式汇报：

```text
任务：阶段 3 核心实体与 Mapper
阶段：phase-3-entities-mappers
分支：phase-3-entities-mappers
提交：
已完成：
未完成：
变更文件：
验证命令：
验证结果：
覆盖率：
MySQL Mapper 验证：
已知风险：
待决策事项：
下一步允许执行的任务：阶段 4 认证与授权
```

## 阶段 3 退出标准

只有同时满足以下条件，才允许进入阶段 4：

- 所有 `schema.sql` 表都有实体类。
- 所有实体都有 Mapper。
- 基础 CRUD Service 和实现存在。
- 状态常量或枚举与 `schema.sql` 默认值一致。
- Mapper 集成测试在 MySQL 8 上通过，或明确记录未验证风险并由用户接受。
- `mvn test` 通过。
- `mvn clean package` 通过。
- `git diff --check` 通过。
- 已提交 Git。
