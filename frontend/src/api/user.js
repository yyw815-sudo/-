import request from '@/utils/request'

// 用户登录
export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

// 管理员登录
export function adminLogin(data) {
  return request({
    url: '/admin/login',
    method: 'post',
    data
  })
}

// 用户注册
export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

// 获取用户信息
export function getUserInfo(userId) {
  return request({
    url: `/user/${userId}`,
    method: 'get'
  })
}

// 获取管理员信息
export function getAdminInfo(adminId) {
  return request({
    url: `/admin/info/${adminId}`,
    method: 'get'
  })
}

// 更新用户信息
export function updateUserInfo(userId, data) {
  return request({
    url: `/user/${userId}`,
    method: 'put',
    data
  })
}

// 发送短信验证码
export function sendCode(phone) {
  return request({
    url: '/user/send-code',
    method: 'post',
    data: { phone }
  })
}

// 手机号登录
export function phoneLogin(data) {
  return request({
    url: '/user/phone-login',
    method: 'post',
    data
  })
}