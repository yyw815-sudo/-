import request from '@/utils/request'

export function getUserList(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

export function getUserDetail(userId) {
  return request({
    url: `/admin/users/${userId}`,
    method: 'get'
  })
}

export function addUser(data) {
  return request({
    url: '/admin/users',
    method: 'post',
    data
  })
}

export function updateUser(userId, data) {
  return request({
    url: `/admin/users/${userId}`,
    method: 'put',
    data
  })
}

export function deleteUser(userId) {
  return request({
    url: `/admin/users/${userId}`,
    method: 'delete'
  })
}

export function resetUserPassword(userId, newPassword) {
  return request({
    url: `/admin/users/${userId}/reset-password`,
    method: 'put',
    data: { newPassword }
  })
}

export function updateAdminProfile(adminId, data) {
  return request({
    url: `/admin/profile/${adminId}`,
    method: 'put',
    data
  })
}

export function deleteAdminAccount(adminId) {
  return request({
    url: `/admin/account/${adminId}`,
    method: 'delete'
  })
}

export function getAdminList(params) {
  return request({
    url: '/admin/admins',
    method: 'get',
    params
  })
}

export function addAdmin(data) {
  return request({
    url: '/admin/admins',
    method: 'post',
    data
  })
}

export function updateAdmin(adminId, data) {
  return request({
    url: `/admin/admins/${adminId}`,
    method: 'put',
    data
  })
}

export function deleteAdmin(adminId) {
  return request({
    url: `/admin/admins/${adminId}`,
    method: 'delete'
  })
}

export function resetAdminPassword(adminId, newPassword) {
  return request({
    url: `/admin/admins/${adminId}/reset-password`,
    method: 'put',
    data: { newPassword }
  })
}
