# 阶段 7：商品到店自提订单代码计划

日期：2026-06-09

负责人：GLM5.1

当前分支：`phase-7-product-orders`

前置条件：

- 阶段 6 社区与内容审核已形成代码提交，最新阶段 6 提交为 `ff61e9d feat: add community post, interaction and admin moderation APIs`。
- 商品、分类、图片、购物车、订单、订单项实体、Mapper 和基础 Service 已存在。
- 后台 Spring Security + JWT 和商品订单权限码已存在。

## 目标

本阶段实现 V1 商品展示与到店自提订单闭环：

- 商品分类、商品列表和商品详情查询。
- 购物车新增、修改数量、选中、删除和查询。
- 从已选购物车项创建到店自提订单。
- 服务端重新读取商品价格并计算订单金额。
- 创建订单时原子预占库存，防止并发超卖。
- 保存商品名称、封面、单价和小计的订单项快照。
- 用户查询自己的订单和取消待确认订单。
- 后台确认订单、备货完成、确认线下收款、完成和取消订单。
- 后台缺货取消。
- 使用细粒度权限码保护后台订单操作。

本阶段不实现微信支付、在线支付、配送、优惠券、积分、复杂库存仓储、前端和商品图片上传。

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
13. `docs/10-admin-permission-design.md`
14. `docs/15-phase-6-community-moderation-plan.md`
15. 本文件

编码前必须先运行：

```powershell
git status --short --branch
git log --oneline -5
```

然后先输出：

```text
已阅读文档：
当前阶段：阶段 7 商品到店自提订单
当前分支：phase-7-product-orders
计划修改范围：
不会修改的范围：
待决策或阻塞项：
验证计划：
```

没有完成以上汇报前，不允许修改项目文件。

## 已决定事项

必须按以下项目决策执行：

- V1 商品订单只支持到店自提。
- V1 不接微信支付或任何在线支付。
- 付款方式使用 `OFFLINE_STORE`。
- 支付状态初始为 `UNPAID`，由后台确认线下收款后变为 `OFFLINE_PAID`。
- V1 不启用 Redis。
- 当前仅维护 `schema.sql`，不引入 Flyway 或 Liquibase。
- 用户真实微信登录仍为占位，不能伪造生产用户身份。
- 后台商品订单操作使用 `docs/10-admin-permission-design.md` 中的权限码。

## 阶段 7 库存策略

阶段 7 使用数据库事务和原子库存更新，不使用 Redis 锁。

库存生命周期：

1. 用户创建订单时原子扣减库存，库存立即被订单预占。
2. 普通取消订单时恢复已预占库存。
3. 订单完成时不再扣库存，只增加 `sales_count`。
4. `OUT_OF_STOCK` 表示门店发现实际库存与系统库存不一致，不自动恢复库存。
5. `OUT_OF_STOCK` 后由拥有 `product:stock:update` 权限的管理员单独修正库存。

该策略避免“确认订单前库存被其他订单抢走”，并避免缺货取消后把不存在的实物重新加回系统库存。

## 阶段 7 允许做的事

允许新增：

- `product/controller`
- `product/dto`
- `product/domain`
- `product/service` 中的购物车、下单、库存、订单状态业务方法
- `product/mapper` 的原子库存更新、订单行锁等自定义 SQL
- 商品、购物车、订单、库存并发和后台权限测试

允许调整：

- `ProductMapper` 增加原子扣减库存、恢复库存、增加销量方法。
- `CartItemMapper` 增加购物车 upsert 和用户购物车查询。
- `ProductOrderMapper` 增加订单 `SELECT ... FOR UPDATE` 和分页查询。
- `ProductOrderItemMapper` 增加订单项批量查询。
- `ErrorCode` 增加商品、购物车、订单、库存相关错误码。
- `GlobalExceptionHandler` 增加库存不足、重复购物车、状态冲突的安全响应映射。

## 阶段 7 禁止做的事

本阶段禁止：

- 不实现微信支付、二维码支付或其他在线支付。
- 不把 `ONLINE_WECHAT` 作为可选付款方式开放。
- 不实现配送、物流地址或快递单。
- 不实现优惠券、积分、会员价、满减或营销活动计价。
- 不实现复杂仓库、批次、保质期或供应链库存。
- 不引入 Redis 分布式锁。
- 不修改 `schema.sql`，除非发现阻塞级错误并先征得用户确认。
- 不接受客户端传入的商品单价、订单总金额作为可信数据。
- 不通过请求体 `userId` 绕过用户认证。
- 不在事务中调用外部服务、AI、文件服务或其他慢操作。
- 不实现商品图片上传。

