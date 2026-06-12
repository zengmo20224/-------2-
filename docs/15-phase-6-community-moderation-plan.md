# 阶段 6：社区与内容审核代码计划

日期：2026-06-09

负责人：GLM5.1

当前分支：`phase-6-community-moderation`

前置条件：

- 阶段 5 服务预约与排班代码已形成提交，最新阶段 5 提交为 `65195f7 test: add MySQL concurrency integration tests`。
- 社区和审核相关实体、Mapper、基础 Service 已在阶段 3 存在。
- 后台 Spring Security + JWT 和权限码体系已存在。

## 目标

本阶段实现社区内容与内容风控闭环：

- 话题查询。
- 帖子发布、列表、详情。
- 评论发布和列表。
- 点赞、取消点赞。
- 收藏、取消收藏。
- 举报帖子。
- 敏感词匹配。
- 内容审核记录创建。
- 后台帖子、评论、举报、敏感词管理。
- 后台审核、隐藏、删除、举报处理权限校验。

本阶段不实现前端、不实现真实文件上传、不接入 AI 发帖辅助、不实现 AI 内容审核、不实现推荐算法。

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
14. `docs/14-phase-5-booking-scheduling-plan.md`
15. 本文件

编码前必须先运行：

```powershell
git status --short --branch
git log --oneline -5
```

然后先输出：

```text
已阅读文档：
当前阶段：阶段 6 社区与内容审核
当前分支：phase-6-community-moderation
计划修改范围：
不会修改的范围：
待决策或阻塞项：
验证计划：
```

没有完成以上汇报前，不允许修改项目文件。

## 已决定事项

必须按以下决策执行：

- 后台使用 Spring Security + JWT。
- 后台社区审核操作使用 `docs/10-admin-permission-design.md` 中的权限码。
- V1 不启用 Redis。
- 当前仅维护 `schema.sql`，不引入 Flyway 或 Liquibase。
- 微信登录仍为占位，不允许伪造生产用户身份。
- 文件上传规则已决定，但真实上传仍属于阶段 14A，不在阶段 6 实现。

## 文件上传阶段边界

`docs/08-pending-decisions.md` 中 D-007 已决定：

- 图片格式白名单为 JPEG、PNG、WebP。
- 每个帖子最多上传 6 张图片。
- V1 使用可配置的本地目录 `./uploads/community/yyyy/MM/dd/`。
- V1 由前端压缩，后端不生成缩略图、不做二次压缩。

阶段 6 仍禁止实现真实文件上传，上传能力必须在阶段 14A 独立实施并通过安全测试。

允许做：

- 保留帖子图片关联 DTO 或 Service 边界。
- 在内部 Service 中支持已存在图片 URL 的 `post_image` 关联能力。
- 生产 Controller 在阶段 14A 完成前不应开放图片上传入口。

不允许做：

- 不新增 `/upload` 接口。
- 不在阶段 6 提前实现已决定的上传策略。
- 不新增未经安全测试的本地文件写入。

如果 GLM5.1 认为阶段 6 必须开放图片上传，必须停止相关实现并转交阶段 14A，不能扩大当前任务范围。

## 阶段 6 允许做的事

允许新增：

- `community/controller`
- `community/dto`
- `community/domain`
- `community/service` 中的业务方法
- `moderation/domain`
- `moderation/dto`
- `moderation/controller`
- 敏感词匹配和审核策略测试
- 社区 API 集成测试
- 后台审核权限测试

允许调整：

- `PostMapper`、`PostCommentMapper` 增加列表查询和计数更新方法。
- `PostLikeMapper`、`PostFavoriteMapper` 增加按用户和帖子查询方法。
- `PostReportMapper` 增加举报处理查询。
- `SensitiveWordMapper` 增加启用敏感词查询。
- `ContentReviewRecordMapper` 增加按内容查询和更新审核结果方法。
- `ErrorCode` 增加社区和内容审核相关错误码。
- `GlobalExceptionHandler` 增加重复点赞、重复收藏、状态冲突等错误映射。

## 阶段 6 禁止做的事

本阶段禁止：

