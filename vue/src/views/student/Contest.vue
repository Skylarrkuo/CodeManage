<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input
        v-model="data.name"
        style="width: 220px; margin-right: 10px"
        placeholder="请输入竞赛名称"
      ></el-input>
      <el-select
        v-model="data.status"
        placeholder="请选择状态"
        style="width: 180px; margin-right: 10px"
        clearable
      >
        <el-option label="报名中" value="SIGNUP" />
        <el-option label="比赛中" value="RUNNING" />
        <el-option label="已结束" value="FINISHED" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin-left: 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table :data="data.tableData" stripe>
        <el-table-column label="名称" prop="name">
          <template #default="scope">
            <el-link type="primary" @click="goContestDetail(scope.row)">
              {{ scope.row.name }}
            </el-link>
          </template>
        </el-table-column>
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
        <el-table-column label="报名状态" width="120">
          <template #default="scope">
            {{ getSignupStatusText(scope.row.id) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="320">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'SIGNUP'"
              type="primary"
              size="small"
              :disabled="!!data.signupMap[scope.row.id]"
              @click="applyContest(scope.row)"
            >
              报名
            </el-button>
            <el-button
              v-else-if="scope.row.status === 'RUNNING'"
              type="success"
              size="small"
              :disabled="!canEnterContest(scope.row)"
              @click="enterContest(scope.row)"
            >
              进入比赛
            </el-button>
            <el-button
              type="warning"
              size="small"
              @click="openRank(scope.row)"
            >
              排行榜
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="goContestDetail(scope.row)"
            >
              竞赛详情
            </el-button>
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
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  name: null,
  status: null,
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  signupMap: {},
})

const router = useRouter()

const fmt = (val) => {
  if (!val) return ''
  return val.replace('T', ' ')
}

const loadMySignups = () => {
  if (!data.user?.id) return
  request
    .get('/signup/selectAll', {
      params: { studentId: data.user.id },
    })
    .then((res) => {
      const map = {}
      ;(res.data || []).forEach((item) => {
        map[item.contestId] = item
      })
      data.signupMap = map
    })
}

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
      loadMySignups()
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

const applyContest = (row) => {
  if (!data.user?.id) {
    ElMessage.error('请先登录')
    return
  }
  request
    .post('/signup/apply', {
      contestId: row.id,
      studentId: data.user.id,
    })
    .then((res) => {
      if (res.code === '200') {
        ElMessage.success('报名成功，等待老师审核')
        loadMySignups()
      } else {
        ElMessage.error(res.msg)
      }
    })
}

// 当前账号是否可以进入某场比赛：
// 比赛状态为 RUNNING，且存在报名记录且未被拒绝
// （允许“已报名待审核”的同学先进入比赛页面，提交代码仍受后端审核限制）
const canEnterContest = (row) => {
  if (row.status !== 'RUNNING') return false
  const signup = data.signupMap[row.id]
  if (!signup) return false
  if (signup.status === 'REJECTED') return false
  return true
}

// 进入竞赛详情页面：展示竞赛信息、规则、题目列表和排行榜
const goContestDetail = (row) => {
  if (!row?.id) return
  router.push({ path: `/student/contest/${row.id}` })
}

// 进入比赛：跳转到题目页，并携带 contestId 作为查询参数
const enterContest = (row) => {
  if (!canEnterContest(row)) {
    ElMessage.error('仅在比赛进行中且已报名该比赛时才能进入比赛')
    return
  }
  router.push({
    path: '/student/problem',
    query: { contestId: row.id },
  })
}

const getSignupStatusText = (contestId) => {
  const signup = data.signupMap[contestId]
  if (!signup) return '未报名'
  const map = {
    APPLIED: '已报名',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
  }
  return map[signup.status] || signup.status
}

// 打开排行榜对话框
const openRank = (row) => {
  if (!row?.id) return
  router.push({ path: `/student/contest/${row.id}` })
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
