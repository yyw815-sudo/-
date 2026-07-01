<template>
  <div class="announcement-container">
    <PageHeader 
      title="系统公告" 
      subtitle="管理系统公告内容，发布重要通知"
      :icon="Bell"
      iconBg="linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)"
    >
      <template #actions>
        <el-button type="primary" @click="handleAdd" class="add-btn">
          <el-icon><Plus /></el-icon>
          新增公告
        </el-button>
      </template>
    </PageHeader>
    
    <ContentCard>
      <template #header>
        <div class="search-header">
          <div class="search-wrapper">
            <el-input 
              v-model="searchForm.keyword" 
              placeholder="请输入关键词搜索" 
              clearable
              @keyup.enter="loadData"
              class="search-input"
            >
              <template #prefix>
                <el-icon class="search-icon"><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="loadData" class="search-btn">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="table-wrapper">
        <el-table :data="tableData" style="width: 100%" v-loading="loading" class="custom-table">
          <el-table-column prop="title" label="公告标题" min-width="220">
            <template #default="{ row }">
              <div class="title-cell">
                <el-tag v-if="row.type" :type="getTypeTag(row.type)" size="small" class="type-tag">
                  {{ row.type }}
                </el-tag>
                <span class="title-text">{{ row.title }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="target" label="发布对象" width="120">
            <template #default="{ row }">
              <el-tag type="info" size="small">
                {{ row.target === 'all' ? '全部用户' : row.target === 'user' ? '普通用户' : '管理员' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag 
                :type="row.status === 1 ? 'success' : 'danger'" 
                :effect="row.status === 1 ? 'light' : 'dark'"
                size="small"
              >
                <span class="status-dot" :class="row.status === 1 ? 'active' : 'inactive'"></span>
                {{ row.status === 1 ? '已发布' : '已下架' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="publishTime" label="发布时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.publishTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="结束时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.endTime) || '无' }}
            </template>
          </el-table-column>
          <el-table-column prop="content" label="公告内容" min-width="200">
            <template #default="{ row }">
              <el-popover
                placement="top-start"
                :width="300"
                trigger="hover"
              >
                <template #reference>
                  <span class="content-preview">{{ row.content || '-' }}</span>
                </template>
                <template #default>
                  <div class="content-full">{{ row.content }}</div>
                </template>
              </el-popover>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button size="small" type="primary" @click="handleEdit(row)" class="action-btn edit-btn">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button 
                  size="small" 
                  :type="row.status === 1 ? 'warning' : 'success'" 
                  @click="handleToggleStatus(row)"
                  class="action-btn"
                >
                  <el-icon><component :is="row.status === 1 ? Close : Check" /></el-icon>
                  {{ row.status === 1 ? '下架' : '发布' }}
                </el-button>
                <el-button size="small" type="danger" @click="handleDelete(row)" class="action-btn delete-btn">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <template #footer>
        <div class="pagination-wrapper">
          <el-pagination 
            :current-page="pagination.pageNum" 
            :page-size="pagination.pageSize" 
            :total="pagination.total"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
            layout="total, prev, pager, next, jumper"
            background
          />
        </div>
      </template>
    </ContentCard>
    
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑公告' : '新增公告'" 
      width="700px"
      class="custom-dialog"
      :close-on-click-modal="false"
      draggable
    >
      <div class="dialog-content">
        <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
          <div class="form-row">
            <el-form-item label="公告标题" prop="title" class="form-item">
              <el-input v-model="formData.title" placeholder="请输入公告标题" size="large" />
            </el-form-item>
            <el-form-item label="公告类型" prop="type" class="form-item">
              <el-select v-model="formData.type" placeholder="请选择公告类型" size="large">
                <el-option label="系统通知" value="系统通知" />
                <el-option label="重要公告" value="重要公告" />
                <el-option label="更新日志" value="更新日志" />
              </el-select>
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="发布对象" prop="target" class="form-item">
              <el-select v-model="formData.target" placeholder="请选择发布对象" size="large">
                <el-option label="全部用户" value="all" />
                <el-option label="普通用户" value="user" />
                <el-option label="管理员" value="admin" />
              </el-select>
            </el-form-item>
            <el-form-item label="结束时间" prop="endTime" class="form-item">
              <el-date-picker v-model="formData.endTime" type="datetime" placeholder="选择结束时间" size="large" />
            </el-form-item>
          </div>
          
          <el-form-item label="公告内容" prop="content">
            <el-input 
              v-model="formData.content" 
              type="textarea" 
              :rows="6" 
              placeholder="请输入公告内容"
              resize="none"
            />
          </el-form-item>
          
          <el-form-item label="状态">
            <div class="status-radio">
              <el-radio-group v-model="formData.status">
                <el-radio :value="1" class="radio-item">
                  <span class="radio-dot active"></span>
                  <span>已发布</span>
                </el-radio>
                <el-radio :value="0" class="radio-item">
                  <span class="radio-dot inactive"></span>
                  <span>已下架</span>
                </el-radio>
              </el-radio-group>
            </div>
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting" class="confirm-btn">
            {{ isEdit ? '保存修改' : '发布公告' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Plus, Search, Edit, Delete, Check, Close, Bell } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getAnnouncementList, 
  createAnnouncement, 
  updateAnnouncement, 
  deleteAnnouncement 
} from '@/api/systemCenter'
import PageHeader from '@/components/PageHeader.vue'
import ContentCard from '@/components/ContentCard.vue'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const searchForm = reactive({
  keyword: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])

const formData = reactive({
  announceId: null,
  title: '',
  content: '',
  type: '系统通知',
  target: 'all',
  endTime: null,
  status: 1
})

const formRules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }],
  type: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
  target: [{ required: true, message: '请选择发布对象', trigger: 'change' }]
}

