# Finsage Backend API

The **Finsage Backend API** powers a personal finance tracking application. It provides secure RESTful endpoints for managing **incomes**, **expenses**, and **investments**, supporting user authentication with **JWT** and **Google OAuth2**. The system uses **PostgreSQL** as the primary database and **Redis** for caching performance-critical data.

---

##  Tech Stack

- **Spring Boot** – Java backend framework
- **PostgreSQL** – Relational database
- **Redis** – In-memory cache for performance
- **Spring Security** – JWT + OAuth2 authentication
- **Docker** – Containerized deployment
- **JUnit & Testcontainers** – Integration testing

---

##  Features

-  JWT & Google OAuth2 authentication
-  CRUD APIs for incomes, expenses, and investments
-  Real-time investment tracking
-  Redis caching for investment summaries
-  Stateless session management
-  Integration test support with PostgreSQL and Redis containers

---

## Getting Started

### Prerequisites

- Docker & Docker Compose

### Run with Docker Compose

1. Get the docker compose file and environment
    ```
   wget https://github.com/DGclasher/finsage-api/raw/refs/heads/main/docker-compose.yml
   ```
   ```
   wget https://github.com/DGclasher/finsage-api/raw/refs/heads/main/.env.example -O .env
   ```
2. Change the environment variables in `.env` file as needed.
3. Start the application
   ```
   docker compose up -d
   ```
4. To start the client application, do the following
   ```
   docker run -p 3000:3000 --network host --rm --name finsage-client dgclasher/finsage-client:latest -d
   ```
   
5. Access the client application at `http://localhost:3000`
6. To stop the application, run
   ```
   docker compose down
   ```
7. To stop the client application, run
   ```
   docker stop finsage-client
   ```
---
## API Documentation
For detailed API documentation, refer to the [Postman Docs](https://documenter.getpostman.com/view/24270306/2sB3BBpWwp).