-- =====================================================
-- APSAS Database Cleanup Script
-- Usage: Run this if you get deadlock or lock timeout errors
-- =====================================================

USE apsas_db;

-- 1. Show current locks (Disabled during init - use manually if needed)
-- This script is for manual cleanup only, not for automatic execution
-- SELECT 'Lock cleanup queries disabled during initialization' as status;

-- 2. Show long-running transactions (> 10 seconds) - Disabled during init
/*
SELECT 
    id,
    user,
    host,
    db,
    command,
    time,
    state,
    LEFT(info, 100) as query_preview
FROM information_schema.processlist
WHERE db = 'apsas_db'
  AND time > 10
  AND command != 'Sleep'
ORDER BY time DESC;
*/

-- 3. Kill long-running transactions (UNCOMMENT if needed)
-- WARNING: This will kill active connections!
/*
SELECT CONCAT('KILL ', id, ';') as kill_command
FROM information_schema.processlist
WHERE db = 'apsas_db'
  AND user = 'user'
  AND time > 30
  AND command != 'Sleep';
*/

-- 4. Check InnoDB lock status - Disabled during init
-- SHOW ENGINE INNODB STATUS\G

-- 5. Check table locks - Disabled during init
-- SHOW OPEN TABLES WHERE In_use > 0;

-- 6. Optimize tables if needed (OPTIONAL - run during maintenance only)
/*
OPTIMIZE TABLE roles;
OPTIMIZE TABLE permissions;
OPTIMIZE TABLE roles_permissions;
OPTIMIZE TABLE users;
OPTIMIZE TABLE users_roles;
*/

-- 7. Reset AUTO_INCREMENT if needed (OPTIONAL)
/*
ALTER TABLE roles AUTO_INCREMENT = 1;
ALTER TABLE permissions AUTO_INCREMENT = 1;
ALTER TABLE users AUTO_INCREMENT = 1;
*/

-- 8. Show current transaction isolation level - Disabled during init
-- SELECT @@GLOBAL.transaction_isolation, @@SESSION.transaction_isolation;

-- 9. Adjust lock wait timeout (default is 50 seconds)
-- SHOW VARIABLES LIKE 'innodb_lock_wait_timeout';
-- SET GLOBAL innodb_lock_wait_timeout = 120; -- Increase to 120 seconds if needed

SELECT 'Database cleanup script completed!' as status;