const getTypeTag = (type) => {
  const tags = {
    '系统通知': 'info',
    '重要公告': 'danger',
    '更新日志': 'success'
  }
  return tags[type] || 'info'
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword
    }
    const res = await getAnnouncementList(params)
    tableData.value = res.data.list
    pagination.total = res.data.total
  } catch (error) {
    console.error('获取公告列表失败:', error)
    ElMessage.error('获取公告列表失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  pagination.pageNum = page
  loadData()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  formData.announceId = null
  formData.title = ''
  formData.content = ''
  formData.type = '系统通知'
  formData.target = 'all'
  formData.endTime = null
  formData.status = 1
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  formData.announceId = row.announceId
  formData.title = row.title
  formData.content = row.content
  formData.type = row.type
  formData.target = row.target
  formData.endTime = row.endTime
  formData.status = row.status
  dialogVisible.value = true
}

const handleToggleStatus = (row) => {
  const action = row.status === 1 ? '下架' : '发布'
  ElMessageBox.confirm(`确定要${action}这条公告吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: row.status === 1 ? 'warning' : 'success'
  }).then(async () => {
    try {
      await updateAnnouncement(row.announceId, { ...row, status: row.status === 1 ? 0 : 1 })
      ElMessage.success(`${action}成功`)
      loadData()
    } catch (error) {
      ElMessage.error(`${action}失败`)
    }
  }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这条公告吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteAnnouncement(row.announceId)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        await updateAnnouncement(formData.announceId, formData)
        ElMessage.success('更新成功')
      } else {
        await createAnnouncement(formData)
        ElMessage.success('发布成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '发布失败')
    } finally {
      submitting.value = false
    }
  })
}

loadData()
</script>

<style scoped>
.announcement-container {
  padding: 0 24px 24px;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 10px;
  font-weight: 500;
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.search-wrapper {
  display: flex;
  gap: 12px;
}

.search-input {
  width: 360px;
}

.search-icon {
  color: #94a3b8;
}

.search-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

.filter-tabs {
  display: flex;
  gap: 8px;
}

.table-wrapper {
  overflow-x: auto;
}

.custom-table {
  --el-table-header-text-color: #64748b;
  --el-table-row-hover-bg-color: rgba(99, 102, 241, 0.03);
  
  :deep(.el-table__header-wrapper) {
    background: #f8fafc;
  }
  
  :deep(.el-table__header th) {
    padding: 16px 12px;
    font-weight: 600;
    font-size: 14px;
    border-bottom: 2px solid #e2e8f0;
  }
  
  :deep(.el-table__body td) {
    padding: 16px 12px;
    border-bottom: 1px solid #f1f5f9;
  }
  
  :deep(.el-table__row:hover td) {
    background: rgba(99, 102, 241, 0.03) !important;
  }
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.type-tag {
  flex-shrink: 0;
}

.title-text {
  font-size: 14px;
  color: #1e293b;
  font-weight: 500;
}

.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 4px;
  
  &.active {
    background: #10b981;
  }
  
  &.inactive {
    background: #ef4444;
  }
}

.action-buttons {
  display: flex;
  gap: 6px;
}

.action-btn {
  padding: 4px 10px;
  border-radius: 6px;
  
  &.edit-btn {
    background: rgba(99, 102, 241, 0.1);
    color: #6366f1;
    border-color: rgba(99, 102, 241, 0.2);
  }
  
  &.delete-btn {
    background: rgba(239, 68, 68, 0.1);
    color: #ef4444;
    border-color: rgba(239, 68, 68, 0.2);
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  
  :deep(.el-pagination) {
    .btn-prev, .btn-next, .el-pager li {
      border-radius: 6px;
    }
    
    .el-pager li.active {
      background: #6366f1;
    }
  }
}

.custom-dialog {
  :deep(.el-dialog__header) {
    padding: 24px;
    background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
    border-radius: 16px 16px 0 0;
    
    .el-dialog__title {
      font-size: 18px;
      font-weight: 600;
      color: #1e293b;
    }
  }
  
  :deep(.el-dialog__body) {
    padding: 24px;
    max-height: 70vh;
    overflow-y: auto;
  }
  
  :deep(.el-dialog__footer) {
    padding: 16px 24px 24px;
    border-top: 1px solid #f1f5f9;
  }
}

.dialog-content {
  .form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
    margin-bottom: 16px;
  }
  
  .form-item {
    margin-bottom: 0;
  }
}

.status-radio {
  .radio-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    border-radius: 8px;
    transition: background 0.2s;
    
    &:hover {
      background: #f8fafc;
    }
  }
  
  .radio-dot {
    display: inline-block;
    width: 10px;
    height: 10px;
    border-radius: 50%;
    
    &.active {
      background: #10b981;
    }
    
    &.inactive {
      background: #ef4444;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;

  .cancel-btn {
    padding: 10px 24px;
    border-radius: 8px;
  }

  .confirm-btn {
    padding: 10px 24px;
    border-radius: 8px;
    font-weight: 500;
  }
}

.content-preview {
  font-size: 13px;
  color: #64748b;
  cursor: pointer;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;

  &:hover {
    color: #6366f1;
  }
}

.content-full {
  font-size: 14px;
  color: #334155;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>