## 用户身份边界

当前真实用户登录仍未启用。

要求：

- 用户侧业务 Service 设计为 `addCartItem(currentUserId, request)`、`createOrder(currentUserId, request)` 等形式。
- Controller 不能从请求体读取 `userId`。
- 如果生产环境没有当前用户身份，用户侧购物车和订单写接口返回 `401`。
- 测试可以通过测试专用安全上下文或直接调用 Service 传入 `currentUserId`。
- 不允许新增测试登录、模拟登录或后门用户 ID Header。

## 推荐包结构

```text
src/main/java/com/petcare/product/
  controller/
    ProductCatalogController.java
    CartController.java
    ProductOrderController.java
    AdminProductOrderController.java
  dto/
    ProductCategoryResponse.java
    ProductSummaryResponse.java
    ProductDetailResponse.java
    CartItemCreateRequest.java
    CartItemUpdateRequest.java
    CartItemResponse.java
    ProductOrderCreateRequest.java
    ProductOrderResponse.java
    ProductOrderDetailResponse.java
    AdminOrderActionRequest.java
  domain/
    ProductOrderAmountCalculator.java
    ProductOrderStateMachine.java
    ProductOrderLineSnapshot.java
  service/
    ProductCatalogApplicationService.java
    CartApplicationService.java
    ProductOrderApplicationService.java
    ProductOrderTransactionService.java
    AdminProductOrderService.java
  service/impl/
    ...
```

说明：

- `domain` 只放纯规则，不直接访问数据库。
- `ProductOrderTransactionService` 负责创建订单、扣库存、保存订单项和清理购物车的单次事务。
- 状态流转事务方法不能依赖同类内部调用。
- Controller 不能直接调用 Mapper。

## 商品目录 API

```text
GET /api/v1/product-categories
GET /api/v1/products
GET /api/v1/products/{id}
```

查询要求：

- 分类只返回 `status = ACTIVE` 且 `deleted = 0`。
- 商品只返回 `status = ON_SALE` 且 `deleted = 0`。
- 商品列表支持 `categoryId`、分页和排序。
- 商品详情可以返回已有 `product_image` URL，但本阶段不提供上传能力。
- 商品响应可以显示当前库存，但不能把库存值当成下单成功承诺。

## 购物车 API

```text
GET /api/v1/cart-items
POST /api/v1/cart-items
PATCH /api/v1/cart-items/{id}
DELETE /api/v1/cart-items/{id}
POST /api/v1/cart-items/check
```

新增购物车请求：

```json
{
  "productId": 1001,
  "quantity": 2
}
```

规则：

- 当前用户身份来自安全上下文。
- 商品必须存在、上架且未删除。
- 数量必须为正整数，建议限制为 `1` 到 `99`。
- 同一用户同一商品只允许一行购物车记录。
- 重复添加同一商品时增加数量，不新增重复行。
- 依赖 `uk_user_product(user_id, product_id)` 防止并发重复行。
- 更新和删除购物车项前必须校验归属当前用户。
- 购物车展示价格是当前商品价格，不是订单成交快照。
- 购物车库存不足可以提示，但最终库存校验必须在创建订单事务中完成。

购物车并发建议：

- 优先使用数据库 upsert 或捕获唯一约束冲突后重试查询更新。
- 不允许“先查询不存在，再插入”作为唯一防重措施。

## 创建订单 API

```text
POST /api/v1/product-orders
```

请求示例：

```json
{
  "storeId": 1,
  "contactName": "张三",
  "contactPhone": "13800000000",
  "remark": "周六下午自提"
}
```

创建规则：

- 当前用户身份来自安全上下文，不来自请求体。
- 从当前用户 `checked = 1` 的购物车项创建订单。
- 请求体不能包含可信商品价格、订单金额或用户 ID。
- 至少存在一个已选购物车项。
- 重新读取每个商品当前状态、价格和库存。
- 只允许 `status = ON_SALE`、`deleted = 0`、`pickup_only = 1` 的商品。
- 按商品 ID 升序处理库存，降低死锁概率。
- 原子扣减每个商品库存。
- 任意商品库存不足时整个事务回滚。
- 创建订单主表和订单项快照。
- 成功后删除当前用户已结算的购物车项。
- 返回 `201 Created`。

