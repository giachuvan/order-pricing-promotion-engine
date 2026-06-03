# Order Pricing & Promotion Engine

Spring Boot 3 service that calculates order totals by applying composable promotion rules loaded from PostgreSQL.

## Context — which challenge this repo implements

*(Optional for reviewers. The assignment README checklist in `engineer-assignment_cha2.md` lists eight items starting with architecture; some briefs also ask “which challenge you chose.” This section clarifies scope only — sections **2–9** below map to those eight required topics.)*

This repository is the **Order Pricing & Promotion Engine** (Challenge 2 in the two-challenge brief). It is not the Warehouse Inventory Reservation System.

**Why this challenge fit what I wanted to demonstrate**

- **Composable rules and patterns:** Stacking promotions (percentage, VIP, coupons, buy‑2‑get‑1) maps directly to **Strategy**, **Chain of Responsibility**, and **Factory** — good material for **Open/Closed** design and a clear domain core.
- **Deterministic pricing logic:** Rule ordering, `BigDecimal` handling, and a verifiable outcome (assignment example: `finalPrice` 102.50) show testable business logic end to end.
- **Hexagonal boundaries:** Ports for promotions, coupons, and order persistence match the take-home’s clean-architecture expectations without mixing JPA into the application layer.
- **Still exercises production concerns:** Concurrent `POST /promotions`, partial unique indexes, transactional calculate + persist, and integration tests against real PostgreSQL — not “rules only in memory.”

I chose this path to showcase **extensible pricing rules and layered architecture**; Challenge 1 in the same brief highlights inventory reservation and lifecycle design — a different problem domain, not a ranking of difficulty.

## 2. Architecture overview

**Style:** **Clean Architecture / Hexagonal** — dependencies point inward. The `application` layer depends only on `domain` and port interfaces; JPA entities and Spring Data repositories exist solely in `infrastructure` adapters.

```
                    ┌─────────────┐
                    │     api     │  Controllers, DTOs, envelope
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │ application │  Services + port interfaces
                    └──────┬──────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
  ┌──────▼──────┐   ┌──────▼──────┐   ┌──────▼──────┐
  │   domain    │   │    ports    │   │ (interfaces)│
  │ rules/models│   │ in applic.  │   │             │
  └─────────────┘   └──────┬──────┘   └─────────────┘
                           │
                    ┌──────▼──────┐
                    │infrastructure│ JPA, adapters, rule impls
                    └─────────────┘
```

### Packages

```
pricing/
├── api/                      REST + DTOs
├── application/
│   ├── port/                 Outbound port interfaces
│   │   ├── ActivePromotionPort
│   │   ├── CouponResolutionPort
│   │   ├── OrderPersistencePort
│   │   ├── PromotionManagementPort
│   │   └── ProductCatalogPort
│   ├── command/              CalculateOrderCommand, CreatePromotionCommand
│   ├── OrderPricingUseCase, PromotionUseCase (interfaces)
│   └── OrderPricingService, PromotionService
├── domain/
│   ├── model/                Order, Promotion, CouponDiscount, …
│   ├── pricing/              PricingContext, PricingResult
│   └── promotion/            PromotionRule, PromotionChain, PromotionRuleFactory
└── infrastructure/
    ├── persistence/          Entities, repositories, *Adapter implements ports
    └── promotion/              Rule classes, *RuleSource registry, RegistryPromotionRuleFactory
```

### Port → adapter mapping

| Port | Adapter | Responsibility |
|------|---------|----------------|
| `ActivePromotionPort` | `ActivePromotionAdapter` | Load active promotions as `PromotionDefinition` |
| `CouponResolutionPort` | `CouponResolutionAdapter` | Resolve coupon code → `CouponDiscount` |
| `OrderPersistencePort` | `OrderPersistenceAdapter` | Persist domain `Order` → JPA |
| `PromotionManagementPort` | `PromotionManagementAdapter` | List/create promotions as domain `Promotion` |
| `ProductCatalogPort` | `ProductCatalogAdapter` | List reference products from seed catalog |
| `PromotionRuleFactory` | `RegistryPromotionRuleFactory` | Assembles pipeline from all `PromotionRuleSource` `@Component` beans |

**Flow:** `POST /orders/calculate` → controller maps DTO → `CalculateOrderCommand` → `OrderPricingUseCase` → ports + `PromotionChain` → `OrderPersistencePort.save(Order)`.

**Discount order:** PERCENTAGE → VIP → COUPON → BUY2 (`@PipelineOrder` on each `PromotionRuleSource`). New rule types: add a `@Component` `PromotionRuleSource` with `@PipelineOrder` — no change to `RegistryPromotionRuleFactory`. Independent discounts on original subtotal.

## 3. Design patterns

