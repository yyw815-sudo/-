import request from '@/utils/request'

export const getMedicationPlans = (userId) => {
  return request.get(`/medicationplan/user/${userId}`)
}

export const getMedicationPlan = (planId) => {
  return request.get(`/medicationplan/${planId}`)
}

export const createMedicationPlan = (data) => {
  return request.post('/medicationplan/create', data)
}

export const updateMedicationPlan = (planId, data) => {
  return request.put(`/medicationplan/${planId}`, data)
}

export const deleteMedicationPlan = (planId) => {
  return request.delete(`/medicationplan/${planId}`)
}

export const getAllMedicines = () => {
  return request.get('/medicationplan/medicines')
}

export const searchMedicines = (keyword) => {
  return request.get('/medicationplan/medicines/search', {
    params: { keyword }
  })
}

export const detectConflicts = (medicineIds) => {
  return request.post('/medicationplan/detect-conflicts', medicineIds)
}

export const getPlanReminderTimes = (planId) => {
  return request.get(`/medicationplan/${planId}/reminder-times`)
}
