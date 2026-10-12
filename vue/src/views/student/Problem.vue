<template>
  <div>
    <!-- 竞赛模式提示 -->
    <div v-if="data.contestMode" class="card" style="margin-bottom: 5px">
      <div>
        当前处于【{{ data.contestInfo?.name || '竞赛' }}】比赛模式，只展示本场竞赛的题目。
      </div>
      <div v-if="data.contestInfo">
        比赛时间：{{ data.contestInfo.startTime }} ~ {{ data.contestInfo.endTime }}
      </div>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-input
        v-model="data.title"
        style="width: 220px; margin-right: 10px"
        placeholder="请输入题目标题"
        :disabled="data.contestMode"
      ></el-input>
      <el-select
        v-model="data.difficulty"
        placeholder="难度"
        style="width: 140px; margin-right: 10px"
        clearable
        :disabled="data.contestMode"
      >
        <el-option label="简单" value="简单" />
        <el-option label="中等" value="中等" />
        <el-option label="困难" value="困难" />
      </el-select>
      <el-button type="primary" @click="load" :disabled="data.contestMode">查询</el-button>
      <el-button type="info" style="margin-left: 10px" @click="reset" :disabled="data.contestMode">
        重置
      </el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table :data="data.tableData" stripe>
        <el-table-column label="ID" prop="id" width="60" />
        <el-table-column label="标题" prop="title" />
        <el-table-column label="难度" prop="difficulty" width="80" />
        <el-table-column label="类型" prop="type" width="100" />
        <el-table-column label="所属竞赛ID" prop="contestId" width="120" />
        <el-table-column label="操作" align="center" width="220">
          <template #default="scope">
            <el-button type="primary" size="small" @click="goProblemDetail(scope.row)">
              查看题目
            </el-button>
            <el-button type="success" size="small" @click="goProblemDetail(scope.row)">
              {{ data.contestMode ? '比赛提交' : '提交代码' }}
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
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  title: null,
  difficulty: null,
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  // 竞赛模式相关
  contestMode: !!route.query.contestId,
  contestId: route.query.contestId ? Number(route.query.contestId) : null,
  contestInfo: null,
})

// 加载竞赛基本信息（仅在比赛模式下）
const loadContestInfo = () => {
  if (!data.contestMode || !data.contestId) return
  request.get(`/contest/selectById/${data.contestId}`).then((res) => {
    if (res.code === '200') {
      data.contestInfo = res.data || null
    }
  })
}

const load = () => {
  request
    .get('/problem/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        title: data.title,
        difficulty: data.difficulty,
        type: data.contestMode ? 'CONTEST' : 'PRACTICE',
        contestId: data.contestMode ? data.contestId : null,
        // 学生端公开题库只展示 isPublic = 1 的题目，比赛模式下不加该过滤
        isPublic: data.contestMode ? null : 1,
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
  data.title = null
  data.difficulty = null
  load()
}
const goProblemDetail = (row) => {
  if (!row?.id) return
  const query = data.contestMode && data.contestId ? { contestId: data.contestId } : {}
  router.push({ path: `/student/problem/${row.id}`, query })
}

loadContestInfo()
load()
</script>

<style scoped>
.card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
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
</style>