| Pattern | Where |
|---------|--------|
| **Strategy** | `PromotionRule.apply(PricingContext)` — `PercentageDiscountRule`, `VipDiscountRule`, `CouponRule`, `Buy2Get1FreeRule` (eligibility inside each rule, `Optional.empty()` when N/A) |
| **Chain of Responsibility** | `PromotionChainHandler.handle(context, continuation)` → `PromotionRuleChainHandler` → `PromotionChainContinuation.proceed()` (explicit next step, not a bare for-loop) |
| **Factory** | `PromotionRuleFactory` → `RegistryPromotionRuleFactory` |
| **Registry** | Spring-discovered `PromotionRuleSource` `@Component` beans, ordered by `@PipelineOrder` (10 → 20 → 30 → 40) |
| **Builder** | `PricingResultBuilder` accumulates `DiscountLine`s and computes totals |
| **Repository** | Spring Data (inside adapters only) |
| **Adapter** | Port `*Adapter` classes; `PromotionRuleChainHandler` adapts Strategy → CoR link |

## 4. SOLID

- **SRP:** One rule per class; adapters only map/persist.
- **OCP:** New promotion = new rule class + new `PromotionRuleSource` `@Component` (registry picks it up automatically).
- **LSP:** Rules interchangeable via `PromotionRule`.
- **ISP:** Focused ports (`CouponResolutionPort` vs `PromotionManagementPort`).
- **DIP:** Controllers depend on `OrderPricingUseCase` / `PromotionUseCase`; services depend on port interfaces; infrastructure implements ports. **No JPA or API DTO imports in `application`.**

## 5. Database design

- **promotions** — rule config; partial unique index on `(type) WHERE active = true`; `created_at` / `updated_at` (TIMESTAMPTZ); `@Version` for optimistic locking
- **coupons** — flat discounts by code; `updated_at`; `@Version`
- **orders** / **order_items** — UUID order id, BIGSERIAL line id, `line_total` app-calculated; `@PrePersist` on `OrderEntity` for `created_at`
- **products** — reference catalog (`GET /api/v1/products`)

**Integrity (SQL CHECK constraints in `001-schema.sql`):**

| Table | Constraint |
|-------|------------|
| `products` | `price > 0` |
| `order_items` | `price > 0`, `quantity > 0` |
| `coupons` | `discount_amount > 0` |
| `promotions` | `value > 0` |
| `orders` | `subtotal`, `total_discount`, `final_price` ≥ 0 |

**Liquibase (SQL only):** `001-schema.sql` (schema + checks + `version` / `updated_at`), `002-seed.sql`, `003-promotions-unique-active-index.sql`

**JPA audit:** `@PrePersist` / `@PreUpdate` on `PromotionEntity` and `CouponEntity` maintain `updated_at` (no DB trigger required).

## API reference

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/orders/calculate` | Calculate and persist order |
| GET | `/api/v1/promotions` | List active promotions |
| POST | `/api/v1/promotions` | Create promotion (201 Created) |
| GET | `/api/v1/products` | List reference product catalog (seed data) |

**Envelope:** `{ "data": ..., "error": null }` or `{ "data": null, "error": { "code", "message" } }`.

**HTTP status mapping:** `COUPON_NOT_FOUND` → **404**; `COUPON_INACTIVE`, `COUPON_EXPIRED`, `PROMOTION_CONFLICT`, validation → **400**; unexpected errors → **500**. `finalPrice` is floored at **0** when discounts exceed subtotal.

**OpenAPI:** SpringDoc UI at http://localhost:8080/swagger-ui.html (spec: `/v3/api-docs`).

### Example calculate

```bash
curl -s http://localhost:8080/api/v1/orders/calculate \
  -H 'Content-Type: application/json' \
  -d '{
    "customerType": "VIP",
    "items": [
      { "sku": "A100", "price": 100, "quantity": 2 },
      { "sku": "B200", "price": 50, "quantity": 1 }
    ],
    "couponCode": "SUMMER10"
  }' | jq
