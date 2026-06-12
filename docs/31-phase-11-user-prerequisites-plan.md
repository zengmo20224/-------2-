# 阶段 11：小程序前置后端能力实施计划

日期：2026-06-12

## 1. 文档目的

本文档是阶段 11 的详细实施计划，规定任务包执行顺序、文件范围、实现要求、测试要求和验证命令。

执行时必须同时遵守：

- `AGENTS.md`
- `docs/08-pending-decisions.md`
- `docs/21-remaining-development-roadmap.md`
- `docs/22-continuous-agent-development-rules.md`
- `docs/30-user-miniapp-frontend-design-spec.md`

## 2. 决策依赖状态

以下阶段 11 相关决策已经获得用户明确批准：

| 决策 | 状态 | 对阶段 11 影响 |
|------|------|----------------|
| D-006 微信登录 | 已决定（存根） | 不接入真实微信，使用 D-013 替代 |
| D-007 文件上传 | 已决定 | 阶段 14A 按决定实施，不在阶段 11 提前实现 |
| D-008 AI Provider | 已决定 | 阶段 14B 按决定实施，不在阶段 11 提前实现 |
| D-013 用户端 E2E 认证 | 已决定 | 仅 `test` Profile 启用测试登录端点 |
| D-014 公开读取策略 | 已决定 | 公开内容 GET 允许匿名读取 |

## 3. 建议分支

`phase-11-user-prerequisites`，从 `phase-10-frontend` 的最新提交创建。

## 4. 任务包执行顺序

阶段 11 有 8 个任务包，以下按依赖顺序排列。无依赖关系的任务包可以并行。

```text
11-01 测试环境测试登录端点
├── 11-02 用户资料 API
│   ├── 11-03 宠物档案 API
│   └── 11-04 地址管理 API
├── 11-06 演示种子数据
└── 11-07 补齐后台 API

11-05 公开读取策略（可与 11-01 并行）

11-08 API 接口清单（依赖 11-01 至 11-07 全部完成）
```

## 5. 任务包详细规格

### 11-01：测试环境测试登录端点

**状态**：已批准，可按 TDD 实施。

**目标**：仅为自动化测试环境提供用户身份获取能力。

**允许修改**：

- `src/main/java/com/petcare/auth/` 下新增或修改的认证相关文件
- `src/main/java/com/petcare/user/` 下用户查询相关文件
- `src/main/java/com/petcare/common/config/SecurityConfig.java`
- 对应测试文件

**禁止修改**：

- 管理后台前端
- 小程序前端
- 管理员认证逻辑
- `schema.sql`

**实现要求**：

1. 新增 `TestLoginController`，端点 `POST /api/v1/auth/test-login`。
2. 请求体：`{ "phone": "13800138001" }`。
3. 行为：只允许按手机号查找预定义测试种子用户并签发 JWT；用户不存在时明确失败，禁止自动创建。
4. 使用 `@Profile("test")` 注解，确保 `dev`、`prod` 和未指定 Profile 时不存在此端点。
5. Spring Security 配置放行此端点，仅在 `test` Profile 相关配置加载时生效。
6. 测试种子用户必须可重复初始化和清理，不包含生产秘密。

**D-013 安全约束**：

- 非 `test` 环境绝对不能暴露此端点。
- `dev`、`prod` 和未指定 Profile 时该 Controller 不加载（不是返回 403，是完全不可达）。
- 测试使用种子数据中定义的预设手机号。
- 禁止通过测试登录端点任意创建用户。

**RED 测试**：

- `test` profile 下，使用预设手机号能获得合法 JWT。
- `test` profile 下，不存在的手机号登录失败且不会创建用户。
- 非 `test` profile 下，该端点不可达（返回 404）。
- 获得的 JWT 能通过 `/api/v1/user/profile` 等用户接口验证。

**完成验证**：

```powershell
mvn test -pl . -Dtest="TestLoginControllerTest"
mvn test
git diff --check
```

