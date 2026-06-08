# Git 防护流程

日期：2026-06-08

项目必须使用 Git 检查点，防止 AI 生成的错误变更破坏核心项目。

## 分支策略

每个阶段使用一个独立分支：

```text
phase-1-schema
phase-2-backend-skeleton
phase-3-entities-mappers
phase-4-auth
phase-5-booking-scheduling
phase-6-community-moderation
phase-7-product-orders
phase-8-ai
phase-9-admin-api
phase-10-frontend
```

不要把无关阶段混在一个分支里。

## 开始工作前

运行：

```powershell
git status --short --branch
git diff --stat
```

如果出现意外变更，先停下来记录清楚，再开始编辑。

## 提交规则

使用 Conventional Commits：

```text
docs: add project planning baseline
feat: add validated mysql schema
test: add booking slot calculation tests
feat: implement booking slot calculation
fix: reject home service outside radius
refactor: simplify booking interval calculation
```

每个提交都应该足够小，方便审查。

## TDD 检查点提交

业务逻辑按以下节奏提交：

1. RED 验证后提交失败测试。
2. GREEN 验证后提交实现。
3. 只有测试保持绿色时，才提交重构。

示例：

```powershell
git add src/test/java
git commit -m "test: add booking availability conflict tests"

git add src/main/java
git commit -m "feat: implement booking availability calculation"
```

## 防护命令

提交前检查变更：

```powershell
git diff --stat
git diff --check
git diff
```

提交后检查：

```powershell
git status --short --branch
git log --oneline -5
```

## 恢复策略

不要随意使用破坏性命令。

除非用户明确授权，否则禁止使用：

```powershell
git reset --hard
git clean -fd
git checkout -- .
```

如果 AI 生成的变更是错的，优先：

- 用新的提交向前修正。
- 使用 `git revert <hash>` 回滚指定提交。
- 在高风险清理前创建备份分支。

## 交接要求

阶段交接必须包含：

- 分支名
- 最新提交 hash
- `git status --short --branch` 输出摘要
- 运行过的验证命令
- 验证结果
