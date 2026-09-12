# CRM Platform Java

A lightweight Java CRM MVP built with Java 21, Spring Boot, Thymeleaf, Spring Data JPA, PostgreSQL, Docker and Maven.

## Stack

- Java 21
- Spring Boot 3.5.5
- Spring MVC
- Thymeleaf
- Spring Data JPA
- PostgreSQL 17
- Docker / Docker Compose
- Maven
- GitHub Actions

## Run locally

```bash
mvn spring-boot:run
```

Or with Docker Compose:

```bash
docker compose -f docker/docker-compose.yml up --build
```

Application: http://localhost:8080

Health: http://localhost:8080/actuator/health

## API

- `GET /api/customers`
- `POST /api/customers`
- `DELETE /api/customers/{id}`

Infrastructure examples are provided under `infra/aws` and `infra/azure`.
