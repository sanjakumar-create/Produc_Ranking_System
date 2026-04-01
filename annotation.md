# 🏷️ Spring Boot Annotations Master Guide (Project Specific)

This document details every Spring Boot, Spring Web, and Spring Data JPA annotation used in the Product Ranking System. It explains what they do and exactly how they were applied to solve business problems in this project.



---

## 1️⃣ Core Application Annotations (The Engine)
*Found in `ProductRankingSystemApplication.java`*

### `@SpringBootApplication`
* **What it does:** This is a 3-in-1 mega annotation. It includes `@Configuration` (allows registering extra beans), `@EnableAutoConfiguration` (tells Spring to auto-configure things like Tomcat and PostgreSQL based on your `pom.xml`), and `@ComponentScan` (tells Spring to look through your folders to find your Controllers and Services).
* **Where we used it:** On the main class to boot up the entire backend.

### `@EnableScheduling`
* **What it does:** Wakes up Spring's background task executor.
* **Where we used it:** On the main class. Without this, the `@Scheduled` ranking updates would never run.

### `@EnableCaching`
* **What it does:** Turns on Spring's ability to intercept method calls and store their results in temporary fast memory (RAM/Redis).
* **Where we used it:** On the main class to prepare the system to cache the heavy `/trending` endpoint.

---

## 2️⃣ Controller Annotations (The Web Layer)
*Found in `ProductController`, `OfferController`, `ReviewController`*

### `@RestController`
* **What it does:** Marks the class as a web endpoint handler AND automatically converts returned Java objects (like `ProductResponse`) into JSON using the Jackson library.
* **Where we used it:** At the top of every single controller.

### `@RequestMapping("/path")`
* **What it does:** Sets the base URL for the entire controller.
* **Where we used it:** `@RequestMapping("/products")` on the `ProductController` so we didn't have to repeat `/products` on every single method inside it.

### `@GetMapping` / `@PostMapping` / `@DeleteMapping`
* **What it does:** Maps specific HTTP verbs (GET for reading, POST for creating, DELETE for removing) to specific Java methods.
* **Where we used it:** `@PostMapping` to create a product, `@GetMapping("/{id}")` to fetch one.

### `@RequestBody`
* **What it does:** Takes the raw JSON sent by the client (e.g., Postman or a React frontend) and maps it to a Java class.
* **Where we used it:** In `createProduct(@RequestBody ProductRequest request)`. This safely transformed the incoming JSON into our secure DTO.

### `@PathVariable`
* **What it does:** Extracts a dynamic variable directly from the URL path.
* **Where we used it:** In `getProduct(@PathVariable Long id)`. If the user calls `/products/5`, this annotation pulls the `5` and passes it to the `id` variable.

---

## 3️⃣ Service Annotations (The Business Logic)
*Found in `ProductService`, `RankingService`, `ReviewService`*

### `@Service`
* **What it does:** Tells the Spring IoC Container: *"Create exactly one instance (Singleton) of this class and hold onto it, because a Controller is going to need it."*
* **Where we used it:** On all our service files to enable Dependency Injection (e.g., injecting `RankingService` into `ProductService`).

### `@Transactional`
* **What it does:** Opens a database transaction. If anything fails inside the method, it rolls back all changes. It also keeps the Hibernate connection open long enough to fetch Lazy data and batch updates together.
* **Where we used it:** 1. In `updateRankingScores()` to fix the N+1 query problem by batching 60 updates into a single transaction.
    2. In `getTopRankedProducts()` (`readOnly = true`) to prevent the `LazyInitializationException` when converting entities to DTOs.

### `@Scheduled(fixedRate = 3600000)`
* **What it does:** Executes a method automatically on a timer.
* **Where we used it:** In `RankingService` to recalculate the all-time ranking scores every 1 hour (3,600,000 milliseconds) without human intervention.

### `@Cacheable(value = "trendingProducts")`
* **What it does:** Checks if "trendingProducts" exists in memory. If yes, returns it instantly. If no, runs the database query, saves the result in memory, and then returns it.
* **Where we used it:** On `getTrendingProducts()` to protect the database from crashing during high traffic.

### `@CacheEvict(value = "trendingProducts", allEntries = true)`
* **What it does:** Deletes data from the cache.
* **Where we used it:** On the `updateRankingScores()` scheduled task so that when the rankings change, the old cache is wiped, forcing the system to show fresh data.

---

## 4️⃣ Entity Annotations (The Database Layer)
*Found in `Product`, `Review`, `ProductMetrics`, `ProductOffer`*

### `@Entity` & `@Table(name="table_name")`
* **What it does:** `@Entity` tells Hibernate that this Java class represents a database table. `@Table` allows you to explicitly name that table in PostgreSQL.
* **Where we used it:** On all our model classes (e.g., `@Table(name="product_metrics")`).

### `@Id` & `@GeneratedValue(strategy = GenerationType.IDENTITY)`
* **What it does:** `@Id` marks the primary key. `@GeneratedValue` tells Postgres to use its auto-incrementing sequence to generate IDs (1, 2, 3...).
* **Where we used it:** On `productId`, `reviewId`, etc.

### `@Column(name = "column_name")`
* **What it does:** Maps a Java variable to a specifically named database column.
* **Where we used it:** In `ProductMetrics` (e.g., mapping `salesCount` to `sales_count`).

### `@OneToOne` / `@ManyToOne`
* **What it does:** Defines SQL foreign-key relationships. `FetchType.LAZY` means "don't fetch this joined data until I explicitly ask for it" (saves memory).
* **Where we used it:** * `@ManyToOne` on `Review` (Many reviews belong to One product).
    * `@OneToOne` on `ProductMetrics` (One product has exactly One metrics row).

### `@JoinColumn(name="product_id")`
* **What it does:** Specifies the exact name of the Foreign Key column in the database table.
* **Where we used it:** On the relationship definitions to link tables together.

### `@MapsId`
* **What it does:** A specialized JPA annotation. It tells a `@OneToOne` child entity to share the exact same Primary Key as its parent.
* **Where we used it:** In `ProductMetrics`. It ensures that Product #5 will always have ProductMetrics #5, saving us from creating an unnecessary `metrics_id` column.

---

## 5️⃣ Repository Annotations (The Data Access Layer)
*Found in `ProductOfferRepository`*

### `@Query`
* **What it does:** Allows you to write raw JPQL (Java Persistence Query Language) or native SQL when Spring's automatic naming conventions aren't enough.
* **Where we used it:** In `ProductOfferRepository` to write a custom query that sorts offers by discount percentage (`SELECT o.product FROM ProductOffer o ORDER BY o.discountPercentage DESC`).

---

## 6️⃣ Exception Handling Annotations (The Safety Net)
*Found in `GlobalExceptionHandler`*

### `@RestControllerAdvice`
* **What it does:** A global interceptor. It listens to all controllers and catches any exceptions they throw before the user sees a 500 Server Error.
* **Where we used it:** On our global error handler class.

### `@ExceptionHandler(ExceptionClass.class)`
* **What it does:** Tells Spring exactly which method should run when a specific error is thrown.
* **Where we used it:** `@ExceptionHandler(ResourceNotFoundException.class)` intercepts our custom "Not Found" errors and returns a clean 404 JSON response.

