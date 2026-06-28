<template>
  <div class="medical-record-page">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form inline>
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keyword" placeholder="搜索疾病名称/医生/医院" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="searchForm.startDate" type="date" placeholder="选择开始日期"
            value-format="YYYY-MM-DD" style="width: 160px" clearable />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="searchForm.endDate" type="date" placeholder="选择结束日期"
            value-format="YYYY-MM-DD" style="width: 160px" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="margin-left: auto">
          <el-button type="success" @click="openAddDialog">+ 新增病历</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 病历列表 -->
    <el-card class="table-card">
      <el-table :data="records" stripe v-loading="loading">
        <el-table-column prop="recordId" label="ID" width="70" />
        <el-table-column prop="recordDate" label="日期" width="110">
          <template #default="{ row }">{{ row.recordDate || '-' }}</template>
        </el-table-column>
        <el-table-column prop="diagnosis" label="诊断" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.diagnosis || '-' }}</template>
        </el-table-column>
        <el-table-column prop="treatment" label="处理意见" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.treatment || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openDetailDialog(row)">查看详情</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          :page-size="pagination.size"
          :total="pagination.total"
          layout="total, prev, pager, next, jumper"
          @current-change="fetchRecords"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close>
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="日期" prop="recordDate">
          <el-date-picker v-model="formData.recordDate" type="date" placeholder="选择日期"
            value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="formData.gender" placeholder="选择性别" clearable style="width: 100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="formData.age" :min="0" :max="150" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="主诉" prop="chiefComplaint">
          <el-input v-model="formData.chiefComplaint" type="textarea" :rows="2" placeholder="请输入主诉" />
        </el-form-item>
        <el-form-item label="现病史" prop="presentHistory">
          <el-input v-model="formData.presentHistory" type="textarea" :rows="3" placeholder="请输入现病史" />
        </el-form-item>
        <el-form-item label="既往病史" prop="pastHistory">
          <el-input v-model="formData.pastHistory" type="textarea" :rows="2" placeholder="请输入既往病史" />
        </el-form-item>
        <el-form-item label="诊断" prop="diagnosis">
          <el-input v-model="formData.diagnosis" type="textarea" :rows="2" placeholder="请输入诊断" />
        </el-form-item>
        <el-form-item label="处理意见" prop="treatment">
          <el-input v-model="formData.treatment" type="textarea" :rows="3" placeholder="请输入处理意见" />
        </el-form-item>
        <el-divider content-position="left">智能导入</el-divider>
        <el-form-item>
          <div class="ocr-result">
            <div class="ocr-label">智能导入（上传图片，自动识别文字并智能填充字段）：</div>
            <div style="margin-top: 8px">
              <el-upload
                ref="uploadRef"
                :auto-upload="false"
                :limit="1"
                accept="image/*"
                :on-change="handleFileChange"
                :show-file-list="false"
              >
                <el-button type="info">📷 上传图片</el-button>
              </el-upload>
              <el-button type="primary" style="margin-left: 8px" :disabled="!selectedFile" :loading="aiParsing" @click="handleOcrUpload">
                🔍 智能识别填充
              </el-button>
            </div>
          </div>
        </el-form-item>
        <!-- 字段识别详情 -->
        <el-form-item v-if="ocrFieldInfo.length > 0">
          <div class="ocr-field-info">
            <div class="ocr-field-label">🔍 字段识别详情：</div>
            <el-table :data="ocrFieldInfo" size="small" stripe style="width: 100%">
              <el-table-column prop="field" label="字段" width="100" />
              <el-table-column prop="value" label="识别值" min-width="200" />
              <el-table-column prop="score" label="置信度" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.score >= 0.85 ? 'success' : row.score >= 0.65 ? 'warning' : 'danger'" size="small">
                    {{ (row.score * 100).toFixed(0) }}%
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="source" label="来源" width="80">
                <template #default="{ row }">
                  <el-tag type="info" size="small">{{ row.source }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗（只读） -->
    <el-dialog v-model="detailVisible" title="病历详情" width="700px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="日期" :span="1">{{ detailData.recordDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别" :span="1">{{ detailData.gender || '-' }}</el-descriptions-item>
        <el-descriptions-item label="年龄" :span="1">{{ detailData.age ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="主诉" :span="2">{{ detailData.chiefComplaint || '-' }}</el-descriptions-item>
        <el-descriptions-item label="现病史" :span="2">{{ detailData.presentHistory || '-' }}</el-descriptions-item>
        <el-descriptions-item label="既往病史" :span="2">{{ detailData.pastHistory || '-' }}</el-descriptions-item>
        <el-descriptions-item label="诊断" :span="2">{{ detailData.diagnosis || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理意见" :span="2">{{ detailData.treatment || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 删除确认弹窗 -->
    <el-dialog v-model="deleteDialogVisible" title="删除病历" width="520px">
      <el-alert type="warning" :closable="false" style="margin-bottom: 16px">
        <template #title>
          确定要删除该病历吗？此操作不可恢复！
        </template>
      </el-alert>
      <div v-if="recordToDelete && recordToDelete.plans && recordToDelete.plans.length > 0" class="cascade-warning">
        <el-icon color="#E6A23C"><WarningFilled /></el-icon>
        <span>该病历下有 <strong>{{ recordToDelete.plans.length }}</strong> 条用药计划，删除病历时将一并删除！</span>
      </div>
      <div v-if="recordToDelete" class="record-summary">
        <p><strong>日期：</strong>{{ recordToDelete.recordDate || '-' }}</p>
        <p><strong>诊断：</strong>{{ recordToDelete.diagnosis || '-' }}</p>
      </div>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDelete" :loading="deleting">确认删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'
import {
  getRecordsPage,
  createRecord,
  deleteRecordWithPlans,
  ocrRecognize
} from '@/api/record'
import { getPlansByRecordId } from '@/api/plan'

const USER_ID = 1

const loading = ref(false)
const submitting = ref(false)
const deleting = ref(false)
const aiParsing = ref(false)
const uploadRef = ref()
const formRef = ref()

const records = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })
const searchForm = reactive({ keyword: '', startDate: '', endDate: '' })

const dialogVisible = ref(false)
const dialogTitle = ref('新增病历')
const formData = reactive({
  recordId: null,
  recordDate: '',
  gender: '',
  age: null,
  chiefComplaint: '',
  presentHistory: '',
  pastHistory: '',
  diagnosis: '',
  treatment: ''
})
const selectedFile = ref(null)
const ocrRawText = ref('')
const ocrFieldInfo = ref([])

const deleteDialogVisible = ref(false)
const recordToDelete = ref(null)

const detailVisible = ref(false)
const detailData = reactive({
  recordId: null,
  recordDate: '',
  gender: '',
  age: null,
  chiefComplaint: '',
  presentHistory: '',
  pastHistory: '',
  diagnosis: '',
  treatment: ''
})

const formRules = {
  // 所有字段非必填
}

onMounted(() => {
  fetchRecords()
})

async function fetchRecords() {
  loading.value = true
  try {
    const res = await getRecordsPage(USER_ID, {
      keyword: searchForm.keyword,
      startDate: searchForm.startDate || null,
      endDate: searchForm.endDate || null,
      page: pagination.page - 1,
      size: pagination.size
    })
    records.value = res.data.content || res.data || []
    pagination.total = res.data.totalElements || 0
  } catch (e) {
    ElMessage.error('加载病历列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchRecords()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.startDate = ''
  searchForm.endDate = ''
  pagination.page = 1
  fetchRecords()
}

function openAddDialog() {
  dialogTitle.value = '新增病历'
  resetForm()
  dialogVisible.value = true
}

function openDetailDialog(row) {
  Object.assign(detailData, {
    recordId: row.recordId,
    recordDate: row.recordDate,
    gender: row.gender || '',
    age: row.age ?? null,
    chiefComplaint: row.chiefComplaint || '',
    presentHistory: row.presentHistory || '',
    pastHistory: row.pastHistory || '',
    diagnosis: row.diagnosis || '',
    treatment: row.treatment || ''
  })
  detailVisible.value = true
}

function resetForm() {
  Object.assign(formData, {
    recordId: null,
    recordDate: '',
    gender: '',
    age: null,
    chiefComplaint: '',
    presentHistory: '',
    pastHistory: '',
    diagnosis: '',
    treatment: ''
  })
  selectedFile.value = null
  ocrRawText.value = ''
  ocrFieldInfo.value = []
  formRef.value?.clearValidate()
}

function handleFileChange(file) {
  selectedFile.value = file.raw
}

async function handleOcrUpload() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择图片')
    return
  }
  const fd = new FormData()
  fd.append('file', selectedFile.value)
  aiParsing.value = true
  try {
    const res = await ocrRecognize(fd)
    // 新响应格式：res.data = { record, rawText, fields, scores, sources, needCorrection }
    const d = res.data
    const p = d.record || d

    // 显示OCR原文
    if (d.rawText) ocrRawText.value = d.rawText

    // 显示字段识别详情（只显示8个核心字段）
    if (d.fields && d.scores) {
      const CORE_FIELDS = ['日期', '性别', '年龄', '主诉', '现病史', '既往史', '诊断', '处理意见']
      const info = []
      for (const field of CORE_FIELDS) {
        if (d.fields[field]) {
          info.push({
            field,
            value: d.fields[field],
            score: d.scores[field] || 0,
            source: (d.sources && d.sources[field]) || '-'
          })
        }
      }
      ocrFieldInfo.value = info
    }

    // 填充表单（优先用record已映射字段，缺失的从raw fields找）
    if (p.recordDate) formData.recordDate = p.recordDate
    if (p.gender) formData.gender = p.gender
    if (p.age !== null && p.age !== undefined) formData.age = p.age
    if (p.chiefComplaint) formData.chiefComplaint = p.chiefComplaint
    if (p.presentHistory) formData.presentHistory = p.presentHistory
    if (p.pastHistory) formData.pastHistory = p.pastHistory
    if (p.diagnosis) formData.diagnosis = p.diagnosis
    if (p.treatment) formData.treatment = p.treatment

    // 从原始fields补填缺失字段
    if (d.fields) {
      const fields = d.fields
      if (!formData.gender) formData.gender = fields['性别'] || ''
      if (formData.age === null || formData.age === undefined) {
        const ageStr = fields['年龄'] || ''
        const m = ageStr.match(/\d+/)
        formData.age = m ? parseInt(m[0]) : null
      }
      if (!formData.chiefComplaint) formData.chiefComplaint = fields['主诉'] || ''
      if (!formData.presentHistory) formData.presentHistory = fields['现病史'] || ''
      if (!formData.pastHistory) formData.pastHistory = fields['既往史'] || ''
      if (!formData.diagnosis) formData.diagnosis = fields['诊断'] || ''
      if (!formData.treatment) formData.treatment = fields['处理意见'] || ''
    }

    ElMessage.success('智能识别成功，请检查并修改后保存')
  } catch (e) {
    ElMessage.error('识别失败: ' + (e.message || '请重试'))
  } finally {
    aiParsing.value = false
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = {
      userId: USER_ID,
      recordDate: formData.recordDate || null,
      gender: formData.gender,
      age: formData.age,
      chiefComplaint: formData.chiefComplaint,
      presentHistory: formData.presentHistory,
      pastHistory: formData.pastHistory,
      diagnosis: formData.diagnosis,
      treatment: formData.treatment,
      diseaseName: formData.diagnosis || formData.chiefComplaint || '未知疾病'
    }

    const res = await createRecord(payload)
    if (res.code === 200) {
      ElMessage.success('创建成功')
      dialogVisible.value = false
      fetchRecords()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    if (e.name !== 'ValidationError') ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    // 先弹窗确认
    await ElMessageBox.confirm(
      `确定要删除「${row.diseaseName}」病历吗？`,
      '确认删除',
      { type: 'warning' }
    )

    // 用户确认后，获取关联计划数
    const plansRes = await getPlansByRecordId(row.recordId)
    const plans = plansRes.data || []

    if (plans.length > 0) {
      // 有关联计划，显示二次确认弹窗
      recordToDelete.value = { ...row, plans }
      deleteDialogVisible.value = true
    } else {
      // 无关联计划，直接级联删除
      const res = await deleteRecordWithPlans(row.recordId)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchRecords()
        window.dispatchEvent(new CustomEvent('refresh-medication-plans'))
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    }
  } catch {
    // 用户取消或操作失败
  }
}

async function confirmDelete() {
  if (!recordToDelete.value) return
  deleting.value = true
  try {
    const res = await deleteRecordWithPlans(recordToDelete.value.recordId)
    if (res.code === 200) {
      ElMessage.success(`删除成功，同时删除了 ${res.data.deletedPlans} 条用药计划`)
      deleteDialogVisible.value = false
      fetchRecords()
      window.dispatchEvent(new CustomEvent('refresh-medication-plans'))
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {
    ElMessage.error('删除失败')
  } finally {
    deleting.value = false
  }
}
</script>

<style lang="scss" scoped>
.medical-record-page {
  padding: 20px;
  .search-card {
    margin-bottom: 16px;
  }
  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
  .upload-tip {
    margin-left: 8px;
    color: #999;
    font-size: 12px;
  }
  .ocr-result {
    width: 100%;
    .ocr-label {
      margin-bottom: 4px;
      color: #666;
      font-size: 13px;
    }
  }
  .cascade-warning {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    background: #fdf6ec;
    border-radius: 4px;
    margin-bottom: 12px;
    color: #e6a23c;
  }
  .record-summary {
    background: #f5f7fa;
    padding: 12px;
    border-radius: 4px;
    p {
      margin: 4px 0;
      font-size: 14px;
    }
  }
  .ocr-raw-text {
    width: 100%;
    .ocr-raw-label {
      margin-bottom: 4px;
      color: #666;
      font-size: 13px;
    }
  }
  .ocr-field-info {
    width: 100%;
    .ocr-field-label {
      margin-bottom: 4px;
      color: #666;
      font-size: 13px;
    }
  }
}
</style>