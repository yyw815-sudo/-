import request from '@/utils/request'

export function sendCode(phone) {
  return request({
    url: '/auth/send-code',
    method: 'post',
    data: { phone }
  })
}

export function phoneLogin(data) {
  return request({
    url: '/auth/phone-login',
    method: 'post',
    data
  })
}

export function resetPassword(data) {
  return request({
    url: '/auth/reset-password',
    method: 'post',
    data
  })
}
