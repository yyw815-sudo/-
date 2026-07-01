import request from '@/utils/request'

export function getStatistics() {
  return request({
    url: '/system/statistics',
    method: 'get'
  })
}

export function getAnnouncementList(params) {
  return request({
    url: '/system/announcements',
    method: 'get',
    params
  })
}

export function getAnnouncementDetail(id) {
  return request({
    url: `/system/announcements/${id}`,
    method: 'get'
  })
}

export function createAnnouncement(data) {
  return request({
    url: '/system/announcements',
    method: 'post',
    data
  })
}

export function updateAnnouncement(id, data) {
  return request({
    url: `/system/announcements/${id}`,
    method: 'put',
    data
  })
}

export function deleteAnnouncement(id) {
  return request({
    url: `/system/announcements/${id}`,
    method: 'delete'
  })
}

export function getAiConfigList(params) {
  return request({
    url: '/system/ai-configs',
    method: 'get',
    params
  })
}

export function getAiConfigDetail(id) {
  return request({
    url: `/system/ai-configs/${id}`,
    method: 'get'
  })
}

export function createAiConfig(data) {
  return request({
    url: '/system/ai-configs',
    method: 'post',
    data
  })
}

export function updateAiConfig(id, data) {
  return request({
    url: `/system/ai-configs/${id}`,
    method: 'put',
    data
  })
}

export function deleteAiConfig(id) {
  return request({
    url: `/system/ai-configs/${id}`,
    method: 'delete'
  })
}

export function toggleAiConfigStatus(id) {
  return request({
    url: `/system/ai-configs/${id}/toggle`,
    method: 'put'
  })
}