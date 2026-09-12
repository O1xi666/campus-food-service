-- ============================================================
--  测试数据（可重复执行）
--
--    · 2 个商家 / 10 个分类 / 30 道菜品 / 24 条菜品口味
--    · 4 个 C 端用户，其中 test003 / 123456 与 test / 123456 可直接登录
--      （test001、test002 密码为空，仅用于关联历史订单）
--    · 2 个管理端员工，其中 admin / 123456 可直接登录
--    · 60 笔历史订单 + 142 条订单明细（AI 用户画像与经营分析的数据来源）
--    · 3 条默认收货地址（下单链路必需，submit 时会校验默认地址）
--
--  说明：开头先 DELETE 再 INSERT，所以可以重复执行；
--        所有主键都显式指定，保证与订单/明细中的引用一致。
-- ============================================================

USE sky_take_out;

SET NAMES utf8mb4;

-- 清理旧数据（先子表后主表）
DELETE FROM order_detail WHERE order_id >= 100;
DELETE FROM orders WHERE id >= 100;
DELETE FROM address_book WHERE id IN (2, 3, 4);
DELETE FROM dish_flavor;
DELETE FROM dish;
DELETE FROM category;
DELETE FROM merchant;
DELETE FROM `user`;
DELETE FROM employee;

-- ============================================================
-- 一、基础数据：商家 / 分类 / 菜品 / 口味 / 用户 / 员工
-- ============================================================
INSERT INTO `merchant` (`id`, `name`, `phone`, `address`, `status`, `create_time`, `update_time`) VALUES (1,'蜀味轩',NULL,NULL,1,'2026-06-13 21:15:34','2026-06-13 21:15:34');
INSERT INTO `merchant` (`id`, `name`, `phone`, `address`, `status`, `create_time`, `update_time`) VALUES (2,'湘菜人家',NULL,NULL,1,'2026-06-13 21:15:34','2026-06-13 21:15:34');

INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (11,1,'酒水饮料',10,1,'2022-06-09 22:09:18','2022-06-09 22:09:18',1,1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (12,1,'传统主食',9,1,'2022-06-09 22:09:32','2022-06-09 22:18:53',1,1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (13,2,'人气套餐',12,1,'2022-06-09 22:11:38','2022-06-10 11:04:40',1,1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (15,2,'商务套餐',13,1,'2022-06-09 22:14:10','2022-06-10 11:04:48',1,1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (16,1,'蜀味烤鱼',4,1,'2022-06-09 22:15:37','2022-08-31 14:27:25',1,1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (17,1,'蜀味牛蛙',5,1,'2022-06-09 22:16:14','2022-08-31 14:39:44',1,1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (18,1,'特色蒸菜',6,1,'2022-06-09 22:17:42','2022-06-09 22:17:42',1,1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (19,1,'新鲜时蔬',7,1,'2022-06-09 22:18:12','2022-06-09 22:18:28',1,1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (20,1,'水煮鱼',8,1,'2022-06-09 22:22:29','2022-06-09 22:23:45',1,1);
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES (21,1,'汤类',11,1,'2022-06-10 10:51:47','2022-06-10 10:51:47',1,1);

INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (46,'王老吉',11,6.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/41bfcacf-7ad4-4927-8b26-df366553a94c.png','',1,'2022-06-09 22:40:47','2022-06-09 22:40:47',1,1,100,1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (47,'北冰洋',11,4.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/4451d4be-89a2-4939-9c69-3a87151cb979.png','还是小时候的味道',1,'2022-06-10 09:18:49','2022-06-10 09:18:49',1,1,100,1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (48,'雪花啤酒',11,4.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/bf8cbfc1-04d2-40e8-9826-061ee41ab87c.png','',1,'2022-06-10 09:22:54','2022-06-10 09:22:54',1,1,100,1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (49,'米饭',12,2.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/76752350-2121-44d2-b477-10791c23a8ec.png','精选五常大米',1,'2022-06-10 09:30:17','2022-06-10 09:30:17',1,1,100,2);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (50,'馒头',12,1.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/475cc599-8661-4899-8f9e-121dd8ef7d02.png','优质面粉',1,'2022-06-10 09:34:28','2022-06-10 09:34:28',1,1,100,2);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (51,'老坛酸菜鱼',20,56.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/4a9cefba-6a74-467e-9fde-6e687ea725d7.png','原料：汤，草鱼，酸菜',1,'2022-06-10 09:40:51','2022-06-10 09:40:51',1,1,100,1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (52,'经典酸菜鮰鱼',20,66.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/5260ff39-986c-4a97-8850-2ec8c7583efc.png','原料：酸菜，江团，鮰鱼',1,'2022-06-10 09:46:02','2022-06-10 09:46:02',1,1,100,1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (53,'蜀味水煮草鱼',20,38.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/a6953d5a-4c18-4b30-9319-4926ee77261f.png','原料：草鱼，汤',1,'2022-06-10 09:48:37','2022-06-10 09:48:37',1,1,100,1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (54,'清炒小油菜',19,18.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/3613d38e-5614-41c2-90ed-ff175bf50716.png','原料：小油菜',1,'2022-06-10 09:51:46','2022-06-10 09:51:46',1,1,100,2);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (55,'蒜蓉娃娃菜',19,18.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/4879ed66-3860-4b28-ba14-306ac025fdec.png','原料：蒜，娃娃菜',1,'2022-06-10 09:53:37','2022-06-10 09:53:37',1,1,100,2);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (56,'清炒西兰花',19,18.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/e9ec4ba4-4b22-4fc8-9be0-4946e6aeb937.png','原料：西兰花',1,'2022-06-10 09:55:44','2022-06-10 09:55:44',1,1,100,2);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (57,'炝炒圆白菜',19,18.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/22f59feb-0d44-430e-a6cd-6a49f27453ca.png','原料：圆白菜',1,'2022-06-10 09:58:35','2022-06-10 09:58:35',1,1,100,2);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (58,'清蒸鲈鱼',18,98.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/c18b5c67-3b71-466c-a75a-e63c6449f21c.png','原料：鲈鱼',1,'2022-06-10 10:12:28','2022-06-10 10:12:28',1,1,100,1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (59,'东坡肘子',18,138.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/a80a4b8c-c93e-4f43-ac8a-856b0d5cc451.png','原料：猪肘棒',1,'2022-06-10 10:24:03','2022-06-10 10:24:03',1,1,100,2);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (60,'梅菜扣肉',18,58.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/6080b118-e30a-4577-aab4-45042e3f88be.png','原料：猪肉，梅菜',1,'2022-06-10 10:26:03','2022-06-10 10:26:03',1,1,100,2);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (61,'剁椒鱼头',18,66.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/13da832f-ef2c-484d-8370-5934a1045a06.png','原料：鲢鱼，剁椒',1,'2022-06-10 10:28:54','2022-06-10 10:28:54',1,1,100,1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (62,'金汤酸菜牛蛙',17,88.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/7694a5d8-7938-4e9d-8b9e-2075983a2e38.png','原料：鲜活牛蛙，酸菜',1,'2022-06-10 10:33:05','2022-06-10 10:33:05',1,1,100,1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (63,'香锅牛蛙',17,88.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/f5ac8455-4793-450c-97ba-173795c34626.png','配料：鲜活牛蛙，莲藕，青笋',1,'2022-06-10 10:35:40','2022-06-10 10:35:40',1,1,100,1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (64,'馋嘴牛蛙',17,88.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/7a55b845-1f2b-41fa-9486-76d187ee9ee1.png','配料：鲜活牛蛙，丝瓜，黄豆芽',1,'2022-06-10 10:37:52','2022-06-10 10:37:52',1,1,100,1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (65,'草鱼2斤',16,68.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/b544d3ba-a1ae-4d20-a860-81cb5dec9e03.png','原料：草鱼，黄豆芽，莲藕',1,'2022-06-10 10:41:08','2022-06-10 10:41:08',1,1,100,1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (66,'江团鱼2斤',16,119.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/a101a1e9-8f8b-47b2-afa4-1abd47ea0a87.png','配料：江团鱼，黄豆芽，莲藕',1,'2022-06-10 10:42:42','2022-06-10 10:42:42',1,1,100,1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (67,'鮰鱼2斤',16,72.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/8cfcc576-4b66-4a09-ac68-ad5b273c2590.png','原料：鮰鱼，黄豆芽，莲藕',1,'2022-06-10 10:43:56','2022-06-10 10:43:56',1,1,99,1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (68,'鸡蛋汤',21,4.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/c09a0ee8-9d19-428d-81b9-746221824113.png','配料：鸡蛋，紫菜',1,'2022-06-10 10:54:25','2022-06-10 10:54:25',1,1,100,2);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (69,'平菇豆腐汤',21,6.00,NULL,NULL,'https://sky-itcast.oss-cn-beijing.aliyuncs.com/16d0a3d6-2253-4cfc-9b49-bf7bd9eb2ad2.png','配料：豆腐，平菇',1,'2022-06-10 10:55:02','2022-06-10 10:55:02',1,1,100,2);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (70,'宫保鸡丁',1,28.00,'辣','热销',NULL,'经典川菜，鸡肉滑嫩',1,NULL,NULL,NULL,NULL,100,2);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (71,'麻婆豆腐',1,18.00,'辣','实惠',NULL,'麻辣鲜香，超级下饭',1,NULL,NULL,NULL,NULL,100,2);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (72,'清炒时蔬',1,15.00,'清淡','健康',NULL,'新鲜时蔬，低脂健康',1,NULL,NULL,NULL,NULL,100,2);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (73,'水煮鱼',1,68.00,'特辣','招牌',NULL,'鱼肉滑嫩，麻辣过瘾',1,NULL,NULL,NULL,NULL,100,1);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (74,'番茄鸡蛋盖饭',1,22.00,'微甜','家常',NULL,'酸甜可口，快速出餐',1,NULL,NULL,NULL,NULL,100,2);
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `flavor`, `category`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `stock`, `merchant_id`) VALUES (75,'红烧肉',1,35.00,'甜咸','经典',NULL,'肥而不腻，入口即化',1,NULL,NULL,NULL,NULL,100,2);

INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (40,10,'甜味','[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (41,7,'忌口','[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (42,7,'温度','[\"热饮\",\"常温\",\"去冰\",\"少冰\",\"多冰\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (45,6,'忌口','[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (46,6,'辣度','[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (47,5,'辣度','[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (48,5,'甜味','[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (49,2,'甜味','[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (50,4,'甜味','[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (51,3,'甜味','[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (52,3,'忌口','[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (86,52,'忌口','[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (87,52,'辣度','[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (88,51,'忌口','[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (89,51,'辣度','[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (92,53,'忌口','[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (93,53,'辣度','[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (94,54,'忌口','[\"不要葱\",\"不要蒜\",\"不要香菜\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (95,56,'忌口','[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (96,57,'忌口','[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (97,60,'忌口','[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (101,66,'辣度','[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (102,67,'辣度','[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]');
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES (103,65,'辣度','[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]');

INSERT INTO `user` (`id`, `openid`, `name`, `phone`, `sex`, `id_number`, `avatar`, `create_time`, `password`) VALUES (4,NULL,'test001',NULL,NULL,NULL,NULL,'2026-04-15 21:12:20',NULL);
INSERT INTO `user` (`id`, `openid`, `name`, `phone`, `sex`, `id_number`, `avatar`, `create_time`, `password`) VALUES (5,NULL,'test002',NULL,NULL,NULL,NULL,'2026-04-15 21:19:44',NULL);
INSERT INTO `user` (`id`, `openid`, `name`, `phone`, `sex`, `id_number`, `avatar`, `create_time`, `password`) VALUES (6,NULL,'test003',NULL,NULL,NULL,NULL,'2026-04-15 21:28:17','e10adc3949ba59abbe56e057f20f883e');
INSERT INTO `user` (`id`, `openid`, `name`, `phone`, `sex`, `id_number`, `avatar`, `create_time`, `password`) VALUES (7,NULL,'test',NULL,NULL,NULL,NULL,'2026-07-22 15:46:05','e10adc3949ba59abbe56e057f20f883e');

INSERT INTO `employee` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `merchant_id`) VALUES (1,'管理员','admin','e10adc3949ba59abbe56e057f20f883e','13812312312','1','110101199001010047',1,'2022-02-15 15:51:20','2022-02-17 09:16:20',10,1,1);
INSERT INTO `employee` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`, `create_user`, `update_user`, `merchant_id`) VALUES (2,'张三','zhangsan','e10adc3949ba59abbe56e057f20f883e','13812345678','男','110101199001011234',1,'2026-03-14 21:30:16','2026-03-14 21:30:16',NULL,NULL,1);

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
-- ============================================================
-- 收货地址：下单链路必需（/user/order/submit/cart 会校验默认地址）
-- ============================================================
DELETE FROM address_book WHERE id IN (2, 3, 4);
INSERT INTO address_book (id, user_id, consignee, phone, province_name, city_name, district_name, detail, is_default)
VALUES (2, 4, '测试同学A', '13800000004', '陕西省', '西安市', '长安区', '西安电子科技大学 竹园公寓 3 号楼', 1);
INSERT INTO address_book (id, user_id, consignee, phone, province_name, city_name, district_name, detail, is_default)
VALUES (3, 5, '测试同学B', '13800000005', '陕西省', '西安市', '长安区', '西安电子科技大学 海棠公寓 5 号楼', 1);
INSERT INTO address_book (id, user_id, consignee, phone, province_name, city_name, district_name, detail, is_default)
VALUES (4, 6, '测试同学C', '13800000006', '陕西省', '西安市', '长安区', '西安电子科技大学 丁香公寓 1 号楼', 1);
-- ============================================================
-- 三、统一初始库存，保证压测 / 演示的起点一致
-- ============================================================
UPDATE dish SET stock = 100;