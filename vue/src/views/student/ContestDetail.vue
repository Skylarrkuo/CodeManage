<template>
  <div class="contest-detail-page">
    <!-- 顶部：竞赛基本信息 -->
    <div class="card contest-header" v-if="data.contest">
      <div class="header-main">
        <div>
          <h2 class="contest-title">{{ data.contest.name }}</h2>
          <div class="contest-tags">
            <el-tag size="small" effect="plain" type="info" v-if="data.contest.level">
              {{ data.contest.level }}
            </el-tag>
            <el-tag size="small" effect="plain" type="success" v-if="data.contest.type">
              {{ data.contest.type }}赛
            </el-tag>
            <el-tag size="small" :type="statusTagType(data.contest.status)" effect="dark">
              {{ statusLabel(data.contest.status) }}
            </el-tag>
          </div>
        </div>

        <div class="header-actions">
          <el-button @click="backToList">返回竞赛列表</el-button>
          <el-button
            v-if="canApplyContest"
            type="primary"
            @click="applyContest"
          >
            报名参加
          </el-button>
          <el-button
            v-else-if="canEnterContest"
            type="success"
            @click="goProblemList"
          >
            进入题目列表
          </el-button>
          <span v-else class="hint-text">
            {{ signupStatusText }}
          </span>
        </div>
      </div>

      <div class="header-meta">
        <div>
          报名时间：
          <span v-if="data.contest.signUpStartTime">
            {{ fmt(data.contest.signUpStartTime) }} ~
            {{ fmt(data.contest.signUpEndTime) }}
          </span>
          <span v-else>未设置</span>
        </div>
        <div>
          比赛时间：
          <span v-if="data.contest.startTime">
            {{ fmt(data.contest.startTime) }} ~
            {{ fmt(data.contest.endTime) }}
          </span>
          <span v-else>未设置</span>
        </div>
      </div>
    </div>

    <div class="layout" v-if="data.contest">
      <!-- 左侧：竞赛说明 + 题目列表 -->
      <div class="layout-left">
        <div class="card" style="margin-bottom: 10px">
          <h3 class="section-title">竞赛说明 / 规则</h3>
          <div v-if="data.contest.description" class="md-block" v-html="md(data.contest.description)"></div>
          <div v-else class="empty-text">暂未填写竞赛说明。</div>
        </div>

        <div class="card" id="contest-problem-list">
          <div class="section-header">
            <h3 class="section-title">题目列表</h3>
            <span class="section-sub" v-if="!showProblemList">
              比赛尚未开始，开始后将展示本场竞赛的所有题目。
            </span>
            <span class="section-sub" v-else>
              仅展示本场竞赛所包含的题目。
            </span>
          </div>

          <el-table :data="showProblemList ? data.problems : []" stripe>
            <el-table-column label="ID" prop="id" width="60" />
            <el-table-column label="标题" prop="title" />
            <el-table-column label="难度" prop="difficulty" width="80" />
            <el-table-column label="操作" align="center" width="160">
              <template #default="scope">
                <el-button
                  type="primary"
                  size="small"
                  :disabled="!canEnterContest"
                  @click="goProblem(scope.row)"
                >
                  进入答题
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 右侧：排行榜 -->
      <div class="layout-right">
        <div class="card">
          <div class="section-header">
            <h3 class="section-title">竞赛排行榜</h3>
            <span class="section-sub">
              可在比赛进行中和结束后查看题目完成情况和名次。
            </span>
          </div>

          <div v-if="data.rankLoading" style="text-align: center; padding: 20px 0">
            正在加载排行榜...
          </div>
          <div v-else>
            <div style="margin-bottom: 10px" v-if="data.rankData.contest">
              <div>
                比赛时间：
                {{ fmt(data.rankData.contest.startTime) }} ~
                {{ fmt(data.rankData.contest.endTime) }}
              </div>
              <div>当前状态：{{ statusLabel(data.rankData.contest.status) }}</div>
            </div>
            <el-table
              :data="data.rankData.ranks || []"
              border
              style="width: 100%"
              empty-text="暂无排行榜数据"
            >
              <el-table-column prop="rank" label="名次" width="60" />
              <el-table-column prop="studentNo" label="学号" width="120" />
              <el-table-column prop="studentName" label="姓名" width="100" />
              <el-table-column prop="clazz" label="班级" width="140" />
              <el-table-column prop="solvedCount" label="过题数" width="80" />
              <el-table-column prop="penalty" label="罚时" width="80" />
              <el-table-column prop="totalScore" label="总分" width="80" />
              <el-table-column
                v-for="p in (data.rankData.problems || [])"
                :key="p.problemId"
                :min-width="90"
              >
                <template #header>
                  <span>{{ p.label }}</span>
                  <span style="color: #999; margin-left: 4px">{{ p.title }}</span>
                </template>
                <template #default="{ row }">
                  {{ formatProblemCell(row, p.problemId) }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载中或数据异常提示 -->
    <div v-if="!data.contest && !data.loading" class="card empty-text">
      未找到该竞赛信息。
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'

const route = useRoute()
const router = useRouter()

const contestId = Number(route.params.id)

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  loading: false,
  contest: null,
  // 报名信息
  signupMap: {},
  // 题目列表
  problems: [],
  // 排行榜
  rankLoading: false,
  rankData: {
    contest: null,
    problems: [],
    ranks: [],
  },
})

const md = (text) => renderMarkdown(text || '')

const fmt = (val) => {
  if (!val) return ''
  return String(val).replace('T', ' ')
}

