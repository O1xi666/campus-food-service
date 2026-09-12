# 校园餐饮与 AI 推荐分析系统

基于一个开源外卖点餐课程项目二次开发、并完成高并发工程化改造的校园餐饮点餐与经营分析系统。

在保留原项目"管理端 + 用户端"外卖业务主干的基础上，我补齐了一条完整的**高并发读缓存链路**、一条**秒杀下单链路**、一条**AI 推荐与分析链路**，并把它们都做成了可复现、可压测、有实测数据支撑的工程实现。

---

## 一、我做了什么

原项目是一个功能完整但技术栈偏"教学"的外卖系统：直接查库、没有缓存、没有限流、没有异步。我在它的基础上做了以下改造：

| # | 能力 | 关键实现 | 代码位置 |
|---|------|----------|----------|
| 1 | 二级缓存（本地 + Redis） | Caffeine 作 L1、Redis 作 L2，`CompositeCacheManager` 组合，先查 L1 再查 L2 | `sky-server/src/main/java/com/sky/config/CacheConfig.java` |
| 2 | 缓存穿透防护 | 手写布隆过滤器（位数组 + 双重哈希，`m=-n·lnp/(ln2)²`），查询前先做"一定不存在"预校验 | `sky-server/src/main/java/com/sky/cache/BloomFilter.java`、`DishBloomFilter.java` |
| 3 | 缓存击穿防护 | 逻辑过期 + 互斥锁异步重建，热点 key 过期瞬间由单个线程回源，其余请求继续读旧值 | `sky-server/src/main/java/com/sky/cache/LogicalExpireCache.java` |
| 4 | 缓存雪崩防护 | 装饰 `RedisCacheWriter`，给每个 key 的 TTL 叠加随机抖动（默认 ±5 分钟） | `sky-server/src/main/java/com/sky/cache/JitterRedisCacheWriter.java` |
| 5 | 缓存与数据库一致性 | 先更新数据库、再删缓存（`@CacheEvict` + 手动淘汰逻辑过期 key） | `sky-server/src/main/java/com/sky/service/impl/DishServiceImpl.java` |
| 6 | 接口限流 | 自定义 `@RateLimit` 注解 + AOP + Redis ZSET 滑动窗口（Lua 原子执行），支持全局 / IP / 用户三维度 | `annotation/RateLimit.java`、`aspect/RateLimitAspect.java`、`resources/lua/sliding_window.lua` |
| 7 | 秒杀下单 | Redis 库存预扣减（Lua 原子执行，key 缺失时用 DB 库存惰性补建，上线前可由发布脚本预热） + RabbitMQ 异步落库 + 定时对账回补 | `service/impl/OrderServiceImpl.java`、`resources/lua/deduct_stock.lua`、`mq/OrderConsumer.java`、`task/StockReconciliationTask.java` |
| 8 | 统计接口全异步 | 线程池 + 任务表（状态机）+ 失败重试 + 幂等抢占 + WebSocket 站内通知 | `task/BusinessAnalysisTaskRunner.java`、`service/impl/NotificationService.java`、`websocket/NotificationWebSocketServer.java` |
| 9 | AI 三层推荐链路 | 用户画像召回 → 规则排序 → 大模型 Prompt 生成 | `service/impl/AIServiceImpl.java` |
| 10 | AI 经营分析 | 真实库表聚合（营业额 / 客单价 / 销量 Top-Bottom）后交给大模型生成日报 | `service/impl/DataAnalysisServiceImpl.java` |

> 说明：AI 部分我先接过阿里云百炼（DashScope），后因为额度问题切换到 **DeepSeek**（OpenAI 兼容协议）。
> `QwenUtil` 同时保留了 `openai` 与 `dashscope` 两种协议实现，切换只改配置，不动代码。

---

## 二、实测性能数据

### AI 推荐接口（`/user/ai/recommend`）：秒级 → 10ms 量级

这是缓存收益最直观的接口：缓存未命中时它要**真实调用一次大模型**（构建用户画像 → 拼 Prompt → 等模型生成），命中本地 Caffeine 后方法体根本不执行，直接返回缓存结果。

- **模型**：`deepseek-flash`（DeepSeek OpenAI 兼容接口）
- **对照组**：`sky.cache.enabled=false`（`NoOpCacheManager`，每次请求都真实打大模型）vs `sky.cache.enabled=true`
- **请求**：同一用户 + 同一 `preference`，即"纯热点场景"
- **缓存键**：`userId + ":" + preference + ":" + merchantId`，TTL 为 L1 5 分钟 / L2 30 分钟（带随机抖动）

