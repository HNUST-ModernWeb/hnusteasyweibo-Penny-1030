<template>
  <div class="home main-container">
    <div class="shares-list" v-loading="loading">
      <ShareCard 
        v-for="share in shares" 
        :key="share.id"
        :share="share"
        @delete="removeShare"
      />
      <el-empty v-if="!loading && shares.length === 0" description="暂无分享，快来发布第一条吧！" />
    </div>
    
    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        @current-change="loadShares"
        layout="prev, pager, next"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import ShareCard from '../components/ShareCard.vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const shares = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadShares = async () => {
  loading.value = true
  try {
    const res = await api.get(`/shares?page=${currentPage.value}&size=${pageSize.value}`)
    if (res.data.code === 200) {
      shares.value = res.data.data.records || []
      total.value = res.data.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

const removeShare = (shareId) => {
  shares.value = shares.value.filter(s => s.id !== shareId)
  total.value = total.value - 1
}

onMounted(() => {
  loadShares()
})
</script>

<style scoped>
.home {
  max-width: 800px;
  margin: 80px auto 40px;
  padding: 0 20px;
}

.shares-list {
  margin-bottom: 40px;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
</style>