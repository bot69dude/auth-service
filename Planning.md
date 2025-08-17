# VitaSync Project Plan

### Project Overview

**VitaSync** is a microservices-based application designed to connect blood donors with patients in need of transfusions. The system automates the process of requesting blood, finding available donors, and coordinating donations, with a focus on real-time communication and data-driven insights.

The backend is built as a set of decoupled microservices that communicate asynchronously using an event-driven architecture.

---

### 1. Architecture

The system is composed of several independent microservices. Communication between services is handled primarily through a **Kafka** message broker, ensuring resilience and scalability. Frontend applications will communicate with the services via REST APIs.

**Core Services:**
* **User & Authentication Service:** Handles user registration, authentication, and profiles.
* **Transfusion & Donation Service:** Manages transfusion requests and donor availability.
* **Notification Service:** Consumes events from Kafka to send real-time notifications via third-party APIs (e.g., WhatsApp).
* **Inventory & Analytics Service:** Tracks real-time blood inventory and provides data for an analytics dashboard.
* **AI Forecasting Service:** Provides a predictive model for blood demand.

**Communication Flow:**
`Transfusion Service` --(REST)--> `Frontend`
`Transfusion Service` --(Event via Kafka)--> `Notification Service`
`Transfusion Service` --(Event via Kafka)--> `Inventory Service`

---

### 2. Technology Stack

* **Language:** Java 17+
* **Framework:** Spring Boot 3+ (with WebFlux for a reactive, non-blocking architecture)
* **Database:** PostgreSQL (with Spring Data R2DBC for reactive access)
* **API Documentation:** SpringDoc OpenAPI / Swagger UI
* **Event Broker:** Apache Kafka
* **Shared Events:** A shared Maven library for DTOs (`vitasync-shared-events`)
* **Notification Provider:** Twilio / Azure Communication Services
* **Real-time Cache:** Redis Enterprise (for the Inventory Service)
* **Containerization:** Docker / Docker Compose

---

### 3. Completed Milestones

This section outlines the significant progress that has been made on the `Transfusion & Donation Service` and the overall backend infrastructure.

* **Milestone 1: Foundational Setup**
    * Project setup with Spring Boot WebFlux, PostgreSQL, and R2DBC.
    * Database schema created for `transfusion_requests` and `donation_schedules`.
    * `TransfusionService` and `TransfusionController` implemented for creating transfusion requests.
    * `DonationService` and `DonationController` implemented for managing donor availability.
    * Local development environment with PostgreSQL running via Docker.

* **Milestone 2: API Documentation & Event-Driven Core**
    * Integrated **SpringDoc OpenAPI** to provide interactive API documentation via Swagger UI.
    * Configured a local **Kafka broker** using Docker.
    * Implemented a **Kafka producer** in the `TransfusionService` to publish a `TransfusionRequestedEvent`.
    * Created a new, decoupled `vitasync-notification-service` to act as a **Kafka consumer**.
    * Correctly designed a **shared Maven library** (`vitasync-shared-events`) to avoid tight coupling between services.
    * Successfully tested the end-to-end event-driven pipeline.

---

### 4. Upcoming Milestones & Next Steps

This is the roadmap for completing the core backend functionality.

#### **Milestone 3: Complete Notification Service**

* **Goal:** The `NotificationService` sends a real-world message to a user.
* **Tasks:**
    * Choose a notification provider (Twilio or Azure Communication Services).
    * Add the provider's SDK as a dependency to the `notification-service`.
    * Implement a `NotificationSender` service that uses the SDK and credentials.
    * Update the `KafkaConsumer` to call the `NotificationSender` when an event is received.

#### **Milestone 4: Build User & Authentication Service**

* **Goal:** A standalone service to handle user management.
* **Tasks:**
    * Create a new Spring Boot project.
    * Implement user registration and login endpoints.
    * Integrate a security framework (e.g., Spring Security with JWT).
    * (Optional) Publish user events (e.g., `UserRegisteredEvent`) to Kafka.

#### **Milestone 5: Build Inventory & Analytics Service**

* **Goal:** A service to manage and analyze blood inventory.
* **Tasks:**
    * Create a new Spring Boot project.
    * Integrate **Redis Enterprise** for real-time, in-memory blood inventory.
    * Implement a Kafka consumer to listen for `TransfusionRequestedEvent` and update inventory numbers.
    * Create REST endpoints to retrieve real-time inventory and analytics data.

#### **Milestone 6: AI Forecasting Integration**

* **Goal:** Integrate the predictive AI service.
* **Tasks:**
    * Expose the AI model as a REST API endpoint.
    * Update the `Inventory & Analytics Service` to consume this endpoint.
    * Use the forecast data to proactively manage inventory and predict shortages.