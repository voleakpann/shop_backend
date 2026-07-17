# MiniStore Backend (Microservices)

Spring Boot microservices backend for the **Mini Shop** Next.js frontend.
Google OAuth2 login issues a JWT; the Products API validates that JWT.

## Architecture

```
                 ┌────────────────────┐
 Next.js  ─────► │  api-gateway :8080  │ ──► routes + CORS
 (:3000)         └─────────┬──────────┘
                           │ (service discovery)
        ┌──────────────────┼───────────────────┐
        ▼                   ▼                    ▼
 discovery-server    auth-service :8081   product-service :8082
   (Eureka :8761)   Google login → JWT    Products API (JWT-guarded writes)
                     + PostgreSQL           + PostgreSQL
```

| Service            | Port | Responsibility                                            |
|--------------------|------|-----------------------------------------------------------|
| `discovery-server` | 8761 | Eureka service registry                                   |
| `api-gateway`      | 8080 | Single entry point, routing, CORS for the frontend        |
| `auth-service`     | 8081 | Google OAuth2 login, upserts users, issues a signed JWT   |
| `product-service`  | 8082 | Products REST API; validates the JWT for write operations |
| `order-service`    | 8083 | Orders REST API; every endpoint requires a valid JWT      |

Both `auth-service` and `product-service` share the same HMAC secret
(`ministore.jwt.secret`) so the JWT signed by one is trusted by the other.

## Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL running on `localhost:5432`
- Google OAuth2 credentials (Client ID + Secret)

## 1. Create the databases

```sql
CREATE DATABASE ministore_auth;
CREATE DATABASE ministore_products;
CREATE DATABASE ministore_orders;
```

Default DB user/password is `postgres`/`postgres`. Override with the
`DB_USERNAME` / `DB_PASSWORD` environment variables if different.

## 2. Google OAuth2 credentials

In [Google Cloud Console](https://console.cloud.google.com/) →
APIs & Services → Credentials → **Create OAuth client ID** → Web application.

Add this **Authorized redirect URI** (auth-service handles the callback directly):

```
http://localhost:8081/login/oauth2/code/google
```

Then export the credentials before starting `auth-service`:

```powershell
# PowerShell
$env:AUTH_GOOGLE_ID     = "your-client-id"
$env:AUTH_GOOGLE_SECRET = "your-client-secret"
```

## 3. Run the services (each in its own terminal)

Start them **in this order** (discovery first):

```bash
cd discovery-server && mvn spring-boot:run
cd api-gateway      && mvn spring-boot:run
cd auth-service     && mvn spring-boot:run    # needs the Google env vars
cd product-service  && mvn spring-boot:run
cd order-service    && mvn spring-boot:run
```

## 4. Try it

- Eureka dashboard: <http://localhost:8761>
- **Products (public):**
  - `GET http://localhost:8080/api/products`
  - `GET http://localhost:8080/api/products/iphone-16`
  - `GET http://localhost:8080/api/products?category=Watches`
  - `GET http://localhost:8080/api/products?featured=true`
- **Login with Google:** open in a browser →
  `http://localhost:8081/oauth2/authorization/google`
  After login you are redirected to
  `http://localhost:3000/account?token=<JWT>`.
- **Protected write (needs the JWT):**
  ```bash
  curl -X POST http://localhost:8080/api/products \
    -H "Authorization: Bearer <JWT>" \
    -H "Content-Type: application/json" \
    -d '{"slug":"ipad","name":"iPad","price":599,"category":"Tablets","brand":"Apple","image":"","tags":["Modern"]}'
  ```

## Notes

- `ddl-auto: update` auto-creates tables. For production use migrations (Flyway/Liquibase).
- The shared JWT secret has a dev default; set `JWT_SECRET` (same value for both
  services) in real environments.
- This backend is independent of the frontend's NextAuth setup. To use this
  instead, the frontend would read the `?token=` returned to `/account` and send
  it as `Authorization: Bearer <token>` on API calls.
# shop_backend
