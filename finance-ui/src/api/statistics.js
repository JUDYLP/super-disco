import request from './request'

export const fetchDashboard = () => {
  return request.get('/statistics/dashboard')
}

export const fetchExpenseByCategory = () => {
  return request.get('/statistics/expense-by-category')
}