### `@ResponseStatus(HttpStatus.NOT_FOUND)`
* **What it does:** Hardcodes an HTTP status code to a custom exception class.
* **Where we used it:** On our `ResourceNotFoundException` class to ensure it always returns a 404 code.
* # 🚀 The Ultimate Spring Boot Cheat Sheet

This document serves as a master reference for the core concepts, annotations, and internal workings of Spring Boot. It explains **what** things are, **why** they exist, and **when** to use them.

---

## 🧠 1. Core Spring Concepts

### Spring IoC Container & Dependency Injection (DI)
* **What it is:** IoC (Inversion of Control) means you hand over control of creating objects to Spring. The IoC Container is like a factory. Dependency Injection is how the factory delivers the objects you need.
* **Why we use it:** It eliminates the `new` keyword. Instead of tightly coupling classes (e.g., `ProductService service = new ProductService()`), Spring builds the service and injects it via the constructor. This makes code modular and easily testable.


### Bean Lifecycle (`@PostConstruct`, `@PreDestroy`)
* **What it is:** The lifecycle represents the birth, life, and death of a Spring Bean (an object managed by Spring).
* **When to use them:**
    * `@PostConstruct`: Runs exactly once *after* Spring creates the bean and injects dependencies. Use it to load initial cache data or validate configurations.
    * `@PreDestroy`: Runs right *before* the application shuts down. Use it to close file streams, clear caches, or gracefully close external connections.

### Bean Scopes (Singleton, Prototype, Request, Session)
* **What it is:** Determines how many instances of a bean Spring will create.
    * **Singleton (Default):** Spring creates exactly ONE instance for the whole application. (Used for Services, Repositories).
    * **Prototype:** Spring creates a BRAND NEW instance every time you ask for it. (Rarely used, but good for objects that hold user-specific state).
    * **Request:** One instance per HTTP request (Web applications only).
    * **Session:** One instance per user session (e.g., a shopping cart).

### Stereotype Annotations (`@Component`, `@Service`, `@Repository`, `@Controller`)
* **What they are:** Tags that tell the IoC container to create a bean.
    * `@Component`: The generic tag. Use this for utility classes or custom components.
    * `@Service`: Functionally identical to `@Component`, but signals to other developers that this class holds **business logic**.
    * `@Repository`: Functionally identical to `@Component`, but it automatically catches database-specific errors and translates them into Spring-friendly exceptions (`DataAccessException`).
    * `@Controller`: Specifically used to handle incoming web requests.

---

## 🕸️ 2. Web & MVC Architecture

### Spring MVC Request Lifecycle
* **What it is:** The exact path an HTTP request takes when it hits your server.
* **The Flow:** Request -> `DispatcherServlet` (The Front Desk) -> `HandlerMapping` (Finds the right Controller) -> `Controller` (Your code) -> `Service` (Business Logic) -> `MessageConverter` (Turns Java into JSON) -> Response to user.


### `@RestController` vs `@Controller`
* **`@Controller`:** Used in traditional web apps. It returns an HTML page (like Thymeleaf or JSP).
* **`@RestController`:** A convenience annotation that combines `@Controller` + `@ResponseBody`. It tells Spring: *"Skip the HTML views. Convert my Java objects directly to JSON and send them back."* (Used for APIs).

### `@RequestMapping`, `@GetMapping`, `@PostMapping`
* **What they are:** Routing instructions.
* **`@RequestMapping`:** Can sit at the class level to define a base URL (e.g., `/products`).
* **`@GetMapping` / `@PostMapping`:** Specific HTTP verbs. `GET` is for reading data safely. `POST` is for creating or submitting new data.

### Exception Handling (`@ControllerAdvice`, `@ExceptionHandler`)
* **What it is:** A global safety net for your API.
* **Why we use it:** Instead of putting `try/catch` blocks in every controller, `@ControllerAdvice` sits globally. When an error is thrown, it intercepts it, uses `@ExceptionHandler` to figure out what type of error it is, and returns a clean, structured JSON error response instead of an ugly Java stack trace.

---

## 🛡️ 3. Security & Advanced Concepts

### Spring Security (JWT, OAuth2, Filter Chain)
* **Filter Chain:** A wall of security checks that every request must pass through before hitting your Controller.
* **JWT (JSON Web Token):** A stateless way to secure APIs. The user logs in, gets a signed token, and sends that token in the header of future requests. The server verifies the signature without needing to check the database every time.
* **OAuth2:** A standard that allows users to log in using external providers (e.g., "Login with Google" or "Login with GitHub").

### Spring AOP (Aspect, Advice, Pointcut, JoinPoint)
* **What it is:** Aspect-Oriented Programming. It lets you write code once and apply it to many methods automatically (like logging every time a method is called, or checking security credentials).
* **The Terminology:**
    * **Aspect:** The feature you are adding (e.g., "LoggingAspect").
    * **Advice:** The actual code that will run (e.g., `System.out.println("Method called")`).
    * **Pointcut:** The rule for *where* it should run (e.g., "Run only on methods inside the Service package").
    * **JoinPoint:** The exact moment it runs (e.g., right before the method executes, or right after it throws an exception).


---

## 🗄️ 4. Data Layer & Configuration

### Spring Data JPA (Repositories, Query Methods, `@Query`)
* **What it is:** A layer over Hibernate that writes database code for you.
* **Query Methods:** You can literally just write a method signature like `findByBrandAndPriceLessThan(String brand, double price);` and Spring writes the exact SQL automatically.
* **`@Query`:** Used when you need complex queries that Spring's naming convention can't handle (e.g., `@Query("SELECT p FROM Product p ORDER BY p.trendingScore DESC")`).

### Profiles & Environment Config (`@Profile`, `application.yml`)
* **What it is:** A way to change your app's behavior depending on where it's running.
* **How it works:** You can have an `application-dev.yml` (connects to a local H2 database) and an `application-prod.yml` (connects to an AWS PostgreSQL database).
* **`@Profile("dev")`:** You can tag beans to only be created in specific environments (like a dummy email sender for testing).

### Spring Boot Auto Configuration
* **What it is:** The "magic" of Spring Boot.
* **How it works internally:** When Spring Boot starts, the `@EnableAutoConfiguration` annotation scans your `pom.xml` dependencies. If it sees `spring-boot-starter-web`, it automatically starts a Tomcat server. If it sees PostgreSQL drivers, it automatically builds a `DataSource` bean and connects to the database. It sets up sane defaults so you don't have to write XML files.


### Spring Boot Actuator
* **What it is:** Built-in production monitoring.
* **Why we use it:** By adding the actuator dependency, you get endpoints like `/actuator/health` (to check if the app and DB are alive) and `/actuator/metrics` (to track memory usage and API response times). Crucial for cloud deployments and Kubernetes.

---

## 🧪 5. Testing