- 不实现真实文件上传。
- 不接入 AI 内容审核或 AI 发帖辅助。
- 不引入 Redis 缓存敏感词。
- 不引入 Elasticsearch 或外部搜索服务。
- 不实现推荐流、热榜算法、关注关系或私信。
- 不修改 `schema.sql`，除非发现阻塞级错误并先征得用户确认。
- 不通过请求体 `userId` 绕过用户认证。
- 不把被审核的敏感内容、完整手机号、Token、密码、API Key 写入日志。
- 不把 SQL 异常、堆栈、敏感词库明细泄露给客户端。

## 用户身份边界

当前真实用户登录仍未启用。

要求：

- 用户侧业务 Service 设计为 `createPost(currentUserId, request)`、`likePost(currentUserId, postId)` 等形式。
- Controller 不能从请求体读取 `userId`。
- 如果生产环境没有当前用户身份，用户侧写操作返回 `401`。
- 测试可以通过测试专用安全上下文或直接调用 Service 传入 `currentUserId`。
- 不允许新增“模拟登录”“测试登录”“后门 header user id”进入生产代码。

如果 GLM5.1 认为必须先实现用户 JWT，必须停止用户侧 Controller 写操作实现，先报告冲突，由用户决定是否调整认证阶段。

## 推荐包结构

```text
src/main/java/com/petcare/
  community/
    controller/
      TopicController.java
      PostController.java
      CommentController.java
      AdminCommunityController.java
    dto/
      TopicResponse.java
      PostCreateRequest.java
      PostResponse.java
      PostDetailResponse.java
      CommentCreateRequest.java
      CommentResponse.java
      ReportPostRequest.java
      AdminReviewRequest.java
      AdminReportHandleRequest.java
    domain/
      CommunityContentType.java
      ReviewStatus.java
      ReportStatus.java
      PostVisibilityPolicy.java
    service/
      CommunityPostApplicationService.java
      CommunityInteractionService.java
      CommunityAdminService.java
    service/impl/
      ...
  moderation/
    controller/
      AdminSensitiveWordController.java
    dto/
      SensitiveWordCreateRequest.java
      SensitiveWordResponse.java
      ContentReviewResult.java
    domain/
      SensitiveWordMatcher.java
      ContentModerationPolicy.java
      MatchedSensitiveWord.java
    service/
      ContentModerationService.java
```

说明：

- `domain` 放纯规则，不直接访问数据库。
- 敏感词匹配、风险等级、状态决策优先写成可单测的纯逻辑。
- Service 负责事务、计数、状态变更和审核记录。
- Controller 只做参数校验和调用 Service。

## API 设计

### 话题

```text
GET /api/v1/topics
GET /api/v1/topics/{id}
```

要求：

- 只返回 `status = ACTIVE` 且 `deleted = 0` 的话题。
- 按 `sort` 升序、`create_time` 降序。

### 帖子列表和详情

```text
GET /api/v1/posts
GET /api/v1/posts/{id}
```

查询参数：

```text
topicId
page
size
```

要求：

- 用户侧只返回 `status = PUBLISHED` 且 `deleted = 0` 的帖子。
- 列表支持分页。
- 详情只允许访问已发布帖子，或当前用户自己的非删除帖子。
- 不返回隐藏、删除、拒绝内容给普通用户。
- 不返回内部审核备注。

### 发布帖子

```text
POST /api/v1/posts
```

请求示例：

```json
{
  "topicId": 1,
  "petId": 2,
  "title": "今天洗护体验很好",
  "content": "门店服务很细心"
}
```

要求：

- 当前用户身份来自安全上下文，不来自请求体。
- `title` 必填，长度建议 1 到 120。
- `content` 必填，长度建议 1 到 5000。
- `topicId` 如果传入，必须存在且启用。
- `petId` 如果传入，必须属于当前用户。
- 本阶段默认不开放图片上传。
- 发布时必须经过敏感词审核。
- 必须创建 `content_review_record`。

发布结果：

- 无敏感词：`post.status = PUBLISHED`，`risk_level = 0`，`review_status = APPROVED`。
- 命中 1 级敏感词：`post.status = PENDING_REVIEW`，`risk_level = 1`，`review_status = PENDING`。
- 命中 2 级敏感词：`post.status = PENDING_REVIEW`，`risk_level = 2`，`review_status = PENDING`。
- 命中 3 级敏感词：`post.status = REJECTED`，`risk_level = 3`，`review_status = REJECTED`。

### 评论

```text
GET /api/v1/posts/{postId}/comments
POST /api/v1/posts/{postId}/comments
```

