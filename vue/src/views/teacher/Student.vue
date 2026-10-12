<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input
        v-model="data.name"
        style="width: 220px; margin-right: 10px"
        placeholder="请输入名称查询"
      ></el-input>
      <el-input
        v-model="data.grade"
        style="width: 160px; margin-right: 10px"
        placeholder="按年级筛选"
      ></el-input>
      <el-input
        v-model="data.major"
        style="width: 160px; margin-right: 10px"
        placeholder="按专业筛选"
      ></el-input>
      <el-input
        v-model="data.clazz"
        style="width: 160px; margin-right: 10px"
        placeholder="按班级筛选"
      ></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
      <el-table :data="data.tableData" stripe>
        <el-table-column label="用户名" prop="username"></el-table-column>
        <el-table-column label="名称" prop="name"></el-table-column>
        <el-table-column label="头像">
          <template #default="scope">
            <el-image
              :src="scope.row.avatar"
              style="width: 40px; height: 40px; border-radius: 50%"
            ></el-image>
          </template>
        </el-table-column>
        <el-table-column label="学号" prop="studentNo"></el-table-column>
        <el-table-column label="年级" prop="grade"></el-table-column>
        <el-table-column label="专业" prop="major"></el-table-column>
        <el-table-column label="班级" prop="clazz"></el-table-column>
        <el-table-column label="角色" prop="role"></el-table-column>
        <el-table-column label="操作" align="center" width="220">
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card">
      <el-pagination
        background
        layout="prev, pager, next"
        v-model:page-size="data.pageSize"
        v-model:current-page="data.pageNum"
        :total="data.total"
        @current-change="handlePageChange"
        @size-change="handlePageSizeChange"
      />
    </div>

    <el-dialog
      title="信息"
      width="30%"
      v-model="data.formVisible"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="data.form" label-width="80px" style="padding-right: 30px">
        <el-form-item label="头像" prop="avatar">
          <el-upload :action="uploadUrl" list-type="picture" :on-success="handleImgSuccess">
            <el-button type="primary">上传图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="账号" prop="username">
          <el-input :disabled="data.form.id" v-model="data.form.username" autocomplete="off" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="data.form.studentNo" autocomplete="off" />
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="data.form.grade" autocomplete="off" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="data.form.major" autocomplete="off" />
        </el-form-item>
        <el-form-item label="班级" prop="clazz">
          <el-input v-model="data.form.clazz" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">保 存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from '@/utils/request'
import { reactive } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'

const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  name: null,
  grade: null,
  major: null,
  clazz: null,
})

const load = () => {
  request
    .get('/student/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        name: data.name,
        grade: data.grade,
        major: data.major,
        clazz: data.clazz,
      },
    })
    .then((res) => {
      data.tableData = res.data?.list
      data.total = res.data?.total
    })
}

const handlePageChange = (page) => {
  data.pageNum = page
  load()
}

const handlePageSizeChange = (size) => {
  data.pageSize = size
  data.pageNum = 1
  load()
}

const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

const add = () => {
  request.post('/student/add', data.form).then((res) => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () => {
  request.put('/student/update', data.form).then((res) => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const save = () => {
  data.form.id ? update() : add()
}

const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', {
    type: 'warning',
  })
    .then(() => {
      request.delete('/student/delete/' + id).then((res) => {
        if (res.code === '200') {
          load()
          ElMessage.success('操作成功')
        } else {
          ElMessage.error(res.msg)
        }
      })
    })
    .catch(() => {})
}

const reset = () => {
  data.name = null
  data.grade = null
  data.major = null
  data.clazz = null
  load()
}

const handleImgSuccess = (res) => {
  data.form.avatar = res.data
}

load()
</script>

<style scoped>
.card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
</style>
