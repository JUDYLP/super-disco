export const messages = {
  zh: {
    /* ── 通用 ── */
    languageName: '中文',
    switchLanguage: '语言',
    appName: '个人财务管理系统',
    login: '登录',
    register: '注册',
    logout: '退出登录',
    signedInAs: '当前登录',
    cancel: '取消',
    delete: '删除',
    save: '保存',
    confirm: '确认',
    processing: '处理中...',
    loading: '加载中...',

    /* ── 问候 ── */
    goodMorning: '早上好',
    goodAfternoon: '下午好',
    goodEvening: '晚上好',
    overviewFor: '以下是你的财务概览',

    /* ── 侧栏 ── */
    sidebarMain: '主要',
    sidebarMore: '更多',
    navDashboard: '仪表盘',
    navTransactions: '交易记录',
    navBudgets: '预算',
    navAnalytics: '分析',
    navInvestments: '投资',
    navAI: 'AI 助手',
    navSettings: '设置',

    /* ── 仪表盘 ── */
    dashboardTitle: '财务仪表盘',
    aiInsights: 'AI 智能洞察',
    updatedJustNow: '刚刚更新',

    /* AI 洞察文案 */
    aiSavingsGood: '你的储蓄率为 {rate}%，财务状况健康，正稳步迈向长期目标。',
    aiSavingsWarn: '你的储蓄率仅为 {rate}%，削减 10% 的弹性支出可显著改善。',
    aiSavingsBad: '储蓄率偏低，建议立即审视主要支出类别，寻找节流空间。',
    aiCategoryWarn: '「{cat}」类别支出占比高达 {pct}%，检查订阅和定期付款可能节省开支。',
    aiIncomeStrong: '收入支出比良好。建议将富余资金分配到投资或应急基金中。',
    aiForecast: '根据当前消费模式，下月调整非必要支出后预计可节省约 {amount}。',

    /* ── KPI ── */
    kpiTotalAssets: '总资产',
    kpiMonthlyIncome: '本月收入',
    kpiMonthlyExpense: '本月支出',
    kpiNetCashFlow: '净现金流',
    kpiSavingsRate: '储蓄率',
    kpiNetWorth: '净资产',
    incInvestments: '含投资',
    thisMonth: '本月',
    incomeMinusExpense: '收入 − 支出',
    incomePctSaved: '收入储蓄占比',
    assetsMinusLiabilities: '资产 − 负债',

    /* ── 储蓄率 ── */
    savingsRateLabel: '储蓄率',
    savingsExcellent: '储蓄状况优秀 — 财富稳健增长中',
    savingsSolid: '储蓄状况良好 — 财务前景明朗',
    savingsFair: '储蓄状况一般 — 小幅调整能带来大改善',
    savingsNeedsAttention: '需要关注 — 支出正在超过收入',

    /* ── 图表 ── */
    incomeVsExpense: '收入 vs 支出',
    expenseByCategory: '支出分类占比',
    totalSpent: '总支出',

    /* ── 筛选 ── */
    all: '全部',
    expenses: '支出',
    income: '收入',
    category: '分类',

    /* ── 交易列表 ── */
    recentTransactions: '最近交易',
    description: '描述',
    date: '日期',
    amount: '金额',
    type: '类型',
    noTransactions: '暂无交易记录',
    addFirstTransaction: '添加第一笔交易',

    /* ── 即将到来账单 ── */
    upcomingBills: '即将到期账单',
    noUpcomingBills: '暂无即将到期的账单',

    /* ── 预算进度 ── */
    budgetProgress: '预算进度',
    budgetNameDining: '餐饮外食',
    budgetNameEntertainment: '娱乐',
    budgetNameGroceries: '生鲜杂货',
    budgetNameShopping: '购物',

    /* ── 交易模态框 ── */
    newTransaction: '新增交易',
    editTransaction: '编辑交易',
    saveChanges: '保存修改',
    addTransaction: '添加交易',
    name: '名称',
    namePlaceholder: '午餐、工资、地铁...',
    note: '备注',
    notePlaceholder: '选填',
    selectCategory: '选择分类',

    /* ── 表单验证 ── */
    validationName: '请输入交易名称。',
    validationCategory: '请选择分类。',
    validationAmount: '请输入大于 0 的金额。',
    validationDate: '请选择日期。',

    /* ── 请求与反馈 ── */
    requestFailed: '请求失败',
    authFailed: '认证失败',
    added: '交易已添加',
    updated: '交易已更新',
    deleted: '交易已删除',
    confirmDeletePrefix: '确认删除交易',
    confirmDeleteSuffix: '？',
    saving: '保存中...',

    /* ── 收支 ── */
    monthlyIncome: '本月收入',
    monthlyExpense: '本月支出',
    monthlyBalance: '本月结余',

    /* ── 认证页 ── */
    authBrandEyebrow: '个人财务管理',
    authBrandTitle: '开启你的智能记账空间',
    authBrandCopy: '追踪每一笔交易，理解消费习惯，用数据驱动财富增长。',
    authSignIn: '登录',
    authCreateAccount: '创建账号',
    authSigningIn: '登录中...',
    authCreating: '创建中...',
    authEmail: '邮箱',
    authPassword: '密码',
    authUsername: '用户名',
    authEmailPlaceholder: 'name@example.com',
    authPasswordPlaceholder: '至少 6 位，包含数字',
    authUsernamePlaceholder: '你的用户名',
    authShow: '显示',
    authHide: '隐藏',
    authSleeping: '闭眼',
    authAwake: '睁眼',
    authPasswordStrength: '密码强度',
    authWeak: '弱',
    authMedium: '中',
    authStrong: '强',
    authMin: '最低',
    authRuleLength: '至少 6 位字符',
    authRuleNumber: '包含数字',
    authRuleLetter: '包含字母',
    authValidationUsername: '请输入用户名。',
    authValidationEmail: '请输入邮箱。',
    authValidationEmailFormat: '请输入正确的邮箱格式。',
    authValidationPassword: '请输入密码。',
    authValidationPasswordLength: '密码不能低于 6 位。',
    authValidationPasswordNumber: '密码必须包含至少 1 个数字。',

    /* ── 确认 ── */
    confirmDeleteMsg: '确认删除此交易？',
  },

  en: {
    /* ── General ── */
    languageName: 'EN',
    switchLanguage: 'Language',
    appName: 'Finance',
    login: 'Sign In',
    register: 'Create Account',
    logout: 'Logout',
    signedInAs: 'Signed in as',
    cancel: 'Cancel',
    delete: 'Delete',
    save: 'Save',
    confirm: 'Confirm',
    processing: 'Processing...',
    loading: 'Loading...',

    /* ── Greeting ── */
    goodMorning: 'Good morning',
    goodAfternoon: 'Good afternoon',
    goodEvening: 'Good evening',
    overviewFor: 'Here\'s your financial overview for',

    /* ── Sidebar ── */
    sidebarMain: 'Main',
    sidebarMore: 'More',
    navDashboard: 'Dashboard',
    navTransactions: 'Transactions',
    navBudgets: 'Budgets',
    navAnalytics: 'Analytics',
    navInvestments: 'Investments',
    navAI: 'AI Assistant',
    navSettings: 'Settings',

    /* ── Dashboard ── */
    dashboardTitle: 'Dashboard',
    aiInsights: 'AI Insights',
    updatedJustNow: 'Updated just now',

    aiSavingsGood: 'Your savings rate is healthy at {rate}%. You\'re on track for long-term financial goals.',
    aiSavingsWarn: 'Your savings rate of {rate}% could improve. Reducing discretionary spending by 10% would boost this significantly.',
    aiSavingsBad: 'Your savings rate is low. Consider reviewing your top expense categories for immediate savings opportunities.',
    aiCategoryWarn: 'Your {cat} spending accounts for {pct}% of total expenses. Reviewing subscriptions or recurring payments in this category could save money.',
    aiIncomeStrong: 'You have a strong income-to-expense ratio. Consider allocating surplus funds to investments or an emergency fund.',
    aiForecast: 'Based on your spending patterns, you could save approximately {amount} next month by adjusting non-essential categories.',

    /* ── KPI ── */
    kpiTotalAssets: 'Total Assets',
    kpiMonthlyIncome: 'Monthly Income',
    kpiMonthlyExpense: 'Monthly Expense',
    kpiNetCashFlow: 'Net Cash Flow',
    kpiSavingsRate: 'Savings Rate',
    kpiNetWorth: 'Net Worth',
    incInvestments: 'Including investments',
    thisMonth: 'This month',
    incomeMinusExpense: 'Income − Expenses',
    incomePctSaved: '% of income saved',
    assetsMinusLiabilities: 'Assets − Liabilities',

    /* ── Savings ── */
    savingsRateLabel: 'Savings Rate',
    savingsExcellent: 'Excellent — you\'re building wealth quickly.',
    savingsSolid: 'Solid — on track for a comfortable financial future.',
    savingsFair: 'Fair — small changes can make a big difference.',
    savingsNeedsAttention: 'Needs attention — expenses are outpacing income.',

    /* ── Charts ── */
    incomeVsExpense: 'Income vs Expense',
    expenseByCategory: 'Expense by Category',
    totalSpent: 'total spent',

    /* ── Filters ── */
    all: 'All',
    expenses: 'Expenses',
    income: 'Income',
    category: 'Category',

    /* ── Transactions ── */
    recentTransactions: 'Recent Transactions',
    description: 'Description',
    date: 'Date',
    amount: 'Amount',
    type: 'Type',
    noTransactions: 'No transactions yet',
    addFirstTransaction: 'Add your first transaction',

    /* ── Upcoming ── */
    upcomingBills: 'Upcoming Bills',
    noUpcomingBills: 'No upcoming bills',

    /* ── Budget ── */
    budgetProgress: 'Budget Progress',
    budgetNameDining: 'Dining Out',
    budgetNameEntertainment: 'Entertainment',
    budgetNameGroceries: 'Groceries',
    budgetNameShopping: 'Shopping',

    /* ── Modal ── */
    newTransaction: 'New Transaction',
    editTransaction: 'Edit Transaction',
    saveChanges: 'Save Changes',
    addTransaction: 'Add Transaction',
    name: 'Name',
    namePlaceholder: 'Lunch, salary, metro...',
    note: 'Note',
    notePlaceholder: 'Optional',
    selectCategory: 'Select category',

    /* ── Validation ── */
    validationName: 'Please enter a name.',
    validationCategory: 'Please select a category.',
    validationAmount: 'Please enter an amount greater than 0.',
    validationDate: 'Please select a date.',

    /* ── Feedback ── */
    requestFailed: 'Request failed',
    authFailed: 'Authentication failed',
    added: 'Transaction added',
    updated: 'Transaction updated',
    deleted: 'Transaction deleted',
    confirmDeletePrefix: 'Delete transaction',
    confirmDeleteSuffix: '?',
    saving: 'Saving...',

    /* ── Income/Expense ── */
    monthlyIncome: 'Monthly Income',
    monthlyExpense: 'Monthly Expense',
    monthlyBalance: 'Monthly Balance',

    /* ── Auth ── */
    authBrandEyebrow: 'Personal Finance',
    authBrandTitle: 'Your financial clarity starts here.',
    authBrandCopy: 'Track every transaction, understand your spending, and build lasting wealth with intelligent insights.',
    authSignIn: 'Sign In',
    authCreateAccount: 'Create Account',
    authSigningIn: 'Signing in...',
    authCreating: 'Creating...',
    authEmail: 'Email',
    authPassword: 'Password',
    authUsername: 'Username',
    authEmailPlaceholder: 'name@example.com',
    authPasswordPlaceholder: 'At least 6 characters with a number',
    authUsernamePlaceholder: 'Your username',
    authShow: 'Show',
    authHide: 'Hide',
    authSleeping: 'Sleep',
    authAwake: 'Awake',
    authPasswordStrength: 'Password strength',
    authWeak: 'Weak',
    authMedium: 'Medium',
    authStrong: 'Strong',
    authMin: 'min',
    authRuleLength: 'At least 6 characters',
    authRuleNumber: 'Contains a number',
    authRuleLetter: 'Contains a letter',
    authValidationUsername: 'Please enter your username.',
    authValidationEmail: 'Please enter your email.',
    authValidationEmailFormat: 'Please enter a valid email.',
    authValidationPassword: 'Please enter your password.',
    authValidationPasswordLength: 'Password must be at least 6 characters.',
    authValidationPasswordNumber: 'Password must contain at least one number.',

    /* ── Confirm ── */
    confirmDeleteMsg: 'Delete this transaction?',
  }
}
