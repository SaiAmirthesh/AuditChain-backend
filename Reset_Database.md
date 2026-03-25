# AuditChain Database Reset Guide

This guide contains the SQL queries necessary to completely wipe all data from the database and reset all auto-increment IDs. This is especially useful when you want to clear out old test data, false-positive alerts, or broken audit chains before presenting a clean demo.

## Instructions
1. Open your MySQL client (e.g., MySQL Workbench, DBeaver, or the MySQL command line).
2. Connect to the `AuditChain` database.
3. Copy and paste the entire SQL script below and execute it.

## The Reset SQL Script

```sql
USE AuditChain;

-- Disable foreign key checks temporarily to allow truncating tables with relationships
SET FOREIGN_KEY_CHECKS = 0;

-- Truncate all tables to remove all rows and reset auto-increment counters back to 1
TRUNCATE TABLE alerts;
TRUNCATE TABLE audit_log;
TRUNCATE TABLE transactions;
TRUNCATE TABLE accounts;
TRUNCATE TABLE users;

-- Re-enable foreign key checks to maintain database integrity
SET FOREIGN_KEY_CHECKS = 1;

-- (Optional) Verify the tables are empty
SELECT 'users' AS table_name, COUNT(*) AS row_count FROM users
UNION ALL
SELECT 'accounts', COUNT(*) FROM accounts
UNION ALL
SELECT 'transactions', COUNT(*) FROM transactions
UNION ALL
SELECT 'audit_log', COUNT(*) FROM audit_log
UNION ALL
SELECT 'alerts', COUNT(*) FROM alerts;
```

### What this does:
* **`SET FOREIGN_KEY_CHECKS = 0`**: Prevents MySQL from throwing errors if tables reference each other.
* **`TRUNCATE TABLE`**: Completely empties the table much faster than `DELETE FROM` and resets the `id` column back to `1`.
* **Zeroes out the Audit Log**: This is crucial because a fresh test run requires the `last_hash` to start clean from `'0'`.
* **The `SELECT` statements at the end**: Will output a small table confirming that every single table now has `0` rows.

After running this, you can follow the **Registration** and **Account Creation** steps in your API Testing Guide to start a completely fresh hash chain!