### Spring Boot Testing Annotations
* **`@SpringBootTest`:** The heavyweight. Loads the ENTIRE application context (Controllers, Services, Repositories, DB). Used for Integration Testing to ensure everything works together.
* **`@WebMvcTest`:** The lightweight web tester. It ONLY loads the web layer (Controllers). It mocks out the Services. Used to quickly test routing, JSON parsing, and HTTP status codes.
* **`@DataJpaTest`:** The lightweight database tester. It ONLY loads the repository layer and an in-memory database. Used to test your custom `@Query` logic without starting a web server.

# 🗄️ Database, ORM & Architecture Master Guide

This section covers the core database concepts, Hibernate internals, and advanced data architecture. Understanding these transforms a developer from a "framework user" into a "software engineer."

---

## 1️⃣ ORM & Database Communication

### JDBC vs JPA vs Hibernate
* **JDBC (Java Database Connectivity):** The raw, low-level API used to connect Java to a database. It requires writing manual SQL strings and parsing result sets. Very fast, but high boilerplate.
* **JPA (Jakarta Persistence API):** This is just a *rulebook* (specification). It defines the rules for Object-Relational Mapping (ORM) in Java using annotations like `@Entity` and `@Id`. It contains no actual code to talk to a DB.
* **Hibernate:** The actual *engine* (implementation) that reads the JPA rules and writes the JDBC code for you. It translates your Java objects into PostgreSQL rows automatically.

### Connection Pooling (HikariCP Internals)
* **What it is:** Opening a new database connection takes a lot of time and CPU. A connection pool creates a "bucket" of active, open connections when the app starts.
* **How it works:** When a user requests data, Spring borrows a connection from the pool, runs the query, and instantly puts it back.
* **Why HikariCP?** It is the default in Spring Boot because it is currently the fastest, most lightweight connection pool available for Java.

---

## 2️⃣ Hibernate Internals & Performance

### The Entity Lifecycle

An object in Hibernate goes through 4 distinct phases:
1. **Transient:** You just typed `new Product()`. Hibernate has no idea this object exists. It is not in the DB.
2. **Persistent (Managed):** You called `repository.save()`. The object is now tracked by Hibernate's "Session". Any changes you make to it in Java will automatically sync to the DB.
3. **Detached:** The `@Transactional` method finished, and the DB connection closed. The object still exists in Java memory, but changes will no longer sync to the database.
4. **Removed:** You called `repository.delete()`. It is marked for deletion and will be removed from the DB upon commit.

### Lazy vs Eager Loading (The N+1 Problem)
* **Eager Loading:** When you fetch a Product, it immediately fetches all related Reviews, even if you didn't ask for them. *Bad for performance.*
* **Lazy Loading:** When you fetch a Product, it leaves a "placeholder" for Reviews. It only hits the DB to get the reviews if you explicitly type `product.getReviews()`. *Good for performance.*
* **The N+1 Problem:** If you fetch 100 Products (1 query), and then loop through them calling `.getReviews()` on each, Hibernate fires 100 extra queries (N+1 = 101 queries).
* **The Fix:** Use a `JOIN FETCH` in your `@Query` to grab the Products and their Reviews in exactly 1 single SQL query, or use Spring's `@EntityGraph`.

---

## 3️⃣ Transactions & ACID

### `@Transactional`
* **Propagation:** Defines what happens if a transaction is called inside another transaction.
  * `REQUIRED` (Default): Join the existing transaction. If none exists, make a new one.
  * `REQUIRES_NEW`: Pause the current transaction and start a brand new, independent one.
* **Isolation Levels:** Controls how "locking" works when multiple users try to read/write the same row at the exact same millisecond.
  * *Read Uncommitted* (Fastest, least safe) -> *Read Committed* -> *Repeatable Read* -> *Serializable* (Slowest, 100% safe).

### ACID Properties
Every relational database (like PostgreSQL) guarantees these 4 rules:
1. **Atomicity:** "All or Nothing." If 5 queries are in a transaction and query 4 fails, the whole transaction rolls back.
2. **Consistency:** Data must always pass your DB constraints (like Foreign Keys or NOT NULL).
3. **Isolation:** Two users buying the same item at the same time will not corrupt the data.
4. **Durability:** Once the DB says "Saved", it is saved permanently, even if the power goes out 1 second later.

---

## 4️⃣ SQL & Advanced Querying

### SQL Joins

* **INNER JOIN:** Returns only records that have matching values in both tables.
* **LEFT JOIN:** Returns ALL records from the left table, and the matched records from the right table (or NULL if no match).
* **RIGHT JOIN:** Returns ALL records from the right table, and the matched records from the left.
* **FULL JOIN:** Returns all records when there is a match in either the left or right table.
* **CROSS JOIN:** Returns the Cartesian product (every row combined with every row).
* **SELF JOIN:** A regular join, but the table is joined with itself (useful for Employee -> Manager hierarchies).

### Window Functions (`ROW_NUMBER`, `RANK`, `PARTITION BY`)
* **What they do:** They allow you to perform calculations across a set of table rows that are related to the current row, *without* collapsing the rows like `GROUP BY` does.
* **Example:** Find the top-priced product *in each category*. You would `PARTITION BY` category and assign a `RANK()` based on price.

### SQL Query Optimization
* **EXPLAIN:** If a query is slow, put `EXPLAIN ANALYZE` in front of it in Postgres. It will output the exact "Query Plan" showing if it did a slow "Sequential Scan" (checked every row) or a fast "Index Scan".
* **Slow Query Log:** A database setting that automatically logs any query that takes longer than X milliseconds, helping you identify bottlenecks.

---

## 5️⃣ Database Scaling & Indexes

### Indexing
Indexes are like the table of contents in a book. They make reading incredibly fast, but slow down writing (because the index must be updated on every INSERT/UPDATE).
* **B-Tree Index:** The default. Good for `<, >, =, BETWEEN`.
* **Composite Index:** An index on multiple columns (e.g., `brand` AND `price`). The order matters!
* **Covering Index:** An index that contains all the data needed for the query, meaning the DB doesn't even need to look at the actual table.

### Database Sharding & Partitioning
* **Partitioning:** Splitting one massive table into smaller physical pieces *on the same server* (e.g., partitioning `product_metrics` by month).
* **Sharding:** Splitting a database across *multiple different servers*. (e.g., Users A-M on Server 1, Users N-Z on Server 2). Highly complex, used by companies like Netflix.

### Read Replicas
* **The Concept:** You have one "Master" database that handles all `INSERT`, `UPDATE`, and `DELETE` commands. You spin up 3 "Read Replicas" (slaves) that constantly copy data from the Master. All `GET` requests are routed to the replicas, massively reducing the load on the Master.

---

## 6️⃣ NoSQL Databases

When do we abandon PostgreSQL and use NoSQL?

### Redis (Key-Value Store)
* **What it is:** An in-memory database. Data is stored in RAM, not on a hard drive.
* **When to use it:** Caching (`@Cacheable`). Storing temporary data like user session tokens, or the `/trending` endpoint results. It returns data in microseconds.

### MongoDB (Document Store)
* **What it is:** Stores data as JSON-like documents instead of rigid tables and rows.
* **When to use it:** When your data structure changes rapidly, or you have highly nested data (like a user profile with multiple variable arrays). Not great for highly transactional financial systems (stick to ACID SQL for that).

