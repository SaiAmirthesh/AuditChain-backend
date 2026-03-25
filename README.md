# AuditChain Backend

AuditChain is a cryptographically secure banking backend designed for absolute transaction integrity. It implements a tamper-evident audit ledger using SHA-256 hash-chaining at the database layer, combined with AI-powered forensic auditing.

## Core Features

- **SHA-256 Hash Chaining**: Every transaction (Insert/Update/Delete) is cryptographically linked to the previous state via MySQL triggers, creating an immutable ledger.
- **Tamper Detection**: An internal verification engine that performs recursive chain sweeps to detect unauthorized database modifications.
- **AI Forensic Integration**: Leverages **Google Gemini 2.5-flash** to analyze audit breaches and provide human-readable security summaries.
- **Role-Based Security**: Secure JWT authentication with distinct permissions for `USER`, `ADMIN`, and `AUDITOR`.
- **Genesis Block Handling**: Robust support for system initialization and genesis hash verification.

## Tech Stack

- **Framework**: Spring Boot 3+
- **Database**: MySQL 8.0 (utilizing JSON functions and AFTER triggers)
- **Security**: Spring Security + JJWT
- **AI Engine**: Gemini AI API
- **ORM**: Spring Data JPA / Hibernate

## Prerequisites

- Java 17 or higher
- MySQL 8.0+
- Maven

## Setup & Installation

1. **Database Setup**:
   - Create a database named `AuditChain`.
   - Execute the SQL scripts in `setup_database.sql` to initialize tables and install the unified cryptographic triggers.

2. **Configuration**:
   - Create a file named `src/main/resources/application-local.properties` (this file is gitignored).
   - Add your local credentials:
     ```properties
     spring.datasource.password=YOUR_DB_PASSWORD
     jwt.secret=YOUR_LONG_RANDOM_SECRET_KEY
     gemini.api.key=YOUR_GEMINI_API_KEY
     ```

3. **Build & Run**:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

## Security Architecture

The system's integrity is enforced by **Database Triggers**. Any change to the `accounts` table automatically generates a new hash in the `audit_log` table:
`row_hash = SHA2(CONCAT(tableName, operation, recordId, oldData, newData, changedBy, changedAt, prevHash), 256)`

If any manual modification is made to the database bypassing the application layer, the chain breaks, and the **Auditor Verification Service** will flag the exact record that was tampered with.

---
© 2026 AuditChain Labs | Secured by Mathematics.
