# 阶段 11-04：GLM5.1 用户地址 API 实施任务书

日期：2026-06-13

状态：已规划，当前锁定

解锁条件：`docs/38-phase-11-03-review-and-remediation-plan.md` 全部门禁通过

执行对象：GLM5.1

目标分支：`phase-11-user-prerequisites`

## 1. 任务目的

实现当前登录用户的地址创建、列表、修改和逻辑删除能力，为后续上门预约选择地址和距离校验提供可信数据。

本任务只负责地址资料管理，不在地址保存时计算门店距离，不修改预约服务半径规则。

## 2. 前置门禁

开始编码前必须确认：

- 11-03R 全部 HIGH/MEDIUM Findings 已关闭。
- `mvn test` 通过。
- `mvn -Ptc-mysql clean test` 通过。
- 当前分支为 `phase-11-user-prerequisites`。
- 工作区除已知 `.claude/` 外无不明修改。

任一条件不满足时停止，不得开始 11-04。

## 3. 真实 Schema 契约

必须使用现有 `schema.sql` 和 `UserAddress` 实体：

| 字段 | 类型 | API 规则 |
|---|---|---|
| `id` | BIGINT | 雪花 ID，对外使用字符串 |
| `user_id` | BIGINT | 只来自当前 USER 身份 |
| `contact_name` | VARCHAR(64) | API 必填，保存前 trim |
| `contact_phone` | VARCHAR(20) | API 必填，保存前 trim |
| `province` | VARCHAR(64) | API 必填，保存前 trim |
| `city` | VARCHAR(64) | API 必填，保存前 trim |
| `district` | VARCHAR(64) | 可选，空白转 `null` |
| `detail_address` | VARCHAR(255) | API 必填，保存前 trim |
| `longitude` | DECIMAL(10,6) | 可选，使用 `BigDecimal` |
| `latitude` | DECIMAL(10,6) | 可选，使用 `BigDecimal` |
| `is_default` | TINYINT | 仅允许 `0` 或 `1` |
| `deleted` | TINYINT | MyBatis-Plus 逻辑删除 |

禁止实现旧计划中的 `label` 字段。禁止使用 `Double` 保存或传输坐标。

## 4. API 契约

所有端点必须明确要求 `ROLE_USER`：

```http
GET    /api/v1/user/addresses
POST   /api/v1/user/addresses
PUT    /api/v1/user/addresses/{addressId}
DELETE /api/v1/user/addresses/{addressId}
```

本阶段不新增地址详情端点；上门预约通过列表选择地址。

### 4.1 创建与修改请求

```json
{
  "contactName": "张三",
  "contactPhone": "13800138000",
  "province": "广东省",
  "city": "深圳市",
  "district": "南山区",
  "detailAddress": "科技园 1 号",
  "longitude": 113.934528,
  "latitude": 22.540503,
  "isDefault": true
}
```

请求中禁止出现或生效：

- `id`、`addressId`。
- `userId`。
- `deleted`、`createTime`、`updateTime`。

### 4.2 响应

```json
{
  "addressId": "2065606349801717762",
  "contactName": "张三",
  "contactPhone": "13800138000",
  "province": "广东省",
  "city": "深圳市",
  "district": "南山区",
  "detailAddress": "科技园 1 号",
  "longitude": 113.934528,
  "latitude": 22.540503,
  "isDefault": true,
  "createdAt": "2026-06-13T09:00:00",
  "updatedAt": "2026-06-13T09:00:00"
}
```

强制要求：

- `addressId` 为字符串。
- 不返回 `userId` 和 `deleted`。
- 列表仅返回当前用户未删除地址。
- 列表按 `isDefault DESC`、`createTime DESC`、`id DESC` 稳定排序。
- 修改或删除他人地址统一返回 404，避免泄露资源存在性。

## 5. 默认地址不变量与并发方案

业务不变量：

```text
用户没有地址时：允许没有默认地址
用户存在地址时：必须且只能有一个默认地址
```

具体规则：

1. 创建第一条地址时，无论请求是否指定默认，都将其设为默认。
2. 创建或修改地址并设置 `isDefault=true` 时，取消该用户其他地址的默认状态。
3. 修改当前默认地址并传 `isDefault=false` 时，仍保持其为默认，避免产生无默认地址状态。
4. 删除非默认地址时，不改变默认地址。
5. 删除默认地址时，将剩余地址中 `createTime DESC、id DESC` 第一条设为默认；没有剩余地址时结束。

