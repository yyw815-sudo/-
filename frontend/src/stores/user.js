import { defineStore } from 'pinia'
import { login as loginApi, adminLogin as adminLoginApi, getUserInfo, getAdminInfo } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
    role: localStorage.getItem('role') || 'user' // 用户角色
  }),
  
  getters: {
    isLoggedIn: state => !!state.token,
    userId: state => state.userInfo?.userId,
    isAdmin: state => state.role === 'admin'
  },
  
  actions: {
    // 用户登录
    async login(loginData) {
      try {
        const res = await loginApi(loginData)
        this.token = res.data.token
        this.role = 'user'
        this.userInfo = {
          userId: res.data.userId,
          userName: res.data.userName,
          realname: res.data.realname
        }
        localStorage.setItem('token', this.token)
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        localStorage.setItem('role', this.role)
        return res
      } catch (error) {
        throw error
      }
    },
    
    // 管理员登录
    async adminLogin(loginData) {
      try {
        const res = await adminLoginApi(loginData)
        this.token = res.data.token
        this.role = 'admin'
        this.userInfo = {
          adminId: res.data.adminId,
          adminName: res.data.adminName,
          realname: res.data.realname
        }
        localStorage.setItem('token', this.token)
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        localStorage.setItem('role', this.role)
        return res
      } catch (error) {
        throw error
      }
    },
    
    // 获取用户信息
    async fetchUserInfo(userId) {
      try {
        const res = await getUserInfo(userId)
        this.userInfo = res.data
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        return res
      } catch (error) {
        throw error
      }
    },
    
    // 获取管理员信息
    async fetchAdminInfo(adminId) {
      try {
        const res = await getAdminInfo(adminId)
        this.userInfo = res.data
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        return res
      } catch (error) {
        throw error
      }
    },
    
    // 退出登录
    logout() {
      this.token = ''
      this.userInfo = {}
      this.role = 'user'
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('role')
    }
  }
})