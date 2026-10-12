<template>
  <div class="admin-home">
    <div class="grid-layout">
      <!-- 欢迎与快捷入口 -->
      <section class="card welcome-card">
        <div class="welcome-header">
          <div class="welcome-title">CodeManage 程序竞赛管理系统 - 管理员工作台</div>
          <div class="welcome-subtitle">欢迎回来，{{ data.user.name || '管理员' }}！</div>
        </div>
        <div class="welcome-body">
          <p>
            本系统用于支撑高校程序设计竞赛的
            <b>报名、组织、评测、统计、公示</b>
            全流程管理，管理员负责账号、竞赛计划、评测环境及系统参数的维护。
          </p>
          <p>
            你可以通过下方快捷入口快速进入核心功能模块：
          </p>
          <div class="quick-actions">
            <el-button type="primary" @click="go('/admin/contest')">竞赛管理</el-button>
            <el-button @click="go('/admin/problem')">题库管理</el-button>
            <el-button @click="go('/admin/student')">学生信息</el-button>
            <el-button @click="go('/admin/submission')">提交记录</el-button>
          </div>
        </div>
      </section>

      <!-- 系统统计概览 -->
      <section class="card stats-card">
        <div class="card-header">
          <span class="card-title">系统概览</span>
          <el-tag v-if="!data.loading" size="small" type="info" effect="plain">实时统计</el-tag>
        </div>

        <el-skeleton :loading="data.loading" animated>
          <template #template>
            <div class="stats-grid">
              <div v-for="i in 6" :key="i" class="stat-item">
                <el-skeleton-item variant="text" style="width: 60%" />
                <el-skeleton-item variant="h3" style="width: 40%; margin-top: 8px" />
              </div>
            </div>
          </template>

          <template #default>
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-label">管理员</div>
                <div class="stat-value">{{ data.stats.adminCount }}</div>
                <div class="stat-desc">系统管理账号数量</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">教师</div>
                <div class="stat-value">{{ data.stats.teacherCount }}</div>
                <div class="stat-desc">已入驻指导教师</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">学生</div>
                <div class="stat-value">{{ data.stats.studentCount }}</div>
                <div class="stat-desc">注册学生总数</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">班级</div>
                <div class="stat-value">{{ data.stats.clazzCount }}</div>
                <div class="stat-desc">已维护教学班级数</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">题目</div>
                <div class="stat-value">{{ data.stats.problemCount }}</div>
                <div class="stat-desc">公开题库与竞赛题目</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">提交记录</div>
                <div class="stat-value">{{ data.stats.submissionCount }}</div>
                <div class="stat-desc">累计评测次数</div>
              </div>
            </div>

            <div class="stats-sub-grid">
              <div class="stat-item small">
                <div class="stat-label">报名中竞赛</div>
                <div class="stat-value">{{ data.stats.signupContestCount }}</div>
              </div>
              <div class="stat-item small">
                <div class="stat-label">比赛中竞赛</div>
                <div class="stat-value">{{ data.stats.runningContestCount }}</div>
              </div>
              <div class="stat-item small">
                <div class="stat-label">已结束竞赛</div>
                <div class="stat-value">{{ data.stats.finishedContestCount }}</div>
              </div>
            </div>
          </template>
        </el-skeleton>
      </section>
    </div>

    <!-- 最近竞赛列表 -->
    <section class="card" style="margin-top: 16px">
      <div class="card-header">
        <span class="card-title">近期竞赛</span>
        <el-button link type="primary" @click="go('/admin/contest')">前往竞赛管理</el-button>
      </div>
      <el-table
        :data="data.recentContests"
        size="small"
        v-loading="data.loadingRecentContest"
        empty-text="暂无竞赛数据"
      >
        <el-table-column label="名称" prop="name" min-width="180" />
        <el-table-column label="状态" prop="status" width="100" />
        <el-table-column label="报名时间" width="230">
          <template #default="{ row }">
            <span v-if="row.signUpStartTime">
              {{ fmt(row.signUpStartTime) }} ~ {{ fmt(row.signUpEndTime) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="比赛时间" width="230">
          <template #default="{ row }">
            <span v-if="row.startTime">
              {{ fmt(row.startTime) }} ~ {{ fmt(row.endTime) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="简介" prop="description" min-width="200" show-overflow-tooltip />
      </el-table>
    </section>

    <!-- 使用提示（保留原有文案） -->
    <section class="card" style="margin-top: 16px">
      <div class="card-header">
        <span class="card-title">使用提示</span>
      </div>
      <ul class="tips-list">
        <li>在「用户管理」中维护管理员 / 教师 / 学生账号信息；</li>
        <li>在「竞赛管理」中创建并发布竞赛，设置报名时间与比赛时间；</li>
        <li>在「报名管理」中查看、删除报名记录（教师端负责审核）；</li>
        <li>在「题库管理」中维护竞赛题目及练习题；</li>
        <li>在「提交记录」中查看评测结果，为后续成绩统计提供依据。</li>
      </ul>
    </section>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import router from '@/router'
import request from '@/utils/request'
import { getUser } from '@/utils/auth'
import { ElMessage } from 'element-plus'

const data = reactive({
  user: getUser(),
  loading: false,
  loadingRecentContest: false,
  stats: {
    adminCount: 0,
    teacherCount: 0,
    studentCount: 0,
    clazzCount: 0,
    contestCount: 0,
    problemCount: 0,
    submissionCount: 0,
    signupContestCount: 0,
    runningContestCount: 0,
    finishedContestCount: 0,
  },
  recentContests: [],
})

const go = (path) => {
  router.push(path)
}

const fmt = (val) => {
  if (!val) return ''
  return String(val).replace('T', ' ')
}

const getTotal = (res) => {
  if (!res || !res.data) return 0
  // PageHelper PageInfo 结构：{ list: [], total: number, ... }
  return typeof res.data.total === 'number' ? res.data.total : 0
}

const loadStats = async () => {
  data.loading = true
  try {
    const [
      adminRes,
      teacherRes,
      studentRes,
      clazzRes,
      contestRes,
      problemRes,
      submissionRes,
      signupContestRes,
      runningContestRes,
      finishedContestRes,
    ] = await Promise.all([
      request.get('/admin/selectPage', { params: { pageNum: 1, pageSize: 1 } }),
      request.get('/teacher/selectPage', { params: { pageNum: 1, pageSize: 1 } }),
      request.get('/student/selectPage', { params: { pageNum: 1, pageSize: 1 } }),
      request.get('/clazz/selectPage', { params: { pageNum: 1, pageSize: 1 } }),
      request.get('/contest/selectPage', { params: { pageNum: 1, pageSize: 1 } }),
      request.get('/problem/selectPage', { params: { pageNum: 1, pageSize: 1 } }),
      request.get('/submission/selectPage', { params: { pageNum: 1, pageSize: 1 } }),
      request.get('/contest/selectPage', {
        params: { pageNum: 1, pageSize: 1, status: 'SIGNUP' },
      }),
      request.get('/contest/selectPage', {
        params: { pageNum: 1, pageSize: 1, status: 'RUNNING' },
      }),
      request.get('/contest/selectPage', {
        params: { pageNum: 1, pageSize: 1, status: 'FINISHED' },
      }),
    ])

    data.stats.adminCount = getTotal(adminRes)
    data.stats.teacherCount = getTotal(teacherRes)
    data.stats.studentCount = getTotal(studentRes)
    data.stats.clazzCount = getTotal(clazzRes)
    data.stats.contestCount = getTotal(contestRes)
    data.stats.problemCount = getTotal(problemRes)
    data.stats.submissionCount = getTotal(submissionRes)
    data.stats.signupContestCount = getTotal(signupContestRes)
    data.stats.runningContestCount = getTotal(runningContestRes)
    data.stats.finishedContestCount = getTotal(finishedContestRes)
  } catch (e) {
    console.error(e)
    ElMessage.error('加载系统统计数据失败')
  } finally {
    data.loading = false
  }
}

const loadRecentContests = async () => {
  data.loadingRecentContest = true
  try {
    const res = await request.get('/contest/selectPage', {
      params: { pageNum: 1, pageSize: 5 },
    })
    data.recentContests = res.data?.list || []
  } catch (e) {
    console.error(e)
  } finally {
    data.loadingRecentContest = false
  }
}

onMounted(() => {
  loadStats()
  loadRecentContests()
})
</script>

<style scoped>
.admin-home {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.grid-layout {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(0, 1.6fr);
  gap: 16px;
}

.card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.welcome-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.welcome-header {
  margin-bottom: 12px;
}

.welcome-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 4px;
}

.welcome-subtitle {
  font-size: 14px;
  color: #6b7280;
}

.welcome-body p {
  margin: 6px 0;
  line-height: 1.7;
}

.quick-actions {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.stats-sub-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.stat-item {
  border-radius: 6px;
  padding: 10px 12px;
  background-color: #f9fafb;
}

.stat-item.small {
  background-color: #f3f4f6;
}

.stat-label {
  font-size: 12px;
  color: #6b7280;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  margin-top: 4px;
  color: #111827;
}

.stat-desc {
  margin-top: 2px;
  font-size: 12px;
  color: #9ca3af;
}

.tips-list {
  margin: 0;
  padding-left: 18px;
  line-height: 1.8;
  font-size: 14px;
}

@media (max-width: 960px) {
  .grid-layout {
    grid-template-columns: minmax(0, 1fr);
  }

  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .stats-sub-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