并发要求：

- 创建、修改、删除必须在事务内执行。
- 任何可能改变默认地址的写操作，先锁定当前用户行，再读取和修改地址。
- 使用现有 `UserService` 在事务内执行固定条件的 `SELECT ... FOR UPDATE`，按 `currentUserId + ACTIVE` 锁定同一用户的默认地址变更。
- 禁止依赖 Redis、JVM 本地锁或请求顺序。
- 更新默认状态必须限定 `userId=currentUserId`，不得影响其他用户。
- 任一写步骤返回 `false` 时必须回滚，不得伪造成功。

本方案不修改 Schema；同一用户的默认地址写操作通过用户行锁串行化。

## 6. 授权与归属

- 当前用户 ID 只能来自 `SecurityContextHelper.getCurrentUserId()`。
- Controller 和应用服务不得接受请求中的 `userId` 作为归属依据。
- 查询、更新和删除条件必须包含 `addressId + currentUserId`。
- ADMIN Token 访问地址端点返回 403。
- 无 Token、无效 Token、禁用或删除用户 Token 返回 401。
- 用户 A 修改或删除用户 B 地址返回 404，数据库不发生变化。
- 逻辑删除后，列表、修改和重复删除均视为不存在。

## 7. DTO 与输入校验

建议新增：

```text
AddressUpsertRequest
AddressResponse
```

校验规则：

```text
contactName:    @NotBlank, @Size(max=64)
contactPhone:   @NotBlank, @Size(max=20)
province:       @NotBlank, @Size(max=64)
city:           @NotBlank, @Size(max=64)
district:       @Size(max=64)，可选
detailAddress:  @NotBlank, @Size(max=255)
longitude:      -180..180，最多 6 位小数
latitude:       -90..90，最多 6 位小数
isDefault:      Boolean，可选
```

补充规则：

- 经纬度必须同时存在或同时为空。
- 地址管理只校验坐标格式和范围，不计算门店距离。
- 必填字符串保存前 trim，trim 后为空返回 400 `validation_error`。
- 可选字符串空白统一转 `null`。
- 坐标使用 `BigDecimal`，禁止使用 `Double`。
- 所有文本按普通文本保存，不执行 HTML。
- 服务层直接调用也必须得到稳定的 `validation_error`，不得依赖 Controller 校验避免 NPE。

## 8. 服务设计

建议新增应用服务：

```java
List<AddressResponse> listCurrentUserAddresses(Long currentUserId);
AddressResponse createCurrentUserAddress(Long currentUserId, AddressUpsertRequest request);
AddressResponse updateCurrentUserAddress(Long currentUserId, Long addressId, AddressUpsertRequest request);
void deleteCurrentUserAddress(Long currentUserId, Long addressId);
```

要求：

- Controller 只做协议适配、身份提取和响应包装。
- 应用服务通过 `UserAddressService` 和 `UserService` 操作，不直接调用 Mapper。
- 创建显式设置 `userId=currentUserId`。
- 更新使用显式字段白名单，禁止请求整体覆盖实体。
- 所有地址查询和写条件包含 `currentUserId`。
- 所有写操作检查返回值。
- 多步默认地址变更必须在同一事务中完成并回滚。

## 9. 允许修改范围

```text
src/main/java/com/petcare/user/controller/AddressController.java
src/main/java/com/petcare/user/dto/ 地址相关 DTO
src/main/java/com/petcare/user/service/AddressApplicationService.java
src/main/java/com/petcare/user/service/impl/AddressApplicationServiceImpl.java
src/test/java/com/petcare/user/controller/AddressControllerTest.java
src/test/java/com/petcare/user/service/AddressApplicationServiceImplTest.java
src/test/java/com/petcare/user/service/AddressDefaultConcurrencyMySqlIT.java（仅需要真实并发验证时）
```

仅在确有必要时允许修改：

```text
src/main/java/com/petcare/user/service/UserAddressService.java
src/main/java/com/petcare/user/service/impl/UserAddressServiceImpl.java
```

## 10. 禁止修改

