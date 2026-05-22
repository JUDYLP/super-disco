# 个人财务管理系统

基于 Vue 3 + Spring Boot 3 + MyBatis-Plus + MySQL 的前后端分离个人财务管理系统，采用 NASA 风格深色主题 UI。

项目定位是"第一个完整全栈练习项目"，重点不是堆复杂技术，而是把账单管理、分类管理、统计展示、登录注册、前后端联调、数据库脚本和项目文档这些基础工程能力做完整、做清楚。

## 项目功能

- 账单管理：新增、编辑、删除、查询和筛选账单（按类型、分类过滤）
- 分类管理：新增和查询收入/支出分类
- 首页统计：展示本月收入、本月支出和本月结余
- 图表统计：按分类统计支出占比（ECharts 饼图）
- 交互展示：3D 风格账单展示区域（Three.js）
  - 基于 Verlet 积分的布料物理模拟，纸张具备真实的软体变形效果
  - 顶点级变形：拖拽时力通过弹簧约束网格传播，产生自然弯曲和曲率
  - 顶部约束 + 底部自由下垂：模拟纸张被夹住悬挂的真实行为
  - 多频呼吸动画 + 微风扰动：闲置时纸张自然漂浮
  - 弹簧系统驱动旋转/位移/缩放，带阻尼和惯性
  - 阴影延迟跟随 + 高光偏移 + 环境光延迟：多层视差运动
  - 设备性能自适应：根据 CPU 核心数和内存调整网格细分精度
- 登录注册：邮箱注册、邮箱登录，密码使用 BCrypt 加密保存
- JWT 鉴权：基于 JWT Token 的身份认证，拦截器统一处理 401
- 角色权限：ADMIN / USER / VIEWER 三种角色，`@RequireRole` 注解控制接口权限
- 用户数据隔离：每个用户只能看到自己的账单和统计数据
- 国际化：支持中文 / 英文切换
- Redis 可选：支持 Redis 缓存和 Token 失效管理，无 Redis 时自动降级为内存缓存

## 技术栈

| 层级 | 技术 |
|---|---|
| 前端 | Vue 3、Vite 8、Axios、ECharts 6、Three.js、GSAP、Element Plus |
| 后端 | Java 17、Spring Boot 3.3.5、Spring Web、Spring Cache、MyBatis-Plus 3.5.7 |
| 数据库 | MySQL 8 |
| 认证 | JWT (jjwt 0.12.5)、BCrypt (spring-security-crypto) |
| 缓存 | Redis（可选）、ConcurrentMapCacheManager（默认降级方案） |
| 工程工具 | Maven、npm、Git |

## 项目结构

```text
my_project/
├── finance-ui/                    前端项目（Vue 3 + Vite）
│   ├── src/
│   │   ├── api/                   API 请求封装（axios 拦截器、JWT 注入）
│   │   ├── components/            UI 组件（图表、侧边栏、3D 收据等）
│   │   ├── i18n/                  国际化（中/英）
│   │   ├── views/                 页面视图（登录注册、仪表盘）
│   │   ├── App.vue                根组件
│   │   ├── main.js                入口
│   │   ├── style.css              全局样式
│   │   └── tokens.css             设计令牌（CSS 变量）
│   ├── package.json
│   └── vite.config.js             Vite 配置（含 API 代理）
│
├── src/main/java/com/example/personalfinance/
│   ├── annotation/                自定义注解（@RequireRole）
│   ├── common/                    公共类（ApiResponse、UserContext）
│   ├── config/                    配置类（CORS、Redis、MyBatis-Plus、WebMvc）
│   ├── controller/                控制器（Auth、Bill、Category、Statistics、Admin）
│   ├── dto/                       请求 DTO
│   ├── entity/                    数据库实体
│   ├── exception/                 全局异常处理
│   ├── interceptor/               JWT 认证拦截器
│   ├── mapper/                    MyBatis-Plus Mapper
│   ├── service/                   业务接口与实现
│   ├── util/                      工具类（JwtUtil）
│   ├── vo/                        响应 VO
│   └── PersonalFinanceApplication.java   启动类
│
├── src/main/resources/
│   ├── application.properties     配置文件（环境变量占位）
│   └── schema.sql                 数据库建表脚本（自动执行）
│
├── Dockerfile                      Docker 构建文件（后端）
├── Dockerfile.frontend             Docker 构建文件（前端）
├── docker-compose.yml               Docker Compose 编排（MySQL + 后端 + 前端）
├── nginx.conf                       Nginx 配置（前端 SPA + API 代理）
├── sql/                           SQL 脚本
├── pom.xml                            Maven 配置
└── README.md
```

