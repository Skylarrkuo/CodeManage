<template>
  <div class="card">
    <div style="margin-bottom: 20px; font-size: 20px">修改密码</div>
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="max-width: 420px">
      <el-form-item label="原密码" prop="password">
        <el-input v-model="form.password" type="password" show-password autocomplete="off" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" type="password" show-password autocomplete="off" />
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password" show-password autocomplete="off" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submit">提 交</el-button>
        <el-button @click="reset">重 置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const user = JSON.parse(localStorage.getItem('system-user') || '{}')

const formRef = ref()

const form = reactive({
  password: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  password: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' },
  ],
}

const submit = () => {
  formRef.value.validate((valid) => {
    if (!valid) return
    const payload = {
      username: user.username,
      password: form.password,
      newPassword: form.newPassword,
      role: user.role,
    }
    request.put('/updatePassword', payload).then((res) => {
      if (res.code === '200') {
        ElMessage.success('密码修改成功，请使用新密码重新登录')
        // 修改密码后强制退出登录
        localStorage.removeItem('system-user')
      } else {
        ElMessage.error(res.msg)
      }
    })
  })
}

const reset = () => {
  form.password = ''
  form.newPassword = ''
  form.confirmPassword = ''
}
</script>

<style scoped>
.card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
</style>
