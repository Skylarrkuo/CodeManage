<template>
  <div class="login-page register-page">
    <div class="login-shell">
      <!-- 顶部品牌，与登录页保持统一风格 -->
      <header class="login-header">
        <div class="brand">
          <img src="@/assets/imgs/it.png" alt="logo" class="brand-logo" />
          <div class="brand-text">
            <div class="brand-title">CodeManage 程序竞赛管理系统</div>
            <div class="brand-subtitle">统一的程序设计竞赛与评测平台</div>
          </div>
        </div>
        <div class="login-header-right">
          <span class="login-header-tag">学生账号注册</span>
        </div>
      </header>

      <main class="login-layout">
        <!-- 左侧介绍区域 -->
        <section class="login-side">
          <h1 class="side-title">注册学生端账号，开始在线刷题与参赛</h1>
          <p class="side-desc">
            注册成功后，可使用学生身份登录系统，参与课程作业、课堂测验与编程竞赛。
          </p>
          <ul class="side-list">
            <li>· 统一账号：一个账号通用全部课程与竞赛</li>
            <li>· 在线评测：实时查看代码评测结果与通过率</li>
            <li>· 成绩追踪：支持历史提交记录与成绩查询</li>
          </ul>
        </section>

        <!-- 右侧注册卡片 -->
        <section class="login-box">
          <div class="welcome-title">学 生 账 号 注 册</div>
          <div class="welcome-subtitle">填写以下信息，完成学生账号注册</div>

          <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="form.username"
                placeholder="用于登录的账号，例如学号"
                autocomplete="off"
              />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="form.password"
                type="password"
                show-password
                autocomplete="new-password"
                placeholder="请输入密码"
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                type="password"
                show-password
                autocomplete="new-password"
                placeholder="请再次输入密码"
              />
            </el-form-item>

            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>

            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="form.studentNo" placeholder="请输入学号" />
            </el-form-item>

            <div class="form-row">
              <el-form-item class="form-row-item" label="年级" prop="grade">
                <el-input v-model="form.grade" placeholder="如 2022" />
              </el-form-item>
              <el-form-item class="form-row-item" label="专业" prop="major">
                <el-input v-model="form.major" placeholder="如 计算机科学与技术" />
              </el-form-item>
            </div>

            <el-form-item label="班级" prop="clazz">
              <el-input v-model="form.clazz" placeholder="如 2201 班" />
            </el-form-item>

            <el-form-item>
              <el-button
                size="large"
                type="primary"
                class="full-width"
                @click="handleRegister"
                :loading="submitting"
              >
                注 册
              </el-button>
            </el-form-item>
          </el-form>

          <div class="login-extra">
            已有账号？
            <router-link class="login-extra-link" to="/login">立即登录</router-link>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import router from '@/router'

const formRef = ref()
const submitting = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  studentNo: '',
  grade: '',
  major: '',
  clazz: '',
})

function validateConfirmPassword(rule, value, callback) {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3-20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6-20 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
}

const handleRegister = () => {
  if (!formRef.value) return
  formRef.value.validate((valid) => {
    if (!valid) return
    submitting.value = true

    const payload = { ...form }
    delete payload.confirmPassword

    request
      .post('/register', payload)
      .then((res) => {
        if (res.code === '200') {
          ElMessage.success('注册成功，请使用新账号登录')
          router.push('/login')
        } else {
          ElMessage.error(res.msg || '注册失败')
        }
      })
      .catch(() => {
        ElMessage.error('注册失败，请稍后重试')
      })
      .finally(() => {
        submitting.value = false
      })
  })
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

.form-row {
  display: flex;
  gap: 16px;
}

.form-row-item {
  flex: 1;
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

@media (max-width: 480px) {
  .form-row {
    flex-direction: column;
  }
}
</style>
