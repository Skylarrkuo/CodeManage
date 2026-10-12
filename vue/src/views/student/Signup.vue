<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <div>以下为当前账号的报名记录：</div>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table :data="data.tableData" stripe>
        <el-table-column label="ID" prop="id" width="60" />
        <el-table-column label="竞赛ID" prop="contestId" width="80" />
        <el-table-column label="审核状态" prop="status" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="plain">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报名时间" prop="applyTime" width="180" />
        <el-table-column label="审核教师" prop="auditTeacherName" width="120" />
        <el-table-column label="审核时间" prop="auditTime" width="180" />
        <el-table-column label="备注" prop="remark" />
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
import request from '@/utils/request'

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
})

// 报名审核状态与颜色、文本映射
const statusConfig = {
  APPLIED: { type: 'info', label: '待审核' },
  APPROVED: { type: 'success', label: '已通过' },
  REJECTED: { type: 'danger', label: '已拒绝' },
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
    .get('/signup/selectPage', {
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
