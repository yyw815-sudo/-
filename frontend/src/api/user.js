import api from './request'

/**
 * 用户相关API
 */

// 用户登录
export const login = (data) => {
  return api.post('/auth/login', data)
}

// 用户注册
export const register = (data) => {
  return api.post('/auth/register', data)
}

// 获取用户信息
export const getUserInfo = () => {
  return api.get('/users/info')
}

// 更新用户信息
export const updateUser = (data) => {
  return api.put('/users', data)
}

// 获取所有用户（管理员）
export const getAllUsers = () => {
  return api.get('/users')
}

// 删除用户（管理员）
export const deleteUser = (id) => {
  return api.delete(`/users/${id}`)
}
