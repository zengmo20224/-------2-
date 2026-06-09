# 阶段 8：AI Provider 与 AI 功能代码计划

日期：2026-06-09

负责人：GLM5.1

当前分支：`phase-8-ai`

前置条件：

- 阶段 7 商品到店自提订单已形成代码提交；开始阶段 8 编码前必须确认门禁 7 已通过或明确记录未通过风险。
- `ai_conversation`、`ai_message`、`ai_usage_log`、`ai_analysis_report`、`faq_knowledge` 表和基础实体、Mapper、Service 已存在。
- `DeepSeekProperties`、`DEEPSEEK_BASE_URL`、`DEEPSEEK_API_KEY` 配置占位已存在。
- 管理员 JWT、细粒度权限码和安全上下文已存在。

当前基线验证风险：

- 2026-06-09 执行标准 `mvn test` 时，JaCoCo 0.8.12 对 `jsqlparser` 的超大方法插桩触发 `MethodTooLargeException`。
- 使用 `mvn "-Djacoco.skip=true" test` 时，现有 420 个测试曾全部通过。
- 使用 `mvn "-Djacoco.skip=true" clean package` 从干净构建执行时，曾在第 417 项出现 `WechatLoginControllerTest` 无法发现 `@SpringBootConfiguration` 的不稳定失败。
- 阶段 8 开发可以使用跳过 JaCoCo 的命令辅助定位业务测试，但不能把长期跳过覆盖率作为完成方案。
- 阶段 8 开始编码前应先解决或稳定复现以上构建基线问题。
- 阶段 8 退出前必须恢复标准 `mvn test` 和 `mvn clean package`，或明确记录阻塞并获得用户接受。

## 目标

本阶段建立安全、可替换、可测试的 AI 能力基础，并实现以下业务能力：

- 统一 AI Provider 端口，业务代码不直接依赖 DeepSeek 请求 DTO。
- AI 会话、用户消息、助手消息和用量日志持久化。
- 基于后端可信数据构造 AI 客服上下文。
- 使用确定性规则保护宠物陪伴助手的医疗安全边界。
- 根据用户明确提供的事实生成或优化帖子文案。
- 后端先聚合经营数据，再由 AI 生成管理端分析报告。
- Provider 缺失、超时、上游失败时返回安全、稳定的错误。
- 使用 Mock Provider 完成自动化测试，不依赖真实 DeepSeek API。

本阶段不能让 AI 直接访问数据库、自由调用工具、生成 SQL 或把模型输出当作系统事实。

## 必须先阅读

GLM5.1 开始编码前必须阅读：

1. `AGENTS.md`
2. `README.md`
3. `schema.sql`
4. `docs/requirements-source.md`
5. `docs/00-project-boundary.md`
6. `docs/01-architecture-design.md`
7. `docs/02-task-breakdown.md`
8. `docs/03-glm5-implementation-plan.md`
9. `docs/04-code-standards.md`
10. `docs/05-testing-and-verification.md`
11. `docs/06-git-safety-workflow.md`
12. `docs/07-integration-gates.md`
13. `docs/08-pending-decisions.md`
14. `docs/10-admin-permission-design.md`
15. `docs/13-phase-4-auth-authorization-plan.md`
16. `docs/15-phase-6-community-moderation-plan.md`
17. `docs/16-phase-7-product-orders-plan.md`
18. 本文件

编码前必须先运行：

```powershell
git status --short --branch
git log --oneline -5
```

然后先输出：

```text
已阅读文档：
当前阶段：阶段 8 AI Provider 与 AI 功能
当前分支：phase-8-ai
计划修改范围：
不会修改的范围：
待决策或阻塞项：
验证计划：
```

没有完成以上汇报前，不允许修改项目文件。

## 已决定事项

必须按以下项目决策执行：

- AI Provider Client 按 OpenAI 兼容 Chat Completions 协议抽象。
- 后续 Provider 计划接入 DeepSeek。
- Base URL 使用可配置项，默认 `https://api.deepseek.com`。
- API Key 使用环境变量 `DEEPSEEK_API_KEY`。
- Provider 凭据不能硬编码、打印或写入数据库。
- V1 不启用 Redis。
- AI 客服只能基于后端提供的门店、配置、服务、商品和 FAQ 上下文回答。
- AI 宠物陪伴不能诊断疾病、开药、替代兽医或承诺治疗效果。
- 高风险宠物症状必须由确定性规则触发建议就医，不能只依赖模型判断。
- AI 发帖辅助不能编造用户没有提供的事实。
- 管理端 AI 分析只能接收后端聚合结果，不能直接查询数据库。
- 管理端生成分析报告使用权限码 `ai:analysis:generate`。
- 管理端查看 AI 用量使用权限码 `ai:usage:read`。

