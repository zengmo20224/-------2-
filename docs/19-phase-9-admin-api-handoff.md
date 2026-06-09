# 第九阶段后台 API 交接文档

## 1. 阶段目标

第九阶段补齐管理后台基础资料 API。预约管理、内容审核、商品订单和 AI 管理沿用前置阶段已有接口，不重复实现。

本阶段新增：

- 门店信息与门店配置管理
- 服务项目管理
- 员工资料、技能与排班管理
- 商品资料与库存管理
- 管理员操作日志分页查询

## 2. 实现边界

- 统一前缀：`/api/v1/admin`
- 所有接口均要求管理员 JWT 和对应细粒度权限。
- Controller 只调用 `AdminManagementService`，不直接调用 Mapper。
- 写操作使用事务，并记录成功操作日志。
- 列表接口支持分页，页大小被限制在 `1-100`。
- 未实现管理员账号与角色管理。
- 未实现前端页面、Redis、真实 DeepSeek、微信支付或文件上传。

## 3. 接口清单

### 门店

| 方法 | 路径 | 权限 |
| --- | --- | --- |
| GET | `/api/v1/admin/stores/{id}` | `store:info:read` |
| PATCH | `/api/v1/admin/stores/{id}` | `store:info:update` |
| GET | `/api/v1/admin/stores/{id}/config` | `store:config:read` |
| PUT | `/api/v1/admin/stores/{id}/config` | `store:config:update` |

### 服务项目

| 方法 | 路径 | 权限 |
| --- | --- | --- |
| GET | `/api/v1/admin/service-items` | `service:item:read` |
| POST | `/api/v1/admin/service-items` | `service:item:create` |
| PUT | `/api/v1/admin/service-items/{id}` | `service:item:update` |
| POST | `/api/v1/admin/service-items/{id}/disable` | `service:item:disable` |

### 员工、技能与排班

| 方法 | 路径 | 权限 |
| --- | --- | --- |
| GET | `/api/v1/admin/staff` | `staff:profile:read` |
| POST | `/api/v1/admin/staff` | `staff:profile:create` |
| PUT | `/api/v1/admin/staff/{id}` | `staff:profile:update` |
| POST | `/api/v1/admin/staff/{id}/disable` | `staff:profile:disable` |
| PUT | `/api/v1/admin/staff/{id}/skills` | `staff:skill:manage` |
| GET | `/api/v1/admin/staff/{id}/schedules` | `staff:schedule:read` |
| POST | `/api/v1/admin/staff/{id}/schedules` | `staff:schedule:manage` |
| PUT | `/api/v1/admin/staff/{id}/schedules/{scheduleId}` | `staff:schedule:manage` |

排班规则：

- 排班门店必须等于员工所属门店。
- 开始时间必须早于结束时间。
- 同一员工同一天的排班时间段不能重叠。
- 冲突返回 `409` 和 `state_conflict`。

### 商品

| 方法 | 路径 | 权限 |
| --- | --- | --- |
| GET | `/api/v1/admin/products` | `product:item:read` |
| POST | `/api/v1/admin/products` | `product:item:create` |
| PUT | `/api/v1/admin/products/{id}` | `product:item:update` |
| POST | `/api/v1/admin/products/{id}/disable` | `product:item:disable` |
| PUT | `/api/v1/admin/products/{id}/stock` | `product:stock:update` |

库存不能小于 `0`。商品新建时库存和销量均初始化为 `0`，库存必须通过独立库存接口修改。库存绝对值修改会先通过 `FOR UPDATE` 锁定商品行，避免与下单原子扣库存产生丢失更新。

### 操作日志

| 方法 | 路径 | 权限 |
| --- | --- | --- |
| GET | `/api/v1/admin/operation-logs` | `admin:operation-log:read` |

支持按 `module` 过滤。操作日志只读，不提供修改或删除接口。

## 4. 操作日志范围

本阶段新增的重要写操作都会记录管理员 ID、模块、操作、HTTP 方法、请求路径、结果和时间。

为避免泄漏敏感信息，当前不保存请求体、Token、密码或密钥。

## 5. 测试与验证

新增测试：

- `AdminManagementControllerTest`

覆盖：

- 未认证返回 `401`
- 无权限返回 `403`
- 门店信息与配置更新
- 服务项目分页、新建与禁用
- 员工技能替换、排班新建、排班分页与排班冲突
- 商品分页与库存修改
- 操作日志分页与模块过滤
- 无效分类引用与非法库存输入
- 员工、服务项目、商品和排班的更新与禁用路径

已验证命令：

```powershell
mvn "-Djacoco.skip=true" "-Dtest=AdminManagementControllerTest" test
mvn "-Djacoco.skip=true" test
mvn test
mvn package "-DskipTests"
git diff --check
```

验证结果：

- 第九阶段定向集成测试：`8` 项通过。
- 全量测试：`517` 项通过。
- 新增 `AdminManagementController` 与 `AdminManagementServiceImpl` 合并行覆盖率：`96.85%`。
- 标准 `mvn test` 退出码为 `0`，但仍会打印既有 JSqlParser JaCoCo `MethodTooLargeException` 仪器化警告。

## 6. 已知风险与后续约束

- 当前阶段未新增 `schema.sql`，依赖现有表结构。
- H2 全量回归已通过；真实 MySQL 的第七阶段库存并发测试仍因本机没有可用测试凭据而未验证。
- `mvn clean` 在本机可能因 VS Code Java Language Server 占用 `target` 目录而失败，不能把该环境占用误判为代码失败。
- 前端接入前应基于本接口清单补充请求示例、空状态和错误状态处理。
