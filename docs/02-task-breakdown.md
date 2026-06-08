# Task Breakdown

Date: 2026-06-08

## Phase 0: Planning Baseline

Status: in progress

Deliverables:

- project boundary document
- architecture design document
- GLM5.1 implementation plan
- code standards
- testing and verification gates
- Git safety workflow

Exit criteria:

- documents committed to Git
- GLM5.1 can start Phase 1 without guessing project scope

## Phase 1: Database Schema

Owner: GLM5.1

Deliverables:

- `schema.sql`
- optional `docs/schema-notes.md` if field-level clarifications are needed

Required tasks:

1. Generate complete MySQL 8 DDL for every table in the pasted requirement.
2. Keep table and field names aligned with the requirement unless a documented correction is necessary.
3. Add table comments and important field comments.
4. Add default status values.
5. Add `create_time DEFAULT CURRENT_TIMESTAMP`.
6. Add `update_time DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`.
7. Add `deleted TINYINT DEFAULT 0`.
8. Add unique indexes for `openid`, `username`, `booking_no`, `order_no`, and relation uniqueness.
9. Add indexes for high-frequency queries.
10. Validate by importing into MySQL 8.

Exit criteria:

- SQL imports successfully from a clean database.
- `SHOW TABLES` confirms all tables exist.
- representative `SHOW CREATE TABLE` checks confirm comments, defaults, and indexes.
- Git commit exists for the validated schema.

## Phase 2: Backend Skeleton

Owner: GLM5.1

Deliverables:

- Spring Boot 3 project
- Maven or Gradle build file
- `application.yml`
- unified response and error structure
- base packages and module directories
- MyBatis-Plus configured
- database connection configured through environment variables

Exit criteria:

- application starts locally
- health endpoint works
- build passes
- no hardcoded secrets
- Git commit exists

## Phase 3: Core Domain Entities And Mappers

Owner: GLM5.1

Deliverables:

- entity classes matching `schema.sql`
- mapper interfaces
- basic CRUD service foundations for each module
- enum constants for status fields

Exit criteria:

- mapper integration tests pass against test database
- status constants match database defaults
- no direct controller-to-mapper shortcuts for business flows
- Git commit exists

## Phase 4: Authentication And Authorization

Owner: GLM5.1

Deliverables:

- admin login
- user login placeholder suitable for later WeChat integration
- token authentication
- admin role checks
- user ownership checks

Exit criteria:

- unauthorized requests return `401`
- forbidden role access returns `403`
- password hashing test exists
- no token or password logged
- Git commit exists

## Phase 5: Service Booking And Scheduling

Owner: GLM5.1

Deliverables:

- service listing
- available time calculation
- booking creation
- merchant confirm, reject, cancel, complete
- status log recording
- manual staff reassignment
- home service radius validation

Exit criteria:

- unit tests cover interval subtraction and slot generation
- integration tests cover booking conflicts
- cancelled and rejected bookings do not occupy time
- pending, confirmed, and in-service bookings occupy time
- Git commit exists

## Phase 6: Community And Moderation

Owner: GLM5.1

Deliverables:

- topic, post, image, comment APIs
- like, favorite, report APIs
- sensitive word matching
- review record creation
- admin moderation APIs

Exit criteria:

- tests cover no-risk, mild-risk, medium-risk, severe-risk content paths
- severe content is rejected
- mild and medium content enters review
- comments also pass moderation
- Git commit exists

## Phase 7: Product Pickup Orders

Owner: GLM5.1

Deliverables:

- product listing
- cart item management
- pickup order creation
- merchant confirmation
- prepare, ready, paid, completed, cancelled flows

Exit criteria:

- stock and order amount are handled transactionally
- duplicate cart rows are prevented
- order state transitions are tested
- Git commit exists

## Phase 8: AI Provider And AI Features

Owner: GLM5.1

Deliverables:

- unified AI provider client
- conversation and message persistence
- usage logging
- customer service context builder
- pet companion safety guardrails
- post assistant
- admin analysis report generation from backend aggregation

Exit criteria:

- AI provider can be mocked in tests
- missing API key fails fast with safe error
- customer service responses are grounded in supplied context
- medical high-risk symptoms trigger veterinarian recommendation
- AI cannot directly query database
- Git commit exists

## Phase 9: Admin API Completion

Owner: GLM5.1

Deliverables:

- store config management
- service management
- staff, skill, schedule management
- booking management
- product management
- content moderation management
- operation logs

Exit criteria:

- admin endpoints require role permissions
- important admin operations write operation logs
- pagination exists for list endpoints
- Git commit exists

## Phase 10: Frontend Integration

Owner: future frontend implementer

Deliverables:

- mini program user flows
- admin dashboard flows
- API integration
- browser or mini-program emulator verification

Exit criteria:

- critical flows have E2E evidence
- UI handles loading, empty, and error states
- Git commit exists
