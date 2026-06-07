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
| ECharts 6 | 数据可视化（饼图、旭日图、折线图） |
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
│   └── FinanceDashboard.vue ★ 主仪表盘（KPI + 图表 + 账单列表）
│
├── components/             ← 可复用组件
│   ├── InteractiveReceipt.vue ★ 3D 小票（Three.js 布料物理 + Canvas 纹理）
│   ├── CoinParticles.vue   ← 金币粒子动画
│   ├── BackgroundParticles.vue ← 背景粒子系统
│   ├── ChartPanel.vue      ← ECharts 图表面板
│   ├── MetricCard.vue      ← 数据指标卡片
│   ├── BillList.vue        ← 账单列表
│   ├── BillForm.vue        ← 账单表单
│   ├── TransactionRow.vue  ← 账单行
│   ├── AddTransactionModal.vue ← 新增账单弹窗
│   ├── ConfirmModal.vue    ← 确认弹窗
│   ├── Sidebar.vue         ← 侧边栏导航
│   └── StatCard.vue        ← 统计卡片
│
├── i18n/                   ← 国际化
│   ├── locale.js           ← 语言切换逻辑
│   └── messages.js         ← 中/英文字典
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

## 独立运行

如果不启动后端，前端仍可启动（`npm run dev`），但所有 API 请求会返回 500。如需完整体验：

```bash
# 终端 1：启动后端
cd .. && mvn spring-boot:run

# 终端 2：启动前端
npm run dev
```

## UI 亮点

- **3D 收据**（`InteractiveReceipt.vue`）：用 Three.js 渲染一张可拖拽的纸质小票，布料物理引擎模拟纸张飘动，Canvas 动态绘制账务数据
- **金币音效**（`sounds.js`）：用 Web Audio API 合成硬币碰撞声，新增账单时触发
- **粒子系统**（`BackgroundParticles.vue` + `CoinParticles.vue`）：Canvas 粒子动效作为背景装饰和操作反馈
- **储蓄率进度条**：根据收支比展示储蓄率，分四个等级（优秀/良好/一般/偏低）
- **中英双语**：登录页和仪表盘均支持中/英文切换
