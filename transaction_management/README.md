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
- Docker

## API Endpoints

- POST /api/v1/transactions - Create a new transaction
- GET /api/v1/transactions - List all transactions (with pagination)
- GET /api/v1/transactions/{id} - Get a specific transaction
- PUT /api/v1/transactions/{id} - Update a transaction
- DELETE /api/v1/transactions/{id} - Delete a transaction

## Building and Running
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
