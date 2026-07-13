# 个人财务管理系统

> 我的第一个完整全栈项目。从零开始，一个人完成了从数据库设计到前端 3D 交互的全部工作。

## 为什么做这个项目

作为一个刚入门的开发者，我不想只停留在"跟着教程敲代码"的阶段。我需要一个真实的项目来验证：**我能不能独立完成一个从后端到前端、从数据库到部署的完整产品？**

这个项目就是我的答案。它不是模板，不是脚手架生成的东西——每一行代码都是我理解、决策、调试后的结果。过程中 AI 帮助我跨越了很多知识盲区，但每一个技术选型、每一次问题排查、每一个架构决策，我都要求自己理解清楚为什么这么做。

## 我在这个项目中学到了什么

这不是一个"功能堆砌"的项目，而是一个"问题驱动"的学习过程：

| 我遇到的问题 | 我的解决方案 | 我学到的 |
|---|---|---|
| 前后端联调总是跨域报错 | 手写 CORS 配置 + Vite 代理 | 理解了浏览器的同源策略和后端跨域处理 |
| 用户密码怎么安全存储 | BCrypt 加密 + JWT Token 认证 | 认证授权不是"加个登录页"那么简单 |
| 不同用户的数据不能互相看到 | 基于 userId 的数据隔离 + 拦截器 | 安全是系统设计的一部分，不是事后补的 |
| 数据库密码硬编码在代码里被上传到了 GitHub | 环境变量 + .gitignore + Git 历史清理 | 安全意识：从犯错中学习，比看十篇文章都深刻 |
| 前端页面太丑没有设计感 | NASA 风格深色主题 + CSS 变量系统 | 好的 UI 不是"好看"，是有设计语言和一致性 |
| 账单展示没有交互感 | Three.js 布料物理模拟 + 弹簧系统 | 前端不只是 DOM 操作，还可以做物理引擎级别的交互 |
| 代码越来越乱不好维护 | 大规模精简重构，拆分组件，统一设计令牌 | 代码质量不是"写得多"，是"删得多"还能保持功能 |
| 部署环境依赖太多 | Docker Compose 一键编排 | 容器化不是运维的事，是开发者的基本能力 |
| Redis 不是每个环境都有 | 可选降级为内存缓存 | 好的架构设计要考虑"没有这个组件时怎么办" |

## 技术架构

```
┌─────────────────────────────────────┐
│         Vue 3 + Vite (前端)          │
│  ECharts · Three.js · GSAP · i18n   │
└──────────────┬──────────────────────┘
               │ Axios + JWT
               ▼
┌─────────────────────────────────────┐
│      Spring Boot 3 (后端)            │
│  JWT 认证 · 角色权限 · 数据隔离       │
│  MyBatis-Plus · Spring Cache        │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│         MySQL 8 (数据库)             │
│  自动建表 · 用户数据隔离              │
└─────────────────────────────────────┘
```

## 核心功能

### 业务功能
- 账单管理：新增、编辑、删除、查询和筛选（按类型、分类过滤）
- 分类管理：新增和查询收入/支出分类
- 首页统计：本月收入、本月支出、本月结余、储蓄率
- 图表统计：分类支出环形图 + 收支对比面积图（ECharts）
- 登录注册：邮箱注册、邮箱登录，BCrypt 加密
- 国际化：中文 / 英文切换
- 智能分析：根据储蓄率、最大支出类别、收支比自动生成财务建议

### 前端展示模块（演示数据）
- KPI 卡片：本月收入、本月支出、净现金流、储蓄率（带数值动画）
- 即将到期账单：待开发（预留 UI 空间）
- 预算进度：待开发（预留 UI 空间）
- 侧边栏导航：7 个菜单项（仪表盘 / 交易记录 / 预算 / 分析 / 投资 / 智能分析 / 设置）

### 安全与权限
- JWT 鉴权：Token 认证，拦截器统一处理 401
- 角色权限：ADMIN / USER / VIEWER，`@RequireRole` 自定义注解
- 用户数据隔离：每个用户只能看到自己的账单和统计数据
- 环境变量管理：所有敏感配置通过 .env 文件注入，不上传到代码仓库

### 3D 交互展示
账单展示区域不是普通的列表，而是一个基于 Three.js 的 3D 交互纸张：
- Verlet 积分布料物理模拟，纸张具备真实的软体变形
- 弹簧约束网格传播拖拽力，产生自然弯曲
- 多频呼吸动画 + 微风扰动，闲置时自然漂浮
- 阴影延迟跟随 + 高光偏移，多层视差运动
- 设备性能自适应：根据 CPU 核心数和内存调整渲染精度

### 工程化
- Docker Compose 一键部署（MySQL + 后端 + 前端 + Nginx）
- Redis 可选，无 Redis 时自动降级为内存缓存
- 全局异常处理、统一响应格式

## 技术栈

