-- ============================================================
-- AuditChain Fix: User Passwords and Roles
-- Run this in MySQL Workbench to fix the invalid login issue
-- ============================================================

use auditchain;

set sql_safe_updates = 0;

-- 1. Update all users to have a verified BCrypt hash for the password "password"
-- New hash: $2a$10$CwTycUXWue0Thq9StjUM0uEnsnW92YWh8zE69fI.L5gC0aVqLax6W
update users 
set password = '$2a$10$CwTycUXWue0Thq9StjUM0uEnsnW92YWh8zE69fI.L5gC0aVqLax6W'
where password is not null;

-- 2. Convert roles to lowercase to match SecurityConfig .hasAuthority() checks
update users set role = 'user' where role = 'USER';
update users set role = 'admin' where role = 'ADMIN';
update users set role = 'auditor' where role = 'AUDITOR';

select 'SUCCESS: All passwords reset to "password" and roles normalized to lowercase.' as message;