订单默认值：

```text
status = PENDING_CONFIRM
payment_method = OFFLINE_STORE
payment_status = UNPAID
pickup_status = WAIT_PREPARE
```

## 订单金额和快照规则

客户端传入的价格和金额一律不可信。

服务端计算：

```text
订单项小计 = 商品当前价格 × 购买数量
订单总金额 = 所有订单项小计之和
```

要求：

- 使用 `BigDecimal`。
- 金额保留 2 位小数。
- 不使用 `double` 或 `float`。
- 订单项必须保存：
  - `product_id`
  - `product_name`
  - `product_cover_url`
  - `price`
  - `quantity`
  - `total_amount`
- 后续商品名称、封面或价格变化不能改变历史订单快照。
- `product_order.total_amount` 必须等于订单项小计总和。

## 库存原子更新要求

`ProductMapper` 建议新增：

```java
int deductStock(@Param("productId") Long productId, @Param("quantity") int quantity);

int restoreStock(@Param("productId") Long productId, @Param("quantity") int quantity);

int increaseSalesCount(@Param("productId") Long productId, @Param("quantity") int quantity);
```

原子扣库存 SQL 语义：

```sql
UPDATE product
SET stock = stock - :quantity
WHERE id = :productId
  AND stock >= :quantity
  AND status = 'ON_SALE'
  AND deleted = 0;
```

要求：

- 更新行数为 `1` 才表示扣减成功。
- 更新行数为 `0` 返回 `409`，错误码 `product_stock_insufficient`。
- 多商品订单按 `product_id` 升序扣减。
- 任意一项失败必须回滚订单、订单项和之前已扣库存。
- 库存不能出现负数。
- H2 测试不能替代真实 MySQL 并发超卖测试。

## 用户订单 API

```text
GET /api/v1/product-orders/my
GET /api/v1/product-orders/{id}
POST /api/v1/product-orders/{id}/cancel
```

要求：

- 用户只能访问自己的订单。
- 用户只能取消 `PENDING_CONFIRM` 订单。
- 取消操作必须锁定订单行并再次检查状态。
- 取消成功后恢复已预占库存。
- 重复取消不能重复恢复库存。
- 用户不能修改订单金额、商品快照、付款状态或自提状态。

## 后台订单 API

```text
GET /api/v1/admin/product-orders
GET /api/v1/admin/product-orders/{id}
POST /api/v1/admin/product-orders/{id}/confirm
POST /api/v1/admin/product-orders/{id}/ready
POST /api/v1/admin/product-orders/{id}/confirm-payment
POST /api/v1/admin/product-orders/{id}/complete
POST /api/v1/admin/product-orders/{id}/cancel
POST /api/v1/admin/product-orders/{id}/out-of-stock
```

权限要求：

```text
GET 列表和详情                    product:order:read
POST /confirm                    product:order:confirm
POST /ready                      product:order:ready
POST /confirm-payment            product:order:confirm-payment
POST /complete                   product:order:complete
POST /cancel                     product:order:cancel
POST /out-of-stock               product:order:cancel
```

说明：

- `product:order:prepare` 为后续更细备货动作预留；V1 确认订单后直接进入 `PREPARING`。
- `STAFF` 默认不能确认收款，除非明确授予 `product:order:confirm-payment`。
- 确认收款、取消、缺货取消、完成订单必须写入 `admin_operation_log`。

## 订单状态流转

允许流转：

```text
null -> PENDING_CONFIRM
PENDING_CONFIRM -> PREPARING
PENDING_CONFIRM -> CANCELLED
PENDING_CONFIRM -> OUT_OF_STOCK
PREPARING -> READY_FOR_PICKUP
PREPARING -> CANCELLED
READY_FOR_PICKUP -> CANCELLED
READY_FOR_PICKUP -> COMPLETED
```

字段联动：

- 确认订单：
  - `PENDING_CONFIRM -> PREPARING`
  - 设置 `confirm_time`
- 备货完成：
  - `PREPARING -> READY_FOR_PICKUP`
  - `pickup_status = READY_FOR_PICKUP`
- 确认线下收款：
  - 只允许 `READY_FOR_PICKUP`
  - `payment_status = OFFLINE_PAID`
  - `pickup_status = PICKED_UP`
