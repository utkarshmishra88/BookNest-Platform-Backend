# BookNest Platform - Backend Microservices

Welcome to the backend repository for the **BookNest Platform**! This is a robust, highly scalable, and fully distributed e-commerce backend built with a modern Spring Boot microservice architecture.

## 🏗 Architecture

The platform is divided into 11 distinct, independently deployable services that communicate securely via REST and Spring Cloud.

1. **API Gateway (`api-gateway`)**: The single entry point for the frontend. Handles routing, load balancing, and global CORS configuration.
2. **Service Registry (`eureka-server`)**: Netflix Eureka server for dynamic service discovery.
3. **Auth Service (`auth-service`)**: Handles User registration, JWT generation, and Google/GitHub OAuth2 authentication.
4. **Book Service (`book-service`)**: Manages the book catalog, categories, inventory, and pricing.
5. **Cart Service (`cart-service`)**: Manages shopping carts and session-based logic.
6. **Order Service (`order-service`)**: Processes checkouts, creates invoices, and communicates with Payment/Cart/Book services via Feign Clients.
7. **Payment Service (`payment-service`)**: Handles secure transaction processing and Razorpay integrations.
8. **Wallet Service (`wallet-service`)**: Manages user digital wallets, top-ups, and balance deductions.
9. **Notification Service (`notification-service`)**: Sends asynchronous email confirmations, OTPs, and PDF invoices.
10. **Review Service (`review-service`)**: Allows users to leave and read reviews/ratings for books.
11. **Wishlist Service (`wishlist-service`)**: Manages user wishlists.

## 💻 Tech Stack

* **Framework:** Java, Spring Boot 3.x
* **Microservices:** Spring Cloud (Gateway, OpenFeign, Netflix Eureka)
* **Security:** Spring Security, JWT (JSON Web Tokens), OAuth2
* **Database:** MySQL 8.x
* **Infrastructure:** Docker, Docker Compose
* **Build Tool:** Maven

## 🚀 How to Run Locally

We use Docker Compose to make running all 11 microservices locally as easy as possible.

### 1. Setup Environment Variables
Create a file named `.env` in the root directory and add your credentials:

# Database
DB_PASSWORD=your_secure_password

# Authentication
JWT_SECRET=YourSuperSecretKey
INTERNAL_API_KEY=your_internal_api_key

# Email Server (For OTPs & Invoices)
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

# OAuth Providers
GOOGLE_CLIENT_ID=your_google_id
GOOGLE_CLIENT_SECRET=your_google_secret
GITHUB_CLIENT_ID=your_github_id
GITHUB_CLIENT_SECRET=your_github_secret

### 2. Build and Run via Docker Compose
Ensure you have Docker Desktop installed and running. Open your terminal in the root directory and run:

docker-compose up -d --build

### 3. Verify Deployment
* **Eureka Dashboard:** http://localhost:8761
* **API Gateway (Frontend Entrypoint):** http://localhost:8080