---

### 11-02：用户资料 API

**目标**：实现用户个人资料的查看和修改接口。

**依赖**：11-01（需要用户身份体系）

**允许修改**：

- `src/main/java/com/petcare/user/` 下文件
- `src/main/java/com/petcare/common/config/SecurityConfig.java`
- 对应测试文件

**禁止修改**：

- 前端文件
- 管理员相关代码
- `schema.sql`

**实现要求**：

1. `GET /api/v1/user/profile`：返回当前登录用户资料。
2. `PUT /api/v1/user/profile`：修改当前登录用户资料（昵称、头像 URL）。
3. 从 JWT 中提取用户 ID，不允许通过请求体指定。
4. 资源归属校验：用户只能查看和修改自己的资料。

**DTO 设计**：

```java
// 响应
UserProfileResponse {
    String userId;
    String nickname;
    String phone;
    String avatarUrl;
    LocalDateTime createdAt;
}

// 修改请求
UpdateProfileRequest {
    @NotBlank String nickname;
    String avatarUrl;  // 可选；真实上传仍在阶段 14A 实现
}
```

**RED 测试**：

- 用户能查看自己的资料。
- 用户能修改自己的昵称。
- 用户 A 不能通过修改 ID 查看用户 B 的资料。
- 未认证请求返回 401。
- 昵称为空返回 422。

**完成验证**：

```powershell
mvn test -pl . -Dtest="UserProfileControllerTest"
mvn test
git diff --check
```

---

### 11-03：宠物档案 API

**目标**：实现用户宠物档案的 CRUD 接口。

**依赖**：11-02

**允许修改**：

- `src/main/java/com/petcare/user/` 下宠物相关文件
- 对应测试文件

**禁止修改**：

- 前端文件
- 用户资料相关代码
- `schema.sql`

**实现要求**：

1. `GET /api/v1/user/pets`：当前用户的宠物列表。
2. `POST /api/v1/user/pets`：新增宠物。
3. `PUT /api/v1/user/pets/{petId}`：修改宠物。
4. `DELETE /api/v1/user/pets/{petId}`：删除宠物（软删除）。
5. 资源归属校验：只能操作自己的宠物。

**DTO 设计**：

```java
// 响应
PetResponse {
    String petId;
    String name;
    String species;     // DOG, CAT, OTHER
    String breed;
    String gender;      // MALE, FEMALE, UNKNOWN
    LocalDate birthday;
    Double weight;
    String notes;
}

// 创建/修改请求
PetRequest {
    @NotBlank String name;
    @NotNull String species;
    String breed;
    String gender;
    LocalDate birthday;
    Double weight;
    String notes;
}
```

**RED 测试**：

- 用户能创建宠物，返回包含自己的 userId。
- 用户能列出自己的宠物。
- 用户能修改自己的宠物。
- 用户能删除自己的宠物。
- 用户 A 不能查看/修改/删除用户 B 的宠物，返回 403 或 404。
- 必填字段缺失返回 422。

**完成验证**：

```powershell
mvn test -pl . -Dtest="PetControllerTest"
mvn test
git diff --check
```

---

### 11-04：地址管理 API

**目标**：实现用户地址的 CRUD 接口。

**依赖**：11-02

**允许修改**：

- `src/main/java/com/petcare/user/` 下地址相关文件
- 对应测试文件

**禁止修改**：

- 前端文件
- 宠物相关代码
- `schema.sql`

**实现要求**：

1. `GET /api/v1/user/addresses`：当前用户的地址列表。
2. `POST /api/v1/user/addresses`：新增地址。
3. `PUT /api/v1/user/addresses/{addressId}`：修改地址。
4. `DELETE /api/v1/user/addresses/{addressId}`：删除地址。
5. 资源归属校验：只能操作自己的地址。
6. 门店服务半径校验在上门预约创建时进行，不在地址管理时校验。

**DTO 设计**：

