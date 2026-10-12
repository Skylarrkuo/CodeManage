<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input
        v-model="data.contestId"
        style="width: 160px; margin-right: 10px"
        placeholder="竞赛ID"
      ></el-input>
      <el-input
        v-model="data.studentId"
        style="width: 160px; margin-right: 10px"
        placeholder="学生ID"
      ></el-input>
      <el-select
        v-model="data.status"
        placeholder="报名状态"
        style="width: 160px; margin-right: 10px"
        clearable
      >
        <el-option label="已报名" value="APPLIED" />
        <el-option label="已通过" value="APPROVED" />
        <el-option label="已拒绝" value="REJECTED" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin-left: 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px; display: flex; justify-content: space-between">
        <div>
          <el-button
            type="success"
            size="small"
            :disabled="!data.multipleSelection.length"
            @click="openBatchApprove"
          >
            批量通过
          </el-button>
          <el-button
            type="warning"
            size="small"
            style="margin-left: 8px"
            :disabled="!data.multipleSelection.length"
            @click="openBatchReject"
          >
            批量拒绝
          </el-button>
        </div>
        <div style="font-size: 13px; color: #909399">
          已选 {{ data.multipleSelection.length }} 条
        </div>
      </div>

      <el-table
        :data="data.tableData"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column label="ID" prop="id" width="60" />
        <el-table-column label="竞赛ID" prop="contestId" width="80" />
        <el-table-column label="学生ID" prop="studentId" width="80" />
        <el-table-column label="状态" prop="status" width="80" />
        <el-table-column label="报名时间" prop="applyTime" width="180" />
        <el-table-column label="审核教师" prop="auditTeacherName" width="120" />
        <el-table-column label="审核时间" prop="auditTime" width="180" />
        <el-table-column label="备注" prop="remark" />
        <el-table-column label="操作" align="center" width="220">
          <template #default="scope">
            <el-button type="success" size="small" @click="openApprove(scope.row)">
              通过
            </el-button>
            <el-button type="warning" size="small" @click="openReject(scope.row)">
              拒绝
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
      v-model="data.auditVisible"
      title="审核"
      width="30%"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="data.auditForm" label-width="80px" style="padding-right: 30px">
        <el-form-item label="审核老师ID">
          <el-input v-model="data.auditForm.auditTeacherId" autocomplete="off" />
        </el-form-item>
        <el-form-item label="审核老师名">
          <el-input v-model="data.auditForm.auditTeacherName" autocomplete="off" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="data.auditForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.auditVisible = false">取 消</el-button>
          <el-button type="primary" @click="doAudit">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  contestId: null,
  studentId: null,
  status: null,
  // 当前登录用户信息（用于自动填充审核老师）
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  auditVisible: false,
  auditForm: {},
  auditType: 'approve', // 单条审核类型
  auditMode: 'single', // single | batch
  multipleSelection: [], // 勾选的报名记录
})

const load = () => {
  request
    .get('/signup/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        contestId: data.contestId,
        studentId: data.studentId,
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
  data.contestId = null
  data.studentId = null
  data.status = null
  load()
}

const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', {
    type: 'warning',
  })
    .then(() => {
      request.delete('/signup/delete/' + id).then((res) => {
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

const openApprove = (row) => {
  data.auditType = 'approve'
  data.auditMode = 'single'
  data.auditForm = {
    id: row.id,
    auditTeacherId: data.user.id,
    auditTeacherName: data.user.name,
    remark: '',
  }
  data.auditVisible = true
}

const openReject = (row) => {
  data.auditType = 'reject'
  data.auditMode = 'single'
  data.auditForm = {
    id: row.id,
    auditTeacherId: data.user.id,
    auditTeacherName: data.user.name,
    remark: '',
  }
  data.auditVisible = true
}

// 处理多选勾选变化
const handleSelectionChange = (val) => {
  data.multipleSelection = val
}

// 批量通过
const openBatchApprove = () => {
  if (!data.multipleSelection.length) {
    ElMessage.warning('请先勾选要审核的报名记录')
    return
  }
  data.auditType = 'approve'
  data.auditMode = 'batch'
  data.auditForm = {
    ids: data.multipleSelection.map((item) => item.id),
    auditTeacherId: data.user.id,
    auditTeacherName: data.user.name,
    remark: '',
  }
  data.auditVisible = true
}

// 批量拒绝
const openBatchReject = () => {
  if (!data.multipleSelection.length) {
    ElMessage.warning('请先勾选要审核的报名记录')
    return
  }
  data.auditType = 'reject'
  data.auditMode = 'batch'
  data.auditForm = {
    ids: data.multipleSelection.map((item) => item.id),
    auditTeacherId: data.user.id,
    auditTeacherName: data.user.name,
    remark: '',
  }
  data.auditVisible = true
}

const doAudit = () => {
  let url
  let payload = { ...data.auditForm }

  if (data.auditMode === 'single') {
    url =
      data.auditType === 'approve'
        ? '/signup/approve/' + data.auditForm.id
        : '/signup/reject/' + data.auditForm.id
  } else {
    url =
      data.auditType === 'approve' ? '/signup/approveBatch' : '/signup/rejectBatch'
  }

  request.put(url, payload).then((res) => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.auditVisible = false
      data.multipleSelection = []
      load()
    } else {
      ElMessage.error(res.msg)
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
