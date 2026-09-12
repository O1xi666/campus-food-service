DELETE FROM order_detail WHERE order_id >= 100;
DELETE FROM orders WHERE id >= 100;

-- 订单
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (101, '202606052800000101', 5, 4, 8, '2026-05-28 21:50:15', '2026-05-28 22:04:15', 1, 1, 66.00, 1, '13700137003', '北京市顺义区空港工业区', '用户4', '王五', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (102, '202606051900000102', 5, 5, 5, '2026-05-19 11:13:32', '2026-05-19 11:24:32', 1, 1, 154.00, 1, '15800158001', '北京市顺义区空港工业区', '用户5', '吴十', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (103, '202606052900000103', 6, 4, 5, '2026-05-29 19:36:18', '2026-05-29 19:39:18', 1, 2, 404.00, 1, '18600186001', '北京市石景山区万达广场', '用户4', '钱七', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (104, '202606060900000104', 6, 5, 7, '2026-06-09 19:02:12', '2026-06-09 19:08:12', 2, 2, 200.00, 1, '13900139002', '北京市大兴区亦庄经济开发区', '用户5', '吴十', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (105, '202606052500000105', 1, 6, 5, '2026-05-25 20:58:53', '2026-05-25 21:04:53', 2, 0, 88.00, 1, '13800138001', '北京市朝阳区建国路88号', '用户6', '李四', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (106, '202606061000000106', 5, 5, 10, '2026-06-10 20:53:36', '2026-06-10 20:57:36', 2, 1, 380.00, 1, '13300133001', '北京市朝阳区建国路88号', '用户5', '周九', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (107, '202606052800000107', 2, 5, 6, '2026-05-28 10:13:47', '2026-05-28 10:15:47', 2, 1, 264.00, 1, '13800138001', '北京市朝阳区建国路88号', '用户5', '孙八', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (108, '202606061000000108', 2, 4, 8, '2026-06-10 16:05:49', '2026-06-10 16:09:49', 1, 1, 6.00, 1, '13600136004', '北京市昌平区回龙观西大街', '用户4', '吴十', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (109, '202606060400000109', 5, 4, 2, '2026-06-04 14:25:42', '2026-06-04 14:29:42', 2, 1, 256.00, 1, '18700187001', '北京市石景山区万达广场', '用户4', '郑十一', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (110, '202606053000000110', 5, 5, 8, '2026-05-30 09:28:08', '2026-05-30 09:38:08', 2, 1, 66.00, 1, '13600136004', '北京市昌平区回龙观西大街', '用户5', '周九', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (111, '202606052800000111', 5, 4, 4, '2026-05-28 15:17:53', '2026-05-28 15:25:53', 2, 1, 154.00, 1, '15800158001', '北京市朝阳区建国路88号', '用户4', '吴十', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (112, '202606060700000112', 5, 5, 10, '2026-06-07 19:56:49', '2026-06-07 19:59:49', 2, 1, 12.00, 1, '15800158001', '北京市丰台区丽泽路10号', '用户5', '张三', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (113, '202606052300000113', 2, 6, 2, '2026-05-23 12:12:03', '2026-05-23 12:21:03', 1, 1, 68.00, 1, '18700187001', '北京市丰台区丽泽路10号', '用户6', '钱七', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (114, '202606060300000114', 5, 6, 8, '2026-06-03 19:12:06', '2026-06-03 19:14:06', 2, 1, 446.00, 1, '15900159002', '北京市丰台区丽泽路10号', '用户6', '张三', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (115, '202606052600000115', 6, 6, 2, '2026-05-26 10:22:01', '2026-05-26 10:27:01', 1, 2, 150.00, 1, '13900139002', '北京市石景山区万达广场', '用户6', '孙八', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (116, '202606060600000116', 6, 5, 2, '2026-06-06 09:36:37', '2026-06-06 09:49:37', 1, 2, 144.00, 1, '18600186001', '北京市大兴区亦庄经济开发区', '用户5', '赵六', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (117, '202606053000000117', 6, 4, 6, '2026-05-30 11:16:44', '2026-05-30 11:28:44', 1, 2, 78.00, 1, '18700187001', '北京市顺义区空港工业区', '用户4', '张三', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (118, '202606061100000118', 5, 5, 9, '2026-06-11 20:09:27', '2026-06-11 20:22:27', 1, 1, 332.00, 1, '13300133001', '北京市石景山区万达广场', '用户5', '陈十二', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (119, '202606060700000119', 6, 5, 4, '2026-06-07 11:30:16', '2026-06-07 11:39:16', 1, 2, 286.00, 1, '18700187001', '北京市顺义区空港工业区', '用户5', '郑十一', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (120, '202606052100000120', 5, 6, 7, '2026-05-21 19:42:52', '2026-05-21 19:51:52', 1, 1, 294.00, 1, '13300133001', '北京市东城区王府井大街201号', '用户6', '周九', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (121, '202606051500000121', 1, 6, 8, '2026-05-15 18:37:24', '2026-05-15 18:52:24', 1, 0, 112.00, 1, '13700137003', '北京市石景山区万达广场', '用户6', '李四', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (122, '202606060600000122', 5, 4, 6, '2026-06-06 18:50:47', '2026-06-06 19:03:47', 1, 1, 440.00, 1, '15900159002', '北京市东城区王府井大街201号', '用户4', '钱七', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (123, '202606061100000123', 5, 5, 5, '2026-06-11 19:13:22', '2026-06-11 19:22:22', 2, 1, 392.00, 1, '13300133001', '北京市朝阳区建国路88号', '用户5', '周九', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (124, '202606051700000124', 5, 6, 6, '2026-05-17 19:40:19', '2026-05-17 19:44:19', 1, 1, 128.00, 1, '13900139002', '北京市大兴区亦庄经济开发区', '用户6', '钱七', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (125, '202606060200000125', 5, 4, 1, '2026-06-02 20:54:25', '2026-06-02 20:59:25', 2, 1, 226.00, 1, '15800158001', '北京市大兴区亦庄经济开发区', '用户4', '孙八', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (126, '202606060900000126', 2, 6, 8, '2026-06-09 11:10:52', '2026-06-09 11:12:52', 1, 1, 132.00, 1, '13300133001', '北京市石景山区万达广场', '用户6', '王五', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (127, '202606052000000127', 5, 5, 4, '2026-05-20 18:19:43', '2026-05-20 18:32:43', 2, 1, 68.00, 1, '18600186001', '北京市东城区王府井大街201号', '用户5', '李四', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (128, '202606061200000128', 5, 4, 5, '2026-06-12 18:09:01', '2026-06-12 18:12:01', 2, 1, 204.00, 1, '18600186001', '北京市西城区金融街15号', '用户4', '李四', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (129, '202606051500000129', 5, 6, 1, '2026-05-15 10:33:53', '2026-05-15 10:36:53', 1, 1, 238.00, 1, '13600136004', '北京市顺义区空港工业区', '用户6', '王五', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (130, '202606052100000130', 5, 4, 1, '2026-05-21 16:30:30', '2026-05-21 16:43:30', 2, 1, 140.00, 1, '13800138001', '北京市大兴区亦庄经济开发区', '用户4', '吴十', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (131, '202606052500000131', 5, 6, 8, '2026-05-25 21:11:27', '2026-05-25 21:23:27', 2, 1, 70.00, 2, '13300133001', '北京市海淀区中关村大街1号', '用户6', '周九', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (132, '202606060600000132', 5, 5, 2, '2026-06-06 18:35:21', '2026-06-06 18:45:21', 2, 1, 90.00, 2, '15900159002', '北京市通州区万达广场', '用户5', '陈十二', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (133, '202606053100000133', 2, 4, 4, '2026-05-31 09:37:46', '2026-05-31 09:44:46', 2, 1, 66.00, 2, '13800138001', '北京市石景山区万达广场', '用户4', '陈十二', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (134, '202606053100000134', 6, 4, 2, '2026-05-31 20:19:11', '2026-05-31 20:25:11', 2, 2, 62.00, 2, '15800158001', '北京市西城区金融街15号', '用户4', '吴十', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (135, '202606052500000135', 5, 5, 5, '2026-05-25 11:05:32', '2026-05-25 11:13:32', 1, 1, 82.00, 2, '13900139002', '北京市丰台区丽泽路10号', '用户5', '吴十', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (136, '202606051500000136', 5, 6, 1, '2026-05-15 18:26:44', '2026-05-15 18:28:44', 2, 1, 140.00, 2, '15800158001', '北京市东城区王府井大街201号', '用户6', '王五', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (137, '202606052100000137', 5, 6, 2, '2026-05-21 15:39:42', '2026-05-21 15:42:42', 1, 1, 14.00, 2, '18700187001', '北京市石景山区万达广场', '用户6', '王五', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (138, '202606053100000138', 6, 6, 10, '2026-05-31 11:12:26', '2026-05-31 11:14:26', 2, 2, 86.00, 2, '18600186001', '北京市石景山区万达广场', '用户6', '吴十', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (139, '202606051900000139', 5, 4, 8, '2026-05-19 16:11:27', '2026-05-19 16:18:27', 2, 1, 61.00, 2, '18600186001', '北京市顺义区空港工业区', '用户4', '郑十一', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (140, '202606052500000140', 5, 4, 6, '2026-05-25 10:39:20', '2026-05-25 10:45:20', 1, 1, 81.00, 2, '13300133001', '北京市大兴区亦庄经济开发区', '用户4', '孙八', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (141, '202606052400000141', 5, 6, 4, '2026-05-24 21:26:44', '2026-05-24 21:29:44', 2, 1, 62.00, 2, '15800158001', '北京市东城区王府井大街201号', '用户6', '郑十一', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (142, '202606052400000142', 5, 4, 9, '2026-05-24 21:41:36', '2026-05-24 21:48:36', 1, 1, 32.00, 2, '13300133001', '北京市顺义区空港工业区', '用户4', '张三', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (143, '202606061300000143', 2, 5, 5, '2026-06-13 17:57:24', '2026-06-13 18:09:24', 2, 1, 39.00, 2, '13300133001', '北京市丰台区丽泽路10号', '用户5', '钱七', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (144, '202606060300000144', 5, 6, 3, '2026-06-03 14:38:16', '2026-06-03 14:47:16', 2, 1, 93.00, 2, '13300133001', '北京市西城区金融街15号', '用户6', '陈十二', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (145, '202606060200000145', 5, 5, 6, '2026-06-02 11:43:56', '2026-06-02 11:56:56', 1, 1, 342.00, 2, '15900159002', '北京市昌平区回龙观西大街', '用户5', '陈十二', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (146, '202606051700000146', 5, 6, 10, '2026-05-17 18:42:40', '2026-05-17 18:54:40', 2, 1, 4.00, 2, '15800158001', '北京市丰台区丽泽路10号', '用户6', '吴十', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (147, '202606060200000147', 5, 4, 9, '2026-06-02 15:56:30', '2026-06-02 15:59:30', 2, 1, 102.00, 2, '13800138001', '北京市西城区金融街15号', '用户4', '李四', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (148, '202606051700000148', 5, 4, 1, '2026-05-17 14:37:10', '2026-05-17 14:46:10', 1, 1, 70.00, 2, '13800138001', '北京市顺义区空港工业区', '用户4', '钱七', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (149, '202606052400000149', 5, 4, 1, '2026-05-24 10:51:13', '2026-05-24 10:57:13', 1, 1, 70.00, 2, '15900159002', '北京市海淀区中关村大街1号', '用户4', '吴十', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (150, '202606052300000150', 5, 6, 9, '2026-05-23 20:07:01', '2026-05-23 20:12:01', 1, 1, 15.00, 2, '13700137003', '北京市昌平区回龙观西大街', '用户6', '李四', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (151, '202606052800000151', 5, 6, 3, '2026-05-28 10:18:35', '2026-05-28 10:32:35', 1, 1, 110.00, 2, '15900159002', '北京市通州区万达广场', '用户6', '钱七', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (152, '202606053000000152', 6, 6, 9, '2026-05-30 20:50:57', '2026-05-30 20:53:57', 2, 2, 56.00, 2, '13900139002', '北京市东城区王府井大街201号', '用户6', '孙八', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (153, '202606052600000153', 5, 5, 1, '2026-05-26 12:26:03', '2026-05-26 12:28:03', 2, 1, 4.00, 2, '13500135005', '北京市丰台区丽泽路10号', '用户5', '周九', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (154, '202606052900000154', 5, 6, 6, '2026-05-29 10:09:03', '2026-05-29 10:24:03', 1, 1, 40.00, 2, '15800158001', '北京市海淀区中关村大街1号', '用户6', '吴十', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (155, '202606051800000155', 5, 5, 1, '2026-05-18 09:17:48', '2026-05-18 09:19:48', 1, 1, 84.00, 2, '13600136004', '北京市西城区金融街15号', '用户5', '钱七', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (156, '202606052300000156', 5, 6, 9, '2026-05-23 13:33:07', '2026-05-23 13:40:07', 1, 1, 72.00, 2, '15800158001', '北京市东城区王府井大街201号', '用户6', '钱七', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (157, '202606060800000157', 5, 5, 10, '2026-06-08 10:19:09', '2026-06-08 10:27:09', 1, 1, 4.00, 2, '15900159002', '北京市石景山区万达广场', '用户5', '郑十一', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (158, '202606052400000158', 2, 6, 2, '2026-05-24 14:43:44', '2026-05-24 14:57:44', 1, 1, 2.00, 2, '15800158001', '北京市顺义区空港工业区', '用户6', '郑十一', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (159, '202606051800000159', 5, 5, 10, '2026-05-18 20:05:49', '2026-05-18 20:13:49', 1, 1, 54.00, 2, '13600136004', '北京市丰台区丽泽路10号', '用户5', '周九', 1, 1);
INSERT INTO orders (id, number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, merchant_id, phone, address, user_name, consignee, delivery_status, tableware_status)
VALUES (160, '202606060600000160', 5, 5, 10, '2026-06-06 16:19:22', '2026-06-06 16:22:22', 2, 1, 66.00, 2, '13500135005', '北京市通州区万达广场', '用户5', '张三', 1, 1);

-- 明细
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (501, '剁椒鱼头', 101, 61, 1, 66.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (502, '香锅牛蛙', 102, 63, 1, 88.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (503, '经典酸菜鮰鱼', 102, 52, 1, 66.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (504, '清蒸鲈鱼', 103, 58, 2, 196.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (505, '水煮鱼', 103, 73, 2, 136.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (506, '鮰鱼2斤', 103, 67, 1, 72.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (507, '北冰洋', 104, 47, 1, 4.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (508, '清蒸鲈鱼', 104, 58, 2, 196.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (509, '香锅牛蛙', 105, 63, 1, 88.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (510, '水煮鱼', 106, 73, 2, 136.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (511, '草鱼2斤', 106, 65, 1, 68.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (512, '馋嘴牛蛙', 106, 64, 2, 176.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (513, '清蒸鲈鱼', 107, 58, 2, 196.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (514, '老坛酸菜鱼', 107, 51, 1, 56.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (515, '王老吉', 107, 46, 2, 12.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (516, '王老吉', 108, 46, 1, 6.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (517, '王老吉', 109, 46, 2, 12.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (518, '草鱼2斤', 109, 65, 1, 68.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (519, '金汤酸菜牛蛙', 109, 62, 2, 176.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (520, '经典酸菜鮰鱼', 110, 52, 1, 66.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (521, '老坛酸菜鱼', 111, 51, 1, 56.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (522, '北冰洋', 111, 47, 1, 4.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (523, '王老吉', 111, 46, 1, 6.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (524, '馋嘴牛蛙', 111, 64, 1, 88.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (525, '王老吉', 112, 46, 2, 12.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (526, '水煮鱼', 113, 73, 1, 68.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (527, '馋嘴牛蛙', 114, 64, 2, 176.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (528, '草鱼2斤', 114, 65, 1, 68.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (529, '水煮鱼', 114, 73, 2, 136.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (530, '经典酸菜鮰鱼', 114, 52, 1, 66.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (531, '老坛酸菜鱼', 115, 51, 2, 112.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (532, '蜀味水煮草鱼', 115, 53, 1, 38.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (533, '水煮鱼', 116, 73, 2, 136.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (534, '北冰洋', 116, 47, 2, 8.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (535, '水煮鱼', 117, 73, 1, 68.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (536, '北冰洋', 117, 47, 1, 4.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (537, '王老吉', 117, 46, 1, 6.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (538, '老坛酸菜鱼', 118, 51, 2, 112.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (539, '剁椒鱼头', 118, 61, 2, 132.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (540, '金汤酸菜牛蛙', 118, 62, 1, 88.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (541, '草鱼2斤', 119, 65, 2, 136.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (542, '北冰洋', 119, 47, 2, 8.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (543, '水煮鱼', 119, 73, 2, 136.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (544, '王老吉', 119, 46, 1, 6.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (545, '老坛酸菜鱼', 120, 51, 1, 56.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (546, '江团鱼2斤', 120, 66, 2, 238.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (547, '老坛酸菜鱼', 121, 51, 2, 112.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (548, '清蒸鲈鱼', 122, 58, 2, 196.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (549, '剁椒鱼头', 122, 61, 2, 132.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (550, '老坛酸菜鱼', 122, 51, 2, 112.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (551, '老坛酸菜鱼', 123, 51, 1, 56.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (552, '水煮鱼', 123, 73, 2, 136.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (553, '草鱼2斤', 123, 65, 1, 68.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (554, '经典酸菜鮰鱼', 123, 52, 2, 132.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (555, '雪花啤酒', 124, 48, 1, 4.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (556, '水煮鱼', 124, 73, 1, 68.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (557, '老坛酸菜鱼', 124, 51, 1, 56.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (558, '水煮鱼', 125, 73, 2, 136.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (559, '王老吉', 125, 46, 1, 6.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (560, '蜀味水煮草鱼', 125, 53, 2, 76.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (561, '雪花啤酒', 125, 48, 2, 8.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (562, '经典酸菜鮰鱼', 126, 52, 2, 132.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (563, '水煮鱼', 127, 73, 1, 68.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (564, '雪花啤酒', 128, 48, 1, 4.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (565, '香锅牛蛙', 128, 63, 1, 88.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (566, '老坛酸菜鱼', 128, 51, 2, 112.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (567, '江团鱼2斤', 129, 66, 2, 238.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (568, '水煮鱼', 130, 73, 2, 136.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (569, '北冰洋', 130, 47, 1, 4.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (570, '红烧肉', 131, 75, 2, 70.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (571, '番茄鸡蛋盖饭', 132, 74, 1, 22.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (572, '宫保鸡丁', 132, 70, 2, 56.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (573, '平菇豆腐汤', 132, 69, 2, 12.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (574, '番茄鸡蛋盖饭', 133, 74, 2, 44.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (575, '麻婆豆腐', 133, 71, 1, 18.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (576, '鸡蛋汤', 133, 68, 1, 4.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (577, '炝炒圆白菜', 134, 57, 2, 36.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (578, '米饭', 134, 49, 2, 4.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (579, '番茄鸡蛋盖饭', 134, 74, 1, 22.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (580, '宫保鸡丁', 135, 70, 1, 28.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (581, '蒜蓉娃娃菜', 135, 55, 1, 18.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (582, '炝炒圆白菜', 135, 57, 2, 36.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (583, '清炒时蔬', 136, 72, 2, 30.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (584, '清炒小油菜', 136, 54, 2, 36.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (585, '米饭', 136, 49, 2, 4.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (586, '红烧肉', 136, 75, 2, 70.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (587, '平菇豆腐汤', 137, 69, 2, 12.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (588, '馒头', 137, 50, 2, 2.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (589, '梅菜扣肉', 138, 60, 1, 58.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (590, '宫保鸡丁', 138, 70, 1, 28.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (591, '番茄鸡蛋盖饭', 139, 74, 1, 22.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (592, '米饭', 139, 49, 1, 2.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (593, '清炒小油菜', 139, 54, 2, 36.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (594, '馒头', 139, 50, 1, 1.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (595, '麻婆豆腐', 140, 71, 2, 36.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (596, '番茄鸡蛋盖饭', 140, 74, 1, 22.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (597, '清炒时蔬', 140, 72, 1, 15.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (598, '鸡蛋汤', 140, 68, 2, 8.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (599, '清炒西兰花', 141, 56, 1, 18.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (600, '番茄鸡蛋盖饭', 141, 74, 2, 44.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (601, '清炒时蔬', 142, 72, 2, 30.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (602, '米饭', 142, 49, 1, 2.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (603, '红烧肉', 143, 75, 1, 35.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (604, '米饭', 143, 49, 2, 4.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (605, '番茄鸡蛋盖饭', 144, 74, 1, 22.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (606, '清炒西兰花', 144, 56, 1, 18.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (607, '清炒小油菜', 144, 54, 1, 18.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (608, '红烧肉', 144, 75, 1, 35.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (609, '清炒西兰花', 145, 56, 1, 18.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (610, '清炒时蔬', 145, 72, 2, 30.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (611, '麻婆豆腐', 145, 71, 1, 18.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (612, '东坡肘子', 145, 59, 2, 276.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (613, '米饭', 146, 49, 2, 4.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (614, '清炒西兰花', 147, 56, 2, 36.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (615, '梅菜扣肉', 147, 60, 1, 58.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (616, '鸡蛋汤', 147, 68, 2, 8.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (617, '红烧肉', 148, 75, 2, 70.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (618, '红烧肉', 149, 75, 2, 70.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (619, '清炒时蔬', 150, 72, 1, 15.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (620, '清炒西兰花', 151, 56, 2, 36.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (621, '红烧肉', 151, 75, 2, 70.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (622, '米饭', 151, 49, 2, 4.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (623, '蒜蓉娃娃菜', 152, 55, 1, 18.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (624, '馒头', 152, 50, 2, 2.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (625, '麻婆豆腐', 152, 71, 2, 36.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (626, '米饭', 153, 49, 2, 4.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (627, '麻婆豆腐', 154, 71, 2, 36.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (628, '米饭', 154, 49, 2, 4.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (629, '清炒西兰花', 155, 56, 1, 18.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (630, '米饭', 155, 49, 1, 2.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (631, '炝炒圆白菜', 155, 57, 2, 36.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (632, '宫保鸡丁', 155, 70, 1, 28.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (633, '清炒西兰花', 156, 56, 1, 18.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (634, '蒜蓉娃娃菜', 156, 55, 2, 36.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (635, '清炒小油菜', 156, 54, 1, 18.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (636, '米饭', 157, 49, 2, 4.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (637, '米饭', 158, 49, 1, 2.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (638, '蒜蓉娃娃菜', 159, 55, 1, 18.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (639, '清炒西兰花', 159, 56, 2, 36.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (640, '番茄鸡蛋盖饭', 160, 74, 2, 44.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (641, '米饭', 160, 49, 2, 4.00);
INSERT INTO order_detail (id, name, order_id, dish_id, number, amount)
VALUES (642, '麻婆豆腐', 160, 71, 1, 18.00);