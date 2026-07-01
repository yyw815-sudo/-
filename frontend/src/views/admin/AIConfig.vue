<template>
  <div class="ai-config-container">
    <PageHeader 
      title="AI配置" 
      subtitle="管理AI相关接口配置，配置API密钥"
      :icon="Setting"
      iconBg="linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)"
    >
      <template #actions>
        <el-button type="primary" @click="handleAdd" class="add-btn">
          <el-icon><Plus /></el-icon>
          新增配置
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
          <el-table-column prop="apiName" label="配置名称" min-width="160">
            <template #default="{ row }">
              <div class="name-cell">
                <div class="name-icon" :style="{ background: getTypeColor(row.apiType) }">
                  <el-icon :size="18"><component :is="getTypeIcon(row.apiType)" /></el-icon>
                </div>
                <span class="name-text">{{ row.apiName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="apiType" label="接口类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getTypeTag(row.apiType)" size="small" class="type-tag">
                {{ row.apiType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="endpoint" label="接口地址" min-width="280">
            <template #default="{ row }">
              <div class="endpoint-cell">
                <el-icon class="link-icon"><Link /></el-icon>
                <span class="endpoint-text">{{ row.endpoint }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="appKey" label="API Key" min-width="150">
            <template #default="{ row }">
              <div class="mask-cell">
                <el-icon class="lock-icon"><Lock /></el-icon>
                <span class="mask-text">{{ row.appKey ? '******' : '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <div class="status-switch">
                <el-switch 
                  :value="row.status === 1" 
                  @change="handleToggle(row)"
                  active-color="#10b981"
                  inactive-color="#ef4444"
                />
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="remarks" label="备注" min-width="150">
            <template #default="{ row }">
              <el-popover
                placement="top-start"
                :width="280"
                trigger="hover"
              >
                <template #reference>
                  <span class="remark-text">{{ row.remarks || '-' }}</span>
                </template>
                <template #default>
                  <div class="remark-full">{{ row.remarks || '无备注信息' }}</div>
                </template>
              </el-popover>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button size="small" type="primary" @click="handleEdit(row)" class="action-btn edit-btn">
                  <el-icon><Edit /></el-icon>
                  编辑
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
      :title="isEdit ? '编辑配置' : '新增配置'" 
      width="700px"
      class="custom-dialog"
      :close-on-click-modal="false"
      draggable
    >
      <div class="dialog-content">
        <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
          <div class="form-row">
            <el-form-item label="配置名称" prop="apiName" class="form-item">
              <el-input v-model="formData.apiName" placeholder="请输入配置名称" size="large" />
            </el-form-item>
            <el-form-item label="接口类型" prop="apiType" class="form-item">
              <el-select v-model="formData.apiType" placeholder="请选择接口类型" size="large" @change="handleApiTypeChange">
                <el-option label="AI咨询" value="AI咨询" />
                <el-option label="健康评估" value="健康评估" />
                <el-option label="智能推荐" value="智能推荐" />
                <el-option label="其他" value="其他" />
              </el-select>
              <el-input 
                v-if="formData.apiType === '其他'" 
                v-model="customApiType" 
                placeholder="请输入自定义接口类型" 
                size="large" 
                class="custom-type-input"
                @blur="updateCustomApiType"
              />
            </el-form-item>
          </div>
          
          <el-form-item label="接口地址" prop="endpoint">
            <div class="input-group">
              <el-icon class="input-icon"><Link /></el-icon>
              <el-input v-model="formData.endpoint" placeholder="请输入接口地址" size="large" />
            </div>
          </el-form-item>
          
          <div class="form-row">
            <el-form-item label="API Key" prop="appKey" class="form-item">
              <div class="input-group">
                <el-icon class="input-icon"><Key /></el-icon>
                <el-input v-model="formData.appKey" placeholder="请输入API Key" size="large" />
              </div>
            </el-form-item>
            <el-form-item label="API Secret" prop="appSecret" class="form-item">
              <div class="input-group">
                <el-icon class="input-icon"><Lock /></el-icon>
                <el-input v-model="formData.appSecret" type="password" placeholder="请输入API Secret" size="large" show-password />
              </div>
            </el-form-item>
          </div>
          
          <el-form-item label="备注">
            <el-input 
              v-model="formData.remarks" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入备注信息"
              resize="none"
            />
          </el-form-item>
          
          <el-form-item label="状态">
            <div class="status-radio">
              <el-radio-group v-model="formData.status">
                <el-radio :value="1" class="radio-item">
                  <span class="radio-dot active"></span>
                  <span>启用</span>
                </el-radio>
                <el-radio :value="0" class="radio-item">
                  <span class="radio-dot inactive"></span>
                  <span>禁用</span>
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
            {{ isEdit ? '保存修改' : '创建配置' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Plus, Search, Edit, Delete, Setting, Link, Lock, Key, Cpu, Clock, Star, Help } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getAiConfigList, 
  createAiConfig, 
  updateAiConfig, 
  deleteAiConfig,
  toggleAiConfigStatus
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
  configId: null,
  apiName: '',
  apiType: 'AI咨询',
  endpoint: '',
  appKey: '',
  appSecret: '',
  remarks: '',
  status: 1
})

const customApiType = ref('')

const formRules = {
  apiName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  apiType: [{ required: true, message: '请选择接口类型', trigger: 'change' }],
  endpoint: [{ required: true, message: '请输入接口地址', trigger: 'blur' }]
}

const getTypeTag = (type) => {
  const tags = {
    'AI咨询': 'primary',
    '健康评估': 'success',
    '智能推荐': 'warning',
    '其他': 'info'
  }
  return tags[type] || 'info'
}

const getTypeColor = (type) => {
  const colors = {
    'AI咨询': 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
    '健康评估': 'linear-gradient(135deg, #10b981 0%, #34d399 100%)',
    '智能推荐': 'linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%)',
    '其他': 'linear-gradient(135deg, #64748b 0%, #94a3b8 100%)'
  }
  return colors[type] || colors['其他']
}

const getTypeIcon = (type) => {
  const icons = {
    'AI咨询': Cpu,
    '健康评估': Clock,
    '智能推荐': Star,
    '其他': Help
  }
  return icons[type] || Help
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
    const res = await getAiConfigList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword
    })
    tableData.value = res.data.list
    pagination.total = res.data.total
  } catch (error) {
    console.error('获取配置列表失败:', error)
    ElMessage.error('获取配置列表失败')
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
  formData.configId = null
  formData.apiName = ''
  formData.apiType = 'AI咨询'
  formData.endpoint = ''
  formData.appKey = ''
  formData.appSecret = ''
  formData.remarks = ''
  formData.status = 1
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  formData.configId = row.configId
  formData.apiName = row.apiName
  const types = ['AI咨询', '健康评估', '智能推荐']
  if (types.includes(row.apiType)) {
    formData.apiType = row.apiType
    customApiType.value = ''
  } else {
    formData.apiType = '其他'
    customApiType.value = row.apiType
  }
  formData.endpoint = row.endpoint
  formData.appKey = row.appKey
  formData.appSecret = row.appSecret
  formData.remarks = row.remarks
  formData.status = row.status
  dialogVisible.value = true
}

const handleApiTypeChange = (val) => {
  if (val !== '其他') {
    customApiType.value = ''
  }
}

const updateCustomApiType = () => {
  if (customApiType.value && formData.apiType === '其他') {
    formData.apiType = customApiType.value
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这条配置吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteAiConfig(row.configId)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleToggle = async (row) => {
  try {
    await toggleAiConfigStatus(row.configId)
    ElMessage.success(row.status === 1 ? '已禁用' : '已启用')
    loadData()
  } catch (error) {
    ElMessage.error('状态切换失败')
    row.status = row.status === 1 ? 0 : 1
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        await updateAiConfig(formData.configId, formData)
        ElMessage.success('更新成功')
      } else {
        await createAiConfig(formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      submitting.value = false
    }
  })
}

loadData()
</script>

<style scoped>
.ai-config-container {
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

.name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.name-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.name-text {
  font-size: 14px;
  color: #1e293b;
  font-weight: 500;
}

.type-tag {
  flex-shrink: 0;
}

.endpoint-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  
  .link-icon {
    color: #6366f1;
    font-size: 14px;
  }
  
  .endpoint-text {
    font-size: 13px;
    color: #64748b;
    font-family: monospace;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 250px;
  }
}

.mask-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  
  .lock-icon {
    color: #94a3b8;
    font-size: 14px;
  }
  
  .mask-text {
    font-size: 13px;
    color: #94a3b8;
    font-family: monospace;
  }
}

.status-switch {
  display: flex;
  justify-content: center;
}

.remark-text {
  font-size: 13px;
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 150px;
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
  
  .input-group {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 12px;
    background: #f8fafc;
    border-radius: 8px;
    border: 1px solid #e2e8f0;
    
    :deep(.el-input__wrapper) {
      box-shadow: none;
      border: none;
      background: transparent;
    }
    
    .input-icon {
      color: #94a3b8;
      font-size: 16px;
    }
  }
}

.custom-type-input {
  margin-top: 8px;
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

.remark-text {
  font-size: 13px;
  color: #64748b;
  cursor: pointer;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;

  &:hover {
    color: #6366f1;
  }
}

.remark-full {
  font-size: 14px;
  color: #334155;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>