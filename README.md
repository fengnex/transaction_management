# Transaction Management Project

This project consists of a backend application developed using Spring Boot 3 and a frontend application developed using React. The goal of this project is to provide a simple transaction management system.

## Project Structure

```plainText
.
├── transaction_management/  # Spring Boot 3 backend project
├── transaction_web/         # React frontend project
├── docker-compose.yml
└── README.md
```

## Backend - Spring Boot

### Prerequisites

- Java 21 or higher
- Maven
- Spring Boot 3
  
### Setup

1. Clone the repository:

```bash
git clone https://github.com/fengnex/transaction_management.git
cd transaction-management
```

2. Build the project:

```bash
mvn clean package -Dmaven.test.skip=true
```

3. Run the application:
```bash
mvn spring-boot:run
or
java -jar target/transaction-management-0.0.1-SNAPSHOT.jar
```  

4. The backend will be running on ```http://localhost:8080```.

### API Documentation

The backend exposes several RESTful endpoints for managing transactions. You can use tools like Postman or curl to interact with the API.

## Frontend - React

### Prerequisites

- Node.js (version 22 or higher)
- npm or yarn
  
### Setup

1. Navigate to the frontend directory:

```bash
cd transaction-web
```

2. Install the dependencies:

```bash
npm install
```

3. Start the development server:

```bash
npm start
```

4. The frontend will be running on ```http://localhost:3000```.	


## Features

- Create, read, update, and delete transactions.
- User-friendly interface for managing transactions.
- Responsive design for better usability.

## Contributing

We welcome contributions to improve the project. Please follow these steps:
1. Fork the repository.
2. Create a new branch for your feature or bug fix.
3. Make your changes and commit them.
4. Push your branch and submit a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgements

- Spring Boot for the backend framework.
- React for the frontend framework.
- All contributors for their valuable input.