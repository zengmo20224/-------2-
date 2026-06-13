# 阶段 11-05：GLM5.1 公开读取与私有数据隔离任务书

日期：2026-06-13

状态：已规划，当前锁定

解锁条件：`docs/40-phase-11-04-review-and-remediation-plan.md` 全部门禁通过

执行对象：GLM5.1

目标分支：`phase-11-user-prerequisites`

## 1. 任务目的

落实 D-014：匿名用户可以浏览公开服务、商品、话题、已发布帖子和已发布评论；所有写操作、用户私有资源、管理后台和 AI 接口继续要求认证。

本任务必须基于真实 Controller 路径实施，不得使用旧计划中的虚构 `/api/v1/services/**`、`/api/v1/community/**` 路径。

## 2. 前置门禁

- 11-04R 全部 HIGH/MEDIUM Findings 已关闭。
- `mvn test`、构建和 `mvn -Ptc-mysql clean test` 通过。
- 当前分支为 `phase-11-user-prerequisites`。
- 工作区除 `.claude/` 外无不明修改。

任一条件不满足时停止。

## 3. 匿名 GET 白名单

仅允许以下真实 GET 路径匿名访问：

```text
/api/v1/service-categories
/api/v1/service-items
/api/v1/service-items/{id}
/api/v1/product-categories
/api/v1/products
/api/v1/products/{id}
/api/v1/topics
/api/v1/topics/{id}
/api/v1/posts
/api/v1/posts/{id}
/api/v1/posts/{postId}/comments
```

`SecurityConfig` 必须使用 `HttpMethod.GET` 和逐项路径 matcher。禁止：

```text
/api/v1/**
/api/v1/user/**
/api/v1/admin/**
/api/v1/product-orders/**
/api/v1/cart-items/**
/api/v1/bookings/**
```

## 4. 公开内容过滤规则

### 服务

- 仅返回 ACTIVE 服务分类。
- 仅返回 ON_SALE 服务项目。
- 下架或不存在详情返回 404。

### 商品

- 仅返回 ACTIVE 商品分类。
- 仅返回 ON_SALE 商品。
- 下架或不存在详情返回 404。

### 社区

- 仅返回 ACTIVE 话题。
- 帖子列表与匿名详情只返回 `PUBLISHED + deleted=0`。
- 评论列表只允许读取 `PUBLISHED + deleted=0` 评论。
- 评论列表必须先确认所属帖子为 `PUBLISHED + deleted=0`；不得通过评论接口推断待审核、拒绝、隐藏或删除帖子。
- 匿名访问非公开帖子、评论或话题时使用安全 404/不可见错误，不泄露审核状态。

## 5. 匿名响应隐私边界

公开响应不得包含：

```text
手机号、openid、unionid、地址、预约、订单、宠物档案、petId、
内部用户 ID、删除标记、风险等级、审核备注、Provider 原始信息
```

社区当前用户端 DTO 含 `userId`、`petId` 和内部 `status`。不得直接把管理员 DTO 当作匿名公开 DTO。

建议新增最小公开 DTO：

```text
PublicPostSummaryResponse
PublicPostDetailResponse
PublicCommentResponse
```

公开 DTO 只保留展示所需内容、计数、图片和公开时间。管理员接口继续使用原有管理 DTO，不得因本任务丢失审核字段。

## 6. 身份与方法边界

- 无 Authorization 请求访问白名单 GET 返回公开结果。
- 携带合法 USER 或 ADMIN Token 访问公开 GET 仍可正常读取公开结果。
- 携带格式错误、签名错误或过期 Bearer Token 不得降级为匿名身份，应返回 401。
- 所有 POST、PUT、PATCH、DELETE 继续要求认证。
- 用户私有 GET、订单、购物车、预约、宠物、地址和用户资料继续要求认证。
- 管理后台 GET 与写操作继续要求管理员认证和权限。
- AI 接口继续要求认证，不进入匿名白名单。

本任务不修复社区用户写操作当前仍返回 401 的历史实现；只保证匿名放行不会扩大写权限。

## 7. 允许修改范围

