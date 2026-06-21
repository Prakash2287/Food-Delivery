# Food Delivery Order Management

Small Spring Boot 3 + Java 17 practice project for the PDF requirement. The goal here is not to build a production-grade food delivery platform, but a solid college-project-style backend that demonstrates domain modeling, role-based APIs, transactions, validation, and a few concurrency-aware decisions.

## Scope Chosen

This implementation includes:

- Admin APIs to create cities, restaurants, and delivery partners
- Restaurant owner APIs to manage menu items and act on orders
- Customer APIs to browse restaurants, place orders, track orders, and add reviews
- Delivery partner APIs to accept delivery assignments and update delivery state
- H2 in-memory database with seeded sample data
- Basic role-based access control using Spring Security with HTTP Basic auth
- Transactional order placement with optimistic locking on stock-bearing menu items
- Transactional delivery assignment acceptance to avoid double-claiming
- Async notification hooks using Spring events and `@Async`
- Focused unit tests for order placement logic

This implementation intentionally skips:

- UI/frontend
- Real payment integration
- JWT/OAuth
- External messaging brokers
- Full integration test coverage

## Tech Stack

- Java 17 target
- Spring Boot 3.3.1
- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- H2 in-memory database
- JUnit 5 + Mockito

## Important Local Setup Notes

Java 17 and Maven were installed locally under Desktop so the project can run outside the Codex workspace path:

- JDK: `C:\Users\THIS PC\Desktop\dev-tools\java17\jdk-17.0.19+10`
- Maven: `C:\Users\THIS PC\Desktop\dev-tools\maven\apache-maven-3.9.16`
- Project copy: `C:\Users\THIS PC\Desktop\food-delivery-order-management`

User-level `JAVA_HOME`, `MAVEN_HOME`, and `Path` entries were also set. If VS Code or an older terminal was already open before installation, close and reopen it so the updated environment variables are picked up.

If you want to run it in the current terminal session manually:

```powershell
$env:JAVA_HOME="C:\Users\THIS PC\Desktop\dev-tools\java17\jdk-17.0.19+10"
$env:MAVEN_HOME="C:\Users\THIS PC\Desktop\dev-tools\maven\apache-maven-3.9.16"
$env:Path="$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:Path"
```

## Running In VS Code On Windows

After reopening VS Code or setting the environment variables in the terminal:

```powershell
mvn clean test
mvn spring-boot:run
```

App endpoints:

- Base URL: `http://localhost:8080`
- H2 console: `http://localhost:8080/h2-console`

H2 settings:

- JDBC URL: `jdbc:h2:mem:food_delivery`
- Username: `sa`
- Password: `password`

## Demo Credentials

All passwords are plain demo passwords because this is a practice project.

- `admin / admin123`
- `owner1 / owner123`
- `owner2 / owner123`
- `customer1 / customer123`
- `customer2 / customer123`
- `partner1 / partner123`
- `partner2 / partner123`

## Domain Summary

Main entities:

- `City`
- `Restaurant`
- `MenuItem`
- `CustomerOrder`
- `OrderItem`
- `DeliveryPartner`
- `DeliveryAssignment`
- `Review`

Key workflow:

1. Customer browses restaurants and menu items
2. Customer places an order
3. Stock is deducted inside the same transaction
4. Restaurant owner accepts or rejects the order
5. On acceptance, a pending delivery assignment is created
6. Delivery partner accepts the assignment
7. Restaurant owner/partner moves the order through its lifecycle
8. Customer adds a review after delivery

## Main Assumptions

- One order belongs to a single restaurant
- Payment is simulated and captured as `PAID` at order placement time
- Rejected orders restock the deducted inventory
- Delivery assignment is created only after the restaurant accepts the order
- A partner can accept only one assignment record for a given order
- Reviews are allowed only after delivery and only once per order
- Basic auth with in-memory users is sufficient for this practice implementation

## Example API Flow

### 1. List restaurants in a city

```powershell
curl -u customer1:customer123 "http://localhost:8080/api/customer/restaurants?city=Delhi"
```

### 2. Place an order

```powershell
curl -u customer1:customer123 -X POST "http://localhost:8080/api/customer/orders" `
  -H "Content-Type: application/json" `
  -d '{
    "restaurantId": 1,
    "items": [
      { "menuItemId": 1, "quantity": 2 },
      { "menuItemId": 2, "quantity": 1 }
    ]
  }'
```

### 3. Owner accepts the order

```powershell
curl -u owner1:owner123 -X POST "http://localhost:8080/api/owner/orders/1/decision" `
  -H "Content-Type: application/json" `
  -d '{ "accepted": true }'
```

### 4. Partner accepts assignment

```powershell
curl -u partner1:partner123 -X POST "http://localhost:8080/api/partner/assignments/1/accept"
```

### 5. Owner marks preparing

```powershell
curl -u owner1:owner123 -X PATCH "http://localhost:8080/api/owner/orders/1/status" `
  -H "Content-Type: application/json" `
  -d '{ "status": "PREPARING" }'
```

### 6. Partner marks out for delivery

```powershell
curl -u partner1:partner123 -X PATCH "http://localhost:8080/api/partner/orders/1/status" `
  -H "Content-Type: application/json" `
  -d '{ "status": "OUT_FOR_DELIVERY" }'
```

## Project Structure

- `src/main/java/com/codex/fooddelivery/config` - security, async config, seed data
- `src/main/java/com/codex/fooddelivery/controller` - REST APIs
- `src/main/java/com/codex/fooddelivery/domain` - entities and enums
- `src/main/java/com/codex/fooddelivery/dto` - request/response models
- `src/main/java/com/codex/fooddelivery/exception` - business exceptions and handler
- `src/main/java/com/codex/fooddelivery/repository` - JPA repositories
- `src/main/java/com/codex/fooddelivery/service` - business logic
- `src/test/java/com/codex/fooddelivery/service` - unit tests

## What To Improve Next

- Add pagination and filtering
- Add order cancellation before acceptance
- Replace in-memory users with persisted users
- Add integration tests with `@SpringBootTest`
- Add audit fields and better reporting
