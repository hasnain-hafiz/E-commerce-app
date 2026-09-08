# Phase 2b — Wishlist + Reviews: Summary

Same working method as before: no live repo/network access, so these are
edited/new files to drop into your real repos at matching paths, then
build/test yourself.

## New backend feature: Wishlist

- `model/WishlistItem.java` — join entity (`user_id`, `product_id`, `added_at`), unique constraint on `(user_id, product_id)` so a product can't be double-wishlisted at the DB level, not just in application code.
- `repository/WishlistItemRepository.java`
- `service/wishlist/{IWishlistService,WishlistService}.java` — `addToWishlist` is **idempotent**: re-adding an already-wishlisted product returns the existing entry instead of erroring, since the frontend treats it as a toggle.
- `controller/WishlistController.java` — `GET/POST/DELETE /api/v1/wishlist[/​{productId}]`, all `ROLE_CUSTOMER`, all scoped to the authenticated user (no `userId` path param to tamper with — same ownership pattern as cart).
- `db/migration/V3__wishlist_and_reviews.sql`
- `src/test/.../WishlistServiceTest.java`

## New backend feature: Reviews

- `model/Review.java` — `rating` (1–5), optional `comment`, unique constraint on `(user_id, product_id)`.
- **Verified-purchase gate**: `OrderRepository.hasPurchased(...)` (new query) checks the user has a non-cancelled order containing the product before `ReviewService.addReview` allows a review. Deliberately checks "purchased" rather than "delivered" — there's no admin order-status-update flow yet (that's Phase 3+), so requiring `DELIVERED` would make reviews undemoable today. Revisit once admin order management ships.
- Duplicate prevention: one review per user per product (`AlreadyExistsException` if violated).
- `deleteReview`: owner or `ROLE_ADMIN` only (moderation path), everyone else gets `ForbiddenException`.
- `controller/ReviewController.java` — `GET /review/product/{id}` is public (product pages show reviews to guests); `POST`/`DELETE` require auth.
- `db/migration/V3__wishlist_and_reviews.sql` (same file as wishlist — both landed together since they're both small, additive tables).
- `src/test/.../ReviewServiceTest.java` — covers the not-purchased rejection, the duplicate rejection, success path, and the delete-ownership rule.

## Product rating summary

- `ProductDto` gained `averageRating` (nullable — `null` means "no reviews yet", not "rated 0") and `reviewCount`.
- Both `ProductService.convertToDto` and `SellerService.convertToDto` (the two places that build a `ProductDto`, previously near-duplicates of each other) now populate these via `ReviewRepository`.
- **Known tradeoff, noted not hidden**: this adds one more query per product on top of the existing per-product image query, so a page of N products now does roughly 2N+1 queries. Consistent with the existing pattern (images were already done this way) rather than a new problem — flagged for batch-loading if the catalog grows enough to matter (Performance phase).

## Security

- `SecurityConfig`: added `/api/v1/wishlist/**` → `authenticated()`, `/api/v1/review/**` → `GET` public / everything else `authenticated()`. Same pattern as the `/image` download-vs-upload split from Phase 1, applied consistently instead of repeating the original mistake of leaving a whole path prefix wide open.
- `GlobalExceptionHandler`: added `ReviewNotAllowedException` → `403`.

## Frontend

| File | Change |
|---|---|
| `context/WishlistContext.jsx` | **New.** Same shape as `CartContext` — fetches on mount/auth-change, exposes `isWishlisted`/`toggleWishlist`. |
| `pages/Wishlist.jsx` | **New.** Reuses `ProductCard` for consistency with the home grid. |
| `components/ProductCard.jsx` | Added a heart toggle button (top-right overlay) and a star-rating line, shown only when `reviewCount > 0`. |
| `pages/ProductDetails.jsx` | Added the wishlist toggle next to the product title, the rating summary, and a full reviews section: list of existing reviews + a submit form (rating select + optional comment) shown to logged-in non-seller users. Server-side gating (must have purchased, one review per product) is authoritative — the frontend just shows the resulting error toast if the backend rejects it, rather than trying to duplicate that logic client-side. |
| `App.jsx` | Wrapped the app in `WishlistProvider` alongside `CartProvider`, added the `/wishlist` route under `PrivateRoute`. |
| `pages/Home.jsx` | Added a "Wishlist" nav button next to "My Orders". |
| `src/App.css.append.txt`, `src/App.css.phase2b.append.txt` | **Not real files** — CSS snippets to paste into your actual `App.css` (pagination controls from Phase 2, wishlist/review styles from this phase). Note `.product-card` needs `position: relative` added to its existing rule for the wishlist button to anchor correctly — called out at the top of the phase2b file. |

## How to verify locally

1. Apply Phase 1 → Phase 2 → this phase's files in order (Phase 2b's `V3` migration assumes `V2`'s `product.version` column already exists).
2. `mvn test` — `WishlistServiceTest` and `ReviewServiceTest` should pass.
3. As a customer, click the heart on a product card — confirm it appears on `/wishlist` and toggles off when clicked again.
4. Try to submit a review for a product you haven't ordered — confirm you get a clear "you can only review products you've purchased" error, not a silent success.
5. Place an order for a product, then review it — confirm it succeeds, appears in the reviews list, and the average rating shown on the product card/detail page updates.
6. Try submitting a second review for the same product — confirm you get an "already reviewed" error.
7. As a different customer (or admin), confirm you can't delete someone else's review, but an admin account can.

## Still open (tracked, not dropped)

- Pagination for `/review/product/{id}` and `/wishlist` — both currently return full lists; fine at this app's scale, deferred alongside the other pagination items from Phase 2.
- Admin review moderation UI (the backend supports admin delete; no admin frontend exists yet — that's Phase 9/10 admin dashboard work).
- Refresh tokens, auth rate limiting, per-controller `@CrossOrigin`/try-catch cleanup — still Phase 3 as originally scoped.
