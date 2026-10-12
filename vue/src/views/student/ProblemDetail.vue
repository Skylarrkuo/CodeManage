<template>
  <div class="problem-detail-page">
    <!-- 竞赛模式提示 -->
    <div v-if="data.contestMode" class="card contest-tip">
      <div>
        当前处于【{{ data.contestInfo?.name || '竞赛' }}】比赛模式，仅展示本场竞赛题目。
      </div>
      <div v-if="data.contestInfo">
        比赛时间：{{ data.contestInfo.startTime }} ~ {{ data.contestInfo.endTime }}
      </div>
    </div>

    <div class="card problem-layout">
      <!-- 左侧题面区域 -->
      <div class="problem-left" v-if="data.problem">
        <div class="problem-header">
          <h2 class="problem-title">{{ data.problem.title }}</h2>
          <div class="problem-meta">
            <el-tag size="small" effect="plain">
              {{ data.problem.difficulty || '未设置难度' }}
            </el-tag>
            <el-tag
              v-if="data.problem.type === 'PRACTICE'"
              size="small"
              type="success"
              effect="plain"
            >练习</el-tag>
            <el-tag
              v-else-if="data.problem.type === 'CONTEST'"
              size="small"
              type="warning"
              effect="plain"
            >竞赛</el-tag>
          </div>
        </div>

        <section class="problem-section">
          <h3>题目描述</h3>
          <div class="md-block" v-html="md(data.problem.description)"></div>
        </section>

        <section class="problem-section" v-if="data.problem.inputDesc">
          <h3>输入描述</h3>
          <div class="md-block" v-html="md(data.problem.inputDesc)"></div>
        </section>

        <section class="problem-section" v-if="data.problem.outputDesc">
          <h3>输出描述</h3>
          <div class="md-block" v-html="md(data.problem.outputDesc)"></div>
        </section>

        <section class="problem-section" v-if="data.sampleCases.length">
          <h3>样例输入输出</h3>
          <div
            v-for="(c, idx) in data.sampleCases"
            :key="c.id || idx"
            class="sample-block"
          >
            <p>样例 {{ idx + 1 }} 输入：</p>
            <pre class="desc">{{ c.inputData }}</pre>
            <p>样例 {{ idx + 1 }} 输出：</p>
            <pre class="desc">{{ c.outputData }}</pre>
          </div>
        </section>

        <section
          class="problem-section"
          v-else-if="data.problem.sampleInput || data.problem.sampleOutput"
        >
          <h3>样例输入输出</h3>
          <div class="sample-block">
            <p>样例输入：</p>
            <pre class="desc">{{ data.problem.sampleInput }}</pre>
            <p>样例输出：</p>
            <pre class="desc">{{ data.problem.sampleOutput }}</pre>
          </div>
        </section>
      </div>

      <!-- 右侧代码编辑与提交区域 -->
      <div class="problem-right">
        <div class="editor-header">
          <div class="editor-title">代码编辑区</div>
          <el-select v-model="data.submitForm.language" size="small" style="width: 140px">
            <el-option label="C" value="C" />
            <el-option label="C++" value="C++" />
            <el-option label="Java" value="Java" />
            <el-option label="Python" value="Python" />
          </el-select>
        </div>

        <!-- 代码编辑器（带基础语法高亮） -->
        <div class="code-editor-wrapper">
          <Codemirror
            v-model="data.submitForm.code"
            :extensions="editorExtensions"
            :autofocus="true"
            :indent-with-tab="true"
            :tabSize="2"
            placeholder="在此编写你的代码，系统将根据该题配置的多组测试用例进行评测"
            style="height: 420px; border-radius: 4px; overflow: hidden;"
          />
        </div>

        <div class="editor-actions">
          <el-button type="primary" :loading="data.submitting" @click="submit">
            {{ data.submitting ? '提交中...' : '提交代码' }}
          </el-button>
          <el-button @click="backToList">返回题目列表</el-button>
        </div>

        <!-- 提交结果栏 -->
        <div class="result-bar" v-if="data.lastResult">
          <div class="result-summary">
            <span>最近一次提交结果：</span>
            <el-tag
              :type="statusTagType(data.lastResult.status)"
              effect="plain"
              style="margin-right: 8px"
            >
              {{ statusLabel(data.lastResult.status) }}
            </el-tag>
            <span class="summary-item">得分：{{ data.lastResult.score ?? '-' }}</span>
            <span class="summary-item">
              耗时：{{ data.lastResult.timeUsed ?? '-' }} ms
            </span>
            <span class="summary-item">
              内存：{{ data.lastResult.memoryUsed ?? '-' }} KB
            </span>
            <span
              v-if="data.lastResult.judgeTime"
              class="summary-item summary-time"
            >
              评测时间：{{ data.lastResult.judgeTime }}
            </span>
          </div>
          <div class="result-msg" v-if="data.lastResult.resultMsg">
            <pre class="result-msg-pre">{{ data.lastResult.resultMsg }}</pre>
          </div>
        </div>

        <div class="result-bar empty" v-else>
          尚未提交代码，提交后将在此处展示评测结果。
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'
import { Codemirror } from 'vue-codemirror'
import { cpp } from '@codemirror/lang-cpp'
import { java } from '@codemirror/lang-java'
import { python } from '@codemirror/lang-python'

