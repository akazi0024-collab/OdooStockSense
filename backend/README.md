# StockSense backend

Java 17 / Spring Boot 3 REST API backed by MySQL and JPA. API docs are served at `/swagger-ui.html` and `/v3/api-docs`.

## Run

1. Create a MySQL database (or allow the configured local MySQL user to create `stocksense`).
2. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and a strong `JWT_SECRET` (at least 32 bytes). Defaults are for local development only.
3. From this directory run `mvn spring-boot:run`; build with `mvn clean package`.

Optional settings: `PORT` (8080), `JWT_EXPIRATION_MS` (86,400,000), `CORS_ORIGIN` (`http://localhost:5173`), and `app.reset.expose-otp` (defaults to `true` for local development; set it to `false` outside development and connect a delivery provider before using password recovery). Hibernate uses `ddl-auto: update`; use migrations and disable automatic schema changes for production.

## Development seed

On a completely empty domain database, startup inserts demo categories, products, warehouse/location and opening stock with ledger entries, plus:

- Email: `admin@stocksense.com`
- Password: `StocksenseDev!2026`

Change/remove this account and the development secret before deploying. The seed is skipped when any domain data exists.

## Authentication

`POST /api/auth/register` accepts `{ "name", "email", "password" }`; `POST /api/auth/login` accepts `{ "email", "password" }`. Both return a JWT and account details. Send `Authorization: Bearer <token>` to protected routes. Registration creates a STAFF account; the empty-database seed creates the ADMIN account. Passwords are BCrypt hashed.

For local reset, call `POST /api/auth/forgot-password` with `{ "email" }`, then `POST /api/auth/reset-password` with `{ "email", "otp", "newPassword" }`. OTPs expire after ten minutes and are held in memory, so a restart invalidates them. The OTP is included in the response only while `app.reset.expose-otp=true`; wire a real delivery provider for production.

## API surface

All endpoints other than `/api/auth/**` and OpenAPI docs require a bearer token.

- Catalog CRUD: `/api/products`, `/api/categories`, `/api/warehouses`, `/api/locations`, `/api/suppliers`, `/api/customers` (`GET`, `POST`, `PUT /{id}`, `DELETE /{id}`; products and categories also have `GET /{id}`).
- Stock: `GET /api/stock?locationId=&productId=`, `GET /api/stock/low`, `GET /api/ledger?locationId=&size=`.
- Receipts: `GET/POST /api/receipts`, `GET /api/receipts/{id}`, `POST /api/receipts/{id}/validate`.
- Deliveries: `GET/POST /api/deliveries`, `GET /api/deliveries/{id}`, then `POST /{id}/pick`, `POST /{id}/pack`, `POST /{id}/validate`.
- Transfers and adjustments: `GET/POST /api/transfers` and `/api/adjustments`; validate with `POST /{id}/validate`.
- Dashboard: `GET /api/dashboard/stats`, `/movements`, `/category-stock`, `/low-stock`.

Document creation is draft-only and does not change stock. Only successful validation changes on-hand balances and appends ledger entries. Deliveries must progress DRAFT → PICKED → PACKED → VALIDATED. A validation is atomic, checks available quantity, locks locations/stocks/documents against concurrent operations, and cannot be applied twice. Ledger records are append-only in the application model. Validation and server errors use a consistent JSON error shape.
