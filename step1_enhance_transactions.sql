-- ============================================================
-- AuditChain Analytics Layer - Step 1
-- Enhanced Transactions Table Schema
-- Run this file in MySQL Workbench
-- ============================================================

use auditchain;

-- ============================================================
-- 1. add analytics columns to transactions table
-- ============================================================
-- using alter table to preserve existing data and audit chain

alter table transactions
    add column transaction_type varchar(50) default 'transfer' after amount,
    add column status varchar(30) default 'completed' after transaction_type,
    add column description varchar(500) null after status,
    add column category varchar(100) default 'general' after description,
    add column channel varchar(50) default 'web' after category,
    add column ip_address varchar(45) null after channel,
    add column device_info varchar(255) null after ip_address,
    add column location varchar(255) null after device_info,
    add column risk_score decimal(5,2) default 0.00 after location,
    add column flagged boolean default false after risk_score,
    add column flag_reason varchar(500) null after flagged;

-- ============================================================
-- 2. performance indexes for analytics queries
-- ============================================================

-- single column indexes
create index idx_txn_from_account on transactions(from_account);
create index idx_txn_to_account on transactions(to_account);
create index idx_txn_timestamp on transactions(timestamp);
create index idx_txn_amount on transactions(amount);
create index idx_txn_status on transactions(status);
create index idx_txn_type on transactions(transaction_type);
create index idx_txn_flagged on transactions(flagged);
create index idx_txn_risk_score on transactions(risk_score);
create index idx_txn_category on transactions(category);
create index idx_txn_channel on transactions(channel);

-- composite indexes for common analytics queries
create index idx_txn_from_time on transactions(from_account, timestamp);
create index idx_txn_to_time on transactions(to_account, timestamp);
create index idx_txn_amount_time on transactions(amount, timestamp);
create index idx_txn_status_time on transactions(status, timestamp);
create index idx_txn_flagged_risk on transactions(flagged, risk_score);

-- ============================================================
-- 3. verify the changes
-- ============================================================

describe transactions;

select 'Step 1 Complete: Transactions table enhanced successfully!' as result;