const route = useRoute()
const router = useRouter()

const problemId = Number(route.params.id)
const contestIdFromRoute = route.query.contestId ? Number(route.query.contestId) : null

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  // 竞赛模式相关
  contestMode: !!contestIdFromRoute,
  contestId: contestIdFromRoute,
  contestInfo: null,
  // 题目信息
  problem: null,
  sampleCases: [],
  // 提交相关
  submitForm: {
    problemId,
    language: 'C++',
    code: '',
  },
  submitting: false,
  lastResult: null, // 最近一次提交的评测结果（后端返回的 Submission）
})

// 终止状态：一旦进入这些状态就停止轮询
const terminalStatuses = ['ACCEPTED', 'PARTIAL', 'WRONG_ANSWER', 'ERROR']

let statusPollTimer = null

// 根据当前语言动态返回 CodeMirror 扩展（语法高亮）
const editorExtensions = computed(() => {
  const lang = data.submitForm.language
  if (lang === 'C' || lang === 'C++') {
    return [cpp()]
  }
  if (lang === 'Java') {
    return [java()]
  }
  if (lang === 'Python') {
    return [python()]
  }
  return []
})

// Markdown 渲染封装
const md = (text) => renderMarkdown(text || '')

// 评测状态与颜色、文本映射（与提交记录页保持一致）
const statusConfig = {
  PENDING: { type: 'info', label: '等待评测' },
  JUDGING: { type: 'warning', label: '评测中' },
  ACCEPTED: { type: 'success', label: '通过' },
  PARTIAL: { type: 'warning', label: '部分通过' },
  WRONG_ANSWER: { type: 'danger', label: '答案错误' },
  ERROR: { type: 'danger', label: '系统错误' },
}

const statusTagType = (status) => {
  return statusConfig[status]?.type || 'info'
}

const statusLabel = (status) => {
  return statusConfig[status]?.label || status || '未知状态'
}

// 加载竞赛信息（仅在比赛模式下）
const loadContestInfo = () => {
  if (!data.contestMode || !data.contestId) return
  request.get(`/contest/selectById/${data.contestId}`).then((res) => {
    if (res.code === '200') {
      data.contestInfo = res.data || null
    }
  })
}

// 加载题目信息
const loadProblem = () => {
  if (!problemId) {
    ElMessage.error('题目ID不存在')
    backToList()
    return
  }
  request.get(`/problem/selectById/${problemId}`).then((res) => {
    if (res.code === '200') {
      data.problem = res.data || null
    }
  })
}

// 加载样例测试用例（isSample = 1 的用例），用于展示题面中的样例
const loadSampleCases = () => {
  if (!problemId) return
  request
    .get('/problem-testcase/list', { params: { problemId } })
    .then((res) => {
      const all = res.data || []
      data.sampleCases = all.filter((c) => c.isSample === 1)
    })
}