| 指标 | 绕过缓存（真实调大模型） | 有缓存（命中 L1 Caffeine） | 变化 |
|------|------------------------|---------------------------|------|
| 样本数 | 6 | 12 | — |
| 平均响应时间 | 17698 ms | **13.11 ms** | **↓ 99.93%** |
| P50 | 14867 ms | **12.04 ms** | ↓ 99.92% |
| 最快 / 最慢 | 12935 / 26900 ms | 5.85 / 21.58 ms | — |
| DB 查询次数（`Com_select`） | 45 | 1 | ↓ 97.8% |

未命中侧 6 次采样全部是秒级（12.9s ~ 26.9s）；命中侧 12 次采样全部落在 6 ~ 22 ms，且两次响应体逐字节一致——缓存返回的就是同一次大模型调用的结果。

原始采样数据：`benchmark/ai-recommend-off.txt`（绕过缓存）、`benchmark/ai-recommend-on.txt`（命中缓存）；复现脚本 `benchmark/ai-recommend-bench.ps1`。

> **这组数字怎么读**：未命中侧的耗时几乎全是"等大模型生成"，所以它随**模型与供应商**变化（本项目先后接过阿里云百炼的通义千问和 DeepSeek，两边差距是成倍的）；命中侧的 10ms 量级才是与模型无关、稳定可复现的那部分。这条链路的优化口径应该记成"**把秒级的大模型调用变成 10ms 级的本地读**"。

### 菜品列表热点读接口（`/user/dish/list`）：JMeter 并发压测

- **环境**：单机（同一台笔记本）；Windows；JDK 1.8.0_202；MySQL 8.0（本机）；Redis 3.2.100（本机）；应用与压测工具同机运行
- **被测接口**：`GET /user/dish/list`（用户端菜单列表，最典型的热点读接口）
- **压测工具**：JMeter 5.6.3，非 GUI 模式，50 并发 × 20 轮 = **1000 次请求**，5 秒爬坡，无思考时间
- **对照组**：`sky.cache.enabled=false`（缓存整体降级为 `NoOpCacheManager`，直接回源 DB）vs `sky.cache.enabled=true`
- **数据库查询次数**：取 MySQL `SHOW GLOBAL STATUS LIKE 'Com_select'` 在压测前后的差值
- **预热**：每组先跑一轮 1000 请求预热（JVM JIT + 连接池），预热数据不计入结果

压测脚本与原始结果都在仓库里，可直接复现：

```
benchmark/dish-list.jmx        # JMeter 测试计划
benchmark/run-benchmark.ps1    # 一键压测 + 统计脚本（输出平均/P50/P90/P95/P99/吞吐/错误率）
benchmark/results-false.jtl    # 无缓存基线原始数据
benchmark/results-true.jtl     # 有缓存原始数据
```

### 压测结果

| 指标 | 无缓存（基线） | 有缓存（Caffeine + Redis） | 变化 |
|------|---------------|---------------------------|------|
| 样本数 | 1000 | 1000 | — |
| 并发 | 50 | 50 | — |
| 平均响应时间 | 4.43 ms | **2.44 ms** | **↓ 44.9%** |
| P50 | 4 ms | **2 ms** | ↓ 50% |
| P90 | 5 ms | **3 ms** | ↓ 40% |
| P95 | 6 ms | **4 ms** | ↓ 33% |
| P99 | 7 ms | 6 ms | ↓ 14% |
| 最大响应时间 | 50 ms | 60 ms | — |
| 吞吐量 | 203.8 req/s | 205.5 req/s | 基本持平 |
| 错误率 | 0.00% | 0.00% | — |
| **DB 查询次数（Com_select）** | **1001** | **1** | **↓ 99.9%** |

### 怎么读这组数据（重要）

- **缓存带来的核心收益是"数据库零穿透"**：1000 次请求从 1001 次 SQL 查询降到 1 次，也就是 1000 次请求里只有 1 次真正回源（首次加载，L1 命中后后续请求连 Redis 都不用走）。
- **响应时间的下降幅度看起来不大（4.43ms → 2.44ms），这是诚实的结果**：本数据集只有 30 条菜品、单表查询、本机 MySQL，回源本身就只有 2ms 左右，所以"省下一次 SQL"能省的时间本来就有限。缓存挡住的是**数据库的并发压力**，而不是这 2ms。
- **吞吐量两边都是 ~200 req/s，说明瓶颈不在数据库**，而在压测客户端与应用同机竞争 CPU。如果要展示吞吐差距，需要把 MySQL 换到独立机器、把数据量放大到十万级。
- **结论口径**：在单机小数据量下，这套缓存方案的作用是 **"削减 99.9% 的数据库查询"**；响应时间的收益会随数据量与并发上升而放大。

