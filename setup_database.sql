
CREATE DATABASE IF NOT EXISTS AuditChain;
USE AuditChain;

DROP TRIGGER IF EXISTS after_account_update;
DROP TRIGGER IF EXISTS after_account_insert;
DROP TRIGGER IF EXISTS after_account_delete;
DROP TRIGGER IF EXISTS trg_accounts_insert;
DROP TRIGGER IF EXISTS trg_accounts_update;
DROP TRIGGER IF EXISTS trg_accounts_delete;

DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS alerts;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL
);

CREATE TABLE accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(255),
    holder_name VARCHAR(255),
    balance DOUBLE,
    updated_by VARCHAR(255)
);

CREATE TABLE audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(255),
    operation VARCHAR(255),
    record_id BIGINT,
    changed_by VARCHAR(255),
    changed_at DATETIME,
    prev_hash VARCHAR(64),
    row_hash VARCHAR(64),
    old_data JSON,
    new_data JSON
);

CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_account VARCHAR(255),
    to_account VARCHAR(255),
    amount DOUBLE,
    timestamp DATETIME
);

CREATE TABLE alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_message TEXT,
    status VARCHAR(50),
    visible_to VARCHAR(50)
);

DELIMITER $$

-- A. INSERT TRIGGER (Genesis & Initial Balances)
CREATE TRIGGER after_account_insert
AFTER INSERT ON accounts
FOR EACH ROW
BEGIN
    DECLARE last_hash VARCHAR(64) DEFAULT '0';
    DECLARE new_hash VARCHAR(64);
    DECLARE data_to_hash TEXT;
    DECLARE formatted_date VARCHAR(20);

    SELECT row_hash INTO last_hash FROM audit_log ORDER BY id DESC LIMIT 1;
    SET last_hash = COALESCE(last_hash, '0');
    SET formatted_date = DATE_FORMAT(NOW(), '%Y-%m-%dT%H:%i:%s');

    -- Formula: tableName + operation + recordId + oldData + newData + changedBy + changedAt + prevHash
    -- oldData is empty string for INSERT
    SET data_to_hash = CONCAT(
        'account',
        'INSERT',
        NEW.id,
        '', 
        JSON_OBJECT('id', NEW.id, 'accountNumber', NEW.account_number, 'balance', NEW.balance),
        COALESCE(NEW.updated_by, 'SYSTEM_REGISTRATION'),
        formatted_date,
        last_hash
    );
    SET new_hash = SHA2(data_to_hash, 256);

    INSERT INTO audit_log (table_name, operation, record_id, changed_by, changed_at, old_data, new_data, prev_hash, row_hash)
    VALUES ('account', 'INSERT', NEW.id, COALESCE(NEW.updated_by, 'SYSTEM_REGISTRATION'), NOW(), NULL, 
            JSON_OBJECT('id', NEW.id, 'accountNumber', NEW.account_number, 'balance', NEW.balance), last_hash, new_hash);
END$$

CREATE TRIGGER after_account_update
AFTER UPDATE ON accounts
FOR EACH ROW
BEGIN
    DECLARE last_hash VARCHAR(64) DEFAULT '0';
    DECLARE new_hash VARCHAR(64);
    DECLARE data_to_hash TEXT;
    DECLARE formatted_date VARCHAR(20);

    IF OLD.balance != NEW.balance OR OLD.account_number != NEW.account_number THEN
        
        SELECT row_hash INTO last_hash FROM audit_log ORDER BY id DESC LIMIT 1;
        SET last_hash = COALESCE(last_hash, '0');
        SET formatted_date = DATE_FORMAT(NOW(), '%Y-%m-%dT%H:%i:%s');

        SET data_to_hash = CONCAT(
            'account',
            'UPDATE',
            NEW.id,
            JSON_OBJECT('id', OLD.id, 'accountNumber', OLD.account_number, 'balance', OLD.balance),
            JSON_OBJECT('id', NEW.id, 'accountNumber', NEW.account_number, 'balance', NEW.balance),
            COALESCE(NEW.updated_by, 'UNKNOWN'),
            formatted_date,
            last_hash
        );
        SET new_hash = SHA2(data_to_hash, 256);

        INSERT INTO audit_log (table_name, operation, record_id, changed_by, changed_at, old_data, new_data, prev_hash, row_hash)
        VALUES ('account', 'UPDATE', NEW.id, COALESCE(NEW.updated_by, 'UNKNOWN'), NOW(), 
                JSON_OBJECT('id', OLD.id, 'accountNumber', OLD.account_number, 'balance', OLD.balance),
                JSON_OBJECT('id', NEW.id, 'accountNumber', NEW.account_number, 'balance', NEW.balance), 
                last_hash, new_hash);
    END IF;
END$$

CREATE TRIGGER after_account_delete
AFTER DELETE ON accounts
FOR EACH ROW
BEGIN
    DECLARE last_hash VARCHAR(64) DEFAULT '0';
    DECLARE new_hash VARCHAR(64);
    DECLARE data_to_hash TEXT;
    DECLARE formatted_date VARCHAR(20);

    SELECT row_hash INTO last_hash FROM audit_log ORDER BY id DESC LIMIT 1;
    SET last_hash = COALESCE(last_hash, '0');
    SET formatted_date = DATE_FORMAT(NOW(), '%Y-%m-%dT%H:%i:%s');

    SET data_to_hash = CONCAT(
        'account',
        'DELETE',
        OLD.id,
        JSON_OBJECT('id', OLD.id, 'accountNumber', OLD.account_number, 'balance', OLD.balance),
        '',
        'SYSTEM_ADMIN',
        formatted_date,
        last_hash
    );
    SET new_hash = SHA2(data_to_hash, 256);

    INSERT INTO audit_log (table_name, operation, record_id, changed_by, changed_at, old_data, new_data, prev_hash, row_hash)
    VALUES ('account', 'DELETE', OLD.id, 'SYSTEM_ADMIN', NOW(), 
            JSON_OBJECT('id', OLD.id, 'accountNumber', OLD.account_number, 'balance', OLD.balance),
            NULL, last_hash, new_hash);
END$$

DELIMITER ;
