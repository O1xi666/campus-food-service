# Sky Take-Out (苍穹外卖)

基于 Spring Boot + MyBatis-Plus 的外卖点餐后台管理系统，集成 AI 智能推荐、RabbitMQ 异步落库、Redis 缓存与排行榜等功能。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 2.7.3 |
| ORM | MyBatis-Plus | 3.4.3 |
| 数据库 | MySQL | — |
| 缓存 | Redis (Jedis) | — |
| 消息队列 | RabbitMQ | — |
| 接口文档 | Knife4j (Swagger) | 3.0.2 |
| AI | Qwen (通义千问) API | — |
| 构建工具 | Maven | 3.9.14 |
| 前端 | Vue 2 (Element UI) | — |

## 项目结构

```
sky-take-out/
├── pom.xml                  # 父 POM
├── mvnw / mvnw.cmd          # Maven Wrapper
├── schema.sql               # 数据库初始化脚本
├── sky-common/              # 公共模块（工具类、常量、异常）
├── sky-pojo/                # POJO 模块（Entity、DTO、VO）
└── sky-server/              # 服务模块（Controller、Service、Mapper）
    └── src/main/resources/
        ├── application.yml        # 主配置（使用环境变量占位符）
        ├── application-dev.yml     # 开发环境配置（已 gitignored）
        └── static/                 # 前端静态资源
```

## 快速开始

### 前置条件

- JDK 17+
- MySQL 8.0+
- Redis
- RabbitMQ
- Maven 3.8+（或使用项目自带的 `mvnw`）

### 1. 初始化数据库

```sql
-- 先创建数据库
CREATE DATABASE IF NOT EXISTS sky_take_out;
-- 然后执行 schema.sql 中的建表语句和表结构迁移
```

### 2. 配置环境变量

复制 `application-dev.yml`（已 gitignored），填入本地的数据库、Redis 和 AI 密钥：

```yaml
# sky-server/src/main/resources/application-dev.yml
sky:
  datasource:
    host: localhost
    port: 3306
    database: sky_take_out
    username: root
    password: your-password
  ai:
    qwen:
      api-key: your-qwen-api-key
```

### 3. 启动项目

```bash
# 使用 Maven Wrapper（推荐）
./mvnw compile
./mvnw spring-boot:run -pl sky-server

# 或使用本地 Maven
mvn compile
mvn spring-boot:run -pl sky-server
```

### 4. 访问页面

| 地址 | 说明 |
|------|------|
| http://localhost:8080/index.html#/login | 管理后台页面 |
| http://localhost:8080/doc.html | Knife4j API 文档 |

**管理员登录账号：** 在数据库中 `employee` 表中配置，默认用户名 `admin`，密码 `123456`。

**用户端注册：** 通过 Knife4j 调用 `POST /user/auth/register` 注册。

## API 概览

| 前缀 | 描述 |
|------|------|
| `/admin/employee` | 管理员员工管理 |
| `/admin/dish` | 菜品管理 |
| `/admin/order` | 订单管理 |
| `/admin/analysis` | 经营数据分析 |
| `/admin/statistics` | 销售统计 |
| `/user/auth` | 用户注册/登录 |
| `/user/ai` | AI 智能推荐 |
| `/user/order` | 用户端订单 |
| `/user/dish` | 用户端菜品浏览 |
| `/user/shoppingCart` | 购物车 |
| `/user/addressBook` | 地址簿 |

## 部署注意事项

1. **密钥保护**：`application.yml` 中的敏感信息使用环境变量占位符，真正的密钥在 `application-dev.yml` 中，该文件已被 `.gitignore` 忽略
2. **数据库迁移**：`schema.sql` 包含建表和字段迁移脚本，首次部署需执行
3. **Service Worker**：前端静态资源包含 Service Worker 缓存，修改 JS 后需清除浏览器缓存或注销 Service Worker
