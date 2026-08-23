# Phase 1 — Foundation: Summary

I don't have live access to your GitHub repos or a network connection in this
environment, so these files were reconstructed from what's in this
conversation and edited here. **Apply them to your actual `E-commerce-app`
repo** (same relative paths), then run the build/tests yourself — I could not
run `mvn` here (no dependency resolution without network access).

## Files changed

| File | Change |
|---|---|
| `pom.xml` | Java 17 → 21 (`java.version`, compiler source/target, `maven-compiler-plugin` `<release>`). Added `flyway-core` + `flyway-database-postgresql`. |
| `Dockerfile` | Base images `eclipse-temurin:17-jdk`/`maven:...-temurin-17` → `...-temurin-21`. |
| `docker-compose.yaml` | MySQL → PostgreSQL 16, with a named volume, healthcheck, and env-var-driven credentials (no more hardcoded MySQL root password). |
| `.env.example` | **New.** Documents local + prod env vars without secrets. |
| `SecurityConfig.java` | Fixed `/api/v1/cart` and `/api/v1/order` matchers to `/api/v1/cart/**` / `/api/v1/order/**` (previously only matched the literal path, so sub-routes were falling through to `permitAll()` and only `@PreAuthorize` was protecting them). Locked down `/actuator/**` to `ROLE_ADMIN` (except `/actuator/health`). Tightened `/api/v1/image/**` to `authenticated()` except the public download route. CORS origins now come from `cors.allowed-origins` (env-overridable), defaulting to the same values as before. |
| `application.properties` | `ddl-auto` `update` → `validate`. Added Flyway config. `management.endpoints.web.exposure.include` `*` → `health,info`. Added `cors.allowed-origins`. |
| `db/migration/V1__baseline.sql` | **New.** Baseline schema reconstructed from the entities. **Read the comment at the top of the file** — because `baseline-on-migrate=true`, this will *not* run against your existing Neon DB (it gets baselined, not migrated); it's what actually creates the schema on a fresh local Docker Postgres / CI database. |
| `SellerService.java` | **Security fix.** `updateProduct` / `deleteProductById` now verify `product.getSeller().getId()` matches the authenticated user before acting — previously any authenticated SELLER could edit/delete *any* product by ID. |
| `ImageService.java` | **Security fix.** `saveImages` / `updateImage` / `deleteImageById` now verify the target product belongs to the authenticated seller. Added `UserRepository` dependency to resolve the current user. |
| `User.java` | `roles` field changed from a bare `@Enumerated` on a `Set<UserRole>` (not a valid JPA mapping on its own) to an explicit `@ElementCollection` backed by a `user_roles` table — needed for `ddl-auto=validate` to have something well-defined to check, and to make the baseline migration correct. **Verify this against your real Neon schema before flipping Neon itself to `validate` mode** — see the note in the migration file. |
| `exception/GlobalExceptionHandler.java` | **New.** `@RestControllerAdvice` producing the standardized `{timestamp, status, code, message, path, errors}` shape for validation errors, the new `ForbiddenException`, `AccessDeniedException`, and any unhandled exception (which now returns a safe generic message instead of `e.getMessage()`). |
| `utils/exceptions/ForbiddenException.java` | **New.** Distinct from `ResourceNotFoundException` — thrown when a resource exists but the caller doesn't own it. |
| `utils/response/ApiError.java` | **New.** The standardized error DTO. |

## Explicitly NOT done in this phase (by design, deferred)

- **Category DTO fix** (`AddProductRequest.category`/`UpdateProductRequest.category` typed as the full `Category` entity, frontend sends a plain string) — this needs backend + frontend changed together in the same deploy or it breaks the app mid-flight. Scheduled for Phase 2 alongside the catalog work.
- **Refresh tokens / short-lived access tokens** — auth contract change touching both sides; scheduled for Phase 3 (Auth & Security) as originally planned.
- **Rate limiting on `/auth/**`** — also Phase 3.
- **Removing per-controller `try/catch` blocks** in favor of always relying on `GlobalExceptionHandler` — left in place for now since most of them already work correctly; consolidating all controllers is a mechanical but broad change I'd rather do as its own reviewable step, not bundled silently into Phase 1.
- **Per-controller `@CrossOrigin(origins = "...")` annotations** — still present on every controller alongside the new global CORS bean. They're redundant now but harmless (same origin list). Removing them touches every controller file for a cosmetic win, so deferred to a cleanup pass.

## How to verify locally

1. Copy these files into your actual repo at the same relative paths.
2. `cp .env.example .env` and fill in real local values (generate `JWT_SECRET` with `openssl rand -base64 48`).
3. `docker compose up -d` — Postgres should come up healthy.
4. `mvn spring-boot:run` (or your existing run command) with the `.env` values exported, or point your IDE's run config at them.
5. Confirm the app starts cleanly — this is the real test of the `User.roles` mapping change and `ddl-auto=validate`; if it fails to start with a schema/entity mismatch, that's exactly the ambiguity flagged above and worth fixing before moving on.
6. Manually verify the fix: log in as Seller A, note a product ID belonging to Seller B (or create two seller accounts), and confirm `PUT /api/v1/seller/update/{sellerBsProductId}` and `DELETE /api/v1/seller/delete/{sellerBsProductId}` now return `403 FORBIDDEN` instead of succeeding.
7. Hit `GET /actuator/beans` without a token — should now be `403`/`401` instead of returning data.

## Tests

Given the "no meaningful test coverage" finding in the audit, Phase 1 didn't add tests yet — the roadmap has testing landing per-feature in Phases 2–4 rather than as a bolt-on afterward. If you'd like, I can add a focused security/authorization test suite (asserting the ownership fix and the actuator lockdown) as the first concrete tests in the project before moving to Phase 2 — that would also give you regression protection for exactly the bugs just fixed.