```java
// 响应
AddressResponse {
    String addressId;
    String label;        // 家、公司等
    String contactName;
    String contactPhone;
    String province;
    String city;
    String district;
    String detail;
    Double latitude;
    Double longitude;
    Boolean isDefault;
}

// 创建/修改请求
AddressRequest {
    String label;
    @NotBlank String contactName;
    @NotBlank String contactPhone;
    @NotBlank String province;
    @NotBlank String city;
    String district;
    @NotBlank String detail;
    Double latitude;
    Double longitude;
    Boolean isDefault;
}
```

**RED 测试**：

- 用户能创建地址。
- 用户能列出自己的地址。
- 用户能修改自己的地址。
- 用户能删除自己的地址。
- 用户 A 不能操作用户 B 的地址。
- 设置默认地址时，取消其他地址的默认状态。
- 必填字段缺失返回 422。

**完成验证**：

```powershell
mvn test -pl . -Dtest="AddressControllerTest"
mvn test
git diff --check
```

---

### 11-05：配置公开读取接口匿名访问

**状态**：已批准，可按 TDD 实施。

**目标**：按照 D-014 决策，放行公开资源的 GET 读取接口。

**可与 11-01 并行执行。**

**允许修改**：

- `src/main/java/com/petcare/common/config/SecurityConfig.java`
- 社区 Controller（确保列表查询只返回审核通过内容）
- 对应测试文件

**禁止修改**：

- 管理后台相关配置
- 管理员认证逻辑
- 写操作接口
- 前端文件

**实现要求**：

1. 扫描真实 Controller 后，按 HTTP GET 和真实路径逐项放行公开内容读取端点，至少覆盖：

   - `/api/v1/services/**`（GET）
   - `/api/v1/products/**`（GET）
   - `/api/v1/community/topics/**`（GET）
   - `/api/v1/community/posts/**`（GET）
   - `/api/v1/community/comments/**`（GET）

2. 确认帖子列表只返回审核通过状态的内容。
3. 确认匿名访问不泄露用户私有信息。
4. 管理后台接口不受影响。
5. 禁止使用 `/api/v1/**` 等过宽匿名规则。

**D-014 安全约束**：

- 放行仅限于 GET 请求。
- 帖子和评论列表必须过滤审核不通过的内容。
- 不泄露其他用户的预约、订单等私有信息。

**RED 测试**：

- 未认证请求 `GET /api/v1/services` 返回 200 和数据。
- 未认证请求 `GET /api/v1/products` 返回 200 和数据。
- 未认证请求 `GET /api/v1/community/topics` 返回 200 和数据。
- 未认证请求 `GET /api/v1/community/posts` 返回 200 和数据。
- 未认证请求 `GET /api/v1/community/posts` 不包含审核未通过的帖子。
- 未认证请求 POST/PUT/DELETE 返回 401。
- 未认证请求管理后台接口返回 401。

**完成验证**：

```powershell
mvn test -pl . -Dtest="PublicAccessIntegrationTest"
mvn test
git diff --check
```

---

### 11-06：可复现演示种子数据

**目标**：提供可重复创建和清理的演示数据方案。

**依赖**：11-01（需要测试用户身份）

**允许修改**：

- 新增 `src/main/resources/data/` 或 `src/test/resources/` 下的种子数据文件
- 新增种子数据初始化组件
- 对应测试文件

**禁止修改**：

- `schema.sql`（不混入测试数据）
- 前端文件
- 生产代码逻辑

**实现要求**：

1. 创建种子数据 SQL 文件，包含：
   - 测试用户（对应 test-login 预设手机号）。
   - 测试宠物。
   - 测试地址。
   - 基础服务分类和服务项目。
   - 基础商品分类和商品。
   - 基础话题。
2. 使用 `@Profile("test")` 或测试专用初始化方案。
3. 提供清理方法。
4. 种子数据不包含真实密码、密钥或生产信息。