## D-008 未决事项与阶段拆分

以下事项在 `docs/08-pending-decisions.md` 中仍为未决：

- 默认模型名称。
- 连接和读取超时。
- Provider 重试次数。
- 最大 Token。
- 用户和管理员调用频率限制。
- 是否需要流式输出。

任何 Agent 都不能自行选择或写死这些值。

因此阶段 8 分成两个可审查部分：

### 阶段 8A：可以立即实施

- 定义 Provider 无关的请求、响应、用量和异常模型。
- 定义 `AiProviderClient` 接口。
- 实现默认关闭的 `DisabledAiProviderClient`。
- 实现 Mock/Fake Provider 测试设施。
- 实现上下文构造、Prompt 构造和确定性安全规则。
- 实现会话、消息、用量日志和分析报告业务服务。
- 实现用户端和管理端 API 形状。
- 实现后端经营数据聚合器。
- 在 Provider 未启用时安全返回 `503`，不伪造 AI 响应。
- 测试 Provider 层不能依赖 Mapper 或数据库。

### 阶段 8B：等待用户决策后实施

- 真实 DeepSeek HTTP Client。
- 真实模型名称配置和校验。
- 超时和重试策略。
- 最大 Token 策略。
- 用户和管理员调用限流。
- 流式或非流式输出实现。
- 真实 DeepSeek 沙箱调用验证。

在 D-008 未全部决定前：

- 不允许声称“DeepSeek 已接入”。
- 不允许在生产 profile 启用真实 Provider。
- 不允许为跑通测试写死模型、超时、重试、Token 或限流值。
- 阶段 8A 可以完成和提交，但阶段 8B 必须标记为阻塞。

## 阶段 8 允许做的事

允许新增：

- `ai/provider`：Provider 端口、Provider 无关模型、禁用实现、异常映射。
- `ai/domain`：安全规则、上下文对象、Prompt 构造、输出校验。
- `ai/controller`：用户 AI API 和管理端 AI API。
- `ai/dto`：AI 请求和响应 DTO。
- `ai/service`：会话、客服、宠物陪伴、发帖辅助、分析报告业务服务。
- `ai/analytics`：后端经营数据聚合器。
- `ai/mapper` 必要的会话、消息、FAQ、用量和分析查询方法。
- AI Provider、上下文、安全、持久化、权限和 API 测试。

允许调整：

- `DeepSeekProperties` 增加配置字段声明，但未决字段不能提供项目默认值。
- `application.yml` 和 `.env.example` 增加 AI 开关及未决配置占位符。
- `ErrorCode` 增加 AI 相关安全错误码。
- `GlobalExceptionHandler` 增加 Provider 异常的安全映射。
- 现有 AI 基础 Service 增加业务方法。
- 经营数据聚合所需 Mapper 增加参数化只读查询。

## 阶段 8 禁止做的事

本阶段禁止：

- 不让 Provider Client、模型或 Prompt 直接持有 Mapper、Service、DataSource、JdbcTemplate 或数据库连接。
- 不向模型开放 SQL、数据库、文件系统、HTTP 工具调用或函数调用。
- 不让模型生成 SQL 后由系统执行。
- 不把模型输出直接写回商品价格、库存、预约、订单、内容审核结果或其他业务状态。
- 不把系统 Prompt、完整可信上下文或用户隐私原样写入用量日志。
- 不记录 API Key、Authorization Header、Provider 原始响应体或完整堆栈。
- 不把 Provider 原始错误返回客户端。
- 不在请求体中接受 `userId` 或 `adminId` 作为身份来源。
- 不实现真实微信登录或测试后门用户身份。
- 不引入 Redis、Flyway 或 Liquibase。
- 不修改 `schema.sql`，除非发现阻塞级错误并先征得用户确认。
- 不实现图片生成、语音、向量数据库、RAG 平台、联网搜索或 Agent 工具调用。
- 不实现 AI 自动内容审核或自动改变帖子审核状态。
- 不实现前端。

## 安全架构

必须保持以下调用方向：

```text
Controller
  -> AI Application Service
    -> Context Builder / Analytics Aggregator
      -> 业务模块只读 Service 或 Mapper
    -> Safety Policy / Prompt Builder
    -> AiProviderClient
      -> Disabled Provider / Mock Provider / 后续 DeepSeek Adapter
    -> Output Safety Policy
    -> Conversation / Message / Usage / Report Persistence
```

