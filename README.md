# 🚢 Shippex Backend

Backend service for the **Shippex** platform.

Built with **Spring Boot**, **MongoDB**, **Redis**, **Spring Security**, **JWT Authentication**, and **Docker**.

---

# 🛠️ Tech Stack

- Java 21
- Spring Boot 3
- Spring Security
- MongoDB
- Redis
- JWT
- Maven
- Docker

---

# 📋 Prerequisites

Choose one of the following approaches:

## Option 1 (Recommended)

- Docker Desktop

## Option 2

- Java 21
- Maven
- MongoDB
- Redis

---

# 📥 Clone Repository

```bash
git clone https://github.com/ShippexDevs/Shippex.git

cd shippex-backend
```

---

# ⚙️ Configure Environment Variables

Copy

```text
.env.example
```

to

```text
.env
```

Fill in all required environment variables.

---

# 🐳 Running with Docker (Recommended)

Docker Compose automatically loads environment variables from the `.env` file.

Simply execute:

```bash
docker compose up --build -d
```

Backend

```
http://localhost:8080
```

## 📚 API Documentation

With the backend running, open Swagger UI at:

```text
http://localhost:8080/swagger-ui.html
```

The OpenAPI JSON document is available at `http://localhost:8080/v3/api-docs`. For protected endpoints, use **Authorize** in Swagger UI and enter your JWT access token; the `Bearer` prefix is added automatically. Obtain a user token from `POST /api/public/login` or an admin token from `POST /api/admin/login`.

## Admin category imports

The category import endpoint requires the existing admin JWT authorization (`ADMIN` or `SUPER_ADMIN`) and accepts 1–100 entries. Each item is persisted independently; imports are **not atomic**. Responses include an item index and `created`, `skipped`, or `failed` status. HTTP status is `201` if all entries were created, `200` if all were skipped, `207` if results are mixed, and `400` when every item failed. Bean validation errors reject the request before processing.

```http
POST /api/admin/categories/bulk
Content-Type: application/json
```

```json
[{"name":"Fruits","skuPrefix":"FR","description":"Fresh fruit","imageUrl":"https://example.test/fruits.jpg","active":true}]
```

Normalized name, slug, and SKU-prefix collisions are reported as `skipped`; existing categories are never overwritten. Retrying category imports is safe.

Example response:

```json
{"success":true,"message":"Bulk category import processed; existing categories were skipped without changes.","data":{"items":[{"index":0,"status":"created","category":{"id":"resolved-by-the-server","name":"Fruits","slug":"fruits","skuPrefix":"FR"}}],"created":1,"skipped":0,"failed":0}}
```

Category changes in `PUT /api/admin/products/{id}` are selected with `categoryId`. The server updates the category ID and denormalized name/slug from that category and preserves the existing SKU. Legacy category name/slug fields in update requests are ignored. Category renames synchronize denormalized product names/slugs by `categoryId`.

## Repair legacy product category IDs

The repair runner is disabled by default. First run in dry-run mode against the intended database (`apply=false` by default):

```bash
java -jar target/Shippex-0.0.1-SNAPSHOT.jar \
  --shippex.migration.category-links.enabled=true \
  --shippex.migration.category-links.aliases='Electronic=Electronics,Frozen Food=Frozen Foods'
```

Aliases are optional comma-separated `legacy value=existing category name or slug` pairs. Matching otherwise uses normalized category names and slugs; ambiguity is reported without a write. Review the logged `MATCH`, `UNMATCHED`, `AMBIGUOUS`, and final count lines. To apply reviewed mappings, rerun explicitly with `--shippex.migration.category-links.apply=true`. The runner resolves every ID from the live category collection and updates **only** `categoryId` on matched legacy products. It never creates categories and can be safely rerun; previously valid relationships are counted and skipped. Back up the database and point `MONGODB_URI` at the intended environment before an apply run.

Redis

```
localhost:6379
```

---

# 💻 Running Without Docker

## Step 1

Load the environment variables into the current PowerShell session.

```powershell
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*#' -or $_ -match '^\s*$') {
        return
    }

    $name, $value = $_ -split '=', 2

    [System.Environment]::SetEnvironmentVariable(
        $name,
        $value,
        "Process"
    )
}
```

---

## Step 2

Verify that the variables have been loaded.

```powershell
echo $env:MONGODB_URI
```

If the MongoDB connection string is displayed, proceed.

---

## Step 3

Run the application.

```bash
mvn spring-boot:run
```

or

```bash
mvn clean package
```

---

# 🐳 Docker Commands

| Task | Command |
|------|---------|
| First-time setup | `docker compose up --build -d` |
| Start application | `docker compose up -d` |
| Stop application | `docker compose down` |
| Restart application | `docker compose restart` |
| View Spring Boot logs | `docker compose logs -f app` |
| View Redis logs | `docker compose logs -f redis` |
| Rebuild after code changes | `docker compose up --build -d` |

---

# ❤️ Health Check

Verify that the backend is running.

```http
GET http://localhost:8080/actuator/health
```

Expected response

```json
{
    "status": "UP"
}
```

---

# ✨ Implemented Features

- User Registration
- Username Availability Check
- WhatsApp OTP Verification
- BCrypt Password Hashing
- JWT Authentication
- Spring Security
- Current User API
- Redis Integration
- Docker Support
- Global Exception Handling
- Logging
- Unit Tests
- Order placement, cancellation, and order-status management

---

---

# 🌳 Project Structure

Explore the repository's folder structure and architecture visually using GitDiagram.

🔗 **[View Shippex Repository Structure](https://gitdiagram.com/shippexdevs/shippex)**

GitDiagram provides a visual representation of the repository, making it easier to understand the project organization, modules, and relationships between components.

---


# 📌 Upcoming Features

- Address Management
- Cart APIs
- Admin Module
- Payment Integration
- Notifications

---

# 👨‍💻 Developer

**Zunaid & Santojeet**
