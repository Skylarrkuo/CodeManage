<template>
  <div>
    <!-- 竞赛题目管理模式提示 -->
    <div v-if="data.contestMode" class="card" style="margin-bottom: 5px">
      <div>当前正在管理【竞赛 ID：{{ data.contestId }}】的题目，只展示本场竞赛题目。</div>
      <div style="margin-top: 5px">
        <el-button type="primary" size="small" @click="backToContest">返回竞赛管理</el-button>
      </div>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-input
        v-model="data.title"
        style="width: 220px; margin-right: 10px"
        placeholder="请输入题目标题"
      ></el-input>
      <el-select
        v-model="data.difficulty"
        placeholder="难度"
        style="width: 140px; margin-right: 10px"
        clearable
      >
        <el-option label="简单" value="简单" />
        <el-option label="中等" value="中等" />
        <el-option label="困难" value="困难" />
      </el-select>
      <el-input
        v-model="data.contestId"
        style="width: 140px; margin-right: 10px"
        placeholder="竞赛ID"
        :disabled="data.contestMode"
      ></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin-left: 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增题目</el-button>
      </div>
      <el-table :data="data.tableData" stripe>
        <el-table-column label="ID" prop="id" width="60" />
        <el-table-column label="标题" prop="title" />
        <el-table-column label="难度" prop="difficulty" width="80" />
        <el-table-column label="类型" prop="type" width="100" />
        <el-table-column label="所属竞赛ID" prop="contestId" width="120" />
        <el-table-column label="公开" prop="isPublic" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.isPublic === 1 ? 'success' : 'info'" effect="plain">
              {{ scope.row.isPublic === 1 ? '公开' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180" />
        <el-table-column label="测试用例" align="center" width="120">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="openCases(scope.row)">
              配置用例
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">
              删除
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

    <el-dialog
      v-model="data.formVisible"
      title="题目信息"
      width="50%"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="data.form" label-width="90px" style="padding-right: 30px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="data.form.title" autocomplete="off" />
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="data.form.difficulty" placeholder="请选择难度" style="width: 100%">
            <el-option label="简单" value="简单" />
            <el-option label="中等" value="中等" />
            <el-option label="困难" value="困难" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="data.form.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="练习" value="PRACTICE" />
            <el-option label="竞赛" value="CONTEST" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属竞赛ID" prop="contestId">
          <el-input v-model="data.form.contestId" autocomplete="off" />
        </el-form-item>
        <el-form-item label="公开状态" prop="isPublic">
          <el-radio-group v-model="data.form.isPublic">
            <el-radio :label="1">公开</el-radio>
            <el-radio :label="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="题目描述" prop="description">
          <el-input
            v-model="data.form.description"
            type="textarea"
            :rows="4"
            placeholder="支持 Markdown 语法，例如：# 标题、**加粗**、`代码` 等"
          />
        </el-form-item>
        <el-form-item label="输入描述" prop="inputDesc">
          <el-input
            v-model="data.form.inputDesc"
            type="textarea"
            :rows="3"
            placeholder="支持 Markdown 语法"
          />
        </el-form-item>
        <el-form-item label="输出描述" prop="outputDesc">
          <el-input
            v-model="data.form.outputDesc"
            type="textarea"
            :rows="3"
            placeholder="支持 Markdown 语法"
          />
        </el-form-item>
        <el-form-item label="样例输入" prop="sampleInput">
          <el-input
            v-model="data.form.sampleInput"
            type="textarea"
            :rows="3"
            placeholder="可使用 Markdown 语法书写多行输入说明"
          />
        </el-form-item>
        <el-form-item label="样例输出" prop="sampleOutput">
          <el-input
            v-model="data.form.sampleOutput"
            type="textarea"
            :rows="3"
            placeholder="可使用 Markdown 语法书写多行输出说明"
          />
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-input v-model="data.form.tags" autocomplete="off" placeholder="例如：入门,数学" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">保 存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 测试用例配置弹窗 -->
    <el-dialog
      v-model="data.caseVisible"
      title="测试用例配置"
      width="60%"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-table :data="data.caseList" size="small" style="width: 100%">
        <el-table-column label="#" type="index" width="50" />
        <el-table-column label="样例" width="80">
          <template #default="scope">
            <el-switch
              v-model="scope.row.isSample"
              :active-value="1"
              :inactive-value="0"
            />
          </template>
        </el-table-column>
        <el-table-column label="输入">
          <template #default="scope">
            <el-input v-model="scope.row.inputData" type="textarea" :rows="2" />
          </template>
        </el-table-column>
        <el-table-column label="输出">
          <template #default="scope">
            <el-input v-model="scope.row.outputData" type="textarea" :rows="2" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="scope">
            <el-button type="danger" text size="small" @click="removeCase(scope.$index)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addCase">新增用例</el-button>
          <el-button type="primary" @click="saveCases">保 存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()

const contestIdFromRoute = route.query.contestId ? Number(route.query.contestId) : null

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  title: null,
  difficulty: null,
  // 竞赛题目管理模式相关
  contestId: contestIdFromRoute,
  contestMode: !!contestIdFromRoute,
  formVisible: false,
  form: {},
  // 测试用例配置
  caseVisible: false,
  currentProblemId: null,
  caseList: [],
})

const load = () => {
  request
    .get('/problem/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        title: data.title,
        difficulty: data.difficulty,
        contestId: data.contestId,
        // 在竞赛模式下，仅查询本竞赛的题目
        type: data.contestMode ? 'CONTEST' : null,
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
  // 在竞赛模式下保留当前竞赛 ID
  if (!data.contestMode) {
    data.contestId = null
  }
  load()
}

const handleAdd = () => {
  // 若从“竞赛管理”进入，则默认新增的题目归属该竞赛，类型为 CONTEST
  if (data.contestMode && data.contestId) {
    data.form = { type: 'CONTEST', contestId: data.contestId, isPublic: 1 }
  } else {
    data.form = { type: 'PRACTICE', isPublic: 1 }
  }
  data.formVisible = true
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

const backToContest = () => {
  router.push('/teacher/contest')
}

const add = () => {
  request.post('/problem/add', data.form).then((res) => {
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
  request.put('/problem/update', data.form).then((res) => {
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
      request.delete('/problem/delete/' + id).then((res) => {
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

// ---------------- 测试用例管理 ----------------
const openCases = (row) => {
  if (!row?.id) return
  data.currentProblemId = row.id
  request
    .get('/problem-testcase/list', { params: { problemId: row.id } })
    .then((res) => {
      data.caseList = res.data || []
      data.caseVisible = true
    })
}

const addCase = () => {
  data.caseList.push({
    id: null,
    problemId: data.currentProblemId,
    inputData: '',
    outputData: '',
    isSample: 0,
    orderNo: data.caseList.length + 1,
  })
}

const removeCase = (index) => {
  const item = data.caseList[index]
  if (item && item.id) {
    request.delete('/problem-testcase/delete/' + item.id).then((res) => {
      if (res.code === '200') {
        data.caseList.splice(index, 1)
      }
    })
  } else {
    data.caseList.splice(index, 1)
  }
}

const saveCases = () => {
  const requests = []
  data.caseList.forEach((c, idx) => {
    c.orderNo = idx + 1
    if (!c.id) {
      requests.push(request.post('/problem-testcase/add', c))
    } else {
      requests.push(request.put('/problem-testcase/update', c))
    }
  })
  Promise.all(requests).then(() => {
    data.caseVisible = false
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

