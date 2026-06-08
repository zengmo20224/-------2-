# Code Standards

Date: 2026-06-08

## General Rules

- Prefer simple, readable code over clever abstractions.
- Keep functions small and focused.
- Keep files cohesive; split files before they become hard to review.
- Validate all external input at system boundaries.
- Return user-friendly messages to clients.
- Log detailed server-side context without leaking secrets.
- Never silently swallow errors.
- Never hardcode API keys, passwords, tokens, or database credentials.

## Java And Spring Boot Rules

- Use Java 17 or newer.
- Use constructor injection.
- Use Bean Validation for request DTOs.
- Use DTOs for API input and output; do not expose entity classes directly from controllers.
- Put business transactions in services.
- Keep mappers focused on persistence.
- Use enum-like constants for status values.
- Use `BigDecimal` for money and distance values where precision matters.
- Use `LocalDate`, `LocalTime`, and `LocalDateTime` for date/time fields.
- Use clear exception types for validation, authorization, not found, conflict, and business rule failures.

## Package Boundaries

Business modules should own their API, service, mapper, entity, and DTO classes.

Example:

```text
booking/
  controller/
  service/
  mapper/
  entity/
  dto/
  domain/
```

Shared utilities belong in `common` only when at least two modules need them.

## API Rules

- Use `/api/v1`.
- Use plural resource names.
- Use nouns for resources and verbs only for business actions such as `/cancel` or `/confirm`.
- Use pagination for list endpoints.
- Use consistent response envelopes.
- Use correct HTTP status codes.
- Never return stack traces to clients.

## Database Rules

- Use indexes intentionally; every index must support a query pattern.
- Do not use `SELECT *` in custom SQL.
- Avoid N+1 query patterns.
- Wrap multi-table state changes in transactions.
- Keep logical delete behavior consistent.
- Keep status transitions explicit and test-covered.

## AI Rules

- AI provider calls must go through one backend adapter.
- Provider credentials must come from environment variables.
- AI prompts must be built from approved context objects.
- AI customer service must not invent business facts.
- AI admin analysis must use backend aggregate data, not direct SQL execution by AI.
- AI medical safety refusals and veterinarian referral must be deterministic where high-risk symptoms are detected.

## Frontend Rules For Later Phases

- Use typed API clients where possible.
- Handle loading, empty, error, and success states.
- Do not store sensitive tokens in unsafe locations without review.
- Keep business state in Pinia stores only when shared across pages.
- Use semantic UI labels and stable test selectors for E2E tests.
