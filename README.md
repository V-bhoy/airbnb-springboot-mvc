# 🏡 Airbnb Backend Clone

A feature-rich backend system inspired by Airbnb, built with **Spring Boot**, **PostgreSQL**, **JWT-based authentication**, and **Stripe for payments**. This application supports user registration, property listing, booking management, and secure payment flows.

> 🚀 This is a backend-only application and is currently not deployed. You can run it locally or integrate it with a frontend of your choice.

---

## 🧰 Tech Stack

- **Backend Framework:** Spring Boot 3.5.3
- **Language:** Java 17
- **Database:** PostgreSQL
- **ORM:** Spring Data JPA + Hibernate
- **Security:** Spring Security + JWT
- **Payment Integration:** Stripe
- **API Documentation:** OpenAPI (Swagger UI via Springdoc)
- **Other:** ModelMapper, Lombok, Maven

---

## 📐 Architecture Overview

- **Controller Layer:** Handles HTTP requests and routes them to appropriate services.
- **Service Layer:** Contains business logic.
- **Repository Layer:** Interacts with the PostgreSQL database using Spring Data JPA.
- **Security Layer:** Manages authentication (login/signup), token validation, and role-based authorization.
- **DTOs:** Data Transfer Objects are used to avoid direct exposure of entities and ensure clean API contracts.
- **ModelMapper:** Maps between Entities and DTOs.

---

## ⚙️ Features

- 🔐 User Signup/Login with JWT Authentication
- 🏠 List Properties for Rent
- 📅 Book Properties with Date and Guest Info
- 💳 Payment Integration using Stripe
- 👨‍💼 Role-based Access Control (Admin / Guest user)
- 📄 Swagger API Docs for easy testing

---

## 🚧 Endpoints Overview (Sample)

| Endpoint                     | Method | Description                      |
|-----------------------------|--------|----------------------------------|
| `/api/v1/auth/signup`       | POST   | Register a new user              |
| `/api/v1/auth/login`        | POST   | Login and receive JWT token      |
| `/api/v1/admin/hotel/all`   | GET    | View all listed properties       |
| `/api/v1/admin/hotel`       | POST   | Add a new property (host only)   |
| `/api/v1/bookings/initiate` | POST   | Book a property                  |
| `/api/v1/{id}/payments`     | POST   | Create a Stripe payment intent   |

📌 Full API docs available at: `http://localhost:8080/swagger-ui.html`

---

## 🛠️ Setup Instructions

1. **Clone the repository**

```bash
git clone https://github.com/V-bhoy/airbnb-springboot-mvc.git
cd airbnb-springboot-mvc
cd airBnbApp
```

2.	**Configure PostgreSQL**

```bash
Create a database (e.g., airbnb_db)
update application.properties or application.yml with your DB credentials
```

3. **Run the Application**
4. **Testing**
   
```bash
Use Postman or Swagger UI to test endpoints.
JWT token will be required for protected routes.
```

## 📌 Notes

- This backend is designed to be integrated with a frontend (React, Angular, etc.).
- Application currently runs locally; deployment will be added later.
- Stripe is integrated in test mode.