- `.claude/` 下任何文件。
- `schema.sql`、数据库迁移、`UserAddress` 和 `User` 实体字段。
- 宠物、用户资料、JWT、管理员认证和测试登录代码。
- 预约距离算法、门店服务半径和预约 API。
- 前端、文件上传、AI、Redis。
- 增加 `label` 或其他 Schema 不存在字段。

## 11. 强制 TDD 顺序

### RED-1：授权、创建、列表和归属

先写失败测试：

- USER 创建第一条地址后自动成为默认地址。
- 请求伪造 `userId`、ID 和删除状态不生效。
- USER 列表只返回本人未删除地址，并稳定排序。
- `addressId` 为字符串，响应不含 `userId` 和 `deleted`。
- ADMIN Token 返回 403，无 Token 返回 401。
- 必填字段、坐标范围、坐标小数位和坐标成对规则非法时返回 400。
- 用户 A 不能修改或删除用户 B 地址。

### GREEN-1：创建与列表最小实现

- 新增 Controller、DTO 和应用服务。
- 实现创建、列表和归属边界。
- 不修改 Schema 和实体字段。

### RED-2：默认地址事务

先写失败测试：

- 设置新默认地址会取消旧默认地址。
- 修改当前默认地址并传 `false` 后仍有且只有一个默认地址。
- 删除非默认地址不改变默认地址。
- 删除默认地址会提升剩余最新地址。
- 任一中间写步骤返回 `false` 时事务回滚。
- 两个并发请求设置不同默认地址后，最终仍只有一个默认地址。

### GREEN-2 与重构

- 在写事务开始时锁定当前 ACTIVE 用户行。
- 完成修改、删除和默认地址重选。
- 检查所有写操作返回值和归属条件。

## 12. 与预约模块的兼容边界

- 不修改预约模块已有 `addressId + userId` 归属校验。
- 地址经纬度为空时允许保存，但上门预约仍必须拒绝缺失坐标的地址。
- 本阶段不计算距离，不修改门店经纬度或服务半径。
- 地址逻辑删除后，历史预约中的地址 ID 和快照字段保持不变。

## 13. 强制验证

```powershell
mvn "-Dtest=AddressControllerTest,AddressApplicationServiceImplTest,PetControllerTest,PetApplicationServiceImplTest,UserProfileControllerTest,TestLoginControllerTest,SecurityAccessTest" test
mvn test
mvn clean package "-DskipTests"
mvn -Ptc-mysql clean test
git diff --check
git status --short --branch
```

如果新增真实 MySQL 并发测试，必须纳入 `tc-mysql` Profile，并证明两个并发默认地址写请求结束后只有一个默认地址。

人工检查：

```powershell
rg -n "label|Double" src/main/java/com/petcare/user src/test/java/com/petcare/user
rg -n "UserAddressMapper" src/main/java/com/petcare/user/controller src/main/java/com/petcare/user/service
rg -n "userId|addressId|deleted" src/main/java/com/petcare/user/controller src/main/java/com/petcare/user/dto
```

## 14. 建议提交顺序

```text
test: define current user address api contract
feat: add current user address create and list api
test: define default address transaction contract
feat: add current user address mutation api
```

禁止 `git add .`，不得暂存 `.claude/`。

## 15. 退出门禁

- 地址创建、列表、修改和逻辑删除通过。
- 授权、归属、逻辑删除和坐标校验通过。
- 同一用户存在地址时始终只有一个默认地址。
- 默认地址并发和事务回滚测试通过。
- API 与真实 Schema 一致，无 `label`、`Double` 或虚构字段。
- 定向、完整、构建、MySQL 和 `git diff --check` 通过。
- `.claude/` 未进入提交。
- 无 CRITICAL/HIGH Review 遗留。

通过后才允许开始 11-05 公开内容匿名读取策略。

## 16. 强制交接格式

```text
任务：11-04 用户地址 API
阶段：11
分支：
提交：
前置门禁证据：
已完成：
未完成：
变更文件：
RED 证据：
验证命令：
验证结果：
覆盖率：
授权与归属检查：
默认地址不变量与并发检查：
Schema 一致性检查：
.claude/ 处理情况：
已知风险：
待决策事项：
下一步允许执行的任务：门禁通过后仅允许 11-05
```
