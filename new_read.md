# 🚀 E-Commerce Product Ranking & Analytics Engine

## 📖 Project Overview
This project is an enterprise-grade backend ranking engine for an E-Commerce platform. It ingests product data, tracks user interactions (views, sales, reviews), and dynamically calculates "Trending" and "Top Ranked" scores using automated background jobs.

The system is built using **Java 17** and **Spring Boot 4.x**, adhering strictly to Object-Oriented Design (OOD), SOLID principles, and advanced Spring Framework paradigms.

---

## 🎯 Module 5: Spring Framework Progress & Implementation Details

This section documents the architectural upgrades implemented to satisfy the **Module 5: Spring** internship requirements. The application has been fully refactored to utilize Spring's Inversion of Control (IoC) container, externalized configurations, and automated integration testing.

### 🔹 5.1 Introduction to Spring Boot
**Requirement:** *The application must run as a standalone application on an embedded server (Tomcat).*
* **What was done:** The application was bootstrapped using the `spring-boot-starter-web` dependency.
* **Why it was done:** This leverages Spring Boot's Autoconfiguration to automatically provision and configure an embedded Apache Tomcat server (running on port `8080`). It removes the need for manual WAR file deployments, allowing the application to run natively via a standard `public static void main` method.

### 🔹 5.2 Inversion of Control (IoC) and Dependency Injection (DI)
**Requirement:** *Split business logic into beans, use interfaces, implement Constructor Injection on final fields, and resolve bean conflicts using `@Qualifier`.*
* **Interface-Based Programming (True IoC):** * **What:** Refactored concrete classes (e.g., `ProductService`) into Interfaces, moving the actual logic into `ProductServiceImpl`. Controllers now only inject the Interface.
    * **Why:** This decouples the Web Layer from the Business Layer. The Controller no longer cares *how* the service is implemented, making the code highly modular and easier to mock during unit testing.
* **Constructor Injection & Final Fields:**
    * **What:** Replaced all field-based `@Autowired` annotations with `private final` fields initialized via constructor injection.
    * **Why:** This is an industry best practice. It guarantees that a Bean cannot be instantiated without its required dependencies, preventing `NullPointerExceptions` at runtime and ensuring thread safety.
* **Handling Multiple Bean Implementations (`@Qualifier`):**
    * **What:** Created a `NotificationService` interface with two distinct implementations: `EmailNotificationService` and `SmsNotificationService`. Used the `@Qualifier("emailNotification")` annotation inside the `ProductServiceImpl` constructor.
    * **Why:** When Spring finds multiple beans of the same type, it throws a `NoUniqueBeanDefinitionException`. `@Qualifier` explicitly instructs the IoC container which specific bean to inject into the application context.

### 🔹 5.3 Spring Configuration & Profiles
**Requirement:** *Organize configurations, implement dev/local/prod profiles, utilize `@ConfigurationProperties`, and execute code on startup.*
* **Environment Profiles (`local`, `dev`, `prod`):**
    * **What:** Separated `application.properties` into environment-specific files.
        * `local`: Configured to use a lightweight, embedded **H2** database for fast, isolated testing.
        * `dev`: Configured to connect to a persistent **PostgreSQL** instance running in Docker.
        * `prod`: Configured to securely ingest database credentials via system Environment Variables (`${DB_URL}`).
    * **Why:** Ensures that testing, local development, and production deployments use appropriate infrastructure without requiring code changes.
* **Externalized Properties (`@ConfigurationProperties`):**
    * **What:** Created a `RankingProperties` config class mapped to the `ranking.*` prefix to store algorithmic weights (e.g., `ranking.default-weight`).
    * **Why:** This groups related properties into a strongly-typed Java object rather than scattering `@Value` strings across the codebase, making the configuration type-safe and easier to refactor.
* **Lifecycle Hooks (`CommandLineRunner`):**
    * **What:** Implemented a bean returning a `CommandLineRunner` in the main application class that prints all loaded service beans to the console.
    * **Why:** Proves interaction with the `ApplicationContext` and provides debugging visibility into the Autoconfiguration process during the exact moment the application finishes starting.

### 🔹 5.4 Spring Test & Integration Testing
**Requirement:** *Cover business logic with integration tests using an embedded H2 database, test dirty context recovery, and mock external dependencies.*
* **Automated Integration Suite (`@SpringBootTest`):**
    * **What:** Built `ProductRankingSystemApplicationTests` utilizing `@ActiveProfiles("local")` to force the application to boot against the H2 in-memory database.
    * **Why:** Verifies that all layers (Web, Service, Data) interact correctly with a real database schema without permanently modifying dev/prod data.
* **Database Rollbacks (`@Transactional`):**
    * **What:** Applied `@Transactional` at the test class level.
    * **Why:** Ensures that every test runs within a transaction that is immediately rolled back after completion. This guarantees test isolation (Test A's data insertion will never break Test B).
* **Mocking Dependencies (`@MockitoBean`):**
    * **What:** Replaced the real `NotificationService` in the application context with a mock using the Spring Boot 4.x `@MockitoBean` annotation. Used `Mockito.verify()` to assert the service was called.
    * **Why:** Integration tests should strictly test internal logic. Mocking prevents the test suite from spamming actual emails or making external API calls while still verifying the internal method triggers.
* **Context Caching & Recovery (`@DirtiesContext`):**
    * **What:** Applied `@DirtiesContext` to the `@Scheduled` ranking task test.
    * **Why:** If a specific test severely alters the static state or the Spring Context, this annotation forces the Spring Test framework to destroy and rebuild a fresh Application Context for the subsequent tests, ensuring environment purity.

---

## 🛠️ Technology Stack
* **Language:** Java 17+
* **Framework:** Spring Boot 4.0.3
* **Data Access:** Spring Data JPA, Hibernate, JDBC
* **Databases:** PostgreSQL (Dev/Prod), H2 Database (Local/Test)
* **Testing:** JUnit 5, Mockito, Spring Test Context Framework

---
*Developed as part of the Grid Dynamics Engineering Internship Program.*