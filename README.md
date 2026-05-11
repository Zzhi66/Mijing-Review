<div align="center">

<img src="https://img.shields.io/badge/觅境点评-生活空间探索平台-FF5C5C?style=for-the-badge&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHBhdGggZmlsbD0id2hpdGUiIGQ9Ik0xMiAyQzYuNDggMiAyIDYuNDggMiAxMnM0LjQ4IDEwIDEwIDEwIDEwLTQuNDggMTAtMTBTMTcuNTIgMiAxMiAyek0xMiAxN2wtNS01aDNWOGg0djRoM2wtNSA1eiIvPjwvc3ZnPg==" />

# 觅境点评

**🌟 臻选生活空间探索平台 · 发现你身边最值得去的地方**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.12-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Redis](https://img.shields.io/badge/Redis-3.0+-DC382D?style=flat-square&logo=redis&logoColor=white)](https://redis.io)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?style=flat-square&logo=mysql&logoColor=white)](https://www.mysql.com)
[![Kafka](https://img.shields.io/badge/Kafka-削峰异步-231F20?style=flat-square&logo=apachekafka&logoColor=white)](https://kafka.apache.org)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-AI客服-8A2BE2?style=flat-square)](https://github.com/langchain4j/langchain4j)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)

</div>

---

## ✨ 项目简介

**觅境点评**是一个面向高品质生活方式的**商铺探索与点评平台**，提供商铺搜索、探店笔记、限时秒杀、AI 智能客服等核心功能。项目后端采用 Spring Boot 生产级架构，集成 Redis 多级缓存、Kafka 消息削峰、Redisson 分布式锁，并通过 LangChain4j 接入阿里云通义千问大模型实现智能客服。

---

## 🖼️ 界面预览

登录页面
<img width="533" height="760" alt="image" src="https://github.com/user-attachments/assets/3a69c309-270c-450d-ad40-579f353c5831" />



AI客服页面
<img width="1918" height="911" alt="image" src="https://github.com/user-attachments/assets/0256ccef-87bc-4e46-8cb9-2c3b68a6c539" />


---

## 🏗️ 技术架构

```
┌─────────────────────────────────────────────────────────┐
│                      前端（Vanilla HTML/CSS/JS）          │
│  login.html  index.html  seckill.html  ai-chat.html     │
│             ES Module + Fetch API + 响应式设计             │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP / REST
┌──────────────────────▼──────────────────────────────────┐
│               后端（Spring Boot 2.7 / Java 17）           │
│                                                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────┐  │
│  │User/Shop │  │  Blog    │  │ Seckill  │  │  AI    │  │
│  │Controller│  │Controller│  │Controller│  │Service │  │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └───┬────┘  │
│       │              │              │              │       │
│  ┌────▼──────────────▼──────┐  ┌───▼──┐   ┌─────▼────┐  │
│  │   Service Layer + Redis  │  │Kafka │   │LangChain │  │
│  │  二级缓存 / 布隆过滤器   │  │ 削峰 │   │   4j     │  │
│  └──────────────────────────┘  └──────┘   └──────────┘  │
│                                                          │
└──────────────────────┬──────────────────────────────────┘
                       │
       ┌───────────────┼───────────────┐
       ▼               ▼               ▼
   MySQL 8.0       Redis 3.0+      Kafka
   (持久化)       (缓存/会话)     (消息队列)
```

---

## 🚀 核心功能

### 👤 用户系统
- 手机号 + 验证码登录（Redis TTL 管理验证码）
- Token 双拦截器刷新机制（自动续期）
- 用户信息缓存、关注 / 共同关注（Redis Set 交集）

### 🏪 商铺模块
- **Caffeine + Redis 二级缓存**，Caffeine 本地毫秒级命中，Redis 分布式共享
- **缓存穿透保护**：空值缓存 + 布隆过滤器双重防御
- **缓存击穿防护**：逻辑过期 + Redisson 分布式锁
- **Redis GEO**：`GEOSEARCH` 查询附近商铺，按距离排序分页

### 📝 探店笔记
- 笔记发布 / 查询 / 热门排行（Redis ZSet 按点赞数排序）
- 点赞（Redis Set 去重，一用户一次）
- 关注 Feed 流推送（推拉结合，Redis ZSet 时间线）

### ⚡ 限时秒杀
- **Lua 脚本原子操作**：库存校验 + 一人一单 + 扣减库存，保证原子性
- **Kafka 消息削峰**：秒杀请求异步入队，Consumer 批量落库
- **Redisson 分布式锁 + 乐观锁**：防重复下单双重保障
- Kafka 发送失败自动回调补偿，Redis 库存回滚

### 🤖 AI 智能客服
- **LangChain4j** 接入阿里云百炼（DashScope）通义千问模型
- **Function Calling**：`@Tool` 注解实现商铺搜索、优惠券查询工具调用
- **Redis List 多轮会话记忆**：按用户 ID 隔离，最近 20 条上下文
- 打字指示器 + Markdown 渲染聊天界面

---

## 📁 项目结构

```
Mijing-Review/
├── src/main/java/com/mj/mijing/
│   ├── controller/          # REST 接口层
│   │   ├── UserController.java
│   │   ├── ShopController.java
│   │   ├── BlogController.java
│   │   ├── VoucherOrderController.java
│   │   └── AiController.java
│   ├── service/impl/        # 业务逻辑层
│   ├── ai/                  # AI 客服模块
│   │   ├── AiChatService.java
│   │   ├── MijingAiAssistant.java
│   │   ├── ShopSearchTool.java
│   │   └── ShopOrderTool.java
│   ├── config/              # 配置（Redis/Kafka/Redisson/MVC）
│   ├── kafka/               # 消息定义与消费者
│   ├── utils/               # 工具（缓存客户端/雪花ID/常量）
│   └── entity/dto/mapper/   # 实体/DTO/Mapper
├── src/main/resources/
│   ├── application.yaml     # 主配置
│   ├── scripts/seckill.lua  # 秒杀 Lua 脚本
│   └── db/mijing.sql        # 初始化 SQL
└── frontend/                # 前端静态页面
    ├── login.html           # 登录页
    ├── index.html           # 首页（商铺列表）
    ├── seckill.html         # 秒杀页
    ├── ai-chat.html         # AI 客服页
    ├── css/common.css       # 全局样式
    └── js/
        ├── api.js           # HTTP 请求封装
        └── utils.js         # 工具函数
```

---

## ⚙️ 快速开始

### 环境要求

| 依赖 | 版本 |
|------|------|
| JDK  | 17+  |
| Maven | 3.8+ |
| MySQL | 8.0+ |
| Redis | 3.0+ |
| Kafka | 可选（秒杀功能需要）|

### 1. 初始化数据库

```sql
-- 执行初始化脚本
source src/main/resources/db/mijing.sql
```

### 2. 修改配置

```yaml
# src/main/resources/application.yaml

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mijing
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379

ai:
  dashscope:
    api-key: your-dashscope-api-key   # 阿里云百炼 API Key
    model: qwen-turbo
```

### 3. 启动服务

```bash
# 1. 启动 Redis
redis-server

# 2. 启动后端（项目根目录）
mvn spring-boot:run

# 3. 启动前端静态服务器
npx serve frontend -p 5500
```

### 4. 访问

| 页面 | 地址 |
|------|------|
| 登录页 | http://localhost:5500/login.html |
| 首页   | http://localhost:5500/index.html |
| 秒杀页 | http://localhost:5500/seckill.html?shopId=1&id=券ID |
| AI 客服 | http://localhost:5500/ai-chat.html |
| 后端接口 | http://localhost:8081 |

---

## 🔑 核心亮点

```
🔒 安全        双拦截器 Token 鉴权 + Redis 会话管理
⚡ 高并发      Lua 原子秒杀 + Kafka 削峰 + Redisson 分布式锁
🚀 高性能      Caffeine + Redis 二级缓存，GEO 空间查询
🛡️ 高可用      缓存穿透/击穿/雪崩全场景保护
🤖 AI 能力     LangChain4j Function Calling + 多轮记忆
🎨 精美前端    深色渐变设计 + 骨架屏 + 微动效
```

---

## 📄 License

[MIT](LICENSE) © 2026 觅境点评

<div align="center">
  <sub>Built with ❤️ using Spring Boot · Redis · Kafka · LangChain4j</sub>
</div>
