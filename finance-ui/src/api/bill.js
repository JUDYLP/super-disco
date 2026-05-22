import request from './request'

export const fetchBills = (params = {}) => {
  return request.get('/bills', { params })
}

export const createBill = (data) => {
  return request.post('/bills', data)
}

export const updateBill = (id, data) => {
  return request.put(`/bills/${id}`, data)
}

export const deleteBill = (id) => {
  return request.delete(`/bills/${id}`)
}
