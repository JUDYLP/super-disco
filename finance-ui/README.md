# 个人财务管理系统 — 前端

> Vue 3 + Vite + Element Plus + ECharts 6 + Three.js + GSAP

## 快速开始

```bash
cd finance-ui
npm install
npm run dev        # 开发服务器 → http://localhost:5173
npm run build      # 生产构建 → dist/
npm run preview    # 预览生产构建
```

## 技术栈

| 技术 | 用途 |
|------|------|
| Vue 3 (Composition API) | UI 框架，`<script setup>` 语法 |
| Vite 8 | 构建工具，HMR 极速热更新 |
| Element Plus | UI 组件库（按钮、表单、弹窗） |
| ECharts 6 | 数据可视化（环形图、面积图） |
| Three.js | 3D 交互式收据（布料物理模拟） |
| GSAP | 高性能动画（金币粒子、页面过渡） |
| Axios | HTTP 请求（JWT 拦截器） |
| Web Audio API | 浏览器原生音效合成 |

## 目录结构

```
src/
├── api/                    ← 后端 API 调用封装
│   ├── request.js          ★ Axios 实例 + JWT 注入 + 401 拦截跳转
│   ├── auth.js             ← 登录/注册
│   ├── bill.js             ← 账单 CRUD
│   ├── category.js         ← 分类查询
│   └── statistics.js       ← 仪表盘 + 分类统计
│
├── views/                  ← 页面级组件
│   ├── AuthPage.vue        ★ 登录注册页（极光动效 + 3D 收据展示）
│   └── FinanceDashboard.vue ★ 主仪表盘（AI 洞察 + KPI + 图表 + 账单列表）
│
├── components/             ← 可复用组件
│   ├── InteractiveReceipt.vue ★ 3D 小票（Three.js 布料物理 + Canvas 纹理）
│   ├── CoinParticles.vue   ← 金币粒子动画
│   ├── BackgroundParticles.vue ← 背景粒子系统
│   ├── ChartPanel.vue      ← ECharts 环形图面板
│   ├── MetricCard.vue      ← 数据指标卡片（数值动画 + 脉冲效果）
│   ├── StatCard.vue        ← KPI 卡片容器
│   ├── BillList.vue        ← 账单列表
│   ├── BillForm.vue        ← 账单表单
│   ├── TransactionRow.vue  ← 账单行
│   ├── AddTransactionModal.vue ← 新增账单弹窗
│   ├── ConfirmModal.vue    ← 确认弹窗
│   └── Sidebar.vue         ← 侧边栏导航（7 个菜单项）
│
├── i18n/                   ← 国际化
│   ├── locale.js           ← 语言切换逻辑
│   └── messages.js         ← 中/英文字典（含 AI 洞察、预算、KPI 等文案）
│
├── utils/
│   └── sounds.js           ← Web Audio API 金币音效
│
├── App.vue                 ★ 根组件（登录态判断 + 布局切换）
├── main.js                 ← Vue 应用入口
├── style.css               ← 全局样式
└── tokens.css              ← 设计令牌（CSS 变量）
```

## 与后端的关系

- **开发环境**：Vite 代理 `/api` → `http://localhost:8080`（配置在 `vite.config.js`）
- **生产环境**：Nginx 代理 `/api` → `backend:8080`（配置在 `../nginx.conf`）
- **认证方式**：`request.js` 请求拦截器自动从 `localStorage.pf_user` 读取 JWT token，附加 `Authorization: Bearer <token>` 头
- **401 处理**：响应拦截器检测到 401 自动清除登录态并刷新页面
- **JSON 格式**：后端使用 snake_case（如 `consume_date`），前端直接用下划线字段名访问
- **Windows 兼容**：`vite.config.js` 将 Vite 缓存目录重定向到系统临时目录，避免 Windows 文件权限问题

## 仪表盘功能模块

主仪表盘（`FinanceDashboard.vue`）包含以下模块：

| 模块 | 说明 | 数据来源 |
|------|------|---------|
| AI 智能洞察 | 根据储蓄率、最大支出类别、收支比生成 3-4 条洞察文案 | 前端本地计算（基于真实账单数据） |
| KPI 卡片 | 总资产、本月收入、本月支出、净现金流、储蓄率、净资产 | 收入/支出/储蓄率来自后端 API；总资产、净资产为前端演示数据 |
| 收支对比面积图 | 本月每日收入与支出趋势 | 前端模拟数据（后端暂无每日趋势接口） |
| 分类支出环形图 | 按分类统计支出占比 | 后端 API `/api/statistics/expense-by-category` |
| 储蓄率进度条 | 储蓄率百分比 + 等级评价（优秀/良好/一般/偏低） | 前端本地计算 |
| 账单列表 | 最近 8 条交易记录，支持类型筛选 | 后端 API `/api/bills` |
| 即将到期账单 | 固定支出提醒（房租、订阅等） | 前端硬编码演示数据 |
| 预算进度 | 分类预算使用进度条 | 前端硬编码演示数据 |
| 3D 收据 | Three.js 布料物理模拟的交互式小票 | 基于真实账单数据渲染 |

## 独立运行

如果不启动后端，前端仍可启动（`npm run dev`），但所有 API 请求会返回 500。如需完整体验：

```bash
# 终端 1：启动后端
cd .. && mvn spring-boot:run

# 终端 2：启动前端
npm run dev
```

## UI 亮点

- **AI 智能洞察**（`FinanceDashboard.vue`）：根据储蓄率、最大支出类别、收入支出比自动生成个性化财务建议，支持 4 种语气等级
- **KPI 卡片动画**（`MetricCard.vue`）：数值变化时脉冲 + 弹跳动画，显示涨跌差值，货币符号自动格式化
- **3D 收据**（`InteractiveReceipt.vue`）：用 Three.js 渲染一张可拖拽的纸质小票，布料物理引擎模拟纸张飘动，Canvas 动态绘制账务数据
- **双图表联动**：收支对比面积图（ECharts line area）+ 分类支出环形图（ECharts donut），从不同维度展示消费结构
- **储蓄率进度条**：根据收支比展示储蓄率，分四个等级（优秀/良好/一般/偏低），颜色和文案同步变化
- **金币音效**（`sounds.js`）：用 Web Audio API 合成硬币碰撞声，新增账单时触发
- **粒子系统**（`BackgroundParticles.vue` + `CoinParticles.vue`）：Canvas 粒子动效作为背景装饰和操作反馈
- **中英双语**：登录页和仪表盘均支持中/英文切换，AI 洞察、预算、KPI 等文案均有对应翻译
- **侧边栏导航**（`Sidebar.vue`）：7 个菜单项（仪表盘 / 交易记录 / 预算 / 分析 / 投资 / AI 助手 / 设置），带图标和徽章
