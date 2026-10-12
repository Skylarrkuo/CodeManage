<template>
  <div class="admin-layout">
    <!-- 顶部栏（浅色） -->
    <header class="top-nav">
      <div class="brand" @click="goHome">
        <img src="@/assets/imgs/it.png" alt="logo" class="brand-logo" />
        <div class="brand-text">
          <div class="brand-title">CodeManage 程序竞赛管理系统</div>
          <div class="brand-subtitle">管理员端 · System Console</div>
        </div>
      </div>

      <div class="user-info">
        <el-dropdown trigger="click">
          <div class="user-trigger">
            <img
              class="avatar"
              :src="data.user.avatar || defaultAvatar"
              alt="avatar"
            />
            <div class="user-meta">
              <div class="user-name">{{ data.user.name || '管理员' }}</div>
              <div class="user-role">管理员端</div>
            </div>
            <span class="dropdown-caret">▾</span>
          </div>

          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goPassword">修改密码</el-dropdown-item>
              <el-dropdown-item divided @click="logout">退出系统</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 主体区域 -->
    <div class="main-area">
      <!-- 左侧菜单 -->
      <aside class="sidebar">
        <el-menu
          router
          class="sidebar-menu"
          :default-active="router.currentRoute.value.path"
          :default-openeds="['user', 'contest']"
        >
          <el-menu-item index="/admin/home">
            <el-icon><HomeFilled /></el-icon>
            <span>系统首页</span>
          </el-menu-item>

          <el-sub-menu index="user">
            <template #title>
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/admin/admin">
              <el-icon><User /></el-icon>
              <span>管理员信息</span>
            </el-menu-item>
            <el-menu-item index="/admin/teacher">
              <el-icon><User /></el-icon>
              <span>教师信息</span>
            </el-menu-item>
            <el-menu-item index="/admin/student">
              <el-icon><User /></el-icon>
              <span>学生信息</span>
            </el-menu-item>
            <el-menu-item index="/admin/clazz">
              <el-icon><User /></el-icon>
              <span>班级管理</span>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="contest">
            <template #title>
              <span>竞赛与题库</span>
            </template>
            <el-menu-item index="/admin/contest">
              <span>竞赛管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/signup">
              <span>报名管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/problem">
              <span>题库管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/submission">
              <span>提交记录</span>
            </el-menu-item>
          </el-sub-menu>

          <el-menu-item @click="logout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出系统</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <!-- 右侧内容 -->
      <main class="content-wrapper">
        <router-view @updateUser="updateUser" />
      </main>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import router from '@/router'
import { ElMessage } from 'element-plus'

import { clearAuth } from '@/utils/auth'

const defaultAvatar =
  'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
})

if (!data.user?.id) {
  ElMessage.error('请登录！')
  router.push('/login')
} else if (data.user.role !== 'ADMIN') {
  ElMessage.error('当前账号没有管理员权限')
  router.push('/login')
}

const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('system-user') || '{}')
}

const goHome = () => {
  router.push('/admin/home')
}

const goPassword = () => {
  router.push('/admin/password')
}

const logout = () => {
  clearAuth()
  router.push('/login')
  ElMessage.success('退出成功')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f3f4f6;
}

.top-nav {
  min-height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.08);
  border-bottom: 1px solid #e5e7eb;
}

.brand {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.brand-logo {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  box-shadow: 0 0 0 2px rgba(248, 250, 252, 0.2);
}

.brand-text {
  margin-left: 10px;
  color: #111827;
}

.brand-title {
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.brand-subtitle {
  font-size: 12px;
  color: #6b7280;
}

.user-info {
  display: flex;
  align-items: center;
  color: #4b5563;
}

.user-trigger {
  display: flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 999px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.user-trigger:hover {
  background-color: #f3f4f6;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e5e7eb;
}

.user-meta {
  margin-left: 8px;
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
}

.user-role {
  font-size: 12px;
  color: #9ca3af;
}

.dropdown-caret {
  margin-left: 4px;
  font-size: 12px;
  color: #9ca3af;
}

.main-area {
  flex: 1;
  display: flex;
  min-height: calc(100vh - 64px);
}

.sidebar {
  width: 220px;
  background: #ffffff;
  border-right: 1px solid #e5e7eb;
}

.sidebar-menu {
  border-right: none;
}

.content-wrapper {
  flex: 1;
  width: 0;
  background: #f5f7fa;
  padding: 16px;
  box-sizing: border-box;
}

.el-menu-item.is-active {
  background-color: #e5f0ff !important;
  color: #1f2933;
}

.el-menu-item:hover {
  background-color: #f3f4f6 !important;
  color: #1f2933;
}
</style>
