<template>
  <div class="teacher-home">
    <div class="grid-layout">
      <!-- 欢迎与快捷入口 -->
      <section class="card welcome-card">
        <div class="welcome-header">
          <div class="welcome-title">教师工作台</div>
          <div class="welcome-subtitle">欢迎回来，{{ data.user.name || '教师' }}！</div>
        </div>
        <div class="welcome-body">
          <p>
            教师端主要面向竞赛指导教师，支持
            <b>竞赛管理、报名审核、题库建设、成绩与评测查询</b>
            等功能。
          </p>
          <p>
            你可以通过下方快捷入口快速进入常用页面：
          </p>
          <div class="quick-actions">
            <el-button type="primary" @click="go('/teacher/contest')">竞赛管理</el-button>
            <el-button @click="go('/teacher/signup')">报名审核</el-button>
            <el-button @click="go('/teacher/problem')">题目管理</el-button>
            <el-button @click="go('/teacher/submission')">评测记录</el-button>
            <el-button @click="go('/teacher/student')">学生管理</el-button>
            <el-button @click="go('/teacher/profile')">个人中心</el-button>
          </div>
        </div>
      </section>

      <!-- 教学与竞赛统计概览 -->
      <section class="card stats-card">
        <div class="card-header">
          <span class="card-title">教学与竞赛概览</span>
          <el-tag v-if="!data.loadingStats" size="small" type="info" effect="plain">
            实时统计
          </el-tag>
        </div>

        <el-skeleton :loading="data.loadingStats" animated>
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
                <div class="stat-label">系统竞赛总数</div>
                <div class="stat-value">{{ data.stats.contestTotal }}</div>
                <div class="stat-desc">当前系统中已配置的程序设计竞赛</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">报名中竞赛</div>
                <div class="stat-value">{{ data.stats.contestSignup }}</div>
                <div class="stat-desc">当前正在接受学生报名的竞赛</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">进行中竞赛</div>
                <div class="stat-value">{{ data.stats.contestRunning }}</div>
                <div class="stat-desc">处于比赛中的竞赛数量</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">已结束竞赛</div>
                <div class="stat-value">{{ data.stats.contestFinished }}</div>
                <div class="stat-desc">历史竞赛累计数量</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">累计评测记录</div>
                <div class="stat-value">{{ data.stats.submissionTotal }}</div>
                <div class="stat-desc">系统中已完成的代码评测次数</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">待审核报名</div>
                <div class="stat-value">{{ data.stats.pendingSignup }}</div>
                <div class="stat-desc">状态为“已报名”的报名记录数量</div>
              </div>
            </div>
          </template>
        </el-skeleton>
      </section>
    </div>

    <!-- 待审核报名列表 -->
    <section class="card" style="margin-top: 16px">
      <div class="card-header">
        <span class="card-title">待审核报名（最近 5 条）</span>
        <el-button link type="primary" @click="go('/teacher/signup')">前往报名审核</el-button>
      </div>
      <el-table
        :data="data.pendingSignups"
        size="small"
        v-loading="data.loadingSignup"
        empty-text="暂无待审核报名"
      >
        <el-table-column label="报名ID" prop="id" width="80" />
        <el-table-column label="竞赛ID" prop="contestId" width="100" />
        <el-table-column label="学生ID" prop="studentId" width="100" />
        <el-table-column label="状态" prop="status" width="100" />
        <el-table-column label="报名时间" width="200">
          <template #default="{ row }">
            {{ fmt(row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" show-overflow-tooltip />
      </el-table>
    </section>

    <!-- 使用提示 -->
    <section class="card" style="margin-top: 16px">
      <div class="card-header">
        <span class="card-title">教学使用建议</span>
      </div>
      <ul class="tips-list">
        <li>在「竞赛管理」中创建或维护本课程 / 团队的程序设计竞赛。</li>
        <li>在「报名审核」中查看报名列表，对学生报名进行通过 / 拒绝操作。</li>
        <li>在「题目管理」中维护训练题与竞赛题，实现以赛促学。</li>
        <li>在「评测记录」中查看学生提交代码的评测结果，用于成绩评定与过程性评价。</li>
        <li>在「学生管理」中维护自己指导的学生信息。</li>
      </ul>
    </section>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import router from '@/router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  loadingStats: false,
  loadingSignup: false,
  stats: {
    contestTotal: 0,
    contestSignup: 0,
    contestRunning: 0,
    contestFinished: 0,
    submissionTotal: 0,
    pendingSignup: 0,
  },
  pendingSignups: [],
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
  return typeof res.data.total === 'number' ? res.data.total : 0
}

const loadStats = async () => {
  data.loadingStats = true
  try {
    const [
      contestTotalRes,
      contestSignupRes,
      contestRunningRes,
      contestFinishedRes,
      submissionRes,
      pendingSignupRes,
    ] = await Promise.all([
      request.get('/contest/selectPage', { params: { pageNum: 1, pageSize: 1 } }),
      request.get('/contest/selectPage', {
        params: { pageNum: 1, pageSize: 1, status: 'SIGNUP' },
      }),
      request.get('/contest/selectPage', {
        params: { pageNum: 1, pageSize: 1, status: 'RUNNING' },
      }),
      request.get('/contest/selectPage', {
        params: { pageNum: 1, pageSize: 1, status: 'FINISHED' },
      }),
      request.get('/submission/selectPage', {
        params: { pageNum: 1, pageSize: 1 },
      }),
      request.get('/signup/selectPage', {
        params: { pageNum: 1, pageSize: 1, status: 'APPLIED' },
      }),
    ])

    data.stats.contestTotal = getTotal(contestTotalRes)
    data.stats.contestSignup = getTotal(contestSignupRes)
    data.stats.contestRunning = getTotal(contestRunningRes)
    data.stats.contestFinished = getTotal(contestFinishedRes)
    data.stats.submissionTotal = getTotal(submissionRes)
    data.stats.pendingSignup = getTotal(pendingSignupRes)
  } catch (e) {
    console.error(e)
    ElMessage.error('加载统计数据失败')
  } finally {
    data.loadingStats = false
  }
}

const loadPendingSignups = async () => {
  data.loadingSignup = true
  try {
    const res = await request.get('/signup/selectPage', {
      params: {
        pageNum: 1,
        pageSize: 5,
        status: 'APPLIED',
      },
    })
    data.pendingSignups = res.data?.list || []
  } catch (e) {
    console.error(e)
  } finally {
    data.loadingSignup = false
  }
}

onMounted(() => {
  loadStats()
  loadPendingSignups()
})
</script>

<style scoped>
.teacher-home {
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

.stat-item {
  border-radius: 6px;
  padding: 10px 12px;
  background-color: #f9fafb;
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
}
</style>
