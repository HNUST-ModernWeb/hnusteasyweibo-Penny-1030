<template>
  <div id="app">
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="nav-container">
        <div class="logo" @click="$router.push('/')">
          📱 社交分享平台
        </div>
        <div class="nav-actions">
          <el-button type="primary" @click="showShareDialog = true" v-if="isLoggedIn">
            <el-icon><Plus /></el-icon> 发布分享
          </el-button>
          <div class="user-menu" v-if="isLoggedIn">
            <el-avatar :size="40" :src="user?.avatar || defaultAvatar" />
            <el-dropdown @command="handleCommand">
              <span class="username">{{ user?.username }}</span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div v-else>
            <el-button @click="showLoginDialog = true">登录</el-button>
            <el-button type="primary" @click="showRegisterDialog = true">注册</el-button>
          </div>
        </div>
      </div>
    </nav>

    <!-- 路由视图 -->
    <router-view />

    <!-- 发布分享对话框 -->
    <el-dialog v-model="showShareDialog" title="发布分享" width="600px">
      <el-form :model="newShare">
        <el-form-item>
          <el-input 
            type="textarea" 
            v-model="newShare.content" 
            :rows="5" 
            placeholder="分享你的想法..." 
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            action="http://localhost:8080/api/shares/upload"
            :headers="{ Authorization: `Bearer ${token}` }"
            list-type="picture-card"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemoveImage"
            :limit="9"
            multiple>
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="可见性">
          <el-radio-group v-model="newShare.visibility">
            <el-radio value="PUBLIC">🌍 公开</el-radio>
            <el-radio value="LOGIN_ONLY">🔒 仅登录用户</el-radio>
            <el-radio value="PRIVATE">🔐 仅自己</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showShareDialog = false">取消</el-button>
        <el-button type="primary" @click="publishShare" :loading="publishing">发布</el-button>
      </template>
    </el-dialog>

    <!-- 登录对话框 -->
    <el-dialog v-model="showLoginDialog" title="登录" width="400px">
      <el-form :model="loginForm" @submit.prevent="login">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input type="password" v-model="loginForm.password" placeholder="请输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showLoginDialog = false">取消</el-button>
        <el-button type="primary" @click="login" :loading="loginLoading">登录</el-button>
      </template>
    </el-dialog>

    <!-- 注册对话框 -->
    <el-dialog v-model="showRegisterDialog" title="注册" width="400px">
      <el-form :model="registerForm" @submit.prevent="register">
        <el-form-item label="用户名">
          <el-input v-model="registerForm.username" placeholder="用户名长度3-20" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input type="password" v-model="registerForm.password" placeholder="密码长度6-20" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegisterDialog = false">取消</el-button>
        <el-button type="primary" @click="register" :loading="registerLoading">注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from './stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

// 对话框显示状态
const showShareDialog = ref(false)
const showLoginDialog = ref(false)
const showRegisterDialog = ref(false)
const publishing = ref(false)
const loginLoading = ref(false)
const registerLoading = ref(false)

// 表单数据
const newShare = ref({
  content: '',
  visibility: 'PUBLIC',
  images: []
})

const loginForm = ref({ username: '', password: '' })
const registerForm = ref({ username: '', password: '' })

// 计算属性
const isLoggedIn = computed(() => userStore.isLoggedIn)
const user = computed(() => userStore.user)
const token = computed(() => userStore.token)
const defaultAvatar = 'https://picsum.photos/40/40'

// 图片上传处理
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    newShare.value.images.push(response.data)
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败')
  }
}

const handleRemoveImage = (file) => {
  const url = file.response?.data || file.url
  const index = newShare.value.images.indexOf(url)
  if (index > -1) {
    newShare.value.images.splice(index, 1)
  }
}

// 发布分享
const publishShare = async () => {
  if (!newShare.value.content.trim()) {
    ElMessage.warning('请输入分享内容')
    return
  }
  
  publishing.value = true
  try {
    await userStore.publishShare(newShare.value)
    ElMessage.success('发布成功')
    showShareDialog.value = false
    newShare.value = { content: '', visibility: 'PUBLIC', images: [] }
    router.go(0)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '发布失败')
  } finally {
    publishing.value = false
  }
}

// 登录
const login = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  
  loginLoading.value = true
  try {
    await userStore.login(loginForm.value)
    ElMessage.success('登录成功')
    showLoginDialog.value = false
    loginForm.value = { username: '', password: '' }
    router.push('/')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '登录失败')
  } finally {
    loginLoading.value = false
  }
}

// 注册
const register = async () => {
  if (!registerForm.value.username || !registerForm.value.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  if (registerForm.value.username.length < 3 || registerForm.value.username.length > 20) {
    ElMessage.warning('用户名长度必须在3-20之间')
    return
  }
  if (registerForm.value.password.length < 6 || registerForm.value.password.length > 20) {
    ElMessage.warning('密码长度必须在6-20之间')
    return
  }
  
  registerLoading.value = true
  try {
    await userStore.register(registerForm.value)
    ElMessage.success('注册成功，请登录')
    showRegisterDialog.value = false
    registerForm.value = { username: '', password: '' }
    showLoginDialog.value = true
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '注册失败')
  } finally {
    registerLoading.value = false
  }
}

// 下拉菜单命令
const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/')
    ElMessage.success('已退出登录')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

onMounted(() => {
  userStore.checkAuth()
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f5f5;
}

#app {
  min-height: 100vh;
}

.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  z-index: 1000;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 12px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  cursor: pointer;
  color: #409eff;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #333;
}

.main-container {
  max-width: 800px;
  margin: 80px auto 40px;
  padding: 0 20px;
}
</style>