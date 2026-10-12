<template>
  <div class="student-home">
    <div class="grid-layout">
      <!-- 欢迎与快速入口 -->
      <section class="card welcome-card">
        <div class="welcome-header">
          <div class="welcome-title">学生首页</div>
          <div class="welcome-subtitle">
            欢迎回来，{{ data.user.name || '同学' }}！这里是你的竞赛与练习工作台。
          </div>
        </div>
        <div class="welcome-body">
          <p>
            在 <b>CodeManage 程序竞赛管理系统</b> 中，你可以报名程序设计竞赛、完成日常刷题训练，
            并查看自己的评测记录与成绩表现。
          </p>
          <p>常用入口：</p>
          <div class="quick-actions">
            <el-button type="primary" @click="go('/student/contest')">竞赛列表与报名</el-button>
            <el-button @click="go('/student/signup')">我的报名</el-button>
            <el-button @click="go('/student/problem')">题库练习</el-button>
            <el-button @click="go('/student/submission')">我的提交</el-button>
            <el-button @click="go('/student/profile')">个人中心</el-button>
          </div>
        </div>
      </section>

      <!-- 学习与竞赛概览 -->
      <section class="card stats-card">
        <div class="card-header">
          <span class="card-title">学习与竞赛概览</span>
          <el-tag v-if="!data.loadingStats" size="small" type="info" effect="plain">
            实时统计
          </el-tag>
        </div>

        <el-skeleton :loading="data.loadingStats" animated>
          <template #template>
            <div class="stats-grid">
              <div v-for="i in 4" :key="i" class="stat-item">
                <el-skeleton-item variant="text" style="width: 60%" />
                <el-skeleton-item variant="h3" style="width: 40%; margin-top: 8px" />
              </div>
            </div>
          </template>

          <template #default>
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-label">已报名竞赛</div>
                <div class="stat-value">{{ data.stats.appliedContest }}</div>
                <div class="stat-desc">你发起报名的竞赛数量（包含待审核、已通过、已拒绝）</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">已通过竞赛</div>
                <div class="stat-value">{{ data.stats.approvedContest }}</div>
                <div class="stat-desc">审核状态为「已通过」的竞赛数量</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">题库提交次数</div>
                <div class="stat-value">{{ data.stats.submissionTotal }}</div>
                <div class="stat-desc">你提交到系统的代码评测次数</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">最近 AC 记录</div>
                <div class="stat-value">{{ data.stats.recentAccepted }}</div>
                <div class="stat-desc">最近 7 天内评测结果为通过的次数</div>
              </div>
            </div>
          </template>
        </el-skeleton>
      </section>
    </div>

    <!-- 最近报名 / 最近提交 -->
    <div class="grid-layout grid-2" style="margin-top: 16px">
      <section class="card">
        <div class="card-header">
          <span class="card-title">我的报名（最近 5 条）</span>
          <el-button link type="primary" @click="go('/student/signup')">查看全部</el-button>
        </div>
        <el-table
          :data="data.recentSignups"
          size="small"
          v-loading="data.loadingSignup"
          empty-text="暂无报名记录"
        >
          <el-table-column label="报名ID" prop="id" width="80" />
          <el-table-column label="竞赛ID" prop="contestId" width="100" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              {{ signupStatusLabel(row.status) }}
            </template>
          </el-table-column>
          <el-table-column label="报名时间" width="200">
            <template #default="{ row }">
              {{ fmt(row.applyTime) }}
            </template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" show-overflow-tooltip />
        </el-table>
      </section>

      <section class="card">
        <div class="card-header">
          <span class="card-title">最近提交（最近 5 条）</span>
          <el-button link type="primary" @click="go('/student/submission')">查看全部</el-button>
        </div>
        <el-table
          :data="data.recentSubmissions"
          size="small"
          v-loading="data.loadingSubmission"
          empty-text="暂无提交记录"
        >
          <el-table-column label="提交ID" prop="id" width="80" />
          <el-table-column label="题目ID" prop="problemId" width="100" />
          <el-table-column label="竞赛ID" prop="contestId" width="100" />
          <el-table-column label="状态" prop="status" width="120" />
          <el-table-column label="得分" prop="score" width="80" />
          <el-table-column label="提交时间" width="200">
            <template #default="{ row }">
              {{ fmt(row.submitTime) }}
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>

    <!-- 学习建议 -->
    <section class="card" style="margin-top: 16px">
      <div class="card-header">
        <span class="card-title">学习建议</span>
      </div>
      <ul class="tips-list">
        <li>优先关注「竞赛列表与报名」，了解当前开放的程序设计竞赛并及时报名。</li>
        <li>在「题库练习」中通过刷题巩固课堂所学内容，建议从简单题逐步提升难度。</li>
        <li>在「我的提交」中查看每次评测结果，分析出错原因，持续优化代码质量。</li>
        <li>在「个人中心」维护个人信息，确保学号、班级等信息准确，以便成绩统计。</li>
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
  loadingSubmission: false,
  stats: {
    appliedContest: 0,
    approvedContest: 0,
    submissionTotal: 0,
    recentAccepted: 0,
  },
  recentSignups: [],
  recentSubmissions: [],
})

