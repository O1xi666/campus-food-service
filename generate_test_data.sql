-- 生成测试订单和订单明细数据
-- 为 sky-take-out 系统生成 60 笔订单（过去30天）

-- 1. 先看看当前状态
SELECT COUNT(*) as before_count FROM orders;

-- 2. 删除已有的测试数据（如果有的话）
-- DELETE FROM order_detail WHERE order_id IN (SELECT id FROM orders);
-- DELETE FROM orders;
