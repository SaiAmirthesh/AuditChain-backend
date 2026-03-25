# AuditChain Architecture Diagram

```mermaid
graph TD
    Client((Client/Postman))

    subgraph Spring Boot Application
        Security[Spring Security & JWT Filter]
        
        subgraph Controllers
            AC[AuthController]
            UC[UserController]
            AdminC[AdminController]
            AuditorC[AuditorController]
        end

        subgraph Services
            AccS[AccountService]
            AudVS[AuditVerificationService]
            GemS[GeminiService]
            AlertS[AlertService]
        end

        subgraph Data Access Layer
            AccR[(AccountRepo)]
            TxR[(TransactionRepo)]
            AudR[(AuditRepo)]
            AlertR[(AlertRepo)]
            UserR[(UserRepository)]
        end
    end

    subgraph Database MySQL
        DB_Users[(Users Table)]
        DB_Accounts[(Accounts Table)]
        DB_Tx[(Transactions Table)]
        DB_Audit[(AuditLog Table)]
        DB_Alerts[(Alerts Table)]
        Trigger{MySQL Trigger\n'after_account_update'}
    end

    subgraph External
        Gemini[Google Gemini API]
    end

    %% Client flows
    Client -->|REST Requests| Security
    Security -->|Routing| AC
    Security -->|Routing| UC
    Security -->|Routing| AdminC
    Security -->|Routing| AuditorC

    %% Controller to Service
    UC --> AccS
    AuditorC --> AudVS
    AuditorC --> AlertS
    AuditorC --> GemS

    %% Service to Repository
    AC --> UserR
    AccS --> AccR
    AccS --> TxR
    AudVS --> AudR
    AudVS --> AlertR
    AlertS --> AlertR

    %% Repository to DB
    UserR --> DB_Users
    AccR --> DB_Accounts
    TxR --> DB_Tx
    AudR --> DB_Audit
    AlertR --> DB_Alerts

    %% DB Internal Flow
    DB_Accounts -.->|Fires on Update| Trigger
    Trigger -.->|Generates Hash Chain| DB_Audit

    %% External Flow
    GemS -->|HTTP POST JSON| Gemini
```

## Data Flow (Tamper-Evident Transaction)
1. **Initiate Transfer**: `UserController` -> `AccountService` 
2. **Account Updates**: `AccountService` saves updated sender/receiver balances. 
3. **Database Trigger**: MySQL `after_account_update` catches the row change.
4. **Hash Chain Generation**: The trigger strings together the `record_id`, `new_balance`, and `last_row_hash`.
5. **Secure Log Created**: Trigger inserts the compiled string hash and old/new JSON data into `AuditLog`.
6. **Verification Request**: Auditor queries `AuditorController`.
7. **Hash Verification Check**: `AuditVerificationService` rebuilds hashes from scratch and compares against stored DB values.
8. **AI Alerting**: If verification fails, an `Alert` is saved. The `GeminiService` grabs the alerts and sends them to the Gemini API for plain English translation.
