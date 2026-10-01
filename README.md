# Simple Bank App API

A Spring Boot REST API for the [React frontend](../bankapp-react-frontend/README.md), managing customers, accounts, balances, transfers, and transaction history.

The running application uses MongoDB via `MongoTemplate`; in-memory repositories and mocks are used in tests. The frontend and backend are separate repositories and run in separate terminals.

## Current Features

| Area | Supported operations |
| --- | --- |
| Customers | Register, view or edit your profile; admin-only customer management |
| Accounts | Open, list, view, and delete zero-balance accounts |
| Money movement | Deposit and withdraw money |
| Transfers | Transfer money between two accounts owned by the same customer |
| History | View deposit, withdrawal, and transfer records for an account |
| Authentication | Customer/admin login, BCrypt password hashes, and JWT bearer tokens |
| Authorization | Owners access their accounts; admins can manage customers' accounts |
| Validation | Positive money amounts, required fields, ownership, sufficient funds, and zero-balance deletion |

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
- A reachable MongoDB deployment (the current configuration targets Atlas)
- No global Maven installation is required

Check Java from PowerShell:

```powershell
java --version
```

## Run Locally

The backend needs MongoDB. In Atlas, create a database user, allow your development IP under Network Access, and obtain a Java driver connection URI. On Windows PowerShell, set the connection and a signing key in **the backend terminal** before starting the API:

```powershell
$env:SPRING_MONGODB_URI = "<your MongoDB connection URI>"
$env:APP_JWT_SECRET = "<your long random signing key>"
```

Spring Boot uses these environment variables to override `spring.mongodb.uri` and `app.jwt.secret`. Use a signing key of at least 32 bytes. The current tracked `application.properties` contains literal development values, **not** `MONGODB_URI` substitution or a localhost fallback. Rotate any exposed database credential and signing key, remove secrets from tracked configuration before deployment, and do not export or commit your environment variables. Changing the signing key invalidates previously issued JWTs.

From the backend project root on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS or Linux, set `SPRING_MONGODB_URI` and `APP_JWT_SECRET` in that shell and run:

```bash
./mvnw spring-boot:run
```

The API listens at `http://localhost:8080`. The root URL has no frontend page; run the [React app](../bankapp-react-frontend/README.md#run-locally) separately, typically at `http://localhost:5173`.

### Configure the admin login

Set these variables in the backend terminal before starting the application. The app creates or updates the configured admin customer at startup and stores the password as a BCrypt hash. The configured username must not already belong to a regular customer.

```powershell
$env:ADMIN_USERNAME = "bank-admin"
$env:ADMIN_PASSWORD = "<choose a strong password>"
.\mvnw.cmd spring-boot:run
```

If both variables are absent, no admin account is created or updated. Providing only one causes startup to fail. Never commit the password or add it to frontend code.

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

| Method | Endpoint | Access | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | Public | Register a regular customer |
| `POST` | `/api/auth/login` | Public | Log in and receive a JWT |
| `POST` | `/api/auth/admin/login` | Public (admin credentials required) | Log in as an admin |
| `GET` | `/api/customers/me` | Customer | Get your profile |
| `PUT` | `/api/customers/me` | Customer | Change your name, username, or password |
| `GET` | `/api/customers` | Admin | List non-admin customers |
| `GET` | `/api/customers/{id}` | Admin | Get a customer |
| `POST` | `/api/customers` | Admin | Create a name-only customer record |
| `PUT` | `/api/customers/{id}` | Admin | Edit a customer's name |
| `DELETE` | `/api/customers/{id}` | Admin | Delete a customer and their accounts |
| `POST` | `/api/accounts` | Owner or admin | Open a Savings or Checking account |
| `GET` | `/api/accounts/me` | Authenticated | List accounts belonging to the signed-in user |
| `GET` | `/api/accounts?userId={id}` | Admin | List a customer's accounts |
| `GET` | `/api/accounts/{id}` | Owner or admin | Get account details |
| `DELETE` | `/api/accounts/{id}` | Owner or admin | Delete a zero-balance account |
| `POST` | `/api/accounts/{id}/deposit` | Owner or admin | Deposit funds |
| `POST` | `/api/accounts/{id}/withdraw` | Owner or admin | Withdraw funds |
| `POST` | `/api/accounts/transfer` | Source owner or admin | Transfer between two accounts of the same customer |
| `GET` | `/api/accounts/{id}/transactions` | Owner or admin | Get account transaction history |

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
  "customerId": "671a9b8a8f4b2c1d9a123400",
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
Authorization: Bearer <admin token>
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
  "name": "Ada Lovelace",
  "username": null
}
```

Expected status: `201 Created`

This admin-only endpoint creates a name-only record; it does not give the customer login credentials. Use `/api/auth/register` to create a customer who can sign in.

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

Customer-by-ID routes require an admin token; customers use `/api/customers/me` for their own profile.

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