要求：

- 只能评论 `PUBLISHED` 帖子。
- 评论内容必填，长度建议 1 到 1000。
- `parentId` 如果存在，必须属于同一个帖子且未删除。
- 评论同样经过敏感词审核。
- 必须创建 `content_review_record`。
- 无敏感词评论进入 `PUBLISHED` 并增加帖子 `comment_count`。
- 待审核或拒绝评论不计入普通用户可见评论数。

### 点赞和收藏

```text
POST /api/v1/posts/{postId}/like
DELETE /api/v1/posts/{postId}/like
POST /api/v1/posts/{postId}/favorite
DELETE /api/v1/posts/{postId}/favorite
```

要求：

- 只能对 `PUBLISHED` 帖子操作。
- 点赞和收藏必须幂等。
- 重复点赞不能重复增加 `like_count`。
- 重复收藏不能重复增加 `favorite_count`。
- 取消不存在的点赞或收藏应返回成功或业务无变化结果，不能报 500。
- 依赖 `post_like(post_id, user_id)` 和 `post_favorite(post_id, user_id)` 唯一约束防重复。
- 计数更新和关系写入必须在同一事务内。

### 举报

```text
POST /api/v1/posts/{postId}/reports
```

请求示例：

```json
{
  "reasonType": "SPAM",
  "reason": "广告刷屏"
}
```

要求：

- 只能举报存在且未删除的帖子。
- `reasonType` 限定为 `SPAM`、`ILLEGAL`、`ABUSE`、`OTHER`。
- 初始状态为 `PENDING`。
- 同一用户重复举报同一帖子时，建议返回已有举报或 `409`，不能重复制造大量举报。

## 后台 API 设计

### 帖子审核

```text
GET /api/v1/admin/community/posts
GET /api/v1/admin/community/posts/{id}
POST /api/v1/admin/community/posts/{id}/approve
POST /api/v1/admin/community/posts/{id}/reject
POST /api/v1/admin/community/posts/{id}/hide
POST /api/v1/admin/community/posts/{id}/delete
```

权限：

```text
community:post:read
community:post:approve
community:post:reject
community:post:hide
community:post:delete
```

要求：

- 使用 `@PreAuthorize("hasAuthority('...')")`。
- 审核通过：`PENDING_REVIEW -> PUBLISHED`，更新审核记录为 `APPROVED`。
- 审核拒绝：`PENDING_REVIEW -> REJECTED`，写入 `reject_reason`，更新审核记录为 `REJECTED`。
- 隐藏：`PUBLISHED -> HIDDEN`。
- 删除：逻辑删除或状态置 `DELETED`，保持和实体逻辑删除策略一致。
- 管理员操作应写入 `admin_operation_log`；如果当前实现能力不足，必须在交接报告中列为未完成风险。

### 评论审核

```text
GET /api/v1/admin/community/comments
POST /api/v1/admin/community/comments/{id}/approve
POST /api/v1/admin/community/comments/{id}/reject
POST /api/v1/admin/community/comments/{id}/hide
POST /api/v1/admin/community/comments/{id}/delete
```

权限：

```text
community:post:read
community:post:approve
community:post:reject
community:comment:hide
community:comment:delete
```

要求：

- 评论通过审核后才增加对应帖子 `comment_count`。
- 评论隐藏或删除后，如果原本是可见评论，需要扣减 `comment_count`。
- 禁止出现负数计数。

### 举报处理

```text
GET /api/v1/admin/community/reports
POST /api/v1/admin/community/reports/{id}/handle
```

权限：

```text
community:report:handle
```

处理结果：

- `PROCESSED`
- `IGNORED`

要求：

- 写入 `handler_id`、`handle_time`、`handle_result`。
- 处理举报时可以同时隐藏帖子，但必须显式传入动作，不能隐式删除内容。

### 敏感词管理

```text
GET /api/v1/admin/moderation/sensitive-words
POST /api/v1/admin/moderation/sensitive-words
PATCH /api/v1/admin/moderation/sensitive-words/{id}
POST /api/v1/admin/moderation/sensitive-words/{id}/disable
```

权限：

```text
community:sensitive-word:manage
```

要求：

- `word` 必填，长度建议 1 到 100。
- `level` 只能为 `1`、`2`、`3`。
- `status` 使用 `ACTIVE`、`DISABLED`。
- 禁止重复启用同一个敏感词。
- 不在普通用户错误响应中暴露完整敏感词库。

