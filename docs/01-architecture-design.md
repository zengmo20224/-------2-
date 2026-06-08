# Architecture Design

Date: 2026-06-08

## System Shape

Use a modular monolith for V1.

Recommended stack:

- Mini program: uni-app + Vue 3 + Pinia
- Admin frontend: Vue 3 + Vite + Element Plus
- Backend: Spring Boot 3 + MyBatis-Plus
- Auth: Sa-Token or Spring Security + JWT, chosen before backend coding starts
- Database: MySQL 8
- Cache: no Redis dependency in V1 core path; Redis is optional for V2
- Deployment: Docker Compose + Nginx + MySQL
- AI: backend-owned unified AI provider client

## Backend Module Boundaries

Backend packages should be grouped by business capability, not by generic technical type alone.

Recommended modules:

- `user`: user account, pet profile, user address
- `store`: store and store configuration
- `service`: service category and service item
- `staff`: staff, skill, schedule, unavailable time
- `booking`: booking lifecycle, status log, available slot calculation
- `community`: topic, post, image, comment, like, favorite, report
- `moderation`: sensitive word and content review record
- `product`: category, product, image, cart, pickup order
- `marketing`: activity, product relation, service relation
- `ai`: conversation, message, usage log, analysis report, FAQ knowledge, AI provider client
- `admin`: admin user and operation log
- `common`: response envelope, exceptions, validation, security context, pagination

## Layering

Use this dependency direction:

```text
Controller -> Application/Service -> Domain Rules -> Repository/Mapper -> Database
```

Rules:

- Controllers validate request shape and delegate.
- Services own business transactions and state transitions.
- Domain helpers own pure calculations such as distance and available time.
- Mappers only perform persistence operations.
- No controller should directly call another controller.
- No AI provider should be called from SQL or mapper code.

## API Contract

Use REST under `/api/v1`.

Response envelope:

```json
{
  "success": true,
  "data": {},
  "error": null,
  "meta": {}
}
```

Error response:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "validation_error",
    "message": "请求参数不合法",
    "details": []
  },
  "meta": null
}
```

Status code rules:

- `200`: read or update success
- `201`: create success
- `204`: delete success with no body
- `400`: malformed input
- `401`: unauthenticated
- `403`: unauthorized
- `404`: resource not found
- `409`: duplicate or state conflict
- `422`: semantically invalid business request
- `500`: unexpected server error without leaking stack trace

## Data Model Direction

Use MySQL 8 with:

- InnoDB
- `utf8mb4`
- `BIGINT` primary keys
- `DECIMAL(10,2)` for money
- `DECIMAL(10,6)` for coordinates
- `VARCHAR(32)` for readable status fields
- `create_time`, `update_time`, `deleted` on normal business tables
- high-frequency query indexes
- unique indexes on business numbers and natural unique fields

The first implementation task is `schema.sql`; it is the foundation for generated entities and mapper work.

## Core Algorithms

### Available Slot Calculation

Input:

- `service_item_id`
- booking date
- store id

Process:

1. Read service duration, category, mode, and status.
2. Find staff with matching service category skill.
3. Load staff schedules for that date.
4. Remove schedules marked unavailable.
5. Subtract unavailable intervals such as lunch, leave, temporary block.
6. Subtract occupied bookings in `PENDING_CONFIRM`, `CONFIRMED`, and `IN_SERVICE`.
7. Generate start times by `store_config.time_slot_minutes`.
8. Keep only slots where `start_time + duration_minutes <= available_interval.end_time`.

Output:

- list of available slots grouped by staff internally
- user-facing available times without exposing staff choice

### Home Service Distance

Use straight-line distance for V1.

Required checks:

- service mode allows `HOME`
- address exists and belongs to user
- address and store both have longitude and latitude
- distance is within `store_config.home_service_radius_km`

### AI Data Grounding

AI calls must receive explicit backend-curated context:

- customer service: store, config, services, products, FAQ
- admin analysis: backend SQL aggregation results
- content generation: user-provided facts only

AI output must never be treated as authoritative system state.

## Security Design

- Passwords must be hashed, never stored in plain text.
- AI API keys must come from environment variables or secret management.
- Admin APIs require role checks.
- User APIs require ownership checks.
- File uploads must validate extension, size, and content type.
- Error responses must not expose SQL, stack traces, secrets, or provider raw errors.
- Operation logs should capture admin-sensitive operations.
