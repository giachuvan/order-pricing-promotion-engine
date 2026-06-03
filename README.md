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
│   │   └── PromotionManagementPort
│   └── OrderPricingService, PromotionService
├── domain/
│   ├── model/                Order, Promotion, CouponDiscount, …
│   ├── pricing/              PricingContext, PricingResult
│   └── promotion/            PromotionRule, PromotionChain, PromotionRuleFactory
└── infrastructure/
    ├── persistence/          Entities, repositories, *Adapter implements ports
    └── promotion/              Rule classes, DbPromotionRuleFactory
```

### Port → adapter mapping

| Port | Adapter | Responsibility |
|------|---------|----------------|
| `ActivePromotionPort` | `ActivePromotionAdapter` | Load active promotions as `PromotionDefinition` |
| `CouponResolutionPort` | `CouponResolutionAdapter` | Resolve coupon code → `CouponDiscount` |
| `OrderPersistencePort` | `OrderPersistenceAdapter` | Persist domain `Order` → JPA |
| `PromotionManagementPort` | `PromotionManagementAdapter` | List/create promotions as domain `Promotion` |
| `PromotionRuleFactory` | `DbPromotionRuleFactory` | Build rule pipeline from domain types |

**Flow:** `POST /orders/calculate` → `OrderPricingService` → ports + `PromotionChain` → `OrderPersistencePort.save(Order)`.

**Discount order:** PERCENTAGE → VIP → COUPON → BUY2 (`DbPromotionRuleFactory` javadoc). Independent discounts on original subtotal.

## 3. Design patterns

| Pattern | Where |
|---------|--------|
| **Strategy** | `PromotionRule` + rule implementations in infrastructure |
| **Chain of Responsibility** | `PromotionChain` |
| **Factory** | `PromotionRuleFactory` → `DbPromotionRuleFactory` |
| **Builder** | `PricingResultBuilder` |
| **Repository** | Spring Data (inside adapters only) |
| **Adapter** | All `*Adapter` classes + `GlobalExceptionHandler` |

## 4. SOLID

- **SRP:** One rule per class; adapters only map/persist.
- **OCP:** New promotion = new rule class + factory registration.
- **LSP:** Rules interchangeable via `PromotionRule`.
- **ISP:** Focused ports (`CouponResolutionPort` vs `PromotionManagementPort`).
- **DIP:** Application services depend on abstractions; infrastructure implements them. **No JPA imports in `application`.**

## 5. Database design

- **promotions** — rule config; partial unique index on `(type) WHERE active = true`
- **coupons** — flat discounts by code
- **orders** / **order_items** — UUID order id, BIGSERIAL line id, `line_total` app-calculated
- **products** — reference catalog (optional validation later)

Liquibase: `001-schema.sql`, `002-seed.sql`, `003-promotions-unique-active-index.sql`

## API reference

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/orders/calculate` | Calculate and persist order |
| GET | `/api/v1/promotions` | List active promotions |
| POST | `/api/v1/promotions` | Create promotion (201 Created) |

**Envelope:** `{ "data": ..., "error": null }` or `{ "data": null, "error": { "code", "message" } }`.

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

API: http://localhost:8080

## 7. How to run the tests

```bash
mvn test
```

| What runs | Notes |
|-----------|--------|
| **Unit tests** (`pricing.application`, `pricing.domain`, `pricing.infrastructure.*`) | Always run; no Spring context on service/rule tests (Mockito mocks ports). |
| **Integration test** (`pricing.integration.OrderCalculateIntegrationTest`) | Picked up by the same `mvn test` (Surefire). Uses Testcontainers + `@SpringBootTest`. |

**Docker:** Integration tests use `@Testcontainers(disabledWithoutDocker = true)`. If Docker is **not** running, that class is **skipped** and the build still **passes** — you only get unit coverage. Start Docker (daemon reachable) before `mvn test` if you want the full calculate endpoint asserted against real PostgreSQL.

`mvn verify` is not required for this project (no Failsafe `integration-test` phase); it runs the same tests as `test`, then packages the app.

## Assignment checklist

| Requirement | Status |
|-------------|--------|
| Challenge 2 — Order Pricing & Promotion Engine | Done |
| REST API — 3 endpoints + envelope | Done |
| 4 promotion rules (Strategy + Factory + Chain) | Done |
| PostgreSQL + Liquibase SQL | Done |
| `docker compose up` | Done |
| Unit + integration tests | Done |
| `finalPrice` 102.50 | Done |

## 8. Trade-offs

- **Discount stacking:** Independent discounts on original subtotal (per assignment example).
- **DTOs in application:** Services accept API DTOs (`CalculateOrderRequest`, `CreatePromotionRequest`) directly to avoid a thin mapping layer with no added logic at assignment scale. Domain types (`Order`, `PricingContext`, `PromotionDefinition`) are used inside pricing and persistence. In a larger codebase, dedicated application commands/queries would sit between the API layer and services, with mapping at the controller or a small assembler.
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
| **Concurrent promotion admin** | Two operators creating the same active `type` can race; app check + DB partial unique index (`003-promotions-unique-active-index.sql`) — one fails with conflict (correct) but UX is 400 under load. | Keep DB constraint as source of truth; return clear `PROMOTION_CONFLICT`; optional retry with `active: false` on old row + activate new in one transactional admin API. |
| **Large carts** | Buy‑2‑get‑1 and subtotal are O(lines); very large baskets increase CPU per request. | Cap line count at API gateway; batch line processing; pre-aggregate per-SKU quantities before rules run. |
| **Monolith + single Postgres** | Vertical scaling ceiling; long-running migrations block deploys; heavy reporting on the same DB competes with calculate OLTP. | Route promotion/coupon reads to a **read replica** once caching is in place; **partition or archive** old `orders` rows; run analytics from replica or warehouse export — no separate microservice required at this size. |
| **No back-pressure** | Flash sales spike calculate traffic; no rate limiting. | Rate limit per API key/customer at gateway; queue non-interactive repricing; circuit-breaker on DB. |

**Concurrency (matches current code):** `calculate()` uses a **single read-write transaction** — not a separate `@Transactional(readOnly = true)` phase for loading promotions. Concurrent calculate requests do not corrupt promotion config (reads are consistent within the transaction), but each request still contends for connections and row inserts on `orders`. `PromotionService.listActive()` is read-only; `create()` plus the partial unique index in `003-promotions-unique-active-index.sql` prevent duplicate active types under admin races. Under higher load, caching promotion/coupon reads, idempotency on calculate, and optional “quote without persist” matter more than changing rule math.

## Tech stack

Java 17, Spring Boot 3.2, PostgreSQL 16, JPA, Liquibase, JUnit 5, Mockito, Testcontainers.
