import request from '@/utils/request'

export const familyApi = {
  getFamilyMembers(userId) {
    return request.get('/family/members', { params: { userId } })
  },

  searchFamilyMembers(userId, keyword) {
    return request.get('/family/members/search', { params: { userId, keyword } })
  },

  getFamilyMember(userId, memberId) {
    return request.get(`/family/members/${memberId}`, { params: { userId } })
  },

  addFamilyMember(userId, data) {
    return request.post('/family/members', data, { params: { userId } })
  },

  updateFamilyMember(userId, memberId, data) {
    return request.put(`/family/members/${memberId}`, data, { params: { userId } })
  },

  deleteFamilyMember(userId, memberId) {
    return request.delete(`/family/members/${memberId}`, { params: { userId } })
  },

  getFamilyAuth(userId) {
    return request.get('/family/auth', { params: { userId } })
  },

  getFamilyAuthByMember(userId, memberId) {
    return request.get(`/family/auth/${memberId}`, { params: { userId } })
  },

  updateFamilyAuth(userId, data) {
    return request.put('/family/auth', data, { params: { userId } })
  },

  getFamilyReminders(userId, memberId) {
    return request.get(`/family/reminders/${memberId}`, { params: { userId } })
  },

  getAllFamilyReminders(userId) {
    return request.get('/family/reminders', { params: { userId } })
  }
}