- 完成订单：
  - 只允许 `READY_FOR_PICKUP`
  - 必须 `payment_status = OFFLINE_PAID`
  - 必须 `pickup_status = PICKED_UP`
  - `status = COMPLETED`
  - 设置 `complete_time`
  - 增加商品 `sales_count`
- 普通取消：
  - 设置 `status = CANCELLED`
  - 设置 `cancel_time`
  - 恢复库存
- 缺货取消：
  - `PENDING_CONFIRM -> OUT_OF_STOCK`
  - 设置 `cancel_time`
  - 不自动恢复库存

禁止：

- 已完成、已取消、缺货取消订单继续流转。
- 未备货完成前确认收款。
- 未确认收款和未自提时完成订单。
- 重复确认收款。
- 重复完成订单并重复增加销量。
- 已支付或已自提订单普通取消。

## 订单并发和事务要求

创建订单事务必须包含：

1. 加载当前用户已选购物车项。
2. 按商品 ID 升序加载商品并计算金额。
3. 原子扣减所有商品库存。
4. 插入 `product_order`。
5. 插入全部 `product_order_item`。
6. 删除已结算购物车项。
7. 提交事务。

状态流转事务必须：

1. 使用 `SELECT ... FOR UPDATE` 锁定订单行。
2. 再次校验当前状态、支付状态和自提状态。
3. 执行库存恢复、销量增加或字段更新。
4. 写入管理员操作日志。
5. 提交事务。

要求：

- 所有状态流转使用同一 `ProductOrderStateMachine` 或等效规则。
- 事务保持短小。
- 不使用 `SKIP LOCKED`。
- 不在事务中调用外部接口。
- 订单取消和完成并发时只能有一个成功。
- 两个并发订单竞争最后库存时，最多一个成功。

## 错误码建议

在 `ErrorCode` 中新增：

```text
product_not_found
product_not_on_sale
product_not_pickup_only
product_stock_insufficient
cart_item_not_found
cart_item_forbidden
cart_empty
cart_no_checked_items
product_order_not_found
product_order_forbidden
product_order_status_invalid
product_order_payment_required
product_order_pickup_required
product_order_amount_invalid
```

HTTP 状态建议：

- `400`：参数格式错误。
- `401`：用户或管理员未认证。
- `403`：无权限或资源不属于当前用户。
- `404`：商品、购物车项或订单不存在。
- `409`：库存不足、订单状态冲突、重复状态操作。
- `422`：购物车为空、商品不可售、非自提商品、未付款完成订单。

## TDD 实施顺序

必须按 RED、GREEN、重构推进。

建议顺序：

1. 写 `ProductOrderAmountCalculatorTest`，覆盖单商品、多商品、金额精度和非法数量。
2. 写 `ProductOrderStateMachineTest`，覆盖全部允许和禁止状态流转。
3. 写 `CartApplicationServiceTest`，覆盖重复添加、数量更新、归属校验。
4. 写 `ProductOrderTransactionServiceTest`，覆盖订单、订单项快照、金额和购物车清理。
5. 写 `ProductInventoryConcurrencyIT`，使用真实 MySQL 验证最后库存只能被一个订单获取。
6. 写 `ProductOrderCancellationTest`，覆盖取消恢复库存和重复取消不重复恢复。
7. 写 `AdminProductOrderServiceTest`，覆盖确认、备货、收款、完成、缺货取消。
8. 写 `ProductOrderControllerTest`，覆盖用户资源归属和 `401`。
9. 写 `AdminProductOrderControllerTest`，覆盖后台 `401`、`403` 和权限码。
10. 实现最小代码让测试变绿。
11. 重构并运行完整验证。

## 最低测试清单

单元测试必须覆盖：

- 订单项小计计算。
- 订单总金额计算。
- 金额使用 `BigDecimal` 且保留 2 位。
- 客户端价格和总金额不参与可信计算。
- 全部允许和禁止状态流转。
- 完成订单必须已付款且已自提。
- `OUT_OF_STOCK` 不自动恢复库存。

集成测试必须覆盖：