## 环境要求

| 依赖 | 版本 | 用途 |
|---|---|---|
| JDK | 17+ | 运行 Spring Boot |
| MySQL | 8.0+ | 数据库 |
| Node.js | 18+ | 构建前端 |
| Redis | 任意版本（可选） | 缓存和 Token 失效管理 |

## 快速开始

### 1. 数据库准备

MySQL 中创建数据库（应用启动时会通过 `schema.sql` 自动建表）：

```sql
CREATE DATABASE IF NOT EXISTS personal_finance;
```

### 2. 配置环境变量

在 PowerShell 中设置（或在系统环境变量中配置）：

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/personal_finance?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的数据库密码"
$env:JWT_SECRET="你的JWT密钥至少32个字符"
```

所有配置项都支持环境变量覆盖，默认值见 `application.properties`。

### 3. 启动后端

```powershell
mvn spring-boot:run
```

后端默认地址：`http://localhost:8080`

### 4. 启动前端

```powershell
cd finance-ui
npm install
npm run dev
```

前端默认地址：`http://localhost:5173`

Vite 会把 `/api` 请求代理到 `http://localhost:8080`。

### 5. 前端构建（部署用）

```powershell
cd finance-ui
npm run build
```

构建产物在 `finance-ui/dist/` 目录，可由 Nginx 或 Spring Boot 静态资源托管。

### 6. 使用 .env 文件管理敏感配置（推荐）

创建 `.env` 文件管理所有敏感配置（该文件已加入 `.gitignore`，不会被上传）：

```env
# MySQL Configuration
DB_PASSWORD=your_database_password
DB_URL=jdbc:mysql://localhost:3306/personal_finance
DB_USERNAME=root

# JWT Configuration
JWT_SECRET=YourSuperSecretJwtKeyMustBeAtLeast32CharactersLong!
JWT_EXPIRATION_MS=86400000

# Docker Configuration
MYSQL_ROOT_PASSWORD=your_secure_mysql_password

# GitHub Configuration (for MCP)
GITHUB_TOKEN=your_github_personal_access_token
```

启动时自动加载 `.env` 文件中的环境变量。

## 主要接口

| 方法 | 路径 | 说明 | 认证 |
|---|---|---|---|
| POST | `/api/auth/register` | 邮箱注册 | 否 |
| POST | `/api/auth/login` | 邮箱登录 | 否 |
| GET | `/api/bills` | 查询账单列表 | 是 |
| POST | `/api/bills` | 新增账单 | 是 |
| PUT | `/api/bills/{id}` | 编辑账单 | 是 |
| DELETE | `/api/bills/{id}` | 删除账单 | 是 |
| GET | `/api/categories` | 查询分类 | 是 |
| POST | `/api/categories` | 新增分类 | 是 |
| GET | `/api/statistics/dashboard` | 首页统计 | 是 |
| GET | `/api/statistics/expense-by-category` | 分类支出统计 | 是 |
| GET | `/api/admin/users` | 用户列表 | ADMIN |
| PUT | `/api/admin/users/role` | 修改用户角色 | ADMIN |

## 认证与权限

- 注册需要用户名、邮箱和密码。密码至少 6 位且必须包含数字。
- 登录成功后返回 JWT Token，前端存储在 `localStorage` 中，后续请求通过 `Authorization: Bearer <token>` 传递。
- 后端通过 `AuthInterceptor` 拦截所有 `/api/**` 请求（登录和注册接口除外），验证 Token 有效性。
- `@RequireRole("ADMIN")` 注解限制接口只能由管理员访问。
- 用户数据隔离：账单和统计查询自动按当前登录用户过滤。

