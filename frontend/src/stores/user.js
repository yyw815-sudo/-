import { defineStore } from 'pinia'
import { login as loginApi, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),
  
  getters: {
    isLoggedIn: state => !!state.token,
    userId: state => state.userInfo?.userId
  },
  
  actions: {
    // 登录
    async login(loginData) {
      try {
        const res = await loginApi(loginData)
        this.token = res.data.token
        this.userInfo = {
          userId: res.data.userId,
          userName: res.data.userName,
          realname: res.data.realname
        }
        localStorage.setItem('token', this.token)
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
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
    
    // 退出登录
    logout() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})