# 🏛️ System Design & Architecture Master Guide

This section transitions from writing code to designing scalable, distributed, and resilient systems. It covers how multiple applications communicate, handle failures, and scale to millions of users.

---

## 1️⃣ Architecture Styles

### Monolith vs Microservices


[Image of Monolith vs Microservices architecture diagram]

* **Monolith:** All code (Controllers, Services, Database connections) lives in one giant codebase and deploys as one single unit.
  * *Pros:* Easy to test, simple to deploy, fast to develop initially.
  * *Cons:* As the team grows, every small change requires redeploying the whole app. A bug in the Review feature can crash the Checkout feature.
* **Microservices:** Breaking the Monolith into small, independent mini-applications (e.g., a `Product Service`, a `Review Service`, and a `Ranking Service`) that talk to each other over the network.
  * *When to switch:* When your engineering team gets too big to work in one codebase, or when specific features (like Ranking) need to scale independently from the rest of the app.

### Event-Driven Architecture (Kafka, RabbitMQ)

* **What it is:** Instead of Service A calling Service B directly and waiting for a response (Synchronous), Service A "broadcasts" an event to a Message Broker, and Service B listens for it (Asynchronous).
* **Why it's amazing:** Decoupling. If the `Review Service` goes down, the `Product Service` can still broadcast "New Review Created" events to Kafka. Kafka holds onto them safely. When the `Review Service` wakes back up, it processes the backlog. No data is lost!

### CQRS & Event Sourcing
* **CQRS (Command Query Responsibility Segregation):** Splitting your app into two halves. One database handles all writes/updates (Commands), and a completely separate, heavily optimized database handles all reads (Queries).
* **Event Sourcing:** Instead of saving the *current state* of an object (e.g., Account Balance: $50), you save every *event* that ever happened (Deposited $100, Withdrew $50). The current state is calculated by replaying the events. (Used heavily in banking).

---

## 2️⃣ API Design & Communication

### REST API Design Best Practices
* **Use Nouns, Not Verbs:** `/products` (Good) vs `/getProducts` (Bad).
* **Use Plurals:** `/products/1` (Good) vs `/product/1` (Bad).
* **Use Proper HTTP Methods:** `GET` (Read), `POST` (Create), `PUT` (Replace), `PATCH` (Partial Update), `DELETE` (Remove).
* **Use Status Codes:** `200` (OK), `201` (Created), `400` (Bad Request), `401` (Unauthorized), `404` (Not Found), `500` (Server Error).

### API Versioning Strategies
APIs change. You can't break the app for mobile users who haven't updated their app yet.
* **URI Versioning:** `/api/v1/products` (Most common, easiest to see).
* **Header Versioning:** Sending `Accept-Version: v1` in the HTTP header (Cleaner URLs, harder to test).
* **Query Parameter:** `/api/products?version=1` (Used by Amazon).

### gRPC vs REST vs GraphQL
* **REST:** The industry standard. JSON over HTTP. Easy to build, easy to cache.
* **GraphQL:** Created by Facebook. The client sends a query asking for *exactly* what fields it wants, preventing over-fetching (getting too much data) and under-fetching (having to make 5 different API calls).
* **gRPC:** Created by Google. Uses ultra-fast, compressed binary data (Protobufs) instead of JSON. Used almost exclusively for internal microservices talking to each other at lightning speed.

---

## 3️⃣ Resilience & Scaling Patterns

### API Gateway Pattern

* **What it is:** The single "Front Door" for your entire microservice ecosystem.
* **Why we use it:** Instead of a mobile app memorizing 50 different IP addresses for 50 different microservices, it just talks to the Gateway. The Gateway handles Routing, Security (checking JWTs), and Rate Limiting before passing the request to the backend.

### Rate Limiting & Throttling
* **What it is:** Stopping users (or bots) from spamming your API and crashing the server.
* **How it works:** You set a rule (e.g., "Max 100 requests per minute per IP address"). If they exceed it, the server returns an HTTP `429 Too Many Requests` status code.

### Circuit Breaker Pattern (Resilience4j)
* **What it is:** Prevents cascading failures. If your `Product Service` calls the `Recommendation Service`, and the `Recommendation Service` is offline, the Product Service will hang and eventually crash too.
* **The Fix:** A Circuit Breaker detects the failure and "Opens" the circuit. It instantly returns a default fallback response (like an empty list) so the main app survives. Periodically, it lets one request through ("Half-Open") to check if the broken service is back online.

### Service Discovery (Eureka, Consul)
* **What it is:** In the cloud, servers die and restart constantly, changing their IP addresses. Service Discovery acts like a dynamic phonebook. When a service starts, it registers its current IP address with Eureka. When Service A needs to call Service B, it asks Eureka for the current IP.

---

## 4️⃣ Distributed Systems & Transactions

### The CAP Theorem

In any distributed system, you can only guarantee TWO of the following three properties:
1. **Consistency:** Every user sees the exact same data at the exact same time.
2. **Availability:** The system is always up and responds to requests.
3. **Partition Tolerance:** The system survives even if the network cable connecting two of your servers is cut.
* *Note:* Because networks fail (Partition is inevitable), you almost always have to choose between Consistency (Banking) and Availability (Social Media).

### Saga Pattern (Distributed Transactions)

* **The Problem:** In a monolith, buying an item is one ACID transaction. In microservices, buying an item hits the `OrderService`, `PaymentService`, and `InventoryService`. If Payment fails, how do you rollback the Order?
* **The Solution:** The Saga pattern. It relies on "Compensating Transactions." If Payment fails, the Payment Service broadcasts a "PaymentFailed" event. The Order Service listens for this and executes an "Undo Order" database query.
* **Choreography:** Services just broadcast events to a Message Broker and react to each other (Decentralized).
* **Orchestration:** A central "Saga Manager" service explicitly tells the other services what to do and when to rollback (Centralized).

---

## 5️⃣ Advanced Caching (Redis)

### Caching Strategies
* **Cache-Aside (Lazy Loading):** The application checks the cache. If it's a miss, it asks the Database, returns the data, and saves it in the cache for the next guy. (Most common).
* **Write-Through:** Every time you save to the database, you simultaneously save to the cache. Slower writes, but the cache is always 100% perfectly accurate.
* **Write-Back:** You save data *only* to the cache, returning instantly. A background job syncs the cache to the actual database every few minutes. Extremely fast, but risks data loss if the cache server crashes.

### Redis Superpowers
* **What is it?** An in-memory Key-Value store. It operates in milliseconds.
* **TTL (Time To Live):** You can tell Redis: "Keep this trending product list, but self-destruct it in exactly 60 seconds."
* **Pub/Sub (Publish/Subscribe):** Redis can act like a lightweight message broker, broadcasting messages to multiple listeners instantly (great for chat apps).
* **Distributed Lock:** If you run 5 instances of your Spring Boot app, how do you prevent them all from running the `@Scheduled` ranking update at the same time? You use a Redis Distributed Lock. The first instance grabs the lock, runs the job, and the other 4 instances skip it.

# 🧪 Testing & Quality Assurance Master Guide

This section covers how to prove that your application works perfectly without having to manually open Postman every time you make a change. Mastering these tools ensures your system is stable, bug-free, and ready for production.



