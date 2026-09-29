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
| Validation | Positive money amounts, required names, valid IDs, and sufficient funds |

## Technology Stack

| Technology | Version / role |
| --- | --- |
| Java | 17 |
| Spring Boot | 4.1.1 |
| Spring Web MVC | REST controllers and HTTP endpoints |
| Spring Validation | Request validation |
| Maven Wrapper | Project builds without a global Maven installation |
| Storage | In-memory repositories |

## Prerequisites

- JDK 17 or newer
- Git
- No database is required yet
- No global Maven installation is required

Check Java from PowerShell:

```powershell
java --version
```

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

There is currently no frontend or root page. Opening `http://localhost:8080/` may show Spring Boot's Whitelabel 404 page. Use one of the API routes below instead.

## Run Tests

Windows:

```powershell
.\mvnw.cmd test
```

macOS or Linux:

```bash
./mvnw test
```

The tests cover the customer lifecycle, multiple accounts per customer, deposits, withdrawals, transfers, balances, and transaction history.

## API Overview

All endpoints return JSON unless otherwise noted.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/customers` | Get all customers |
| `GET` | `/api/customers/{id}` | Get one customer |
| `POST` | `/api/customers` | Create a customer |
| `PUT` | `/api/customers/{id}` | Edit a customer's name |
| `DELETE` | `/api/customers/{id}` | Delete a customer |
| `POST` | `/api/accounts` | Create an account |
| `GET` | `/api/accounts/{id}` | Get account details |
| `POST` | `/api/accounts/{id}/deposit` | Deposit money |
| `POST` | `/api/accounts/{id}/withdraw` | Withdraw money |
| `POST` | `/api/accounts/transfer` | Transfer money between accounts |
| `GET` | `/api/accounts/{id}/transactions` | Get account transaction history |

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
  "id": 1,
  "name": "Ada Lovelace"
}
```

Expected status: `201 Created`

### Get all customers

```http
GET http://localhost:8080/api/customers
```

### Get one customer

```http
GET http://localhost:8080/api/customers/1
```

### Edit a customer

```http
PUT http://localhost:8080/api/customers/1
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
DELETE http://localhost:8080/api/customers/1
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
  "userId": 1,
  "accountType": "SAVINGS"
}
```

Supported account types:

- `SAVINGS`
- `CHECKING`

Example response:

```json
{
  "id": 1,
  "userId": 1,
  "accountType": "SAVINGS",
  "balance": 0
}
```

Expected status: `201 Created`

### Get account details

```http
GET http://localhost:8080/api/accounts/1
```

Example response:

```json
{
  "id": 1,
  "userId": 1,
  "accountType": "SAVINGS",
  "balance": 500.00
}
```

## Deposits and Withdrawals

### Deposit money

```http
POST http://localhost:8080/api/accounts/1/deposit
Content-Type: application/json
```

```json
{
  "amount": 500.00
}
```

### Withdraw money

```http
POST http://localhost:8080/api/accounts/1/withdraw
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
  "id": 1,
  "userId": 1,
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
  "fromAccountId": 1,
  "toAccountId": 2,
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
GET http://localhost:8080/api/accounts/1/transactions
```

Example response:

```json
[
  {
    "id": 1,
    "accountId": 1,
    "type": "DEPOSIT",
    "amount": 500.00,
    "balanceAfter": 500.00,
    "relatedAccountId": null,
    "createdAt": "2026-09-29T15:30:00Z"
  },
  {
    "id": 2,
    "accountId": 1,
    "type": "TRANSFER",
    "amount": 100.00,
    "balanceAfter": 400.00,
    "relatedAccountId": 2,
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
- Data is lost when the application restarts because storage is currently in memory.

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
│   │   │   └── in-memory implementations
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
Controller -> Service -> Repository -> In-memory storage
```

The repository interfaces provide the boundary where database-backed implementations can be introduced later.

## Postman Quick Start

1. Start the API with `./mvnw spring-boot:run` or `.\mvnw.cmd spring-boot:run`.
2. Create a customer and note its ID.
3. Create two accounts using that customer ID.
4. Deposit money into the first account.
5. Transfer money to the second account.
6. Check both account balances.
7. Check transaction history for both accounts.

For `POST` and `PUT` requests, use:

- Body: `raw`
- Format: `JSON`
- Header: `Content-Type: application/json`

No authentication header is required yet.

## Current Limitations and Next Steps

- Storage is in memory; a database has not been connected.
- Authentication and login are not implemented.
- There is no frontend yet.
- Transfer operations should use a database transaction once persistence is added.
- The API currently has no centralized error-response format.
- Account and customer authorization rules will need to be added when authentication is introduced.

## Git Workflow

After making and testing changes:

```powershell
git status
git add .
git commit -m "Describe the change"
git push
```
