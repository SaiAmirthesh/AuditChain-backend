use auditchain;

-- 1. Get User Financial Metrics
-- Provides: Category-wise spending, Income vs. Expense, Monthly growth
drop procedure if exists get_user_analytics;

delimiter //
create procedure get_user_analytics(in p_account_no varchar(255))
begin
    -- 1. Category-wise Spending (Debit only)
    select 
        category, 
        sum(amount) as total_amount,
        count(*) as transaction_count
    from transactions
    where from_account = p_account_no
    group by category;

    -- 2. Total Income vs Total Expense
    select 
        sum(case when to_account = p_account_no then amount else 0 end) as total_income,
        sum(case when from_account = p_account_no then amount else 0 end) as total_expense
    from transactions
    where from_account = p_account_no or to_account = p_account_no;
end //
delimiter ;

-- 2. Get Admin System Metrics
-- Provides: Global volume, status distribution, top channels
drop procedure if exists get_admin_analytics;

delimiter //
create procedure get_admin_analytics()
begin
    -- 1. System-wide Volume & Counts
    select 
        count(*) as total_count,
        sum(amount) as total_volume,
        avg(amount) as average_tx_value
    from transactions;

    -- 2. Status Distribution
    select 
        status, 
        count(*) as count
    from transactions
    group by status;

    -- 3. Channel Usage
    select 
        channel, 
        count(*) as count
    from transactions
    group by channel;
end //
delimiter ;

-- 3. Detect Anomalies (Auditor Tool)
-- Logic: High Value (> 1 Lakh), Night Activity (1-4 AM), Rapid Bursts (< 60s)
drop procedure if exists detect_anomalies;

delimiter //
create procedure detect_anomalies()
begin
    -- Reset flags first (optional, usually better to append)
    -- update transactions set flagged = false, flag_reason = null;

    -- 1. Flag High Value (> 1,00,000 INR)
    update transactions 
    set flagged = true, 
        flag_reason = concat(ifnull(flag_reason, ''), '| HIGH_VALUE'),
        risk_score = risk_score + 30
    where amount > 100000;

    -- 2. Flag Night Activity (1 AM - 4 AM)
    update transactions 
    set flagged = true, 
        flag_reason = concat(ifnull(flag_reason, ''), '| NIGHT_ACTIVITY'),
        risk_score = risk_score + 20
    where hour(timestamp) between 1 and 4;

    -- 3. Flag Rapid Bursts (Multiple txs from same account within 60 seconds)
    update transactions t
    join (
        select t1.id 
        from transactions t1
        join transactions t2 on t1.from_account = t2.from_account 
            and t1.id != t2.id
            and abs(timestampdiff(second, t1.timestamp, t2.timestamp)) < 60
    ) as burst on t.id = burst.id
    set t.flagged = true, 
        t.flag_reason = concat(ifnull(t.flag_reason, ''), '| RAPID_BURST'),
        t.risk_score = t.risk_score + 40;
end //
delimiter ;

-- 4. Update Account Risk Scores
-- Logic: Recalculate account-level risk based on historical transaction risk
drop procedure if exists update_account_risk_scores;

delimiter //
create procedure update_account_risk_scores()
begin
    update accounts a
    set a.risk_score = (
        select ifnull(avg(t.risk_score), 0)
        from transactions t
        where t.from_account = a.account_number
    );
end //
delimiter ;

select 'SUCCESS: Analytics procedures created for User, Admin, and Auditor.' as status;
