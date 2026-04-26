# Module 5.1: Introduction to Spring Boot & Architecture

## 🎯 Module Goal
To understand the core philosophy of Spring Boot, how it differs from the traditional Spring Framework, and to successfully run the E-Commerce Ranking System as a standalone service.

## 📖 Glossary of Key Terms Explored
* **Spring Framework:** A massive, modular Java framework for building enterprise applications. Historically, it required heavy XML configuration to wire components together.
* **Spring Boot:** An extension of the Spring Framework. It provides an "opinionated" approach, meaning it makes smart assumptions about what you need (like a web server and database connections) and pre-configures them automatically to save setup time.
* **Autoconfiguration:** Spring Boot's "magic" mechanism. It scans the `pom.xml` dependencies (the classpath) and automatically creates Spring Beans. For example, because it saw `spring-boot-starter-web`, it automatically configured a web server for us.
* **Embedded Server (Tomcat):** In the past, Java apps had to be compiled into a `.war` file and manually uploaded into an external server program. Spring Boot *embeds* the Apache Tomcat server directly inside the Java application.
* **Standalone Application:** Because the server is embedded, our application runs entirely on its own via a standard `public static void main(String[] args)` method.

## 🚀 Implementation & Progress
**What was done:**
The E-Commerce ranking application was bootstrapped using Spring Boot 4.x. By including the `spring-boot-starter-web` dependency, the application automatically launches an embedded Tomcat web server on port `8080` (or `8087` depending on the active profile).



**Why this matters for the project:**
By using Spring Boot, we eliminated hundreds of lines of boilerplate configuration. Our application is now a modern, deployable microservice that can be containerized (e.g., via Docker) and run anywhere simply by executing the `.jar` file.