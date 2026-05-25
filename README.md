# Travel360 - Travel Management System

A full-stack microservices-based Travel Management System built with **Spring Boot 3.2.5**, **Spring Cloud 2023.0.3**, and **Java 21**.

---

## Architecture Overview

The system follows a microservices architecture with an API Gateway as the single entry point.

```
Client → API Gateway (9002) → Microservices
                ↓
         Eureka Server (8761) - Service Discovery
         Config Server (9001) - Centralized Configuration
```

---

## Services & Ports

| Service | Port | Description |
|---------|------|-------------|
| Eureka Server | 8761 | Service Discovery |
| Config Server | 9001 | Centralized Configuration |
| API Gateway | 9002 | Single entry point, JWT validation |
| IAM Service | 9003 | Authentication & Authorization |
| Partner Inventory Service | 9005 | Partners, packages, destinations |
| Itinerary Service | 9006 | Travel itinerary management |
| Billing Payment Service | 9007 | Invoices & payments |
| Compliance Audit Service | 9008 | Audit logs & compliance records |
| Booking Service | 9009 | Bookings & reservations |
| Analytics Service | 9010 | Reports, KPIs & dashboard |
| Notification Service | 9011 | Notifications & templates |

---

## Tech Stack

- **Java 21**
- **Spring Boot 3.2.5**
- **Spring Cloud 2023.0.3** (Eureka, Config Server, Gateway, OpenFeign)
- **Spring Security** with JWT (JJWT 0.12.3)
- **Spring Data JPA** + **MySQL 8**
- **Resilience4j** - Circuit Breaker
- **Lombok**
- **SpringDoc OpenAPI 2.3.0** (Swagger UI)
- **Maven** (Multi-module project)

---

## Prerequisites

- Java 21
- Maven
- MySQL 8 (running on localhost:3306)
- IntelliJ IDEA (recommended)

---

## MySQL Setup

Create the following databases (or let JPA auto-create them with `createDatabaseIfNotExist=true`):

```sql
CREATE DATABASE travel_iam_db;
CREATE DATABASE travel_partner_db;
CREATE DATABASE travel_booking_db;
CREATE DATABASE travel_itinerary_db;
CREATE DATABASE travel_billing_db;
CREATE DATABASE travel_compliance_db;
CREATE DATABASE travel_analytics_db;
CREATE DATABASE travel_notification_db;
```

MySQL credentials (default):
- Username: `root`
- Password: `Root`

---

## How to Run

Start services **in this exact order**:

1. **Eureka Server** → wait for it to be fully up
2. **Config Server** → wait for it to be fully up
3. **API Gateway**
4. All remaining services (in any order):
   - IAM Service
   - Partner Inventory Service
   - Booking Service
   - Itinerary Service
   - Billing Payment Service
   - Compliance Audit Service
   - Analytics Service
   - Notification Service

---

## Swagger UI

All APIs are accessible via the **API Gateway aggregated Swagger UI**:

```
http://localhost:9002/swagger-ui.html
```

Use the dropdown to switch between services. Authorize once with your JWT token.

Individual service Swagger UIs are also available at:
```
http://localhost:{port}/swagger-ui.html
```

---

## Authentication

### Register
```
POST http://localhost:9002/api/iam/auth/register
```
```json
{
  "username": "admin",
  "password": "Admin@123",
  "email": "admin@travel.com",
  "fullName": "Admin User",
  "role": "ROLE_ADMIN"
}
```

### Login
```
POST http://localhost:9002/api/iam/auth/login
```
```json
{
  "username": "admin",
  "password": "Admin@123"
}
```

Copy the `accessToken` from the response and use it as `Bearer <token>` in the Authorization header.

---

## Available Roles

| Role | Description |
|------|-------------|
| ROLE_ADMIN | Full access to all services |
| ROLE_TRAVELER | Basic booking and itinerary access |
| ROLE_TRAVEL_AGENT | Partner and booking management |
| ROLE_CORPORATE_MANAGER | Analytics and reporting access |
| ROLE_FINANCE_OFFICER | Billing and KPI access |
| ROLE_COMPLIANCE_OFFICER | Compliance reports access |

---

## Service Overview

### IAM Service
- User registration and login
- JWT token generation (HS512, 24h expiry)
- User management (CRUD)

### Partner Inventory Service
- Travel partners management
- Tour packages (with pricing tiers)
- Destinations management
- Package search and filtering

### Booking Service
- Create and manage bookings
- Reservation management
- Booking status workflow: `PENDING → CONFIRMED → CANCELLED`

### Itinerary Service
- Personal travel itineraries
- Itinerary items (activities)
- Link/unlink bookings to itineraries

### Billing Payment Service
- Invoice generation
- Payment processing
- Refund management
- Overdue invoice tracking

### Compliance Audit Service
- Audit log tracking (with AOP `@Auditable` aspect)
- Compliance records management
- Regulatory report generation (GDPR, Security, Data Retention)

### Analytics Service
- Analytics report generation
- Dashboard metrics
- KPI reports (Booking Volume, Revenue, Cancellation Rate, Spend per Traveler)

### Notification Service
- Send individual and bulk notifications
- Notification types: EMAIL, SMS, PUSH, IN_APP
- Template management with `{{variable}}` substitution
- Mark as read, retry failed notifications

---

## Key Features

- **JWT Authentication** — Gateway validates token before routing to any service
- **Role-Based Access Control** — `@PreAuthorize` on all secured endpoints
- **Circuit Breaker** — Resilience4j for fault tolerance on Feign calls
- **AOP Audit Logging** — `@Auditable` annotation auto-logs method calls
- **Centralized Config** — All service configs managed by Config Server
- **Service Discovery** — Eureka for dynamic service registration
- **Aggregated Swagger** — Single Swagger UI for all 8 services

---

## Project Structure

```
travel-management-system/
├── eureka-server/
├── config-server/
│   └── src/main/resources/config/     ← service-specific properties
├── api-gateway/
├── iam-service/
├── partner-inventory-service/
├── booking-service/
├── itinerary-service/
├── billing-payment-service/
├── compliance-audit-service/
├── analytics-service/
└── notification-service/
```

---

## Setting Up on Your Local Machine

### Step 1 — Change MySQL Password

All service configurations are centrally managed by the **Config Server**.
You only need to edit files in ONE place:

```
config-server/src/main/resources/config/
```

Open each `.properties` file in that folder and update the MySQL password:

```properties
spring.datasource.password=YourMySQLPassword   ← change this in all 8 files
```

Files to update (all inside the same folder):
- `iam-service.properties`
- `partner-inventory-service.properties`
- `booking-service.properties`
- `itinerary-service.properties`
- `billing-payment-service.properties`
- `compliance-audit-service.properties`
- `analytics-service.properties`
- `notification-service.properties`

> **You do NOT need to touch any individual service's `application.properties`** — that's the whole point of the Config Server!

### Step 2 — Check JDBC URL

If your MySQL runs on a port other than `3306`, update the URL in each file:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/travel_iam_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```
- `createDatabaseIfNotExist=true` — databases will be **auto-created**, no manual setup needed

### Step 3 — No other changes needed!

Everything else (JWT secret, service ports, Eureka config) works out of the box.

---

## Notes

- Token expiry: **24 hours** (`jwt.expiration=86400000`)
- All paginated endpoints accept `page` (default 0) and `size` (default 10) query params
- Soft delete is used in some services (records marked inactive, not physically deleted)
- The analytics dashboard shows zeros if Feign clients can't reach other services — this is expected in standalone mode