### 复现方式

```powershell
# 1. 无缓存基线
$env:SKY_CACHE_ENABLED="false"
java -jar sky-server/target/sky-server-1.0-SNAPSHOT.jar
# 另开一个终端
.\benchmark\run-benchmark.ps1 -Label cache-off

# 2. 有缓存
$env:SKY_CACHE_ENABLED="true"
java -jar sky-server/target/sky-server-1.0-SNAPSHOT.jar
.\benchmark\run-benchmark.ps1 -Label cache-on
# 3. AI 推荐接口（大模型链路）：需要用户端 token
.\benchmark\ai-recommend-bench.ps1 -Token <token> -Hits 6  -Tag off
.\benchmark\ai-recommend-bench.ps1 -Token <token> -Hits 12 -Tag on -Warmup
```

---

## 三、核心设计说明

### 1. 缓存三大问题

**穿透**——`DishBloomFilter` 在应用启动时用全量菜品 id 预热（1 万元素 / 1% 误判率 ≈ 12KB 位数组、7 个哈希函数），新增菜品时追加写入。查询单个菜品前先过一遍 `mightContain`，返回 `false` 就直接判定"不存在"，既不查 Redis 也不打 DB。布隆过滤器只会有"假阳性"，不会有"假阴性"，所以拦截是安全的。查库结果为空时还会写入一个短 TTL 的空值缓存作为兜底。

**击穿**——`LogicalExpireCache` 不再给热点 key 设"物理过期即删除"，而是在 value 里额外存一个 `expireTime`。逻辑过期后 key 依然存在：抢到互斥锁（`SETNX`）的线程把重建任务丢进后台线程池异步执行，其余请求立刻拿到旧数据返回。这样热点 key 失效的瞬间不会出现"所有请求一起打到数据库"。物理 TTL 比逻辑 TTL 长一段，保证逻辑过期时旧数据还在。

**雪崩**——`JitterRedisCacheWriter` 用装饰器模式包住默认的 `RedisCacheWriter`，在 `put` / `putIfAbsent` 时给 TTL 叠加 `±ttlJitterMillis` 的随机偏移。同批写入的 key 不会在同一秒集体失效。

### 2. 缓存与数据库一致性

采用 **Cache-Aside + 先更库再删缓存**：`updateById` 上标 `@CacheEvict(value={"dishCache","aiRecommendCache"}, allEntries=true)` 清空二级缓存，同时手动淘汰逻辑过期 key。顺序不能反——先删缓存再更库会在"删完还没更库"的窗口里读到旧值。此外所有缓存都带 TTL，作为最终兜底。

### 3. 滑动窗口限流

`@RateLimit` 注解声明阈值、窗口和维度，AOP 拦截后用 Redis ZSET 实现滑动窗口：

```java
@RateLimit(key = "order:page", limit = 60, window = 60,
        dimensions = {RateLimit.Dimension.USER, RateLimit.Dimension.IP})
```

相比"每 60 秒清零"的固定窗口，滑动窗口不会出现窗口边界的双倍流量。整个"清理过期成员 + 计数 + 写入本次请求"在 Lua 里原子完成，并发下不会超卖。已接入的接口：`/user/ai/recommend`（大模型调用成本高，重点保护）、`/user/order/submit`、`/user/order/submit/cart`、`/user/order/page`。

### 4. 秒杀下单链路

```
用户下单 → Redis + Lua 原子预扣库存 → 发送 MQ 消息 → 立即返回订单号
                                            ↓
                        RabbitMQ → 消费者异步落库（订单 + 明细 + 扣 MySQL 库存 + 更新销量榜）
                                            ↓
                                  定时任务对账：Redis 与 DB 库存偏差自动回补
```

关键点：库存判断与扣减在同一个 Lua 脚本里完成（`deduct_stock.lua`），避免"查完再扣"的并发超卖；下单请求只做 Redis + MQ 两步，落库全部异步，因此接口 RT 很短。

### 5. 统计接口全异步 + 站内通知

统计类接口耗时长（要聚合库表还要调用大模型），因此：

