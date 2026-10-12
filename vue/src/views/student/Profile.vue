<template>
  <div class="profile-page">
    <!-- 顶部个人信息总览 -->
    <section class="card profile-header">
      <div class="profile-header-left">
        <div class="profile-avatar-wrapper">
          <el-avatar
            :size="72"
            :src="data.form.avatar || data.user.avatar || defaultAvatar"
          />
        </div>
        <div class="profile-basic">
          <div class="profile-name">{{ data.form.name || data.user.name || '未设置姓名' }}</div>
          <div class="profile-tags">
            <el-tag size="small" type="success" effect="light">学生端</el-tag>
            <el-tag v-if="data.form.studentNo" size="small" effect="plain">
              学号 {{ data.form.studentNo }}
            </el-tag>
            <el-tag v-if="data.form.clazz" size="small" effect="plain">
              {{ data.form.grade ? data.form.grade + ' 级 · ' : '' }}{{ data.form.clazz }}
            </el-tag>
          </div>
        </div>
      </div>

      <div class="profile-header-right">
        <div class="meta-item">
          <div class="meta-label">账号</div>
          <div class="meta-value">{{ data.form.username || data.user.username || '-' }}</div>
        </div>
        <div class="meta-item">
          <div class="meta-label">角色</div>
          <div class="meta-value">学生（{{ data.user.role || 'STUDENT' }}）</div>
        </div>
      </div>
    </section>

    <!-- 主体：资料编辑表单 -->
    <section class="profile-main">
      <div class="card profile-form-card">
        <div class="section-header">
          <div class="section-title">基本信息</div>
          <div class="section-subtitle">更新头像和个人资料，确保学籍与成绩统计信息准确。</div>
        </div>

        <el-form :model="data.form" label-width="90px" class="profile-form">
          <el-form-item label="头像">
            <div class="avatar-upload">
              <el-avatar
                :size="60"
                :src="data.form.avatar || data.user.avatar || defaultAvatar"
              />
              <el-upload
                :action="uploadUrl"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleImgSuccess"
              >
                <el-button type="primary" plain>更换头像</el-button>
              </el-upload>
            </div>
          </el-form-item>

          <el-row :gutter="16">
            <el-col :xs="24" :sm="12">
              <el-form-item label="账号">
                <el-input v-model="data.form.username" disabled />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="姓名">
                <el-input v-model="data.form.name" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :xs="24" :sm="12">
              <el-form-item label="学号">
                <el-input v-model="data.form.studentNo" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="年级">
                <el-input v-model="data.form.grade" placeholder="如：2022" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :xs="24" :sm="12">
              <el-form-item label="专业">
                <el-input v-model="data.form.major" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="班级">
                <el-input v-model="data.form.clazz" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item>
            <el-button type="primary" @click="save">保 存</el-button>
          </el-form-item>
        </el-form>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { authHeaders, setUser } from '@/utils/auth'

const emit = defineEmits(['updateUser'])

const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'
// el-upload 直连后端，不经过 axios 拦截器，需手动带上鉴权头
const uploadHeaders = authHeaders()

const defaultAvatar =
  'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  form: {},
})

const load = () => {
  if (!data.user?.id) return
  request.get('/student/selectById/' + data.user.id).then((res) => {
    data.form = res.data || {}
  })
}

const handleImgSuccess = (res) => {
  data.form.avatar = res.data
}

const save = () => {
  request.put('/student/update', data.form).then((res) => {
    if (res.code === '200') {
      ElMessage.success('保存成功')
      const newUser = { ...data.user, ...data.form }
      data.user = newUser
      setUser(newUser)
      emit('updateUser')
    } else {
      ElMessage.error(res.msg)
    }
  })
}

load()
</script>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.profile-header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.profile-basic {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.profile-name {
  font-size: 20px;
  font-weight: 600;
}

.profile-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.profile-header-right {
  display: flex;
  gap: 24px;
}

.meta-item {
  text-align: right;
}

.meta-label {
  font-size: 12px;
  color: #9ca3af;
}

.meta-value {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.profile-main {
  display: flex;
}

.profile-form-card {
  flex: 1;
}

.section-header {
  margin-bottom: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
}

.section-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #6b7280;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 16px;
}

.profile-form {
  max-width: 720px;
}

.card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .profile-header-right {
    width: 100%;
    justify-content: flex-start;
    gap: 32px;
  }

  .meta-item {
    text-align: left;
  }
}
</style>
