# Project Boundary

Date: 2026-06-08

## Product Positioning

The project is an AI-enhanced pet store O2O service booking and customer operations platform for a single real pet store.

It must look stronger than a CRUD demo by showing:

- real service booking and staff scheduling
- merchant confirmation and manual reassignment
- pet profiles and user addresses
- community content operations and moderation
- light product pickup orders
- AI features with business data grounding and safety boundaries
- PC admin operations

## V1 In Scope

V1 must complete the minimum business loop:

1. User and admin authentication model.
2. Store, service, staff, staff skill, staff schedule, unavailable time.
3. Service booking with merchant confirmation.
4. Available time calculation based on service duration, staff skills, work schedule, unavailable time, and occupied bookings.
5. Home service distance validation using store and address coordinates.
6. Pet profile and address management.
7. Community post, comment, like, favorite, report, sensitive word review.
8. Product display, cart, pickup order, offline payment confirmation.
9. Unified AI provider client with no hardcoded provider secrets.
10. AI customer service, pet companion, post assistant, and admin analysis API foundations.
11. Admin APIs for core business management.
12. Docker Compose for backend, MySQL, and optional admin frontend deployment.

## V1 Out Of Scope

Do not implement these in V1 unless all P0 and P1 gates are already passed:

- WeChat Pay or any online payment settlement.
- Multi-store chain support.
- Independent employee mini program.
- Real map routing distance or third-party map paid API.
- Full membership, coupon, points, and complex marketing system.
- Complex inventory warehouse management.
- AI direct database access.
- AI medical diagnosis, prescription, or treatment recommendation.
- Production object storage migration to MinIO, Tencent COS, or Aliyun OSS.

## Reserved For V2

- Redis caching and rate-limit backing store.
- Employee mobile workflow.
- Online payment.
- Object storage.
- More detailed marketing activity ROI analysis.
- Member levels, coupons, points.
- Multi-store expansion.

## Non-Negotiable Business Rules

- User cannot choose staff directly.
- Merchant must confirm service bookings unless a later accepted decision enables auto-confirmation.
- `CANCELLED` and `REJECTED` bookings do not occupy staff time.
- `PENDING_CONFIRM`, `CONFIRMED`, and `IN_SERVICE` bookings occupy staff time.
- Home service must require address and distance validation.
- AI customer service cannot invent price, stock, business hours, booking rules, or service scope.
- AI pet companion cannot diagnose disease, prescribe medicine, replace a veterinarian, or promise treatment outcomes.
- Admin AI analysis must receive backend aggregated data; it must not query the database directly.

## Delivery Principle

Each phase must finish with:

- code or SQL committed to Git
- build or syntax validation evidence
- relevant tests passing
- no unresolved critical security issue
- integration gate approval before the next phase depends on it