- `ThreadPoolConfig` 提供命名线程池，`@Async("taskExecutor")` 把执行体切到池中，请求线程立刻返回
- `task_info` 表承载任务状态机（`PENDING → RUNNING → SUCCESS/FAILED`）、结果、重试次数
- **幂等**：提交前用 `businessKey`（`BUSINESS_ANALYSIS:{merchantId}:{日期}`）查重；执行前用 CAS（`UPDATE ... WHERE status='PENDING'`）抢占任务，重复提交只会有一个线程真正执行
- **重试**：失败按 1s / 2s / 3s 退避重试，最多 3 次；业务异常属于不可重试错误，直接失败不浪费重试次数
- **通知**：任务完成或最终失败都通过 WebSocket（`ws://host:8080/ws/notify/{userId}`）推送站内通知，前端不用轮询

> 踩坑记录：`@Async` 依赖 Spring 代理，**同一个类内部自调用不会走代理**，异步会静默失效退化成同步。所以执行体单独拆成了 `BusinessAnalysisTaskRunner`。

### 6. AI 三层推荐链路

`AIServiceImpl` 把推荐拆成三层：

1. **召回**：从用户历史已完成订单构建画像（订单数、累计消费、客单价、最爱菜品 Top3、口味偏好、菜品频次）；新用户则走商家销量榜单兜底
2. **排序**：按画像与偏好对候选菜品打分排序，取 Top10
3. **生成**：把画像 + 候选菜单 + 用户本次需求拼成 Prompt，交给大模型生成自然语言推荐（含"为什么推荐这道菜"）

经营分析走同一套模型能力，但数据来自真实库表聚合（`sumTotalSales` / `countTotalOrders` / `selectSalesTop5` / `selectSalesBottom5`），不再有硬编码的模拟数据。

---

## 四、技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 语言 / 运行时 | Java | **JDK 8** |
| 框架 | Spring Boot | 2.7.3 |
| ORM | MyBatis-Plus + MyBatis | 3.4.3 / 2.2.0 |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis（Jedis 客户端）+ Caffeine 本地缓存 | Redis 3.2+ |
| 消息队列 | RabbitMQ | 3.13 |
| 接口文档 | Knife4j（Swagger） | 3.0.2 |
| 大模型 | DeepSeek（OpenAI 兼容协议）/ 阿里云百炼 DashScope | — |
| 构建 | Maven | 3.8+ |
| 压测 | JMeter | 5.6.3 |

---

## 五、快速开始

### 前置条件

- **JDK 8**（项目基于 Spring Boot 2.7，用 JDK 17 运行会有兼容问题）
- MySQL 8.0+
- Redis 3.2+
- RabbitMQ 3.x
- Maven 3.8+

> 注意：仓库里的 `mvnw` / `mvnw.cmd` 缺少 `maven-wrapper.jar`，暂时不可用，请使用本机安装的 `mvn`。

### 1. 初始化数据库

```bash
mysql -uroot -p < schema.sql                    # 建库 + 建表 + 结构迁移（幂等，可重复执行）
mysql -uroot -p sky_take_out < test_data.sql    # 测试数据：60 笔订单 / 142 条明细 / 收货地址
```

`schema.sql` 是从本机运行中的 MySQL 8.0 实例导出的完整表结构（13 张表，含 `merchant`、`task_info`），
全部使用 `CREATE TABLE IF NOT EXISTS`，并在末尾用 `information_schema` + 预处理语句做了幂等的字段迁移（`merchant_id` 等），
所以对已有库重复执行也不会报错。
`test_data.sql` 会插入 2 个商家、10 个分类、30 道菜品及 24 条口味、4 个 C 端用户、2 个管理端员工，
以及 60 笔历史订单、142 条订单明细和 3 条默认收货地址；脚本开头先 DELETE 再 INSERT，可以重复执行。

**测试账号**：`test003` / `123456`（用户端登录）

### 2. 配置

复制配置示例并填入本机密码：

```bash
copy sky-server\src\main\resources\application-dev.example.yml sky-server\src\main\resources\application-dev.yml
```

`application-dev.yml` 已在 `.gitignore` 中排除，不会被提交。敏感项也支持环境变量注入：

| 环境变量 | 作用 | 默认值 |
|----------|------|--------|
| `SKY_DB_PASSWORD` | MySQL 密码 | — |
| `SKY_REDIS_PASSWORD` | Redis 密码 | 空 |
| `SKY_AI_API_KEY` | 大模型 API Key | 空（不配则 AI 接口不可用） |
| `SKY_AI_PROVIDER` | `openai` / `dashscope` | `openai` |
| `SKY_AI_BASE_URL` | 大模型服务地址 | `https://api.deepseek.com` |
| `SKY_AI_MODEL` | 模型名 | `deepseek-chat` |
| `SKY_CACHE_ENABLED` | 缓存总开关（`false` = 压测基线） | `true` |
| `SKY_BLOOM_ENABLED` | 布隆过滤器开关 | `true` |

