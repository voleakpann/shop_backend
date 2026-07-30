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
| `comment-service`  | see compose | Comments/replies on a slug; reads public, posting needs a JWT |
| `blog-service`     | 8085 | Blog posts REST API; reads public, authoring is ADMIN-only |

Both `auth-service` and `product-service` share the same HMAC secret
(`ministore.jwt.secret`) so the JWT signed by one is trusted by the other.

## Quick Start (Docker Compose) — Recommended

```bash
# 1. Copy the example env file and fill in your secrets
cp .env.example .env

# Edit .env and set:
# - DB_USERNAME, DB_PASSWORD (database)
# - JWT_SECRET (generate with: openssl rand -base64 32)
# - AUTH_GOOGLE_ID, AUTH_GOOGLE_SECRET (from Google Cloud Console)
# - FRONTEND_BASE_URL (http://localhost:3000 for local dev)
# - MINISTORE_FRONTEND_REDIRECT_URI (http://localhost:3000/account for local dev)

# 2. Start all services
docker compose up --build

# 3. Services are now running on:
#    - API Gateway: http://localhost:8000
#    - Auth Service: http://localhost:8081
#    - Product Service: http://localhost:1000
#    - Order Service: http://localhost:9002
#    - Comment Service: port 9003
#    - PostgreSQL: localhost:5432
```

## Prerequisites

- Java 17+ (for manual Maven setup)
- Maven 3.9+ (for manual Maven setup)
- Docker & Docker Compose (recommended)
- PostgreSQL running on `localhost:5432` (if not using Docker)
- Google OAuth2 credentials (Client ID + Secret)

## Manual Setup (Maven)

## 1. Create the databases

```sql
CREATE DATABASE ministore_auth;
CREATE DATABASE ministore_products;
CREATE DATABASE ministore_orders;
CREATE DATABASE ministore_comments;
CREATE DATABASE ministore_blog;
```

`db/init.sql` runs these automatically — but **only the first time the Postgres
volume is created**. If your volume already exists, add new databases by hand:

```powershell
docker compose exec postgres psql -U <user> -c "CREATE DATABASE ministore_blog;"
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
cd comment-service  && mvn spring-boot:run
cd blog-service     && mvn spring-boot:run
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

## Blog API (`blog-service`)

Backs the frontend's **Blog** list page and **Single Post** page. Reads are public;
authoring requires a JWT whose `role` claim is `ADMIN`.

| Method | Path | Notes |
|--------|------|-------|
| `GET` | `/api/posts` | Paged list, newest first. Filters: `?category=`, `?tag=`, `?q=` (title search), `?page=`, `?size=` (default 6, max 50) |
| `GET` | `/api/posts/categories` | Category names + post counts, for the sidebar |
| `GET` | `/api/posts/{slug}` | One article **plus** `previous`/`next` links and 3 `related` posts — the whole single-post page in one request |
| `POST` | `/api/posts` | ADMIN. Byline comes from the JWT, never the body |
| `PUT` | `/api/posts/{id}` | ADMIN. Keeps the original author |
| `DELETE` | `/api/posts/{id}` | ADMIN |

The list endpoint returns a stable envelope rather than Spring's `Page` shape:

```json
{ "content": [ /* PostSummary[] */ ], "page": 0, "size": 6,
  "totalElements": 6, "totalPages": 1, "first": true, "last": true }
```

Drafts (`published: false`) are invisible to every public endpoint — an unpublished
slug returns 404, not an empty article. A duplicate slug returns 409.

Comments on a post reuse `comment-service` as-is: its `/api/comments/{slug}` is
keyed on a plain slug string, so pass the **post** slug and threaded comments work
with no change to that service.

```bash
# List page 2 of the Camera category
curl "http://localhost:8080/api/posts?category=Camera&page=1&size=3"

# Single post page
curl http://localhost:8080/api/posts/top-10-small-camera-in-the-world

# Publish a post (ADMIN JWT)
curl -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer <ADMIN_JWT>" \
  -H "Content-Type: application/json" \
  -d '{"slug":"my-post","title":"My Post","excerpt":"Teaser.","content":"Body.","category":"Camera","coverImage":"/images/blog-item1.jpg","tags":["Camera"]}'
```

## Notes

- `ddl-auto: update` auto-creates tables. For production use migrations (Flyway/Liquibase).
- The shared JWT secret has a dev default; set `JWT_SECRET` (same value for both
  services) in real environments.
- This backend is independent of the frontend's NextAuth setup. To use this
  instead, the frontend would read the `?token=` returned to `/account` and send
  it as `Authorization: Bearer <token>` on API calls.
# shop_backend
