# Phase 2 — Core E-commerce: Summary

Same caveat as Phase 1: no live repo/network access here, so these are the
touched files reconstructed and edited in this sandbox. Apply them to your
actual repos at the matching relative paths, then build/test locally.

## Scope decision

The full Phase 2 wishlist (products, categories, search, filtering, cart,
wishlist, inventory, orders, reviews, tests) is large enough that doing all
of it in one pass risks shipping something I can't actually verify compiles
or behaves correctly. I split it:

**Done this batch** — contained, each independently verifiable:
1. `Category` DTO fix (backend + frontend, coordinated)
2. Product listing pagination (backend + `Home.jsx`)
3. Checkout price/stock revalidation (backend)
4. Inventory concurrency safety via optimistic locking (backend)
5. First real test suite (backend)
6. Two small frontend bugs fixed along the way (see below)

**Deferred to Phase 2b** — genuinely new domain features, not fixes to
existing code, so they deserve their own reviewable batch: **Wishlist** and
**Reviews** (new entities, repositories, services, controllers, and
frontend UI for both). Also deferred: pagination for `/product/search` and
`/seller/products`/`/order/all` (only `/product/all` was paginated this
round, since that's the primary browse path).

## Backend changes

| File | Change |
|---|---|
| `utils/request/AddProductRequest.java`, `UpdateProductRequest.java` | `category` changed from the full `Category` entity to a plain validated `String`. This closes the fragile/implicit mapping flagged in the audit — the frontend was already sending a plain string; now the contract says so explicitly. |
| `service/seller/SellerService.java` | Updated to look up the category by the plain string directly (`categoryRepository.findByName(product.getCategory())`). |
| `model/Product.java` | Added `@Version private Long version;` for optimistic locking. |
| `db/migration/V2__product_version_and_search_index.sql` | **New.** Adds the `version` column, plus `pg_trgm` GIN indexes on `product.name`/`product.brand` so the existing ILIKE-based search can actually use an index. Same baseline-on-migrate caveat as V1 — coordinate this with whoever manages the Neon deploy since it needs to actually *run* there (V1 was baselined, not executed, against the existing DB). |
| `service/product/IProductService.java`, `ProductService.java` | Added `getAllProductsPaged(page, size)`, capped at 60 items/page server-side regardless of what the client requests. |
| `controller/ProductController.java` | `GET /product/all` now accepts `page`/`size` query params (defaults `0`/`12`) and returns a Spring `Page<ProductDto>` instead of the full list. |
| `service/order/OrderService.java` | **Correctness fix.** Order item prices are now snapshotted from the *live* `Product.price` at checkout, not the cart item's `unitPrice` (which was captured once at add-to-cart time and never refreshed — a seller changing price mid-cart previously meant the customer checked out at a stale price). Order total is recalculated from the snapshotted item prices rather than copied from the cart's stale total. Also removed the leftover `System.out.println` debug statements. |
| `exception/GlobalExceptionHandler.java` | Added a handler for `ObjectOptimisticLockingFailureException` → `409 CONFLICT` with a clear "cart changed, please retry" message, for when the new `@Version` field catches a concurrent inventory write. |
| `src/test/java/.../SellerServiceTest.java` | **New.** Regression-protects the Phase 1 IDOR fix: asserts a non-owner gets `ForbiddenException` on update/delete, the owner succeeds, and a missing product still 404s. |
| `src/test/java/.../OrderServiceTest.java` | **New.** Asserts the checkout price fix (order snapshots live price, not stale cart price), correct inventory decrement, and that both insufficient stock and an empty cart throw before anything is persisted. |

## Frontend changes

| File | Change |
|---|---|
| `pages/Home.jsx` | Updated to read `res.data.data.content` / `.totalPages` / `.number` from the now-paginated `/product/all` response, with simple Prev/Next controls. Search (`/product/search`) is untouched and still returns a flat list, so pagination controls are hidden in search mode. |
| `src/App.css.append.txt` | **New, not a real file to commit** — a small CSS block for `.pagination-controls` to paste into your existing `App.css` (didn't want to regenerate your whole stylesheet just to add ~20 lines). |
| `components/AddProduct.jsx` | Categories now fetched from `GET /category/all` instead of a hardcoded `<option>` list. Also: removed the dead unused `SellerProducts` import, and fixed `UploadImage` being called even when no files were selected (was throwing a spurious "Select images" error toast on every text-only add... actually on add it's arguably needed, but harmless either way — see EditProduct for where this mattered more). |
| `pages/EditProduct.jsx` | Same dynamic-category fix. **Bug fix**: `UploadImage` was being called unconditionally on every save, including metadata-only edits with no new files, producing a confusing "Select images" error toast right after a successful update. Now only uploads if `files.length > 0`. |

## Why this order

The category fix had to land before touching product pagination or tests
that construct `UpdateProductRequest`, since the test suite's request
objects use the new `String category` field — doing pagination first would
have meant writing tests against a contract I was about to break.

## How to verify locally

1. Apply the Phase 1 zip first if you haven't already, then these files on top.
2. Run `mvn test` — `SellerServiceTest` and `OrderServiceTest` should pass and are the actual regression check for this phase's two correctness fixes.
3. `docker compose up -d`, run the backend, run the frontend.
4. As a seller, add a product — confirm the category dropdown is now populated from the real category list instead of the old hardcoded 7 options.
5. Edit a product without touching the image field — confirm you no longer get a "Select images" error toast.
6. Add an item to cart, then (as the seller) change that product's price, then check out as the customer — confirm the order total reflects the *new* price, not the price at add-to-cart time.
7. Load the home page — confirm Prev/Next pagination appears once there are more than 12 products, and that it's hidden while searching.

## What's still open (tracked, not silently dropped)

- Wishlist, Reviews — Phase 2b.
- Pagination on `/product/search`, `/seller/products`, `/order/all`.
- Refresh tokens, auth rate limiting, per-controller `@CrossOrigin` cleanup, replacing remaining per-controller try/catch with `GlobalExceptionHandler` — all still Phase 3 as originally scoped.
