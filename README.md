# Bank App API

A small Spring Boot REST API for managing customers, bank accounts, balances, and account transaction history.

The application currently uses in-memory repositories so it can be developed and tested without a database. The repository interfaces are already separated from the services, making a future database migration straightforward.

## Current Features

| Area | Supported operations |
| --- | --- |
| Customers | Create, list, retrieve, edit, and delete customers |
| Accounts | Create accounts for customers and retrieve account details |
| Money movement | Deposit and withdraw money |
| Transfers | Transfer money between two accounts owned by the same customer |
| History | View deposit, withdrawal, and transfer records for an account |
| Authentication | Register customers and log in with a JWT bearer token |
| Validation | Positive money amounts, required names, valid IDs, and sufficient funds |

## Technology Stack

| Technology | Version / role |
| --- | --- |
| Java | 17 |
| Spring Boot | 4.1.1 |
| Spring Web MVC | REST controllers and HTTP endpoints |
| Spring Validation | Request validation |
| Spring Security | Password hashing and protected endpoints |
| JJWT | JSON Web Token creation and verification |
| Maven Wrapper | Project builds without a global Maven installation |
| Storage | MongoDB Atlas through MongoTemplate; in-memory fixtures for unit tests |

## Prerequisites

- JDK 17 or newer
- Git
- A MongoDB Atlas cluster, database user, and password
- No global Maven installation is required

Check Java from PowerShell:

```powershell
java --version
```

## MongoDB Atlas Setup

The application reads the MongoDB connection string from the `MONGODB_URI` environment variable. Do not commit the URI, username, password, or any other credentials.

In MongoDB Atlas:

1. Open the `simple-bank-app` cluster.
2. Create or select a database user.
3. Add your development IP address under **Network Access**.
4. Choose **Connect -> Drivers**, select Java, and copy the connection string.
5. Replace the username and password placeholders in the connection string.

Set the URI for the current PowerShell session:

```powershell
$env:MONGODB_URI = "mongodb+srv://USERNAME:PASSWORD@simple-bank-app.xxxxx.mongodb.net/simplebankapp?retryWrites=true&w=majority"
```

Then start the application:

```powershell
.\mvnw.cmd spring-boot:run
```

If the password contains characters such as `@`, `:`, `/`, or `#`, URL-encode the password before placing it in the URI. The application falls back to `mongodb://localhost:27017/simplebankapp` when `MONGODB_URI` is not set, which is useful for local MongoDB development but will not connect to Atlas.

## Run the Application

From the project root on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```bash
./mvnw spring-boot:run
```

The API starts at:

```text
http://localhost:8080
```

Opening `http://localhost:8080/` may show Spring Boot's Whitelabel 404 page. Use one of the API routes below instead.

### Configure the admin login

Set these variables in the backend terminal before starting the application. The app creates or updates the configured admin customer at startup and stores the password as a BCrypt hash. The configured username must not already belong to a regular customer.

```powershell
$env:ADMIN_USERNAME = "bank-admin"
$env:ADMIN_PASSWORD = "<choose a strong password>"
.\mvnw.cmd spring-boot:run
```

If either variable is missing, no admin account is created. Never commit the password or add it to frontend code.

## Run Tests

Windows:

```powershell
.\mvnw.cmd test
```

macOS or Linux:

```bash
./mvnw test
```

Maven includes focused customer-profile and JWT identity tests. Run the Postman collection below to exercise the full API workflow.

## API Overview

All endpoints return JSON unless otherwise noted.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Register a customer with login credentials |
| `POST` | `/api/auth/login` | Authenticate and receive a JWT |
| `GET` | `/api/customers/me` | Get the signed-in customer's profile |
| `PUT` | `/api/customers/me` | Edit the signed-in customer's name, username, or password |
| `GET` | `/api/customers` | Get all customers |
| `GET` | `/api/customers/{id}` | Get one customer |
| `POST` | `/api/customers` | Create a customer (authenticated/internal flow) |
| `PUT` | `/api/customers/{id}` | Edit a customer's name |
| `DELETE` | `/api/customers/{id}` | Delete a customer |
| `POST` | `/api/accounts` | Create an account |
| `GET` | `/api/accounts/me` | Get the signed-in customer's accounts |
| `GET` | `/api/accounts?userId={id}` | List a customer's accounts (admin only) |
| `GET` | `/api/accounts/{id}` | Get account details |
| `DELETE` | `/api/accounts/{id}` | Delete an account only when its balance is zero |
| `POST` | `/api/accounts/{id}/deposit` | Deposit money |
| `POST` | `/api/accounts/{id}/withdraw` | Withdraw money |
| `POST` | `/api/accounts/transfer` | Transfer money between accounts |
| `GET` | `/api/accounts/{id}/transactions` | Get account transaction history |

Protected endpoints require:

```http
Authorization: Bearer <token>
```

Newly issued JWTs identify the customer by ID rather than username. After upgrading from older versions, sign in again to receive a new token.

## Authentication Examples

