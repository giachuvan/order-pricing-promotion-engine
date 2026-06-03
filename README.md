# Order Pricing & Promotion Engine

Spring Boot 3 service that calculates order totals by applying composable promotion rules loaded from PostgreSQL.

## Architecture overview

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

## Design patterns

| Pattern | Where |
|---------|--------|
| **Strategy** | `PromotionRule` + rule implementations in infrastructure |
| **Chain of Responsibility** | `PromotionChain` |
| **Factory** | `PromotionRuleFactory` → `DbPromotionRuleFactory` |
| **Builder** | `PricingResultBuilder` |
| **Repository** | Spring Data (inside adapters only) |
| **Adapter** | All `*Adapter` classes + `GlobalExceptionHandler` |

## SOLID

- **SRP:** One rule per class; adapters only map/persist.
- **OCP:** New promotion = new rule class + factory registration.
- **LSP:** Rules interchangeable via `PromotionRule`.
- **ISP:** Focused ports (`CouponResolutionPort` vs `PromotionManagementPort`).
- **DIP:** Application services depend on abstractions; infrastructure implements them. **No JPA imports in `application`.**

## Database design

- **promotions** — rule config; partial unique index on `(type) WHERE active = true`
- **coupons** — flat discounts by code
- **orders** / **order_items** — UUID order id, BIGSERIAL line id, `line_total` app-calculated
- **products** — reference catalog (optional validation later)

Liquibase: `001-schema.sql`, `002-seed.sql`, `003-promotions-unique-active-index.sql`

## API

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

## Run the system

```bash
docker compose up --build
```

API: http://localhost:8080

## Run tests

```bash
mvn test
```

Unit tests mock ports (no Spring context). Integration test uses Testcontainers (`disabledWithoutDocker`).

## Assignment checklist

| Requirement | Status |
|-------------|--------|
| REST API — 3 endpoints + envelope | Done |
| 4 promotion rules (Strategy + Factory + Chain) | Done |
| PostgreSQL + Liquibase SQL | Done |
| `docker compose up` | Done |
| Unit + integration tests | Done |
| `finalPrice` 102.50 | Done |

## Trade-offs

- **Discount stacking:** Independent discounts on original subtotal (per assignment example).
- **DTOs in application:** Services accept API DTOs for Spring pragmatism; domain used for persistence and pricing.
- **No promotion PUT / coupon CRUD:** Out of assignment scope.
- **Integration tests:** May use `OrderRepository` directly at the outer test boundary to verify DB state.

## Scale considerations

- Cache active promotions; idempotency keys for calculate retries; async persist if needed.

## Tech stack

Java 17, Spring Boot 3.2, PostgreSQL 16, JPA, Liquibase, JUnit 5, Mockito, Testcontainers.