关键边界：

- 只有上下文构造器和经营数据聚合器可以读取后端业务数据。
- `AiProviderClient` 只接收已经整理完成的结构化请求。
- Provider 层不能反向调用数据库。
- Provider 返回值必须经过输出安全检查后才能返回用户。
- 模型输出只能作为解释、文案或建议，不能作为业务事实来源。

建议增加架构测试：

```text
AiProviderArchitectureTest
```

至少验证：

- `ai.provider` 包不依赖 `*.mapper`。
- `ai.provider` 包不依赖 `javax.sql.DataSource`、`JdbcTemplate` 或 MyBatis。
- Provider 请求模型不包含 SQL、数据库连接或工具定义字段。

## 推荐包结构

```text
src/main/java/com/petcare/ai/
  analytics/
    BusinessAnalyticsAggregator.java
    CommunityAnalyticsAggregator.java
    SalesAnalyticsAggregator.java
    ActivityAnalyticsAggregator.java
    dto/
  controller/
    AiConversationController.java
    AiPostAssistantController.java
    AdminAiAnalysisController.java
    AdminAiUsageController.java
  domain/
    CustomerServiceContext.java
    CustomerServiceContextBuilder.java
    CustomerServiceGroundingPolicy.java
    HighRiskSymptomDetector.java
    PetMedicalSafetyPolicy.java
    PostAssistantFactPolicy.java
    AiOutputSafetyPolicy.java
    PromptFactory.java
  dto/
    AiConversationCreateRequest.java
    AiConversationResponse.java
    AiMessageCreateRequest.java
    AiMessageResponse.java
    PostAssistantRequest.java
    PostAssistantResponse.java
    AiAnalysisCreateRequest.java
    AiAnalysisReportResponse.java
    AiUsageResponse.java
  provider/
    AiProviderClient.java
    AiProviderRequest.java
    AiProviderResponse.java
    AiProviderMessage.java
    AiProviderUsage.java
    AiProviderException.java
    AiProviderUnavailableException.java
    DisabledAiProviderClient.java
    deepseek/
      DeepSeekAiProviderClient.java
      DeepSeekRequest.java
      DeepSeekResponse.java
  service/
    AiConversationApplicationService.java
    AiCustomerService.java
    AiPetChatService.java
    AiPostAssistantService.java
    AiAnalysisApplicationService.java
    AiUsageApplicationService.java
```

说明：

- `deepseek/DeepSeekAiProviderClient` 属于阶段 8B，在 D-008 决策完成前不能实现真实 HTTP 调用。
- Provider DTO 与业务 API DTO 必须分离。
- Prompt 模板集中在 `PromptFactory` 或同等组件，不散落在 Controller。
- 安全规则优先使用纯 Java 规则，便于直接单元测试。

## Provider 端口设计

建议 Provider 无关接口：

```java
public interface AiProviderClient {
    AiProviderResponse complete(AiProviderRequest request);
}
```

`AiProviderRequest` 只允许包含：

- 业务用途 `apiType`。
- Provider 模型名，由配置层提供，业务请求不能指定。
- `messages`。
- 已获批准的生成参数。
- 请求关联 ID。

禁止包含：

- API Key。
- Base URL。
- `userId`、`adminId` 等身份控制字段。
- SQL 或数据库连接信息。
- 任意工具或函数调用定义。
- 客户端传入的模型名。

`AiProviderResponse` 至少包含：

- 助手文本。
- Provider 返回的模型名。
- `promptTokens`。
- `completionTokens`。
- `totalTokens`。
- Provider 请求 ID，如果存在且不敏感。

## Provider 启用和失败行为

建议增加显式开关：

```text
AI_PROVIDER_ENABLED
```

规则：

- 默认关闭。
- 关闭时使用 `DisabledAiProviderClient`。
- 关闭时应用可以正常启动，非 AI 业务不受影响。
- 调用 AI API 时返回 `503`，错误码 `ai_provider_not_enabled`。
- 开启真实 Provider 时，API Key、模型和所有已决定的必填 Provider 配置缺失必须快速失败。
- Provider 调用失败不能回滚已经完成的其他业务流程。
- Provider 失败必须写一条脱敏后的失败用量日志。

在 D-008 未决前，配置文件只能出现无默认值占位：

