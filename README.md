# Deltaclause Enterprise Spring Boot Backend

A high-fidelity, industry-grade Spring Boot microservice backend for the **Deltaclause** training and certification platform. Designed using clean architecture principles, utilizing **Hibernate JPA** for MySQL relational persistence, and **Spring Data Redis** for high-performance credential caching.

---

## 🛠️ Tech Stack & Key Architectures
- **Java 17** with **Spring Boot 3.2.x**
- **Spring Security 6** with Stateless **JSON Web Tokens (JWT)** for secure session filtering
- **Hibernate ORM / Spring Data JPA** mapping complex relational schemas safely
- **MySQL Driver** for permanent data persistence (courses, profiles, application contracts, logs)
- **Redis Starter Cache** mapping read-heavy endpoints (e.g., certificate lookups and course catalogues)
- **Lombok** for robust, boiler-free models and data objects mapping

---

## 📂 Codebase Directory Structural Mapping

The project components are organized under standard enterprise packages:
- `com.deltaclause.backend`
  - `config`: Handles **Security (Stateless Filters, CORS, Cryptography)**, **JWT Issuers**, and **Redis Cache Managers** defining precise TTL timeouts.
  - `entity`: Maps **Hibernate Database schemas** (`User`, `Internship`, `Enrollment`, `UserTaskSubmission`, `Certificate`, `EmailLog`).
  - `repository`: Exposes safe **Spring JPA transaction methods**.
  - `service`: Contains core business logic rules (**Referral discount logic, SFTP Outbox simulated mail logs, and crypto certificate builders**).
  - `controller`: Exposes secure REST endpoints compliant with Deltaclause's frontend requirements.
  - `dto`: Encapsulates validation payloads passed safely between controllers and filters.

---

## 🐳 Running with Docker (MySQL + Redis)

Spin up MySQL and Redis instantly inside light, sandboxed containers using the following `docker-compose.yml` boilerplate:

```yaml
version: '3.8'

services:
  mysql-db:
    image: mysql:8.0
    container_name: deltaclause-mysql
    restart: always
    environment:
      MYSQL_DATABASE: deltaclause_db
      MYSQL_ROOT_PASSWORD: yoursecurepassword
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql

  redis-cache:
    image: redis:7.0-alpine
    container_name: deltaclause-redis
    restart: always
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data

volumes:
  mysql-data:
  redis-data:
```

---

## ⚡ Quick Start & Execution Logs Guide

### 1. Prerequisites
- **JDK 17 or higher** installed internationally
- **Maven 3.6+** installed

### 2. Configure Database Connections
Verify configuration limits or secret keys mapped inside `/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/deltaclause_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=yoursecurepassword

spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### 3. Compile, Package, and Execute
Run standard maven life-cycles in your terminal at this project root:

```bash
# Clean previous compilations and build source files
mvn clean install

# Execute locally on Port 8080
mvn spring-boot:run
```

Upon starting successfully, Hibernate will automatically scan your entities and synchronize correct table structures inside your MySQL instance, and the Redis cache filters will listen on port `6379`!

---

## 🔒 Security & JWT Policies
All `/api/admin/*` and course modification endpoints block unauthorized requests and enforce administrative validation checks conventionally. 

To make a secure API call, provide the JWT in your authorization headers:
```http
Authorization: Bearer <your_jwt_access_token>
```

---

## ⚡ Redis Key Caching Topologies
1. **Verified Certificates Cache**
   - Active Endpoint: `/api/certificates/verify/{id}`
   - TTL: **7 Days**
   - Rationale: Student certificates are immutable and read-heavy. Redis bypasses MySQL indexing during scans entirely!
2. **Trainings Map Cache**
   - Active Endpoint: `/api/internships`
   - TTL: **2 Hours** (Automatically evicted via `@CacheEvict` when an admin adds or changes course configurations).
