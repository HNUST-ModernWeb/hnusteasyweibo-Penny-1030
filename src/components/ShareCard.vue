<template>
  <div class="share-card">
    <div class="card-header">
      <div class="user-info">
        <el-avatar :size="48" :src="share.user?.avatar || defaultAvatar" />
        <div class="user-detail">
          <div class="username">{{ share.user?.username }}</div>
          <div class="time">{{ formatTime(share.createTime) }}</div>
        </div>
      </div>
      <el-dropdown v-if="canEdit" @command="handleCommand">
        <el-icon><MoreFilled /></el-icon>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="delete">删除</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    
    <div class="card-content">
      <p>{{ share.content }}</p>
    </div>
    
    <div class="card-images" v-if="share.images && share.images.length">
      <div class="image-grid" :class="`grid-${Math.min(share.images.length, 3)}`">
        <img 
          v-for="(img, idx) in share.images.slice(0, 9)" 
          :key="idx"
          :src="img.imageUrl"
          @click="previewImage(img.imageUrl)"
          class="share-image"
        />
      </div>
    </div>
    
    <div class="card-actions">
      <div class="action-item" @click="toggleLike">
        <el-icon :color="share.liked ? '#f56c6c' : ''">
          <StarFilled v-if="share.liked" />
          <Star v-else />
        </el-icon>
        <span>{{ share.likeCount || 0 }}</span>
      </div>
      <div class="action-item" @click="toggleComments">
        <el-icon><ChatLineRound /></el-icon>
        <span>{{ comments.length }}</span>
      </div>
    </div>
    
    <div class="comments-section" v-if="showComments">
      <div class="comment-input">
        <el-input 
          v-model="newComment" 
          placeholder="写下你的评论..."
          @keyup.enter="submitComment">
          <template #append>
            <el-button @click="submitComment" :loading="commentLoading">发送</el-button>
          </template>
        </el-input>
      </div>
      
      <div class="comment-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-user">
            <el-avatar :size="32" :src="comment.user?.avatar || defaultAvatar" />
            <div class="comment-content">
              <div class="comment-name">{{ comment.user?.username }}</div>
              <div class="comment-text">{{ comment.content }}</div>
              <div class="comment-time">{{ formatTime(comment.createTime) }}</div>
              <div class="comment-reply" @click="replyTo(comment)">回复</div>
            </div>
          </div>
          
          <div class="replies" v-if="comment.replies && comment.replies.length">
            <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
              <div class="reply-user">
                <el-avatar :size="24" :src="reply.user?.avatar || defaultAvatar" />
                <div class="reply-content">
                  <div class="reply-name">
                    {{ reply.user?.username }}
                    <span v-if="reply.parentId !== comment.id">回复 @{{ getReplyToUsername(reply) }}</span>
                  </div>
                  <div class="reply-text">{{ reply.content }}</div>
                  <div class="reply-time">{{ formatTime(reply.createTime) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, StarFilled, ChatLineRound, MoreFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import api from '../api'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const props = defineProps({
  share: Object
})

const emit = defineEmits(['delete'])

const userStore = useUserStore()
const showComments = ref(false)
const newComment = ref('')
const comments = ref([])
const replyTarget = ref(null)
const commentLoading = ref(false)
const defaultAvatar = 'https://picsum.photos/40/40'

const canEdit = computed(() => {
  return userStore.user?.id === props.share.userId || userStore.user?.role === 'ROLE_ADMIN'
})

const formatTime = (time) => {
  if (!time) return '刚刚'
  return dayjs(time).fromNow()
}

const toggleLike = async () => {
  try {
    await api.post(`/shares/${props.share.id}/like`)
    props.share.liked = !props.share.liked
    props.share.likeCount = (props.share.likeCount || 0) + (props.share.liked ? 1 : -1)
  } catch (error) {
    ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
  }
}

const toggleComments = async () => {
  showComments.value = !showComments.value
  if (showComments.value && comments.value.length === 0) {
    await loadComments()
  }
}

const loadComments = async () => {
  try {
    const res = await api.get(`/shares/${props.share.id}/comments`)
    comments.value = res.data.data || []
  } catch (error) {
    ElMessage.error('加载评论失败')
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  commentLoading.value = true
  try {
    const data = { content: newComment.value }
    if (replyTarget.value) {
      data.parentId = replyTarget.value.id
    }
    
    await api.post(`/shares/${props.share.id}/comments`, data)
    newComment.value = ''
    replyTarget.value = null
    await loadComments()
    ElMessage.success('评论成功')
  } catch (error) {
    ElMessage.error('评论失败：' + (error.response?.data?.message || error.message))
  } finally {
    commentLoading.value = false
  }
}

const replyTo = (comment) => {
  replyTarget.value = comment
  newComment.value = `@${comment.user?.username} `
  showComments.value = true
}

const getReplyToUsername = (reply) => {
  const parentComment = comments.value.find(c => c.id === reply.parentId)
  if (parentComment && parentComment.user) {
    return parentComment.user.username
  }
  return ''
}

const previewImage = (url) => {
  window.open(url, '_blank')
}

const handleCommand = async (command) => {
  if (command === 'delete') {
    try {
      await ElMessageBox.confirm('确定要删除这条分享吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await api.delete(`/shares/${props.share.id}`)
      ElMessage.success('删除成功')
      emit('delete', props.share.id)
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }
}
</script>

<style scoped>
.share-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.user-info {
  display: flex;
  gap: 12px;
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 4px;
}

.time {
  font-size: 12px;
  color: #999;
}

.card-content {
  margin-bottom: 16px;
  line-height: 1.6;
}

.card-content p {
  word-wrap: break-word;
  white-space: pre-wrap;
}

.image-grid {
  display: grid;
  gap: 8px;
  margin-bottom: 16px;
}

.grid-1 {
  grid-template-columns: 1fr;
}

.grid-2 {
  grid-template-columns: repeat(2, 1fr);
}

.grid-3 {
  grid-template-columns: repeat(3, 1fr);
}

.share-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.share-image:hover {
  transform: scale(1.02);
}

.card-actions {
  display: flex;
  gap: 24px;
  padding: 12px 0;
  border-top: 1px solid #f0f0f0;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #666;
  transition: color 0.2s;
}

.action-item:hover {
  color: #409eff;
}

.comments-section {
  margin-top: 20px;
}

.comment-input {
  margin-bottom: 20px;
}

.comment-list {
  max-height: 400px;
  overflow-y: auto;
}

.comment-item {
  margin-bottom: 16px;
}

.comment-user {
  display: flex;
  gap: 12px;
}

.comment-content {
  flex: 1;
}

.comment-name {
  font-weight: bold;
  font-size: 14px;
  margin-bottom: 4px;
}

.comment-text {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
  word-wrap: break-word;
}

.comment-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.comment-reply {
  font-size: 12px;
  color: #409eff;
  cursor: pointer;
}

.replies {
  margin-left: 44px;
  margin-top: 12px;
  padding-left: 12px;
  border-left: 2px solid #f0f0f0;
}

.reply-item {
  margin-bottom: 12px;
}

.reply-user {
  display: flex;
  gap: 8px;
}

.reply-content {
  flex: 1;
}

.reply-name {
  font-weight: bold;
  font-size: 13px;
  margin-bottom: 2px;
}

.reply-text {
  font-size: 13px;
  color: #555;
  margin-bottom: 2px;
  word-wrap: break-word;
}

.reply-time {
  font-size: 11px;
  color: #999;
}
</style>