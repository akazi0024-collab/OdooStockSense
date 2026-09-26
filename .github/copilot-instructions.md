# StockSense workspace guidance

## Project-specific rules

- Keep the React/Vite client in `frontend/` and the Spring Boot API in `backend/`.
- Treat MySQL and backend service logic as the source of truth for inventory; do not simulate successful stock operations in React.
- Route all frontend API calls through service modules and the Axios client. Keep the API origin in `VITE_API_URL`.
- Keep controllers thin, expose DTOs rather than JPA entities, validate inputs, and protect stock-changing actions with database transactions.
- Every validated stock change must update stock and append ledger entries atomically.
- Do not commit real credentials, production secrets, or customer data.

## Project setup checklist

- [x] Clarify requirements: React + TypeScript/Vite/Tailwind and Java 17/Spring Boot/MySQL.
- [x] Scaffold separated frontend and backend directories.
- [x] Implement core inventory, authentication, dashboard, and API integration.
- [x] Add local development environment configuration and README instructions.
- [ ] Install required extensions: none required by this project setup.
- [x] Install frontend packages and compile the React/Vite production bundle successfully; backend compilation remains blocked because Java and Maven are unavailable.
- [x] Add a VS Code task for the frontend dev server and start the site locally. The backend/database still need Java, Maven, and MySQL before authentication and API-backed pages can operate.
- [x] Document local setup and launch instructions in the root README.
