# Banking Transaction Service

A Spring Boot application for managing banking transactions with in-memory storage and caching capabilities.

## Features

- Create, read, update, and delete transactions
- Pagination support for transaction listing
- In-memory storage with Caffeine caching
- Comprehensive error handling
- RESTful API design
- Docker support

## Technologies Used
- Java 21
- Spring Boot 3.4.3
- Caffeine Cache
- Lombok
- Maven 3.9.6 or above
- Jackson-databind
---
## Imported Dependencies and Their Purpose

### **1. Spring Boot Starter WebFlux**
- **Dependency**: `org.springframework.boot:spring-boot-starter-webflux`
- **Purpose**: Provides the necessary components for building reactive web applications using Spring WebFlux, allowing for non-blocking and asynchronous programming.

### **2. Spring Boot Starter Actuator**
- **Dependency**: `org.springframework.boot:spring-boot-starter-actuator`
- **Purpose**: Adds production-ready features to the application, such as health checks, metrics, and monitoring endpoints to help manage and monitor the application.

### **3. Spring Boot Starter Cache**
- **Dependency**: `org.springframework.boot:spring-boot-starter-cache`
- **Purpose**: Enables caching support in the application, allowing for the configuration and use of various caching mechanisms to improve performance.

### **4. Spring Boot Starter Validation**
- **Dependency**: `org.springframework.boot:spring-boot-starter-validation`
- **Purpose**: Provides support for validating JavaBeans using JSR-303/JSR-380 (Bean Validation) annotations, facilitating data validation in the application.

### **5. Spring Data Commons**
- **Dependency**: `org.springframework.data:spring-data-commons`
- **Purpose**: Provides the foundational APIs and infrastructure for Spring Data projects, enabling data access and repository support.

### **6. Caffeine**
- **Dependency**: `com.github.ben-manes.caffeine:caffeine`
- **Purpose**: Implements an in-memory caching layer for optimizing read-heavy APIs and reducing latency by caching frequently accessed data.

### **7. Lombok**
- **Dependency**: `org.projectlombok:lombok`
- **Purpose**: A Java library that helps reduce boilerplate code by providing annotations for generating getters, setters, constructors, and other common methods at compile time.

### **8. Jakarta Servlet API**
- **Dependency**: `jakarta.servlet:jakarta.servlet-api`
- **Purpose**: Provides the API for creating servlets, which are Java programs that run on a server and handle requests and responses in web applications.

### **9. Spring Boot Starter Test**
- **Dependency**: `org.springframework.boot:spring-boot-starter-test`
- **Purpose**: Includes testing libraries and tools for Spring Boot applications, facilitating unit and integration testing.

### **10. Mockito Core**
- **Dependency**: `org.mockito:mockito-core`
- **Purpose**: A mocking framework for unit tests in Java, allowing for the creation of mock objects and verifying interactions in tests.

### **11. Mockito JUnit Jupiter**
- **Dependency**: `org.mockito:mockito-junit-jupiter`
- **Purpose**: Provides support for using Mockito with JUnit 5, enabling seamless integration for writing tests.

### **12. Reactor Test**
- **Dependency**: `io.projectreactor:reactor-test`
- **Purpose**: Provides testing utilities for Project Reactor, allowing for the testing of reactive streams and asynchronous code.

### **13. Spring REST Docs WebTestClient**
- **Dependency**: `org.springframework.restdocs:spring-restdocs-webtestclient`
- **Purpose**: Facilitates the generation of API documentation for Spring WebFlux applications using WebTestClient, enabling easy documentation of RESTful APIs.

### **14. Jackson Databind**
- **Dependency**: `com.fasterxml.jackson.core:jackson-databind`
- **Purpose**: Provides functionality for converting Java objects to and from JSON, enabling serialization and deserialization in the application.

---
# Transaction API Documentation
## Overview
This API provides endpoints to manage transactions. It includes functionalities to create, update, delete, and retrieve transactions.
## Base URL
/api/v1/transactions
## API Endpoints
- POST /api/v1/transactions - Create a new transaction
- GET /api/v1/transactions - List all transactions (with pagination)
- GET /api/v1/transactions/{id} - Get a specific transaction
- PUT /api/v1/transactions/{id} - Update a transaction
- DELETE /api/v1/transactions/{id} - Delete a transaction

## Details of ALL Endpoints
### 1. Create Transaction
- **Endpoint:** `POST /api/v1/transactions`
- **Description:** Creates a new transaction.
- **Request Body:**
    - **Content-Type:** `application/json`
    - **Schema:**
      json
      {
      "id": "Long",
      "amount": "BigDecimal",
      "description": "String",
      "date": "LocalDate"
      }

- **Responses:**
    - **201 Created:** Returns the created transaction.
    - **400 Bad Request:** If validation fails, returns error messages.
### 2. Update Transaction
- **Endpoint:** `PUT /api/v1/transactions/{id}`
- **Description:** Updates an existing transaction by ID.
- **Path Parameters:**
    - `id` (required): The ID of the transaction to update.
- **Request Body:**
    - **Content-Type:** `application/json`
    - **Schema:** Same as Create Transaction.
- **Responses:**
    - **200 OK:** Returns the updated transaction.
    - **400 Bad Request:** If validation fails, returns error messages.
### 3. Delete Transaction
- **Endpoint:** `DELETE /api/v1/transactions/{id}`
- **Description:** Deletes a transaction by ID.
- **Path Parameters:**
    - `id` (required): The ID of the transaction to delete.
- **Responses:**
    - **204 No Content:** Successfully deleted.
    - **404 Not Found:** If the transaction with the specified ID does not exist.
### 4. Get Transaction
- **Endpoint:** `GET /api/v1/transactions/{id}`
- **Description:** Retrieves a transaction by ID.
- **Path Parameters:**
    - `id` (required): The ID of the transaction to retrieve.
- **Responses:**
    - **200 OK:** Returns the requested transaction.
    - **404 Not Found:** If the transaction with the specified ID does not exist.
### 5. Get All Transactions
- **Endpoint:** `GET /api/v1/transactions`
- **Description:** Retrieves a paginated list of all transactions.
- **Query Parameters:**
    - `page` (optional, default=0): The page number to retrieve.
    - `size` (optional, default=10): The number of transactions per page.
- **Responses:**
    - **200 OK:** Returns a paginated list of transactions.
## Error Handling
Validation errors will return a `400 Bad Request` status with a message detailing the validation issues. For example:
json
{
"error": "amount: must not be null; description: must not be empty"
}

## Notes
- All endpoints are accessible via CORS.
- Ensure that the request body adheres to the specified schema to avoid validation errors.

## Building and Running

### **Prerequisites**
- Java 21 or later
- Maven

### Unit test
```
mvn test
```

### Build and run
```
mvn clean package -Dmaven.test.skip=true
java -jar target/transaction_management-0.0.1-SNAPSHOT.jar
```

## Docker
```bash
docker build -t transaction_management .
docker run -p 8080:8080 transaction_management
```

## k8s
```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

### Using Maven
