-- ============================================================
-- AuditChain Analytics Layer - Step 2
-- Synthetic Data Generator (Accounts + 10,000 Transactions)
-- Run this file in MySQL Workbench
-- ============================================================

use auditchain;

-- set session variables to allow mass inserts and procedural logic
set sql_safe_updates = 0;

-- ============================================================
-- 1. clean up existing data (optional, but ensures fresh start for 50 accounts)
-- ============================================================
-- delete from transactions;
-- delete from accounts;
-- delete from users where role = 'USER';

-- ============================================================
-- 2. Create Procedure to Generate Accounts
-- ============================================================

delimiter //

create procedure generate_accounts()
begin
    declare i int default 1;
    declare acc_no varchar(50);
    declare h_name varchar(100);
    
    -- Insert specific accounts: sai and ajay
    if not exists (select 1 from users where username = 'sai') then
        insert into users (username, password, role) values ('sai', '$2a$10$CwTycUXWue0Thq9StjUM0uEnsnW92YWh8zE69fI.L5gC0aVqLax6W', 'user'); -- password: password
        insert into accounts (account_number, holder_name, balance, updated_by) values ('sai', 'Sai Kumar', 500000.00, 'system');
    end if;
    
    if not exists (select 1 from users where username = 'ajay') then
        insert into users (username, password, role) values ('ajay', '$2a$10$CwTycUXWue0Thq9StjUM0uEnsnW92YWh8zE69fI.L5gC0aVqLax6W', 'user'); -- password: password
        insert into accounts (account_number, holder_name, balance, updated_by) values ('ajay', 'Ajay Sharma', 500000.00, 'system');
    end if;

    -- Generate remaining 48 accounts
    set i = 3;
    while i <= 50 do
        set acc_no = concat('ACC', 1000 + i);
        set h_name = concat('User ', i);
        
        if not exists (select 1 from users where username = acc_no) then
            insert into users (username, password, role) values (acc_no, '$2a$10$CwTycUXWue0Thq9StjUM0uEnsnW92YWh8zE69fI.L5gC0aVqLax6W', 'user');
            insert into accounts (account_number, holder_name, balance, updated_by) values (acc_no, h_name, floor(10000 + rand() * 100000), 'system');
        end if;
        
        set i = i + 1;
    end while;
end //

delimiter ;

call generate_accounts();
drop procedure generate_accounts;

-- ============================================================
-- 3. Create Procedure to Generate 10,000 Transactions with Anomalies
-- ============================================================

delimiter //

create procedure generate_synthetic_transactions(in total_txns int)
begin
    declare i int default 1;
    declare from_acc varchar(255);
    declare to_acc varchar(255);
    declare amt double;
    declare txn_time datetime;
    declare txn_type varchar(50);
    declare cat varchar(100);
    declare chn varchar(50);
    declare loc varchar(255);
    declare risk decimal(5,2);
    declare is_flagged boolean;
    declare reason varchar(500);
    declare rand_val float;

    while i <= total_txns do
        -- 1. Selection of accounts
        select account_number into from_acc from accounts order by rand() limit 1;
        select account_number into to_acc from accounts where account_number != from_acc order by rand() limit 1;
        
        -- 2. Base transaction properties
        set amt = round(10 + rand() * 5000, 2);
        set txn_time = date_sub(now(), interval floor(rand() * 30 * 24 * 60) minute); -- last 30 days
        set txn_type = 'transfer';
        set cat = elt(1 + floor(rand() * 5), 'shopping', 'utilities', 'salary', 'entertainment', 'investment');
        set chn = elt(1 + floor(rand() * 3), 'web', 'mobile', 'api');
        set loc = elt(1 + floor(rand() * 4), 'New York', 'London', 'Mumbai', 'Singapore');
        set risk = 0.0;
        set is_flagged = false;
        set reason = null;

        -- 3. Injected Anomalies Logic
        set rand_val = rand();

        -- A. Large Transaction Anomaly (0.5%)
        if rand_val < 0.005 then
            set amt = round(60000 + rand() * 150000, 2);
            set risk = 75.0;
            set is_flagged = true;
            set reason = 'High value transaction exceeding threshold';
        
        -- B. Night Transaction Anomaly (1%)
        elseif rand_val < 0.015 then
            -- Set time between 1 AM and 4 AM
            set txn_time = concat(date(txn_time), ' 02:', floor(rand()*59), ':', floor(rand()*59));
            set risk = 40.0;
            set reason = 'Unusual activity during non-business hours';

        -- C. Sudden Spike / Rapid Activity (0.5% - reuse same account)
        elseif rand_val < 0.020 then
            set txn_time = now(); -- Set many to now
            set risk = 60.0;
            set reason = 'Rapid transaction burst detected';
        
        -- D. Potential Circular Transfer (0.5% - simulated by fixed high-risk pair)
        elseif rand_val < 0.025 then
            set cat = 'circular';
            set risk = 90.0;
            set is_flagged = true;
            set reason = 'Suspicious circular flow pattern';
        end if;

        -- 4. Insert Transaction
        insert into transactions (from_account, to_account, amount, transaction_type, status, description, category, channel, ip_address, device_info, location, risk_score, flagged, flag_reason, timestamp)
        values (
            from_acc, to_acc, amt, txn_type, 'completed', 
            concat('Txn #', i), cat, chn, 
            concat('192.168.1.', floor(rand()*254)), 
            'Device-ID-XYZ', loc, risk, is_flagged, reason, txn_time
        );

        set i = i + 1;
    end while;
end //

delimiter ;

-- Execute the generator for 10,000 transactions
call generate_synthetic_transactions(10000);

-- cleanup procedure
drop procedure generate_synthetic_transactions;

select 'Step 2 Complete: 50 Accounts and 10,000 Transactions generated with anomalies!' as result;