```yaml
petcare:
  ai:
    provider-enabled: ${AI_PROVIDER_ENABLED:false}
    deepseek:
      base-url: ${DEEPSEEK_BASE_URL:https://api.deepseek.com}
      api-key: ${DEEPSEEK_API_KEY:}
      model: ${DEEPSEEK_MODEL:}
      connect-timeout: ${DEEPSEEK_CONNECT_TIMEOUT:}
      read-timeout: ${DEEPSEEK_READ_TIMEOUT:}
      max-tokens: ${DEEPSEEK_MAX_TOKENS:}
      max-retries: ${DEEPSEEK_MAX_RETRIES:}
```

注意：

- 上述字段只是配置占位，不代表已决定默认值。
- 流式开关和限流配置必须等待用户决策后再加入。
- 不允许把真实 Key 写入 `application.yml`、`.env.example`、测试或文档。

## DeepSeek 适配器边界

阶段 8B 实现时必须遵守：

- 使用 OpenAI 兼容 Chat Completions 请求形状。
- 使用 `Authorization: Bearer <DEEPSEEK_API_KEY>`。
- Base URL 可配置。
- 业务层不感知 DeepSeek 专用 DTO。
- Provider 错误映射为内部稳定异常。
- `401`、`402`、`422`、`429`、`5xx` 等上游错误不能原样透传。
- 只对用户批准可重试的错误执行有限重试。
- 不在应用日志中输出请求消息全文、Authorization Header 或原始响应体。
- 真实调用只用于人工验收，不进入默认自动化测试。

官方协议参考：

- `https://api-docs.deepseek.com/`
- `https://api-docs.deepseek.com/api/create-chat-completion`

## 身份和资源归属边界

当前真实微信登录仍未启用。

用户侧要求：

- Service 方法设计为 `createConversation(currentUserId, request)`、`sendMessage(currentUserId, conversationId, request)` 等形式。
- Controller 不能从请求体读取 `userId`。
- 没有当前用户身份时返回 `401`。
- 用户只能查看和继续自己的会话。
- 测试可以直接调用 Service 或使用测试专用安全上下文。
- 不新增生产模拟登录、测试登录或后门 Header。

管理员侧要求：

- `adminId` 来自管理员 JWT 安全上下文。
- 生成分析报告需要 `ai:analysis:generate`。
- 查看用量日志需要 `ai:usage:read`。
- 读取分析报告至少需要 `analytics:dashboard:read`。
- 生成分析报告成功和失败都必须写入 `admin_operation_log`。

## AI 会话和消息规则

现有会话类型：

```text
CUSTOMER_SERVICE
PET_CHAT
ADMIN_ANALYSIS
```

规则：

- 用户只能创建 `CUSTOMER_SERVICE` 或 `PET_CHAT` 会话。
- 管理员分析由分析报告 API 创建，不允许用户创建 `ADMIN_ANALYSIS` 会话。
- `ai_conversation` 中用户会话设置 `user_id`，`admin_id` 为空。
- 管理员分析会话设置 `admin_id`，`user_id` 为空。
- 同一会话不能同时属于用户和管理员。
- 会话类型创建后不能改变。
- 用户消息和最终安全助手消息写入 `ai_message`。
- 系统 Prompt 和完整业务上下文每次动态构造，不写入 `ai_message`。
- Provider 失败时保留用户消息，但不能写入伪造助手成功消息。
- 会话历史必须限制读取数量，具体数量受 D-008 Token 决策影响，未决定前不能写死生产值。

由于 `ai_conversation` 没有 `CONTENT_GENERATE` 类型：

- AI 发帖辅助使用无会话的单次请求。
- 发帖辅助只写 `ai_usage_log`，不创建虚假的会话类型。
- 不修改 Schema 来新增会话类型，除非用户后续批准。

## 用量日志规则

每次 Provider 调用尝试都必须写入 `ai_usage_log`。

成功日志：

- 记录安全上下文中的 `user_id` 或 `admin_id`。
- 记录 `api_type`。
- 记录 Provider 实际返回的模型名。
- 记录 Token 用量。
- `success = 1`。
- `error_message = null`。

失败日志：

- `success = 0`。
- Token 不可获得时保留为 `null`。
- `error_message` 只保存内部脱敏错误码和简短摘要。
- 禁止保存 API Key、Header、完整 Prompt、完整用户消息、Provider 原始错误体或堆栈。

建议 `api_type`：

```text
CUSTOMER_SERVICE
CHAT
CONTENT_GENERATE
ANALYSIS
```

## AI 客服上下文接地

AI 客服数据来源只允许：

