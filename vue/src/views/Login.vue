<template>
  <div class="login-page">
    <div class="login-shell">
      <!-- 顶部品牌，与学生 / 教师端保持统一风格 -->
      <header class="login-header">
        <div class="brand">
          <img src="@/assets/imgs/it.png" alt="logo" class="brand-logo" />
          <div class="brand-text">
            <div class="brand-title">CodeManage 程序竞赛管理系统</div>
            <div class="brand-subtitle">统一的程序设计竞赛与评测平台</div>
          </div>
        </div>
        <div class="login-header-right">
          <span class="login-header-tag">登录入口</span>
        </div>
      </header>

      <main class="login-layout">
        <!-- 左侧介绍区域，突出平台定位 -->
        <section class="login-side">
          <h1 class="side-title">专注教学与竞赛的在线评测平台</h1>
          <p class="side-desc">
            面向课程作业、课堂测验与竞赛训练的统一评测系统，支持多角色协同管理。
          </p>
          <ul class="side-list">
            <li>· 学生端：题库练习、竞赛报名与成绩查询</li>
            <li>· 教师端：题目管理、竞赛配置与评测监控</li>
            <li>· 管理端：账号管理与系统配置</li>
          </ul>
        </section>

        <!-- 右侧登录卡片 -->
        <section class="login-box">
          <div class="welcome-title">欢 迎 登 录</div>
          <div class="welcome-subtitle">请使用分配的账号密码登录系统</div>

          <el-form :model="data.form" ref="formRef" :rules="data.rules" label-position="top">
            <el-form-item prop="username" label="账号">
              <el-input
                :prefix-icon="User"
                size="large"
                v-model="data.form.username"
                placeholder="请输入账号"
              />
            </el-form-item>
            <el-form-item prop="password" label="密码">
              <el-input
                :prefix-icon="Lock"
                size="large"
                v-model="data.form.password"
                placeholder="请输入密码"
                show-password
              />
            </el-form-item>
            <el-form-item prop="role" label="登录角色">
              <el-select size="large" class="full-width" v-model="data.form.role" placeholder="请选择角色">
                <el-option value="ADMIN" label="管理员"></el-option>
                <el-option value="TEACHER" label="教师"></el-option>
                <el-option value="STUDENT" label="学生"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button size="large" type="primary" class="full-width" @click="login">
                登 录
              </el-button>
            </el-form-item>
          </el-form>

          <div class="quick-login">
            <div class="quick-login-header">
              <span class="quick-login-label">示例账号快捷登录</span>
              <span class="quick-login-tip">一键填充体验账号和密码</span>
            </div>
            <div class="quick-login-buttons">
              <el-button size="small" class="quick-login-btn" @click="quickLogin('ADMIN')">
                <span class="quick-role">管理员</span>
                <span class="quick-account">admin / admin</span>
              </el-button>
              <el-button size="small" class="quick-login-btn" @click="quickLogin('TEACHER')">
                <span class="quick-role">教师</span>
                <span class="quick-account">teacher / 123456</span>
              </el-button>
              <el-button size="small" class="quick-login-btn" @click="quickLogin('STUDENT')">
                <span class="quick-role">学生</span>
                <span class="quick-account">student / 123456</span>
              </el-button>
            </div>
          </div>

          <div class="login-extra">
            还没有账号？请联系管理员开通或
            <router-link class="login-extra-link" to="/register">在线注册</router-link>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
  import { reactive, ref } from "vue";
  import { User, Lock } from "@element-plus/icons-vue";
  import request from "@/utils/request";
  import {ElMessage} from "element-plus";
  import router from "@/router";

  const data = reactive({
    form: { role: 'ADMIN' },
    rules: {
      username: [
        { required: true, message: '请输入账号', trigger: 'blur' },
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
      ],
    }
  })

  const formRef = ref()

  // 点击登录按钮的时候会触发这个方法
  const login = () => {
    formRef.value.validate((valid => {
      if (valid) {
        request.post('/login', data.form).then(res => {
          if (res.code === '200') {
            ElMessage.success('登录成功')
            localStorage.setItem('system-user', JSON.stringify(res.data || {}))

            const role = res.data?.role
            let path = '/login'
            if (role === 'ADMIN') {
              path = '/admin/home'
            } else if (role === 'TEACHER') {
              path = '/teacher/home'
            } else if (role === 'STUDENT') {
              path = '/student/home'
            }
            router.push(path)
          } else {
            ElMessage.error(res.msg)
          }
        })
      }
    })).catch(error => {
      console.error(error)
    })
  }

  const quickAccountMap = {
    ADMIN: { username: 'admin', password: 'admin' },
    TEACHER: { username: 'teacher', password: '123456' },
    STUDENT: { username: 'student', password: '123456' },
  }

  const quickLogin = (role) => {
    const account = quickAccountMap[role]
    if (!account) return
    data.form.username = account.username
    data.form.password = account.password
    data.form.role = role
    login()
  }