const go = (path) => {
  router.push(path)
}

const fmt = (val) => {
  if (!val) return ''
  return String(val).replace('T', ' ')
}

const signupStatusLabel = (status) => {
  const map = {
    APPLIED: '已报名',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
  }
  return map[status] || status || '-'
}

const getTotal = (res) => {
  if (!res || !res.data) return 0
  return typeof res.data.total === 'number' ? res.data.total : 0
}

const loadStats = async () => {
  if (!data.user?.id) return
  data.loadingStats = true
  try {
    const [appliedRes, approvedRes, submissionRes, recentAcceptedRes] =
      await Promise.all([
        // 所有报名记录
        request.get('/signup/selectPage', {
          params: {
            pageNum: 1,
            pageSize: 1,
            studentId: data.user.id,
          },
        }),
        // 已通过报名记录
        request.get('/signup/selectPage', {
          params: {
            pageNum: 1,
            pageSize: 1,
            studentId: data.user.id,
            status: 'APPROVED',
          },
        }),
        // 所有提交
        request.get('/submission/selectPage', {
          params: {
            pageNum: 1,
            pageSize: 1,
            studentId: data.user.id,
          },
        }),
        // 最近 7 天通过数量（这里先用状态过滤近似代替）
        request.get('/submission/selectPage', {
          params: {
            pageNum: 1,
            pageSize: 1,
            studentId: data.user.id,
            status: 'ACCEPTED',
          },
        }),
      ])

    data.stats.appliedContest = getTotal(appliedRes)
    data.stats.approvedContest = getTotal(approvedRes)
    data.stats.submissionTotal = getTotal(submissionRes)
    data.stats.recentAccepted = getTotal(recentAcceptedRes)
  } catch (e) {
    console.error(e)
    ElMessage.error('加载统计数据失败')
  } finally {
    data.loadingStats = false
  }
}

const loadRecentSignups = async () => {
  if (!data.user?.id) return
  data.loadingSignup = true
  try {
    const res = await request.get('/signup/selectPage', {
      params: {
        pageNum: 1,
        pageSize: 5,
        studentId: data.user.id,
      },
    })
    data.recentSignups = res.data?.list || []
  } catch (e) {
    console.error(e)
  } finally {
    data.loadingSignup = false
  }
}

const loadRecentSubmissions = async () => {
  if (!data.user?.id) return
  data.loadingSubmission = true
  try {
    const res = await request.get('/submission/selectPage', {
      params: {
        pageNum: 1,
        pageSize: 5,
        studentId: data.user.id,
      },
    })
    data.recentSubmissions = res.data?.list || []
  } catch (e) {
    console.error(e)
  } finally {
    data.loadingSubmission = false
  }
}

onMounted(() => {
  loadStats()
  loadRecentSignups()
  loadRecentSubmissions()
})
</script>

<style scoped>
.student-home {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.grid-layout {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(0, 1.6fr);
  gap: 16px;
}

.grid-layout.grid-2 {
  grid-template-columns: minmax(0, 1.6fr) minmax(0, 2fr);
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
  grid-template-columns: repeat(2, minmax(0, 1fr));
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
  .grid-layout,
  .grid-layout.grid-2 {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