// 竞赛状态展示
const statusConfig = {
  DRAFT: { type: 'info', label: '草稿' },
  PUBLISHED: { type: 'success', label: '已发布' },
  SIGNUP: { type: 'warning', label: '报名中' },
  RUNNING: { type: 'success', label: '比赛中' },
  FINISHED: { type: 'info', label: '已结束' },
}

const statusTagType = (status) => statusConfig[status]?.type || 'info'
const statusLabel = (status) => statusConfig[status]?.label || status || '未知状态'

// 当前用户在本场竞赛的报名记录
const currentSignup = computed(() => {
  return data.signupMap[contestId] || null
})

const signupStatusText = computed(() => {
  const s = currentSignup.value
  if (!s) return '未报名'
  const map = {
    APPLIED: '已报名，等待审核',
    APPROVED: '已通过报名审核',
    REJECTED: '报名已被拒绝',
  }
  return map[s.status] || s.status
})

// 是否，可以报名 / 进入比赛 / 展示题目列表
const canApplyContest = computed(() => {
  if (!data.contest) return false
  if (data.contest.status !== 'SIGNUP') return false
  return !currentSignup.value
})

const canEnterContest = computed(() => {
  if (!data.contest) return false
  if (data.contest.status !== 'RUNNING') return false
  const s = currentSignup.value
  if (!s) return false
  if (s.status === 'REJECTED') return false
  return true
})

const showProblemList = computed(() => {
  if (!data.contest) return false
  return data.contest.status === 'RUNNING' || data.contest.status === 'FINISHED'
})

// 加载竞赛详情
const loadContest = () => {
  if (!contestId) return
  data.loading = true
  request
    .get(`/contest/selectById/${contestId}`)
    .then((res) => {
      if (res.code === '200') {
        data.contest = res.data || null
        // 在比赛进行中或结束后，展示并加载本场竞赛的题目列表
        if (data.contest && (data.contest.status === 'RUNNING' || data.contest.status === 'FINISHED')) {
          loadProblems()
        }
      } else {
        ElMessage.error(res.msg || '加载竞赛信息失败')
      }
    })
    .finally(() => {
      data.loading = false
    })
}

// 加载当前学生的报名记录（用于判断报名状态与参赛资格）
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

// 加载本场竞赛的题目列表
const loadProblems = () => {
  if (!contestId) return
  request
    .get('/problem/selectPage', {
      params: {
        pageNum: 1,
        pageSize: 1000,
        type: 'CONTEST',
        contestId,
      },
    })
    .then((res) => {
      data.problems = res.data?.list || []
    })
}

// 加载排行榜
const loadRank = () => {
  if (!contestId) return
  data.rankLoading = true
  request
    .get(`/contest/rank/${contestId}`)
    .then((res) => {
      if (res.code === '200') {
        data.rankData = res.data || { contest: null, problems: [], ranks: [] }
      } else {
        ElMessage.error(res.msg || '加载排行榜失败')
      }
    })
    .finally(() => {
      data.rankLoading = false
    })
}

// 报名当前竞赛
const applyContest = () => {
  if (!data.user?.id) {
    ElMessage.error('请先登录')
    return
  }
  if (!data.contest) return
  request
    .post('/signup/apply', {
      contestId,
      studentId: data.user.id,
    })
    .then((res) => {
      if (res.code === '200') {
        ElMessage.success('报名成功，等待老师审核')
        loadMySignups()
      } else {
        ElMessage.error(res.msg || '报名失败')
      }
    })
}

// 从竞赛详情跳转到题目列表页（竞赛模式）
const goProblemList = () => {
  if (!canEnterContest.value) {
    ElMessage.error('仅在比赛进行中且已报名该比赛时才能进入题目列表')
    return
  }
  router.push({ path: '/student/problem', query: { contestId } })
}

// 从题目列表跳转到具体做题页面
const goProblem = (row) => {
  if (!row?.id) return
  if (!canEnterContest.value && data.contest?.status === 'RUNNING') {
    ElMessage.error('仅在比赛进行中且已报名该比赛时才能进入题目')
    return
  }
  router.push({ path: `/student/problem/${row.id}`, query: { contestId } })
}

// 排行榜中单个题目的单元格展示
const formatProblemCell = (row, problemId) => {
  const map = row.problemResultMap || {}
  const r = map[problemId]
  if (!r) return ''
  const wrong = r.wrongAttempts || 0
  if (!r.solved) {
    return wrong > 0 ? `-${wrong}` : ''
  }
  const t = r.time != null ? `${r.time}min` : ''
  return wrong > 0 ? `${t} (+${wrong})` : t || 'AC'
}

const backToList = () => {
  router.push('/student/contest')
}

onMounted(() => {
  loadContest()
  loadMySignups()
  loadRank()
})
</script>

<style scoped>
.contest-detail-page {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.contest-header {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.header-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.contest-title {
  margin: 0 0 8px;
}

.contest-tags {
  display: flex;
  gap: 8px;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.header-meta {
  display: flex;
  gap: 40px;
  font-size: 13px;
  color: #6b7280;
  flex-wrap: wrap;
}

.layout {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.layout-left {
  flex: 1.1;
}

.layout-right {
  flex: 0.9;
}

.section-title {
  margin: 0 0 8px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 8px;
}

.section-sub {
  font-size: 12px;
  color: #9ca3af;
}

.md-block {
  background: #f8f8f8;
  padding: 10px;
  border-radius: 4px;
  line-height: 1.6;
  font-size: 14px;
}

.empty-text {
  color: #9ca3af;
  font-size: 13px;
}

.hint-text {
  font-size: 13px;
  color: #6b7280;
}

@media (max-width: 960px) {
  .header-main {
    flex-direction: column;
    align-items: flex-start;
  }

  .layout {
    flex-direction: column;
  }
}
</style>