- 当前单门店 `store`。
- `store_config`。
- 上架的 `service_item`。
- 上架的 `product`。
- 启用的 `faq_knowledge`。

`CustomerServiceContextBuilder` 负责读取并整理这些数据。

传给 Provider 的上下文必须：

- 使用明确结构，区分系统规则、可信事实和用户问题。
- 标明每条事实的类型和来源 ID。
- 不包含数据库连接、SQL、内部权限、密码、手机号或其他无关隐私。
- 对数量和文本设置边界，避免把整个数据库内容塞入 Prompt。
- 只包含当前问题可能相关的事实。

接地规则：

- 没有可信事实可回答时，不调用 Provider，返回固定兜底：“当前资料中没有该信息，请联系门店确认。”
- 价格、库存、营业时间、服务半径和取消规则只能来自上下文。
- Provider 输出如果声称上下文中不存在的关键业务事实，必须被拒绝或替换为固定兜底。
- API 响应建议返回 `sourceFacts` 摘要，便于前端展示回答依据，但不能暴露内部字段。
- 模型输出不能更新 FAQ、商品、服务或门店配置。

建议测试类：

```text
CustomerServiceContextBuilderTest
CustomerServiceGroundingPolicyTest
AiCustomerServiceTest
```

## 宠物陪伴安全边界

必须建立确定性高风险症状规则，至少覆盖：

```text
呕吐
抽搐
便血
中毒
呼吸困难
长期不吃不喝
误食异物
严重外伤
高烧
骨折
```

处理顺序：

1. 校验输入长度和内容。
2. 在调用 Provider 前运行 `HighRiskSymptomDetector`。
3. 命中高风险症状时，不调用 Provider，直接返回固定就医建议。
4. 未命中时，构造禁止诊断、开药、承诺疗效的系统规则。
5. Provider 返回后运行 `PetMedicalSafetyPolicy`。
6. 输出包含诊断、处方、自行用药或治疗承诺时，替换为安全拒答和就医建议。

固定安全响应必须：

- 明确不能提供诊断或处方。
- 建议尽快联系正规宠物医院或急诊。
- 高风险时提示保持宠物安全并携带误食物、药品包装或症状记录。
- 不承诺具体治疗结果。

禁止完全依赖 Prompt 来实现医疗安全。

建议测试类：

```text
HighRiskSymptomDetectorTest
PetMedicalSafetyPolicyTest
AiPetChatServiceTest
```

## AI 发帖辅助规则

API 只接收用户明确提供的事实，例如：

- 宠物称呼。
- 宠物类型。
- 发生的事件。
- 用户希望的语气。
- 用户原始文案。

规则：

- 不读取或推断用户未提供的宠物疾病、年龄、品种、地点或服务体验。
- 不自动发布帖子。
- 不自动改变帖子审核状态。
- 生成内容仍需经过阶段 6 的敏感词和内容审核流程。
- 输入和输出都要经过长度校验。
- Provider 输出命中严重敏感词时不得返回可发布文案。
- AI 生成结果必须标记为建议稿，由用户确认后再提交发帖接口。

由于当前文件上传仍有未决事项，发帖辅助不处理图片。

建议测试类：

```text
PostAssistantFactPolicyTest
AiPostAssistantServiceTest
```

## 管理端 AI 分析

管理端分析必须采用两步结构：

```text
后端参数化 SQL 聚合
  -> 固定结构的统计快照
  -> AI 解释和建议
  -> 保存 ai_analysis_report
```

禁止结构：

```text
管理员问题
  -> AI 生成 SQL
  -> 系统执行 SQL
```

支持的报告类型：

```text
BUSINESS
COMMUNITY
SALES
ACTIVITY
```

聚合器职责：

- `BusinessAnalyticsAggregator`：预约数量、完成量、取消量、忙碌时段等。
- `CommunityAnalyticsAggregator`：帖子、评论、举报和话题聚合。
- `SalesAnalyticsAggregator`：订单、商品销量和金额聚合。
- `ActivityAnalyticsAggregator`：已有营销活动关联数据聚合；数据不足时明确返回不足，不编造 ROI。

聚合规则：

- 日期范围必须校验，不能由 AI 决定。
- Mapper SQL 必须参数化，禁止拼接用户输入。
- 聚合结果使用固定 DTO。
- 聚合结果序列化后写入 `raw_data_json`。
- Provider 只接收聚合 DTO，不接收 Mapper、表名、SQL 或数据库连接。
- AI 摘要和建议分别写入 `ai_summary`、`suggestions`。
- Provider 失败时不保存伪造成功报告；可以记录失败用量和管理员操作日志。
- AI 建议只能作为管理建议，不能自动修改排班、价格、库存或活动。

