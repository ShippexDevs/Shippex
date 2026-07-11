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

---

# 🚀 Raising a Pull Request

## Step 1

Create a new branch.

```bash
git checkout -b feature/<feature-name>
```

Example

```bash
git checkout -b feature/authentication
```

---

## Step 2

Verify the project builds successfully.

```bash
mvn clean package
```

---

## Step 3

Run the application.

```bash
docker compose up --build -d
```

Verify

- Registration
- Login
- Protected APIs
- Docker
- Redis
- Actuator Health

---

## Step 4

Stage all changes.

```bash
git add .
```

---

## Step 5

Commit.

```bash
git commit -m "feat(auth): implement JWT authentication"
```

---

## Step 6

Push the branch.

```bash
git push origin feature/authentication
```

---

## Step 7

Create a Pull Request on GitHub.

Include:

- Summary of changes
- Features implemented
- Testing performed
- Screenshots (if applicable)

Recommended screenshots:

- Registration API
- Login API
- `/api/appUser/me`
- MongoDB document showing hashed password
- `docker ps`
- `/actuator/health`

---

# 📌 Upcoming Features

- Address Management
- Cart APIs
- Order APIs
- Admin Module
- Payment Integration
- Notifications

---

# 👨‍💻 Developer

**Zunaid & Santojeet**