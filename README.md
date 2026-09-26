# StockSense

StockSense is a full-stack inventory management system for tracking products, locations, stock operations, and an auditable movement ledger. The Spring Boot API and MySQL database are the source of truth; the React client reads and mutates inventory only through REST APIs.

> Hackathon/local-development project. Review security, migrations, email delivery, observability, and deployment settings before production use.

## Features

- JWT signup/login, protected API routes, BCrypt password storage, user roles, logout, and a local-development OTP password-reset flow.
- Product and category catalog; multi-warehouse locations; supplier and customer directories.
- Draft receipts, deliveries, internal transfers, and physical-count adjustments. Drafts do not change stock.
- Transactional validation with locked locations/stocks, available-quantity checks, and append-only movement records.
- Dashboard KPIs, movement and category charts, low-stock information, pending operations, and searchable inventory views.
- Responsive React UI, typed TypeScript, Axios API services, route protection, form feedback, and retryable loading/error states.
- OpenAPI/Swagger API documentation.

## Technology

- Frontend: React 18, Vite, TypeScript, Tailwind CSS, React Router, Axios, Lucide React, Recharts.
- Backend: Java 17, Spring Boot 3, Spring Web, Spring Data JPA, Spring Security, JWT, Bean Validation, Maven, Springdoc OpenAPI.
- Database: MySQL 8+.

## Project layout

```text
frontend/   Vite React application
backend/    Spring Boot REST API
README.md   This guide
```

## Requirements

- Java 17+
- Maven 3.8+
- Node.js 20+ and npm
- MySQL 8+

## Database setup

Start MySQL and create the database (or use the configured local account's create-database permission):

```sql
CREATE DATABASE stocksense CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

The API defaults to `jdbc:mysql://localhost:3306/stocksense`, user `root`, and an empty password for local convenience. Set environment variables rather than committing credentials:

- `DB_URL` (optional; full JDBC URL)
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET` (use a strong random secret of at least 32 bytes outside local development)
- `PORT` (optional, default `8080`)
- `CORS_ORIGIN` (optional, default `http://localhost:5173`)
- `RESET_EXPOSE_OTP` (defaults to `true` for local OTP testing; set `false` in deployments with an OTP delivery provider)

## Backend setup

From `backend/`:

```bash
mvn spring-boot:run
```

Package the API with `mvn clean package`. The server listens at `http://localhost:8080` by default. Hibernate creates/updates the local schema (`ddl-auto=update`); use versioned migrations and disable automatic DDL updates for production.

### Development login

When the domain tables are empty, startup seeds an administrator and sample catalog/opening stock:

- Email: `admin@stocksense.com`
- Password: `StocksenseDev!2026`

The seed is skipped if domain data already exists. Change or remove this credential before deployment. New self-registered accounts use the `WAREHOUSE_STAFF` role; catalog changes require `ADMIN` or `INVENTORY_MANAGER`, and catalog deletion requires `ADMIN`.

### Inventory workflow

1. Create draft receipt/delivery/transfer/adjustment documents through the API.
2. Draft documents leave stock unchanged.
3. Validate a receipt to add item quantities; a delivery must be picked, packed, and validated to subtract quantities.
4. Validate a transfer to subtract at the source and add at the destination.
5. Create an adjustment with a physical count. At validation, the service compares that count with the latest database quantity and records the difference.
6. Every validated stock change creates ledger entries in the same database transaction. Insufficient stock rejects delivery/transfer/negative adjustment updates.

For demonstration, an OTP is returned by the forgot-password API while `RESET_EXPOSE_OTP=true`; the OTP is held in memory for ten minutes and is cleared on API restart. The application does not send actual email/SMS.

## Frontend setup

The local `frontend/.env` file is configured to use the local API. The committed example is `frontend/.env.example`:

```env
VITE_API_URL=http://localhost:8080/api
```

From `frontend/`:

```bash
npm install
npm run dev
```

Vite serves the UI at `http://localhost:5173`. Create a production bundle with `npm run build`; preview it with `npm run preview`. The Axios client attaches the stored bearer token and uses the `VITE_API_URL` value (with a localhost fallback).

## Run locally

1. Start MySQL and create the `stocksense` database.
2. Start the backend in one terminal with `cd backend && mvn spring-boot:run`.
3. Start the frontend in another terminal with `cd frontend && npm install && npm run dev`.
4. Open `http://localhost:5173` and log in using the development account above.

## API overview

Protected routes require `Authorization: Bearer <JWT>`. Authentication and OpenAPI endpoints are public.

| Area | Endpoints |
| --- | --- |
| Authentication | `POST /api/auth/register`, `/login`, `/forgot-password`, `/verify-otp`, `/reset-password` |
| Catalog | `/api/products`, `/api/categories`, `/api/warehouses`, `/api/locations`, `/api/suppliers`, `/api/customers` |
| Stock | `GET /api/stock`, `/api/stock/low`, `/api/ledger` |
| Receipts | `GET/POST /api/receipts`, `GET /api/receipts/{id}`, `POST /api/receipts/{id}/validate` |
| Deliveries | `GET/POST /api/deliveries`, `POST /api/deliveries/{id}/pick`, `/pack`, `/validate` |
| Transfers | `GET/POST /api/transfers`, `POST /api/transfers/{id}/validate` |
| Adjustments | `GET/POST /api/adjustments`, `POST /api/adjustments/{id}/validate` |
| Dashboard | `GET /api/dashboard/stats`, `/movements`, `/category-stock`, `/low-stock` |

Swagger UI: `http://localhost:8080/swagger-ui.html` (OpenAPI JSON: `/v3/api-docs`). API errors use a JSON response with timestamp, status, message, and path.

## Troubleshooting

- **Frontend cannot reach the backend:** confirm the API is running on port 8080, `frontend/.env` contains the correct `VITE_API_URL`, and restart Vite after changing environment values.
- **CORS error:** set `CORS_ORIGIN` to the exact frontend origin (including scheme and port).
- **MySQL connection failure:** check MySQL is running, database name, JDBC URL, credentials, and create-database privileges.
- **No seeded demo account/data:** the seed only runs on an empty domain database; existing databases are not overwritten.
- **Reset code unavailable:** local OTP is returned only when `RESET_EXPOSE_OTP=true`; OTPs expire after ten minutes and are lost on server restart.
- **403 on catalog writes:** these require `ADMIN` or `INVENTORY_MANAGER`; destructive catalog deletes require `ADMIN`.