---

## 1️⃣ Unit Testing (JUnit 5)

Unit testing means testing a single "Unit" of code (like one method in `ProductService`) in complete isolation. We don't load the database, we don't start the web server. It should run in milliseconds.

### Core Annotations
* **`@Test`:** Tells JUnit, *"Run this method as a test."*
* **`@BeforeEach`:** Runs before *every single test* in the class. Used to reset data or initialize variables so tests don't pollute each other.
* **`@AfterEach`:** Runs after every test. Used to clean up resources.
* **`@BeforeAll` / `@AfterAll`:** Runs exactly once for the entire test class (must be static methods).

---

## 2️⃣ Mockito (Faking the Dependencies)

If we want to test the `ProductService`, we have a problem: it needs the `ProductRepository` to talk to the database. But unit tests aren't supposed to talk to the database!
**Solution:** We use Mockito to create a "dummy" repository that behaves exactly how we want it to.

### Mockito Tools
* **`mock()` / `@Mock`:** Creates a 100% fake object. If you call a method on it, it does nothing and returns `null` unless you explicitly tell it what to do (e.g., `when(repo.findById(1L)).thenReturn(dummyProduct)`).
* **`spy()` / `@Spy`:** A "partial mock." It wraps a *real* object. It runs the real code, but you can override specific methods if you want to.
* **`verify()`:** Used to check if a method was actually called. Example: *"Did the Service actually call `repository.deleteById(5)`?"*
* **`ArgumentCaptor`:** The detective tool. If your service creates a `Product` and passes it to `repository.save()`, the ArgumentCaptor intercepts that exact `Product` object so you can assert that the name and price were calculated correctly before it hit the database.

---

## 3️⃣ `@Mock` vs `@MockBean` (The Interview Trap)

Interviewers love asking this. They both create fakes, but they work completely differently:

* **`@Mock` (Fast):** Pure Mockito. It doesn't know what Spring is. It just creates a fake Java object in memory. Your test runs in 0.01 seconds because it never starts the Spring application. **(Use this for pure Unit Tests).**
* **`@MockBean` (Slow):** A Spring Boot annotation. It actually starts up the Spring Application Context, finds the real bean (like `RankingService`), rips it out, and replaces it with a Mockito fake. **(Use this for Integration/Web layer tests where you need Spring to be running).**

---

## 4️⃣ Integration Testing & TestContainers

Unit tests prove your *logic* works. Integration tests prove your *wiring* works (e.g., Does my SQL query actually work in PostgreSQL?).

### The Old Way vs The Industry Standard
* **The Old Way (H2 Database):** Developers used to spin up a fake, lightweight in-memory database called H2 for testing. *Problem:* H2 isn't PostgreSQL. Sometimes a test passes in H2 but crashes in production because the SQL dialects are slightly different.
* **The Industry Standard (TestContainers):** This is a Java library that uses Docker. When you run your test, TestContainers literally spins up a real, temporary PostgreSQL database inside a Docker container, runs your tests against it, and then destroys the container when the test finishes. You get 100% confidence it will work in production.

---

## 5️⃣ TDD (Test-Driven Development)

TDD is a workflow, not a tool. Instead of writing code and then testing it, you write the test *first*.

### The Red-Green-Refactor Cycle:
1. **Red:** Write a test for a feature that doesn't exist yet. Run it. It fails (Red).
2. **Green:** Write the absolute bare minimum amount of sloppy code just to make the test pass (Green).
3. **Refactor:** Now that the test protects you, rewrite the code to make it clean, efficient, and professional. The test ensures you didn't break the functionality.

---

## 6️⃣ Code Coverage (JaCoCo)

* **What it is:** JaCoCo (Java Code Coverage) is a plugin that watches your tests run and generates a visual HTML report showing exactly which lines of code were tested and which were missed.
* **Branch Coverage:** It doesn't just check if a method was called; it checks if you tested both the `if` and the `else` conditions.
* **The Golden Rule:** 100% code coverage is a trap. It leads to writing useless tests just to hit a metric. A mature engineering team aims for **75% to 85%** coverage, focusing entirely on complex business logic (like our `RankingService` formulas) rather than testing simple getters and setters.


# 🕵️‍♂️ Production Debugging & Performance Master Guide

Writing code is only half the job. The other half is figuring out why the application is slow, crashing, or running out of memory when 10,000 users log in at the same time. This section covers the dark art of JVM performance tuning.

---

## 1️⃣ Profiling the JVM (VisualVM & JProfiler)

Before you can fix a performance issue, you need to see inside the running application.

* **What it is:** VisualVM (free) and JProfiler (paid, industry standard) are x-ray machines for Java. You attach them to a running Spring Boot application to watch it in real-time.
* **CPU Profiling:** Shows you exactly which Java method is eating up the processor. If your `calculateTrendingScore()` method is taking 80% of the CPU time, the profiler will highlight it in red.
* **Memory Profiling:** Shows you a live graph of your RAM usage. If you see the memory usage going up in a straight line and never coming down, you have a problem.

---

## 2️⃣ Memory Leaks & Heap Dump Analysis

Wait, doesn't Java have a Garbage Collector (GC)? Yes! But Java can still have memory leaks.


* **How Memory Leaks Happen in Java:** The Garbage Collector only destroys objects that are "unreachable" (meaning no other code is using them). If you accidentally add 10,000 `ProductMetrics` to a `static List` and forget to clear it, the GC says, *"Oh, they are still in a list, I can't delete them."* Eventually, your RAM fills up entirely, and the app crashes with a fatal `OutOfMemoryError` (OOM).
* **Heap Dump:** A Heap Dump is a massive file (often gigabytes) that contains a snapshot of *every single object in RAM* at the exact millisecond the app crashed.
* **The Fix:** You open the Heap Dump file using a tool like Eclipse MAT (Memory Analyzer Tool) or VisualVM. You look for the "Dominator Tree" (the objects taking up the most space) and trace them back to their "GC Root" to find the exact line of code holding onto them.

---

## 3️⃣ Thread Dumps & Deadlocks

If your application hasn't crashed, but it's completely frozen and no APIs are responding, you likely have a Thread problem.

* **What is a Thread Dump?** A text file showing exactly what every single worker (thread) in your application is doing at this exact second. Are they waiting for a database? Are they calculating math?
* **Deadlock Detection:** * *The Scenario:* Thread A locks the `Product` table and needs the `Review` table. Thread B locks the `Review` table and needs the `Product` table.
  * *The Result:* Both threads wait for each other... forever. The app freezes.
  * *The Fix:* When you generate a thread dump during a freeze, the JVM is actually smart enough to analyze it for you. It will literally print: `Found one Java-level deadlock:` and tell you exactly which two threads are stuck and on which lines of code.


---

## 4️⃣ Garbage Collection (GC) Logs Analysis

Java's automatic memory management comes with a cost: it has to pause your application to take out the trash.