**约束**：

- 不在 `schema.sql` 中混入生产测试账号秘密。
- 种子数据可通过脚本或命令重复创建和清理。
- 测试用户密码使用环境变量或固定哈希值。

**完成验证**：

```powershell
mvn test -pl . -Dtest="SeedDataIntegrationTest"
git diff --check
```

---

### 11-07：补齐必要后台 API

**目标**：补齐小程序或后台真实依赖的后台 API。

**依赖**：11-05（需要确认哪些接口需要补充）

**允许修改**：

- `src/main/java/com/petcare/` 下对应业务模块
- 对应测试文件

**禁止修改**：

- 前端文件
- 已有的 API 契约和路径
- 认证和权限逻辑

**待补齐 API 清单**（需逐一核对现有 Controller）：

1. 服务分类管理（新增、修改、禁用）— 后台管理需要。
2. 商品分类管理（新增、修改、禁用）— 后台管理需要。
3. 员工不可用时间管理 — 预约时段计算依赖。

**约束**：

- 只实现小程序或后台真实依赖的 API。
- 新增 API 必须有对应权限码保护。
- 遵循已有 Controller 风格和 DTO 规范。

**完成验证**：

```powershell
mvn test
mvn clean package -DskipTests
git diff --check
```

---

### 11-08：API 接口清单

**目标**：建立与真实 Controller 一致的 API 接口清单。

**依赖**：11-01 至 11-07 全部完成。

**允许修改**：

- `docs/` 下文档文件

**禁止修改**：

- 代码文件
- 配置文件

**实现要求**：

1. 扫描所有 Controller，生成接口清单。
2. 每个接口记录：路径、方法、请求体、响应体、权限码、匿名访问标记。
3. 格式可与 `docs/25-admin-web-api-contract.md` 保持一致。
4. 清单必须与真实代码一致，不由手动猜测。

**完成验证**：

- 人工核对至少 5 个接口与 Controller 代码一致。
- 所有新增接口（11-01 至 11-07）都已包含。

---

## 6. 阶段 11 退出门禁

- [ ] 用户、宠物和地址 API 有授权和归属校验集成测试。
- [ ] 测试登录端点仅在 `test` Profile 可用。
- [ ] `dev`、`prod` 和未指定 Profile 时 test-login 不可达（有验证测试）。
- [ ] 公开读取接口按 D-014 决策放行，匿名访问测试通过。
- [ ] 演示数据可重复创建和清理。
- [ ] API 接口清单与真实 Controller 一致。
- [ ] `mvn test` 通过。
- [ ] `mvn clean package -DskipTests` 通过。
- [ ] `git diff --check` 通过。
- [ ] 无 CRITICAL 或 HIGH Review 问题遗留。
- [ ] 所有变更已提交 Git。

## 7. 每个任务包的提交规范

```text
test(auth): add test-login controller tests
feat(auth): implement test-profile login endpoint

test(user): add user profile controller tests
feat(user): implement user profile API with ownership validation

test(user): add pet controller tests
feat(user): implement pet CRUD API with ownership validation

test(user): add address controller tests
feat(user): implement address CRUD API with ownership validation

test(security): add public access integration tests
feat(security): configure anonymous access for public read endpoints

test(data): add seed data integration tests
feat(data): add reproducible seed data for test profile

feat(catalog): add service category management API
feat(catalog): add product category management API
feat(staff): add staff unavailability management API

docs: update API contract list for user endpoints
```

## 8. Agent 任务包领取规则

- 每个 Agent 一次只能领取一个任务包。
- 任务包开始前必须完成 AGENTS.md 规定的开工汇报。
- 任务包完成后必须完成 AGENTS.md 规定的交接。
- 11-01 和 11-05 可以并行，但文件范围不重叠。
- 11-02、11-03、11-04 必须按顺序执行（依赖关系）。
- 11-08 必须等 11-01 至 11-07 全部完成后才能开始。
