import request from '@/utils/request'

export const aiCenterApi = {
  getStatistics() {
    return request({
      url: '/ai/statistics',
      method: 'get'
    })
  },

  getMedicationPlanList(params) {
    return request({
      url: '/ai/medication-plans',
      method: 'get',
      params
    })
  },

  getMedicationPlanDetail(id) {
    return request({
      url: `/ai/medication-plans/${id}`,
      method: 'get'
    })
  },

  createMedicationPlan(data) {
    return request({
      url: '/ai/medication-plans',
      method: 'post',
      data
    })
  },

  updateMedicationPlan(id, data) {
    return request({
      url: `/ai/medication-plans/${id}`,
      method: 'put',
      data
    })
  },

  deleteMedicationPlan(id) {
    return request({
      url: `/ai/medication-plans/${id}`,
      method: 'delete'
    })
  },

  getOcrReviewList(params) {
    return request({
      url: '/ai/ocr-reviews',
      method: 'get',
      params
    })
  },

  getOcrReviewDetail(id) {
    return request({
      url: `/ai/ocr-reviews/${id}`,
      method: 'get'
    })
  },

  reviewOcr(id, data) {
    return request({
      url: `/ai/ocr-reviews/${id}/review`,
      method: 'put',
      data
    })
  },

  deleteOcrReview(id) {
    return request({
      url: `/ai/ocr-reviews/${id}`,
      method: 'delete'
    })
  },

  getPillReviewList(params) {
    return request({
      url: '/ai/pill-reviews',
      method: 'get',
      params
    })
  },

  getPillReviewDetail(id) {
    return request({
      url: `/ai/pill-reviews/${id}`,
      method: 'get'
    })
  },

  reviewPill(id, data) {
    return request({
      url: `/ai/pill-reviews/${id}/review`,
      method: 'put',
      data
    })
  },

  deletePillReview(id) {
    return request({
      url: `/ai/pill-reviews/${id}`,
      method: 'delete'
    })
  }
}