### Register a customer

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json
```

```json
{
  "name": "Ada Lovelace",
  "username": "ada",
  "password": "password123"
}
```

Passwords are hashed with BCrypt and are never returned by the API.

### Log in

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json
```

```json
{
  "username": "ada",
  "password": "password123"
}
```

Example response:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "customerId": 1,
  "username": "ada",
  "admin": false
}
```

Regular sign-in is available at `/api/auth/login`. Admin sign-in uses `/api/auth/admin/login` and rejects non-admin customers. Use the returned token in the `Authorization` header.

## Customer Examples

### Create a customer

```http
POST http://localhost:8080/api/customers
Content-Type: application/json
```

```json
{
  "name": "Ada Lovelace"
}
```

Example response:

```json
{
  "id": "671a9b8a8f4b2c1d9a123400",
  "name": "Ada Lovelace"
}
```

Expected status: `201 Created`

### Get all customers

```http
GET http://localhost:8080/api/customers
```

This endpoint requires an admin token. Admins can list a customer's accounts with `GET /api/accounts?userId=<customerId>`.

Regular customers can list only their own accounts with:

```http
GET http://localhost:8080/api/accounts/me
```

### Get one customer

```http
GET http://localhost:8080/api/customers/671a9b8a8f4b2c1d9a123400
```

### Edit your profile

Customers can read their own profile with `GET /api/customers/me`. To change their name, username, or password:

```http
PUT http://localhost:8080/api/customers/me
Content-Type: application/json
Authorization: Bearer <token>
```

```json
{
  "name": "Ada Byron",
  "username": "ada-byron",
  "currentPassword": "password123",
  "newPassword": "newpassword123"
}
```

Only `name` and `username` are required. A name-only change does not need `currentPassword`; changing the username or supplying `newPassword` requires it. New passwords must be at least 8 characters and are stored as BCrypt hashes. Usernames already in use return `409 Conflict`. The frontend signs out after a password change so the customer can sign in with the new password. Previously issued JWTs are not revoked server-side and remain valid until they expire.

### Edit a customer as admin

```http
PUT http://localhost:8080/api/customers/671a9b8a8f4b2c1d9a123400
Content-Type: application/json
```

```json
{
  "name": "Ada Byron"
}
```

Expected status: `200 OK`

### Delete a customer

```http
DELETE http://localhost:8080/api/customers/671a9b8a8f4b2c1d9a123400
```

Expected status: `204 No Content`

## Account Examples

A customer can own multiple accounts, such as a savings account and a checking account. The relationship is represented by `userId` on each account.

### Create an account

```http
POST http://localhost:8080/api/accounts
Content-Type: application/json
```

```json
{
  "userId": "671a9b8a8f4b2c1d9a123400",
  "accountType": "SAVINGS"
}
```

Supported account types:

- `SAVINGS`
- `CHECKING`

Example response:

```json
{
  "id": "671a9c2e8f4b2c1d9a123456",
  "userId": "671a9b8a8f4b2c1d9a123400",
  "accountType": "SAVINGS",
  "balance": 0
}
```

Expected status: `201 Created`

### List a customer's accounts

```http
GET http://localhost:8080/api/accounts?userId=671a9b8a8f4b2c1d9a123400
```

The response is a list of accounts, each including its `accountType` and current `balance`.

### Get account details

```http
GET http://localhost:8080/api/accounts/671a9c2e8f4b2c1d9a123456
```

Example response:

```json
{
  "id": "671a9c2e8f4b2c1d9a123456",
  "userId": "671a9b8a8f4b2c1d9a123400",
  "accountType": "SAVINGS",
  "balance": 500.00
}
```

### Delete an empty account

```http
DELETE http://localhost:8080/api/accounts/671a9c2e8f4b2c1d9a123456
Authorization: Bearer <token>
```

The account owner or an admin can delete it only if its current balance is zero. Success returns `204 No Content`; a nonzero balance returns `409 Conflict`. Deleting an account also removes its transaction history.

## Deposits and Withdrawals

### Deposit money

```http
POST http://localhost:8080/api/accounts/671a9c2e8f4b2c1d9a123456/deposit
Content-Type: application/json
```

```json
{
  "amount": 500.00
}
```

### Withdraw money

```http
POST http://localhost:8080/api/accounts/671a9c2e8f4b2c1d9a123456/withdraw
Content-Type: application/json
```

```json
{
  "amount": 200.00
}
```

Both operations return the updated account:

```json
{
  "id": "671a9c2e8f4b2c1d9a123456",
  "userId": "671a9b8a8f4b2c1d9a123400",
  "accountType": "SAVINGS",
  "balance": 300.00
}
```

## Transfers

Transfers use both account IDs in the request body:

```http
POST http://localhost:8080/api/accounts/transfer
Content-Type: application/json
```

```json
{
  "fromAccountId": "671a9c2e8f4b2c1d9a123456",
  "toAccountId": "671a9c2e8f4b2c1d9a123457",
  "amount": 100.00
}
```

A transfer:

1. Loads both accounts.
2. Verifies that the accounts are different.
3. Verifies that both accounts belong to the same customer.
4. Verifies that the source has enough money.
5. Decreases the source balance.
6. Increases the target balance.
7. Adds a transfer record to both account histories.

## Transaction History

```http
GET http://localhost:8080/api/accounts/671a9c2e8f4b2c1d9a123456/transactions
```

Example response:

```json
[
  {
    "id": "671a9d2e8f4b2c1d9a123456",
    "accountId": "671a9c2e8f4b2c1d9a123456",
    "type": "DEPOSIT",
    "amount": 500.00,
    "balanceAfter": 500.00,
    "relatedAccountId": null,
    "createdAt": "2026-09-29T15:30:00Z"
  },
  {
    "id": "671a9d2e8f4b2c1d9a123457",
    "accountId": "671a9c2e8f4b2c1d9a123456",
    "type": "TRANSFER",
    "amount": 100.00,
    "balanceAfter": 400.00,
    "relatedAccountId": "671a9c2e8f4b2c1d9a123457",
    "createdAt": "2026-09-29T15:35:00Z"
  }
]
```

Transaction types currently include:

| Type | Meaning |
| --- | --- |
| `DEPOSIT` | Money added to an account |
| `WITHDRAWAL` | Money removed from an account |
| `TRANSFER` | Money moved between two accounts |

`relatedAccountId` is `null` for deposits and withdrawals. For transfers, it identifies the other account involved.

## Business Rules

- Customer names are required and cannot be blank.
- Account creation requires an existing customer.
- Account IDs are generated automatically.
- Money amounts must be at least `0.01`.
- Withdrawals cannot exceed the current balance.
- Transfers must use two different accounts.
- Transfers are allowed only between accounts belonging to the same customer.
- Each successful deposit, withdrawal, and transfer creates transaction history.
- MongoDB data persists across application restarts when `MONGODB_URI` is configured; in-memory test fixtures reset between runs.

## Project Structure

```text
src/
├── main/
│   ├── java/com/example/bankapp/
│   │   ├── controllers/
│   │   │   ├── AccountController.java
│   │   │   └── CustomerController.java
│   │   ├── models/
│   │   │   ├── Account.java
│   │   │   ├── AccountRequest.java
│   │   │   ├── AccountTransaction.java
│   │   │   ├── AccountType.java
│   │   │   ├── Customer.java
│   │   │   ├── CustomerRequest.java
│   │   │   ├── MoneyRequest.java
│   │   │   ├── TransactionType.java
│   │   │   └── TransferRequest.java
│   │   ├── repos/
│   │   │   ├── AccountRepository.java
│   │   │   ├── AccountTransactionRepository.java
│   │   │   ├── CustomerRepository.java
│   │   │   ├── MongoAccountRepository.java
│   │   │   ├── MongoAccountTransactionRepository.java
│   │   │   ├── MongoCustomerRepository.java
│   │   │   └── in-memory test implementations
│   │   ├── services/
│   │   │   ├── AccountService.java
│   │   │   └── CustomerService.java
│   │   └── BankappApplication.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/example/bankapp/
```

The application follows a layered MVC design:

```text
Controller -> Service -> Repository -> MongoDB Atlas
```

The repository interfaces provide the boundary where database-backed implementations can be introduced later.

## Postman Quick Start

The repository includes a ready-to-import collection at [postman/william-rowley-simple-bank-app.postman_collection.json](postman/william-rowley-simple-bank-app.postman_collection.json).

In Postman, select **Import**, choose that JSON file, and open the **Simple Bank App API** collection. The collection only needs the running API; no admin credentials are required for these requests.

Run the **entire collection in order** with the Collection Runner. The first registration generates new usernames for that run, and subsequent requests reuse them; no database reset is needed. The scripts save the customer tokens and account IDs for dependent requests. Do not run individual requests out of order without first running their prerequisites. Both registered customers and their accounts remain in the database after each run. Each run uses fresh usernames, so the collection can run again against the same database.

The collection checks duplicate-username rejection, regular login, account creation/reads/deletion, balances and transaction history, self-only account listings, unauthorized cross-customer account access, and customer profile edits with password verification. Account operations use the owning customer's token. Admin-only customer management and account-list-by-customer requests are not included in this run.

For `POST` and `PUT` requests, use:

- Body: `raw`
- Format: `JSON`
- Header: `Content-Type: application/json`

The collection applies the saved JWT as a bearer token to protected requests automatically.

## Current Limitations and Next Steps

- In-memory repositories remain available for unit tests; the running application uses MongoDB through the configured URI.
- MongoDB Atlas is configured through `MONGODB_URI`; set that variable before using persistent storage.
- There is no frontend yet.
- Transfer operations should use a database transaction once persistence is added.
- The API currently has no centralized error-response format.
- Account ownership checks should be tightened so authenticated users can only access their own resources.
- The JWT secret is development-only and must be supplied securely before deployment.

## Git Workflow

After making and testing changes:

```powershell
git status
git add .
git commit -m "Describe the change"
git push
```
