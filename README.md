# Cartly (E-Commerce Backend)

Cartly is a robust, modular e-commerce backend API built entirely in Java. It provides a complete set of features for managing users, products, categories, shopping carts, addresses, and orders. 

This project follows Clean Architecture principles, utilizing Jakarta Servlets, Hibernate ORM, and MySQL to deliver a scalable and testable e-commerce solution.

---

## 🛠 Technology Stack

- **Language:** Java 25
- **Web API:** Jakarta Servlet (Tomcat 11.0)
- **Database:** MySQL 8.0
- **ORM:** Hibernate 6.6.x (JPA)
- **Build Tool:** Maven
- **Authentication:** Custom JWT/Session logic with BCrypt password hashing
- **JSON Serialization:** Jackson
- **Testing:** JUnit 5, Mockito

---

## 📦 Project Structure

```text
Server/
├── src/
│   ├── main/
│   │   ├── java/com/cartly/
│   │   │   ├── controller/   # API Endpoint Servlets (@WebServlet)
│   │   │   ├── service/      # Business Logic & Transactions
│   │   │   ├── dao/          # Data Access Objects (Hibernate queries)
│   │   │   ├── entity/       # JPA Entities
│   │   │   ├── dto/          # Data Transfer Objects
│   │   │   ├── filter/       # Global Filters (CORS, Authentication)
│   │   │   └── util/         # Utilities (Hibernate setup, Password encryption)
│   │   └── resources/
│   │       └── hibernate.cfg.xml
│   └── test/
│       └── java/com/cartly/  # Extensive Unit & Integration Tests (77 Tests)
├── pom.xml                   # Maven configuration
└── test_cart.bat             # End-to-End API test script using cURL
```

---

## 🚀 Features & API Endpoints

The backend is organized into RESTful endpoints mapped under the `/api/*` context.

### 1. Authentication (`/api/auth/*`)
- **`POST /register`**: Register a new user with BCrypt password hashing.
- **`POST /login`**: Authenticate and establish a session.
- **`POST /logout`**: Terminate the current session.

### 2. Products (`/api/products/*`)
- **`GET /`**: List all active products.
- **`GET /{id}`**: Get product details by ID.
- **`POST /`**: Create a new product.
- **`PUT /{id}`**: Update product details.

### 3. Categories (`/api/categories/*`)
- **`GET /`**: List all categories.
- **`GET /{id}`**: Get category details.
- **`POST /`**: Create a new category.

### 4. Shopping Cart (`/api/cart/*`)
- **`GET /`**: Get the current user's shopping cart and calculate total.
- **`POST /items`**: Add a product to the cart (auto-increments if it exists).
- **`PUT /items/{productId}`**: Update the quantity of a cart item.
- **`DELETE /items/{productId}`**: Remove an item from the cart.
- **`DELETE /`**: Clear the entire cart.

### 5. Addresses (`/api/addresses/*`)
- **`GET /`**: Fetch all saved addresses for the authenticated user.
- **`POST /`**: Add a new shipping/billing address.

### 6. Orders (`/api/orders/*`)
- **`POST /`**: Place an order from the current shopping cart (verifies stock, creates order items, deducts stock, and clears the cart).
- **`GET /`**: View past orders.

---

## ⚙️ Setup & Installation

### 1. Database Setup
Ensure MySQL is running on your machine on port `3306`.
Execute the `Database/schema.sql` script to create the `cartly_db` database and all required tables.

```sql
mysql -u root -p < Database/schema.sql
```

### 2. Build the Project
Navigate to the `Server` directory and build the project using Maven:
```bash
cd Server
mvn clean package
```

### 3. Run the Tests
The project includes a comprehensive test suite (77 tests covering DAOs and Services using Mockito and JUnit 5).
```bash
mvn test
```

### 4. Deployment
Deploy the generated `Cartly-0.0.1-SNAPSHOT.war` from the `target/` directory into your Tomcat 11 `webapps/` folder, or run the project directly through Eclipse WTP.

By default, the API will be accessible at:
`http://localhost:8084/Cartly/api/`

---

## 🧪 Testing the API
You can use the provided `test_cart.bat` script to run a full end-to-end integration test of the Cart lifecycle against a running server instance.

```bash
test_cart.bat
```
