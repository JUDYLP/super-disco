import request from './request'

export const registerUser = (data) => {
  return request.post('/auth/register', data)
}

export const loginUser = (data) => {
  return request.post('/auth/login', data)
}