| 层级 | 技术 | 为什么选它 |
|---|---|---|
| 前端框架 | Vue 3 + Vite | 组合式 API 更灵活，Vite 开发体验好 |
| UI / 图表 | Element Plus + ECharts 6 | 组件库 + 数据可视化，覆盖大部分需求 |
| 3D 交互 | Three.js + GSAP | 实现物理级别的交互效果 |
| 后端框架 | Spring Boot 3.3.5 | Java 生态最成熟的方案，学习资源丰富 |
| ORM | MyBatis-Plus 3.5.7 | 比 MyBatis 少写大量模板代码 |
| 认证 | JWT + BCrypt | 无状态认证，适合前后端分离架构 |
| 数据库 | MySQL 8 | 最常用的关系型数据库 |
| 缓存 | Redis（可选） | 优雅降级设计，不强依赖外部组件 |
| 部署 | Docker Compose + Nginx | 一键部署，环境一致性 |

## 项目结构

```text
├── finance-ui/                        # 前端（Vue 3 + Vite）
│   ├── src/api/                       #   请求封装（axios 拦截器、JWT 注入）
│   ├── src/components/                #   组件（3D 收据、图表、侧边栏等）
│   ├── src/i18n/                      #   国际化
│   ├── src/views/                     #   页面（登录注册、仪表盘）
│   └── src/utils/sounds.js            #   硬币音效（Web Audio API）
│
├── src/main/java/.../personalfinance/
│   ├── annotation/                    # @RequireRole 自定义注解
│   ├── config/                        # CORS、Redis、MyBatis-Plus、WebMvc
│   ├── controller/                    # Auth、Bill、Category、Statistics、Admin
│   ├── interceptor/                   # JWT 认证拦截器
│   ├── service/                       # 业务逻辑
│   └── util/JwtUtil.java             # JWT 工具类
│
├── src/main/resources/
│   ├── application.properties         # 配置（环境变量占位，无硬编码密码）
│   └── schema.sql                     # 建表脚本（启动自动执行）
│
├── docker-compose.yml                 # 一键部署（环境变量，无硬编码密码）
├── Dockerfile / Dockerfile.frontend   # 前后端容器化
├── nginx.conf                         # Nginx 反向代理配置
└── .env.example                       # 环境变量模板
```

## 快速开始

### 环境要求

| 依赖 | 版本 |
|---|---|
| JDK | 17+ |
| MySQL | 8.0+ |
| Node.js | 20.19+ 或 22.12+（Vite 8 要求） |
| Redis | 可选 |

### 1. 配置环境变量

```powershell
# 复制模板并填入你的配置
cp .env.example .env
```

`.env` 文件内容：

```env
DB_PASSWORD=你的数据库密码
DB_URL=jdbc:mysql://localhost:3306/personal_finance
DB_USERNAME=root
JWT_SECRET=你的JWT密钥至少32个字符
```

### 2. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS personal_finance;
```

### 3. 启动

```powershell
# 后端
mvn spring-boot:run

# 前端
cd finance-ui
npm install
npm run dev
```

访问 http://localhost:5173

### Docker 一键部署

```powershell
# 设置环境变量后一键启动
$env:MYSQL_ROOT_PASSWORD="你的密码"
$env:JWT_SECRET="你的JWT密钥"
docker-compose up -d --build
```

访问 http://localhost

### 演示账号

| 字段 | 值 |
|------|-----|
| 邮箱 | demo@test.com |
| 密码 | password |

> ⚠️ 该账户仅用于本地演示，BCrypt 哈希为 Spring Security 公开测试向量。生产部署请删除或修改该种子数据。

## API 接口

| 方法 | 路径 | 说明 | 认证 |
|---|---|---|---|
| POST | `/api/auth/register` | 注册 | 否 |
| POST | `/api/auth/login` | 登录 | 否 |
| GET | `/api/bills` | 账单列表 | 是 |
| POST | `/api/bills` | 新增账单 | 是 |
| PUT | `/api/bills/{id}` | 编辑账单 | 是 |
| DELETE | `/api/bills/{id}` | 删除账单 | 是 |
| GET | `/api/categories` | 分类列表 | 是 |
| POST | `/api/categories` | 新增分类 | 是 |
| GET | `/api/statistics/dashboard` | 首页统计 | 是 |
| GET | `/api/statistics/expense-by-category` | 分类支出统计 | 是 |
| GET | `/api/admin/users` | 用户列表 | ADMIN |
| PUT | `/api/admin/users/role` | 修改角色 | ADMIN |

## 安全实践

这个项目的安全配置是我在犯错后学习并修复的——数据库密码曾经硬编码在代码里并被上传到 GitHub，我通过以下方式彻底修复：

1. **环境变量管理**：所有密码、密钥通过 `.env` 注入，代码中零硬编码
2. **Git 历史清理**：删除包含敏感信息的旧提交，重建干净的提交历史
3. **.gitignore 保护**：`.env` 文件不会被上传到任何远程仓库
4. **JWT 密钥**：无默认值，必须通过环境变量配置才能启动

## 关于这个项目

这是一个学习项目，但不是一个"照着教程做"的项目。它的每一个功能都是我遇到真实问题后主动寻找解决方案的结果。如果你也是刚入门的开发者，希望这个项目能给你一些参考——不是参考代码怎么写，而是参考**遇到问题时怎么思考和解决**。

---

**Tech Stack:** Vue 3 · Spring Boot 3 · MyBatis-Plus · MySQL · JWT · Three.js · Docker · Nginx
