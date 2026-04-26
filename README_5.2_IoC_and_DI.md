# Module 5.2: Inversion of Control (IoC) and Dependency Injection (DI)

## 🎯 Module Goal
To decouple the application's layers (Web, Logic, Data) using Spring's IoC container, making the code modular, secure, and testable.

## 📖 Glossary of Key Terms Explored
* **Inversion of Control (IoC):** A software design principle famously known as the "Hollywood Principle" (*Don't call us, we'll call you*). Instead of my custom classes using the `new` keyword to create objects, the Spring Framework takes control and manages the creation and lifecycle of all objects.
* **Dependency Injection (DI):** The actual implementation of IoC. It is the process where Spring automatically "injects" (hands over) the required objects (dependencies) into a class that needs them.
* **ApplicationContext (The IoC Container):** The central brain of Spring. It is the environment where Spring creates, wires, and stores all the application's Beans.
* **Spring Beans:** Simple Java objects that are instantiated, assembled, and managed by the Spring IoC container (annotated with `@Service`, `@RestController`, `@Repository`).
* **Constructor Injection:** Passing dependencies through a class's constructor rather than setting them directly on a field.

## 🚀 Progress & Implementation

### 1. Interface-Based Programming (True IoC)
Instead of Controllers depending on concrete implementation classes (like `ProductServiceImpl`), I created interfaces (like `ProductService`).
* **Why:** This hides the complex business logic from the web layer. If we ever want to swap out the `ProductServiceImpl` for a new version, the Controller doesn't need to change. This ensures **Loose Coupling**.

### 2. Constructor Injection with Final Fields
I removed all field-level `@Autowired` annotations and replaced them with `private final` fields populated via the constructor.
* **Why:** 1. `final` variables *must* be initialized when the class is created. This guarantees that a Service cannot be created without its required Repository, preventing `NullPointerException` crashes.
    2. It makes Unit Testing easier because dependencies can be passed manually via constructors without needing the Spring Framework to start up.



### 3. Resolving Bean Conflicts with `@Qualifier`
I created a `NotificationService` interface with two implementations: `EmailNotificationService` and `SmsNotificationService`.
* **The Problem:** When `ProductServiceImpl` asks Spring for a `NotificationService`, Spring panics because it finds *two* beans that match, throwing a `NoUniqueBeanDefinitionException`.
* **The Fix:** I used `@Qualifier("emailNotification")` inside the constructor parameter. This acts as a nametag, explicitly telling the `ApplicationContext` exactly which bean to inject.