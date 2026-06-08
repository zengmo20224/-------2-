# Testing And Verification

Date: 2026-06-08

## Coverage Target

Minimum target: 80% line coverage for backend business logic after implementation starts.

Coverage is not a replacement for meaningful tests. Critical business rules must have direct tests even if coverage is already above 80%.

## Test Layers

### Unit Tests

Required for:

- distance calculation
- interval overlap and subtraction
- available slot generation
- booking state transition rules
- sensitive word matching and risk level classification
- product order amount calculation
- AI safety guardrail classification

### Integration Tests

Required for:

- mapper/database access
- booking creation with conflict detection
- booking status log persistence
- content review record creation
- product order creation with order items
- admin authorization checks
- AI provider adapter using mocked provider

### E2E Tests

Required before frontend integration is considered complete:

- user books a store service
- user attempts home service outside service radius and gets rejected
- merchant confirms booking
- user publishes normal post
- risky post enters review or is rejected
- user creates pickup order
- admin completes pickup order
- AI customer service answers from seeded store/service/FAQ data

## Phase 1 SQL Verification

For `schema.sql`, test coverage is replaced by database validation:

- import SQL into clean MySQL 8 database
- verify all required tables exist
- inspect representative table DDL
- verify unique indexes exist
- verify default status, time, and deleted values exist

## Backend Verification Commands

These commands should exist once backend is created:

```powershell
mvn test
mvn clean package
```

If formatting or linting is configured:

```powershell
mvn spotless:check
```

If dependency audit tooling is configured:

```powershell
mvn org.owasp:dependency-check-maven:check
```

## Verification Report Template

Every phase handoff must include:

```text
Build: PASS/FAIL
Tests: PASS/FAIL
Coverage: percentage or not applicable
Security check: PASS/FAIL
Database validation: PASS/FAIL/not applicable
Git state: clean/dirty
Commit: commit hash
Known risks:
```

## Failure Policy

- Build failure blocks integration.
- SQL import failure blocks Phase 1 completion.
- Critical security issue blocks integration.
- Booking conflict test failure blocks booking module integration.
- AI safety guardrail failure blocks AI module integration.
- Dirty Git state blocks handoff unless the dirty files are explicitly listed and justified.