* **Stop-The-World (STW) Pauses:** When the GC does a "Major Collection" to clean up old memory, it literally freezes your entire Spring Boot app for a fraction of a second.
* **The Problem:** If your app is creating too many useless objects too fast, the GC has to run constantly. This is called "GC Thrashing."
* **The Symptom:** Your API usually responds in 50ms. But randomly, 1 out of every 20 requests takes 3,000ms. Why? Because that unlucky user hit the API at the exact moment the GC paused the world to clean up RAM.
* **The Fix:** Turn on GC Logging (`-Xlog:gc*`). Read the logs to see if pauses are taking too long. If so, you either need to give the JVM more RAM (`-Xmx`), switch to a faster Garbage Collector (like ZGC or Shenandoah), or write more memory-efficient Java code.

---

## 5️⃣ Slow API Debugging (The Big 3)

If a specific endpoint (like `GET /trending`) is slow, it is almost always one of these three things:

1. **The N+1 Query Problem:** (Covered in ORM section). The API is slow because Hibernate is firing 500 separate SQL queries instead of 1 `JOIN FETCH`.
2. **Missing Database Indexes:** The API is slow because the database is doing a "Sequential Scan" (reading all 1,000,000 rows one by one to find the right product). Fix this by adding a B-Tree Index and using `EXPLAIN ANALYZE` in PostgreSQL.
3. **Connection Pool Exhaustion (HikariCP):** * *The Scenario:* Your database connection pool has a maximum of 10 connections. 10 users hit a slow API that takes 5 seconds to run. While they are running, user #11 hits the API.
  * *The Result:* User #11's thread has no database connections available. It waits for 30 seconds, gives up, and throws a `ConnectionTimeoutException`.
  * *The Fix:* Never blindly increase the pool size to 100 (that will overwhelm the database). Instead, fix the slow queries so connections are returned to the pool faster!


# 🧵 Multithreading & Concurrency Master Guide

This section covers how Java handles multiple tasks at the exact same time. Mastering concurrency is essential for building high-performance, non-blocking applications like a global e-commerce ranking engine.

---

## 1️⃣ The Basics of Threads

### Thread Lifecycle

Every thread in Java goes through specific states:
1. **New:** The thread is created (`new Thread()`) but hasn't started yet.
2. **Runnable:** You called `.start()`. It is either running or waiting for the CPU to give it a time slice.
3. **Blocked:** The thread is trying to enter a `synchronized` block/method but another thread already holds the lock.
4. **Waiting / Timed Waiting:** The thread is waiting for a specific signal (`wait()`) or a set amount of time (`sleep(1000)`).
5. **Terminated:** The `run()` method has finished. The thread is dead and cannot be restarted.

### Runnable vs Callable vs Thread
* **Thread:** A raw class you can extend, but it's bad practice to manage them manually.
* **Runnable:** An interface with a `run()` method. It cannot return a result and cannot throw checked exceptions. It just executes a task.
* **Callable:** An interface with a `call()` method. It **can** return a result (like a `Double` for a ranking score) and **can** throw exceptions.

---

## 2️⃣ Thread Management

### ExecutorService & ThreadPools