// 代码提交
const submit = () => {
  if (!data.user?.id) {
    ElMessage.error('请先登录')
    return
  }
  if (!data.submitForm.code) {
    ElMessage.error('请填写代码后再提交')
    return
  }
  data.submitting = true
  request
    .post('/submission/submit', {
      contestId: data.contestMode ? data.contestId : null,
      problemId: data.submitForm.problemId,
      studentId: data.user.id,
      language: data.submitForm.language,
      code: data.submitForm.code,
    })
    .then((res) => {
      if (res.code === '200') {
        // 后端返回刚创建的 Submission 对象，初始状态为 PENDING
        data.lastResult = res.data || null
        ElMessage.success('提交成功，已进入评测队列，请稍后在此处或提交记录中查看评测结果')

        // 开始轮询该提交的评测状态
        if (data.lastResult && data.lastResult.id) {
          startStatusPolling(data.lastResult.id)
        }
      } else {
        ElMessage.error(res.msg || '提交失败')
      }
    })
    .finally(() => {
      data.submitting = false
    })
}

// 停止状态轮询
const stopStatusPolling = () => {
  if (statusPollTimer) {
    clearInterval(statusPollTimer)
    statusPollTimer = null
  }
}

// 启动状态轮询：直到评测进入终止状态或请求失败
const startStatusPolling = (submissionId) => {
  stopStatusPolling()
  if (!submissionId) return

  statusPollTimer = setInterval(() => {
    request.get(`/submission/selectById/${submissionId}`).then((res) => {
      if (res.code !== '200' || !res.data) {
        // 查询不到记录或出错时停止轮询
        stopStatusPolling()
        return
      }
      const latest = res.data
      data.lastResult = latest

      const status = latest.status
      if (!status) return

      // 进入终止状态后停止轮询
      if (terminalStatuses.includes(status)) {
        stopStatusPolling()
      }
    })
  }, 2000) // 每 2 秒刷新一次
}

// 返回题目列表
const backToList = () => {
  if (data.contestMode && data.contestId) {
    router.push({ path: '/student/problem', query: { contestId: data.contestId } })
  } else {
    router.push('/student/problem')
  }
}

onMounted(() => {
  loadContestInfo()
  loadProblem()
  loadSampleCases()
})

onBeforeUnmount(() => {
  stopStatusPolling()
})
</script>

<style scoped>
.problem-detail-page {
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

.contest-tip {
  margin-bottom: 5px;
}

.problem-layout {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.problem-left {
  flex: 1.1;
  max-width: 55%;
  border-right: 1px solid #ebeef5;
  padding-right: 16px;
  max-height: calc(100vh - 160px);
  overflow-y: auto;
}

.problem-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.problem-header {
  margin-bottom: 16px;
}

.problem-title {
  margin: 0 0 8px;
}

.problem-meta {
  display: flex;
  gap: 8px;
  align-items: center;
}

.problem-section {
  margin-bottom: 16px;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.editor-title {
  font-weight: 500;
}

.editor-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.code-editor-wrapper {
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
}

.code-editor-wrapper :deep(.cm-editor) {
  font-family: SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono',
    'Courier New', monospace;
  font-size: 13px;
}

.code-editor-wrapper :deep(.cm-scroller) {
  min-height: 380px;
}

.desc {
  background: #f8f8f8;
  padding: 10px;
  border-radius: 4px;
  white-space: pre-wrap;
}

.md-block {
  background: #f8f8f8;
  padding: 10px;
  border-radius: 4px;
  line-height: 1.6;
  font-size: 14px;
}

.md-block p {
  margin: 0 0 8px;
}

.md-block code {
  background: #eee;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono',
    'Courier New', monospace;
}

.md-block pre {
  background: #2d2d2d;
  color: #f8f8f2;
  padding: 10px;
  border-radius: 4px;
  overflow: auto;
}

.sample-block {
  margin-bottom: 10px;
}

.result-bar {
  margin-top: 8px;
  padding: 10px 12px;
  border-radius: 6px;
  background-color: #f9fafb;
  border: 1px solid #e5e7eb;
  font-size: 13px;
}

.result-bar.empty {
  color: #9ca3af;
}

.result-summary {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}

.summary-item {
  margin-left: 4px;
}

.summary-time {
  color: #6b7280;
}

.result-msg-pre {
  margin: 0;
  white-space: pre-wrap;
  font-family: SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono',
    'Courier New', monospace;
}

@media (max-width: 960px) {
  .problem-layout {
    flex-direction: column;
  }

  .problem-left {
    max-width: 100%;
    border-right: none;
    border-bottom: 1px solid #ebeef5;
    padding-right: 0;
    padding-bottom: 16px;
  }
}
</style>
