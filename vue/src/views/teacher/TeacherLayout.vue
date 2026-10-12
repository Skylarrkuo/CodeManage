<template>
  <div class="teacher-layout">
    <!-- 顶部导航栏（与学生端统一的 OJ 风格） -->
    <header class="top-nav">
      <!-- 左侧品牌区：点击返回首页 -->
      <div class="brand" @click="goHome">
        <img src="@/assets/imgs/it.png" alt="logo" class="brand-logo" />
        <div class="brand-text">
          <div class="brand-title">CodeManage 程序竞赛管理系统</div>
          <div class="brand-subtitle">教师端 · Management & OJ</div>
        </div>
      </div>

      <!-- 中间主导航：入口尽量扁平化，减少折叠层级 -->
      <nav class="nav-menu">
        <el-menu
          mode="horizontal"
          router
          :default-active="router.currentRoute.value.path"
          background-color="transparent"
          text-color="#333333"
          active-text-color="#409eff"
          :ellipsis="false"
          class="nav-el-menu"
        >
          <el-menu-item index="/teacher/home">首页</el-menu-item>
          <el-menu-item index="/teacher/contest">竞赛管理</el-menu-item>
          <el-menu-item index="/teacher/signup">报名审核</el-menu-item>
          <el-menu-item index="/teacher/problem">题目管理</el-menu-item>
          <el-menu-item index="/teacher/submission">评测记录</el-menu-item>
          <el-menu-item index="/teacher/student">学生管理</el-menu-item>

        </el-menu>
      </nav>

      <!-- 右侧个人区：头像 + 姓名，下拉集中个人相关操作 -->
      <div class="user-info">
        <el-dropdown trigger="click">
          <div class="user-trigger">
            <img
              class="avatar"
              :src="data.user.avatar || defaultAvatar"
              alt="avatar"
            />
            <div class="user-meta">
              <div class="user-name">{{ data.user.name || '教师' }}</div>
              <div class="user-role">教师端</div>
            </div>
            <span class="dropdown-caret">▾</span>
          </div>

          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goPassword">修改密码</el-dropdown-item>
              <el-dropdown-item @click="goProfile">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 主内容区域 -->
    <main class="content-wrapper">
      <router-view @updateUser="updateUser" />
    </main>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import router from '@/router'
import { ElMessage } from 'element-plus'

const defaultAvatar =
  'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
})

if (!data.user?.id) {
  ElMessage.error('请登录！')
  router.push('/login')
} else if (data.user.role !== 'TEACHER') {
  ElMessage.error('当前账号没有教师权限')
  router.push('/login')
}

const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('system-user') || '{}')
}

const logout = () => {
  router.push('/login')
  ElMessage.success('退出成功')
  localStorage.removeItem('system-user')
}

const goHome = () => {
  router.push('/teacher/home')
}

const goPassword = () => {
  router.push('/teacher/password')
}

const goProfile = () => {
  router.push('/teacher/profile')
}
</script>

<style scoped>
.teacher-layout {
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
  column-gap: 24px;
}

.brand {
  display: flex;
  align-items: center;
  cursor: pointer;
  flex-shrink: 0;
}

.brand-logo {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  box-shadow: 0 0 0 2px rgba(248, 250, 252, 0.2);
}

.brand-text {
  margin-left: 10px;
  color: #1f2933;
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

.nav-menu {
  flex: 1;
  display: flex;
  justify-content: center;
  overflow: visible;
}

.nav-el-menu {
  border-bottom: none;
}

.user-info {
  display: flex;
  align-items: center;
  color: #4b5563;
  flex-shrink: 0;
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

.content-wrapper {
  flex: 1;
  background: #f5f7fa;
  padding: 16px;
  box-sizing: border-box;
}

/* 修复顶部导航栏菜单项点击后如果不点击页面其他位置就会一直处于“按下”状态的问题
   同学生端一致，去掉横向菜单项的 focus 背景色，仅保留 hover 和路由激活样式。 */
:deep(.el-menu--horizontal > .el-menu-item:focus) {
  background-color: transparent !important;
}

</style>
