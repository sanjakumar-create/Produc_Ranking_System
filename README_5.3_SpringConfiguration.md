# Module 5.3: Spring Configuration & Application Environments

## 🎯 Module Goal
To externalize application settings so the code behaves appropriately across different deployment environments without requiring code changes.

## 📖 Glossary of Key Terms Explored
* **Spring Profiles:** A core feature that allows us to segregate parts of our application configuration and make them available only in certain environments (e.g., `local`, `dev`, `prod`).
* **H2 Database:** An open-source, lightweight, in-memory relational database. It runs in the computer's RAM and is completely erased when the application shuts down.
* **Environment Variables:** Values set on the host operating system rather than hardcoded in the application. They are used for injecting sensitive data like passwords.
* **`@ConfigurationProperties`:** A Spring Boot feature that binds external properties (from `.properties` files) directly into strongly-typed Java Objects (POJOs).
* **`CommandLineRunner`:** A Spring Boot functional interface. Any bean implementing this will automatically run its `run()` method exactly once, immediately after the application startup is fully complete.

## 🚀 Progress & Implementation

### 1. Multi-Environment Profiles
I created three specific configuration files:
* **`application-local.properties`:** Uses the **H2 Database**. Perfect for rapid local development and testing without risking real data.
* **`application-dev.properties`:** Connects to the local **PostgreSQL Docker container** for real-world integration.
* **`application-prod.properties`:** Utilizes standard Environment Variables (`${DB_URL}`, `${DB_USERNAME}`) to ensure zero credentials are leaked into Git for production environments.



### 2. Type-Safe Mathematical Configurations
Instead of using `@Value("${ranking.default-weight}")` across multiple services, I created the `RankingProperties` class annotated with `@ConfigurationProperties(prefix = "ranking")`.
* **Why:** This maps all properties starting with `ranking.` into a single Java object. It provides type-safety (ensuring a weight is always a `double`), prevents spelling typos, and centralizes the configuration of the algorithm.

### 3. Application Lifecycle Tracing
I implemented a `CommandLineRunner` in the main application class.
* **Why:** This proves interaction with the ApplicationContext. On startup, it grabs the `ApplicationContext` and prints the names of all the custom beans (Services, Repositories) that the Component Scanner successfully initialized, verifying our IoC container is working perfectly.