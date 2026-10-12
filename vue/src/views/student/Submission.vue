<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <div>以下为当前账号的提交记录（评测结果）：</div>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table :data="data.tableData" stripe>
        <el-table-column label="ID" prop="id" width="60" />
        <el-table-column label="竞赛ID" prop="contestId" width="80" />
        <el-table-column label="题目ID" prop="problemId" width="80" />
        <el-table-column label="语言" prop="language" width="100" />
        <el-table-column label="状态" prop="status" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="plain">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="得分" prop="score" width="80" />
        <el-table-column label="耗时(ms)" prop="timeUsed" width="100" />
        <el-table-column label="内存(KB)" prop="memoryUsed" width="100" />
        <el-table-column label="评测详情" width="100" align="center">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              @click="viewDetail(row)"
            >查看详情</el-button>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" prop="submitTime" width="180" />
        <el-table-column label="评测时间" prop="judgeTime" width="180" />
        <el-table-column label="代码" width="120" align="center">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              @click="viewCode(row)"
            >查看代码</el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              @click="handleRejudge(row)"
            >重测</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="data.codeDialogVisible"
      title="提交代码"
      width="60%"
      :close-on-click-modal="false"
    >
      <div style="margin-bottom: 10px">
        语言：{{ data.currentLanguage || '未知' }}
      </div>
      <el-input
        v-model="data.currentCode"
        type="textarea"
        :rows="18"
        :readonly="true"
      />
    </el-dialog>

    <el-dialog
      v-model="data.detailDialogVisible"
      title="评测详情"
      width="60%"
      :close-on-click-modal="false"
    >
      <div style="margin-bottom: 10px">
        结果说明：{{ data.currentResultMsg || '无' }}
      </div>
      <div v-if="!data.currentDetailList || !data.currentDetailList.length">
        当前提交暂无测试点详情，仅有结果说明。
      </div>
      <div v-else>
        <el-table :data="data.currentDetailList" size="small" stripe>
          <el-table-column label="#" prop="index" width="60" />
          <el-table-column label="类型" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="row.sample ? 'info' : 'success'">
                {{ row.sample ? '样例' : '测试点' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="140">
            <template #default="{ row }">
              <el-tag
                size="small"
                :type="row.accepted ? 'success' : 'danger'"
              >
                {{ row.status || (row.accepted ? '通过' : '未通过') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="耗时(ms)" prop="time" width="100" />
          <el-table-column label="内存(KB)" prop="memory" width="100" />
          <el-table-column label="输入预览" prop="input" />
          <el-table-column label="期望输出预览" prop="expectedOutput" />
        </el-table>

        <div style="margin-top: 10px" v-if="data.currentDetailErrorList.length">
          <h4>未通过测试点详情</h4>
          <el-collapse>
            <el-collapse-item
              v-for="item in data.currentDetailErrorList"
              :key="item.index"
              :title="`测试点 #${item.index} - ${item.status || '未通过'}`"
            >
              <p><strong>输入：</strong></p>
              <pre style="white-space: pre-wrap">{{ item.input }}</pre>
              <p><strong>期望输出：</strong></p>
              <pre style="white-space: pre-wrap">{{ item.expectedOutput }}</pre>
              <p v-if="item.stdout"><strong>实际输出(stdout)：</strong></p>
              <pre v-if="item.stdout" style="white-space: pre-wrap">{{ item.stdout }}</pre>
              <p v-if="item.stderr"><strong>错误输出(stderr)：</strong></p>
              <pre v-if="item.stderr" style="white-space: pre-wrap">{{ item.stderr }}</pre>
              <p v-if="item.compileOutput"><strong>编译输出：</strong></p>
              <pre v-if="item.compileOutput" style="white-space: pre-wrap">{{ item.compileOutput }}</pre>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>
    </el-dialog>

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
import request from '@/utils/request'

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  codeDialogVisible: false,
  currentCode: '',
  currentLanguage: '',
  detailDialogVisible: false,
  currentDetailList: [],
  currentDetailErrorList: [],
  currentResultMsg: '',
})

// 评测状态与颜色、文本映射
const statusConfig = {
  PENDING: { type: 'info', label: '排队中' },
  JUDGING: { type: 'warning', label: '评测中' },
  ACCEPTED: { type: 'success', label: '通过' },
  WRONG_ANSWER: { type: 'danger', label: '答案错误' },
  ERROR: { type: 'danger', label: '系统错误' },
}

const statusTagType = (status) => {
  return statusConfig[status]?.type || 'info'
}

const statusLabel = (status) => {
  return statusConfig[status]?.label || status || '未知状态'
}

const load = () => {
  if (!data.user?.id) return
  request
    .get('/submission/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        studentId: data.user.id,
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

const viewCode = (row) => {
  if (!row) return
  data.currentCode = row.code || ''
  data.currentLanguage = row.language || ''
  data.codeDialogVisible = true
}

// 查看评测详情（各测试点结果）
const viewDetail = (row) => {
  if (!row) return
  data.currentResultMsg = row.resultMsg || ''
  if (!row.detail) {
    data.currentDetailList = []
    data.currentDetailErrorList = []
    data.detailDialogVisible = true
    return
  }
  try {
    const list = JSON.parse(row.detail)
    data.currentDetailList = Array.isArray(list) ? list : []
    data.currentDetailErrorList = data.currentDetailList.filter(
      (item) => !item.accepted,
    )
  } catch (e) {
    data.currentDetailList = []
    data.currentDetailErrorList = []
  }
  data.detailDialogVisible = true
}

// 重新评测某一条提交记录
const handleRejudge = (row) => {
  if (!row?.id) return
  request.post(`/submission/rejudge/${row.id}`).then((res) => {
    if (res.code === '200') {
      // 重新加载当前页数据，刷新评测结果
      load()
    }
  })
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