建议测试类：

```text
BusinessAnalyticsAggregatorTest
CommunityAnalyticsAggregatorTest
SalesAnalyticsAggregatorTest
AiAnalysisApplicationServiceTest
AdminAiAnalysisControllerTest
```

## 用户端 API 计划

### 创建会话

```text
POST /api/v1/ai/conversations
```

请求示例：

```json
{
  "conversationType": "CUSTOMER_SERVICE",
  "title": "咨询洗护预约"
}
```

规则：

- 只允许 `CUSTOMER_SERVICE`、`PET_CHAT`。
- 当前用户身份来自安全上下文。
- 返回 `201 Created`。

### 查询我的会话

```text
GET /api/v1/ai/conversations/my
GET /api/v1/ai/conversations/{id}
```

规则：

- 支持分页。
- 用户只能读取自己的会话。
- 详情可返回经过限制的消息列表。

### 发送消息

```text
POST /api/v1/ai/conversations/{id}/messages
```

请求示例：

```json
{
  "content": "洗澡服务需要提前多久预约？"
}
```

规则：

- 校验会话归属。
- 根据会话类型选择客服或宠物陪伴流程。
- 不能由请求体指定模型、系统 Prompt 或 Provider。
- Provider 禁用时返回 `503`。
- 高风险宠物症状可以不调用 Provider，直接返回固定安全响应。

### 发帖辅助

```text
POST /api/v1/ai/post-assistant/generate
```

请求示例：

```json
{
  "petName": "豆包",
  "petType": "DOG",
  "event": "今天第一次完成洗护",
  "tone": "轻松",
  "originalText": "豆包今天洗澡了"
}
```

规则：

- 只生成建议稿，不自动发布。
- 不允许客户端指定不存在的事实为“系统已确认事实”。
- 生成结果仍必须经过内容审核。

## 管理端 API 计划

### 生成分析报告

```text
POST /api/v1/admin/ai/analysis-reports
```

权限：

```text
ai:analysis:generate
```

请求示例：

```json
{
  "reportType": "BUSINESS",
  "startDate": "2026-06-01",
  "endDate": "2026-06-07"
}
```

规则：

- 日期范围必须合法。
- 后端选择对应聚合器。
- 管理员不能提交 SQL、Prompt 或模型名。
- 成功和失败都写管理员操作日志。

### 查询分析报告

```text
GET /api/v1/admin/ai/analysis-reports
GET /api/v1/admin/ai/analysis-reports/{id}
```

权限：

```text
analytics:dashboard:read
```

### 查询 AI 用量

```text
GET /api/v1/admin/ai/usage
```

权限：

```text
ai:usage:read
```

规则：

- 支持日期、API 类型、成功状态和分页筛选。
- 不返回完整 Prompt、完整响应或 Provider 原始错误。
- `STAFF` 默认不能访问。

## 输入校验和 Prompt 注入防护

所有 AI 输入必须：

- 使用 Bean Validation。
- 设置明确最大长度。
- 拒绝空白消息。
- 拒绝客户端传入系统 Prompt、模型名、Provider、工具或 SQL。
- 将用户内容作为不可信数据包裹，不与系统规则拼接成同一指令段。
- 在系统规则中明确忽略用户要求泄露 Prompt、密钥、内部配置或绕过安全边界的指令。

Prompt 构造必须区分：

```text
系统不可变规则
可信业务上下文
不可信用户输入
输出格式要求
```

Prompt 注入防护不能替代业务层授权和确定性安全规则。

## 错误码建议

在 `ErrorCode` 中新增：

```text
ai_provider_not_enabled
ai_provider_configuration_invalid
ai_provider_unavailable
ai_provider_timeout
ai_request_invalid
ai_conversation_not_found
ai_conversation_forbidden
ai_conversation_type_invalid
ai_output_rejected
ai_medical_safety_blocked
ai_grounding_context_missing
ai_analysis_range_invalid
ai_analysis_data_insufficient
```

HTTP 状态建议：

- `400`：请求格式或长度错误。
- `401`：用户或管理员未认证。
- `403`：会话不属于当前用户或管理员缺少权限。
- `404`：会话或报告不存在。
- `409`：会话类型或状态冲突。
- `422`：安全规则拒绝、数据不足或业务语义不合法。
- `429`：仅在用户决定限流策略并实现后使用。
- `503`：Provider 未启用、不可用或临时失败。

