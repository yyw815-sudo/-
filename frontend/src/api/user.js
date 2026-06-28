import request from '@/utils/request'

export function login(data) {
  return request.post('/user/login', data)
}

export function register(data) {
  return request.post('/user/register', data)
}

export function getUserInfo(userId) {
  return request.get(`/user/${userId}`)
}

export function updateUserInfo(data) {
  return request.put('/user/profile', data)
}