```

Expected: `finalPrice` 102.50, `totalDiscount` 147.50, four discount lines.

## 6. How to run the system

```bash
docker compose up --build
```

API: http://localhost:8080 — OpenAPI UI: http://localhost:8080/swagger-ui.html

## 7. How to run the tests

```bash
mvn test
```

| What runs | Notes |
|-----------|--------|
| **Unit tests** (`pricing.application`, `pricing.domain`, `pricing.infrastructure.*`) | Rule tests (incl. parameterized Buy2), chain combined + floor-at-zero, service tests (Mockito). |
| **`@WebMvcTest`** (`OrderControllerWebTest`, `PromotionControllerWebTest`) | Controller validation and response mapping without full context. |
| **Integration tests** (`pricing.integration.*`) | Testcontainers + MockMvc: calculate (coupon 404/expired/inactive), promotions CRUD paths, products, concurrency. |
| **Concurrency IT** (`ConcurrencyIntegrationTest`) | Parallel promotion create + parallel calculate. |

**Docker:** Integration tests use `@Testcontainers(disabledWithoutDocker = true)`. If Docker is **not** running, that class is **skipped** and the build still **passes** — you only get unit coverage. Start Docker (daemon reachable) before `mvn test` if you want the full calculate endpoint asserted against real PostgreSQL.

`mvn verify` is not required for this project (no Failsafe `integration-test` phase); it runs the same tests as `test`, then packages the app.

## Assignment checklist

| Requirement | Status |
|-------------|--------|
| Challenge 2 — Order Pricing & Promotion Engine | Done |
| REST API — required endpoints + envelope + products catalog | Done |
| 4 promotion rules (Strategy + Factory + Chain) | Done |
| PostgreSQL + Liquibase SQL | Done |
| `docker compose up` | Done |
| Unit + integration tests | Done |
| `finalPrice` 102.50 | Done |

## 8. Trade-offs

- **Discount stacking:** Independent discounts on original subtotal (per assignment example).
- **API mapping at the edge:** Controllers map request DTOs to application commands (`OrderRequestMapper`, `PromotionRequestMapper`); use cases accept commands and return domain results (`PricingResult`, `Promotion`). Response DTOs are built only in the API layer.
- **No promotion PUT / coupon CRUD:** Out of assignment scope.
- **Integration tests:** May use `OrderRepository` directly at the outer test boundary to verify DB state.

## 9. What would break at scale and how you would fix it

The current implementation is correct for assignment scope (single Postgres instance, synchronous request/response, moderate traffic). Under high load or large catalog/promotion churn, these areas would strain first:

| Pressure | What breaks today | How to fix |
|----------|-------------------|------------|
| **Hot `POST /orders/calculate`** | Every request calls `ActivePromotionPort.findActivePromotions()` (DB, no cache), resolves coupon, builds the rule chain, then **writes** `orders` + `order_items` — all inside one `@Transactional` on `OrderPricingService.calculate()`. Throughput becomes DB-bound; connections are held for the full read+write path. | Cache active promotions (and optionally coupon metadata) in **Redis** with short TTL + explicit invalidation on `POST /promotions`. Tune Hikari pool; scale **stateless** app replicas behind a load balancer. |
| **Stale or wrong discounts after config change** | No cache today — safe but slow. Naive caching without invalidation would serve outdated rules. | Versioned cache keys (`promotions:v3`), pub/sub or message on promotion create/update to bust cache; optional read-through with `If-None-Match` on internal config API. |
| **Duplicate calculate / retries** | Clients retrying the same checkout can insert **multiple order rows** for the same logical checkout (no idempotency key). | Accept `Idempotency-Key` header; store key → result in Redis or DB unique constraint; return cached pricing response on replay. |
| **Write amplification on calculate** | Persist-on-every-calculate grows `orders` quickly and adds write latency to the critical path. | Split **pricing** (read-only, fast) from **order capture** (async): return quote immediately, persist via outbox/Kafka if the business allows eventual persistence; or persist only on “place order”, not on every preview. |
| **Concurrent promotion admin** | Two operators creating the same active `type` can race past `existsActiveByType()`. | Partial unique index + `DataIntegrityViolationException` → `PROMOTION_CONFLICT` (not 500); `@Version` on promotion/coupon rows; `@Retryable` on `PromotionService.create()` for optimistic lock failures. |
| **Large carts** | Buy‑2‑get‑1 and subtotal are O(lines); very large baskets increase CPU per request. | Cap line count at API gateway; batch line processing; pre-aggregate per-SKU quantities before rules run. |
| **Monolith + single Postgres** | Vertical scaling ceiling; long-running migrations block deploys; heavy reporting on the same DB competes with calculate OLTP. | Route promotion/coupon reads to a **read replica** once caching is in place; **partition or archive** old `orders` rows; run analytics from replica or warehouse export — no separate microservice required at this size. |
| **No back-pressure** | Flash sales spike calculate traffic; no rate limiting. | Rate limit per API key/customer at gateway; queue non-interactive repricing; circuit-breaker on DB. |

## Concurrency safety

| Mechanism | Where |
|-----------|--------|
| **Transaction boundaries** | `OrderPricingService.calculate()` — one read-write `@Transactional`; `PromotionService.listActive()` — `readOnly = true`; `PromotionService.create()` — write transaction |
| **Isolation** | Spring default (**PostgreSQL `READ COMMITTED`**): each `calculate()` sees committed promotion/coupon rows as of statement execution; order insert is atomic with the same transaction |
| **Admin duplicate active type** | `existsActiveByType()` + partial unique index `idx_promotions_type_active`; races mapped to **`PROMOTION_CONFLICT`** via `GlobalExceptionHandler` + `DataIntegrityViolationException` |
| **Optimistic locking** | `@Version` on `PromotionEntity` / `CouponEntity` (`version` column in `001-schema.sql`) for safe concurrent updates when update APIs exist |
| **Retry** | `@EnableRetry` + `@Retryable` on `PromotionService.create()` for `OptimisticLockingFailureException` (max 3 attempts, 50 ms backoff) |
| **Proof** | `ConcurrencyIntegrationTest` — parallel promotion create and parallel calculate (requires Docker) |

Concurrent `calculate()` requests do not share mutable pricing state; each persists a new `orders` row. Coupon codes have **no usage limit** in this assignment (unlimited concurrent applies of `SUMMER10` is correct). Under higher load, caching reads, idempotency keys, and optional quote-without-persist matter more than changing rule math.

## Tech stack

Java 17, Spring Boot 3.2, PostgreSQL 16, JPA, Liquibase, SpringDoc OpenAPI, JUnit 5, Mockito, Testcontainers.
