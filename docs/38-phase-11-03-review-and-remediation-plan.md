# 阶段 11-03 Review 结论与修复任务书

日期：2026-06-13

状态：11-03R Review 已通过，任务关闭

执行对象：GLM5.1

目标分支：`phase-11-user-prerequisites`

基线提交：

```text
7152886 test: define current user pet profile contract
143f5c7 feat: add current user pet create, list, detail, update and delete api
```

## 1. Review 结论

11-02R 已关闭 ACTIVE 用户边界和手机号隐私 Findings，可以关闭。

11-03 已实现宠物档案 CRUD、角色限制、资源归属、稳定排序和逻辑删除，API 与真实 `Pet` Schema 基本一致。但创建流程没有检查数据库写入返回值，可能在写入失败时仍返回成功响应，违反 `docs/37-phase-11-03-glm5-pet-profile-api-brief.md` 的退出门禁。

历史结论为禁止直接开始 11-04；当前 11-03R 已通过 Review，本任务已关闭。

## 2. 已验证结果

```text
定向回归：146 项通过
完整 mvn test：通过
构建：mvn clean package "-DskipTests" 通过
真实 MySQL：16 项通过
```

工作区注意事项：

- `.claude/` 为未跟踪外部状态，不属于本任务。
- 禁止修改、暂存或提交 `.claude/`。

## 3. Review Findings

### HIGH-1：创建写入失败时可能伪造成功

证据：

- `PetApplicationServiceImpl.createCurrentUserPet` 调用 `petService.save(pet)` 后未检查布尔返回值。
- 当 `save` 返回 `false` 且未抛异常时，服务仍执行 `PetResponse.from(pet)`。
- 当前测试没有覆盖 `petService.save(...) == false` 的路径。

影响：

- 客户端可能收到 `201 Created`，但数据库中不存在对应宠物。
- 返回 DTO 中可能包含由 MyBatis-Plus 预分配的 ID，使伪成功更难被发现。
- 后续预约或社区流程使用该 `petId` 时会失败。

必须修复：

- 创建时检查 `petService.save(pet)` 返回值。
- 返回 `false` 时抛出 `IllegalStateException` 或等价的非业务运行时异常，由全局异常处理器返回 500 `internal_error`，不得返回宠物 DTO。
- 不向客户端暴露 SQL、堆栈或底层数据库错误。
- 增加独立服务单元测试，使用 Mock 验证 `save=false` 时抛错且不返回成功。

### MEDIUM-1：服务层必填字段失败语义不稳定

证据：

- `validatePetRequest` 直接执行 `VALID_TYPES.contains(request.type())`。
- `setPetFields` 和更新 Wrapper 直接执行 `request.name().trim()`。
- Controller 的 Bean Validation 可以保护 HTTP 路径，但应用服务被内部代码直接调用时，空请求、空名称或空类型可能产生 `NullPointerException`，而不是稳定的 `validation_error`。

影响：

- 服务层直接调用与 HTTP 调用错误契约不一致。
- 将来复用服务时可能产生未预期的 500 和堆栈日志。

必须修复：

- 服务层显式校验 `request != null`、`name` 非空白、`type` 非空。
- 非法值统一抛出 `BusinessException(ErrorCode.VALIDATION_ERROR, ...)`。
- 增加服务层直接调用测试，覆盖空请求、空名称和空类型。

### LOW-1：URL 协议大小写处理未使用固定 Locale

`validateAndNormalizeAvatarUrl` 使用无参数 `toLowerCase()`。建议改为 `toLowerCase(Locale.ROOT)`，避免特殊系统 Locale 下行为变化。本项可随 11-03R 修复，但不单独阻塞。

## 4. 下一项可执行任务

任务编号：`11-03R`

目标：关闭宠物创建伪成功和服务层必填字段失败语义问题。

## 5. 允许修改范围

```text
src/main/java/com/petcare/user/service/impl/PetApplicationServiceImpl.java
src/test/java/com/petcare/user/service/PetApplicationServiceImplTest.java
src/test/java/com/petcare/user/service/PetApplicationServiceImplUnitTest.java（可新增）
```

仅在验证 HTTP 错误契约确有必要时允许修改：

```text
src/test/java/com/petcare/user/controller/PetControllerTest.java
```

## 6. 禁止修改

- `.claude/` 下任何文件。
- Schema、实体、Mapper 和现有 API 路径。
- 用户资料、JWT、管理员认证和测试登录。
- 地址、预约、社区、商品、AI、文件上传和前端。
- 与 Findings 无关的重构。

## 7. 强制 TDD 顺序

### RED-1：创建写入失败

先写独立服务单元测试：

1. Mock `PetService.save(...)` 返回 `false`。
2. 调用 `createCurrentUserPet(...)`。
3. 断言抛出 `IllegalStateException` 或等价的非业务运行时异常。
4. 断言没有返回 DTO，且只调用一次 `save`。
5. Controller 集成测试断言该失败路径返回 500 `internal_error`，且不泄露底层异常。

### GREEN-1：拒绝伪成功

- 检查 `save` 返回值。
- `false` 时抛出安全错误。
- 正常保存行为保持不变。

### RED-2：服务层必填字段

先写服务层直接调用测试：

```text
request == null -> validation_error
name == null/blank -> validation_error
type == null -> validation_error
```

### GREEN-2 与重构

- 在服务入口统一完成稳定校验。
- 可将 URL 小写处理改为 `Locale.ROOT`。
- 不改变现有 DTO、字段和 HTTP 成功契约。

## 8. 强制验证

```powershell
mvn "-Dtest=PetControllerTest,PetApplicationServiceImplTest,PetApplicationServiceImplUnitTest,UserProfileControllerTest,UserProfileServiceImplTest,TestLoginControllerTest,SecurityAccessTest" test
mvn test
mvn clean package "-DskipTests"
mvn -Ptc-mysql clean test
git diff --check
git status --short --branch
```

如果未新增 `PetApplicationServiceImplUnitTest`，从定向命令中移除该类名。

## 9. 建议提交

```text
test: expose pet profile write failure contract
fix: reject failed pet profile writes
```

禁止 `git add .`，必须显式暂存文件。

## 10. 退出门禁

- HIGH 和 MEDIUM Findings 全部关闭。
- `save=false` 时不返回 `201` 或宠物 DTO。
- 服务层空请求、空名称和空类型稳定返回 `validation_error`。
- 现有授权、归属和逻辑删除测试不回归。
- 定向、完整、构建、MySQL 和 `git diff --check` 全部通过。
- `.claude/` 未进入提交。

全部通过后，才允许执行 `docs/39-phase-11-04-glm5-address-api-brief.md`。

## 11. 强制交接格式

```text
任务：11-03R 宠物档案 Review 修复
阶段：11
分支：
提交：
Review Findings 关闭情况：
已完成：
未完成：
变更文件：
RED 证据：
验证命令：
验证结果：
覆盖率：
写入失败检查：
.claude/ 处理情况：
已知风险：
待决策事项：
下一步允许执行的任务：门禁通过后仅允许 11-04
```