## Redis 配置（可选）

项目默认使用内存缓存（`ConcurrentMapCacheManager`），无需安装 Redis。

如果需要 Redis 缓存和 Token 失效管理功能：

1. 安装并启动 Redis
2. 设置环境变量：`$env:CACHE_TYPE="redis"`
3. 重启后端即可自动切换到 Redis 缓存

## Docker 一键启动（推荐）

项目支持 Docker Compose 一键启动全部服务：**MySQL + 后端 + 前端**，无需手动安装 JDK、MySQL、Node.js。

### 前置条件

安装 [Docker Desktop](https://www.docker.com/products/docker-desktop/)（Windows/macOS）或 Docker Engine（Linux）。安装后确保 Docker 已启动。

### 设置环境变量

Docker 需要设置以下环境变量：

```powershell
$env:MYSQL_ROOT_PASSWORD="your_secure_mysql_password"
$env:JWT_SECRET="YourSuperSecretJwtKeyMustBeAtLeast32CharactersLong!"
```

或在 `.env` 文件中配置。

### 启动命令

```powershell
# 在项目根目录执行
docker-compose up -d --build
```

等待构建完成（约 3-5 分钟），访问：

- 前端页面：http://localhost
- 后端接口：http://localhost:8080

## 安全注意事项

### 🔐 敏感信息保护

1. **不要硬编码密码**：所有密码、密钥、Token 都通过环境变量传递
2. **.env 文件不上传**：`.env` 已加入 `.gitignore`，确保敏感信息不会被提交到 GitHub
3. **JWT 密钥**：生产环境使用至少 32 位的随机密钥
4. **数据库密码**：使用强密码，避免使用简单密码如 `password123`

### 🚫 禁止上传的文件

```
.env                    # 环境变量配置（包含密码）
application-local.properties  # 本地配置
*.log                   # 日志文件
node_modules/           # 依赖目录
target/                 # 编译产物
dist/                   # 构建产物
```

### 🔑 生成安全的 JWT 密钥

```powershell
# PowerShell 生成随机 64 位密钥
-join ((65..90) + (97..122) + (48..57) | Get-Random -Count 64 | % {[char]$_})
```

### 关闭服务

```powershell
docker-compose down
```

### 查看日志

```powershell
# 全部服务日志
docker-compose logs -f

#只看后端日志
docker-compose logs -f backend

# 只看数据库日志
docker-compose logs -f mysql
```

### 初始账号

| 字段 | 值 |
|------|-----|
| 邮箱 | demo@test.com |
| 密码 | demo123 |

> 如果数据库初始化脚本未自动执行，进入容器手动执行：
> `docker exec -i personal-finance-mysql mysql -uroot -p${MYSQL_ROOT_PASSWORD} < /docker-entrypoint-initdb.d/01-init.sql`

### 架构说明

```
┌─────────────────────────────────────────────┐
│              localhost:80                    │
│           Nginx (静态文件 + API 代理)         │
└──────────────────────┬──────────────────────┘
                       │ /api/
       ┌───────────────┴───────────────┐
       ▼                               ▼
┌──────────────┐              ┌──────────────────┐
│   MySQL:3306 │◄─────────────│  Spring Boot     │
│   (数据持久化) │              │  localhost:8080  │
└──────────────┘              └──────────────────┘
       │
       │ docker network（内部网络）
       ▼
  ┌─────────────┐
  │  MySQL 容器  │
  └─────────────┘
```

### 自定义配置

修改 `docker-compose.yml` 中的 `environment` 或 `ports` 可自定义端口和密码。

---

## 手动部署（传统方式）

部署到服务器只需要 **JDK 17+** 和 **MySQL 8**：

```powershell
# 构建前端
cd finance-ui && npm run build

# 构建后端 JAR
cd .. && mvn clean package -DskipTests

# 运行
java -jar target/personal-finance-1.0-SNAPSHOT.jar \
  --DB_URL=jdbc:mysql://... \
  --DB_PASSWORD=... \
  --JWT_SECRET=...
```

前端静态文件可由 Nginx 托管，反向代理 `/api` 到后端 8080 端口。
