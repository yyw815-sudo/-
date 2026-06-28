import request from '@/utils/request'

/** 获取用户的用药计划列表 */
export function getPlansByUserId(userId) {
  return request.get(`/plan/user/${userId}`)
}

/** 获取病历的用药计划列表 */
export function getPlansByRecordId(recordId) {
  return request.get(`/plan/record/${recordId}`)
}

/** 统计病历的用药计划数 */
export function countPlansByRecordId(recordId) {
  return request.get(`/plan/record/${recordId}/count`)
}

/** AI分析处方并创建计划 */
export function analyzePrescription(userId, recordId) {
  return request.post('/prescription/analyze', { userId, recordId })
}

/** 更新计划 */
export function updatePlan(planId, data) {
  return request.put(`/plan/${planId}`, data)
}

/** 删除计划 */
export function deletePlan(planId) {
  return request.delete(`/plan/${planId}`)
}

/** 获取每日记录（按计划） */
export function getDailyByPlanId(planId) {
  return request.get(`/plan/daily/plan/${planId}`)
}

/** 获取每日记录（按用户和日期） */
export function getDailyByUserIdAndDate(userId, date) {
  return request.get(`/plan/daily/user/${userId}/date/${date}`)
}

/** 获取每日记录（按日期范围） */
export function getDailyByUserIdAndDateRange(userId, startDate, endDate) {
  return request.get(`/plan/daily/user/${userId}/range`, { params: { startDate, endDate } })
}

/** 标记为漏服 */
export function markAsMissed(takeId) {
  return request.post(`/plan/daily/${takeId}/missed`)
}

/** 上传药片照片确认服药 */
export function uploadTakePhoto(takeId, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/plan/daily/${takeId}/photo`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 获取历史计划 */
export function getHistoryPlans(userId, status) {
  return request.get(`/plan/user/${userId}/history/${status}`)
}
