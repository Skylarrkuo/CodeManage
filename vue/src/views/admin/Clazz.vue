<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input
        v-model="data.grade"
        style="width: 160px; margin-right: 10px"
        placeholder="按年级查询"
      ></el-input>
      <el-input
        v-model="data.major"
        style="width: 200px; margin-right: 10px"
        placeholder="按专业查询"
      ></el-input>
      <el-input
        v-model="data.clazz"
        style="width: 160px; margin-right: 10px"
        placeholder="按班级查询"
      ></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin-left: 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增班级</el-button>
      </div>
      <el-table :data="data.tableData" stripe>
        <el-table-column label="年级" prop="grade" />
        <el-table-column label="专业" prop="major" />
        <el-table-column label="班级" prop="clazz" />
        <el-table-column label="备注" prop="remark" />
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
      title="班级信息"
      width="30%"
      v-model="data.formVisible"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="data.form" label-width="80px" style="padding-right: 30px">
        <el-form-item label="年级" prop="grade">
          <el-input v-model="data.form.grade" autocomplete="off" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="data.form.major" autocomplete="off" />
        </el-form-item>
        <el-form-item label="班级" prop="clazz">
          <el-input v-model="data.form.clazz" autocomplete="off" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="data.form.remark" autocomplete="off" />
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
import { reactive } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  formVisible: false,
  form: {},
  grade: null,
  major: null,
  clazz: null,
})

const load = () => {
  request
    .get('/clazz/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
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

const reset = () => {
  data.grade = null
  data.major = null
  data.clazz = null
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
  request.post('/clazz/add', data.form).then((res) => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () => {
  request.put('/clazz/update', data.form).then((res) => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
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
      request.delete('/clazz/delete/' + id).then((res) => {
        if (res.code === '200') {
          ElMessage.success('操作成功')
          load()
        } else {
          ElMessage.error(res.msg)
        }
      })
    })
    .catch(() => {})
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