### 3. 启动

```bash
mvn clean package -DskipTests
java -jar sky-server/target/sky-server-1.0-SNAPSHOT.jar
```

启动后：

- 接口文档：http://localhost:8080/doc.html
- 点餐 + AI 推荐页面：http://localhost:8080/user/ai/page

---

## 六、项目结构

```
sky-take-out/
├── pom.xml                       # 父 POM（统一依赖版本）
├── schema.sql                    # 建表 + 幂等结构迁移
├── test_data.sql                 # 测试数据（用户/商家/菜品/订单/地址）
├── benchmark/                    # 压测脚本与原始结果
│   ├── dish-list.jmx
│   ├── run-benchmark.ps1
│   └── results-*.jtl
├── sky-common/                   # 公共模块：工具类、常量、异常、上下文
│   └── com/sky/utils/QwenUtil.java           # 大模型调用（openai / dashscope 双协议）
├── sky-pojo/                     # POJO 模块：Entity / DTO / VO
└── sky-server/                   # 主服务模块
    └── src/main/
        ├── java/com/sky/
        │   ├── cache/            # 布隆过滤器、逻辑过期缓存、TTL 抖动写入器
        │   ├── annotation/        # @RateLimit
        │   ├── aspect/            # 限流切面
        │   ├── config/            # 缓存 / Redis / RabbitMQ / 线程池 / AI / WebMvc 配置
        │   ├── controller/        # admin（管理端）、user（用户端）
        │   ├── service/impl/      # 业务实现（含 AI、经营分析、通知）
        │   ├── mapper/            # MyBatis-Plus Mapper
        │   ├── mq/                # RabbitMQ 消息体与消费者
        │   ├── task/              # 定时对账任务、异步分析执行器
        │   └── websocket/         # 站内通知 WebSocket 端点
        └── resources/
            ├── application.yml            # 主配置（全环境变量占位符）
            ├── application-dev.example.yml # 开发配置示例（真实配置不提交）
            ├── lua/                       # 库存预扣减、滑动窗口限流脚本
            ├── mapper/                    # MyBatis XML
            └── static/ai-order.html       # 点餐 + AI 推荐单页
```

---

## 七、接口一览（用户端）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/user/auth/register` `/user/auth/login` | 注册 / 登录（返回 JWT） |
| GET | `/user/dish/list` | 菜品列表（二级缓存 + 逻辑过期） |
| GET | `/user/dish/{id}` | 菜品详情（布隆过滤器 + 二级缓存） |
| POST | `/user/shoppingCart/add` `/list` | 购物车 |
| GET | `/user/addressBook/default` | 默认收货地址 |
| POST | `/user/order/submit/cart` | 购物车下单（秒杀链路：Redis+Lua 预扣减 → MQ 异步落库） |
| GET | `/user/order/page` `/detail` | 订单分页 / 详情 |
| GET | `/user/ai/recommend` | AI 智能推荐（三层链路，带限流） |
| GET | `/user/ai/analysis` | 异步经营分析（返回 taskId） |
| GET | `/user/ai/analysis/sync` | 同步经营分析（直接返回结果） |
| WS | `/ws/notify/{userId}` | 站内通知推送 |

---

## 八、说明与已知限制

- **管理端功能范围**：本次改造聚焦"高并发读 + 秒杀 + AI"这条链路，用户端接口完整可用；管理端保留了**登录 → 菜品管理 → 订单管理 → 数据分析 → AI 经营分析**这一条主流程，其余教学用的功能菜单（套餐、分类、员工、报表导出等）沿用原项目代码，未做改造也未经我验证。
- **布隆过滤器是单机的**：位数组存在 JVM 内存里，应用重启会重新预热。多实例部署时应换成 Redis Bitmap（`SETBIT` / `GETBIT`）做共享，并在新增菜品时同步写入。
- **布隆过滤器不支持删除**：菜品删除后会留下一个"假阳性"，代价只是这次查询多走一次数据库，不影响正确性（这是布隆过滤器的固有特性）。
- **`mvnw` 不可用**：缺少 `maven-wrapper.jar`，请用本机 Maven。
- **压测结论的适用范围**：见上文"怎么读这组数据"，单机小数据量下缓存的主要收益是削减数据库查询次数，而不是大幅降低响应时间。
