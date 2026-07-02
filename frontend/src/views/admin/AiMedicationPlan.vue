<template>
  <div class="ai-medication-plan-container">
    <PageHeader 
      title="AI用药计划" 
      subtitle="管理AI生成的用药计划，查看详细方案"
      :icon="TrendCharts"
      iconBg="linear-gradient(135deg, #667eea 0%, #764ba2 100%)"
    >
      <template #actions>
        <el-button type="primary" @click="handleAdd" class="add-btn">
          <el-icon><Plus /></el-icon>
          新增计划
        </el-button>
      </template>
    </PageHeader>
    
    <ContentCard>
      <template #header>
        <div class="search-header">
          <div class="search-wrapper">
            <el-input 
              v-model="searchForm.keyword" 
              placeholder="请输入计划名称或疾病类型搜索" 
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
          <div class="filter-wrapper">
            <el-radio-group v-model="searchForm.status" @change="loadData">
              <el-radio :value="null">全部</el-radio>
              <el-radio :value="1">启用中</el-radio>
              <el-radio :value="0">已停用</el-radio>
            </el-radio-group>
          </div>
        </div>
      </template>
      
      <div class="table-wrapper">
        <el-table :data="tableData" style="width: 100%" v-loading="loading" class="custom-table">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="planName" label="计划名称" min-width="180">
            <template #default="{ row }">
              <div class="name-cell">
                <div class="name-icon" :style="{ background: getDiseaseColor(row.diseaseType) }">
                  <el-icon :size="16"><component :is="getDiseaseIcon(row.diseaseType)" /></el-icon>
                </div>
                <span class="name-text">{{ row.planName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="diseaseType" label="疾病类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getDiseaseTag(row.diseaseType)" size="small" class="type-tag">
                {{ row.diseaseType || '-' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="medicationList" label="用药清单" min-width="200">
            <template #default="{ row }">
              <el-button 
                v-if="row.medicationList"
                size="small" 
                type="primary" 
                @click="handleViewMedication(row)"
              >
                <el-icon><Key /></el-icon>
                查看清单
              </el-button>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="startDate" label="开始日期" width="120" />
          <el-table-column prop="endDate" label="结束日期" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <div class="status-switch">
                <template v-if="row.status === 2">
                  <el-tag type="danger" size="small">已禁用</el-tag>
                </template>
                <el-switch 
                  v-else
                  v-model="row.status" 
                  :active-value="1"
                  :inactive-value="0"
                  @change="(val) => handleToggle(row, val)"
                  active-text="启用"
                  inactive-text="停用"
                />
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="{ row }">
              <span>{{ formatDate(row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="handleView(row)">查看</el-button>
              <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
          class="custom-pagination"
        />
      </div>
    </ContentCard>

    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="600px"
      :close-on-click-modal="false"
      :draggable="true"
      class="custom-dialog"
    >
      <el-form :model="form" label-width="120px" class="dialog-form">
        <el-form-item label="用户ID">
          <el-input v-model.number="form.userId" type="number" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="计划名称" required>
          <el-input v-model="form.planName" placeholder="请输入计划名称" />
        </el-form-item>
        <el-form-item label="疾病类型">
          <el-select v-model="form.diseaseType" placeholder="请选择或输入疾病类型" filterable allow-create>
            <el-option label="高血压" value="高血压" />
            <el-option label="糖尿病" value="糖尿病" />
            <el-option label="冠心病" value="冠心病" />
            <el-option label="高血脂" value="高血脂" />
            <el-option label="哮喘" value="哮喘" />
            <el-option label="类风湿关节炎" value="类风湿关节炎" />
            <el-option label="慢性阻塞性肺疾病" value="慢性阻塞性肺疾病" />
            <el-option label="肾病综合征" value="肾病综合征" />
            <el-option label="甲状腺功能亢进" value="甲状腺功能亢进" />
            <el-option label="痛风" value="痛风" />
          </el-select>
        </el-form-item>
        <el-form-item label="用药清单">
          <el-input v-model="form.medicationList" type="textarea" :rows="3" placeholder="请输入用药清单" />
        </el-form-item>
        <el-form-item label="用药说明">
          <el-input v-model="form.dosageInstructions" type="textarea" :rows="3" placeholder="请输入用药说明" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="form.startDate" type="date" placeholder="请选择开始日期" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="form.endDate" type="date" placeholder="请选择结束日期" />
        </el-form-item>
        <el-form-item label="AI分析">
          <el-input v-model="form.aiAnalysis" type="textarea" :rows="4" placeholder="请输入AI分析内容" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog 
      v-model="viewVisible" 
      title="计划详情" 
      width="700px"
      :close-on-click-modal="false"
      :draggable="true"
      class="custom-dialog"
    >
      <div v-if="viewData" class="view-content">
        <div class="view-row">
          <span class="view-label">计划名称：</span>
          <span class="view-value">{{ viewData.planName }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">疾病类型：</span>
          <span class="view-value">{{ viewData.diseaseType || '-' }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">用药清单：</span>
          <span class="view-value">{{ viewData.medicationList || '-' }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">用药说明：</span>
          <span class="view-value">{{ viewData.dosageInstructions || '-' }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">开始日期：</span>
          <span class="view-value">{{ viewData.startDate || '-' }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">结束日期：</span>
          <span class="view-value">{{ viewData.endDate || '-' }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">AI分析：</span>
          <span class="view-value">{{ viewData.aiAnalysis || '-' }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">状态：</span>
          <span class="view-value">{{ viewData.status === 1 ? '启用中' : '已停用' }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">创建时间：</span>
          <span class="view-value">{{ viewData.createTime || '-' }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="viewVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog 
      v-model="medicationVisible" 
      title="用药清单详情" 
      width="500px"
      :close-on-click-modal="false"
      :draggable="true"
      class="custom-dialog"
    >
      <div v-if="medicationList.length > 0" class="medication-list">
        <div v-for="(item, index) in medicationList" :key="index" class="medication-item">
          <div class="item-header">
            <el-icon class="item-icon"><Key /></el-icon>
            <span class="item-name">{{ item.name }}</span>
          </div>
          <div class="item-details">
            <div class="detail-row">
              <span class="detail-label">剂量：</span>
              <span class="detail-value">{{ item.dose || '-' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">服用时间：</span>
              <span class="detail-value">{{ item.time || '-' }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-tip">暂无用药信息</div>
      <template #footer>
        <el-button @click="medicationVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Search, TrendCharts, Clock, Star, Medal, User, CircleCheck, Key } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import ContentCard from '@/components/ContentCard.vue'
import { aiCenterApi } from '@/api/aiCenter'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const viewVisible = ref(false)
const medicationVisible = ref(false)
const dialogTitle = ref('')
const editId = ref(null)
const viewData = ref(null)
const medicationList = ref([])

const searchForm = reactive({
  keyword: '',
  status: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  userId: null,
  planName: '',
  diseaseType: '',
  medicationList: '',
  dosageInstructions: '',
  startDate: '',
  endDate: '',
  status: 1,
  aiAnalysis: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await aiCenterApi.getMedicationPlanList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status
    })
    if (res.code === 200) {
      tableData.value = res.data.list
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增AI用药计划'
  editId.value = null
  Object.assign(form, {
    userId: null,
    planName: '',
    diseaseType: '',
    medicationList: '',
    dosageInstructions: '',
    startDate: '',
    endDate: '',
    status: 1,
    aiAnalysis: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑AI用药计划'
  editId.value = row.id
  Object.assign(form, {
    userId: row.userId,
    planName: row.planName,
    diseaseType: row.diseaseType,
    medicationList: row.medicationList,
    dosageInstructions: row.dosageInstructions,
    startDate: row.startDate,
    endDate: row.endDate,
    status: row.status,
    aiAnalysis: row.aiAnalysis
  })
  dialogVisible.value = true
}

const handleView = async (row) => {
  try {
    const res = await aiCenterApi.getMedicationPlanDetail(row.id)
    if (res.code === 200) {
      viewData.value = res.data
      viewVisible.value = true
    }
  } catch (error) {
    console.error('查看详情失败:', error)
  }
}

const handleSubmit = async () => {
  try {
    if (editId.value) {
      await aiCenterApi.updateMedicationPlan(editId.value, form)
    } else {
      await aiCenterApi.createMedicationPlan(form)
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

const handleToggle = async (row, val) => {
  try {
    await aiCenterApi.updateMedicationPlan(row.id, { status: val ? 1 : 0 })
    loadData()
  } catch (error) {
    console.error('切换状态失败:', error)
  }
}

const handleViewMedication = (row) => {
  try {
    medicationList.value = JSON.parse(row.medicationList)
    medicationVisible.value = true
  } catch (error) {
    medicationList.value = [{ name: row.medicationList, dose: '', time: '' }]
    medicationVisible.value = true
  }
}

const handleDelete = async (row) => {
  try {
    await aiCenterApi.deleteMedicationPlan(row.id)
    loadData()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const getDiseaseColor = (type) => {
  const colors = {
    '高血压': 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    '糖尿病': 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    '冠心病': 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    '高血脂': 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    '其他': 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)'
  }
  return colors[type] || colors['其他']
}

const getDiseaseIcon = (type) => {
  const icons = {
    '高血压': Clock,
    '糖尿病': Medal,
    '冠心病': Star,
    '高血脂': CircleCheck
  }
  return icons[type] || User
}

const getDiseaseTag = (type) => {
  const tags = {
    '高血压': 'danger',
    '糖尿病': 'primary',
    '冠心病': 'success',
    '高血脂': 'warning'
  }
  return tags[type] || 'info'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.ai-medication-plan-container {
  padding: 24px;
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.search-wrapper {
  display: flex;
  gap: 12px;
}

.search-input {
  width: 280px;
}

.search-btn {
  white-space: nowrap;
}

.filter-wrapper {
  display: flex;
  gap: 8px;
}

.table-wrapper {
  margin-bottom: 16px;
}

.custom-table {
  --el-table-header-text-color: #666;
  --el-table-header-bg-color: #fafafa;
}

.name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.name-text {
  font-weight: 500;
}

.type-tag {
  font-size: 12px;
}

.medication-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.pill-icon {
  color: #4facfe;
}

.medication-text {
  color: #4facfe;
  font-size: 13px;
}

.status-switch {
  display: flex;
  justify-content: center;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}

.custom-pagination {
  --el-pagination-text-color: #666;
}

.dialog-form {
  padding-top: 16px;
}

.view-content {
  padding: 16px;
}

.view-row {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.view-label {
  width: 120px;
  color: #999;
  font-weight: 500;
}

.view-value {
  flex: 1;
  color: #333;
  word-break: break-all;
}

.medication-list {
  padding: 8px;
}

.medication-item {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.medication-item:last-child {
  margin-bottom: 0;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.item-icon {
  color: #4facfe;
}

.item-name {
  font-weight: 600;
  font-size: 14px;
}

.item-details {
  padding-left: 24px;
}

.detail-row {
  display: flex;
  margin-bottom: 6px;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-label {
  width: 80px;
  color: #999;
  font-size: 13px;
}

.detail-value {
  flex: 1;
  color: #333;
  font-size: 13px;
}

.empty-tip {
  text-align: center;
  color: #999;
  padding: 40px;
}
</style>