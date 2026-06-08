# Integration Gates

Date: 2026-06-08

No phase should be treated as finished until its gate passes.

## Gate 1: Schema

- [ ] `schema.sql` exists.
- [ ] all required tables are present.
- [ ] SQL imports into MySQL 8 from a clean database.
- [ ] table comments exist.
- [ ] important field comments exist.
- [ ] unique indexes exist.
- [ ] high-frequency query indexes exist.
- [ ] defaults exist for status, create time, update time, and deleted fields.
- [ ] Git commit exists.

## Gate 2: Backend Skeleton

- [ ] Spring Boot app starts.
- [ ] health endpoint works.
- [ ] database connection is environment-configured.
- [ ] unified response envelope exists.
- [ ] global exception handling exists.
- [ ] no hardcoded secrets.
- [ ] Git commit exists.

## Gate 3: Core Business Logic

- [ ] unit tests cover pure calculations.
- [ ] integration tests cover database-backed flows.
- [ ] booking conflict rules pass.
- [ ] home service distance rules pass.
- [ ] status transition rules pass.
- [ ] Git commit exists.

## Gate 4: Security

- [ ] admin APIs require authentication.
- [ ] role authorization is enforced.
- [ ] user ownership is enforced.
- [ ] passwords are hashed.
- [ ] provider secrets are environment-driven.
- [ ] errors do not leak stack traces or SQL details.
- [ ] Git commit exists.

## Gate 5: AI Safety

- [ ] AI provider is behind one adapter.
- [ ] provider can be mocked.
- [ ] customer service is grounded by backend context.
- [ ] admin analysis uses backend aggregates.
- [ ] high-risk pet symptoms trigger veterinarian recommendation.
- [ ] AI cannot directly query the database.
- [ ] Git commit exists.

## Gate 6: Frontend Readiness

- [ ] API docs or endpoint list exists.
- [ ] user critical flows are available.
- [ ] admin critical flows are available.
- [ ] list endpoints support pagination.
- [ ] error shape is consistent.
- [ ] seed data or demo data exists.
- [ ] Git commit exists.

## Gate 7: Release Candidate

- [ ] backend build passes.
- [ ] tests pass.
- [ ] coverage target is met or documented.
- [ ] SQL migration/import path is reproducible.
- [ ] Docker Compose starts required services.
- [ ] no critical security issue remains.
- [ ] Git working tree is clean.