## 敏感词匹配规则

V1 使用确定性本地规则，不调用 AI。

输入：

- 标题和正文，或评论内容。
- `sensitive_word.status = ACTIVE` 且 `deleted = 0` 的词库。

处理：

1. 对输入文本做基础归一化：
   - `null` 转为空字符串。
   - 去除首尾空白。
   - 英文统一小写。
2. 使用包含匹配，不使用用户输入构造正则。
3. 收集命中的敏感词和最高等级。
4. `risk_level = max(matched.level)`。
5. `matched_words` 写入审核记录，长度不能超过字段上限，超出时安全截断。

风险决策：

```text
无命中：PUBLISHED / APPROVED
最高等级 1：PENDING_REVIEW / PENDING
最高等级 2：PENDING_REVIEW / PENDING
最高等级 3：REJECTED / REJECTED
```

注意：

- 不能只在前端做审核。
- 评论和帖子必须共用同一套审核策略。
- 敏感词匹配失败不能静默放行；未知异常应返回安全错误并记录服务端日志。

## 事务要求

必须使用事务的操作：

- 发布帖子和创建审核记录。
- 发布评论、创建审核记录、更新 `comment_count`。
- 点赞/取消点赞和更新 `like_count`。
- 收藏/取消收藏和更新 `favorite_count`。
- 审核通过/拒绝内容和更新审核记录。
- 隐藏/删除评论并维护 `comment_count`。
- 处理举报和可选隐藏帖子。

要求：

- Controller 不能直接调用 Mapper。
- 自定义 SQL 不使用字符串拼接。
- 多表变更必须在 Service 层 `@Transactional`。
- 唯一约束冲突必须转成业务响应，不能暴露数据库异常。

## 错误码建议

在 `ErrorCode` 中新增：

```text
community_post_not_found
community_topic_not_found
community_comment_not_found
community_content_rejected
community_content_pending_review
community_post_not_visible
community_duplicate_like
community_duplicate_favorite
community_duplicate_report
community_review_status_invalid
community_sensitive_word_duplicate
community_file_upload_not_decided
```

HTTP 状态建议：

- `400`：参数格式错误。
- `401`：用户或管理员未认证。
- `403`：无权限或资源不属于当前用户。
- `404`：帖子、评论、话题、举报不存在。
- `409`：重复点赞、重复收藏、重复举报、非法状态流转。
- `422`：内容被拒绝、内容进入待审核、文件上传未决。

## TDD 实施顺序

必须按 RED、GREEN、重构推进。

建议顺序：

1. 写 `SensitiveWordMatcherTest`，覆盖无命中、1 级、2 级、3 级、多词命中、大小写归一化。
2. 写 `ContentModerationPolicyTest`，覆盖帖子和评论的发布状态、审核状态和风险等级。
3. 写 `CommunityPostServiceTest`，覆盖发帖、审核记录、宠物归属、话题状态。
4. 写 `CommentServiceTest`，覆盖评论审核、父评论校验、评论计数。
5. 写 `CommunityInteractionServiceTest`，覆盖点赞、取消点赞、收藏、取消收藏的幂等和计数。
6. 写 `PostReportServiceTest`，覆盖举报创建、重复举报、后台处理。
7. 写 `AdminCommunityControllerTest`，覆盖 `401`、`403` 和权限码。
8. 写 `AdminSensitiveWordControllerTest`，覆盖敏感词管理权限和重复词校验。
9. 实现最小代码让测试变绿。
10. 重构并运行完整验证。

## 最低测试清单

单元测试必须覆盖：

- 无风险内容直接发布。
- 轻度风险内容进入待审核。
- 中度风险内容进入待审核。
- 严重风险内容直接拒绝。
- 评论和帖子使用同一审核策略。
- 多敏感词命中时取最高风险等级。
- `matched_words` 安全截断。
- 点赞、收藏计数不能重复增加。
- 取消不存在的点赞或收藏不报 500。

集成测试必须覆盖：

