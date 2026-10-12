<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input
        v-model="data.name"
        style="width: 200px; margin-right: 10px"
        placeholder="请输入竞赛名称"
      ></el-input>
      <el-select
        v-model="data.status"
        placeholder="请选择状态"
        style="width: 160px; margin-right: 10px"
        clearable
      >
        <el-option label="草稿" value="DRAFT" />
        <el-option label="已发布" value="PUBLISHED" />
        <el-option label="报名中" value="SIGNUP" />
        <el-option label="比赛中" value="RUNNING" />
        <el-option label="已结束" value="FINISHED" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin-left: 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增竞赛</el-button>
      </div>
      <el-table :data="data.tableData" stripe>
        <el-table-column label="名称" prop="name" />
        <el-table-column label="级别" prop="level" width="80" />
        <el-table-column label="类型" prop="type" width="80" />
        <el-table-column label="状态" prop="status" width="100" />
        <el-table-column label="报名时间" width="220">
          <template #default="scope">
            <div v-if="scope.row.signUpStartTime">
              {{ fmt(scope.row.signUpStartTime) }} ~
              {{ fmt(scope.row.signUpEndTime) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="比赛时间" width="220">
          <template #default="scope">
            <div v-if="scope.row.startTime">
              {{ fmt(scope.row.startTime) }} ~ {{ fmt(scope.row.endTime) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="创建人" prop="creatorName" width="100" />
        <el-table-column label="操作" align="center" width="260">
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="success" @click="goProblem(scope.row)" style="margin: 0 5px">
              题目
            </el-button>
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
      title="竞赛信息"
      width="40%"
      v-model="data.formVisible"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="data.form" label-width="90px" style="padding-right: 30px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item label="级别" prop="level">
          <el-select v-model="data.form.level" placeholder="请选择级别" style="width: 100%">
            <el-option label="校级" value="校级" />
            <el-option label="省级" value="省级" />
            <el-option label="国家级" value="国家级" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="data.form.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="个人" value="个人" />
            <el-option label="团队" value="团队" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="data.form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="报名中" value="SIGNUP" />
            <el-option label="比赛中" value="RUNNING" />
            <el-option label="已结束" value="FINISHED" />
          </el-select>
        </el-form-item>
        <el-form-item label="报名时间">
          <el-date-picker
            v-model="data.signRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="比赛时间">
          <el-date-picker
            v-model="data.contestRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input v-model="data.form.description" type="textarea" :rows="4" />
          <div
            v-if="data.form.description"
            class="md-preview"
            v-html="mdPreview(data.form.description)"
          ></div>
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
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'

const router = useRouter()

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  name: null,
  status: null,
  formVisible: false,
  form: {},
  signRange: [],
  contestRange: [],
})

const fmt = (val) => {
  if (!val) return ''
  return val.replace('T', ' ')
}

// 竞赛简介 Markdown 预览
const mdPreview = (text) => renderMarkdown(text || '')

const load = () => {
  request
    .get('/contest/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        name: data.name,
        status: data.status,
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
  data.name = null
  data.status = null
  load()
}

const handleAdd = () => {
  data.form = {}
  data.signRange = []
  data.contestRange = []
  data.formVisible = true
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.signRange =
    row.signUpStartTime && row.signUpEndTime
      ? [row.signUpStartTime, row.signUpEndTime]
      : []
  data.contestRange =
    row.startTime && row.endTime ? [row.startTime, row.endTime] : []
  data.formVisible = true
}

const goProblem = (row) => {
  if (!row?.id) return
  router.push({ path: '/admin/problem', query: { contestId: row.id } })
}

const fillTimeRange = () => {
  if (data.signRange && data.signRange.length === 2) {
    data.form.signUpStartTime = data.signRange[0]
    data.form.signUpEndTime = data.signRange[1]
  }
  if (data.contestRange && data.contestRange.length === 2) {
    data.form.startTime = data.contestRange[0]
    data.form.endTime = data.contestRange[1]
  }
}

const add = () => {
  fillTimeRange()
  request.post('/contest/add', data.form).then((res) => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      const newId = res.data
      data.formVisible = false
      load()
      if (newId) {
        ElMessageBox.confirm('竞赛已创建，是否立即前往添加题目？', '提示', {
          type: 'info',
        })
          .then(() => {
            router.push({ path: '/admin/problem', query: { contestId: newId } })
          })
          .catch(() => {})
      }
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () => {
  fillTimeRange()
  request.put('/contest/update', data.form).then((res) => {
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
      request.delete('/contest/delete/' + id).then((res) => {
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

.md-preview {
  margin-top: 8px;
  padding: 10px;
  background: #f8f8f8;
  border-radius: 4px;
  max-height: 200px;
  overflow: auto;
  font-size: 13px;
  line-height: 1.6;
}
</style>