错误响应禁止包含：

- API Key。
- Authorization Header。
- Provider 原始错误体。
- 完整 Prompt。
- SQL。
- 堆栈。

## 事务边界

Provider 调用是慢速外部操作，不能放在持有数据库行锁的事务中。

建议流程：

1. 短事务校验会话归属并保存用户消息。
2. 事务结束。
3. 构造上下文并调用 Provider。
4. 运行输出安全规则。
5. 新短事务保存助手消息和成功用量日志。
6. Provider 失败时使用独立短事务保存失败用量日志。

管理端分析：

1. 只读聚合查询。
2. 聚合完成后调用 Provider。
3. 输出安全检查通过后，短事务保存分析报告。
4. 独立记录管理员操作日志和用量日志。

禁止在数据库锁定事务中调用 Provider。

## TDD 实施顺序

必须按 RED、GREEN、重构推进。

建议顺序：

1. 写 `AiProviderClientContractTest` 和 `DisabledAiProviderClientTest`。
2. 写 `AiProviderArchitectureTest`，阻止 Provider 依赖数据库。
3. 写 `HighRiskSymptomDetectorTest` 和 `PetMedicalSafetyPolicyTest`。
4. 写 `CustomerServiceContextBuilderTest` 和 `CustomerServiceGroundingPolicyTest`。
5. 写 `PostAssistantFactPolicyTest`。
6. 写会话归属、消息持久化和用量日志测试。
7. 写客服、宠物陪伴和发帖辅助 Service 测试，使用 Mock Provider。
8. 写经营数据聚合器测试，确认只输出固定聚合 DTO。
9. 写管理端分析报告和权限测试。
10. 写 Controller 的 `401`、`403`、`422`、`503` 测试。
11. 实现最小代码让阶段 8A 测试变绿。
12. 重构并运行完整验证。
13. 等待 D-008 决策后，再为阶段 8B 写真实 DeepSeek Adapter 的 RED 测试。

## 最低测试清单

Provider 层：

- Provider 可以被 Mock。
- 默认 Provider 禁用。
- Provider 禁用时返回安全 `503`。
- Provider 包不依赖数据库。
- Provider 失败不会泄露原始错误。
- 每次调用尝试都会写成功或失败用量日志。

AI 客服：

- 上下文只包含批准的数据来源。
- 没有可信上下文时不编造回答。
- 价格、库存、营业时间和服务半径回答基于上下文。
- 用户 Prompt 注入不能修改系统规则或请求工具调用。

宠物陪伴：

- 每个高风险症状都触发固定就医建议。
- 命中高风险症状时不调用 Provider。
- 诊断、处方、自行用药和治疗承诺输出被阻止。

发帖辅助：

- 不编造用户未提供事实。
- 不自动发布帖子。
- 生成内容仍经过敏感词审核。

会话和资源归属：

- 用户不能读取或继续其他用户会话。
- 用户不能创建 `ADMIN_ANALYSIS` 会话。
- 请求体不能指定 `userId`、`adminId`、模型或系统 Prompt。
- Provider 失败时不写伪造助手消息。

管理端分析：

- AI 只接收固定聚合 DTO。
- 不执行 AI 生成 SQL。
- 日期范围非法时拒绝。
- 数据不足时不编造报告。
- `STAFF` 不能生成或查看管理端 AI 分析。
- 缺少 `ai:analysis:generate` 返回 `403`。
- 缺少 `ai:usage:read` 返回 `403`。
- 生成报告成功和失败都写管理员操作日志。

## 测试策略

默认自动化测试必须使用 Mock/Fake Provider：

```text
MockAiProviderClient
```

要求：

- 测试不访问互联网。
- 测试不需要真实 `DEEPSEEK_API_KEY`。
- Mock Provider 可以固定返回内容、Token 用量和错误。
- 安全规则测试不能依赖随机模型输出。
- 真实 DeepSeek 调用只能作为阶段 8B 的显式人工验收命令，不能进入默认 `mvn test`。

阶段 8A 验证命令：

```powershell
mvn test
mvn clean package
git diff --check
git status --short --branch
```

阶段 8B 决策完成后的人工验收必须单独记录：

```text
真实 Provider 验证命令：
使用模型：
超时与重试配置：
最大 Token：
是否流式：
限流配置：
验证结果：
是否确认没有密钥和原始响应泄露：
```

## 实施顺序

### 阶段 8A