- 重复添加购物车不会产生重复行。
- 从已选购物车项创建订单。
- 创建订单写入订单项价格快照。
- 创建订单后清理已结算购物车项。
- 库存不足时整个订单事务回滚。
- 多商品中任一库存不足时所有库存扣减回滚。
- 两个并发订单竞争最后一件库存时只有一个成功。
- 库存永不为负。
- 普通取消恢复库存。
- 重复取消不重复恢复库存。
- 完成订单增加销量且不能重复增加。
- 用户不能访问或取消其他用户订单。
- 后台缺少权限返回 `403`。
- 未认证访问后台订单接口返回 `401`。

## 测试数据库策略

普通快速测试：

```powershell
mvn test
```

真实 MySQL 库存并发门禁测试：

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/petcare_o2o?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的本地密码"
mvn "-Dtest=ProductInventoryConcurrencyIT" test
```

如果没有真实 MySQL，不能声称库存并发门禁通过。必须在交接报告中明确记录：

```text
未执行 MySQL 库存并发测试，原因：
已执行的替代测试：
风险：
用户需要执行的命令：
```

## 实施顺序

建议按以下顺序做：

1. 新增阶段 7 测试骨架，先让测试 RED。
2. 实现订单金额计算和状态机纯规则。
3. 实现商品目录查询。
4. 实现购物车业务和唯一约束防重复。
5. 实现订单创建事务和订单项快照。
6. 实现原子库存扣减和恢复。
7. 实现用户订单查询和取消。
8. 实现后台确认、备货完成、确认收款、完成、取消和缺货取消。
9. 加上 `@PreAuthorize` 权限码。
10. 补齐真实 MySQL 库存并发测试。
11. 运行完整验证。
12. 提交 Git。

## 验证命令

阶段完成前必须运行：

```powershell
mvn test
mvn clean package
mvn "-Dtest=ProductInventoryConcurrencyIT" test
git diff --check
git status --short --branch
```

## Git 提交建议

阶段 7 代码建议拆成多个小提交：

```powershell
git add src/test/java/com/petcare/product
git commit -m "test: add product order domain tests"

git add src/main/java/com/petcare/product src/test/java/com/petcare/product
git commit -m "feat: add product catalog and cart APIs"

git add src/main/java/com/petcare/product src/test/java/com/petcare/product
git commit -m "feat: add transactional pickup order creation"

git add src/main/java/com/petcare/product src/test/java/com/petcare/product
git commit -m "feat: add admin pickup order workflow"
```

如果只提交计划文档，使用：

```powershell
git add AGENTS.md README.md docs/02-task-breakdown.md docs/03-glm5-implementation-plan.md docs/07-integration-gates.md docs/16-phase-7-product-orders-plan.md
git commit -m "docs: add phase 7 product orders plan"
```

提交前必须确认：

- 没有在线支付实现。
- 没有 Redis、Flyway、Liquibase。
- 没有前端代码。
- 没有商品图片上传。
- 没有优惠券、积分、会员价或配送。
- 没有信任客户端金额或价格。
- 没有通过请求体 `userId` 绕过用户认证。
- `git diff --check` 通过。

## 阶段 7 交接报告格式

完成后必须按以下格式汇报：

```text
任务：阶段 7 商品到店自提订单
阶段：phase-7-product-orders
分支：phase-7-product-orders
提交：
已完成：
未完成：
变更文件：
验证命令：
验证结果：
覆盖率：
订单金额验证：
库存事务验证：
MySQL 库存并发门禁：
订单状态流转验证：
后台权限验证：
已知风险：
待决策事项：
下一步允许执行的任务：阶段 8 AI Provider 与 AI 功能
```

## 阶段 7 退出标准

只有同时满足以下条件，才允许进入阶段 8：

- 商品分类、商品列表和详情查询可用。
- 购物车新增、更新、选中、删除和查询可用。
- 重复添加购物车不会产生重复行。
- 创建订单只使用服务端商品价格和金额计算。
- 订单项保存商品快照。
- 库存扣减、订单、订单项和购物车清理在同一事务。
- 两个并发订单竞争最后库存时只有一个成功。
- 库存不会变为负数。
- 普通取消恢复库存，重复取消不会重复恢复。
- 订单状态流转通过测试。
- 完成订单前必须已付款且已自提。
- 后台订单操作使用权限码保护。
- 没有实现在线支付、配送或复杂营销计价。
- 真实 MySQL 8 库存并发测试通过，或未验证风险已明确并由用户接受。
- `mvn test` 通过。
- `mvn clean package` 通过。
- `git diff --check` 通过。
- 已提交 Git。
