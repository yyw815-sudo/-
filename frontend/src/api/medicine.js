import request from '@/utils/request'

export function getMedicineList() {
  return request.get('/medicine/list')
}

export function getMedicineById(id) {
  return request.get(`/medicine/${id}`)
}

export function searchMedicine(keyword) {
  return request.get('/medicine/search', { params: { keyword } })
}
