import { defineStore } from 'pinia'
import api from '../api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: null
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token
  },
  
  actions: {
    async login(credentials) {
      const res = await api.post('/auth/login', credentials)
      if (res.data.code === 200) {
        this.token = res.data.data
        localStorage.setItem('token', this.token)
        await this.getUserInfo()
        return true
      }
      throw new Error(res.data.message)
    },
    
    async register(userData) {
      const res = await api.post('/auth/register', userData)
      if (res.data.code !== 200) {
        throw new Error(res.data.message)
      }
      return res.data
    },
    
    async getUserInfo() {
      try {
        const res = await api.get('/users/me')
        if (res.data.code === 200) {
          this.user = res.data.data
        }
      } catch (error) {
        console.error('获取用户信息失败', error)
      }
    },
    
    async publishShare(shareData) {
      const res = await api.post('/shares', shareData)
      if (res.data.code !== 200) {
        throw new Error(res.data.message)
      }
      return res.data
    },
    
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
    },
    
    checkAuth() {
      if (this.token) {
        this.getUserInfo()
      }
    }
  }
})