* **The Problem:** Creating a raw `new Thread()` asks the Operating System for memory. Doing this for every API request will crash your server (OutOfMemoryError).
* **The Solution:** A Thread Pool creates a fixed batch of threads (e.g., 10) when the app starts. When a task comes in, an idle thread takes it, runs it, and goes back to the pool to wait for the next one.
* **Types of Pools:**
  * **Fixed:** `newFixedThreadPool(10)` — Exactly 10 threads. Best for normal, predictable workloads.
  * **Cached:** `newCachedThreadPool()` — Creates new threads as needed, but destroys them if idle for 60 seconds. Best for short-lived bursts.
  * **Scheduled:** Used for delayed or repeating tasks (This is what Spring's `@Scheduled` uses under the hood!).
  * **Work-Stealing:** Used by Fork/Join. Idle threads actively "steal" tasks from the queues of busy threads to maximize CPU usage.

---

## 3️⃣ Asynchronous Programming

### Future & CompletableFuture
* **Future:** A placeholder for a result that will arrive later. *Problem:* You have to call `.get()`, which blocks your current thread until the result is ready.
* **CompletableFuture:** A modern, non-blocking pipeline.
  * `thenApply()`: When the task finishes, instantly pass the result to this next function.
  * `thenCompose()`: Chain two async operations together.
  * `allOf()`: Wait for 5 different microservices to all return their data before continuing.
  * `anyOf()`: Ping 3 different servers and use the data from whichever one responds fastest.

---

## 4️⃣ Synchronization & Thread Safety

### Race Conditions & How to Avoid Them
* **What it is:** When two threads read and write to the same variable at the exact same millisecond. (e.g., Thread A and Thread B both read `viewCount = 10`. Both add 1, and both write `11`. You just lost a view!)
* **How to avoid:** Use synchronization, Locks, or Atomic variables.

### `synchronized` (Method vs Block)
* **What it does:** Uses a "Monitor Lock." Only one thread can execute this code at a time.
* **Method:** `public synchronized void addView()` locks the *entire object*. Safe, but slow.
* **Block:** `synchronized(this) { ... }` locks only a specific few lines of code. Much faster for performance.

### `volatile` Keyword
* **What it does:** CPU cores cache variables for speed. If Thread A changes a variable, Thread B might still see the old cached version. `volatile` forces all threads to read/write that specific variable directly from Main RAM, guaranteeing **visibility**. (Note: It does *not* prevent race conditions on its own).

### ReentrantLock vs `synchronized`
* `synchronized` is automatic but rigid.
* `ReentrantLock` is manual (`lock.lock()` and `lock.unlock()`). It gives you advanced powers like `tryLock(5, TimeUnit.SECONDS)` — "Try to get the lock, but if another thread is holding it for more than 5 seconds, give up and return an error instead of freezing."

### Deadlock, Livelock, & Starvation
* **Deadlock:** Thread A has Lock 1 and wants Lock 2. Thread B has Lock 2 and wants Lock 1. Both wait forever. (Prevention: Always acquire locks in the exact same order).
* **Livelock:** Two threads keep yielding to each other to be polite, constantly changing state but making zero actual progress (like two people dodging each other in a hallway).
* **Starvation:** A low-priority thread never gets CPU time because high-priority threads keep jumping the queue.

---

## 5️⃣ Advanced Concurrency Utilities

### Signaling & Barriers
* **Semaphore:** A bouncer at a club. You give it 5 "permits." Only 5 threads can access the resource at once. Great for rate-limiting.
* **CountDownLatch:** A starting gun. You set it to 3. Three threads do their setup work and call `countDown()`. A main thread waits. Once it hits 0, the main thread fires. *Cannot be reset.*
* **CyclicBarrier:** Like a hiking group. 5 threads do partial work, then wait at the barrier. Once all 5 arrive, they all proceed to the next phase together. *Can be reused.*
* **Phaser:** A more advanced, dynamic version of a CyclicBarrier where the number of waiting threads can change on the fly.

### ThreadLocal
* **What it is:** A variable that looks global, but every thread gets its own isolated, private copy.
* **Use case:** Storing the currently logged-in user's ID or Database Transaction Context without passing it through 50 method parameters.

### Atomics & CAS Operations
* **Classes:** `AtomicInteger`, `AtomicReference`.
* **CAS (Compare-And-Swap):** A hardware-level CPU instruction. Instead of locking a thread (which is slow), it says: *"I want to update `viewCount` from 10 to 11. If the value in RAM is still 10, change it to 11. If another thread snuck in and changed it to 11 already, loop around and try again."* This is called **Lock-Free Thread Safety**.

### Fork/Join Framework
* **What it is:** Divide and Conquer. It recursively breaks a massive task (like processing 10 million products) into tiny chunks, hands them to a `Work-Stealing` thread pool, and then joins the results back together. (This is what `List.parallelStream()` uses under the hood!).

---

## 6️⃣ The Future: Virtual Threads (Java 21 / Project Loom)

* **The Old Problem (Platform Threads):** Java threads map 1-to-1 with Operating System threads. OS threads are heavy (1MB RAM each). You can only have a few thousand before your server crashes.
* **The New Solution (Virtual Threads):** Introduced in Java 21. The JVM manages these, not the OS. They are virtually weightless. When a Virtual Thread waits for a database query, it instantly unmounts from the OS thread, letting another Virtual Thread use the CPU.
* **The Impact:** You can now spin up **millions** of threads concurrently using standard, easy-to-read synchronous code, completely eliminating the need for complex reactive frameworks like WebFlux!


# 🏗️ OOP & System Design Principles Master Guide

Writing code that the computer understands is easy. Writing code that *other humans* can understand, maintain, and safely modify is the mark of a Senior Engineer. This section covers the core rules of software design.

---

## 1️⃣ The 4 Pillars of OOP


1. **Encapsulation (Data Hiding):** * *What it is:* Keeping fields private and strictly controlling access via Getters/Setters.
  * *Project Example:* Our `ProductRequest` DTO. Users cannot arbitrarily change a product's internal Database ID because we simply didn't provide a `setId()` method in the incoming DTO.
2. **Abstraction (Implementation Hiding):**
  * *What it is:* Hiding complex backend details and only showing the essential features to the user.
  * *Project Example:* The `getTopRankedProducts()` method in the Controller. The Controller has no idea about the logarithmic math happening in the Service; it just calls the method and gets the result.
3. **Inheritance (Code Reusability):**
  * *What it is:* A child class inheriting fields and methods from a parent class ("Is-a" relationship).
  * *Project Example:* Our `ResourceNotFoundException` extends Java's `RuntimeException`. It gets all the power of a standard Java error, plus our custom HTTP 404 behavior.
4. **Polymorphism (Many Forms):**
  * *What it is:* One interface or method behaving differently depending on the object executing it.
  * *Project Example:* Spring's `JpaRepository`. We call `.findAll()`, and Spring dynamically figures out whether to write a PostgreSQL query, a MySQL query, or an H2 query at runtime based on our configuration.

---

## 2️⃣ The SOLID Principles


SOLID is the gold standard for writing maintainable code.

* **S - Single Responsibility Principle (SRP):** A class should have only one reason to change.
  * *Example:* Our `ProductController` strictly handles HTTP web traffic. Our `ProductService` strictly handles business logic. If we need to change how rankings are calculated, we never touch the Controller.
* **O - Open/Closed Principle (OCP):** Code should be open for extension, but closed for modification.
  * *Example:* If we want to add a new ranking algorithm (like `HolidayRanking`), we shouldn't rewrite our existing `RankingService`. We should create a `RankingStrategy` interface and create a new class that implements it.
* **L - Liskov Substitution Principle (LSP):** A child class must be able to replace its parent class without breaking the application.
  * *Example:* If `PremiumProduct` extends `Product`, passing a `PremiumProduct` into the `save(Product p)` database method must work flawlessly.
* **I - Interface Segregation Principle (ISP):** Don't force a class to implement an interface with methods it doesn't need. Keep interfaces small and specific.
* **D - Dependency Inversion Principle (DIP):** High-level modules should not depend on low-level modules. Both should depend on abstractions (Interfaces).
  * *Example:* Our Services depend on the `ProductRepository` interface, NOT a concrete `PostgreSQLRepository` class. This allows us to swap databases easily.

---

## 3️⃣ Advanced Class Design

### Composition over Inheritance
* **Inheritance ("Is-a"):** A `Car` *is a* `Vehicle`. (Can lead to deeply nested, fragile code).
* **Composition ("Has-a"):** A `Car` *has an* `Engine`. (Much more flexible).
* *Project Example:* We used Composition! A `Product` *has a* `ProductMetrics` object (`@OneToOne`). If we had used Inheritance (`ProductMetrics extends Product`), it would have created a nightmare of massive, bloated database tables.

### Abstract Class vs Interface
* **Interface:** A pure contract. It defines *what* a class can do (e.g., `Runnable`, `Serializable`), but provides no actual data state. A class can implement multiple interfaces.
* **Abstract Class:** A base template. It defines *what a class is*. It can hold state (instance variables) and shared code, but cannot be instantiated directly. A class can only extend ONE abstract class.

### Cohesion & Coupling
* **High Cohesion (Good):** Code that belongs together stays together. `ReviewService` handles everything related to Reviews.
* **Loose Coupling (Good):** Classes don't heavily rely on the concrete details of other classes. Because we use Spring's Dependency Injection, our Controller is loosely coupled to our Service.

---

## 4️⃣ Core Coding Maxims (The 3 Rules)

* **DRY (Don't Repeat Yourself):** If you copy-paste code 3 times, extract it into a helper method.
* **KISS (Keep It Simple, Stupid):** Don't write a 50-line custom sorting algorithm if `metricsRepository.findTop10ByOrderByRankingScoreDesc()` does the same thing in one line.
* **YAGNI (You Aren't Gonna Need It):** Don't build complex features (like an AI recommendation engine) today just because you *might* need it in 3 years. Build only what is required now.

---

## 5️⃣ Java Mechanics: Methods & Variables

### Method Overloading vs Overriding
* **Overloading (Compile-Time Polymorphism):** Same method name, different parameters in the same class. (e.g., `calculateScore(int sales)` vs `calculateScore(int sales, int views)`). The compiler decides which to run.
* **Overriding (Runtime Polymorphism):** Same method name and parameters, but the Child class replaces the Parent class's logic. Added via `@Override`. The JVM decides which to run dynamically at runtime.

### Covariant Return Types
* When you `@Override` a parent method, Java allows the child method to return a *more specific* subclass than the parent did. (e.g., Parent returns `Product`, Child overrides and returns `PremiumProduct`).

### Static vs Instance (Methods & Variables)
* **Instance:** Belongs to the specific object. (e.g., `product.getName()` — every product has a different name).
* **Static:** Belongs to the Class itself. Shared across all instances. (e.g., `Math.log()` or `public static final int MAX_DISCOUNT = 50;`). Never use `static` for user data!



# ☕ Java Core & JVM Master Guide

This section dives into the very foundation of the Java programming language. Understanding memory management, the JVM, modern Java features, and how Collections work under the hood separates standard developers from elite engineers.

---

## 1️⃣ The Java Ecosystem & JVM Architecture

### JDK vs JRE vs JVM
* **JVM (Java Virtual Machine):** The engine that runs the code. It translates Java byte code (`.class` files) into machine code that your specific OS (Windows/Mac/Linux) can understand.
* **JRE (Java Runtime Environment):** JVM + Core Java Libraries (like `java.util`). It is everything you need to *run* a Java program, but not build one.
* **JDK (Java Development Kit):** JRE + Development Tools (like the compiler `javac`, debuggers, and profilers). You need this to *write* code.

### JVM Architecture

1. **ClassLoader Subsystem:** Loads, links, and initializes `.class` files into memory when the program starts.
2. **Runtime Data Areas (Memory):**
  * **Heap:** Where all Objects (like `new Product()`) are stored. Shared across all threads.
  * **Stack:** Where method calls and local primitive variables (like `int x = 5`) live. Every thread gets its own isolated Stack.
  * **Metaspace:** Where class metadata, static variables, and method definitions are stored (Replaced the old "PermGen" in Java 8).
3. **Execution Engine:** Contains the Interpreter (runs code line-by-line) and the JIT (Just-In-Time) Compiler, which finds "hot spots" in your code and compiles them down to blazing-fast native machine code.

---

## 2️⃣ Memory Management & Garbage Collection

### The Java Memory Model (Stack vs Heap)
* **Stack:** Extremely fast, LIFO (Last-In-First-Out). When a method finishes, its stack frame is instantly popped off and destroyed. Thread-safe by default.
* **Heap:** Massive, slower, and shared. Objects live here until they are no longer referenced by anything in the Stack.

### Garbage Collection (GC) Strategies
* **Serial GC:** Single-threaded. Pauses the whole app. Good for tiny apps with less than 100MB of data.
* **Parallel GC:** Uses multiple threads to clean up memory. Maximizes overall throughput but can have noticeable "Stop-The-World" pauses.
* **G1 (Garbage First) GC:** The default in modern Java. It breaks the Heap into tiny regions and cleans the regions with the most garbage first. Balances high throughput with low pause times.
* **ZGC (Z Garbage Collector):** The cutting-edge collector. Designed for massive heaps (Terabytes of RAM) with sub-millisecond pause times. Use this for high-frequency trading or ultra-low-latency APIs.

---

## 3️⃣ Java Fundamentals & "Gotchas"

### String Pool & Interning
* **What it is:** Strings are immutable. To save memory, Java maintains a "String Pool" in the Heap.
* **The Trap:** `String a = "hello";` goes to the pool. `String b = "hello";` points to the exact same object in the pool. BUT `String c = new String("hello");` forces Java to create a brand new, separate object in the regular Heap.
* **`.intern()`:** Calling this on a String forces Java to move it into the String Pool to save memory.

### Autoboxing, Unboxing & The Integer Cache Trap
* **Autoboxing:** Java automatically converting a primitive `int` to an object `Integer`.
* **The Cache Trap:** Java caches `Integer` objects from `-128 to 127` to save memory.
  * `Integer x = 100; Integer y = 100;` ➡️ `x == y` is **TRUE** (same cached object).
  * `Integer a = 200; Integer b = 200;` ➡️ `a == b` is **FALSE** (outside cache, two different objects in memory). **Always use `.equals()` for objects!**

### Immutability & `final` Keyword
* **`final` Variable:** Cannot be reassigned once initialized.
* **`final` Method:** Cannot be overridden by a child class.
* **`final` Class:** Cannot be extended (e.g., the `String` class is final).
* **Immutability:** An object whose state cannot change after it is constructed. Makes objects 100% thread-safe because no thread can alter their data.

---

## 4️⃣ Collections Framework & Internals

### The Core Interfaces
* **List:** Ordered, allows duplicates (`ArrayList`, `LinkedList`).
* **Set:** Unordered, NO duplicates (`HashSet`, `TreeSet`).
* **Map:** Key-Value pairs. Keys must be unique (`HashMap`, `TreeMap`).
* **Queue:** FIFO (First-In-First-Out) processing (`PriorityQueue`).

### HashMap Internals (The Interview Favorite)

* **How it works:** An array of "Buckets" (Nodes). When you call `put(key, value)`, Java runs a hashing algorithm on the key to find the exact array index where it belongs.
* **Collision:** What if two different keys hash to the same index? Java puts them in a LinkedList at that index.
* **Treeification (Java 8+):** If a LinkedList collision grows to 8 or more items, it magically transforms into a Red-Black Tree to keep search speeds at `O(log n)` instead of degrading to `O(n)`.
* **Load Factor & Resizing:** By default, the load factor is `0.75`. If an array of size 16 gets 12 items in it (75% full), Java automatically creates a new array double the size (32) and redistributes everything to prevent collisions.

### `ConcurrentHashMap` vs `HashMap` vs `Hashtable`
* **`HashMap`:** Fast, but NOT thread-safe. Will corrupt if two threads write at the same time.
* **`Hashtable`:** Thread-safe, but terrible performance. It locks the *entire* map for every read/write.
* **`ConcurrentHashMap`:** The modern solution. Uses "Lock Striping" and CAS operations. It only locks the specific *bucket* being updated, allowing 15 other threads to write to other buckets at the exact same time.

### Comparable vs Comparator
* **Comparable (`compareTo`):** Defines the *natural, default* sorting order of a class (e.g., putting `implements Comparable` on `Product` to always sort by ID). Modifies the original class.
* **Comparator (`compare`):** An external sorting rule. Used when you want multiple ways to sort (e.g., `PriceComparator` or `RatingComparator`) without touching the original class code.

---

## 5️⃣ Generics

* **Why we use them:** To provide compile-time type safety. (e.g., `List<Product>` ensures you can't accidentally add a `Review` object to the list).
* **Type Erasure:** Generics are a "compiler trick." To maintain backward compatibility with ancient Java versions, the compiler completely erases the `<Product>` tags at runtime, turning them back into raw `Object` types.
* **Wildcards & Bounded Types:** * `<? extends Product>`: Accepts `Product` or any child class (Upper Bound).
  * `<? super PremiumProduct>`: Accepts `PremiumProduct` or any parent class (Lower Bound).

---

## 6️⃣ Modern Java Features (Java 10 to 21+)

Java has evolved rapidly to become less verbose and more powerful.

* **`var` (Local Type Inference - Java 10+):** * Instead of `List<ProductMetrics> list = new ArrayList<>();`
  * You can write: `var list = new ArrayList<ProductMetrics>();` (The compiler figures out the type automatically. Only works for local variables, not class fields).
* **Records (Java 14+):** * Replaces boilerplate DTOs. A `record` automatically generates private final fields, getters, `equals()`, `hashCode()`, and `toString()`.
  * Example: `public record ProductRequest(String name, double price) {}`
* **Sealed Classes (Java 17+):** * Allows a class to strictly dictate which specific classes are allowed to extend it.
  * Example: `public sealed class Payment permits CreditCard, PayPal {}`
* **Pattern Matching for `instanceof` (Java 16+):**
  * Old way: `if (obj instanceof Product) { Product p = (Product) obj; p.getName(); }`
  * New way: `if (obj instanceof Product p) { p.getName(); }` (Automatically casts and assigns the variable in one step!).