```text
src/main/java/com/petcare/common/config/SecurityConfig.java
src/main/java/com/petcare/community/controller/PostController.java
src/main/java/com/petcare/community/dto/ 公开读取 DTO
src/main/java/com/petcare/community/service/CommunityPostApplicationService.java
src/test/java/com/petcare/common/security/PublicAccessIntegrationTest.java
src/test/java/com/petcare/community/
src/test/java/com/petcare/product/controller/ProductCatalogControllerTest.java
src/test/java/com/petcare/service/controller/ServiceCatalogControllerTest.java
```

仅为修复已发现的公开过滤缺口时允许修改对应 catalog 查询服务。

## 8. 禁止修改

- `.claude/`、Schema、实体字段和数据库迁移。
- 用户认证、管理员登录、JWT 签发规则。
- 用户资料、宠物、地址、预约、购物车和订单业务。
- 管理后台 DTO 和权限粒度。
- 前端、文件上传、AI Provider 和 Redis。
- 使用过宽匿名 matcher。

## 9. 强制 TDD 顺序

### RED-1：真实匿名 GET 白名单

先写 `PublicAccessIntegrationTest`：

- 无 Token 访问全部白名单 GET 返回 200 或资源不存在时的安全 404。
- 无 Token 访问用户私有 GET、管理 GET、订单和购物车返回 401。
- 无 Token 对公开路径发送 POST/PUT/DELETE 返回 401。
- 非白名单 GET 继续返回 401。

### GREEN-1：最小 SecurityConfig 放行

- 使用 `requestMatchers(HttpMethod.GET, ...)`。
- 路径与真实 Controller 完全一致。
- 保持 `/api/v1/**` 默认认证。

### RED-2：公开过滤与隐私

先写失败测试：

- 匿名服务目录不含下架数据。
- 匿名商品目录不含下架数据。
- 匿名话题不含禁用数据。
- 匿名帖子列表和详情不含非 PUBLISHED 数据。
- 匿名评论列表不含非 PUBLISHED 评论。
- 非 PUBLISHED 帖子的评论接口不可匿名读取。
- 公开社区 JSON 不含 `userId`、`petId`、内部 `status`、风险和删除字段。

### GREEN-2：公开 DTO 与查询收敛

- 新增公开 DTO，不破坏管理后台契约。
- 收紧评论所属帖子可见性。
- 不增加匿名写能力。

### RED-3：无效 Token 与角色回归

- 无效或过期 Token 访问公开 GET 返回 401。
- 合法 USER/ADMIN Token 访问公开 GET 正常。
- ADMIN 接口权限和 USER 私有接口不回归。

## 10. 强制验证

```powershell
mvn "-Dtest=PublicAccessIntegrationTest,SecurityAccessTest,ServiceCatalogControllerTest,ProductCatalogControllerTest,CommunityPostApplicationServiceTest,AdminCommunityControllerTest,AddressControllerTest,PetControllerTest" test
mvn test
mvn clean package "-DskipTests"
mvn -Ptc-mysql clean test
git diff --check
git status --short --branch
```

人工检查：

```powershell
rg -n "permitAll|requestMatchers" src/main/java/com/petcare/common/config/SecurityConfig.java
rg -n "userId|petId|phone|address|deleted|riskLevel" src/main/java/com/petcare/community/dto
rg -n "/api/v1/\\*\\*|/api/v1/user/\\*\\*|/api/v1/admin/\\*\\*" src/main/java/com/petcare/common/config/SecurityConfig.java
```

## 11. 建议提交顺序

```text
test: define anonymous public read security contract
feat: allow anonymous public catalog reads
test: define public community privacy contract
fix: isolate public community responses
```

禁止 `git add .`，不得暂存 `.claude/`。

## 12. 退出门禁

- 真实公开 GET 白名单匿名访问通过。
- 所有写操作、私有资源和管理接口继续受保护。
- 非公开社区内容无法通过帖子或评论接口匿名获取。
- 匿名响应不泄露私有标识或审核字段。
- 无效 Token 不降级为匿名身份。
- 定向、完整、构建、MySQL 和 `git diff --check` 全部通过。
- `.claude/` 未进入提交。

通过后才允许开始 11-06 可复现演示种子数据任务。

## 13. 强制交接格式

```text
任务：11-05 公开读取与私有数据隔离
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
匿名白名单检查：
社区可见性检查：
私有数据隔离检查：
.claude/ 处理情况：
已知风险：
待决策事项：
下一步允许执行的任务：门禁通过后仅允许 11-06
```
