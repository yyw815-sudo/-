import request from '@/utils/request'

/** AI分析处方（单病历） */
export function analyzePrescription(userId, recordId) {
  return request.post('/prescription/analyze', { userId, recordId })
}

/** 获取病历的AI分析结果 */
export function getAnalysisResult(recordId) {
  return request.get(`/prescription/analysis/${recordId}`)
}

/** 获取用户所有AI分析记录 */
export function getAnalysisResultsByUserId(userId) {
  return request.get(`/prescription/analysis/user/${userId}`)
}

/** 多病历冲突检测 */
export function checkConflicts(recordIds) {
  return request.post('/prescription/checkConflicts', { recordIds })
}

/** 获取所有药品 */
export function getMedicineList() {
  return request.get('/medicine/list')
}

/** 按关键字搜索药品 */
export function searchMedicine(keyword) {
  return request.get('/medicine/search', { params: { keyword } })
}
