# 阶段 11-05 Review 结论与 11-05R 修复任务书

日期：2026-06-13

状态：11-04R Review 通过；11-05 首轮编码 Review 未通过

执行对象：GLM5.1

目标分支：`phase-11-user-prerequisites`

## 1. Review 结论

11-04R 已关闭地址默认值写入失败、禁用用户错误语义、事务回滚和真实 MySQL 并发验证问题，可以关闭。

11-05 已完成真实公开 GET 白名单、公开目录过滤、社区公开 DTO 隔离和无效 Bearer Token 拒绝。但当前认证过滤器会把“已携带但不是 Bearer 的 Authorization 请求头”当作未携带凭据，使公开 GET 降级为匿名访问。这与 `docs/41-phase-11-05-glm5-public-read-access-brief.md` 的身份边界和失败关闭原则冲突。

当前禁止开始 11-06，下一项只能执行 `11-05R`。

## 2. 已确认通过的范围

- 11-04R 的 HIGH/MEDIUM Findings 已关闭。
- 地址默认值事务回滚测试和真实 MySQL 并发测试已建立。
- 公开 GET 使用 `HttpMethod.GET` 和真实路径逐项放行，没有使用 `/api/v1/**` 过宽匿名规则。
- 服务、商品、话题、帖子和评论公开查询按公开状态过滤。
- 社区公开响应不暴露 `userId`、`petId`、内部状态、风险或审核信息。
- 格式错误或过期的 Bearer Token 访问公开 GET 返回 401。
- 合法 USER/ADMIN Token 访问公开 GET 保持可用。

工作区仅存在未跟踪 `.claude/`，禁止修改、暂存或提交。

## 3. Review Finding

### MEDIUM-1：非 Bearer Authorization 请求头降级为匿名

证据：

```java
if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
    filterChain.doFilter(request, response);
    return;
}
```

当前行为：

- 无 `Authorization` 请求头：按匿名请求继续，符合预期。
- `Authorization: Bearer <合法令牌>`：按认证身份继续，符合预期。
- `Authorization: Bearer <无效令牌>`：返回 401，符合预期。
- `Authorization: Basic abc`、`Token abc` 或其他已存在但非 Bearer 的值：按匿名请求继续，公开 GET 返回 200，不符合预期。

影响：

- 客户端携带错误认证方案时不会得到认证失败，容易掩盖凭据配置错误。
- 认证失败语义依赖目标接口是否公开，同一个无效凭据在不同路径表现不一致。
- 违反“已提交凭据必须验证成功，否则失败关闭”的安全边界。

本问题不会使私有资源变为公开，因此定级为 MEDIUM；但未修复前 11-05 不得通过。

## 4. 11-05R 目标

严格区分“没有提交凭据”和“提交了无效凭据”：

| 请求头状态 | 预期行为 |
|---|---|
| 没有 `Authorization` 请求头 | 允许按匿名身份继续，由 Spring Security 决定目标路径是否可访问 |
| 合法 Bearer Token | 建立对应身份并继续 |
| 已存在但方案不是 Bearer | 返回 401，停止过滤器链 |
| Bearer 缺少值、空白值或格式错误 | 返回 401，停止过滤器链 |
| Bearer 签名错误、过期、类型未知或主体失效 | 返回 401，停止过滤器链 |

`authenticationEntryPoint` 写出 401 后不得再次调用 `filterChain.doFilter(...)`。

## 5. 允许修改范围

```text
src/main/java/com/petcare/common/security/JwtAuthenticationFilter.java
src/test/java/com/petcare/common/security/PublicAccessIntegrationTest.java
src/test/java/com/petcare/common/security/SecurityAccessTest.java
```

只有测试证明全局错误响应契约无法稳定表达 401 时，才允许最小修改直接相关安全测试夹具。不得扩大到其他业务模块。

## 6. 禁止修改

- `.claude/` 下任何文件。
- `SecurityConfig` 匿名白名单和已批准的公开路径。
- JWT 签发规则、密钥、有效期和 Token Claim。
- 用户、管理员、公开 DTO、目录查询和社区业务。
- Schema、前端、文件上传、AI Provider 和 Redis。
- 通过允许非 Bearer 请求头或删除断言来获得绿色结果。
- 提前实施 11-06。

## 7. 强制 TDD 顺序

### RED-1：请求头状态矩阵

先在 `PublicAccessIntegrationTest` 增加公开 GET 测试：

1. 无 `Authorization` 请求头返回 200。
2. `Authorization: Basic abc` 返回 401。
3. `Authorization: Token abc` 返回 401。
4. 空值、仅空白、`Bearer`、`Bearer ` 和空白 Token 返回 401。
5. 格式错误和过期 Bearer Token 返回 401。
6. 合法 USER/ADMIN Bearer Token 返回 200。

如果 MockMvc 无法发送某种非法 Header 值，必须记录限制并使用最接近的合法 HTTP 表达验证，不能删除整个类别。

### GREEN-1：最小过滤器修复

- 仅当请求头完全不存在时走匿名路径。
- 请求头存在时先验证认证方案和 Token 非空，再进入解析。
- 任一失败统一交给现有 `RestAuthenticationEntryPoint` 返回 401。
- 失败响应不泄露解析异常、主体状态或内部信息。

### RED/GREEN-2：受保护接口回归

- 无 Token 访问私有/管理接口仍返回 401。
- 非 Bearer 请求头访问私有/管理接口返回 401。
- 合法 USER Token 不获得管理员权限。
- 合法 ADMIN Token 不改变现有权限判断。

## 8. 强制验证

```powershell
mvn "-Dtest=PublicAccessIntegrationTest,SecurityAccessTest" test
mvn "-Dtest=PublicCommunityReadIntegrationTest,ServiceCatalogControllerTest,ProductCatalogControllerTest,AddressControllerTest,PetControllerTest" test
mvn test
mvn clean package "-DskipTests"
mvn -Ptc-mysql clean test
git diff --check
git status --short --branch
```

人工检查：

```powershell
rg -n "authHeader|BEARER_PREFIX|filterChain.doFilter|authenticationEntryPoint" src/main/java/com/petcare/common/security/JwtAuthenticationFilter.java
rg -n "Basic|Token|Bearer|isUnauthorized|isOk" src/test/java/com/petcare/common/security/PublicAccessIntegrationTest.java
```

## 9. 建议提交顺序

```text
test: reject malformed authorization schemes on public reads
fix: fail closed for invalid authorization headers
```

禁止 `git add .`，不得暂存 `.claude/`。

## 10. 退出门禁

- MEDIUM-1 已关闭。
- 只有真正缺少 Authorization 请求头时才允许匿名路径。
- 任意已提交但无效的 Authorization 请求头返回 401，且不继续过滤器链。
- 公开 GET、私有接口、管理接口和合法角色行为均无回归。
- 定向、完整、构建、MySQL 和 `git diff --check` 全部通过。
- `.claude/` 未进入提交。

全部通过并完成 Review 后，才允许执行 `docs/43-phase-11-06-glm5-demo-seed-data-brief.md`。

## 11. 强制交接格式

```text
任务：11-05R Authorization 失败关闭修复
阶段：11
分支：
提交：
Review Finding 关闭情况：
已完成：
未完成：
变更文件：
RED 证据：
验证命令：
验证结果：
覆盖率：
请求头状态矩阵：
受保护接口回归：
.claude/ 处理情况：
已知风险：
待决策事项：
下一步允许执行的任务：门禁与 Review 通过后仅允许 11-06
```