</script>

<style scoped>
.login-page {
  min-height: 100vh;
  padding: 32px 16px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.login-shell {
  width: 100%;
  max-width: 1000px;
}

.login-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
  color: #1f2933;
}

.brand {
  display: flex;
  align-items: center;
}

.brand-logo {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  box-shadow: 0 0 0 2px rgba(248, 250, 252, 0.2);
}

.brand-text {
  margin-left: 10px;
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

.login-header-right {
  font-size: 13px;
}

.login-header-tag {
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid #e4e7ed;
  background: #ffffff;
  color: #606266;
}

.login-layout {
  display: flex;
  gap: 40px;
  align-items: stretch;
}

.login-side {
  flex: 1.3;
  color: #4b5563;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.side-title {
  font-size: 26px;
  font-weight: 600;
  margin: 0 0 12px;
}

.side-desc {
  margin: 0 0 12px;
  font-size: 14px;
  color: #6b7280;
}

.side-list {
  list-style: none;
  padding: 0;
  margin: 0;
  font-size: 13px;
  color: #4b5563;
  line-height: 1.8;
}

.login-box {
  flex: 1;
  background-color: #ffffff;
  border-radius: 10px;
  padding: 32px 28px 24px;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
}

.welcome-title {
  font-size: 24px;
  font-weight: 600;
  text-align: center;
  color: #303133;
}

.welcome-subtitle {
  margin-top: 6px;
  margin-bottom: 24px;
  text-align: center;
  font-size: 13px;
  color: #909399;
}

.full-width {
  width: 100%;
}

.quick-login {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px dashed #e4e7ed;
  font-size: 12px;
  color: #666;
}

.quick-login-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 6px;
}

.quick-login-label {
  font-weight: 500;
}

.quick-login-tip {
  font-size: 11px;
  color: #a0aec0;
}

.quick-login-buttons {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.quick-login-btn {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  padding: 6px 10px;
}

.quick-role {
  font-size: 12px;
  font-weight: 500;
  color: #303133;
}

.quick-account {
  font-size: 11px;
  color: #909399;
}

.login-extra {
  margin-top: 12px;
  text-align: right;
  font-size: 12px;
  color: #909399;
}

.login-extra-link {
  color: #1967e3;
  margin-left: 4px;
  text-decoration: none;
}

.login-extra-link:hover {
  text-decoration: underline;
}

@media (max-width: 900px) {
  .login-layout {
    gap: 24px;
  }
}

@media (max-width: 768px) {
  .login-shell {
    max-width: 420px;
  }

  .login-header {
    margin-bottom: 20px;
  }

  .login-layout {
    flex-direction: column;
  }

  .login-side {
    display: none;
  }

  .login-box {
    padding: 28px 20px 20px;
  }
}

@media (max-width: 600px) {
  .quick-login-buttons {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 420px) {
  .quick-login-buttons {
    grid-template-columns: 1fr;
  }
}
</style>