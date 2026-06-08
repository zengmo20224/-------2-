# GLM5.1 Implementation Plan

Date: 2026-06-08

This is the execution brief for GLM5.1.

## Role

GLM5.1 is responsible for concrete code and SQL implementation. Codex owns planning, task boundaries, quality gates, and review criteria.

Do not skip phases. Do not integrate a phase into the main project until its exit criteria pass.

## Current Repository State

- The repository starts as a planning baseline.
- There is no application code yet.
- Use Git for every safe checkpoint.
- Start implementation from `schema.sql`; do not build frontend first.

## Required Git Setup Before Coding

Run:

```powershell
git status --short --branch
git branch
```

Then create a dedicated branch for the current phase:

```powershell
git switch -c phase-1-schema
```

If the branch already exists:

```powershell
git switch phase-1-schema
```

Never work for a phase directly on `main` unless the user explicitly asks.

## Phase 1 Mandatory Task: schema.sql

Create:

```text
schema.sql
```

Requirements:

- Use MySQL 8 compatible SQL.
- Use `CREATE DATABASE IF NOT EXISTS petcare_o2o DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`.
- Use InnoDB for every table.
- Use `BIGINT` primary keys.
- Use `DECIMAL(10,2)` for money.
- Use `DECIMAL(10,6)` for longitude and latitude.
- Use `VARCHAR(32)` for status fields.
- Add `COMMENT` to every table.
- Add `COMMENT` to important fields.
- Add `create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP`.
- Add `update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` where the table has update time.
- Add `deleted TINYINT NOT NULL DEFAULT 0` where the table has logical deletion.
- Add unique indexes exactly where the requirement says unique.
- Add useful indexes for user id, store id, staff id, status, date, create time, and relation lookups.

Do not omit tables.

Tables required:

- `user`
- `pet`
- `user_address`
- `store`
- `store_config`
- `service_category`
- `service_item`
- `staff`
- `staff_skill`
- `staff_schedule`
- `staff_unavailable_time`
- `service_booking`
- `booking_status_log`
- `topic`
- `post`
- `post_image`
- `post_comment`
- `post_like`
- `post_favorite`
- `post_report`
- `sensitive_word`
- `content_review_record`
- `product_category`
- `product`
- `product_image`
- `cart_item`
- `product_order`
- `product_order_item`
- `marketing_activity`
- `activity_product`
- `activity_service`
- `ai_conversation`
- `ai_message`
- `ai_usage_log`
- `ai_analysis_report`
- `faq_knowledge`
- `admin_user`
- `admin_operation_log`

## Allowed Field Adjustments

Do not change the business design casually.

Allowed corrections if documented in comments or `docs/schema-notes.md`:

- escape reserved table names with backticks, especially `user`
- use explicit `NOT NULL` only where safe
- add `store_id` indexes and foreign-key-style indexes even if no physical foreign key is declared
- add `created_by` or `updated_by` only if the user approves later; do not add in Phase 1

Do not add physical foreign keys in Phase 1 unless explicitly approved. Prefer indexed logical relationships for simpler iteration and easier test data setup.

## Schema Validation Commands

Preferred with Docker:

```powershell
$env:PETCARE_MYSQL_ROOT_PASSWORD = "replace-with-local-test-password"
docker run --name petcare-mysql -e "MYSQL_ROOT_PASSWORD=$env:PETCARE_MYSQL_ROOT_PASSWORD" -e MYSQL_DATABASE=petcare_o2o -p 3306:3306 -d mysql:8.0
Get-Content -Raw .\schema.sql | docker exec -i petcare-mysql mysql -uroot "-p$env:PETCARE_MYSQL_ROOT_PASSWORD" petcare_o2o
docker exec -i petcare-mysql mysql -uroot "-p$env:PETCARE_MYSQL_ROOT_PASSWORD" -e "USE petcare_o2o; SHOW TABLES;"
```

If Docker is unavailable, use local MySQL 8:

```powershell
Get-Content -Raw .\schema.sql | mysql -uroot -p
mysql -uroot -p -e "USE petcare_o2o; SHOW TABLES;"
```

Spot-check:

```powershell
mysql -uroot -p -e "USE petcare_o2o; SHOW CREATE TABLE service_booking;"
mysql -uroot -p -e "USE petcare_o2o; SHOW CREATE TABLE post;"
mysql -uroot -p -e "USE petcare_o2o; SHOW CREATE TABLE admin_user;"
```

## Phase 1 Commit Requirement

After validation:

```powershell
git status --short
git add schema.sql
# Run only when the optional notes file exists:
git add docs/schema-notes.md
git commit -m "feat: add validated mysql schema"
git status --short --branch
```

If no `docs/schema-notes.md` exists, do not add it.

## Phase 2 Backend Rules

Only start after Phase 1 passes.

Backend must use:

- Spring Boot 3
- MyBatis-Plus
- Java 17 or newer
- Maven unless there is a strong reason to choose Gradle
- environment variables for database and AI configuration
- module/package layout from `docs/01-architecture-design.md`

Minimum package sketch:

```text
src/main/java/.../petcare/
  common/
  user/
  store/
  service/
  staff/
  booking/
  community/
  moderation/
  product/
  marketing/
  ai/
  admin/
```

## TDD Rule For Business Logic

For each non-trivial module:

1. Write tests first.
2. Run tests and confirm the expected RED failure.
3. Implement minimal code.
4. Rerun tests and confirm GREEN.
5. Refactor only while tests stay green.
6. Commit the stage.

Required early tests:

- distance calculation
- interval overlap detection
- available slot generation
- booking state transition
- sensitive word risk classification
- order amount calculation
- AI provider missing-key behavior

## Integration Rule

A phase is not considered integrated until:

- relevant tests pass
- build passes
- lint or formatting check passes if configured
- security checks pass
- Git commit exists
- `docs/07-integration-gates.md` checklist is satisfied

## Handoff Report Format

At the end of each phase, report:

```text
Phase:
Branch:
Commit:
Files changed:
Validation commands:
Validation result:
Known limitations:
Next phase recommendation:
```
