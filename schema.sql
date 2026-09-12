-- 1. 建 merchant 表
CREATE TABLE IF NOT EXISTS merchant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT "商家名称",
    phone VARCHAR(20),
    address VARCHAR(255),
    status INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. 加 merchant_id 字段
SET @exist1 := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = "sky_take_out" AND TABLE_NAME = "employee" AND COLUMN_NAME = "merchant_id");
SET @sql1 := IF(@exist1 = 0, "ALTER TABLE employee ADD COLUMN merchant_id BIGINT DEFAULT NULL", "SELECT 1");
PREPARE stmt1 FROM @sql1;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

SET @exist2 := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = "sky_take_out" AND TABLE_NAME = "dish" AND COLUMN_NAME = "merchant_id");
SET @sql2 := IF(@exist2 = 0, "ALTER TABLE dish ADD COLUMN merchant_id BIGINT DEFAULT NULL", "SELECT 1");
PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

SET @exist3 := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = "sky_take_out" AND TABLE_NAME = "orders" AND COLUMN_NAME = "merchant_id");
SET @sql3 := IF(@exist3 = 0, "ALTER TABLE orders ADD COLUMN merchant_id BIGINT DEFAULT NULL", "SELECT 1");
PREPARE stmt3 FROM @sql3;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

-- 3. 插入两个商家
INSERT IGNORE INTO merchant (id, name) VALUES (1, "蜀味轩"), (2, "湘菜人家");

-- 4. 更新 employee 的 merchant_id
UPDATE employee SET merchant_id = 1 WHERE id IN (1, 2) AND (merchant_id IS NULL OR merchant_id = 0);

-- 5. 分配菜品给商家：蜀味轩 = 鱼鲜+酒水
UPDATE dish SET merchant_id = 1 WHERE id IN (51,52,53,58,61,62,63,64,65,66,67,73,46,47,48) AND (merchant_id IS NULL OR merchant_id = 0);
-- 湘菜人家 = 家常+主食+汤
UPDATE dish SET merchant_id = 2 WHERE id IN (49,50,54,55,56,57,59,60,68,69,70,71,72,74,75) AND (merchant_id IS NULL OR merchant_id = 0);

-- 验证
SELECT id, name FROM merchant;
SELECT id, name, username, merchant_id FROM employee;
SELECT merchant_id, COUNT(*) as dish_count FROM dish WHERE merchant_id IS NOT NULL GROUP BY merchant_id;
SELECT COUNT(*) as no_merchant_dishes FROM dish WHERE merchant_id IS NULL;
