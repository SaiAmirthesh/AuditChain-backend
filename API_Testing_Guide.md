# AuditChain API Testing Guide

This guide will walk you through how to properly test the AuditChain System using Postman or cURL.

## 1. Setup Database & Start Server
1. Start your Spring Boot application. It will auto-create the tables via Hibernate (`spring.jpa.hibernate.ddl-auto=update`).
2. Open your MySQL client (e.g. MySQL Workbench) and run the `setup_database.sql` script to create the Hash-Chaining Audit Trigger.

## 2. Register Users
Register the three different roles required for testing. Send these as a `POST` request with a `raw` JSON body.

**Register User 1:**
```json
POST http://localhost:8081/auth/register

{
  "username": "alice",
  "password": "password123",
  "role": "user"
}
```

**Register Admin:**
```json
POST http://localhost:8081/auth/register

{
  "username": "admin",
  "password": "adminpass",
  "role": "admin"
}
```

**Register Auditor:**
```json
POST http://localhost:8081/auth/register

{
  "username": "auditor",
  "password": "auditpass",
  "role": "auditor"
}
```

## 3. Create Sample Accounts
Because there's no endpoint for creating accounts directly in the current scope, you should insert two accounts manually into the DB to test transfers:
```sql
INSERT INTO accounts (account_number, holder_name, balance, updated_by) VALUES ('alice', 'Alice', 1000.00, 'system');
INSERT INTO accounts (account_number, holder_name, balance, updated_by) VALUES ('bob', 'Bob', 500.00, 'system');
```

## 4. Run a Transaction (As User)
First, Login as Alice to get the JWT Token. Send as a `raw` JSON body:
```json
POST http://localhost:8081/auth/login

{
  "username": "alice",
  "password": "password123"
}
```
*Copy the `token` from the JSON response.*

Now, perform a money transfer. Send as a `raw` JSON body:
```json
POST http://localhost:8081/user/transfer
Headers: 
Authorization: Bearer <ALICE_TOKEN>

{
  "fromAccount": "alice",
  "toAccount": "bob",
  "amount": 100
}
```
*Note: This will trigger the MySQL trigger to hash and chain the audit log changes!*

## 5. Verify Hash Chain (As Auditor)
Login as Auditor:
```
POST http://localhost:8081/auth/login?username=auditor&password=auditpass
```
*Copy the `token` from the JSON response.*

Run Verification:
```
GET http://localhost:8081/auditor/verify
Headers: 
Authorization: Bearer <AUDITOR_TOKEN>
```
*Expected output:* A list of arrays stating "row X intact" and ending with "CHAIN INTACT: All rows verified successfully".

## 6. Simulate Tampering
Manually go into your MySQL database and modify an `audit_log` row to simulate a hacker bypassing the application layer:
```sql
UPDATE audit_log SET new_data = '{"id": 1, "balance": 99999.0}' WHERE id = 1;
```

## 7. Detect Tampering
Call the verification endpoint again as Auditor:
```
GET http://localhost:8081/auditor/verify
Headers: 
Authorization: Bearer <AUDITOR_TOKEN>
```
*Expected output:* It will immediately catch the mismatch and output "ROW MODIFIED". 

## 8. Generate AI Summary
Because tampering was detected, an alert was automatically saved to the `alerts` table. Using the Gemini API integration, you can summarize this for a human auditor:
```
GET http://localhost:8081/auditor/ai-summary
Headers: 
Authorization: Bearer <AUDITOR_TOKEN>
```
*Expected Output:* A clear, plain English assessment from Gemini AI detailing the suspicious modifications.
