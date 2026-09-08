# Phase 3 — Authentication & Security: Summary

Same working method as before: no live repo/network access, so these are
edited/new files to drop into your real repos at matching paths, then
build/test yourself.

## Refresh tokens + short-lived access tokens

- **`JwtService`**: access token TTL was hardcoded to `100*60*60*10` ms (~41 days). Now defaults to 15 minutes, configurable via `jwt.access-token-expiration-ms`.
- **`RefreshToken`** (new entity): opaque `UUID` string, not a JWT — stored server-side specifically so it can be individually revoked, unlike a stateless JWT. Default 7-day expiry, configurable via `jwt.refresh-token-expiration-days`.
- **`RefreshTokenService`**: `createRefreshToken`, and `verifyAndRotate` — validates the token (not revoked, not expired) then **rotates** it: the old one is revoked and a new one issued in the same call. This limits how long a leaked refresh token stays useful. Noted, not silently skipped: a fully hardened implementation would treat *reuse of an already-revoked token* as a signal of possible theft and revoke the whole session family — not implemented here, flagged as a real follow-up.
- **`POST /api/v1/auth/refresh`**: exchanges a refresh token for a new access token + rotated refresh token.
- `AuthResponse` gained a `refreshToken` field; `register`/`authenticate`/`refreshAccessToken` all return it now.
- **`db/migration/V4__refresh_tokens_and_password_reset.sql`**.
- `RefreshTokenServiceTest`: rotation, revoked-token rejection, expired-token rejection, unknown-token rejection.

## Password reset

- **`PasswordResetToken`** (new entity): one-time, expiring (`password-reset.token-expiration-minutes`, default 30), single-use.
- **`PasswordResetService`**: `requestReset(email)` is **enumeration-safe** — it always behaves identically to the caller whether or not the email exists (the token/email are only actually created/"sent" internally if the account is real). `resetPassword(token, newPassword)` validates the token, updates the password, marks the token used, and **revokes all existing access tokens and refresh tokens for that user** — a password reset should end every existing session, including the one used to request it.
- **`IEmailService` / `ConsoleEmailService`**: there's no real email provider wired up (that's a genuine infra decision — SMTP/SendGrid/SES account, API key, verified sender domain — not something to fake in this environment). `ConsoleEmailService` logs the reset link to the backend console instead. `IEmailService` is the seam for swapping in a real provider later without touching `PasswordResetService`.
- **Endpoints**: `POST /api/v1/auth/forgot-password`, `POST /api/v1/auth/reset-password`.
- `PasswordResetServiceTest`: request-with-existing-email, request-with-nonexistent-email (must not throw or leak), successful reset, session revocation on reset, used-token rejection, expired-token rejection.
- **Frontend**: new `ForgotPassword.jsx` / `ResetPassword.jsx` pages, wired into `App.jsx` as ungated public routes (a logged-in user can still reach them — relevant if they suspect their account is compromised), and a "Forgot password?" link added to `Login.jsx`.

## Rate limiting

- **`RateLimitingFilter`** (new): hand-rolled, in-memory, fixed-window limiter (8 requests/minute/IP) on `/auth/authenticate`, `/auth/register`, `/auth/forgot-password`. **Deliberately not a library** (e.g. bucket4j) — this environment has no network access to verify current artifact coordinates/API against up-to-date docs, and an unverified dependency is worse than a small, readable hand-rolled version. **Explicitly flagged limitation**: this is per-instance in-memory state, so it does **not** coordinate across multiple backend instances behind a load balancer. Fine for Render's current single-instance setup; revisit with bucket4j+Redis if the app is ever scaled horizontally.
- Wired into `SecurityConfig` via `addFilterBefore(new RateLimitingFilter(), JwtFilter.class)` — deliberately *not* a `@Component`, to avoid Spring Boot potentially double-registering it (once via its own filter auto-registration, once via the explicit Security chain wiring).

## Security headers

- `SecurityConfig` now explicitly configures `X-Content-Type-Options`, `X-Frame-Options: DENY`, HSTS (1 year, includeSubDomains), and `Referrer-Policy: strict-origin-when-cross-origin`. The first two were already Spring Security defaults even before this change; made explicit rather than relying on implicit behavior, and the referrer policy is a genuinely new addition (not part of the defaults).

## Cleanup finally landed this phase

- **`@CrossOrigin` removed from every controller** (`AuthController`, `CartController`, `OrderController`, `SellerController`, `ImageController`, `CategoryController`, `HealthController`, plus `ProductController`/`WishlistController`/`ReviewController` from earlier phases) — this had been deferred twice. CORS is handled once, globally, by `SecurityConfig.corsConfigurationSource()`; the per-controller annotations were redundant duplicates of the same origin list.
- **Fixed a real information-leak bug found while touching `AuthController`**: failed login previously returned the raw exception message in the response's `data` field. Now always a generic `"Invalid Email or Password"` with `data: null`. This surfaced a **second, pre-existing frontend bug**: `Login.jsx` was reading `err.response?.data?.data` for its error toast (the wrong field — the message has always lived in `.message`), so it would have shown literally `"undefined"` once the leak was fixed. Both are fixed together in this batch since one exposed the other.

## Still explicitly deferred (not silently dropped)

- **Per-controller `try`/`catch` → `GlobalExceptionHandler` consolidation.** This is more invasive than the `@CrossOrigin` cleanup (touches every controller method's body, not just a class-level annotation), and bundling it into this already-large security batch would make mistakes harder to spot. Re-scoped explicitly to its own reviewable pass — either early Phase 4 or the Phase 10 polish pass.
- `CategoryController` still accepts/returns the raw `Category` entity (no DTO) — lower severity than everything else in this batch (admin-only endpoints), flagged again for a future cleanup.
- Refresh-token-reuse-detection (see above).
- Multi-instance-safe rate limiting (see above).

## How to verify locally

1. Apply Phases 1 → 2 → 2b → this phase's files in order (`V4` migration assumes `V1`-`V3` already applied).
2. `mvn test` — new suites: `RefreshTokenServiceTest`, `PasswordResetServiceTest`, plus everything from prior phases should still pass unchanged.
3. Log in — confirm the response now includes a `refreshToken`, and that it's persisted (check `localStorage`).
4. Manually shrink `jwt.access-token-expiration-ms` to something tiny (e.g. `10000` = 10s) locally, wait past it, then make an authenticated request — confirm the app silently refreshes and the request still succeeds, instead of logging you out.
5. Call `POST /auth/forgot-password` with a real account's email — check the backend console log for the reset link (no real email will send). Use that link's token against `POST /auth/reset-password` — confirm the password changes and old tokens stop working.
6. Hammer `POST /auth/authenticate` with bad credentials more than 8 times in a minute from the same client — confirm you start getting `429 Too Many Requests`.
7. Open browser dev tools → Network tab on any page load — confirm response headers include `X-Frame-Options: DENY` and (over HTTPS in production) `Strict-Transport-Security`.
