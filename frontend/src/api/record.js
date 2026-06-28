import request from '@/utils/request'

export function getRecordsByUserId(userId) {
  return request.get(`/record/user/${userId}`)
}

export function getRecordById(recordId) {
  return request.get(`/record/${recordId}`)
}

export function getRecordsPage(userId, params) {
  return request.get(`/record/user/${userId}/page`, { params })
}

export function createRecord(data) {
  return request.post('/record', data)
}

export function updateRecord(recordId, data) {
  return request.put(`/record/${recordId}`, data)
}

export function deleteRecord(recordId) {
  return request.delete(`/record/${recordId}`)
}

export function deleteRecordWithPlans(recordId) {
  return request.delete(`/record/${recordId}/cascade`)
}

export function ocrRecognize(formData) {
  return request.post('/record/ocr/recognize', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function ocrParse(data) {
  return request.post('/record/ocr/parse', data)
}
