# Git Safety Workflow

Date: 2026-06-08

The project must use Git checkpoints to prevent AI-generated changes from damaging the core project.

## Branch Policy

Use one branch per phase:

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

Do not mix unrelated phases in one branch.

## Before Starting Work

Run:

```powershell
git status --short --branch
git diff --stat
```

If there are unexpected changes, stop and document them before editing.

## Commit Rules

Use conventional commits:

```text
docs: add project planning baseline
feat: add validated mysql schema
test: add booking slot calculation tests
feat: implement booking slot calculation
fix: reject home service outside radius
refactor: simplify booking interval calculation
```

Each commit should be small enough to review.

## TDD Checkpoint Commits

For business logic:

1. Commit failing tests after RED is verified.
2. Commit implementation after GREEN is verified.
3. Commit refactor only after tests remain GREEN.

Example:

```powershell
git add src/test/java
git commit -m "test: add booking availability conflict tests"

git add src/main/java
git commit -m "feat: implement booking availability calculation"
```

## Protection Commands

Inspect changes before committing:

```powershell
git diff --stat
git diff --check
git diff
```

After commit:

```powershell
git status --short --branch
git log --oneline -5
```

## Restore Policy

Do not use destructive commands casually.

Forbidden unless the user explicitly authorizes:

```powershell
git reset --hard
git clean -fd
git checkout -- .
```

If a generated change is wrong, prefer:

- edit forward with a new commit
- revert a specific commit with `git revert <hash>`
- create a backup branch before risky cleanup

## Handoff Requirement

A phase handoff must include:

- branch name
- latest commit hash
- `git status --short --branch` output summary
- validation commands run
- validation results
