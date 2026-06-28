import request from '@/utils/request'

export const getReminders = (userId) => {
  return request.get(`/reminder/user/${userId}`)
}

export const createReminder = (data) => {
  return request.post('/reminder/create', data)
}

export const createRemindersFromPlan = (data) => {
  return request.post('/reminder/create-from-plan', data)
}

export const toggleReminder = (reminderId, enabled) => {
  return request.put(`/reminder/${reminderId}/toggle`, null, {
    params: { enabled }
  })
}

export const deleteReminder = (reminderId) => {
  return request.delete(`/reminder/${reminderId}`)
}

export const sendReminder = (reminderId) => {
  return request.post(`/reminder/send/${reminderId}`)
}

export const getReminderLogs = (userId) => {
  return request.get(`/reminder-log/user/${userId}`)
}

export const updateLogStatus = (logId, status, response) => {
  return request.put(`/reminder-log/${logId}/status`, {
    status,
    response
  })
}

export const markAsReceived = (logId) => {
  return request.put(`/reminder-log/${logId}/received`)
}

export const markAsRead = (logId, response) => {
  return request.put(`/reminder-log/${logId}/read`, { response })
}
