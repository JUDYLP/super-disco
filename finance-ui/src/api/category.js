import request from './request'

export const fetchCategories = (params = {}) => {
  return request.get('/categories', { params })
}

export const createCategory = (data) => {
  return request.post('/categories', data)
}