1. 新增 Provider 端口、请求响应模型和禁用实现。
2. 新增 Provider 架构边界测试。
3. 新增高风险症状检测和输出医疗安全规则。
4. 新增客服可信上下文构造和接地规则。
5. 新增发帖辅助事实约束。
6. 新增会话、消息和用量日志业务服务。
7. 新增用户端 AI API。
8. 新增经营数据聚合器。
9. 新增管理端分析报告和用量 API。
10. 加上管理员权限和操作日志。
11. 完成 Mock Provider 集成测试。
12. 运行完整验证并提交。

### 阶段 8B

只有用户完成 D-008 决策后才允许：

1. 先更新 `docs/08-pending-decisions.md` 和本文件。
2. 写真实 DeepSeek Adapter 契约和错误映射测试。
3. 实现配置校验和真实 HTTP Client。
4. 实现用户批准的超时、重试、Token、限流和流式策略。
5. 完成显式真实 Provider 验收。
6. 运行安全复核并提交独立 Git 提交。

## Git 提交建议

阶段 8A 建议拆成多个小提交：

```powershell
git add src/test/java/com/petcare/ai
git commit -m "test: add ai provider and safety contract tests"

git add src/main/java/com/petcare/ai/provider src/main/java/com/petcare/ai/domain src/test/java/com/petcare/ai
git commit -m "feat: add ai provider boundary and safety policies"

git add src/main/java/com/petcare/ai src/test/java/com/petcare/ai
git commit -m "feat: add grounded user ai services"

git add src/main/java/com/petcare/ai src/test/java/com/petcare/ai
git commit -m "feat: add admin ai analysis reports"
```

如果只提交计划文档，使用：

```powershell
git add AGENTS.md README.md docs/02-task-breakdown.md docs/03-glm5-implementation-plan.md docs/07-integration-gates.md docs/08-pending-decisions.md docs/17-phase-8-ai-provider-functions-plan.md
git commit -m "docs: add phase 8 ai provider plan"
```

提交前必须确认：

- 没有真实 API Key。
- 没有写死未决模型、超时、重试、Token、限流或流式配置。
- Provider 层没有数据库依赖。
- 没有 AI 生成并执行 SQL。
- 没有把 AI 输出直接写入核心业务状态。
- 没有 Redis、Flyway、Liquibase。
- 没有前端代码。
- 没有通过请求体 `userId` 或 `adminId` 绕过认证。
- `git diff --check` 通过。

## 阶段 8 交接报告格式

完成后必须按以下格式汇报：

```text
任务：阶段 8 AI Provider 与 AI 功能
阶段：phase-8-ai
分支：phase-8-ai
提交：
已完成：
未完成：
变更文件：
验证命令：
验证结果：
覆盖率：
阶段 8A 状态：
阶段 8B 状态：
Provider Mock 验证：
Provider 数据库隔离验证：
客服上下文接地验证：
宠物医疗安全验证：
发帖事实约束验证：
管理端聚合分析验证：
后台权限验证：
密钥和错误脱敏验证：
已知风险：
待决策事项：
下一步允许执行的任务：
```

## 阶段 8 退出标准

阶段 8A 退出标准：

- Provider 端口和业务代码解耦。
- 默认禁用 Provider，不伪造 AI 成功响应。
- Provider 可以被 Mock。
- Provider 层不能访问数据库。
- 会话、消息和用量日志规则通过测试。
- AI 客服使用后端可信上下文。
- 无可信上下文时不会编造关键业务事实。
- 高风险宠物症状由确定性规则直接建议就医。
- 诊断、处方、自行用药和治疗承诺输出被阻止。
- 发帖辅助不自动发布，且不编造用户未提供事实。
- 管理端分析只使用后端聚合 DTO。
- AI 不生成并执行 SQL。
- 管理端 AI 权限和操作日志通过测试。
- Provider 错误和日志不泄露密钥、Prompt、原始响应或堆栈。
- `mvn test` 通过。
- `mvn clean package` 通过。
- `git diff --check` 通过。
- 已提交 Git。

阶段 8B 退出标准：

- D-008 所有未决项已由用户决定并更新文档。
- 真实 DeepSeek Adapter 已按决定实现。
- 真实 Provider 调用已显式验证。
- 超时、重试、Token、限流和流式策略与用户决定一致。
- 不存在真实密钥提交。
- 安全复核通过。

在阶段 8B 未完成前：

- 可以进入不依赖真实 AI Provider 的后续后台 API 开发。
- 不能声称真实 DeepSeek 已接入或生产 AI 功能已可用。