- 发帖写入 `post` 和 `content_review_record`。
- 评论写入 `post_comment` 和 `content_review_record`。
- 严重风险帖子不会进入普通用户列表。
- 待审核帖子不会进入普通用户列表。
- 后台可查询待审核内容。
- 后台审核通过后内容进入已发布。
- 后台审核拒绝后内容进入已拒绝。
- 后台隐藏和删除后普通用户不可见。
- 举报处理写入 `handler_id` 和 `handle_time`。
- 敏感词新增、禁用后影响后续审核。
- 后台无权限访问返回 `403`。
- 未认证访问后台审核接口返回 `401`。

## 测试数据库策略

普通快速测试：

```powershell
mvn test
```

如需要真实 MySQL 验证唯一约束和事务行为：

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/petcare_o2o?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的本地密码"
mvn "-Dtest=CommunityModerationIntegrationIT" test
```

如果没有真实 MySQL，至少必须用 H2 覆盖业务规则和 Controller 权限；但重复点赞、重复收藏等依赖唯一约束的行为最好在 MySQL 8 上验证。

## 实施顺序

建议按以下顺序做：

1. 新增阶段 6 测试骨架，先让测试 RED。
2. 实现敏感词匹配纯规则。
3. 实现内容审核策略。
4. 实现话题和帖子只读查询。
5. 实现发帖 Service 和审核记录。
6. 实现评论 Service 和审核记录。
7. 实现点赞、收藏和计数事务。
8. 实现举报和举报处理。
9. 实现后台帖子、评论审核。
10. 实现敏感词管理。
11. 加上 `@PreAuthorize` 权限码。
12. 补齐集成测试。
13. 运行完整验证。
14. 提交 Git。

## 验证命令

阶段完成前必须运行：

```powershell
mvn test
mvn clean package
git diff --check
git status --short --branch
```

如果某项验证未执行，必须在交接报告中写明：

```text
未执行命令：
原因：
替代验证：
风险：
```

## Git 提交建议

阶段 6 代码建议拆成多个小提交：

```powershell
git add src/test/java/com/petcare/moderation src/test/java/com/petcare/community
git commit -m "test: add community moderation rule tests"

git add src/main/java/com/petcare/moderation src/main/java/com/petcare/community
git commit -m "feat: implement content moderation rules"

git add src/main/java/com/petcare/community src/test/java/com/petcare/community
git commit -m "feat: add community post and interaction APIs"

git add src/main/java/com/petcare/community src/main/java/com/petcare/moderation src/test/java
git commit -m "feat: add admin community moderation APIs"
```

如果只提交计划文档，使用：

```powershell
git add AGENTS.md README.md docs/02-task-breakdown.md docs/03-glm5-implementation-plan.md docs/07-integration-gates.md docs/15-phase-6-community-moderation-plan.md
git commit -m "docs: add phase 6 community moderation plan"
```

提交前必须确认：

- 没有真实密钥。
- 没有真实微信 AppID/AppSecret。
- 没有 Redis、Flyway、Liquibase。
- 没有前端代码。
- 没有真实文件上传实现。
- 没有越界实现 AI 发帖辅助或 AI 审核。
- 没有通过请求体 `userId` 绕过用户认证。
- `git diff --check` 通过。

## 阶段 6 交接报告格式

完成后必须按以下格式汇报：

```text
任务：阶段 6 社区与内容审核
阶段：phase-6-community-moderation
分支：phase-6-community-moderation
提交：
已完成：
未完成：
变更文件：
验证命令：
验证结果：
覆盖率：
敏感词审核验证：
帖子与评论验证：
点赞收藏验证：
举报处理验证：
后台权限验证：
图片上传边界：
已知风险：
待决策事项：
下一步允许执行的任务：阶段 7 商品到店自提订单
```

## 阶段 6 退出标准

只有同时满足以下条件，才允许进入阶段 7：

- 话题查询可用。
- 帖子发布、列表、详情业务服务可用。
- 评论发布和列表业务服务可用。
- 点赞、收藏、举报业务服务可用。
- 敏感词匹配规则通过测试。
- 无风险、轻度风险、中度风险、严重风险内容路径都有测试。
- 严重风险内容被拒绝。
- 轻度和中度风险内容进入待审核。
- 评论同样经过内容审核。
- `content_review_record` 创建和审核更新通过测试。
- 后台审核、隐藏、删除、举报处理接口有权限保护。
- 敏感词管理接口有权限保护。
- 图片上传未决事项没有被擅自实现。
- `mvn test` 通过。
- `mvn clean package` 通过。
- `git diff --check` 通过。
- 已提交 Git。
