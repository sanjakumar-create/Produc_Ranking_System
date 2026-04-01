
# 🚀 E-Commerce Product Ranking & Analytics Engine

## 📖 Project Overview
This project is a high-performance backend ranking engine for an E-Commerce platform. It is designed to ingest product data, track user interactions (views, sales, reviews), and dynamically calculate "Trending" and "Top Ranked" scores using automated background jobs.

The system is built with **Java 17 and Spring Boot 3**, utilizes a highly optimized **PostgreSQL** relational database running in **Docker**, and is designed with strict adherence to Object-Oriented Design (OOD), SOLID principles, and advanced database optimization techniques.

---

## 🛠️ Technology Stack
* **Language:** Java 17
* **Framework:** Spring Boot 3.x (Spring Web, Spring Data JPA, Spring Cache)
* **Database:** PostgreSQL (Relational Database Management System)
* **Infrastructure:** Docker & Docker Compose
* **Database Tools:** pgAdmin 4, DBeaver, raw JDBC
* **Connection Pooling:** HikariCP
* **Testing:** JUnit 5, Mockito

---

## 🏛️ Architecture & Database Design

### Domain Model (Entity Relationships)
The relational model was designed to handle complex e-commerce data with strict foreign-key constraints to guarantee data integrity:
1. **`Product`**: The core entity holding basic details (Name, Brand, Price).
2. **`ProductMetrics` (One-To-One)**: Separated from the `Product` table for high cohesion. It holds rapidly changing data (`view_count`, `sales_count`, `ranking_score`, `trending_score`). Shares the Primary Key with `Product` via `@MapsId`.
3. **`Review` (Many-To-One)**: Multiple reviews belong to a single product, tracking user comments and 1-5 star ratings. Fetched lazily (`FetchType.LAZY`) to optimize memory.
4. **`ProductOffer` (Many-To-One)**: Tracks dynamic marketing discounts applied to specific products.

---

## 🚀 Core Features Implemented

### 1. REST API & Data Transfer (Spring Web)
* **N-Tier Architecture:** Completely decoupled the codebase into `Controllers` (Web Layer), `Services` (Business Logic), and `Repositories` (Data Access).
* **Data Transfer Objects (DTOs):** Implemented `ProductRequest` and `ProductResponse` objects. This strictly encapsulates database entities (`@Entity`), preventing sensitive backend data from leaking to the frontend and protecting against Mass Assignment vulnerabilities.
* **Global Exception Handling:** Implemented `@RestControllerAdvice` to intercept application errors. Custom exceptions like `ResourceNotFoundException` automatically return clean `404 Not Found` JSON responses instead of exposing Java stack traces.

### 2. Business Logic & Automation (Spring Core)
* **Algorithmic Scoring:** Built a `RankingService` that applies mathematical formulas to calculate product scores based on recent sales, views, and review averages.
* **Automated Background Jobs:** Utilized Spring's `@Scheduled` annotation to run a background thread that recalculates historical "All-Time" ranking scores periodically without human intervention.
* **Caching (`@Cacheable`):** Applied in-memory caching to the high-traffic `/trending` endpoint. This serves data to users in milliseconds and protects the database from heavy read loads. Old caches are wiped dynamically using `@CacheEvict` when rankings update.

---

## 🧠 Deep Dive: Engineering Concepts Applied

### 1. Spring IoC & Dependency Injection
Instead of tightly coupling classes with the `new` keyword, the system leverages Spring's Inversion of Control (IoC) container. Beans are registered via stereotype annotations (`@RestController`, `@Service`, `@Repository`) and injected via constructors, making the system highly modular and testable.

### 2. Hibernate & ORM Optimization
* **The N+1 Query Problem:** Optimized data fetching by avoiding N+1 loops. Used `JOIN FETCH` in JPQL queries to grab Products and their associated Reviews in a single SQL query.
* **Transactional Safety:** Critical business logic that modifies multiple tables (like updating ranking scores) is wrapped in `@Transactional`. This ensures **Atomicity**—either all database updates succeed, or the entire transaction rolls back cleanly.

### 3. Advanced RDBMS & PostgreSQL Optimization
As part of the system's performance tuning, deep analysis was conducted on the PostgreSQL engine:
* **ACID Compliance:** Evaluated PostgreSQL's default `READ COMMITTED` isolation level and documented how to prevent "Non-Repeatable Reads" during concurrent high-volume traffic.
* **B-Tree Indexing & Query Plans:** Generated 1,000,000 dummy products to analyze database bottlenecks.
    * Used `EXPLAIN ANALYZE` to identify slow `Parallel Seq Scans` (Execution Time: ~49.2ms).
    * Implemented **Compound Indexes** (`CREATE INDEX idx_brand_price ON products(brand, price);`).
    * Successfully optimized the query planner to utilize a **`Bitmap Index Scan`**, dropping execution time to **~16.9ms** (a 65% performance increase).
* **Raw SQL Modules:** Created standalone `.sql` files (`1_querying_generic.sql`, `2_querying_domain.sql`) to demonstrate mastery of complex multi-table `JOIN`s, Subqueries, and aggregations (`GROUP BY`, `HAVING`).

### 4. Concurrency & JVM Awareness
* Designed the background `@Scheduled` tasks to run efficiently without exhausting the HikariCP database connection pool.
* Ensured local variables were used effectively to allow the JVM's G1 Garbage Collector to quickly clean up discarded DTOs in the Young Generation heap, preventing "Stop-The-World" pauses.

---

## 🧪 Testing Strategy
* **Unit Testing (JUnit 5 & Mockito):** The core mathematical algorithms in the `RankingService` are tested in strict isolation. Repositories are mocked using `@Mock` to ensure tests run in milliseconds without requiring a database connection.
* **Behavior Verification:** Used Mockito's `verify()` and `ArgumentCaptor` to assert that the service layers pass the correct, transformed data down to the database layer.

---

## 📂 Project Structure
```text
📁 src/main/java/com/ecommerce/ranking/
 ├── 📁 controller/        # REST API Endpoints (@RestController)
 ├── 📁 service/           # Business Logic & Scheduled Tasks (@Service)
 ├── 📁 repository/        # Spring Data JPA Interfaces & Custom Queries
 ├── 📁 model/             # Database Entities (@Entity)
 ├── 📁 dto/               # Data Transfer Objects (Request/Response)
 └── 📁 exception/         # Global Exception Handlers (@ControllerAdvice)
📁 module-4-tasks/         # Raw SQL & RDBMS Database Analysis
 ├── 📄 1_querying_generic.sql   # Advanced SQL generic practice
 ├── 📄 2_querying_domain.sql    # Domain-specific business SQL
 └── 📄 3_RDBMS_Analysis.md      # ACID, Indexing, and Query Plan Analysis
📄 docker-compose.yml      # Infrastructure setup
📄 pom.xml                 # Maven dependencies
```

---

## ⚙️ How to Run Locally

**1. Start the Database**
Ensure Docker Desktop is running. In the root directory, start the PostgreSQL container:
```bash
docker-compose up -d
```

**2. Configure the Environment**
Ensure your `src/main/resources/application.properties` (or `.yml`) is pointing to the correct local Docker port (`localhost:5432`) with the configured username and password.

**3. Start the Spring Boot Application**
Run the main application class `ProductRankingSystemApplication.java` via your IDE, or use Maven:
```bash
mvn spring-boot:run
```

**4. Access the APIs**
The server will start on port `8082`. You can test the endpoints using Postman or cURL:
* `GET http://localhost:8082/products`
* `GET http://localhost:8082/products/trending`


