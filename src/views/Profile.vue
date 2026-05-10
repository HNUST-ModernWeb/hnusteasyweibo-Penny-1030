<template>
  <div class="profile main-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
        </div>
      </template>
      
      <div class="profile-info">
        <div class="avatar-section">
          <el-avatar :size="100" :src="user?.avatar || defaultAvatar" />
          <h2>{{ user?.username }}</h2>
          <el-tag :type="user?.role === 'ROLE_ADMIN' ? 'danger' : 'primary'">
            {{ user?.role === 'ROLE_ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </div>
        
        <div class="stats">
          <div class="stat-item">
            <div class="stat-number">{{ shareCount }}</div>
            <div class="stat-label">发布的分享</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ commentCount }}</div>
            <div class="stat-label">评论数</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ likeCount }}</div>
            <div class="stat-label">获赞数</div>
          </div>
        </div>
      </div>
    </el-card>
    
    <el-card style="margin-top: 20px">
      <template #header>
        <span>我的分享</span>
      </template>
      <ShareCard 
        v-for="share in myShares" 
        :key="share.id"
        :share="share"
        @delete="removeShare"
      />
      <el-empty v-if="myShares.length === 0" description="暂无分享" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import ShareCard from '../components/ShareCard.vue'
import api from '../api'

const userStore = useUserStore()
const user = ref(null)
const myShares = ref([])
const shareCount = ref(0)
const commentCount = ref(0)
const likeCount = ref(0)
const defaultAvatar = 'https://picsum.photos/100/100'

const loadUserData = async () => {
  try {
    const res = await api.get('/users/me')
    if (res.data.code === 200) {
      user.value = res.data.data
    }
  } catch (error) {
    console.error('加载用户信息失败', error)
  }
}

const loadMyShares = async () => {
  try {
    const res = await api.get('/shares?page=1&size=100')
    if (res.data.code === 200 && res.data.data.records) {
      myShares.value = res.data.data.records.filter(s => s.userId === user.value?.id)
      shareCount.value = myShares.value.length
    }
  } catch (error) {
    console.error('加载分享失败', error)
  }
}

const removeShare = (shareId) => {
  myShares.value = myShares.value.filter(s => s.id !== shareId)
  shareCount.value = myShares.value.length
}

onMounted(async () => {
  await loadUserData()
  await loadMyShares()
})
</script>

<style scoped>
.profile {
  max-width: 800px;
  margin: 80px auto 40px;
  padding: 0 20px;
}

.profile-info {
  text-align: center;
}

.avatar-section {
  margin-bottom: 20px;
}

.avatar-section h2 {
  margin: 10px 0;
}

.stats {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
  border-top: 1px solid #eee;
  margin-top: 20px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}
</style>